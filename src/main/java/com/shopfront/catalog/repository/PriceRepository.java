package com.shopfront.catalog.repository;

import com.shopfront.catalog.model.Price;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface PriceRepository extends JpaRepository<Price, Long> {

    List<Price> findByProductIdOrderByValidFromDesc(Long productId);

    @Query("""
            select pr from Price pr
            where pr.product.id = :productId
              and pr.validFrom <= :at
              and (pr.validTo is null or pr.validTo > :at)
            order by pr.validFrom desc
            limit 1
            """)
    Optional<Price> findValidAt(@Param("productId") Long productId, @Param("at") Instant at);

    Optional<Price> findFirstByProductIdAndValidToIsNullOrderByValidFromDesc(Long productId);
}
