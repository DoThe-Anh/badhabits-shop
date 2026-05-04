package com.shop.exception;

/**
 * Ném khi đặt hàng nhưng tồn kho không đủ. Mang theo tên SP, số yêu cầu và số còn lại
 * để GlobalExceptionHandler render thông điệp dễ hiểu cho user.
 */
public class OutOfStockException extends RuntimeException {

    private final String productName;
    private final int requested;
    private final int available;

    public OutOfStockException(String productName, int requested, int available) {
        super(String.format("Sản phẩm \"%s\" chỉ còn %d (yêu cầu %d).",
                productName, available, requested));
        this.productName = productName;
        this.requested = requested;
        this.available = available;
    }

    public String getProductName() { return productName; }
    public int getRequested() { return requested; }
    public int getAvailable() { return available; }
}
