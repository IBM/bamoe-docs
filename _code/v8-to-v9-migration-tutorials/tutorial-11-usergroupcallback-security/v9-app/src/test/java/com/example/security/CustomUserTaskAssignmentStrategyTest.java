package com.example.security;

import com.example.security.security.CustomUserTaskAssignmentStrategy;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.kie.kogito.auth.IdentityProvider;
import org.junit.jupiter.api.Test;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for CustomUserTaskAssignmentStrategy CDI bean.
 *
 * Tests Test 5 from the README:
 * - Verifies round-robin assignment across multiple users
 * - Verifies single-user assignment
 * - Verifies empty return for empty user set
 * - Demonstrates how to create a mock IdentityProvider for testing
 *
 * MIGRATION NOTE (v8 → v9):
 * In v8, AssignmentStrategy was configured via system property and tested
 * through the KIE task service. In v9, it implements the UserTaskAssignmentStrategy
 * interface as a CDI bean. The framework calls computeAssignment() for each user task.
 * For unit testing, we call assignFromPotentialOwners() directly with a mock IdentityProvider.
 *
 * Run with: mvn test
 */
@QuarkusTest
public class CustomUserTaskAssignmentStrategyTest {

    @Inject
    CustomUserTaskAssignmentStrategy strategy;

    /**
     * Helper: create a simple IdentityProvider for testing.
     * In production this is backed by the OIDC JWT token.
     * In dev mode the framework provides an anonymous IdentityProvider.
     */
    private IdentityProvider mockIdentity(String name, String... roles) {
        List<String> roleList = Arrays.asList(roles);
        return new IdentityProvider() {
            @Override public String getName() { return name; }
            @Override public List<String> getRoles() { return roleList; }
            @Override public boolean hasRole(String role) { return roleList.contains(role); }
        };
    }

    // --- empty / null users ---

    @Test
    public void testAssign_emptyUsers_returnsEmpty() {
        Optional<String> result = strategy.assignFromPotentialOwners(
            Collections.emptySet(), mockIdentity("john", "manager"));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void testAssign_nullUsers_returnsEmpty() {
        Optional<String> result = strategy.assignFromPotentialOwners(null, mockIdentity("john"));
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // --- single user ---

    @Test
    public void testAssign_singleUser_returnsThatUser() {
        Optional<String> result = strategy.assignFromPotentialOwners(
            Set.of("alice"), mockIdentity("john", "manager"));
        assertTrue(result.isPresent());
        assertEquals("alice", result.get());
    }

    // --- round-robin across multiple users ---

    @Test
    public void testAssign_roundRobin_cyclesThroughUsers() {
        Set<String> users = new LinkedHashSet<>(Arrays.asList("alice", "test", "dev"));
        IdentityProvider identity = mockIdentity("john", "user");

        
        List<String> assignments = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            Optional<String> result = strategy.assignFromPotentialOwners(users, identity);
            assertTrue(result.isPresent());
            assignments.add(result.get());
        }

        // Every user must appear at least once
        assertTrue(assignments.contains("alice"));
        assertTrue(assignments.contains("test"));
        assertTrue(assignments.contains("dev"));

        // The pattern must repeat: positions 0,3 same; 1,4 same; 2,5 same
        assertEquals(assignments.get(0), assignments.get(3));
        assertEquals(assignments.get(1), assignments.get(4));
        assertEquals(assignments.get(2), assignments.get(5));
    }

    // --- IdentityProvider integration ---

    @Test
    public void testAssign_withAdminIdentity_returnsUser() {
        // Admin identity — strategy uses hasRole("admin") for priority logic
        IdentityProvider adminIdentity = mockIdentity("mary", "admin", "approver");
        assertTrue(adminIdentity.hasRole("admin"));
        assertTrue(adminIdentity.hasRole("approver"));
        assertFalse(adminIdentity.hasRole("manager"));

        Optional<String> result = strategy.assignFromPotentialOwners(
            new LinkedHashSet<>(Arrays.asList("alice", "test")), adminIdentity);
        assertTrue(result.isPresent());
    }

    @Test
    public void testAssign_withAnonymousIdentity_stillAssigns() {
        // Dev mode: framework provides anonymous IdentityProvider with no roles
        IdentityProvider anonymous = mockIdentity("anonymous");
        assertEquals("anonymous", anonymous.getName());
        assertTrue(anonymous.getRoles().isEmpty());
        assertFalse(anonymous.hasRole("admin"));

        Optional<String> result = strategy.assignFromPotentialOwners(
            new LinkedHashSet<>(Arrays.asList("alice", "test")), anonymous);
        assertTrue(result.isPresent());
    }
}

