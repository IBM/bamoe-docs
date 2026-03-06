package com.example.database.config;

import com.example.database.handler.DatabaseWorkItemHandler;
import org.kie.kogito.process.impl.DefaultWorkItemHandlerConfig;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

/**
 * Configuration class to register custom work item handlers
 * Uses @PostConstruct to register after CDI injection
 */
@ApplicationScoped
public class DatabaseWorkItemHandlerConfig extends DefaultWorkItemHandlerConfig {
    
    @Inject
    DatabaseWorkItemHandler databaseWorkItemHandler;
    
    @PostConstruct
    public void init() {
        register("DatabaseTask", databaseWorkItemHandler);
    }
}


