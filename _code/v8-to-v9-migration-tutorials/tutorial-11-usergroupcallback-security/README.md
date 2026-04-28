# Tutorial 11: Custom Identity Provider - Security Migration from v8 to v9

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

## Introduction

This tutorial demonstrates implementing custom authorization in BAMOE v9, replacing v8's UserGroupCallback with a custom IdentityProvider.

### How It Works

When a request arrives with ?user=john, the UserContextFilter intercepts it and calls CustomUserGroupCallback to get john's roles. These are stored in CustomIdentityProvider using ThreadLocal. When the framework needs to authorize a task, it calls CustomIdentityProvider.getName() and getRoles(), which return john's identity and roles. The framework then checks if john has the required role and grants or denies access accordingly.

### v8 to v9 Migration Overview

| Aspect | v8 | v9 |
|--------|----|----|
| UserGroupCallback | org.kie.api.task.UserGroupCallback interface | org.kie.kogito.auth.IdentityProvider with @Alternative and @Priority |
| AssignmentStrategy | System property org.jbpm.task.assignment.strategy | UserTaskAssignmentStrategy interface (CDI bean) |
| Configuration | kie-deployment-descriptor.xml | CDI beans and application.properties |
| User/Group Source | Custom implementation (LDAP/DB/Properties) | Query parameters (dev) or JWT tokens (prod) |

## Prerequisites

- Java 17+
- Maven 3.8.1+
- Understanding of CDI and JAX-RS filters
- Optional: Docker for Keycloak in production mode

## Step-by-Step Migration

### Step 1: Create CustomIdentityProvider

Create [CustomIdentityProvider.java](v9-app/src/main/java/com/example/security/security/CustomIdentityProvider.java) that implements org.kie.kogito.auth.IdentityProvider.

Key requirements:
- Add @Alternative and @Priority(1) annotations to override the default QuarkusIdentityProvider
- Use ThreadLocal to store user context per request
- Implement getName(), getRoles(), and hasRole() methods
- Inject CustomUserGroupCallback for user-group lookups

### Step 2: Create UserContextFilter

Create [UserContextFilter.java](v9-app/src/main/java/com/example/security/security/UserContextFilter.java) as a JAX-RS filter.

Key requirements:
- Annotate with @Provider
- Implement both ContainerRequestFilter and ContainerResponseFilter
- Extract user from query parameter (?user=john)
- Call CustomUserGroupCallback to get user's roles
- Set user context in CustomIdentityProvider using setCurrentUser()
- Clear context in response filter to prevent memory leaks

### Step 3: Update CustomUserGroupCallback

Update [CustomUserGroupCallback.java](v9-app/src/main/java/com/example/security/security/CustomUserGroupCallback.java) to serve as a helper bean.

Key requirements:
- Remove v8's UserGroupCallback interface implementation
- Keep as @ApplicationScoped CDI bean
- Maintain user-to-roles mapping (e.g., john maps to manager, approver, user)
- Provide getGroupsForUser(String userId) method

### Step 4: Implement CustomUserTaskAssignmentStrategy (Optional)

Create [CustomUserTaskAssignmentStrategy.java](v9-app/src/main/java/com/example/security/security/CustomUserTaskAssignmentStrategy.java) for custom task assignment logic.

Key requirements:
- Implement UserTaskAssignmentStrategy interface
- Annotate with @ApplicationScoped
- Implement custom assignment logic (e.g., round-robin, role-based)

### Step 5: Configure application.properties

Update [application.properties](v9-app/src/main/resources/application.properties) for dev and production modes.

Dev Mode (without OIDC):
```properties
%dev.quarkus.oidc.enabled=false
%dev.quarkus.http.auth.permission.authenticated.policy=permit
```

Production Mode (with Keycloak):
```properties
quarkus.oidc.enabled=true
quarkus.oidc.auth-server-url=http://127.0.0.1:8180/realms/kie
quarkus.oidc.client-id=kie-app
quarkus.oidc.credentials.secret=secret
```

Persistence:
```properties
kogito.persistence.type=jdbc
kie.flyway.enabled=true
```

## Testing

### Build and Run

```bash
cd v9-app
mvn clean compile quarkus:dev
```

Access:
- Application: http://localhost:8080
- Swagger UI: http://localhost:8080/q/swagger-ui
- Dev UI: http://localhost:8080/q/dev-ui

### Test 1: Approval Process with Custom Authorization

Step 1: Start an approval process

```bash
curl -X POST http://localhost:8080/approvalProcess \
  -H "Content-Type: application/json" \
  -d '{"request": "Purchase order for $500"}'
```

Expected response:
```json
{
  "id":"9246547d-5d7c-4ee7-a4be-d6ae76575bfb",
  "request":"Purchase order for $500",
  "approved":null,
  "approverComments":null
}
```

