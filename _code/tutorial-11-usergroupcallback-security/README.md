# Tutorial 11: Security Migration - UserGroupCallback to IdentityProvider

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

---

## Introduction

This tutorial demonstrates the **primary security migration** from BAMOE v8 to v9: migrating from `UserGroupCallback` to `IdentityProvider` and implementing custom user task assignment strategies.

**What You'll Learn:**
- How `UserGroupCallback` is replaced by `IdentityProvider` in v9
- How to implement `UserTaskAssignmentStrategy` with `IdentityProvider`
- How `IdentityProvider` integrates with OIDC/JWT tokens
- Testing user/group resolution in v9

### v8 to v9 Security Migration Overview

| Aspect | v8 (KIE-specific) | v9 (OIDC-based) |
|--------|-------------------|-----------------|
| **UserGroupCallback** | `org.kie.api.task.UserGroupCallback` interface | Replaced by `org.kie.kogito.auth.IdentityProvider` backed by OIDC/JWT |
| **AssignmentStrategy** | System property `org.jbpm.task.assignment.strategy` | `UserTaskAssignmentStrategy` interface (CDI bean) |
| **Configuration** | `kie-deployment-descriptor.xml` | `application.properties` (OIDC) |
| **User/Group Source** | Custom implementation (LDAP/DB/Properties) | JWT token claims (automatic) |

---

## Prerequisites

- Java 17+
- Maven 3.8.1+
- Basic understanding of OAuth2/OIDC 
- Optional: Docker (for Keycloak in production mode)

---

## Step-by-Step Migration

### Step 1: Understanding IdentityProvider in v9

In BAMOE v9, `UserGroupCallback` is replaced by the `IdentityProvider` interface for user task assignment and authorization.

**Key Methods:**
- `getName()` - Returns current user's login name
- `getRoles()` - Returns list of roles/groups
- `hasRole(String role)` - Checks if user has specific role

**Identity Source by Mode:**
- **Production (OIDC)**: JWT token claims (`preferred_username`, realm roles)
- **Dev/Test**: Query parameters (`?user=john&group=manager`)

### Step 2: Migrate User Group Callbacks

#### Understanding the v8 to v9 Migration

In v8, user group callbacks implemented the `org.kie.api.task.UserGroupCallback` interface, which served as both an identity provider and a user repository. It integrated with external identity management systems (LDAP, databases, properties files) to resolve user identities and their associated groups.

In v9, the `UserGroupCallback` mechanism is **replaced by the `IdentityProvider` interface**. Authentication and authorization are now delegated to the runtime environment (Quarkus), which integrates with external identity providers using OpenID Connect (OIDC) and OAuth 2.0.

**For detailed migration guidance, see the IBM BAMOE documentation:**
[Upgrading UserGroupCallback and AssignmentStrategy](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-usergroupcallback-assignmentstrategy)

**v8 Approach:**
- Implemented `org.kie.api.task.UserGroupCallback` interface
- Key built-in implementations: `JAASUserGroupCallbackImpl`, `DBUserGroupCallbackImpl`, `LDAPUserGroupCallbackImpl`, `PropsUserGroupCallbackImpl`
- Configured in `kie-deployment-descriptor.xml`
- Tightly coupled to KIE API

**v9 Approach:**
- `UserGroupCallback` is **replaced** by `org.kie.kogito.auth.IdentityProvider`
- `IdentityProvider` is backed by the Quarkus security context (OIDC/JWT tokens) via `QuarkusIdentityProvider`
- User identity and roles come from the JWT token or query parameters (in non-secured mode)
- No custom interface to implement for basic user/group resolution

**v8 Implementation:**
```java
// v8 - Implements KIE-specific interface
public class CustomUserGroupCallback implements UserGroupCallback {
    @Override
    public List<String> getGroupsForUser(String userId) {
        return USER_GROUPS.getOrDefault(userId, Collections.emptyList());
    }
}
```

**v9 Migration - No Interface to Implement:**

In v9, `UserGroupCallback` is **completely replaced** by the framework's `IdentityProvider`. User identity and roles are automatically extracted from:
- **Production**: JWT token claims (OIDC)
- **Dev/Test**: Query parameters (`?user=john&group=manager&group=approver`)

