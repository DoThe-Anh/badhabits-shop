package com.shop.controller;

import com.shop.entity.Cart;
import com.shop.service.CartService;
import com.shop.service.CategoryService;
import com.shop.service.ProductService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;
    private final CategoryService categoryService;

    public CartController(CartService cartService,
                          ProductService productService,
                          CategoryService categoryService) {
        this.cartService = cartService;
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @ModelAttribute("cart")
    public Cart cart() { return cartService.getCart(); }

    @ModelAttribute("categories")
    public Object categories() { return categoryService.findAll(); }

    @GetMapping
    public String view(Model model) {
        return "pages/cart";
    }

    @PostMapping("/add")
    public String add(@RequestParam String productId,
                      @RequestParam(required = false) String size,
                      @RequestParam(required = false) String color,
                      @RequestParam(defaultValue = "1") int quantity,
                      @RequestParam(required = false, defaultValue = "false") boolean buyNow) {
        Long parsed = parseLong(productId);
        if (parsed != null) {
            productService.findAll().stream()
                    .filter(p -> parsed.equals(p.getId()))
                    .findFirst()
                    .ifPresent(p -> cartService.addProduct(p, size, color, Math.max(1, quantity)));
        }
        return buyNow ? "redirect:/checkout" : "redirect:/cart";
    }

    private static Long parseLong(String value) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException | NullPointerException e) {
            return null;
        }
    }

    @PostMapping("/update")
    public String update(@RequestParam String lineId,
                         @RequestParam int quantity) {
        cartService.updateQuantity(lineId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String remove(@RequestParam String lineId) {
        cartService.remove(lineId);
        return "redirect:/cart";
    }
}
