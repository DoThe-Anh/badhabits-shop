package com.shop.controller;

import com.shop.entity.Cart;
import com.shop.entity.Product;
import com.shop.service.CartService;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;

    public ProductController(ProductService productService,
                             CategoryService categoryService,
                             CartService cartService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @ModelAttribute("cart")
    public Cart cart() { return cartService.getCart(); }

    @ModelAttribute("categories")
    public Object categories() { return categoryService.findAll(); }

    @GetMapping("/product/{slug}")
    public String detail(@PathVariable String slug, Model model) {
        Product product = productService.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("related", productService.findRelated(product, 4));
        return "pages/product-detail";
    }
}