**Optional Helper Bean** ([`CustomUserGroupCallback.java`](v9-app/src/main/java/com/example/security/security/CustomUserGroupCallback.java)):

For dev/testing without OIDC, you can create an optional CDI bean:

```java
@ApplicationScoped
public class CustomUserGroupCallback {
    private static final Map<String, List<String>> USER_GROUPS = new HashMap<>();
    
    static {
        USER_GROUPS.put("john", Arrays.asList("manager", "approver", "user"));
        USER_GROUPS.put("mary", Arrays.asList("admin", "approver", "user"));
    }
    
    public List<String> getGroupsForUser(String userId) {
        return USER_GROUPS.getOrDefault(userId, Collections.emptyList());
    }
}
```

**Key Points:**
- ✅ No interface to implement — `IdentityProvider` is framework-provided
- ✅ No `kie-deployment-descriptor.xml` configuration
- ✅ User/roles from JWT token in production
- ✅ Helper bean optional for dev/testing only

**Reference:** [IBM BAMOE: Upgrading UserGroupCallback](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-usergroupcallback-assignmentstrategy)

### Step 3: Implement Custom User Task Assignment Strategy

**v8 Approach:**
- System property: `org.jbpm.task.assignment.strategy`
- Built-in strategies: RoundRobin, Potential Owner Busyness, Business Rules

**v9 Approach:**

#### Advanced: User Impersonation (Optional)

In production environments with OIDC enabled, you may need to allow certain privileged users to impersonate other users for testing or administrative purposes.

**Configuration:**

```properties
# Enable authentication
kogito.security.auth.enabled=true

# Allow users with 'admin' role to impersonate others
kogito.security.auth.impersonation.allowed-for-roles=admin,managers
```

**Usage:**

When impersonation is enabled, authenticated users with the specified roles can pass `user` and `group` query parameters to act as another user:

```bash
# Admin user (authenticated via JWT) impersonating john
curl -X GET "http://localhost:8080/usertasks/instance?user=john&group=approver" \
  -H "Authorization: Bearer <admin_jwt_token>"
```

**Security Notes:**
- Only users with roles listed in `kogito.security.auth.impersonation.allowed-for-roles` can impersonate
- Without this configuration, query parameters are ignored when OIDC is enabled
- Audit logs should track impersonation activities

**When to Use:**
- Administrative testing of user-specific workflows
- Support teams troubleshooting user-specific issues
- Delegated task management scenarios

**v9 Approach:**
- Implement `UserTaskAssignmentStrategy` interface as CDI bean
- Framework calls `computeAssignment()` with `IdentityProvider`
- In dev mode: `IdentityProvider` defaults to `"anonymous"`
- In production: `IdentityProvider` backed by JWT token from OIDC

**v9 [`CustomUserTaskAssignmentStrategy.java`](v9-app/src/main/java/com/example/security/security/CustomUserTaskAssignmentStrategy.java):**

```java
import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.UserTaskAssignmentStrategy;

import jakarta.enterprise.context.ApplicationScoped;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class CustomUserTaskAssignmentStrategy implements UserTaskAssignmentStrategy {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public String getName() {
        return "custom-round-robin";
    }

    /**
     * Compute the assignment for a user task.
     *
     * @param userTaskInstance the user task instance being assigned
     * @param identityProvider the framework's IdentityProvider — gives you the
     *                         current user's name and roles from the security context
     * @return Optional containing the selected user ID, or empty if no assignment
     */
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

        // Example: if the current user is an admin, assign to first user
        if (identityProvider != null && identityProvider.hasRole("admin")) {
            return Optional.of(userList.get(0));
        }

        // Default: round-robin assignment
        return Optional.of(selectUserRoundRobin(userList));
    }

    private String selectUserRoundRobin(List<String> users) {
        int index = counter.getAndIncrement() % users.size();
        return users.get(index);
    }
}
```

**Key Features:**
- **Implements `UserTaskAssignmentStrategy`**: Integrates directly with the BAMOE v9 framework
- **Round-Robin Assignment**: Distributes tasks evenly across potential owners
- **Role-Aware**: Uses `IdentityProvider.hasRole()` to make role-based decisions
- **CDI Bean**: No system property configuration needed


### Step 5: Migrate Security Configuration

