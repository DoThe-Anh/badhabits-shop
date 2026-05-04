package com.shop.service.impl;

import com.shop.entity.Cart;
import com.shop.entity.CartItem;
import com.shop.entity.Category;
import com.shop.entity.Order;
import com.shop.entity.OrderStatus;
import com.shop.entity.Product;
import com.shop.entity.User;
import com.shop.exception.OutOfStockException;
import com.shop.exception.ProductNotFoundException;
import com.shop.repository.OrderRepository;
import com.shop.repository.ProductRepository;
import com.shop.service.OrderService.CheckoutRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductRepository productRepository;

    @InjectMocks private OrderServiceImpl service;

    // ---------- Helpers ----------

    private static Category category(Long id, String slug) {
        Category c = new Category(slug, slug.toUpperCase(), "");
        ReflectionTestUtils.setField(c, "id", id);
        return c;
    }

    private static Product product(Long id, String name, int stock) {
        Product p = new Product(
                "slug-" + id, name, category(1L, "tops"), "HO25",
                100_000L, null, "/img/p.webp", null,
                List.of(), List.of(), List.of(),
                "desc", "material",
                stock, false, false
        );
        ReflectionTestUtils.setField(p, "id", id);
        return p;
    }

    private static CartItem cartItem(Long productId, int quantity) {
        return new CartItem(
                String.valueOf(productId),
                "slug-" + productId,
                "P" + productId,
                100_000L,
                "/img/p.webp",
                "M",
                "BLACK",
                quantity
        );
    }

    private static Cart cartWith(CartItem... items) {
        Cart cart = new Cart();
        for (CartItem i : items) cart.addItem(i);
        return cart;
    }

    private static CheckoutRequest req() {
        return new CheckoutRequest("Anh", "a@b.vn", "0900", "1 Lê Lợi", "HCM", null, "cod");
    }

    // ---------- placeOrder ----------

    @Test
    void placeOrder_decrementsStockAndSavesOrder() {
        Cart cart = cartWith(cartItem(1L, 2), cartItem(2L, 3));
        Product p1 = product(1L, "P1", 10);
        Product p2 = product(2L, "P2", 5);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p1));
        when(productRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(p2));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order saved = service.placeOrder(cart, null, req());

        assertThat(p1.stock()).isEqualTo(8);   // 10 - 2
        assertThat(p2.stock()).isEqualTo(2);   // 5 - 3
        assertThat(saved.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(saved.getItems()).hasSize(2);
    }

    @Test
    void placeOrder_outOfStock_throwsAndDoesNotSave() {
        Cart cart = cartWith(cartItem(1L, 100));
        Product p1 = product(1L, "P1", 5);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p1));

        assertThatThrownBy(() -> service.placeOrder(cart, null, req()))
                .isInstanceOf(OutOfStockException.class)
                .hasMessageContaining("P1")
                .hasMessageContaining("5");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_oneItemMissing_throwsProductNotFound() {
        Cart cart = cartWith(cartItem(1L, 1), cartItem(99L, 1));
        Product p1 = product(1L, "P1", 10);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p1));
        when(productRepository.findByIdForUpdate(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.placeOrder(cart, null, req()))
                .isInstanceOf(ProductNotFoundException.class)
                .hasMessageContaining("#99");

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_emptyCart_throwsIllegalState() {
        Cart cart = new Cart();
        assertThatThrownBy(() -> service.placeOrder(cart, null, req()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("trống");

        verify(productRepository, never()).findByIdForUpdate(any());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_invalidProductIdFormat_throwsProductNotFound() {
        Cart cart = new Cart();
        cart.addItem(new CartItem("not-a-number", "slug-x", "X", 100L, "/img.webp", "M", "BLACK", 1));

        assertThatThrownBy(() -> service.placeOrder(cart, null, req()))
                .isInstanceOf(ProductNotFoundException.class);

        verify(orderRepository, never()).save(any());
    }

    @Test
    void placeOrder_setsTotalsFromCart() {
        // 2 items x 100,000 = 200,000 (subtotal < 500k → ship 30k → total 230k)
        Cart cart = cartWith(cartItem(1L, 2));
        Product p1 = product(1L, "P1", 10);
        when(productRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(p1));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));

        Order saved = service.placeOrder(cart, null, req());

        assertThat(saved.getSubtotal()).isEqualTo(200_000L);
        assertThat(saved.getShipping()).isEqualTo(30_000L);
        assertThat(saved.getTotal()).isEqualTo(230_000L);
    }

    // ---------- updateStatus ----------

    @Test
    void updateStatus_validStatus_updatesOrder() {
        Order order = new Order("BH1", java.time.LocalDateTime.now(), null,
                "x", "x@y.vn", "0900", "addr", "city", "", "cod",
                100L, 0L, 100L, OrderStatus.PENDING);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        Order result = service.updateStatus(5L, "SHIPPING");

        assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPING);
    }

    @Test
    void updateStatus_invalidStatusName_throws() {
        assertThatThrownBy(() -> service.updateStatus(5L, "NONSENSE"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("không hợp lệ");
    }

    @Test
    void updateStatus_nullStatus_throws() {
        assertThatThrownBy(() -> service.updateStatus(5L, null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void updateStatus_orderNotFound_throws() {
        when(orderRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateStatus(99L, "SHIPPING"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("#99");
    }

    // ---------- findByUser ----------

    @Test
    void findByUser_nullUser_returnsEmptyList() {
        assertThat(service.findByUser(null)).isEmpty();
        verify(orderRepository, never()).findByUserOrderByPlacedAtDesc(any());
    }

    @Test
    void findByUser_delegatesToRepository() {
        User u = new User("a@b.vn", "h", "x", "", "", null, java.time.Instant.now());
        when(orderRepository.findByUserOrderByPlacedAtDesc(u)).thenReturn(List.of());

        service.findByUser(u);
        verify(orderRepository).findByUserOrderByPlacedAtDesc(u);
    }
}
