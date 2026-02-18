package com.chapman.edu.commissions.orm.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ============================================================
 * JPA ENTITY: Deal
 * ============================================================
 *
 * ENTITY RELATIONSHIPS DEMONSTRATED:
 * - @ManyToOne: Each deal belongs to one sales representative (User).
 *   This is the OWNING side of the User-Deal bidirectional relationship.
 *   The owning side is determined by which entity holds the foreign key.
 *
 * - @OneToMany with CascadeType.ALL: A deal contains many DealProducts.
 *   CascadeType.ALL means all operations (PERSIST, MERGE, REMOVE, REFRESH, DETACH)
 *   cascade from Deal to DealProduct. When you save a Deal, its products are saved too.
 *
 * - orphanRemoval = true: If a DealProduct is removed from the products list,
 *   it is automatically deleted from the database. This is different from
 *   CascadeType.REMOVE, which only deletes children when the parent is deleted.
 *
 * FETCH STRATEGIES:
 * - FetchType.LAZY (default for @OneToMany): Products loaded on-demand
 *   BEST PRACTICE: Use LAZY by default, then use JOIN FETCH in queries
 *   when you know you'll need the collection.
 *
 * - FetchType.LAZY (for @ManyToOne): Sales rep loaded on-demand
 *   NOTE: @ManyToOne defaults to EAGER. We explicitly set LAZY to avoid
 *   loading the full User entity every time a Deal is fetched.
 */
@Entity
@Table(name = "deals", indexes = {
        @Index(name = "idx_deal_status", columnList = "status"),
        @Index(name = "idx_deal_sales_rep", columnList = "sales_rep_id"),
        @Index(name = "idx_deal_close_date", columnList = "close_date"),
        @Index(name = "idx_deal_created_date", columnList = "created_date")
})
@Data
@NoArgsConstructor
public class Deal {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(nullable = false)
    private String title;

    @Column(name = "deal_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal value;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DealStatus status = DealStatus.OPEN;

    /**
     * @ManyToOne RELATIONSHIP:
     * Many deals can belong to one sales representative.
     *
     * This is the OWNING SIDE of the User <-> Deal relationship.
     * The @JoinColumn specifies which column in the 'deals' table
     * holds the foreign key to 'users'.
     *
     * FetchType.LAZY: The User entity is NOT loaded when the Deal is fetched.
     * Instead, Hibernate creates a proxy. The actual SQL query for the User
     * only executes when you call deal.getSalesRep().getUsername() etc.
     *
     * WARNING: Accessing a lazy-loaded relationship outside an open
     * Hibernate Session/EntityManager will throw LazyInitializationException.
     * Solutions: JOIN FETCH queries, @EntityGraph, or DTO projections.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_rep_id", nullable = false)
    @JsonIgnore
    private User salesRep;

    /**
     * @OneToMany with CASCADE and ORPHAN REMOVAL:
     * A deal has many products. The products' lifecycle is tied to the deal.
     *
     * CascadeType.ALL: All JPA operations cascade to products:
     *   - PERSIST: Saving a deal saves its products
     *   - MERGE: Updating a deal updates its products
     *   - REMOVE: Deleting a deal deletes its products
     *   - REFRESH: Refreshing a deal refreshes its products
     *   - DETACH: Detaching a deal detaches its products
     *
     * orphanRemoval = true: Products removed from the list are deleted from DB.
     *   Example: deal.getProducts().remove(product) -> DELETE FROM deal_products
     *
     * mappedBy = "deal": DealProduct.deal is the owning side with the FK.
     */
    @OneToMany(mappedBy = "deal", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<DealProduct> products = new ArrayList<>();

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "last_modified_date")
    private LocalDate lastModifiedDate = LocalDate.now();

    /**
     * @OneToMany: A deal can have many commission calculations over time.
     * No cascade: Calculations are managed independently by the service layer.
     */
    @OneToMany(mappedBy = "deal", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<CommissionCalculation> calculations = new ArrayList<>();

    public Deal(String title, BigDecimal value, User salesRep) {
        this.title = title;
        this.value = value;
        this.salesRep = salesRep;
        this.status = DealStatus.OPEN;
        this.products = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.lastModifiedDate = LocalDate.now();
    }

    /**
     * Helper method for managing the bidirectional relationship.
     * BEST PRACTICE: Always use helper methods to keep both sides of
     * a bidirectional relationship in sync.
     */
    public void addProduct(DealProduct product) {
        products.add(product);
        product.setDeal(this);
    }

    public void removeProduct(DealProduct product) {
        products.remove(product);
        product.setDeal(null);
    }

    public void setStatus(DealStatus status) {
        this.status = status;
        this.lastModifiedDate = LocalDate.now();
    }

    public BigDecimal calculateTotalValue() {
        if (products == null || products.isEmpty()) {
            return BigDecimal.ZERO;
        }
        BigDecimal result = products.stream()
                .map(DealProduct::calculateTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return result.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deal deal = (Deal) o;
        return id != null && id.equals(deal.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    @Override
    public String toString() {
        return "Deal{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", value=" + value +
                ", status=" + status +
                ", closeDate=" + closeDate +
                '}';
    }
}