**v8 Security:** [`kie-deployment-descriptor.xml`](v8-app/src/main/resources/META-INF/kie-deployment-descriptor.xml)

v8 uses KIE-specific configuration with XML:
```xml
<!-- v8 - XML-based security -->
<deployment-descriptor>
    <required-roles>
        <required-role>admin</required-role>
        <required-role>manager</required-role>
        <required-role>approver</required-role>
    </required-roles>
</deployment-descriptor>
```

**v9 Security:** Configure in [`application.properties`](v9-app/src/main/resources/application.properties)

Key v9 OIDC properties:
```properties
# OIDC Configuration (Standard, not Keycloak-specific)
quarkus.oidc.enabled=true
quarkus.oidc.auth-server-url=http://127.0.0.1:8180/realms/kie
quarkus.oidc.client-id=kie-app
quarkus.oidc.credentials.secret=secret

# Path-based security
quarkus.http.auth.permission.authenticated.paths=/*
quarkus.http.auth.permission.authenticated.policy=authenticated

# Dev mode - disable for testing without Keycloak
%dev.quarkus.oidc.enabled=false

```

**Key Changes:** Standard OIDC, properties-based config, automatic token management

### Step 6: Update Maven Dependencies

**V8 vs V9 Dependency Differences:**

```xml
<!-- V8 Dependencies - JavaEE with explicit versions -->
<dependencies>
    <!-- KIE API -->
    <dependency>
        <groupId>org.kie</groupId>
        <artifactId>kie-api</artifactId>
        <version>7.74.1.Final-redhat-00005</version>
    </dependency>

    <!-- JavaEE -->
    <dependency>
        <groupId>javax</groupId>
        <artifactId>javaee-api</artifactId>
        <version>8.0</version>
    </dependency>

    <!-- Keycloak Adapter -->
    <dependency>
        <groupId>org.keycloak</groupId>
        <artifactId>keycloak-core</artifactId>
        <version>15.0.2</version>
    </dependency>
</dependencies>

<!-- V9 Dependencies - Quarkus extensions (versions from BOM) -->
<dependencies>
    <!-- Kogito Core -->
    <dependency>
        <groupId>org.jbpm</groupId>
        <artifactId>jbpm-with-drools-quarkus</artifactId>
    </dependency>

    <!-- OIDC Security - replaces Keycloak adapter -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-oidc</artifactId>
    </dependency>

    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-keycloak-authorization</artifactId>
    </dependency>
</dependencies>
```

**Key Changes:**
- Keycloak adapter → `quarkus-oidc`
- Explicit versions → Managed by Quarkus BOM
- `javax.*` → `jakarta.*` (handled by Quarkus)

### Step 7: Remove v8 Configuration Files

Delete these v8-specific files:
- `META-INF/kmodule.xml`
- `META-INF/kie-deployment-descriptor.xml`
- `keycloak.json`
- `WEB-INF/web.xml`

### Step 8: Build and Test

```bash
# Build v9 application
cd v9-app
mvn clean package -DskipTests

# Run in dev mode
mvn quarkus:dev

# Access Swagger UI
open http://localhost:8080/q/swagger-ui
```

---

## Testing

### Running All Tests

```bash
cd v9-app

# Run unit tests (CustomUserGroupCallbackTest, CustomUserTaskAssignmentStrategyTest)
mvn test

# Run integration tests (UserLookupProcessIT, ApprovalProcessIT) — requires packaged app
mvn verify

# Run in dev mode for manual curl testing
mvn clean compile quarkus:dev
# Swagger UI: http://localhost:8080/q/swagger-ui
```

| Test File | Type | What it tests | Run with |
|-----------|------|---------------|----------|
| [`CustomUserGroupCallbackTest.java`](src/test/java/com/example/security/CustomUserGroupCallbackTest.java) | `@QuarkusTest` unit | `existsUser`, `existsGroup`, `getGroupsForUser`, `mapOidcRolesToGroups` | `mvn test` |
| [`CustomUserTaskAssignmentStrategyTest.java`](src/test/java/com/example/security/CustomUserTaskAssignmentStrategyTest.java) | `@QuarkusTest` unit | `assignFromPotentialOwners()`, round-robin, mock `IdentityProvider`, admin/anonymous identity | `mvn test` |
| [`ApprovalProcessIT.java`](src/test/java/com/example/security/ApprovalProcessIT.java) | `@QuarkusIntegrationTest` | `approvalProcess` BPMN endpoint, `approved=true` | `mvn verify` |
| [`UserLookupProcessIT.java`](src/test/java/com/example/security/UserLookupProcessIT.java) | `@QuarkusIntegrationTest` | `userLookup` BPMN endpoint, external API call via `HttpClient` | `mvn verify` |

