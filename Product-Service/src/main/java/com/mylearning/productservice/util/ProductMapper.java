package com.mylearning.productservice.util;

import com.mylearning.productservice.dto.ProductRequestDto;
import com.mylearning.productservice.dto.ProductResponseDto;
import com.mylearning.productservice.entity.Product;

public class ProductMapper {

    public static ProductResponseDto mapToProductResponseDto(Product product) {
        return ProductResponseDto.builder()
                .id(product.getId())
                .productCode(product.getProductCode())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    public static Product mapToProduct(ProductRequestDto productRequestDto) {
        return Product.builder()
                .productCode(productRequestDto.getProductCode())
                .name(productRequestDto.getName())
                .description(productRequestDto.getDescription())
                .price(productRequestDto.getPrice())
                .build();
    }

}
