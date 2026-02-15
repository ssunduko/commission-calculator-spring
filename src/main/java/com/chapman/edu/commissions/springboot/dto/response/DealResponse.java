package com.chapman.edu.commissions.springboot.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Response DTO for Deal data returned to clients.
 *
 * CONCEPT: Response DTOs vs Domain Models
 * ------------------------------------------
 * We don't expose domain models directly in API responses because:
 *   1. Security — Some fields (e.g., internal IDs, audit fields) shouldn't be exposed
 *   2. Decoupling — API contract is independent of internal model changes
 *   3. Flexibility — Different views of the same data for different endpoints
 *   4. Serialization control — DTOs are designed for JSON serialization
 */
public class DealResponse {
    private String id;
    private String title;
    private BigDecimal value;
    private String status;
    private String salesRepId;
    private List<ProductInfo> products;
    private LocalDate closeDate;
    private LocalDate createdDate;

    /**
     * Nested DTO for product information within a deal.
     */
    public static class ProductInfo {
        private String productName;
        private int quantity;
        private BigDecimal price;
        private BigDecimal totalPrice;

        // --- Getters and Setters ---
        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }
        public int getQuantity() { return quantity; }
        public void setQuantity(int quantity) { this.quantity = quantity; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getTotalPrice() { return totalPrice; }
        public void setTotalPrice(BigDecimal totalPrice) { this.totalPrice = totalPrice; }
    }

    // --- Getters and Setters ---

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getSalesRepId() { return salesRepId; }
    public void setSalesRepId(String salesRepId) { this.salesRepId = salesRepId; }
    public List<ProductInfo> getProducts() { return products; }
    public void setProducts(List<ProductInfo> products) { this.products = products; }
    public LocalDate getCloseDate() { return closeDate; }
    public void setCloseDate(LocalDate closeDate) { this.closeDate = closeDate; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
}
