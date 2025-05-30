package com.mylearning.inventoryservice.repository;

import com.mylearning.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Optional<Inventory> findByProductCode(String productCode);
    List<Inventory> findByProductCodeIn(List<String> productCodes);
    void deleteByProductCode(String productCode);
    boolean existsByProductCode(String productCode);
}