package com.chapman.edu.commissions.model;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Represents a product within a deal.
 * Each deal can have multiple products with different quantities and prices.
 */
public class DealProduct {
    private String id;
    private String productId;
    private String productName;
    private int quantity;
    private BigDecimal price;
    private BigDecimal discount;
    private String dealId;

    /**
     * Default constructor
     */
    public DealProduct() {
        this.quantity = 1;
        this.discount = BigDecimal.ZERO;
    }

    /**
     * Constructor with essential fields
     */
    public DealProduct(String productId, String productName, int quantity, BigDecimal price) {
        this();
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getDiscount() {
        return discount;
    }

    public void setDiscount(BigDecimal discount) {
        this.discount = discount;
    }

    public String getDealId() {
        return dealId;
    }

    public void setDealId(String dealId) {
        this.dealId = dealId;
    }

    /**
     * Calculate the total price for this product (price * quantity - discount)
     * @return the total price
     */
    public BigDecimal calculateTotalPrice() {

        return price.multiply(new BigDecimal(quantity)).subtract(discount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealProduct that = (DealProduct) o;

        // If both IDs are null, compare other fields
        if (id == null && that.id == null) {
            return Objects.equals(productId, that.productId) &&
                   Objects.equals(productName, that.productName) &&
                   quantity == that.quantity &&
                   Objects.equals(price, that.price);
        }

        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        if (id == null) {
            return Objects.hash(productId, productName, quantity, price);
        }
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DealProduct{" +
                "id='" + id + '\'' +
                ", productName='" + productName + '\'' +
                ", quantity=" + quantity +
                ", price=" + price +
                '}';
    }
}
