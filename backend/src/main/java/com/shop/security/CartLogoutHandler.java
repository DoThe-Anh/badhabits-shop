package com.shop.security;

import com.shop.service.CartService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

/**
 * Empties the session-scoped cart when a user logs out so the next account to
 * sign in on the same browser starts with a clean cart. Tied to the user
 * chain's logout; admin chain doesn't use the cart.
 */
@Component
public class CartLogoutHandler implements LogoutHandler {

    private final CartService cartService;

    public CartLogoutHandler(CartService cartService) {
        this.cartService = cartService;
    }

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        cartService.clear();
    }
}
