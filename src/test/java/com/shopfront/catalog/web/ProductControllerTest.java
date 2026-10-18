package com.shopfront.catalog.web;

import com.shopfront.catalog.service.CatalogService;
import com.shopfront.catalog.service.NotFoundException;
import com.shopfront.catalog.web.dto.ProductDtos.Money;
import com.shopfront.catalog.web.dto.ProductDtos.ProductPage;
import com.shopfront.catalog.web.dto.ProductDtos.ProductResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
class ProductControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockBean
    private CatalogService catalogService;

    @Test
    void getReturnsProductWithCurrentPrice() throws Exception {
        when(catalogService.get(42L)).thenReturn(new ProductResponse(42L, "BEAN-1KG", "Espresso beans 1kg",
                "Dark roast", 3L, true, new Money(new BigDecimal("18.90"), "EUR")));

        mvc.perform(get("/api/products/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("BEAN-1KG"))
                .andExpect(jsonPath("$.price.amount").value(18.90))
                .andExpect(jsonPath("$.price.currency").value("EUR"));
    }

    @Test
    void unknownProductIs404WithErrorBody() throws Exception {
        when(catalogService.get(7L)).thenThrow(new NotFoundException("Product", 7L));

        mvc.perform(get("/api/products/7"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"))
                .andExpect(jsonPath("$.message").value("Product 7 not found"));
    }

    @Test
    void searchPassesFiltersThrough() throws Exception {
        when(catalogService.search(3L, "bean", true, 0, 20))
                .thenReturn(new ProductPage(List.of(), 0, 0, 20));

        mvc.perform(get("/api/products").param("categoryId", "3").param("q", "bean"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(0))
                .andExpect(jsonPath("$.pageSize").value(20));
    }

    @Test
    void createRejectsLowercaseCurrency() throws Exception {
        mvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sku": "MUG-01", "name": "Mug", "price": {"amount": 8.00, "currency": "eur"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("validation_failed"));

        verify(catalogService, never()).create(any());
    }
}
