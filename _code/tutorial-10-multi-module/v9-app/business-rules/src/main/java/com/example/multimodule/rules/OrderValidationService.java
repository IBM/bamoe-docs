package com.example.multimodule.rules;

import com.example.multimodule.model.Order;
import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

/**
 * Rule Unit for Order Validation in BAMOE v9.
 * 
 
 * @see com.example.multimodule.model.Order
 * @see OrderValidationService.drl
 */
public class OrderValidationService implements RuleUnitData {

    /**
     * DataStore for Order objects to be validated.
     * 
     * RULE EXECUTION FLOW:
     * 1. Client POSTs orders array to /OrderValidationService endpoint
     * 2. Orders are inserted into this DataStore
     * 3. Rules in OrderValidationService.drl are evaluated
     * 4. Rules modify order status based on business logic
     * 5. Modified orders are returned in the response
     */
    @Schema(implementation = Order[].class)
    private DataStore<Order> orders = DataSource.createStore();

    /**
     * Gets the DataStore containing orders to be validated.
     * 
     * @return DataStore of Order objects for rule evaluation
     */
    public DataStore<Order> getOrders() {
        return orders;
    }
    
    /**
     * Sets the DataStore for orders (used by Quarkus during deserialization).
     * 
     * @param orders DataStore to set
     */
    public void setOrders(DataStore<Order> orders) {
        this.orders = orders;
    }
}