### Test 1: Start the Application

```bash
cd v9-app
mvn clean compile quarkus:dev
# Access: http://localhost:8080
# Swagger UI: http://localhost:8080/q/swagger-ui
```

### Test 2: Test the User Lookup Process (REST API Call from BPMN)

The [`userLookup.bpmn`](v9-app/src/main/resources/com/example/security/userLookup.bpmn) process demonstrates calling an external REST API from a BPMN script task using `java.net.http.HttpClient`.

**Process variables:**
- `userId` (Long) — input: the user ID to look up
- `userData` (String) — output: the JSON response from the API

**Start a process instance:**

```bash
# Fetch user with ID 1
curl -X POST http://localhost:8080/userLookup \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"userId": 1}'

# Expected response:
# {"id":"83f18e34-c18b-4d2f-97b3-c2218c621d25","userData":"{\n  \"id\": 1,\n  \"name\": \"Leanne Graham\",\n  \"username\": \"Bret\",\n  \"email\": \"Sincere@april.biz\",\n  \"address\": {\n    \"street\": \"Kulas Light\",\n    \"suite\": \"Apt. 556\",\n    \"city\": \"Gwenborough\",\n    \"zipcode\": \"92998-3874\",\n    \"geo\": {\n      \"lat\": \"-37.3159\",\n      \"lng\": \"81.1496\"\n    }\n  },\n  \"phone\": \"1-770-736-8031 x56442\",\n  \"website\": \"hildegard.org\",\n  \"company\": {\n    \"name\": \"Romaguera-Crona\",\n    \"catchPhrase\": \"Multi-layered client-server neural-net\",\n    \"bs\": \"harness real-time e-markets\"\n  }\n}","userId":1}
```

**Try different user IDs:**

```bash
# Fetch user with ID 2
curl -X POST http://localhost:8080/userLookup \
  -H "Content-Type: application/json" \
  -d '{"userId": 2}'

# Fetch user with ID 5
curl -X POST http://localhost:8080/userLookup \
  -H "Content-Type: application/json" \
  -d '{"userId": 5}'
```

**Key Points:**
- The script task uses `java.net.http.HttpClient` (Java 11+ standard library)
- No CDI lookup or `KogitoProcessContext.from()` — those do not exist in v9
- The `userData` variable is typed as `java.lang.String` (Serializable) — required for Kogito persistence
- The process variable `userData` contains the raw JSON string from the API

### Test 3: Test the Approval Process with User Task

The [`approvalProcess.bpmn`](v9-app/src/main/resources/approvalProcess.bpmn) demonstrates a complete approval workflow with a User Task that requires group membership.

**Process Flow:**
1. Start → User Task ("Approve Request") → Script Task → End

**User Task Configuration:**
- **Potential Groups**: `approver`, `manager` (task assigned to these groups)
- **Potential Users**: `approver`, `manager`  (task is group-based, not assigned to specific users)
- **Owner**: None (unassigned until claimed)
- **Input**: `request` (String)
- **Output**: `approved` (Boolean), `approverComments` (String)

**Note:**
 The task is assigned to groups, and any user who belongs to these groups can claim it.

**Users who can claim this task (based on group membership):**
- ✅ `john` → has `manager`, `approver` groups → **CAN claim**
- ✅ `mary` → has `admin`, `approver` groups → **CAN claim**
- ✅ `alice` → has `finance`, `approver` groups → **CAN claim**
- ❌ `steve` → has `user`, `requestor` groups → **CANNOT claim**
- ❌ `bharu` → has `hr`, `user` groups → **CANNOT claim**

> **💡 Tip:** You can also perform all these operations using the Dev UI at http://localhost:8080/q/dev-ui

