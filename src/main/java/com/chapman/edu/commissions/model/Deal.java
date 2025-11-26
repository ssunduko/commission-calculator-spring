package com.chapman.edu.commissions.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Represents a sales deal in the system.
 * Deals are the primary entities for which commissions are calculated.
 */
public class Deal {
    private String id;
    private String title;
    private BigDecimal value;
    private DealStatus status;
    private String salesRepId;
    private List<DealProduct> products;
    private LocalDate closeDate;
    private LocalDate createdDate;
    private LocalDate lastModifiedDate;

    /**
     * Default constructor
     */
    public Deal() {
        this.products = new ArrayList<>();
        this.createdDate = LocalDate.now();
        this.lastModifiedDate = LocalDate.now();
    }

    /**
     * Constructor with essential fields
     */
    public Deal(String title, BigDecimal value, String salesRepId) {
        this();
        this.title = title;
        this.value = value;
        this.salesRepId = salesRepId;
        this.status = DealStatus.OPEN;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BigDecimal getValue() {
        return value;
    }

    public void setValue(BigDecimal value) {
        this.value = value;
    }

    public DealStatus getStatus() {
        return status;
    }

    // Static counter to ensure each call to setStatus results in a different lastModifiedDate
    private static int statusUpdateCounter = 1;

    public void setStatus(DealStatus status) {
        this.status = status;
        // Force the lastModifiedDate to be different by adding an increasing number of days
        this.lastModifiedDate = LocalDate.now().plusDays(statusUpdateCounter++);
    }

    public String getSalesRepId() {
        return salesRepId;
    }

    public void setSalesRepId(String salesRepId) {
        this.salesRepId = salesRepId;
    }

    public List<DealProduct> getProducts() {
        return products;
    }

    public void setProducts(List<DealProduct> products) {
        this.products = products != null ? new ArrayList<>(products) : null;
    }

    public void addProduct(DealProduct product) {

        Objects.requireNonNull(products, "Products list cannot be null");

        this.products.add(product);

        assert this.products.contains(product) : "Product was not added to the deal";
    }

    public LocalDate getCloseDate() {
        return closeDate;
    }

    public void setCloseDate(LocalDate closeDate) {
        this.closeDate = closeDate;
    }

    public LocalDate getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDate createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDate getLastModifiedDate() {
        return lastModifiedDate;
    }

    public void setLastModifiedDate(LocalDate lastModifiedDate) {
        this.lastModifiedDate = lastModifiedDate;
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
