package com.example.security;

import com.example.security.security.CustomUserGroupCallback;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit test for CustomUserGroupCallback CDI bean.
 *
 * Tests Test 4 from the README:
 * - Verifies the v9 CDI bean (no UserGroupCallback interface) works correctly
 * - Tests existsUser(), existsGroup(), getGroupsForUser(), mapOidcRolesToGroups()
 *
 * In v8, UserGroupCallback was tested via KIE API.
 * In v9, it is a plain CDI bean — inject and call directly.
 *
 * Run with: mvn test
 */
@QuarkusTest
public class CustomUserGroupCallbackTest {

    @Inject
    CustomUserGroupCallback userGroupCallback;

    // --- existsUser ---

    @Test
    public void testExistsUser_knownUser() {
        assertTrue(userGroupCallback.existsUser("john"));
        assertTrue(userGroupCallback.existsUser("mary"));
        assertTrue(userGroupCallback.existsUser("steve"));
        assertTrue(userGroupCallback.existsUser("alice"));
        assertTrue(userGroupCallback.existsUser("bharu"));
    }

    @Test
    public void testExistsUser_unknownUser() {
        assertFalse(userGroupCallback.existsUser("unknown"));
        assertFalse(userGroupCallback.existsUser(""));
    }

    // --- existsGroup ---

    @Test
    public void testExistsGroup_knownGroup() {
        assertTrue(userGroupCallback.existsGroup("manager"));
        assertTrue(userGroupCallback.existsGroup("admin"));
        assertTrue(userGroupCallback.existsGroup("approver"));
        assertTrue(userGroupCallback.existsGroup("user"));
        assertTrue(userGroupCallback.existsGroup("hr"));
        assertTrue(userGroupCallback.existsGroup("finance"));
    }

    @Test
    public void testExistsGroup_unknownGroup() {
        assertFalse(userGroupCallback.existsGroup("superadmin"));
        assertFalse(userGroupCallback.existsGroup(""));
    }

    // --- getGroupsForUser ---

    @Test
    public void testGetGroupsForUser_john() {
        List<String> groups = userGroupCallback.getGroupsForUser("john");
        assertTrue(groups.contains("manager"));
        assertTrue(groups.contains("approver"));
        assertTrue(groups.contains("user"));
    }

    @Test
    public void testGetGroupsForUser_mary() {
        List<String> groups = userGroupCallback.getGroupsForUser("mary");
        assertTrue(groups.contains("admin"));
        assertTrue(groups.contains("approver"));
        assertTrue(groups.contains("user"));
    }

    @Test
    public void testGetGroupsForUser_unknownUser_returnsEmpty() {
        List<String> groups = userGroupCallback.getGroupsForUser("nobody");
        assertNotNull(groups);
        assertTrue(groups.isEmpty());
    }

    // --- mapOidcRolesToGroups ---

    @Test
    public void testMapOidcRolesToGroups_adminRole() {
        List<String> groups = userGroupCallback.mapOidcRolesToGroups(Set.of("realm:admin"));
        assertTrue(groups.contains("admin"));
    }

    @Test
    public void testMapOidcRolesToGroups_managerAndApprover() {
        List<String> groups = userGroupCallback.mapOidcRolesToGroups(
            Set.of("realm:manager", "realm:approver"));
        assertTrue(groups.contains("manager"));
        assertTrue(groups.contains("approver"));
        assertFalse(groups.contains("admin"));
    }

    @Test
    public void testMapOidcRolesToGroups_noMatchingRoles() {
        List<String> groups = userGroupCallback.mapOidcRolesToGroups(Set.of("realm:viewer"));
        assertTrue(groups.isEmpty());
    }

    @Test
    public void testMapOidcRolesToGroups_emptyRoles() {
        List<String> groups = userGroupCallback.mapOidcRolesToGroups(Set.of());
        assertTrue(groups.isEmpty());
    }
}

