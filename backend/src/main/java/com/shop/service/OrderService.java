package com.shop.service;

import com.shop.entity.Cart;
import com.shop.entity.Order;
import com.shop.entity.User;

import java.util.List;

public interface OrderService {

    Order placeOrder(Cart cart, User user, CheckoutRequest request);

    List<Order> findByUser(User user);

    List<Order> findAll();

    Order findById(Long id);

    Order updateStatus(Long orderId, String statusName);

    record CheckoutRequest(
            String fullName,
            String email,
            String phone,
            String address,
            String city,
            String note,
            String paymentMethod
    ) {}
}
