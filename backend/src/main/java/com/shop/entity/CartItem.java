package com.shop.entity;

public class CartItem {

    private final String lineId;
    private final String productId;
    private final String productSlug;
    private final String name;
    private final long price;
    private final String image;
    private final String size;
    private final String color;
    private int quantity;

    public CartItem(String productId, String productSlug, String name, long price,
                    String image, String size, String color, int quantity) {
        this.productId = productId;
        this.productSlug = productSlug;
        this.name = name;
        this.price = price;
        this.image = image;
        this.size = size;
        this.color = color;
        this.quantity = Math.max(1, quantity);
        this.lineId = productId + "::" + (size == null ? "" : size) + "::" + (color == null ? "" : color);
    }

    public String getLineId() { return lineId; }
    public String getProductId() { return productId; }
    public String getProductSlug() { return productSlug; }
    public String getName() { return name; }
    public long getPrice() { return price; }
    public String getImage() { return image; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public int getQuantity() { return quantity; }

    public void setQuantity(int quantity) {
        this.quantity = Math.max(0, quantity);
    }

    public long getLineTotal() { return price * quantity; }

    public String getPriceFormatted() { return Product.formatVnd(price); }

    public String getLineTotalFormatted() { return Product.formatVnd(getLineTotal()); }
}
