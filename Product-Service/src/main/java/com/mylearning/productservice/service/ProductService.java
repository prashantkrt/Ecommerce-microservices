package com.mylearning.productservice.service;

import com.mylearning.productservice.dto.ProductRequestDto;
import com.mylearning.productservice.dto.ProductResponseDto;

import java.util.List;

public interface ProductService {
    public ProductResponseDto create(ProductRequestDto productRequestDto);
    public List<ProductResponseDto> getAll();
    public ProductResponseDto getById(Long id);
}
