package com.shop.controller;

import com.shop.entity.Cart;
import com.shop.entity.Order;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CheckoutController {

    private final CartService cartService;
    private final OrderService orderService;
    private final CategoryService categoryService;
    private final UserService userService;

    public CheckoutController(CartService cartService,
                              OrderService orderService,
                              CategoryService categoryService,
                              UserService userService) {
        this.cartService = cartService;
        this.orderService = orderService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @ModelAttribute("cart")
    public Cart cart() { return cartService.getCart(); }

    @ModelAttribute("categories")
    public Object categories() { return categoryService.findAll(); }

    @GetMapping("/checkout")
    public String checkout(@AuthenticationPrincipal UserDetails principal, Model model) {
        if (cartService.getCart().isEmpty()) {
            return "redirect:/cart";
        }
        // Prefill shipping form from the logged-in user's profile (editable).
        User user = principal == null
                ? null
                : userService.findByEmail(principal.getUsername()).orElse(null);
        if (user != null) {
            model.addAttribute("prefillFullName", user.fullName());
            model.addAttribute("prefillEmail", user.email());
            model.addAttribute("prefillPhone", user.phone());
            model.addAttribute("prefillAddress", user.address());
        }
        return "pages/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam String address,
                             @RequestParam String city,
                             @RequestParam(required = false) String note,
                             @RequestParam String paymentMethod,
                             @AuthenticationPrincipal UserDetails principal,
                             RedirectAttributes redirect,
                             Model model) {
        Cart cart = cartService.getCart();
        if (cart.isEmpty()) {
            return "redirect:/cart";
        }
        User user = principal == null
                ? null
                : userService.findByEmail(principal.getUsername()).orElse(null);
        OrderService.CheckoutRequest request = new OrderService.CheckoutRequest(
                fullName, email, phone, address, city, note, paymentMethod);
        Order order = orderService.placeOrder(cart, user, request);
        cartService.clear();
        redirect.addFlashAttribute("order", order);
        return "redirect:/checkout/success";
    }

    @GetMapping("/checkout/success")
    public String success(Model model) {
        if (!model.containsAttribute("order")) {
            return "redirect:/";
        }
        return "pages/checkout-success";
    }
}
