package com.shopfront.catalog.service;

import com.shopfront.catalog.model.Category;
import com.shopfront.catalog.model.Price;
import com.shopfront.catalog.model.Product;
import com.shopfront.catalog.repository.CategoryRepository;
import com.shopfront.catalog.repository.PriceRepository;
import com.shopfront.catalog.repository.ProductRepository;
import com.shopfront.catalog.web.dto.ProductDtos.CreatePriceRequest;
import com.shopfront.catalog.web.dto.ProductDtos.CreateProductRequest;
import com.shopfront.catalog.web.dto.ProductDtos.Money;
import com.shopfront.catalog.web.dto.ProductDtos.PriceResponse;
import com.shopfront.catalog.web.dto.ProductDtos.ProductPage;
import com.shopfront.catalog.web.dto.ProductDtos.ProductResponse;
import com.shopfront.catalog.web.dto.ProductDtos.UpdateProductRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
public class CatalogService {

    private final ProductRepository products;
    private final CategoryRepository categories;
    private final PriceRepository prices;

    public CatalogService(ProductRepository products, CategoryRepository categories, PriceRepository prices) {
        this.products = products;
        this.categories = categories;
        this.prices = prices;
    }

    @Transactional(readOnly = true)
    public ProductPage search(Long categoryId, String q, boolean activeOnly, int page, int pageSize) {
        String query = (q == null || q.isBlank()) ? null : q.trim();
        PageRequest pageable = PageRequest.of(Math.max(page, 0), Math.min(Math.max(pageSize, 1), 200), Sort.by("name"));
        Page<Product> result = products.search(categoryId, query, activeOnly, pageable);
        Instant now = Instant.now();
        // TODO: one price query per product. Fine for a page of 20, not for the export job.
        List<ProductResponse> items = result.getContent().stream().map(p -> toResponse(p, now)).toList();
        return new ProductPage(items, result.getTotalElements(), result.getNumber(), result.getSize());
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return toResponse(find(id), Instant.now());
    }

    public ProductResponse create(CreateProductRequest request) {
        String sku = request.sku().trim().toUpperCase();
        if (products.existsBySku(sku)) {
            throw new DuplicateException("SKU " + sku + " already exists");
        }
        Product product = products.save(new Product(sku, request.name().trim(), request.description(),
                findCategory(request.categoryId())));
        Instant now = Instant.now();
        if (request.price() != null) {
            prices.save(new Price(product, request.price().amount(), request.price().currency(), now));
        }
        return toResponse(product, now);
    }

    public ProductResponse update(Long id, UpdateProductRequest request) {
        Product product = find(id);
        product.setName(request.name().trim());
        product.setDescription(request.description());
        product.setCategory(findCategory(request.categoryId()));
        product.setActive(request.active());
        return toResponse(product, Instant.now());
    }

    @Transactional(readOnly = true)
    public List<PriceResponse> priceHistory(Long productId) {
        find(productId);
        return prices.findByProductIdOrderByValidFromDesc(productId).stream().map(CatalogService::toPriceResponse).toList();
    }

    /** Adds a price from {@code validFrom} (default now) and closes the open one at that moment. */
    public PriceResponse addPrice(Long productId, CreatePriceRequest request) {
        Product product = find(productId);
        Instant validFrom = request.validFrom() != null ? request.validFrom() : Instant.now();
        prices.findFirstByProductIdAndValidToIsNullOrderByValidFromDesc(productId)
                .filter(open -> open.getValidFrom().isBefore(validFrom))
                .ifPresent(open -> open.setValidTo(validFrom));
        Price price = prices.save(new Price(product, request.amount(), request.currency(), validFrom));
        return toPriceResponse(price);
    }

    private ProductResponse toResponse(Product p, Instant at) {
        Money price = prices.findValidAt(p.getId(), at)
                .map(pr -> new Money(pr.getAmount(), pr.getCurrency()))
                .orElse(null);
        Long categoryId = p.getCategory() == null ? null : p.getCategory().getId();
        return new ProductResponse(p.getId(), p.getSku(), p.getName(), p.getDescription(), categoryId, p.isActive(), price);
    }

    private static PriceResponse toPriceResponse(Price pr) {
        return new PriceResponse(pr.getId(), pr.getAmount(), pr.getCurrency(), pr.getValidFrom(), pr.getValidTo());
    }

    private Product find(Long id) {
        return products.findById(id).orElseThrow(() -> new NotFoundException("Product", id));
    }

    private Category findCategory(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return categories.findById(categoryId).orElseThrow(() -> new NotFoundException("Category", categoryId));
    }
}
