package com.example.security.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.inject.Inject;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.util.List;

/**
 * JAX-RS Filter to extract user and groups from query parameters
 * Sets user context in CustomIdentityProvider for the request
 * Validates group membership if group parameter is provided
 */
@Provider
public class UserContextFilter implements ContainerRequestFilter, ContainerResponseFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(UserContextFilter.class);
    
    @Inject
    CustomUserGroupCallback userGroupCallback;
    
    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        String user = requestContext.getUriInfo().getQueryParameters().getFirst("user");
        List<String> groups = requestContext.getUriInfo().getQueryParameters().get("group");
        
        if (user != null) {
            logger.info("UserContextFilter: Setting user context for user: {}", user);
            
            List<String> userGroups;
            if (groups != null && !groups.isEmpty()) {
                logger.info("UserContextFilter: Using groups from query params: {}", groups);
                userGroups = groups;
            } else {
                userGroups = userGroupCallback.getGroupsForUser(user);
                logger.info("UserContextFilter: Using groups from CustomUserGroupCallback: {}", userGroups);
            }
            
            // Validate group membership if group parameter is provided
            if (groups != null && !groups.isEmpty()) {
                String requestedGroup = groups.get(0);
                if (!userGroups.contains(requestedGroup)) {
                    logger.warn("Access denied: User {} does not have group {}", user, requestedGroup);
                    requestContext.abortWith(
                        Response.status(Response.Status.FORBIDDEN)
                            .entity(String.format("{\"message\":\"User %s does not have group %s\",\"status\":403}",
                                user, requestedGroup))
                            .build()
                    );
                    return;
                }
            }
            
            CustomIdentityProvider.setCurrentUser(user, userGroups);
        }
    }
    
    @Override
    public void filter(ContainerRequestContext requestContext,
                      ContainerResponseContext responseContext) throws IOException {
        logger.debug("UserContextFilter: Clearing user context");
        CustomIdentityProvider.clearCurrentUser();
    }
}


