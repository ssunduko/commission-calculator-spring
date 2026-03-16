package com.chapman.edu.commissions.verticalslice.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Represents a product within a deal.
 * Each deal can have multiple products with different quantities and prices.
 */
@Entity
@Table(name = "deal_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DealProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name = "product_id")
    private String productId;

    @Column(name = "product_name")
    private String productName;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(precision = 19, scale = 2)
    private BigDecimal discount = BigDecimal.ZERO;

    @Column(name = "deal_id")
    private String dealId;

    /**
     * Constructor with essential fields
     */
    public DealProduct(String productId, String productName, int quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.discount = BigDecimal.ZERO;
    }

    /**
     * Calculate the total price for this product (price * quantity - discount)
     * @return the total price
     */
    public BigDecimal calculateTotalPrice() {
        return price.multiply(new BigDecimal(quantity)).subtract(discount);
    }
}