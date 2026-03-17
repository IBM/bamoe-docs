package com.myspace.mortgage_app;

/**
 * Property data model - migrated to v9
 * Removed @Label annotations (not needed in v9)
 */
public class Property implements java.io.Serializable {

    static final long serialVersionUID = 1L;

    private Integer age;
    private String address;
    private String locale;
    private Integer saleprice;

    public Property() {
    }

    public Property(Integer age, String address, String locale, Integer saleprice) {
        this.age = age;
        this.address = address;
        this.locale = locale;
        this.saleprice = saleprice;
    }

    public Integer getAge() {
        return this.age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLocale() {
        return this.locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public Integer getSaleprice() {
        return this.saleprice;
    }

    public void setSaleprice(Integer saleprice) {
        this.saleprice = saleprice;
    }
}


