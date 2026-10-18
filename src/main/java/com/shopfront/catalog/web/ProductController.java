package com.shopfront.catalog.web;

import com.shopfront.catalog.service.CatalogService;
import com.shopfront.catalog.web.dto.ProductDtos.CreatePriceRequest;
import com.shopfront.catalog.web.dto.ProductDtos.CreateProductRequest;
import com.shopfront.catalog.web.dto.ProductDtos.PriceResponse;
import com.shopfront.catalog.web.dto.ProductDtos.ProductPage;
import com.shopfront.catalog.web.dto.ProductDtos.ProductResponse;
import com.shopfront.catalog.web.dto.ProductDtos.UpdateProductRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Products")
public class ProductController {

    private final CatalogService catalogService;

    public ProductController(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    @GetMapping
    @Operation(summary = "Search products", description = "Sorted by name. Only active products unless activeOnly=false.")
    public ProductPage search(@RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) String q,
                              @RequestParam(defaultValue = "true") boolean activeOnly,
                              @RequestParam(defaultValue = "0") int page,
                              @RequestParam(defaultValue = "20") int pageSize) {
        return catalogService.search(categoryId, q, activeOnly, page, pageSize);
    }

    @GetMapping("/{id}")
    public ProductResponse get(@PathVariable Long id) {
        return catalogService.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse create(@Valid @RequestBody CreateProductRequest request) {
        return catalogService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse update(@PathVariable Long id, @Valid @RequestBody UpdateProductRequest request) {
        return catalogService.update(id, request);
    }

    @GetMapping("/{id}/prices")
    @Operation(summary = "Price history, newest first")
    public List<PriceResponse> prices(@PathVariable Long id) {
        return catalogService.priceHistory(id);
    }

    @PostMapping("/{id}/prices")
    @ResponseStatus(HttpStatus.CREATED)
    public PriceResponse addPrice(@PathVariable Long id, @Valid @RequestBody CreatePriceRequest request) {
        return catalogService.addPrice(id, request);
    }
}
