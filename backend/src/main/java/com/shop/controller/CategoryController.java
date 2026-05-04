package com.shop.controller;

import com.shop.entity.Cart;
import com.shop.entity.Category;
import com.shop.entity.Product;
import com.shop.service.CartService;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class CategoryController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;

    public CategoryController(ProductService productService,
                              CategoryService categoryService,
                              CartService cartService) {
        this.productService = productService;
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @ModelAttribute("cart")
    public Cart cart() { return cartService.getCart(); }

    @ModelAttribute("categories")
    public List<Category> categories() { return categoryService.findAll(); }

    @GetMapping("/shop")
    public String shopAll(@RequestParam(required = false) String sort, Model model) {
        List<Product> products = productService.sorted(productService.findAll(), sort);
        model.addAttribute("title", "Tất cả sản phẩm");
        model.addAttribute("category", null);
        model.addAttribute("products", products);
        model.addAttribute("sort", sort);
        return "pages/category";
    }

    @GetMapping("/shop/{slug}")
    public String shopByCategory(@PathVariable String slug,
                                  @RequestParam(required = false) String sort,
                                  Model model) {
        Category category = categoryService.findBySlug(slug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
        List<Product> products = productService.sorted(
                productService.findByCategory(slug), sort);
        model.addAttribute("title", category.name());
        model.addAttribute("category", category);
        model.addAttribute("products", products);
        model.addAttribute("sort", sort);
        return "pages/category";
    }
}
