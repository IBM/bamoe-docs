package com.myspace.mortgage_app;

/**
 * Mortgage Application data model - migrated to v9
 * Removed @Label annotations (not needed in v9)
 */
public class Application implements java.io.Serializable {

    static final long serialVersionUID = 1L;

    private Applicant applicant;
    private Property property;
    private ValidationErrorDO errors;
    private Integer downpayment;
    private Integer amortization;
    private Integer mortgageamount;

    public Application() {
    }

    public Application(Applicant applicant, Property property, 
                      ValidationErrorDO errors, Integer downpayment, 
                      Integer amortization, Integer mortgageamount) {
        this.applicant = applicant;
        this.property = property;
        this.errors = errors;
        this.downpayment = downpayment;
        this.amortization = amortization;
        this.mortgageamount = mortgageamount;
    }

    public Applicant getApplicant() {
        return this.applicant;
    }

    public void setApplicant(Applicant applicant) {
        this.applicant = applicant;
    }

    public Property getProperty() {
        return this.property;
    }

    public void setProperty(Property property) {
        this.property = property;
    }

    public ValidationErrorDO getErrors() {
        return this.errors;
    }

    public void setErrors(ValidationErrorDO errors) {
        this.errors = errors;
    }

    public Integer getDownpayment() {
        return this.downpayment;
    }

    public void setDownpayment(Integer downpayment) {
        this.downpayment = downpayment;
    }

    public Integer getAmortization() {
        return this.amortization;
    }

    public void setAmortization(Integer amortization) {
        this.amortization = amortization;
    }

    public Integer getMortgageamount() {
        return this.mortgageamount;
    }

    public void setMortgageamount(Integer mortgageamount) {
        this.mortgageamount = mortgageamount;
    }
}


