package com.example.database.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Concrete type for database row data
 * Uses JSON string internally to avoid Map marshalling issues in BAMOE v9
 */
public class RowData implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    private String data;
    
    public RowData() {
        this.data = "{}";
    }
    
    public RowData(Map<String, Object> columns) {
        try {
            this.data = objectMapper.writeValueAsString(columns != null ? columns : new HashMap<>());
        } catch (JsonProcessingException e) {
            this.data = "{}";
        }
    }
    
    public String getData() {
        return data;
    }
    
    public void setData(String data) {
        this.data = data;
    }
    
    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        try {
            return objectMapper.readValue(data, Map.class);
        } catch (JsonProcessingException e) {
            return new HashMap<>();
        }
    }
    
    @Override
    public String toString() {
        return "RowData{" +
                "data='" + data + '\'' +
                '}';
    }
}

