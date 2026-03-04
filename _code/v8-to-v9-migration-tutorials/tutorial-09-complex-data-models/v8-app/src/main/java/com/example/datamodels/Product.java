package com.example.datamodels;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.Enumerated;
import javax.persistence.EnumType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.DecimalMin;
import java.math.BigDecimal;

/**
 * Product entity demonstrating enum types and validation (v8)
 * 
 * This class demonstrates:
 * - @Enumerated for enum fields
 * - Complex validation rules
 * - Business logic in entity
 */
@Entity
@Table(name = "products")
public class Product implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "product_id", length = 50)
    private String id;
    
    @NotNull(message = "Product name is required")
    @Size(min = 3, max = 200, message = "Product name must be between 3 and 200 characters")
    @Column(name = "product_name", nullable = false, length = 200)
    private String name;
    
    @Column(name = "description", length = 1000)
    private String description;
    
    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "stock_quantity")
    private Integer stockQuantity;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "category", length = 50)
    private ProductCategory category;
    
    @Column(name = "is_available")
    private Boolean available;
    
    public Product() {
    }
    
    public Product(String id, String name, BigDecimal price, ProductCategory category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.category = category;
        this.available = true;
        this.stockQuantity = 0;
    }
    
    // Business logic methods
    
    public boolean isInStock() {
        return stockQuantity != null && stockQuantity > 0;
    }
    
    public boolean canFulfillOrder(int quantity) {
        return isInStock() && stockQuantity >= quantity;
    }
    
    public void reduceStock(int quantity) {
        if (stockQuantity != null && stockQuantity >= quantity) {
            stockQuantity -= quantity;
        }
    }
    
    // Getters and Setters
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getStockQuantity() {
        return stockQuantity;
    }
    
    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }
    
    public ProductCategory getCategory() {
        return category;
    }
    
    public void setCategory(ProductCategory category) {
        this.category = category;
    }
    
    public Boolean getAvailable() {
        return available;
    }
    
    public void setAvailable(Boolean available) {
        this.available = available;
    }
    
    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", stockQuantity=" + stockQuantity +
                ", category=" + category +
                ", available=" + available +
                '}';
    }
    
    /**
     * Product category enum
     */
    public enum ProductCategory {
        ELECTRONICS,
        CLOTHING,
        FOOD,
        BOOKS,
        HOME,
        SPORTS,
        OTHER
    }
}


