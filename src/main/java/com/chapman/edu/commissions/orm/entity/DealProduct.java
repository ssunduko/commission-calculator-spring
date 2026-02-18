package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * ============================================================
 * JPA ENTITY: DealProduct
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne: Many products belong to one deal.
 *   This is the OWNING side of the Deal-DealProduct relationship.
 *
 * MAPPING STRATEGY - COMPONENT/CHILD ENTITY:
 * DealProduct is a "child" entity whose lifecycle is managed by its parent (Deal).
 * This pattern is common in aggregate design:
 *   - The parent (Deal) uses CascadeType.ALL + orphanRemoval
 *   - The child (DealProduct) has a @ManyToOne back-reference
 *   - Children are created/deleted through the parent's collection
 *
 * IMPORTANT: The @ManyToOne side is always the owning side in JPA.
 * The owning side is responsible for persisting the relationship
 * (i.e., setting the foreign key column value).
 */
@Entity
@Table(name = "deal_products", indexes = {
        @Index(name = "idx_dp_deal_id", columnList = "deal_id"),
        @Index(name = "idx_dp_product_id", columnList = "product_id")
})
@Data
@NoArgsConstructor
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

    /**
     * @ManyToOne: The owning side of the Deal-DealProduct relationship.
     * The 'deal_id' column in this table is the FK pointing to 'deals.id'.
     *
     * FetchType.LAZY: The Deal is not loaded when fetching a DealProduct.
     * Since we typically navigate from Deal -> DealProduct (not the reverse),
     * lazy loading here avoids unnecessary queries.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deal_id")
    @JsonIgnore
    private Deal deal;

    public DealProduct(String productId, String productName, int quantity, BigDecimal price) {
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.discount = BigDecimal.ZERO;
    }

    public BigDecimal calculateTotalPrice() {
        return price.multiply(new BigDecimal(quantity)).subtract(discount);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DealProduct that = (DealProduct) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
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
