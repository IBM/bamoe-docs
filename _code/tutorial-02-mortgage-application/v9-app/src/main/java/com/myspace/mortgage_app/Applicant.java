package com.myspace.mortgage_app;

/**
 * Applicant data model - migrated to v9
 * Removed @Label annotations (not needed in v9)
 */
public class Applicant implements java.io.Serializable {

    static final long serialVersionUID = 1L;

    private String name;
    private Integer annualincome;
    private String address;
    private Integer ssn;
    private Integer creditrating;

    public Applicant() {
    }

    public Applicant(String name, Integer annualincome, String address, 
                    Integer ssn, Integer creditrating) {
        this.name = name;
        this.annualincome = annualincome;
        this.address = address;
        this.ssn = ssn;
        this.creditrating = creditrating;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAnnualincome() {
        return this.annualincome;
    }

    public void setAnnualincome(Integer annualincome) {
        this.annualincome = annualincome;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getSsn() {
        return this.ssn;
    }

    public void setSsn(Integer ssn) {
        this.ssn = ssn;
    }

    public Integer getCreditrating() {
        return this.creditrating;
    }

    public void setCreditrating(Integer creditrating) {
        this.creditrating = creditrating;
    }
}


