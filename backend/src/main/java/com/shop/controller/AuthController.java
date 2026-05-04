package com.shop.controller;

import com.shop.entity.Cart;
import com.shop.service.CartService;
import com.shop.service.CategoryService;
import com.shop.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final CategoryService categoryService;
    private final CartService cartService;

    public AuthController(UserService userService,
                          CategoryService categoryService,
                          CartService cartService) {
        this.userService = userService;
        this.categoryService = categoryService;
        this.cartService = cartService;
    }

    @ModelAttribute("cart")
    public Cart cart() { return cartService.getCart(); }

    @ModelAttribute("categories")
    public Object categories() { return categoryService.findAll(); }

    @GetMapping("/login")
    public String loginPage(@RequestParam(required = false) String error,
                            @RequestParam(required = false) String registered,
                            @RequestParam(required = false) String logout,
                            @RequestParam(required = false) String requireLogin,
                            Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Email hoặc mật khẩu không đúng.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất.");
        }
        if (requireLogin != null) {
            model.addAttribute("warningMessage",
                    "Bạn chưa đăng nhập. Vui lòng đăng nhập để thêm giỏ hàng hoặc mua sản phẩm.");
        }
        return "pages/login";
    }

    @GetMapping("/admin/login")
    public String adminLoginPage(@RequestParam(required = false) String error,
                                 @RequestParam(required = false) String logout,
                                 Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Sai email/mật khẩu hoặc tài khoản không có quyền admin.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "Đã đăng xuất khỏi admin panel.");
        }
        return "pages/admin-login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "pages/register";
    }

    @PostMapping("/register")
    public String register(@RequestParam String email,
                           @RequestParam String password,
                           @RequestParam(required = false) String fullName,
                           @RequestParam(required = false) String phone,
                           @RequestParam(required = false) String address,
                           RedirectAttributes ra) {
        try {
            userService.register(email, password, fullName, phone, address);
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            ra.addFlashAttribute("email", email);
            ra.addFlashAttribute("fullName", fullName);
            ra.addFlashAttribute("phone", phone);
            ra.addFlashAttribute("address", address);
            return "redirect:/register";
        }
        return "redirect:/login?registered";
    }
}
