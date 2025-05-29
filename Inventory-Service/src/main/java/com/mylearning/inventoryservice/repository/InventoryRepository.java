package com.mylearning.inventoryservice.repository;

import com.mylearning.inventoryservice.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory,Long> {
    Optional<Inventory> findByProductCode(String productCode);
    void deleteByProductCode(String productCode);
}