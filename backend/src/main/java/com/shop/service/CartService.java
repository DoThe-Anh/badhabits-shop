package com.shop.service;

import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Product;

public class CartService {

    private final Cart cart;

    public CartService(Cart cart) {
        this.cart = cart;
    }

    public Cart getCart() {
        return cart;
    }

    public void addProduct(Product product, String size, String color, int quantity) {
        CartItem item = new CartItem(
                String.valueOf(product.getId()),
                product.slug(),
                product.name(),
                product.price(),
                product.primaryImage(),
                size,
                color,
                quantity
        );
        cart.addItem(item);
    }

    public void updateQuantity(String lineId, int quantity) {
        cart.updateQuantity(lineId, quantity);
    }

    public void remove(String lineId) {
        cart.remove(lineId);
    }

    public void clear() {
        cart.clear();
    }
}
