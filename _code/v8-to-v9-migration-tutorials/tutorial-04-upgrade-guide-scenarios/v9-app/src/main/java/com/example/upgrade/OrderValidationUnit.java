package com.example.upgrade;

import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.RuleUnitData;
import org.drools.ruleunits.api.SingletonStore;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Rule Unit for Order Validation (v9)
 *
 * This replaces the v8 ruleflow-group "order-validation".
 * In v9, all rules must be organized into Rule Units.
 *
 * Key Differences from v8:
 * - v8: Used ruleflow-group attribute in DRL
 * - v9: Uses Rule Unit class with SingletonStore for single objects
 *
 * Migration from v8 ruleflow-group:
 * 1. Create this RuleUnitData class
 * 2. Convert DRL to use "unit OrderValidationUnit"
 * 3. Update BPMN Business Rule Task:
 *    - v8: drools:ruleFlowGroup="order-validation"
 *    - v9: drools:ruleFlowGroup="unit:com.example.upgrade.OrderValidationUnit"
 *
 * IMPORTANT: Use SingletonStore for single objects, DataStore for collections
 */
public class OrderValidationUnit implements RuleUnitData {
    
    /**
     * SingletonStore for the order to be validated
     * Use SingletonStore for single facts that can be modified by rules
     *
     * Schema annotation required to correctly generate the OpenAPI v3 spec
     */
    @Schema(implementation = Order.class)
    private SingletonStore<Order> order;
    
    // Default constructor
    public OrderValidationUnit() {
        this(DataSource.createSingleton());
    }
    
    // Constructor with SingletonStore
    public OrderValidationUnit(SingletonStore<Order> order) {
        this.order = order;
    }
    
    // Getter and Setter
    public SingletonStore<Order> getOrder() {
        return order;
    }
    
    public void setOrder(SingletonStore<Order> order) {
        this.order = order;
    }
}