```bash
# 1. Start an approval process
curl -X POST http://localhost:8080/approvalProcess \
  -H "Content-Type: application/json" \
  -d '{"request": "Purchase order for $500"}'

# Response includes process instance ID
# {"id":"c9589ec2-4d45-4913-8f88-8d95edb55a41","request":"Purchase order for $500","approved":null,"approverComments":null}

# 2. List all user tasks (returns empty [] without user/group parameters)
curl http://localhost:8080/usertasks/instance

# 3. List tasks for john (has approver group - WILL see the task)
curl "http://localhost:8080/usertasks/instance?user=john&group=manager&group=approver&group=user"

# 4. List tasks for steve (no approver group - will NOT see the task)
curl "http://localhost:8080/usertasks/instance?user=steve&group=user&group=requestor"

# 5. Get specific task details (replace {taskId} with actual task ID)
curl "http://localhost:8080/usertasks/instance/{taskId}?user=john&group=manager&group=approver"

# 6. Complete the task as john (replace {processId} and {taskId} with actual IDs)
# Option A: Using task completion endpoint (shortcut)
curl -X POST "http://localhost:8080/approvalProcess/{processId}/ApproveRequest/{taskId}?phase=complete&user=john&group=approver" \
  -H "Content-Type: application/json" \
  -d '{"approved":true,"approverComments":"Approved by john"}'

# Option B: Using task transition endpoint (generic)
curl -X POST "http://localhost:8080/approvalProcess/{processId}/ApproveRequest/{taskId}/phases/complete?user=john&group=approver" \
  -H "Content-Type: application/json" \
  -d '{"approved":true,"approverComments":"Approved by john"}'
```

### Test 4: Verify IdentityProvider Filtering

Test 3 demonstrates how `IdentityProvider` filters tasks based on user groups:

```bash
# Users WITH approver/manager groups can see the task
curl "http://localhost:8080/usertasks/instance?user=john&group=manager&group=approver"

# Users WITHOUT approver/manager groups cannot see the task
curl "http://localhost:8080/usertasks/instance?user=steve&group=user&group=requestor"
# Returns: empty list
```

### Test 5: Verify UserTaskAssignmentStrategy

The [`CustomUserTaskAssignmentStrategy`](v9-app/src/main/java/com/example/security/security/CustomUserTaskAssignmentStrategy.java) implements round-robin task assignment using `IdentityProvider`.

**Testing Round-Robin Assignment:**

```bash
# Start multiple process instances to verify round-robin distribution
curl -X POST http://localhost:8080/approvalProcess \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"request": "Request 1 - should assign to first user"}'

# 3. Start second approvalProcess instance
curl -X POST http://localhost:8080/approvalProcess \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"request": "Request 2 - should assign to second user (round-robin)"}'

# 4. Start third approvalProcess instance
curl -X POST http://localhost:8080/approvalProcess \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"request": "Request 3 - should assign to third user (round-robin)"}'


# 5. Complete a task as john (replace {processId} and {taskId})
curl -X POST "http://localhost:8080/approvalProcess/{processId}/usertasks/v2/{taskId}/transition" \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{"transitionId": "complete", "data": {}, "user": "john", "groups": ["manager", "approver", "user"]}'
```


### Test 6: Test OIDC Authentication (Production Mode)

**Setup:**
1. Start Keycloak: `docker run -p 8180:8080 -e KEYCLOAK_ADMIN=admin -e KEYCLOAK_ADMIN_PASSWORD=admin quay.io/keycloak/keycloak:latest start-dev`
2. Create realm "kie", client "kie-app" (secret: "secret"), and users with roles
3. Enable OIDC in `application.properties` by commenting out `%dev.quarkus.oidc.enabled=false`

**Test with JWT token:**

```bash
# Get access token from Keycloak
export TOKEN=$(curl -s -X POST http://127.0.0.1:8180/realms/kie/protocol/openid-connect/token \
  -H 'content-type: application/x-www-form-urlencoded' \
  -d "client_id=kie-app" \
  -d "client_secret=secret" \
  -d "grant_type=password" \
  -d "username=john" \
  -d "password=john" \
  -d "scope=openid" | jq -r '.access_token')

# Verify token was obtained
echo $TOKEN

# Start a process with authentication
curl -X POST http://localhost:8080/userLookup \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"userId": 1}'

# Access management APIs with token
curl -H "Authorization: Bearer $TOKEN" \
  http://localhost:8080/management/processes
```


