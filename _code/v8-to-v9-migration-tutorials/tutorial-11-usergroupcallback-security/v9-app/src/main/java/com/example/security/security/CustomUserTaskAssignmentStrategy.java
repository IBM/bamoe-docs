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
 * Custom User Task Assignment Strategy for BAMOE v9
 *
 * MIGRATION NOTE (v8 → v9):
 * In BAMOE v8, task assignment was managed using the AssignmentStrategy mechanism
 * configured via system property org.jbpm.task.assignment.strategy. Built-in strategies
 * included RoundRobin, Potential Owner Busyness, and Business Rules strategies.
 *
 * In BAMOE v9, custom task assignment is implemented by implementing the
 * UserTaskAssignmentStrategy interface as a CDI bean. The framework calls
 * computeAssignment() for each user task, passing the UserTaskInstance and
 * the current IdentityProvider.
 *
 * For the full migration guide, see:
 * https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-usergroupcallback-assignmentstrategy
 *
 * Key differences from v8:
 * - Implements UserTaskAssignmentStrategy interface (not a system property)
 * - Uses CDI for dependency injection
 * - Works with framework's IdentityProvider (backed by OIDC/JWT in production)
 * - computeAssignment() is called by the framework for each user task
 */
@ApplicationScoped
public class CustomUserTaskAssignmentStrategy implements UserTaskAssignmentStrategy {

    // Round-robin counter for load balancing
    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public String getName() {
        return "custom-round-robin";
    }

    /**
     * Compute the assignment for a user task.
     *
     * This method is called by the BAMOE v9 framework for each user task.
     *
     * @param userTaskInstance the user task instance being assigned
     * @param identityProvider the framework's IdentityProvider — gives you the
     *                         current user's name and roles from the security context
     *                         (anonymous in dev mode, JWT-backed in production)
     * @return Optional containing the selected user ID, or empty if no assignment
     */
    @Override
    public Optional<String> computeAssignment(UserTaskInstance userTaskInstance,
                                              IdentityProvider identityProvider) {
        Set<String> potentialOwners = userTaskInstance.getPotentialUsers();
        return assignFromPotentialOwners(potentialOwners, identityProvider);
    }

    /**
     * Core assignment logic — extracted for testability.
     *
     * @param potentialUsers  set of potential users who can be assigned the task
     * @param identityProvider the framework's IdentityProvider
     * @return Optional containing the selected user ID, or empty if no assignment
     */
    public Optional<String> assignFromPotentialOwners(Set<String> potentialUsers,
                                                      IdentityProvider identityProvider) {
        if (potentialUsers == null || potentialUsers.isEmpty()) {
            return Optional.empty();
        }

        List<String> userList = new ArrayList<>(potentialUsers);

        if (userList.size() == 1) {
            return Optional.of(userList.get(0));
        }

        // Example: if the current user is an admin, assign to first user
        if (identityProvider != null && identityProvider.hasRole("admin")) {
            return Optional.of(userList.get(0));
        }

        // Default: round-robin assignment
        return Optional.of(selectUserRoundRobin(userList));
    }

    /**
     * Round-robin selection of users for load balancing.
     *
     * @param users list of potential users
     * @return selected user
     */
    private String selectUserRoundRobin(List<String> users) {
        int index = counter.getAndIncrement() % users.size();
        return users.get(index);
    }
}