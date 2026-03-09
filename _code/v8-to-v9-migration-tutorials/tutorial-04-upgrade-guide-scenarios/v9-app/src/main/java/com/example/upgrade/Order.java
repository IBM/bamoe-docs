package com.example.upgrade;

/**
 * Order domain model for rule validation
 */
public class Order {
    
    private String orderId;
    private Double amount;
    private Integer creditScore;
    private String status;
    private Boolean validated;
    private String rejectionReason;
    
    public Order() {
    }
    
    public Order(String orderId, Double amount, Integer creditScore) {
        this.orderId = orderId;
        this.amount = amount;
        this.creditScore = creditScore;
        this.validated = false;
    }
    
    // Getters and Setters
    public String getOrderId() {
        return orderId;
    }
    
    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }
    
    public Double getAmount() {
        return amount;
    }
    
    public void setAmount(Double amount) {
        this.amount = amount;
    }
    
    public Integer getCreditScore() {
        return creditScore;
    }
    
    public void setCreditScore(Integer creditScore) {
        this.creditScore = creditScore;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public Boolean getValidated() {
        return validated;
    }
    
    public void setValidated(Boolean validated) {
        this.validated = validated;
    }
    
    // JavaBeans convention for boolean - required by Quarkus marshaller
    public boolean isValidated() {
        return validated != null && validated;
    }
    
    public String getRejectionReason() {
        return rejectionReason;
    }
    
    public void setRejectionReason(String rejectionReason) {
        this.rejectionReason = rejectionReason;
    }
    
    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", creditScore=" + creditScore +
                ", status='" + status + '\'' +
                ", validated=" + validated +
                ", rejectionReason='" + rejectionReason + '\'' +
                '}';
    }
}