---

## Common Issues

### Issue 1: User Task Names Must Not Contain Spaces

**Symptom:**
- Cannot find form in dev-ui even though forms are generated

**Cause:** User task names with spaces (e.g., "Approve Request") can cause issues with form generation and API endpoints.

**Solution:**

Use camelCase naming without spaces for user task names in BPMN:

```xml
<!-- Wrong - contains space -->
<bpmn2:userTask id="_userTask" name="Approve Request">

<!-- Correct - camelCase, no spaces -->
<bpmn2:userTask id="_userTask" name="ApproveRequest">
```

**Best Practices:**
- Use camelCase: `ApproveRequest`, `ReviewDocument`, `ProcessPayment`
- Avoid spaces, special characters, or hyphens in task names
- Keep names concise and descriptive

### Issue 2: `KogitoProcessContext.from()` Does Not Exist

**Symptom:**
```
[ERROR] cannot find symbol
  symbol:   method from(org.kie.kogito.internal.process.runtime.KogitoProcessContext)
  location: interface org.kie.kogito.internal.process.runtime.KogitoProcessContext
```

**Cause:** BPMN script task uses `KogitoProcessContext.from(kcontext)` to look up CDI beans. This static method does not exist on the `KogitoProcessContext` interface in v9.

**Solution:**

Replace CDI bean lookups in script tasks with standard Java APIs. For HTTP calls, use `java.net.http.HttpClient`:

```java
// ✅ Correct v9 approach in BPMN script tasks
java.net.http.HttpClient httpClient = java.net.http.HttpClient.newHttpClient();
java.net.http.HttpRequest httpRequest = java.net.http.HttpRequest.newBuilder()
    .uri(java.net.URI.create("https://api.example.com/users/" + userId))
    .header("Accept", "application/json")
    .GET()
    .build();
java.net.http.HttpResponse httpResponse = httpClient.send(
    httpRequest, java.net.http.HttpResponse.BodyHandlers.ofString());
kcontext.setVariable("result", httpResponse.body());
```

### Issue 3: `java.lang.Object` Not Supported by Kogito Persistence

**Symptom:**
```
[ERROR] Java type java.lang.Object is not supported by Kogito persistence,
please consider using a class that extends java.io.Serializable
```

**Cause:** A BPMN process variable uses `structureRef="java.lang.Object"` which is not Serializable.

**Solution:**

Change the `structureRef` to a concrete Serializable type in the BPMN file:

```xml
<!-- ❌ Wrong -->
<bpmn2:itemDefinition id="_userDataItem" structureRef="java.lang.Object"/>

<!-- ✅ Correct -->
<bpmn2:itemDefinition id="_userDataItem" structureRef="java.lang.String"/>
```

Or use your own model class that implements `java.io.Serializable`:

```xml
<bpmn2:itemDefinition id="_requestItem" structureRef="com.example.security.model.ApprovalRequest"/>
```

```java
public class ApprovalRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    // ...
}
```

### Issue 4: User Roles Not Working

**Symptom:**
```
403 Forbidden - User does not have required role
```

**Cause:** OIDC roles not mapped to application groups

**Solution:**

Implement role mapping in `CustomUserGroupCallback`:
```java
public List<String> mapOidcRolesToGroups(Set<String> oidcRoles) {
    List<String> groups = new ArrayList<>();
    for (String role : oidcRoles) {
        if (role.startsWith("realm:")) {
            groups.add(role.substring(6)); // Remove "realm:" prefix
        }
    }
    return groups;
}
```

### Issue 5: javax to jakarta Import Errors

**Symptom:**
```
[ERROR] cannot find symbol: class ApplicationScoped
[ERROR] location: package javax.enterprise.context
```

**Cause:** Using javax imports in v9

**Solution:**

Replace all javax imports with jakarta:
```java
// v8
import javax.enterprise.context.ApplicationScoped;

// v9
import jakarta.enterprise.context.ApplicationScoped;
```

### Issue 6: `Table "CORRELATION_INSTANCES" not found` — Database Schema Not Initialized

**Symptom:**
```
org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "CORRELATION_INSTANCES" not found
(this database is empty); SQL statement:
SELECT encoded_correlation_id, correlation FROM correlation_instances WHERE correlated_id = ?
```

