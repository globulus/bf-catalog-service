package com.shopfront.catalog.service;

import com.shopfront.catalog.model.Category;
import com.shopfront.catalog.repository.CategoryRepository;
import com.shopfront.catalog.web.dto.ProductDtos.CreatePriceRequest;
import com.shopfront.catalog.web.dto.ProductDtos.CreateProductRequest;
import com.shopfront.catalog.web.dto.ProductDtos.Money;
import com.shopfront.catalog.web.dto.ProductDtos.PriceResponse;
import com.shopfront.catalog.web.dto.ProductDtos.ProductPage;
import com.shopfront.catalog.web.dto.ProductDtos.ProductResponse;
import com.shopfront.catalog.web.dto.ProductDtos.UpdateProductRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class CatalogServiceTest {

    @Autowired
    CatalogService catalogService;

    @Autowired
    CategoryRepository categoryRepository;

    @Test
    void createWithPriceReturnsCurrentPrice() {
        ProductResponse created = catalogService.create(new CreateProductRequest("mug-01", "Mug", null, null,
                new Money(new BigDecimal("8.00"), "EUR")));

        assertThat(created.sku()).isEqualTo("MUG-01");
        assertThat(created.price().amount()).isEqualByComparingTo("8.00");
        assertThat(catalogService.get(created.id()).price().currency()).isEqualTo("EUR");
    }

    @Test
    void duplicateSkuIsRejected() {
        catalogService.create(new CreateProductRequest("DUP-1", "First", null, null, null));

        assertThatThrownBy(() -> catalogService.create(new CreateProductRequest("dup-1", "Second", null, null, null)))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    void productWithoutPriceHasNullPrice() {
        ProductResponse created = catalogService.create(new CreateProductRequest("NOPRICE-1", "Sample", null, null, null));

        assertThat(created.price()).isNull();
    }

    @Test
    void addingPriceClosesThePreviousOne() {
        ProductResponse product = catalogService.create(new CreateProductRequest("KETTLE-1", "Kettle", null, null,
                new Money(new BigDecimal("49.00"), "EUR")));
        Instant later = Instant.now().plus(1, ChronoUnit.SECONDS);

        catalogService.addPrice(product.id(), new CreatePriceRequest(new BigDecimal("44.00"), "EUR", later));

        List<PriceResponse> history = catalogService.priceHistory(product.id());
        assertThat(history).hasSize(2);
        assertThat(history.get(0).amount()).isEqualByComparingTo("44.00");
        assertThat(history.get(0).validTo()).isNull();
        assertThat(history.get(1).validTo()).isEqualTo(later);
    }

    @Test
    void searchFiltersByCategoryAndText() {
        Category grinders = categoryRepository.save(new Category("Grinders", "grinders-test"));
        catalogService.create(new CreateProductRequest("GR-100", "Hand grinder", null, grinders.getId(), null));
        catalogService.create(new CreateProductRequest("GR-200", "Electric grinder", null, grinders.getId(), null));
        catalogService.create(new CreateProductRequest("TMP-1", "Tamper", null, null, null));

        ProductPage byCategory = catalogService.search(grinders.getId(), null, true, 0, 20);
        ProductPage byText = catalogService.search(null, "electric", true, 0, 20);

        assertThat(byCategory.items()).extracting(ProductResponse::name)
                .containsExactly("Electric grinder", "Hand grinder");
        assertThat(byText.items()).extracting(ProductResponse::sku).containsExactly("GR-200");
    }

    @Test
    void inactiveProductsAreHiddenByDefault() {
        ProductResponse old = catalogService.create(new CreateProductRequest("OLD-9", "Discontinued thing", null, null, null));
        catalogService.update(old.id(), new UpdateProductRequest("Discontinued thing", null, null, false));

        assertThat(catalogService.search(null, "discontinued", true, 0, 20).items()).isEmpty();
        assertThat(catalogService.search(null, "discontinued", false, 0, 20).items()).hasSize(1);
    }

    @Test
    void unknownCategoryIsNotFound() {
        assertThatThrownBy(() -> catalogService.create(new CreateProductRequest("X-1", "X", null, 9999L, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Category 9999 not found");
    }
}