Step 2: List tasks using query parameters

The GET method supports `user` and `group` query parameters for authorization:

**Using user parameter only:**
```bash
# List tasks for user john (uses groups from CustomUserGroupCallback)
curl "http://localhost:8080/usertasks/instance?user=john"
```

**Using user and group parameters:**
```bash
# List tasks for user john with specific group validation
curl "http://localhost:8080/usertasks/instance?user=steve&group=approver"
```

If the user doesn't have the specified group, you'll receive a 403 Forbidden response:
```json
{
  "message": "User steve does not have group approver",
  "status": 403
}
```

Step 3: List tasks for authorized user

```bash
curl "http://localhost:8080/usertasks/instance?user=john"
```

Expected: Returns tasks because john has the approver role

Step 4: List tasks for unauthorized user

```bash
curl "http://localhost:8080/usertasks/instance?user=steve"
```

Expected: Returns empty list because steve doesn't have the approver role

Step 5: Test group validation

```bash
# This will succeed - john has the approver group
curl "http://localhost:8080/usertasks/instance?user=john&group=approver"

# This will fail with 403 - steve doesn't have the approver group
curl "http://localhost:8080/usertasks/instance?user=steve&group=approver"
```

Expected response for unauthorized group:
```json
{
  "message": "User steve does not have group approver",
  "status": 403
}
```

Step 6: Try to claim task (unauthorized user)

```bash
curl -X POST "http://localhost:8080/usertasks/instance/$TASK_ID/transition?user=steve" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{"transitionId": "claim"}'
```

Expected response:
```json
{
  "message": "User steve is not authorized to claim task",
  "status": 403
}
```

Step 7: Claim the task (authorized user)

```bash
TASK_ID="76d40c80-b8bf-4686-856b-06c75fa954cd"

curl -X POST "http://localhost:8080/usertasks/instance/$TASK_ID/transition?user=john" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{"transitionId": "claim"}'
```

Expected: Success - john has approver role




Step 8: Complete the task

```bash
curl -X POST "http://localhost:8080/usertasks/instance/$TASK_ID/transition?user=john" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "complete",
  "outputs": {
    "approved": true,
    "approverComments": "Approved by john"
  }
}'
```

Expected: Task completed successfully with approval data captured

### Test 2: User Authorization Matrix

Task requirement: The approvalProcess task requires the approver role.

Users who can claim the task:
- john has manager, approver, user roles - CAN claim
- mary has admin, approver, user roles - CAN claim
- alice has finance, approver, user roles - CAN claim
- steve has user, requestor roles - CANNOT claim (missing approver)
- bharu has hr, user roles - CANNOT claim (missing approver)

Test commands:
```bash
curl "http://localhost:8080/usertasks/instance?user=john"    # Returns tasks
curl "http://localhost:8080/usertasks/instance?user=mary"    # Returns tasks
curl "http://localhost:8080/usertasks/instance?user=alice"   # Returns tasks
curl "http://localhost:8080/usertasks/instance?user=steve"   # Returns empty
curl "http://localhost:8080/usertasks/instance?user=bharu"   # Returns empty
```

### Test 3: Verification - Custom Authorization Components

Expected log output when running approval process tests:

```
INFO  [UserContextFilter] UserContextFilter: Setting user context for user: john
INFO  [CustomUserGroupCallback] Getting groups for user john: [manager, approver, user]
INFO  [UserContextFilter] UserContextFilter: Using groups from CustomUserGroupCallback: [manager, approver, user]
DEBUG [CustomIdentityProvider] Setting current user: john with roles: [manager, approver, user]
DEBUG [CustomIdentityProvider] CustomIdentityProvider.getName() returning: john
```

Verification checklist:
- UserContextFilter called - Intercepts request and extracts user from query parameter
- CustomUserGroupCallback called - Provides user groups
- CustomIdentityProvider.setCurrentUser() called - Sets ThreadLocal context
- CustomIdentityProvider.getName() called - Framework uses custom provider for authorization
- Context cleanup - ThreadLocal cleared after response

Component call flow:
```
HTTP Request (?user=john)
  -> UserContextFilter.filter() (Request)
  -> CustomUserGroupCallback.getGroupsForUser("john")
  -> Returns: [manager, approver, user]
  -> CustomIdentityProvider.setCurrentUser("john", [manager, approver, user])
  -> Framework calls CustomIdentityProvider.getName()
  -> Returns: "john"
  -> Framework calls CustomIdentityProvider.getRoles()
  -> Returns: [manager, approver, user]
  -> Framework checks authorization (has "approver" role?)
  -> Result: TRUE
  -> Task operation allowed
  -> UserContextFilter.filter() (Response)
  -> CustomIdentityProvider.clearCurrentUser()
```