**Cause:** Two possible causes:

1. **Missing `kogito.persistence.type=jdbc`** — without this property, the Kogito framework does not trigger Flyway to create the required schema tables (`correlation_instances`, `process_instances`, etc.)

**Solution:**

Add `kogito.persistence.type=jdbc` to [`application.properties`](v9-app/src/main/resources/application.properties):

```properties
# Required: tells Kogito to use JDBC persistence and triggers Flyway schema creation
kogito.persistence.type=jdbc

# Flyway must also be enabled
kie.flyway.enabled=true

# H2 in-memory database
quarkus.datasource.db-kind=h2
quarkus.datasource.username=kogito
quarkus.datasource.jdbc.url=jdbc:h2:mem:default;NON_KEYWORDS=VALUE,KEY
```
---

## Summary

### Migration Checklist

- [ ] Remove `UserGroupCallback` implementation — replaced by `IdentityProvider`
- [ ] Implement `UserTaskAssignmentStrategy` interface as CDI bean
- [ ] Replace Keycloak adapter with Quarkus OIDC
- [ ] Update security configuration in `application.properties`
- [ ] Update all `javax.*` imports to `jakarta.*`
- [ ] Remove v8 configuration files (`kmodule.xml`, `kie-deployment-descriptor.xml`)
- [ ] Update Maven dependencies
- [ ] Replace `KogitoProcessContext.from()` with `java.net.http.HttpClient` in script tasks
- [ ] Change `java.lang.Object` variables to `Serializable` types
- [ ] Test OIDC authentication and role-based access control

### Key Takeaways

1. **UserGroupCallback → IdentityProvider**: No interface to implement; backed by OIDC/JWT
2. **AssignmentStrategy**: Implement `UserTaskAssignmentStrategy` CDI bean
3. **Security**: Standard OIDC replaces vendor-specific adapters
4. **Configuration**: Centralized in `application.properties`
5. **Script Tasks**: Use `java.net.http.HttpClient` instead of `KogitoProcessContext.from()`
6. **Variables**: All process variables must be `Serializable`

### Migration Comparison

| Feature | v8 | v9 |
|---------|----|----|
| **UserGroupCallback** | Custom interface implementation | Framework-provided `IdentityProvider` |
| **AssignmentStrategy** | System property | `UserTaskAssignmentStrategy` CDI bean |
| **Authentication** | Keycloak adapter | Standard OIDC |
| **Configuration** | XML files | `application.properties` |
| **Script Task HTTP** | Custom handlers | `java.net.http.HttpClient` |

### Files Migrated

| v8 File | v9 File | Key Changes |
|---------|---------|-------------|
| [`pom.xml`](v8-app/pom.xml) | [`pom.xml`](v9-app/pom.xml) | Quarkus dependencies |
| [`CustomUserGroupCallback.java`](v8-app/src/main/java/com/example/security/callback/CustomUserGroupCallback.java) | [`CustomUserGroupCallback.java`](v9-app/src/main/java/com/example/security/security/CustomUserGroupCallback.java) | Helper CDI bean for dev/testing; `UserGroupCallback` replaced by `IdentityProvider` |
| N/A | [`CustomUserTaskAssignmentStrategy.java`](v9-app/src/main/java/com/example/security/security/CustomUserTaskAssignmentStrategy.java) | Implements `UserTaskAssignmentStrategy` interface; receives `IdentityProvider` in `computeAssignment()` |
| [`kie-deployment-descriptor.xml`](v8-app/src/main/resources/META-INF/kie-deployment-descriptor.xml) | [`application.properties`](v9-app/src/main/resources/application.properties) | Properties-based config |
| REST WorkItem (v8 `kie-deployment-descriptor.xml`) | [`userLookup.bpmn`](v9-app/src/main/resources/com/example/security/userLookup.bpmn) + [`RestWorkDefinition.wid`](v9-app/src/main/resources/com/example/security/RestWorkDefinition.wid) | Script task uses `java.net.http.HttpClient`; variable typed as `String` |

### Additional Resources

- [IBM BAMOE v8 to v9 Upgrade Guide](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-from-80x)
- [IBM BAMOE: Upgrading UserGroupCallback and AssignmentStrategy](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-usergroupcallback-assignmentstrategy)

---