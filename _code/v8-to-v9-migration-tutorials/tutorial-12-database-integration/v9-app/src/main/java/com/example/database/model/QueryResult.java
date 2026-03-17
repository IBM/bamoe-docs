package com.example.database.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Query Result wrapper for BAMOE v9 process variable marshalling.
 *
 * Used as the type of the 'results' process variable in database-process.bpmn.
 * Kogito generates marshallers for this class automatically since it is a
 * concrete type with a no-arg constructor and standard getters/setters.
 *
 * The rowCount field is stored explicitly so that it round-trips correctly
 * through the REST API (request body → process variable → response body).
 */
public class QueryResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<RowData> rows;
    private int rowCount;

    public QueryResult() {
        this.rows = new ArrayList<>();
        this.rowCount = 0;
    }

    public QueryResult(List<RowData> rows) {
        this.rows = rows != null ? rows : new ArrayList<>();
        this.rowCount = this.rows.size();
    }

    public List<RowData> getRows() {
        return rows;
    }

    public void setRows(List<RowData> rows) {
        this.rows = rows != null ? rows : new ArrayList<>();
        this.rowCount = this.rows.size();
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    @Override
    public String toString() {
        return "QueryResult{" +
                "rowCount=" + rowCount +
                ", rows=" + rows +
                '}';
    }
}

