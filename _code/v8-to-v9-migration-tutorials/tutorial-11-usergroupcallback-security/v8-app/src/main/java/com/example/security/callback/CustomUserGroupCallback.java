package com.example.security.callback;

import org.kie.api.task.UserGroupCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * v8 Custom User Group Callback for role-based access control
 * Implements org.kie.api.task.UserGroupCallback
 */
public class CustomUserGroupCallback implements UserGroupCallback {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserGroupCallback.class);
    
    // In-memory user-role mapping (in production, this would come from LDAP/DB)
    private static final Map<String, List<String>> USER_GROUPS = new HashMap<>();
    
    static {
        // Define user-role mappings
        USER_GROUPS.put("john", Arrays.asList("manager", "approver", "user"));
        USER_GROUPS.put("mary", Arrays.asList("admin", "approver", "user"));
        USER_GROUPS.put("steve", Arrays.asList("user", "requestor"));
        USER_GROUPS.put("alice", Arrays.asList("finance", "approver", "user"));
        USER_GROUPS.put("bharu", Arrays.asList("hr", "user"));
    }
    
    @Override
    public boolean existsUser(String userId) {
        boolean exists = USER_GROUPS.containsKey(userId);
        logger.debug("Checking if user exists: {} = {}", userId, exists);
        return exists;
    }
    
    @Override
    public boolean existsGroup(String groupId) {
        // Check if any user has this group
        boolean exists = USER_GROUPS.values().stream()
            .anyMatch(groups -> groups.contains(groupId));
        logger.debug("Checking if group exists: {} = {}", groupId, exists);
        return exists;
    }
    
    @Override
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
}


