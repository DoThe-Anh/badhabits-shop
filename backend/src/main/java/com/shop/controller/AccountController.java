package com.shop.controller;

import com.shop.entity.Cart;
import com.shop.entity.User;
import com.shop.service.CartService;
import com.shop.service.CategoryService;
import com.shop.service.OrderService;
import com.shop.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@Controller
public class AccountController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final CartService cartService;
    private final OrderService orderService;

    public AccountController(UserService userService,
                             CategoryService categoryService,
                             CartService cartService,
                             OrderService orderService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.cartService = cartService;
        this.orderService = orderService;
    }

    @ModelAttribute("cart")
    public Cart cart() { return cartService.getCart(); }

    @ModelAttribute("categories")
    public Object categories() { return categoryService.findAll(); }

    @GetMapping("/account")
    public String account(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (principal == null) return "redirect:/login";
        User user = userService.findByEmail(principal.getUsername()).orElse(null);
        model.addAttribute("user", user);
        model.addAttribute("orders", user == null ? List.of() : orderService.findByUser(user));
        return "pages/account";
    }
}
