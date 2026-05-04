package com.shop.service.impl;

import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Order;
import com.shop.entity.OrderDetail;
import com.shop.entity.OrderStatus;
import com.shop.entity.User;
import com.shop.repository.OrderRepository;
import com.shop.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Override
    @Transactional
    public Order placeOrder(Cart cart, User user, CheckoutRequest request) {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống");
        }

        Order order = new Order(
                nextOrderCode(),
                LocalDateTime.now(),
                user,
                request.fullName(),
                request.email(),
                request.phone(),
                request.address(),
                request.city(),
                request.note(),
                request.paymentMethod(),
                cart.getSubtotal(),
                cart.getShippingFee(),
                cart.getTotal(),
                OrderStatus.PENDING
        );

        for (CartItem cartItem : cart.getItems()) {
            order.addItem(OrderDetail.fromCart(cartItem));
        }

        return orderRepository.save(order);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findByUser(User user) {
        if (user == null) return List.of();
        return orderRepository.findByUserOrderByPlacedAtDesc(user);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Order> findAll() {
        return orderRepository.findAllByOrderByPlacedAtDesc();
    }

    @Override
    @Transactional(readOnly = true)
    public Order findById(Long id) {
        return orderRepository.findWithItemsById(id).orElse(null);
    }

    @Override
    @Transactional
    public Order updateStatus(Long orderId, String statusName) {
        OrderStatus newStatus;
        try {
            newStatus = OrderStatus.valueOf(statusName);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Trạng thái không hợp lệ: " + statusName);
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng #" + orderId));
        order.setStatus(newStatus);
        return order;
    }

    private String nextOrderCode() {
        // Epoch-ms suffix — unique across restarts without needing a DB sequence.
        return "BH" + System.currentTimeMillis();
    }
}
