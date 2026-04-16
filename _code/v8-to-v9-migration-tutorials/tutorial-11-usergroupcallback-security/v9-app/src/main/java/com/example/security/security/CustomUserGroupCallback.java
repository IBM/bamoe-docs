package com.example.security.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;

/**
 * Helper bean for user-group mappings
 * In production, integrate with OIDC/Keycloak or LDAP
 */
@ApplicationScoped
public class CustomUserGroupCallback {
    
    private static final Logger logger = LoggerFactory.getLogger(CustomUserGroupCallback.class);
    
    private static final Map<String, List<String>> USER_GROUPS = new HashMap<>();
    
    static {
        USER_GROUPS.put("john", Arrays.asList("manager", "approver", "user"));
        USER_GROUPS.put("mary", Arrays.asList("admin", "approver", "user"));
        USER_GROUPS.put("steve", Arrays.asList("user", "requestor"));
        USER_GROUPS.put("alice", Arrays.asList("finance", "approver", "user"));
        USER_GROUPS.put("bharu", Arrays.asList("hr", "user"));
    }
    
    public List<String> getGroupsForUser(String userId) {
        List<String> groups = USER_GROUPS.getOrDefault(userId, Collections.emptyList());
        logger.info("Getting groups for user {}: {}", userId, groups);
        return new ArrayList<>(groups);
    }
}


