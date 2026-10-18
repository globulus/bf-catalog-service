package com.shopfront.catalog.repository;

import com.shopfront.catalog.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    @Query("""
            select p from Product p
            where (:categoryId is null or p.category.id = :categoryId)
              and (:activeOnly = false or p.active = true)
              and (:q is null or lower(p.name) like lower(concat('%', cast(:q as string), '%'))
                   or lower(p.sku) like lower(concat('%', cast(:q as string), '%')))
            """)
    Page<Product> search(@Param("categoryId") Long categoryId,
                         @Param("q") String q,
                         @Param("activeOnly") boolean activeOnly,
                         Pageable pageable);
}
