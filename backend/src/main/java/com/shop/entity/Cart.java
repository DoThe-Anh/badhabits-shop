package com.shop.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cart {

    private final List<CartItem> items = new ArrayList<>();

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public CartItem findByLineId(String lineId) {
        return items.stream()
                .filter(i -> i.getLineId().equals(lineId))
                .findFirst()
                .orElse(null);
    }

    public void addItem(CartItem item) {
        CartItem existing = findByLineId(item.getLineId());
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + item.getQuantity());
        } else {
            items.add(item);
        }
    }

    public void updateQuantity(String lineId, int quantity) {
        CartItem item = findByLineId(lineId);
        if (item == null) return;
        if (quantity <= 0) {
            items.remove(item);
        } else {
            item.setQuantity(quantity);
        }
    }

    public void remove(String lineId) {
        items.removeIf(i -> i.getLineId().equals(lineId));
    }

    public void clear() {
        items.clear();
    }

    public int getItemCount() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public long getSubtotal() {
        return items.stream().mapToLong(CartItem::getLineTotal).sum();
    }

    public long getShippingFee() {
        long subtotal = getSubtotal();
        if (subtotal == 0) return 0;
        return subtotal >= 500_000L ? 0L : 30_000L;
    }

    public long getTotal() {
        return getSubtotal() + getShippingFee();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    public String getSubtotalFormatted() { return Product.formatVnd(getSubtotal()); }
    public String getShippingFeeFormatted() {
        return getShippingFee() == 0 ? "Miễn phí" : Product.formatVnd(getShippingFee());
    }
    public String getTotalFormatted() { return Product.formatVnd(getTotal()); }

    public long getFreeShippingRemaining() {
        long subtotal = getSubtotal();
        return subtotal >= 500_000L ? 0 : (500_000L - subtotal);
    }

    public String getFreeShippingRemainingFormatted() {
        return Product.formatVnd(getFreeShippingRemaining());
    }
}