### Test 4: OIDC Authentication (Production Mode)

Setup Keycloak:

```bash
docker run -p 8180:8080 \
  -e KEYCLOAK_ADMIN=admin \
  -e KEYCLOAK_ADMIN_PASSWORD=admin \
  quay.io/keycloak/keycloak:latest start-dev
```

Verify Keycloak Server Access:
- Open your browser and navigate to: http://localhost:8180/auth
- Ensure that the Keycloak server is running properly.
- Log in to the Keycloak Administration Console using the following credentials:
  - Username: admin
  - Password: admin

Import Realm Configuration:

To import the provided kie-relam.json (realm configuration file), follow these steps:
- In the Admin Console, click on Manage Realm.
- Select Create Realm.
- Click on Browse and locate the kie-relam.json file from the 'keyCloak-config' directory of this example.
- Select the file and click Create.
 
 After successful import, the new realm should appear in the realm dropdown.


Enable OIDC in application.properties:
```properties
# Comment out this line:
# %dev.quarkus.oidc.enabled=false
```

Test with JWT token:

```bash
# Get access token
export TOKEN=$(curl -s -X POST http://localhost:8180/realms/kie/protocol/openid-connect/token \
  -H 'content-type: application/x-www-form-urlencoded' \
  -d "client_id=kie-app" \
  -d "client_secret=secret" \
  -d "grant_type=password" \
  -d "username=john" \
  -d "password=john" \
  -d "scope=openid" | jq -r '.access_token')

# Start a process with authentication
curl -X POST http://localhost:8080/approvalProcess \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"request": "Authenticated request"}'

# List tasks with authentication (using JWT token)

curl -H "Authorization: Bearer $TOKEN" \
  "http://localhost:8080/usertasks/instance?user=john"
```

## Common Issues

### Issue 1: AmbiguousResolutionException

Symptom:
```
AmbiguousResolutionException: Ambiguous dependencies for type org.kie.kogito.auth.IdentityProvider
- CustomIdentityProvider
- QuarkusIdentityProvider (framework default)
```

Solution: Add @Alternative and @Priority(1) to CustomIdentityProvider

### Issue 2: CustomIdentityProvider Not Called

Symptom: No logs from CustomIdentityProvider

Solution:
1. Verify @Alternative and @Priority(1) are present
2. Check CDI beans in Dev UI: http://localhost:8080/q/dev-ui
3. Enable debug logging:
```properties
quarkus.log.category."com.example.security.security".level=DEBUG
```

### Issue 3: User Context Not Cleared

Symptom: User context leaks between requests

Solution: Verify UserContextFilter implements both ContainerRequestFilter and ContainerResponseFilter

### Issue 4: Table "CORRELATION_INSTANCES" not found

Symptom:
```
org.h2.jdbc.JdbcSQLSyntaxErrorException: Table "CORRELATION_INSTANCES" not found
```

Solution: Add to application.properties:
```properties
kogito.persistence.type=jdbc
kie.flyway.enabled=true
```

### Issue 5: User Task Names Must Not Contain Spaces

Symptom: Cannot find form in dev-ui

Solution: Use camelCase naming without spaces in BPMN file

## Summary

### What Was Achieved

- Custom authorization in v9 - Replaces v8's UserGroupCallback pattern
- Framework calls your code - CustomIdentityProvider is used for all authorization decisions
- Works without OIDC - Uses query parameters for dev/testing
- Production ready - Can be extended for JWT, LDAP, database integration
- Verified working - Logs confirm custom components are called

### Architecture

```
Query Params -> UserContextFilter -> CustomIdentityProvider -> Authorization
                                           |
                                   CustomUserGroupCallback (fallback)
```

### v8 vs v9 Comparison

| Aspect | v8 | v9 |
|--------|----|----|
| Interface | UserGroupCallback | IdentityProvider |
| Method Called | getGroupsForUser(userId) | getName(), getRoles(), hasRole() |
| Configuration | kie-deployment-descriptor.xml | CDI bean with @Alternative and @Priority |
| Context | Method parameter | ThreadLocal storage |
| Filter | N/A | JAX-RS filter extracts query params |




### Migration Checklist

- CustomIdentityProvider - Implement with @Alternative and @Priority(1)
- UserContextFilter - Extract user/groups from requests
- CustomUserGroupCallback - Helper bean for user-role mappings
- CustomUserTaskAssignmentStrategy - Optional custom assignment logic
- application.properties - Configure dev and prod modes
- Test dev mode with query parameters
- Test production mode with Keycloak/OIDC

### References

- [IBM BAMOE: Upgrading UserGroupCallback](https://www.ibm.com/docs/en/ibamoe/9.4.0?topic=projects-upgrading-usergroupscallback-assignmentstrategy)