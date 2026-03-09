package com.example.upgrade;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * DataModel - MIGRATED VERSION for v9
 * 
 * Migrations Applied:
 * 
 *  Raw List Type → Parameterized Types
 * - Changed: List → List<String>
 * - Changed: Map → Map<String, Object>
 * - Changed: ArrayList() → ArrayList<>()
 * - Changed: HashMap() → HashMap<>()
 * - Updated all method signatures with proper type parameters
 */
public class DataModel implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    //  Added type parameter
    private List<String> items;
    
    // Added type parameters
    private Map<String, Object> properties;
    
    private String name;
    private Integer count;
    
    public DataModel() {
        // Diamond operator with type inference
        this.items = new ArrayList<>();
        this.properties = new HashMap<>();
    }
    
    public DataModel(String name) {
        this.name = name;
        this.items = new ArrayList<>();
        this.properties = new HashMap<>();
    }
    
    //  Return type with parameter
    public List<String> getItems() {
        return items;
    }
    
    //  Parameter type specified
    public void setItems(List<String> items) {
        this.items = items;
    }
    
    //  Return type with parameters
    public Map<String, Object> getProperties() {
        return properties;
    }
    
    //  Parameter types specified
    public void setProperties(Map<String, Object> properties) {
        this.properties = properties;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public Integer getCount() {
        return count;
    }
    
    public void setCount(Integer count) {
        this.count = count;
    }
    
    //  Specific parameter type
    public void addItem(String item) {
        if (this.items == null) {
            this.items = new ArrayList<>();
        }
        this.items.add(item);
    }
    
    //  Specific parameter types
    public void setProperty(String key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap<>();
        }
        this.properties.put(key, value);
    }
    
    // Helper method to get property with type safety
    public Object getProperty(String key) {
        return this.properties != null ? this.properties.get(key) : null;
    }
    
    // Helper method to check if property exists
    public boolean hasProperty(String key) {
        return this.properties != null && this.properties.containsKey(key);
    }
    
    @Override
    public String toString() {
        return "DataModel{" +
                "items=" + items +
                ", properties=" + properties +
                ", name='" + name + '\'' +
                ", count=" + count +
                '}';
    }
}


