package com.shop.service.impl;

import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Order;
import com.shop.entity.OrderDetail;
import com.shop.entity.OrderStatus;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.exception.OutOfStockException;
import com.shop.exception.ProductNotFoundException;
import com.shop.repository.OrderRepository;
import com.shop.repository.ProductRepository;
import com.shop.service.OrderService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;

    public OrderServiceImpl(OrderRepository orderRepository,
                            ProductRepository productRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
    }

    @Override
    @Transactional
    public Order placeOrder(Cart cart, User user, CheckoutRequest request) {
        if (cart.isEmpty()) {
            throw new IllegalStateException("Giỏ hàng trống");
        }

        // Lock + decrement stock cho từng item. Nếu bất kỳ item nào không đủ tồn,
        // ném OutOfStockException → @Transactional rollback toàn bộ (kể cả các trừ stock trước đó).
        for (CartItem cartItem : cart.getItems()) {
            Long productId = parseProductId(cartItem.getProductId());
            Product product = productRepository.findByIdForUpdate(productId)
                    .orElseThrow(() -> new ProductNotFoundException(productId));

            int requested = cartItem.getQuantity();
            int available = product.stock();
            if (available < requested) {
                throw new OutOfStockException(product.name(), requested, available);
            }
            product.setStock(available - requested);
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

    private static Long parseProductId(String raw) {
        try {
            return Long.valueOf(raw);
        } catch (NumberFormatException e) {
            throw new ProductNotFoundException(raw);
        }
    }

    private String nextOrderCode() {
        return "BH" + System.currentTimeMillis();
    }
}
