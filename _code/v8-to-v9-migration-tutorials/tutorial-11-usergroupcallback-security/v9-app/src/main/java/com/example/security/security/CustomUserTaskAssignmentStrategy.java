package com.example.security.security;

import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.UserTaskAssignmentStrategy;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Custom User Task Assignment Strategy
 * Implements round-robin assignment with role-based priority
 */
@ApplicationScoped
public class CustomUserTaskAssignmentStrategy implements UserTaskAssignmentStrategy {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public String getName() {
        return "custom-round-robin";
    }

    @Override
    public Optional<String> computeAssignment(UserTaskInstance userTaskInstance,
                                              IdentityProvider identityProvider) {
        Set<String> potentialOwners = userTaskInstance.getPotentialUsers();
        if (potentialOwners == null || potentialOwners.isEmpty()) {
            return Optional.empty();
        }

        List<String> userList = new ArrayList<>(potentialOwners);

        if (userList.size() == 1) {
            return Optional.of(userList.get(0));
        }

        // If current user is admin, assign to first user
        if (identityProvider != null && identityProvider.hasRole("admin")) {
            return Optional.of(userList.get(0));
        }

        // Default: round-robin assignment
        int index = counter.getAndIncrement() % userList.size();
        return Optional.of(userList.get(index));
    }
}
