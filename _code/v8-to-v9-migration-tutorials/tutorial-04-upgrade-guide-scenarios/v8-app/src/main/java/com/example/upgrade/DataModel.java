package com.example.upgrade;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * DataModel demonstrates Issue #4: Raw List Type Without Type Parameter
 * 
 * Issue #4: Can't use java.util.List without type parameter
 * - v8: Raw types (List without <T>) work without issues
 * - v9: Raw types cause Jandex indexing error
 * 
 * Error in v9:
 * "Jandex indexing failed"
 * "Raw type 'List' is not allowed. Please specify type parameter: List<T>"
 * 
 * Migration Steps for v9:
 * 1. Add type parameters to all generic collections
 * 2. Change List to List<String>, List<Integer>, etc.
 * 3. Change Map to Map<String, Object>, etc.
 * 4. Update method signatures with proper types
 */
public class DataModel implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // Issue #4: v8 allows raw types
    // v9 requires: List<String>
    private List items;
    
    // Issue #4: v8 allows raw Map
    // v9 requires: Map<String, Object>
    private Map properties;
    
    private String name;
    private Integer count;
    
    public DataModel() {
        this.items = new ArrayList();  // Also raw type
        this.properties = new HashMap();  // Also raw type
    }
    
    public DataModel(String name) {
        this.name = name;
        this.items = new ArrayList();
        this.properties = new HashMap();
    }
    
    // Issue #4: v8 method with raw type return
    // v9 requires: public List<String> getItems()
    public List getItems() {
        return items;
    }
    
    // Issue #4: v8 method with raw type parameter
    // v9 requires: public void setItems(List<String> items)
    public void setItems(List items) {
        this.items = items;
    }
    
    // Issue #4: v8 method with raw Map
    // v9 requires: public Map<String, Object> getProperties()
    public Map getProperties() {
        return properties;
    }
    
    // Issue #4: v8 method with raw Map parameter
    // v9 requires: public void setProperties(Map<String, Object> properties)
    public void setProperties(Map properties) {
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
    
    // Issue #4: v8 method that adds to raw List
    // v9 requires: public void addItem(String item)
    public void addItem(Object item) {
        if (this.items == null) {
            this.items = new ArrayList();
        }
        this.items.add(item);
    }
    
    // Issue #4: v8 method that puts to raw Map
    // v9 requires: public void setProperty(String key, Object value)
    public void setProperty(Object key, Object value) {
        if (this.properties == null) {
            this.properties = new HashMap();
        }
        this.properties.put(key, value);
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
