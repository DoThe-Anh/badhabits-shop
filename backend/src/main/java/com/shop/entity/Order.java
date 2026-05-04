package com.shop.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_code", nullable = false, unique = true, length = 32)
    private String orderCode;

    @Column(name = "placed_at", nullable = false)
    private LocalDateTime placedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name", nullable = false, length = 190)
    private String fullName;

    @Column(nullable = false, length = 190)
    private String email;

    @Column(nullable = false, length = 32)
    private String phone;

    @Column(nullable = false, length = 500)
    private String address;

    @Column(nullable = false, length = 120)
    private String city;

    @Column(length = 1000)
    private String note;

    @Column(name = "payment_method", nullable = false, length = 32)
    private String paymentMethod;

    @Column(nullable = false)
    private long subtotal;

    @Column(nullable = false)
    private long shipping;

    @Column(nullable = false)
    private long total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 32)
    private OrderStatus status = OrderStatus.PENDING;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderColumn(name = "position")
    private List<OrderDetail> items = new ArrayList<>();

    protected Order() {}

    public Order(String orderCode, LocalDateTime placedAt, User user,
                 String fullName, String email, String phone,
                 String address, String city, String note, String paymentMethod,
                 long subtotal, long shipping, long total, OrderStatus status) {
        this.orderCode = orderCode;
        this.placedAt = placedAt;
        this.user = user;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.city = city;
        this.note = note;
        this.paymentMethod = paymentMethod;
        this.subtotal = subtotal;
        this.shipping = shipping;
        this.total = total;
        this.status = status == null ? OrderStatus.PENDING : status;
    }

    public void addItem(OrderDetail item) {
        items.add(item);
        item.setOrder(this);
    }

    public void setStatus(OrderStatus status) {
        this.status = status == null ? OrderStatus.PENDING : status;
    }

    public Long getId() { return id; }
    public String getOrderCode() { return orderCode; }
    public LocalDateTime getPlacedAt() { return placedAt; }
    public User getUser() { return user; }
    public String getFullName() { return fullName; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getAddress() { return address; }
    public String getCity() { return city; }
    public String getNote() { return note; }
    public String getPaymentMethod() { return paymentMethod; }
    public long getSubtotal() { return subtotal; }
    public long getShipping() { return shipping; }
    public long getTotal() { return total; }
    public OrderStatus getStatus() { return status; }
    public String getStatusLabel() { return status == null ? "" : status.label(); }
    public List<OrderDetail> getItems() { return Collections.unmodifiableList(items); }

    // Legacy-friendly accessors used by existing Thymeleaf templates
    public String orderId() { return orderCode; }
    public LocalDateTime placedAt() { return placedAt; }
    public List<OrderDetail> items() { return getItems(); }
    public String fullName() { return fullName; }
    public String email() { return email; }
    public String phone() { return phone; }
    public String address() { return address; }
    public String city() { return city; }
    public String note() { return note; }
    public String paymentMethod() { return paymentMethod; }
    public long subtotal() { return subtotal; }
    public long shipping() { return shipping; }
    public long total() { return total; }
    public OrderStatus status() { return status; }
    public String statusLabel() { return status == null ? "" : status.label(); }

    public String totalFormatted() { return Product.formatVnd(total); }
    public String subtotalFormatted() { return Product.formatVnd(subtotal); }
    public String shippingFormatted() {
        return shipping == 0 ? "Miễn phí" : Product.formatVnd(shipping);
    }
}
