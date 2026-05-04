package com.shop.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "order_items")
public class OrderDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;

    @Column(name = "product_slug", nullable = false, length = 190)
    private String productSlug;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(nullable = false)
    private long price;

    @Column(length = 500)
    private String image;

    @Column(length = 32)
    private String size;

    @Column(length = 64)
    private String color;

    @Column(nullable = false)
    private int quantity;

    protected OrderDetail() {}

    public OrderDetail(String productId, String productSlug, String name, long price,
                     String image, String size, String color, int quantity) {
        this.productId = productId;
        this.productSlug = productSlug;
        this.name = name;
        this.price = price;
        this.image = image;
        this.size = size;
        this.color = color;
        this.quantity = Math.max(1, quantity);
    }

    public static OrderDetail fromCart(CartItem cartItem) {
        return new OrderDetail(
                cartItem.getProductId(),
                cartItem.getProductSlug(),
                cartItem.getName(),
                cartItem.getPrice(),
                cartItem.getImage(),
                cartItem.getSize(),
                cartItem.getColor(),
                cartItem.getQuantity()
        );
    }

    void setOrder(Order order) { this.order = order; }

    public Long getId() { return id; }
    public String getProductId() { return productId; }
    public String getProductSlug() { return productSlug; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getImage() { return image; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public int getQuantity() { return quantity; }

    public long getLineTotal() { return price * quantity; }
    public String getPriceFormatted() { return Product.formatVnd(price); }
    public String getLineTotalFormatted() { return Product.formatVnd(getLineTotal()); }
}
