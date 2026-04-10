package com.example.security.security;

import org.kie.kogito.auth.IdentityProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import jakarta.inject.Inject;
import java.util.Collection;
import java.util.List;

/**
 * Custom IdentityProvider - replaces v8 UserGroupCallback
 * Uses @Alternative and @Priority(1) to override default QuarkusIdentityProvider
 */
@Alternative
@Priority(1)
@ApplicationScoped
public class CustomIdentityProvider implements IdentityProvider {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomIdentityProvider.class);
    
    @Inject
    CustomUserGroupCallback userGroupCallback;
    
    private static final ThreadLocal<String> currentUser = new ThreadLocal<>();
    private static final ThreadLocal<List<String>> currentRoles = new ThreadLocal<>();
    
    public static void setCurrentUser(String username, List<String> roles) {
        logger.debug("Setting current user: {} with roles: {}", username, roles);
        currentUser.set(username);
        currentRoles.set(roles);
    }
    
    public static void clearCurrentUser() {
        logger.debug("Clearing current user context");
        currentUser.remove();
        currentRoles.remove();
    }
    
    @Override
    public String getName() {
        String user = currentUser.get();
        if (user != null) {
            logger.debug("CustomIdentityProvider.getName() returning: {}", user);
            return user;
        }
        logger.debug("CustomIdentityProvider.getName() returning: anonymous");
        return "anonymous";
    }
    
    @Override
    public Collection<String> getRoles() {
        List<String> roles = currentRoles.get();
        if (roles != null) {
            logger.debug("CustomIdentityProvider.getRoles() returning: {}", roles);
            return roles;
        }
        
        // Fallback to CustomUserGroupCallback
        String user = getName();
        if (!"anonymous".equals(user)) {
            List<String> userGroups = userGroupCallback.getGroupsForUser(user);
            logger.debug("CustomIdentityProvider.getRoles() from callback: {}", userGroups);
            return userGroups;
        }
        
        return List.of();
    }
    
    @Override
    public boolean hasRole(String role) {
        boolean result = getRoles().contains(role);
        logger.debug("CustomIdentityProvider.hasRole({}) = {}", role, result);
        return result;
    }
}
