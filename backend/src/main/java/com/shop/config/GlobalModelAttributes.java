package com.shop.config;

import com.shop.entity.User;
import com.shop.service.UserService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final UserService userService;

    public GlobalModelAttributes(UserService userService) {
        this.userService = userService;
    }

    @ModelAttribute("currentUser")
    public User currentUser(@AuthenticationPrincipal UserDetails principal) {
        if (principal == null) return null;
        return userService.findByEmail(principal.getUsername()).orElse(null);
    }
}
