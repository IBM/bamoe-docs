package com.example.security.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Domain model for approval requests in v9
 * Migrated from v8 - uses modern Java time API
 */
public class ApprovalRequest implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private String requestId;
    private String requestor;
    private String department;
    private BigDecimal amount;
    private String description;
    private String status;
    private LocalDateTime requestDate;
    private String approver;
    private LocalDateTime approvalDate;
    private String comments;
    
    public ApprovalRequest() {
        this.status = "PENDING";
        this.requestDate = LocalDateTime.now();
    }
    
    public ApprovalRequest(String requestId, String requestor, String department, 
                          BigDecimal amount, String description) {
        this();
        this.requestId = requestId;
        this.requestor = requestor;
        this.department = department;
        this.amount = amount;
        this.description = description;
    }
    
    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }
    
    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    
    public String getRequestor() {
        return requestor;
    }
    
    public void setRequestor(String requestor) {
        this.requestor = requestor;
    }
    
    public String getDepartment() {
        return department;
    }
    
    public void setDepartment(String department) {
        this.department = department;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getStatus() {
        return status;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public LocalDateTime getRequestDate() {
        return requestDate;
    }
    
    public void setRequestDate(LocalDateTime requestDate) {
        this.requestDate = requestDate;
    }
    
    public String getApprover() {
        return approver;
    }
    
    public void setApprover(String approver) {
        this.approver = approver;
    }
    
    public LocalDateTime getApprovalDate() {
        return approvalDate;
    }
    
    public void setApprovalDate(LocalDateTime approvalDate) {
        this.approvalDate = approvalDate;
    }
    
    public String getComments() {
        return comments;
    }
    
    public void setComments(String comments) {
        this.comments = comments;
    }
    
    @Override
    public String toString() {
        return "ApprovalRequest{" +
                "requestId='" + requestId + '\'' +
                ", requestor='" + requestor + '\'' +
                ", department='" + department + '\'' +
                ", amount=" + amount +
                ", status='" + status + '\'' +
                '}';
    }
}


