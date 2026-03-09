package com.example.upgrade;

// Issue #2: javax → jakarta migration
// In v8, we use javax.persistence imports
// In v9, these MUST be changed to jakarta.persistence
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Table;

/**
 * UserEntity demonstrates two migration issues:
 * 
 * Issue #2: javax → jakarta Package Migration
 * - v8: Uses javax.persistence imports
 * - v9: Must change to jakarta.persistence imports
 * 
 * Issue #6: @Entity Annotation on Process Variables
 * - v8: @Entity annotation works on process variables
 * - v9: Causes Hibernate validation errors unless properly configured
 * 
 * Migration Steps for v9:
 * 1. Change all javax.persistence imports to jakarta.persistence
 * 2. Either:
 *    a) Remove @Entity if only used as process variable, OR
 *    b) Properly configure Hibernate/JPA persistence, OR
 *    c) Exclude from Hibernate scanning in application.properties
 */
@Entity
@Table(name = "users")
public class UserEntity implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue
    private Long id;
    
    private String username;
    private String email;
    private String role;
    
    // Issue #5: Boolean getter naming
    // v8: Can use getActive()
    // v9: Should use isActive() for boolean primitive
    private boolean active;
    
    public UserEntity() {
    }
    
    public UserEntity(String username, String email, String role, boolean active) {
        this.username = username;
        this.email = email;
        this.role = role;
        this.active = active;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    // Issue #5: v8 uses get prefix for boolean
    // v9 should use is prefix for boolean primitive
    public boolean getActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
    
    @Override
    public String toString() {
        return "UserEntity{" +
                "id=" + id +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", role='" + role + '\'' +
                ", active=" + active +
                '}';
    }
}


