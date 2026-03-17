package com.example.security.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * Optional helper CDI bean for development and testing — NOT the v9 replacement for UserGroupCallback.
 *
 * MIGRATION NOTE (v8 → v9):
 * In BAMOE v8, this class implemented org.kie.api.task.UserGroupCallback and was
 * registered in kie-deployment-descriptor.xml. The UserGroupCallback served as both
 * an identity provider and a user repository (LDAP, DB, properties file, etc.).
 *
 * In BAMOE v9, the UserGroupCallback interface is REPLACED by the framework's
 * IdentityProvider mechanism (org.kie.kogito.auth.IdentityProvider). User identity
 * and group/role information is now sourced from the security context (OIDC/JWT tokens)
 * via QuarkusIdentityProvider. There is no custom interface to implement for basic
 * user/group resolution.
 *
 * For the full migration guide, see:
 * https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-usergroupcallback-assignmentstrategy
 *
 * This CDI bean is provided as a HELPER for:
 * - Development/testing without OIDC (in-memory user-group mapping)
 * - Mapping OIDC roles to application-specific groups
 *
 * In production, user and group information comes from your Identity Provider (IdP)
 * via OIDC/OAuth2 tokens. The framework's QuarkusIdentityProvider automatically
 * extracts user identity and roles from the JWT token in the security context.
 */
@ApplicationScoped
public class CustomUserGroupCallback {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserGroupCallback.class);
    
    // In-memory user-role mapping (in production, integrate with OIDC/Keycloak)
    private static final Map<String, List<String>> USER_GROUPS = new HashMap<>();
    
    static {
        // Define user-role mappings
        USER_GROUPS.put("john", Arrays.asList("manager", "approver", "user"));
        USER_GROUPS.put("mary", Arrays.asList("admin", "approver", "user"));
        USER_GROUPS.put("steve", Arrays.asList("user", "requestor"));
        USER_GROUPS.put("alice", Arrays.asList("finance", "approver", "user"));
        USER_GROUPS.put("bharu", Arrays.asList("hr", "user"));
    }
    
    public boolean existsUser(String userId) {
        boolean exists = USER_GROUPS.containsKey(userId);
        logger.debug("Checking if user exists: {} = {}", userId, exists);
        return exists;
    }
    
    public boolean existsGroup(String groupId) {
        // Check if any user has this group
        boolean exists = USER_GROUPS.values().stream()
            .anyMatch(groups -> groups.contains(groupId));
        logger.debug("Checking if group exists: {} = {}", groupId, exists);
        return exists;
    }
    
    public List<String> getGroupsForUser(String userId) {
        List<String> groups = USER_GROUPS.getOrDefault(userId, Collections.emptyList());
        logger.info("Getting groups for user {}: {}", userId, groups);
        return new ArrayList<>(groups);
    }
    
    /**
     * Check if user has specific role
     */
    public boolean hasRole(String userId, String role) {
        List<String> userGroups = getGroupsForUser(userId);
        boolean hasRole = userGroups.contains(role);
        logger.debug("User {} has role {}: {}", userId, role, hasRole);
        return hasRole;
    }
    
    /**
     * Check if user has any of the specified roles
     */
    public boolean hasAnyRole(String userId, String... roles) {
        List<String> userGroups = getGroupsForUser(userId);
        for (String role : roles) {
            if (userGroups.contains(role)) {
                logger.debug("User {} has role {}", userId, role);
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get all users in a specific group
     */
    public List<String> getUsersInGroup(String groupId) {
        List<String> users = new ArrayList<>();
        for (Map.Entry<String, List<String>> entry : USER_GROUPS.entrySet()) {
            if (entry.getValue().contains(groupId)) {
                users.add(entry.getKey());
            }
        }
        logger.debug("Users in group {}: {}", groupId, users);
        return users;
    }
    
    /**
     * Add user to group dynamically
     */
    public void addUserToGroup(String userId, String groupId) {
        List<String> groups = USER_GROUPS.computeIfAbsent(userId, k -> new ArrayList<>());
        if (!groups.contains(groupId)) {
            groups.add(groupId);
            logger.info("Added user {} to group {}", userId, groupId);
        }
    }
    
    /**
     * Remove user from group
     */
    public void removeUserFromGroup(String userId, String groupId) {
        List<String> groups = USER_GROUPS.get(userId);
        if (groups != null && groups.remove(groupId)) {
            logger.info("Removed user {} from group {}", userId, groupId);
        }
    }
    
    /**
     * Integration with Quarkus Security Context
     * Maps OIDC roles to application groups
     */
    public List<String> mapOidcRolesToGroups(Set<String> oidcRoles) {
        List<String> groups = new ArrayList<>();
        
        // Map OIDC roles to application groups
        if (oidcRoles.contains("realm:admin")) {
            groups.add("admin");
        }
        if (oidcRoles.contains("realm:manager")) {
            groups.add("manager");
        }
        if (oidcRoles.contains("realm:approver")) {
            groups.add("approver");
        }
        
        logger.debug("Mapped OIDC roles {} to groups {}", oidcRoles, groups);
        return groups;
    }
}


