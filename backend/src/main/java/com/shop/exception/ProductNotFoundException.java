package com.shop.exception;

public class ProductNotFoundException extends RuntimeException {
    public ProductNotFoundException(Long id) {
        super("Không tìm thấy sản phẩm #" + id);
    }

    public ProductNotFoundException(String slug) {
        super("Không tìm thấy sản phẩm: " + slug);
    }
}
