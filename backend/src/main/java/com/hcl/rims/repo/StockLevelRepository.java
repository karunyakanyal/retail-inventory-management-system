package com.hcl.rims.repo;

import com.hcl.rims.model.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {
    List<StockLevel> findByProductId(Long productId);
    List<StockLevel> findByLocation(String location);
    Optional<StockLevel> findByProductIdAndLocation(Long productId, String location);

    @Query("SELECT s FROM StockLevel s WHERE s.qty <= s.reorderPoint")
    List<StockLevel> findLowStock();
}
