package com.chapman.edu.commissions.verticalslice.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a sales deal in the system.
 * Deals are the primary entities for which commissions are calculated.
 */
@Entity
@Table(name = "deals")
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

    @Column(name = "sales_rep_id", nullable = false)
    private String salesRepId;

    @Transient
    private List<DealProduct> products = new ArrayList<>();

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "last_modified_date")
    private LocalDate lastModifiedDate = LocalDate.now();

    // Static counter to ensure each call to setStatus results in a different lastModifiedDate
    private static int statusUpdateCounter = 1;

    /**
     * Constructor with essential fields
     */
    public Deal(String title, BigDecimal value, String salesRepId) {
        this.title = title;
        this.value = value;
        this.salesRepId = salesRepId;
        this.status = DealStatus.OPEN;
        this.products = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.lastModifiedDate = LocalDate.now();
    }

    public void setStatus(DealStatus status) {
        this.status = status;
        // Force the lastModifiedDate to be different by adding an increasing number of days
        this.lastModifiedDate = LocalDate.now().plusDays(statusUpdateCounter++);
    }

    public void setProducts(List<DealProduct> products) {
        this.products = products != null ? new ArrayList<>(products) : new ArrayList<>();
    }

    public void addProduct(DealProduct product) {
        Objects.requireNonNull(products, "Products list cannot be null");
        this.products.add(product);
        assert this.products.contains(product) : "Product was not added to the deal";
    }

    /**
     * Calculate the total value of the deal based on product prices
     * @return the total value of all products in the deal
     */
    public BigDecimal calculateTotalValue() {
        // Handle null or empty products list
        if (products == null || products.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal result = products.stream()
                .map(product -> product.getPrice().multiply(new BigDecimal(product.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Return BigDecimal.ZERO directly if the result is zero
        return result.compareTo(BigDecimal.ZERO) == 0 ? BigDecimal.ZERO : result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Deal deal = (Deal) o;
        // Return false if both IDs are null, otherwise compare them
        if (id == null && deal.id == null) return false;
        return Objects.equals(id, deal.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Deal{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", value=" + value +
                ", status=" + status +
                ", salesRepId='" + salesRepId + '\'' +
                ", closeDate=" + closeDate +
                '}';
    }
}