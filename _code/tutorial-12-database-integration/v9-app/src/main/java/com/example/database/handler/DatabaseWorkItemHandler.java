package com.example.database.handler;

import com.example.database.model.QueryResult;
import com.example.database.model.RowData;
import org.kie.kogito.internal.process.workitem.KogitoWorkItem;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemHandler;
import org.kie.kogito.internal.process.workitem.KogitoWorkItemManager;
import org.kie.kogito.internal.process.workitem.WorkItemTransition;
import org.kie.kogito.process.workitems.impl.DefaultKogitoWorkItemHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * v9 Database Work Item Handler
 * Uses CDI injection for datasource and extends DefaultKogitoWorkItemHandler.
 * Registered via DatabaseWorkItemHandlerConfig.
 *
 * Returns QueryResult as a structured object — Kogito generates marshallers
 * for com.example.database.model.QueryResult and RowData automatically since
 * they are concrete types with no-arg constructors and standard getters/setters.
 */
@ApplicationScoped
public class DatabaseWorkItemHandler extends DefaultKogitoWorkItemHandler {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseWorkItemHandler.class);

    @Inject
    DataSource dataSource;

    @Override
    public Optional<WorkItemTransition> activateWorkItemHandler(KogitoWorkItemManager manager,
                                                                  KogitoWorkItemHandler handler,
                                                                  KogitoWorkItem workItem,
                                                                  WorkItemTransition transition) {
        logger.info("Executing database query work item: {}", workItem.getStringId());

        String query = (String) workItem.getParameter("Query");

        if (query == null || query.trim().isEmpty()) {
            logger.error("Query parameter is required");
            return Optional.of(handler.abortTransition(workItem.getPhaseStatus()));
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            List<RowData> rows = new ArrayList<>();
            ResultSetMetaData metaData = rs.getMetaData();
            int columnCount = metaData.getColumnCount();

            while (rs.next()) {
                Map<String, Object> row = new HashMap<>();
                for (int i = 1; i <= columnCount; i++) {
                    row.put(metaData.getColumnName(i), rs.getObject(i));
                }
                rows.add(new RowData(row));
            }

            QueryResult queryResult = new QueryResult(rows);

            Map<String, Object> output = new HashMap<>();
            output.put("Results", queryResult);
            output.put("RowCount", queryResult.getRowCount());

            logger.info("Query executed successfully, returned {} rows", queryResult.getRowCount());
            return Optional.of(handler.completeTransition(workItem.getPhaseStatus(), output));

        } catch (Exception e) {
            logger.error("Database query failed", e);

            Map<String, Object> output = new HashMap<>();
            output.put("Results", new QueryResult());
            output.put("RowCount", 0);

            // Complete with empty result rather than aborting
            return Optional.of(handler.completeTransition(workItem.getPhaseStatus(), output));
        }
    }

    @Override
    public Optional<WorkItemTransition> abortWorkItemHandler(KogitoWorkItemManager manager,
                                                               KogitoWorkItemHandler handler,
                                                               KogitoWorkItem workItem,
                                                               WorkItemTransition transition) {
        logger.info("Aborting database query work item: {}", workItem.getStringId());
        return Optional.empty();
    }
}

