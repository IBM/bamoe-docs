package com.example.database.handler;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * v8 Database Work Item Handler
 * Uses JNDI lookup for datasource and manual JDBC operations
 */
public class DatabaseWorkItemHandler implements WorkItemHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(DatabaseWorkItemHandler.class);
    private DataSource dataSource;
    
    public DatabaseWorkItemHandler() {
        try {
            InitialContext ctx = new InitialContext();
            this.dataSource = (DataSource) ctx.lookup("java:jboss/datasources/ExampleDS");
            logger.info("DataSource initialized successfully");
        } catch (NamingException e) {
            logger.error("Failed to lookup datasource", e);
            throw new RuntimeException("Failed to lookup datasource", e);
        }
    }
    
    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        logger.info("Executing database query work item: {}", workItem.getId());
        
        String query = (String) workItem.getParameter("Query");
        
        if (query == null || query.trim().isEmpty()) {
            logger.error("Query parameter is required");
            manager.abortWorkItem(workItem.getId());
            return;
        }
        
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            
            List<Map<String, Object>> results = new ArrayList<>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();
            
            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                results.add(row);
            }
            
            Map<String, Object> output = new HashMap<>();
            output.put("Results", results);
            output.put("RowCount", results.size());
            
            logger.info("Query executed successfully, returned {} rows", results.size());
            manager.completeWorkItem(workItem.getId(), output);
            
        } catch (SQLException e) {
            logger.error("Database query failed", e);
            
            Map<String, Object> output = new HashMap<>();
            output.put("Error", e.getMessage());
            output.put("Results", new ArrayList<>());
            output.put("RowCount", 0);
            
            // Complete with error information rather than aborting
            manager.completeWorkItem(workItem.getId(), output);
        }
    }
    
    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        logger.info("Aborting database query work item: {}", workItem.getId());
        // Cleanup if needed
    }
}


