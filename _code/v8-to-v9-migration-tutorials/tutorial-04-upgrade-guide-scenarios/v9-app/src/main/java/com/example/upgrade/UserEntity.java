package com.example.upgrade;

// Issue #2: MIGRATED - Changed from javax to jakarta
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Table;

/**
 * UserEntity - MIGRATED VERSION for v9
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
    
    // Changed from getActive() to isActive()
    // v9 uses "is" prefix for boolean primitive (JavaBeans standard)
    public boolean isActive() {
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


