package com.shop.controller;

import com.shop.entity.Cart;
import com.shop.service.CartService;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

@Controller
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final CartService cartService;

    public HomeController(ProductService productService,
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

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("newArrivals", productService.findNewArrivals(4));
        model.addAttribute("summerItems", productService.findSummer(12));
        model.addAttribute("winterItems", productService.findWinter(12));
        model.addAttribute("saleItems", productService.findOnSale(6));
        model.addAttribute("featured", productService.findFeatured(4));
        return "pages/home";
    }
}
