package com.mylearning.productservice.service;

import com.mylearning.productservice.dto.ProductRequestDto;
import com.mylearning.productservice.dto.ProductResponseDto;
import com.mylearning.productservice.entity.Product;
import com.mylearning.productservice.exception.ProductNotFoundException;
import com.mylearning.productservice.repository.ProductRepository;
import com.mylearning.productservice.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true, timeout = 10)
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 30, propagation = Propagation.REQUIRED)
    public ProductResponseDto create(ProductRequestDto productRequestDto) {
        Product product = ProductMapper.mapToProduct(productRequestDto);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return ProductMapper.mapToProductResponseDto(productRepository.save(product));
    }

    @Override
    @Transactional(readOnly = true, timeout = 15)
    public List<ProductResponseDto> getAll() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductMapper::mapToProductResponseDto)
                .toList();
    }

    @Override
    @Transactional(readOnly = true, timeout = 10)
    public ProductResponseDto getById(Long id) {
        return ProductMapper.mapToProductResponseDto(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id)));

    }

    @Override
    @Transactional(readOnly = true, timeout = 10)
    public ProductResponseDto getByProductCode(String productCode) {
        return ProductMapper.mapToProductResponseDto(productRepository.findByProductCode(productCode)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with Product Code: " + productCode)));

    }

}
