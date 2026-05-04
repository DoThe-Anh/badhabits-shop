package com.shop.service;

import com.shop.entity.Product;

import java.util.List;
import java.util.Optional;

public interface ProductService {

    List<Product> findAll();

    Optional<Product> findBySlug(String slug);

    List<Product> findByCategory(String categorySlug);

    List<Product> findNewArrivals(int limit);

    List<Product> findFeatured(int limit);

    List<Product> findOnSale(int limit);

    List<Product> findRelated(Product product, int limit);

    List<Product> findSummer(int limit);

    List<Product> findWinter(int limit);

    List<Product> findHoodies(int limit);

    List<Product> sorted(List<Product> input, String sort);
}
