package com.myspace.mortgage_app;

/**
 * Validation Error data model - migrated to v9
 * Removed @Label annotations (not needed in v9)
 */
public class ValidationErrorDO implements java.io.Serializable {

    static final long serialVersionUID = 1L;

    private String error;

    public ValidationErrorDO() {
    }

    public ValidationErrorDO(String error) {
        this.error = error;
    }

    public String getError() {
        return this.error;
    }

    public void setError(String error) {
        this.error = error;
    }
}


