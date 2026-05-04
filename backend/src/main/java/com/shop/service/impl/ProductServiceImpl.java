package com.shop.service.impl;

import com.shop.entity.Product;
import com.shop.repository.ProductRepository;
import com.shop.service.ProductService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public List<Product> findAll() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findBySlug(String slug) {
        return productRepository.findBySlug(slug);
    }

    @Override
    public List<Product> findByCategory(String categorySlug) {
        return productRepository.findByCategorySlug(categorySlug);
    }

    @Override
    public List<Product> findNewArrivals(int limit) {
        return productRepository.findNewArrivals(PageRequest.of(0, Math.max(1, limit)));
    }

    @Override
    public List<Product> findFeatured(int limit) {
        return productRepository.findFeatured(PageRequest.of(0, Math.max(1, limit)));
    }

    @Override
    public List<Product> findOnSale(int limit) {
        return productRepository.findOnSale(PageRequest.of(0, Math.max(1, limit)));
    }

    @Override
    public List<Product> findRelated(Product product, int limit) {
        return productRepository.findRelated(
                product.categorySlug(),
                product.getId(),
                PageRequest.of(0, Math.max(1, limit))
        );
    }

    @Override
    public List<Product> findSummer(int limit) {
        return productRepository.findAll().stream()
                .filter(p -> p.name() != null && p.name().startsWith("Set "))
                .limit(limit)
                .toList();
    }

    @Override
    public List<Product> findWinter(int limit) {
        return productRepository.findAll().stream()
                .filter(p -> p.name() != null
                        && containsAny(p.name(), "khoác", "len", "jeans", "kaki dài", "cargo dài", "puffer"))
                .limit(limit)
                .toList();
    }

    @Override
    public List<Product> findHoodies(int limit) {
        return productRepository.findAll().stream()
                .filter(p -> p.name() != null && p.name().toLowerCase().contains("hoodie"))
                .limit(limit)
                .toList();
    }

    private static boolean containsAny(String source, String... keys) {
        String lower = source.toLowerCase();
        for (String key : keys) {
            if (lower.contains(key.toLowerCase())) return true;
        }
        return false;
    }

    @Override
    public List<Product> sorted(List<Product> input, String sort) {
        if (sort == null) return input;
        return switch (sort) {
            case "price-asc" -> input.stream()
                    .sorted(Comparator.comparingLong(Product::price))
                    .toList();
            case "price-desc" -> input.stream()
                    .sorted(Comparator.comparingLong(Product::price).reversed())
                    .toList();
            case "newest" -> input.stream()
                    .sorted(Comparator.comparing(Product::isNew).reversed())
                    .toList();
            default -> input;
        };
    }
}
