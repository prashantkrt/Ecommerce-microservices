package com.mylearning.productservice.service;

import com.mylearning.productservice.dto.ProductRequestDto;
import com.mylearning.productservice.dto.ProductResponseDto;
import com.mylearning.productservice.entity.Product;
import com.mylearning.productservice.exception.ProductNotFoundException;
import com.mylearning.productservice.repository.ProductRepository;
import com.mylearning.productservice.util.ProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    @Override
    public ProductResponseDto create(ProductRequestDto productRequestDto) {
        Product product = ProductMapper.mapToProduct(productRequestDto);
        product.setCreatedAt(LocalDateTime.now());
        product.setUpdatedAt(LocalDateTime.now());
        return ProductMapper.mapToProductResponseDto(productRepository.save(product));
    }

    @Override
    public List<ProductResponseDto> getAll() {
        List<Product> products = productRepository.findAll();
        return products.stream()
                .map(ProductMapper::mapToProductResponseDto)
                .toList();
    }

    @Override
    public ProductResponseDto getById(Long id) {
        return ProductMapper.mapToProductResponseDto(productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id)));

    }
}
