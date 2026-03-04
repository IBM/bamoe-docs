package com.example.persistence;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

/**
 * Order entity demonstrating v9 persistence with jakarta.persistence annotations.
 * This class will be used as a process variable and persisted to the database.
 * 
 * Key changes from v8:
 * - javax.persistence → jakarta.persistence (all imports)
 * - No other code changes required for basic JPA entities
 */
@Entity
@Table(name = "orders")
public class Order implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @Column(name = "order_id")
    private String orderId;
    
    @Column(name = "customer_name")
    private String customerName;
    
    @Column(name = "total_amount")
    private Double totalAmount;
    
    @Column(name = "order_status")
    private String status;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "order_date")
    private Date orderDate;
    
    @Column(name = "approved")
    private Boolean approved;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> items;
    
    // Default constructor required by JPA
    public Order() {
    }
    
    public Order(String orderId, String customerName, Double totalAmount) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.totalAmount = totalAmount;
        this.status = "PENDING";
        this.orderDate = new Date();
        this.approved = false;
    }
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public String getCustomerName() {
        return customerName;
    }
    
    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
    
    public Double getTotalAmount() {
        return totalAmount;
    }
    
    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Date getOrderDate() {
        return orderDate;
    }
    
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    
    public Boolean getApproved() {
        return approved;
    }
    
    // JavaBeans convention: Boolean properties should have both getXxx() and isXxx()
    // The Kogito marshaller expects isXxx() for Boolean fields
    public Boolean isApproved() {
        return approved;
    }
    
    public void setApproved(Boolean approved) {
        this.approved = approved;
    }
    
    public List<OrderItem> getItems() {
        return items;
    }
    
    public void setItems(List<OrderItem> items) {
        this.items = items;
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", customerName='" + customerName + '\'' +
                ", totalAmount=" + totalAmount +
                ", status='" + status + '\'' +
                ", orderDate=" + orderDate +
                ", approved=" + approved +
                '}';
    }
}


