package com.example.database.entity;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import java.io.Serializable;
import java.util.Date;

/**
 * v8 Audit Record Entity
 * Uses javax.persistence annotations
 */
@Entity
@Table(name = "audit_records")
public class AuditRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @Column(name = "process_instance_id", nullable = false)
    private String processInstanceId;
    
    @Column(name = "node_name")
    private String nodeName;
    
    @Column(name = "event_type")
    private String eventType;
    
    @Column(name = "event_data", length = 4000)
    private String eventData;
    
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "event_timestamp")
    private Date eventTimestamp;
    
    public AuditRecord() {
        this.eventTimestamp = new Date();
    }
    
    public AuditRecord(String processInstanceId, String nodeName, String eventType, String eventData) {
        this();
        this.processInstanceId = processInstanceId;
        this.nodeName = nodeName;
        this.eventType = eventType;
        this.eventData = eventData;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getProcessInstanceId() {
        return processInstanceId;
    }
    
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }
    
    public String getNodeName() {
        return nodeName;
    }
    
    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }
    
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getEventData() {
        return eventData;
    }
    
    public void setEventData(String eventData) {
        this.eventData = eventData;
    }
    
    public Date getEventTimestamp() {
        return eventTimestamp;
    }
    
    public void setEventTimestamp(Date eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    @Override
    public String toString() {
        return "AuditRecord{" +
                "id=" + id +
                ", processInstanceId='" + processInstanceId + '\'' +
                ", nodeName='" + nodeName + '\'' +
                ", eventType='" + eventType + '\'' +
                ", eventTimestamp=" + eventTimestamp +
                '}';
    }
}


