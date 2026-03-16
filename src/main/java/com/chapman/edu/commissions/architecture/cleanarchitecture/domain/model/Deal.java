package com.chapman.edu.commissions.architecture.cleanarchitecture.domain.model;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Domain entity representing a sales deal.
 */
@Entity
@Table(name = "deals")
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
    private DealStatus status;

    @Column(name = "sales_rep_id", nullable = false)
    private String salesRepId;

    @Column(name = "close_date")
    private LocalDate closeDate;

    @Column(name = "created_date", nullable = false)
    private LocalDate createdDate;

    @Column(name = "last_modified_date")
    private LocalDate lastModifiedDate;

    @Transient
    private List<DealProduct> products;

    public Deal() {
        this.status = DealStatus.OPEN;
        this.createdDate = LocalDate.now();
        this.lastModifiedDate = LocalDate.now();
        this.products = new ArrayList<>();
    }

    public Deal(String title, BigDecimal value, String salesRepId) {
        this();
        this.title = title;
        this.value = value;
        this.salesRepId = salesRepId;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public BigDecimal getValue() { return value; }
    public void setValue(BigDecimal value) { this.value = value; }
    public DealStatus getStatus() { return status; }
    public void setStatus(DealStatus status) { this.status = status; }
    public String getSalesRepId() { return salesRepId; }
    public void setSalesRepId(String salesRepId) { this.salesRepId = salesRepId; }
    public LocalDate getCloseDate() { return closeDate; }
    public void setCloseDate(LocalDate closeDate) { this.closeDate = closeDate; }
    public LocalDate getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDate createdDate) { this.createdDate = createdDate; }
    public LocalDate getLastModifiedDate() { return lastModifiedDate; }
    public void setLastModifiedDate(LocalDate lastModifiedDate) { this.lastModifiedDate = lastModifiedDate; }
    public List<DealProduct> getProducts() { return products; }
    public void setProducts(List<DealProduct> products) { this.products = products; }
}
