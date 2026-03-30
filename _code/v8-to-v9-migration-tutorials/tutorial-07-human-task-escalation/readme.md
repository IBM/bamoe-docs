# Tutorial 7: Human Task Management (Escalation & Reassignment) Migration from BAMOE v8 to v9


## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

---

## Introduction

This tutorial demonstrates how to migrate human task management features from BAMOE v8 to v9, focusing on deadline handlers, escalation rules, SLA monitoring, task delegation, and dynamic assignment strategies.

### What You'll Learn

- How human task lifecycle differs between v8 and v9
- Migrating deadline handlers (NotStartedReassign, NotCompletedReassign, NotCompletedNotify)
- Configuring escalation rules and SLA monitoring
- Implementing custom task assignment strategies
- Using the User Task Subsystem REST API
- Task delegation and forwarding patterns
- Testing deadline-based escalations

### What You'll Migrate

**v8 Configuration:**
- `kie-deployment-descriptor.xml` with work item handlers
- `kmodule.xml` with KIE session configuration
- `persistence.xml` for task persistence
- Custom `WorkItemHandler` implementations
- Manual task lifecycle management

**v9 Configuration:**
- `application.properties` with Quarkus configuration
- Kogito User Task Subsystem with REST API
- `UserTaskAssignmentStrategy` interface for custom assignment
- Embedded Jobs Service for deadline processing
- Simplified task lifecycle (default Kogito or WS-Human Task)

### Understanding Human Task Management

#### v8 Human Task Architecture

In BAMOE v8 (based on jBPM 7.x), human tasks are managed through:

1. **kie-deployment-descriptor.xml** - Configures work item handlers and event listeners
2. **kmodule.xml** - Defines KIE sessions and work item handlers
3. **persistence.xml** - JPA configuration for task persistence
4. **Custom Handlers** - Java classes implementing `WorkItemHandler` interface
5. **Manual Lifecycle** - Explicit state transitions required

**v8 Task Lifecycle:**
```
Created → Ready → Reserved → InProgress → Completed
   ↓        ↓         ↓           ↓
Manual transitions required at each step
```

#### v9 Human Task Architecture

In BAMOE v9 (based on Kogito), human tasks are managed through:

1. **application.properties** - Centralized Quarkus configuration
2. **User Task Subsystem** - Built-in REST API for task management
3. **Kogito Add-ons** - `jbpm-quarkus-usertask` and persistence add-ons
4. **Custom Strategy** - Implements `UserTaskAssignmentStrategy` interface
5. **Flexible Lifecycle** - Choose between default Kogito or WS-Human Task

**v9 Task Lifecycle Options:**

**Option 1: Default Kogito Lifecycle (Recommended)**
```
Created → Ready → Reserved → Completed
          ↓         ↓
    Automatic    Manual
    activation   transitions
```

**Available transitions:**
- Ready state: `claim`, `release`, `complete`, `skip`, `fail`, `reassign`
- Reserved state: `release`, `complete`, `skip`, `fail`, `reassign`

**Option 2: WS-Human Task Lifecycle (Advanced)**
```
Created → Ready → Reserved → InProgress → Completed
   ↓        ↓         ↓           ↓
Manual activation and transitions required
```

**Additional transitions:**
- Created state: `activate`
- Reserved state: `start`, `stop`, `delegate`, `forward`

#### Key Differences

| Aspect | v8 | v9 |
|--------|----|----|
| **Configuration** | XML files (kie-deployment-descriptor.xml, kmodule.xml) | application.properties |
| **Task API** | Custom REST endpoints | User Task Subsystem REST API |
| **Custom Logic** | WorkItemHandler classes | UserTaskAssignmentStrategy interface |
| **Deadline Processing** | External timer service | Embedded Jobs Service |
| **Lifecycle** | Manual state management | Automatic (Kogito) or WS-Human Task |
| **Persistence** | JPA with persistence.xml | Kogito persistence add-ons |
| **Dependency Injection** | CDI with beans.xml | Quarkus CDI (automatic) |

---

## Prerequisites

### Required Software

1. **Java 17 or later** (CRITICAL for v9)
   ```bash
   java -version  # Must show 17+
   ```

2. **Maven 3.8.1 or later**
   ```bash
   mvn -version
   ```

3. **Docker and Docker Compose** (for Kafka and MailHog)
   ```bash
   docker --version
   docker-compose --version
   ```

4. **IDE** (VS Code with BAMOE Developer Tools extension recommended)

5. **curl or Postman** for API testing

### Required Knowledge

- Basic understanding of BPMN user tasks
- Familiarity with REST APIs
- Understanding of task lifecycle states
- Basic knowledge of Quarkus configuration

---

## Step-by-Step Migration

### Step 1: Analyze v8 Configuration

First, examine your v8 project structure:

```bash
cd v8-to-v9-upgrade-tutorials/tutorial-07-human-task-escalation/v8-app
```

**Key v8 Files to Review:**

1. **kie-deployment-descriptor.xml** - Work item handlers and persistence settings
2. **kmodule.xml** - KIE session configuration
3. **approvalProcess.bpmn** - Process with user tasks
4. **Custom handlers** - Any WorkItemHandler implementations

### Step 2: Create v9 Project Structure

Navigate to the v9 application directory:

```bash
cd ../v9-app
```

### Step 3: Configure pom.xml

Add the following key dependencies to your v9 [`pom.xml`](v9-app/pom.xml):

```xml
<!-- Core Process Engine -->
<dependency>
    <groupId>org.jbpm</groupId>
    <artifactId>jbpm-with-drools-quarkus</artifactId>
</dependency>

<!-- User Task Storage with JPA (enables WS-Human Task lifecycle) -->
<dependency>
    <groupId>org.jbpm</groupId>
    <artifactId>jbpm-addons-quarkus-usertask-storage-jpa</artifactId>
</dependency>

<!-- Kogito Persistence -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-addons-quarkus-persistence-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-addons-flyway</artifactId>
</dependency>

<!-- Kogito Data-Index (Required for Process Instances view in Dev UI) -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kogito-addons-quarkus-data-index-jpa</artifactId>
</dependency>

<!-- Kogito Jobs Service (Required for deadline processing) -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kogito-addons-quarkus-embedded-jobs</artifactId>
</dependency>
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kogito-addons-quarkus-embedded-jobs-jpa</artifactId>
</dependency>

<!-- Email Notification Dependencies (for NotCompletedNotify) -->
<dependency>
    <groupId>org.jbpm</groupId>
    <artifactId>jbpm-addons-quarkus-mail</artifactId>
</dependency>
<dependency>
    <groupId>org.jbpm</groupId>
    <artifactId>jbpm-addons-quarkus-task-notification</artifactId>
</dependency>

<!-- Kafka for deadline events -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-messaging-kafka</artifactId>
</dependency>
```

### Step 4: Configure application.properties

Create [`application.properties`](v9-app/src/main/resources/application.properties) with the following **essential configurations**:

```properties
# Persistence
kogito.persistence.type=jdbc
%dev.kie.flyway.enabled=true
%dev.quarkus.datasource.db-kind=h2
%dev.quarkus.datasource.username=kogito
%dev.quarkus.datasource.jdbc.url=jdbc:h2:mem:default;NON_KEYWORDS=VALUE,KEY
quarkus.hibernate-orm.database.generation=none

# User Task Lifecycle 
kogito.usertasks.lifecycle=ws-human-task

# Services - Required for deadline processing
kogito.service.url=http://localhost:${quarkus.http.port}
kogito.jobs-service.url=http://localhost:${quarkus.http.port}
kogito.data-index.url=http://localhost:${quarkus.http.port}

# Security 
quarkus.oidc.enabled=false
kogito.auth.enabled=false

# User Groups - Required for task assignment
%dev.bamoe.devui.users.admin.groups=reviewers
%dev.bamoe.devui.users.supervisor.groups=reviewers,management
%dev.bamoe.devui.users.manager.groups=reviewers,management

# Development
quarkus.http.port=8080
quarkus.swagger-ui.always-include=true
quarkus.kogito.devservices.enabled=false
```

> **Note:** The above shows only the essential properties. For the complete configuration including email notifications, Kafka setup, production database settings, and debug logging, refer to the full [`application.properties`](v9-app/src/main/resources/application.properties) file.

**Key Configuration Notes:**

1. **User Task Lifecycle**: `kogito.usertasks.lifecycle=ws-human-task` enables full WS-Human Task specification support with advanced transitions (activate, start, delegate, forward). Omit this for simpler default Kogito lifecycle.

2. **Jobs Service**: The `kogito.jobs-service.url` property is required for deadline handlers (NotStartedReassign, NotCompletedReassign, NotCompletedNotify) to function.

3. **User Groups**: Configure groups in `bamoe.devui.users.<username>.groups` to match the GroupId assignments in your BPMN process.

4. **Flyway**: `kie.flyway.enabled=true` automatically creates required database tables for user tasks and process instances.

### Step 5: Migrate BPMN Process with Deadline Handlers

In your BPMN file ([`approvalProcess.bpmn`](v9-app/src/main/resources/approvalProcess.bpmn)), configure deadline handlers:

#### NotStartedReassign
Reassigns task if not started within 1 minute:
```xml
<bpmn2:dataInputAssociation>
    <bpmn2:targetRef>_reviewTask_NotStartedReassignInputX</bpmn2:targetRef>
    <bpmn2:assignment>
        <bpmn2:from xsi:type="bpmn2:tFormalExpression">[users:supervisor]@[1m]</bpmn2:from>
        <bpmn2:to xsi:type="bpmn2:tFormalExpression">_reviewTask_NotStartedReassignInputX</bpmn2:to>
    </bpmn2:assignment>
</bpmn2:dataInputAssociation>
```

#### NotCompletedReassign
Reassigns task if not completed within 2 minutes:
```xml
<bpmn2:dataInputAssociation>
    <bpmn2:targetRef>_reviewTask_NotCompletedReassignInputX</bpmn2:targetRef>
    <bpmn2:assignment>
        <bpmn2:from xsi:type="bpmn2:tFormalExpression">[users:manager|groups:management]@[2m]</bpmn2:from>
        <bpmn2:to xsi:type="bpmn2:tFormalExpression">_reviewTask_NotCompletedReassignInputX</bpmn2:to>
    </bpmn2:assignment>
</bpmn2:dataInputAssociation>
```

#### NotCompletedNotify
Sends notification if not completed within 90 seconds:
```xml
<bpmn2:dataInputAssociation>
    <bpmn2:targetRef>_reviewTask_NotCompletedNotifyInputX</bpmn2:targetRef>
    <bpmn2:assignment>
        <bpmn2:from xsi:type="bpmn2:tFormalExpression">[users:manager]@[90s]</bpmn2:from>
        <bpmn2:to xsi:type="bpmn2:tFormalExpression">_reviewTask_NotCompletedNotifyInputX</bpmn2:to>
    </bpmn2:assignment>
</bpmn2:dataInputAssociation>
```

### Step 6: Implement Custom Task Assignment Strategy (Optional)

For advanced scenarios, you can implement custom assignment logic. See the v9-app source code for examples.

### Step 7: Generate Custom Forms 

Custom forms can be generated using the BAMOE Canvas or created manually. See the v9-app `custom-forms-dev` directory for examples.

### Step 8: Start Required Services

Before running the application, start Kafka and MailHog using Docker Compose:

```bash
# From the tutorial-07-human-task-escalation directory
docker-compose up -d
```

This will start:
- **Kafka** on `localhost:9092` - for deadline event messaging
- **MailHog** on `localhost:1025` (SMTP) and `localhost:8025` (Web UI) - for email notifications

Verify services are running:
```bash
docker-compose ps
```

Access MailHog Web UI to view sent emails:
```
http://localhost:8025
```

### Step 9: Build and Run

```bash
cd v9-app
mvn clean package -DskipTests
mvn quarkus:dev
```

The application will start on `http://localhost:8080`

**Note**: The application is configured to connect to Kafka and MailHog for deadline notifications. If you don't need to test deadline handlers, you can skip starting these services, but you may see connection warnings in the logs.

---

## Testing

### Configuration Summary

This tutorial supports two testing modes:

**Section A: WS-Human Task Lifecycle (ENABLED by default)**
- Configuration: `kogito.usertasks.lifecycle=ws-human-task`
- Task states:  Reserved → InProgress → Completed
- Requires: `activate` transition before task becomes Ready
- Supports: `delegate`, `forward` transitions

**Section B: Default Kogito Lifecycle (requires configuration change)**
- Configuration: Comment out or remove `kogito.usertasks.lifecycle=ws-human-task`
- Task states: Ready → Reserved → Completed
- Automatic: Tasks start in Ready state (no activate needed)
- Simpler: Fewer transitions required

### Section A: Testing with WS-Human Task Lifecycle ENABLED

> **Current Configuration:** `kogito.usertasks.lifecycle=ws-human-task` (line 232 in application.properties)
>
> **Reference:** Based on [`process-user-tasks-subsystem`](../../process-user-tasks-subsystem) and [`bamoe-examples/process-user-tasks-subsystem`](../../bamoe-examples/process-user-tasks-subsystem) examples

Tests WS-Human Task specification with states: Created → Ready → Reserved → InProgress → Completed

**Key Difference:** When using WS-Human Task lifecycle, the User Task Subsystem API uses `/usertasks/instance` endpoints instead of process-specific task endpoints.

#### Test A1: Start Process and Verify Task Creation

**WS-Human Task lifecycle curl:**
```bash
curl -X POST "http://localhost:8080/approvalProcess" \
  -H "Content-Type: application/json" \
  -d '{"requestor":"john","amount":5000}'
```

**Expected Response:**
```json
{
  "id": "639645a4-1d4e-4ccf-95ca-1d5b54433da5",
  "amount": 5000.0,
  "approved": null,
  "requestor": "john"
}
```

#### Test A2: Query Tasks Using User Task Subsystem API

**WS-Human Task lifecycle curl - Query by user only:**
```bash
curl -X GET "http://localhost:8080/usertasks/instance?user=admin" \
  -H "accept: application/json"
```

**Expected Response:**
```json
[
  {
    "id": "a9d49d6e-6700-431d-9856-61c46af97ee5",
    "userTaskId": "_reviewTask",
    "status": {
      "terminate": null,
      "name": "Reserved"
    },
    "processInfo": {
      "processInstanceId": "639645a4-1d4e-4ccf-95ca-1d5b54433da5",
      "processId": "approvalProcess",
      "processVersion": "1.0"
    },
    "taskName": "review_request",
    "taskPriority": "5",
    "potentialUsers": ["admin"],
    "potentialGroups": ["reviewers"],
    "adminUsers": [],
    "adminGroups": [],
    "excludedUsers": [],
    "externalReferenceId": "d6f18cdb-78c2-48e2-8bf1-b3d5a763fc82",
    "actualOwner": "admin",
    "inputs": {
      "amount": 5000.0,
      "requestor": "john"
    },
    "outputs": {},
    "metadata": {
      "ProcessType": "BPMN",
      "Lifecycle": "ws-human-task",
      "Skippable": "false",
      "NodeInstanceId": "44063b0d-3923-47a5-adf7-b2be3368aebe",
      "ProcessInstanceState": 1
    }
  }
]
```

**WS-Human Task lifecycle curl - Query by both user and group:**
```bash
curl -X GET "http://localhost:8080/usertasks/instance?group=reviewers&user=admin" \
  -H "accept: application/json"
```

**Expected Response:**
```json
[
  {
    "id": "a9d49d6e-6700-431d-9856-61c46af97ee5",
    "userTaskId": "_reviewTask",
    "status": {
      "terminate": null,
      "name": "Reserved"
    },
    "processInfo": {
      "processInstanceId": "639645a4-1d4e-4ccf-95ca-1d5b54433da5",
      "processId": "approvalProcess",
      "processVersion": "1.0"
    },
    "taskName": "review_request",
    "taskPriority": "5",
    "potentialUsers": ["admin"],
    "potentialGroups": ["reviewers"],
    "adminUsers": [],
    "adminGroups": [],
    "excludedUsers": [],
    "externalReferenceId": "d6f18cdb-78c2-48e2-8bf1-b3d5a763fc82",
    "actualOwner": "admin",
    "inputs": {
      "amount": 5000.0,
      "requestor": "john"
    },
    "outputs": {},
    "metadata": {
      "ProcessType": "BPMN",
      "Lifecycle": "ws-human-task",
      "Skippable": "false",
      "NodeInstanceId": "44063b0d-3923-47a5-adf7-b2be3368aebe",
      "ProcessInstanceState": 1
    }
  }
]
```

#### Test A3: Check Available Transitions

**WS-Human Task lifecycle curl:**
```bash
curl -X GET "http://localhost:8080/usertasks/instance/<task-id>/transition?user=admin" \
  -H "accept: application/json"
```

**Example with actual task ID:**
```bash
curl -X GET "http://localhost:8080/usertasks/instance/a9d49d6e-6700-431d-9856-61c46af97ee5/transition?user=admin" \
  -H "accept: application/json"
```

**Expected Response:**
```json
[
  {
    "transitionId": "release",
    "source": {
      "terminate": null,
      "name": "Reserved"
    },
    "target": {
      "terminate": null,
      "name": "Ready"
    }
  },
  {
    "transitionId": "forward",
    "source": {
      "terminate": null,
      "name": "Reserved"
    },
    "target": {
      "terminate": null,
      "name": "Ready"
    }
  },
  {
    "transitionId": "start",
    "source": {
      "terminate": null,
      "name": "Reserved"
    },
    "target": {
      "terminate": null,
      "name": "InProgress"
    }
  },
  {
    "transitionId": "delegate",
    "source": {
      "terminate": null,
      "name": "Reserved"
    },
    "target": {
      "terminate": null,
      "name": "Reserved"
    }
  },
  {
    "transitionId": "fault",
    "source": {
      "terminate": null,
      "name": "Reserved"
    },
    "target": {
      "terminate": "ERROR",
      "name": "Error"
    }
  },
  {
    "transitionId": "exit",
    "source": {
      "terminate": null,
      "name": "Reserved"
    },
    "target": {
      "terminate": "EXITED",
      "name": "Exited"
    }
  },
  {
    "transitionId": "suspend",
    "source": {
      "terminate": null,
      "name": "Reserved"
    },
    "target": {
      "terminate": null,
      "name": "Suspended"
    }
  }
]
```

**Note:** Available transitions depend on the current task state. The User Task Subsystem API provides a consistent way to query available transitions.

#### Test A4: Start and Complete Task Using User Task Subsystem API

**WS-Human Task lifecycle curl - Start the task (transition from Reserved to InProgress):**
```bash
curl -X POST "http://localhost:8080/usertasks/instance/<task-id>/transition?user=admin&group=reviewers" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "start",
  "data": {
    "approved": true
  }
}'
```

**Example with actual task ID:**
```bash
curl -X POST "http://localhost:8080/usertasks/instance/a9d49d6e-6700-431d-9856-61c46af97ee5/transition?user=admin&group=reviewers" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "start",
  "data": {
    "approved": true
  }
}'
```

**Expected Response:**
```json
{
  "id": "a9d49d6e-6700-431d-9856-61c46af97ee5",
  "userTaskId": "_reviewTask",
  "status": {
    "terminate": null,
    "name": "InProgress"
  },
  "processInfo": {
    "processInstanceId": "639645a4-1d4e-4ccf-95ca-1d5b54433da5",
    "processId": "approvalProcess",
    "processVersion": "1.0"
  },
  "taskName": "review_request",
  "taskPriority": "5",
  "potentialUsers": ["manager"],
  "potentialGroups": ["management"],
  "adminUsers": [],
  "adminGroups": [],
  "excludedUsers": [],
  "externalReferenceId": "d6f18cdb-78c2-48e2-8bf1-b3d5a763fc82",
  "actualOwner": "admin",
  "inputs": {
    "amount": 5000.0,
    "requestor": "john"
  },
  "outputs": {},
  "metadata": {
    "ProcessType": "BPMN",
    "Lifecycle": "ws-human-task",
    "Skippable": "false",
    "NodeInstanceId": "44063b0d-3923-47a5-adf7-b2be3368aebe",
    "ProcessInstanceState": 1
  }
}
```

**WS-Human Task lifecycle curl - Complete the task:**
```bash
curl -X POST "http://localhost:8080/usertasks/instance/<task-id>/transition?user=admin&group=reviewers" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "complete",
  "data": {
    "approved": true
  }
}'
```

**Example with actual task ID:**
```bash
curl -X POST "http://localhost:8080/usertasks/instance/a9d49d6e-6700-431d-9856-61c46af97ee5/transition?user=admin&group=reviewers" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "complete",
  "data": {
    "approved": true
  }
}'
```

**Expected Response:**
```json
{
  "id": "a9d49d6e-6700-431d-9856-61c46af97ee5",
  "userTaskId": "_reviewTask",
  "status": {
    "terminate": "COMPLETED",
    "name": "Completed"
  },
  "processInfo": {
    "processInstanceId": "639645a4-1d4e-4ccf-95ca-1d5b54433da5",
    "processId": "approvalProcess",
    "processVersion": "1.0"
  },
  "taskName": "review_request",
  "taskPriority": "5",
  "potentialUsers": ["manager"],
  "potentialGroups": ["management"],
  "adminUsers": [],
  "adminGroups": [],
  "excludedUsers": [],
  "externalReferenceId": "d6f18cdb-78c2-48e2-8bf1-b3d5a763fc82",
  "actualOwner": "admin",
  "inputs": {
    "amount": 5000.0,
    "requestor": "john"
  },
  "outputs": {
    "approved": true
  },
  "metadata": {
    "ProcessType": "BPMN",
    "Lifecycle": "ws-human-task",
    "Skippable": "false",
    "NodeInstanceId": "44063b0d-3923-47a5-adf7-b2be3368aebe",
    "ProcessInstanceState": 1
  }
}
```

**NOTE**: In place of 'a9d49d6e-6700-431d-9856-61c46af97ee5' , replace with task id from your response.

### Section B: Testing with Default Kogito Lifecycle

> **Configuration Change Required:**
> 1. Edit [`application.properties`](v9-app/src/main/resources/application.properties)
> 2. Comment out line 9: `# kogito.usertasks.lifecycle=ws-human-task`
> 3. Edit [`approvalProcess.bpmn`](v9-app/src/main/resources/approvalProcess.bpmn)
> 4. Comment out ActorId assignment (lines 64-70):
>    ```xml
>    <!-- Comment out this entire block:
>    <bpmn2:dataInputAssociation>
>      <bpmn2:targetRef>_reviewTask_ActorIdInputX</bpmn2:targetRef>
>      <bpmn2:assignment>
>        <bpmn2:from xsi:type="bpmn2:tFormalExpression">admin</bpmn2:from>
>        <bpmn2:to xsi:type="bpmn2:tFormalExpression">_reviewTask_ActorIdInputX</bpmn2:to>
>      </bpmn2:assignment>
>    </bpmn2:dataInputAssociation>
>    -->
>    ```
> 5. Restart the application
>
> **Why comment out ActorId?**
> - With ActorId set to "admin", the task is automatically assigned/reserved to that specific user
> - In default Kogito lifecycle, this causes the task to skip the "Ready" state and go directly to "Reserved"
> - By removing ActorId, the task stays in "Ready" state and can be claimed by any user in the "reviewers" group
> - This demonstrates the group-based task assignment pattern typical in default Kogito lifecycle
>


Tests default Kogito lifecycle with states: Ready → Reserved → Completed (simpler, automatic activation)

#### Test B1: Start Process and Verify Task Creation

**Default Kogito lifecycle curl:**
```bash
curl -X POST "http://localhost:8080/approvalProcess" \
  -H "Content-Type: application/json" \
  -d '{"requestor":"jane","amount":3000}'
```

**Expected Response:**
```json
{
  "id": "4122f400-a4d6-40ca-9a5f-11357fbe876d",
  "amount": 3000.0,
  "approved": null,
  "requestor": "jane"
}
```

#### Test B2: Query Tasks by Group

**Default Kogito lifecycle curl:**
```bash
curl -X GET "http://localhost:8080/usertasks/instance?group=reviewers&user=admin" \
  -H "accept: application/json"
```

**Expected Response:**
```json
[
  {
    "id": "36bdc7ef-4d17-42fd-bbfe-54bd3f4f0a71",
    "userTaskId": "_reviewTask",
    "status": {
      "terminate": null,
      "name": "Ready"
    },
    "processInfo": {
      "processInstanceId": "4122f400-a4d6-40ca-9a5f-11357fbe876d",
      "processId": "approvalProcess",
      "processVersion": "1.0"
    },
    "taskName": "review_request",
    "taskPriority": "5",
    "potentialUsers": [],
    "potentialGroups": ["reviewers"],
    "adminUsers": [],
    "adminGroups": [],
    "excludedUsers": [],
    "externalReferenceId": "4365c75c-725f-4aa7-a92c-96e93de78eb2",
    "inputs": {
      "amount": 3000.0,
      "requestor": "jane"
    },
    "outputs": {},
    "metadata": {
      "ProcessType": "BPMN",
      "Lifecycle": "kogito",
      "Skippable": "false",
      "NodeInstanceId": "9ef7606e-13c6-4d4e-b228-f72e5e7b8816",
      "ProcessInstanceState": 1
    }
  }
]
```

#### Test B3: Claim Task

**Default Kogito lifecycle curl:**
```bash
curl -X POST "http://localhost:8080/usertasks/instance/<task-id>/transition?group=reviewers&user=admin" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "claim"
}'
```

**Example with actual task ID:**
```bash
curl -X POST "http://localhost:8080/usertasks/instance/36bdc7ef-4d17-42fd-bbfe-54bd3f4f0a71/transition?group=reviewers&user=admin&group=reviewers" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "claim"
}'
```

**Expected Response:**
```json
{
  "id": "36bdc7ef-4d17-42fd-bbfe-54bd3f4f0a71",
  "userTaskId": "_reviewTask",
  "status": {
    "terminate": null,
    "name": "Reserved"
  },
  "processInfo": {
    "processInstanceId": "4122f400-a4d6-40ca-9a5f-11357fbe876d",
    "processId": "approvalProcess",
    "processVersion": "1.0"
  },
  "taskName": "review_request",
  "taskPriority": "5",
  "potentialUsers": [],
  "potentialGroups": ["reviewers"],
  "adminUsers": [],
  "adminGroups": [],
  "excludedUsers": [],
  "externalReferenceId": "4365c75c-725f-4aa7-a92c-96e93de78eb2",
  "actualOwner": "admin",
  "inputs": {
    "amount": 3000.0,
    "requestor": "jane"
  },
  "outputs": {},
  "metadata": {
    "ProcessType": "BPMN",
    "Lifecycle": "kogito",
    "Skippable": "false",
    "NodeInstanceId": "9ef7606e-13c6-4d4e-b228-f72e5e7b8816",
    "ProcessInstanceState": 1
  }
}
```

#### Test B4: Complete Claimed Task

**Default Kogito lifecycle curl:**
```bash
curl -X POST "http://localhost:8080/usertasks/instance/<task-id>/transition?user=admin&group=reviewers" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "complete",
  "data": {
    "approved": true
  }
}'
```

**Example with actual task ID:**
```bash
curl -X POST "http://localhost:8080/usertasks/instance/36bdc7ef-4d17-42fd-bbfe-54bd3f4f0a71/transition?user=admin&group=reviewers" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "complete",
  "data": {
    "approved": true
  }
}'
```

**Expected Response:**
```json
{
  "id": "36bdc7ef-4d17-42fd-bbfe-54bd3f4f0a71",
  "userTaskId": "_reviewTask",
  "status": {
    "terminate": "COMPLETED",
    "name": "Completed"
  },
  "processInfo": {
    "processInstanceId": "4122f400-a4d6-40ca-9a5f-11357fbe876d",
    "processId": "approvalProcess",
    "processVersion": "1.0"
  },
  "taskName": "review_request",
  "taskPriority": "5",
  "potentialUsers": [],
  "potentialGroups": ["reviewers"],
  "adminUsers": [],
  "adminGroups": [],
  "excludedUsers": [],
  "externalReferenceId": "4365c75c-725f-4aa7-a92c-96e93de78eb2",
  "actualOwner": "admin",
  "inputs": {
    "amount": 3000.0,
    "requestor": "jane"
  },
  "outputs": {
    "approved": true
  },
  "metadata": {
    "ProcessType": "BPMN",
    "Lifecycle": "kogito",
    "Skippable": "false",
    "NodeInstanceId": "9ef7606e-13c6-4d4e-b228-f72e5e7b8816",
    "ProcessInstanceState": 1
  }
}
```

### Testing Deadline Handlers (Both Lifecycles)

These deadline handlers work with both WS-Human Task and Default Kogito lifecycles.

#### Test D1: NotStartedReassign (1 minute deadline)

```bash
# Start process and wait 1 minute without starting the task
curl -X POST http://localhost:8080/approvalProcess \
  -H "Content-Type: application/json" \
  -d '{"requestor":"deadline-test","amount":1000}'

# After 1 minute, check if task was reassigned to supervisor


curl -X GET "http://localhost:8080/usertasks/instance?user=supervisor" \
  -H "accept: application/json"
```

#### Test D2: NotCompletedReassign (2 minute deadline)

```bash
# Start task but don't complete it, wait 2 minutes

# For WS-Human Task lifecycle (Section A):
curl -X POST "http://localhost:8080/usertasks/instance/<task-id>/transition?user=admin" \
  -H "Content-Type: application/json" \
  -d '{"transitionId":"start"}'

# For Default Kogito lifecycle (Section B):
curl -X POST "http://localhost:8080/usertasks/instance/<task-id>/transition?user=admin&group=reviewers" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "claim"
}'

# After 2 minutes, check if task was reassigned to manager

# For WS-Human Task lifecycle (Section A):
curl "http://localhost:8080/usertasks/instance?user=manager&group=management"

# For Default Kogito lifecycle (Section B):
curl -X GET "http://localhost:8080/usertasks/instance?user=manager&group=management" \
  -H "accept: application/json"
```

#### Test D3: NotCompletedNotify (90 seconds)

```bash
# Start task and wait 90 seconds without completing
# Check application logs for notification message
```

### Testing with Dev UI

Access at `http://localhost:8080/q/dev-ui` to start processes and manage tasks through the UI.


---

## Common Issues

### Issue 1: Transitions Endpoint Returns Empty Response

**Symptom:**
```bash
# Getting transitions returns nothing
curl "http://localhost:8080/usertasks/instance/<task-id>/transition?user=admin"
# Returns: (empty response)

# Executing transitions returns nothing
curl -X POST "http://localhost:8080/usertasks/instance/<task-id>/transition?user=admin" \
  -H "Content-Type: application/json" \
  -d '{"transitionId":"start"}'
# Returns: (empty response)
```

**Cause:** The task has already been claimed/reserved (possibly by the Dev UI) and is in a different state than expected. When a task is assigned with ActorId, it may be automatically claimed.

**Solution:**

1. **Check current task state first:**

```bash
curl -X GET "http://localhost:8080/usertasks/instance?user=admin&group=reviewers" \
  -H "accept: application/json"
```

2. **Based on the status.name value, use the correct transition:**
   - Created: Use `activate` or `start`
   - Ready: Use `claim` or `complete`
   - Reserved: Use `start` or `complete`
   - InProgress: Use `complete`

3. **If task is already Reserved, complete it directly:**
```bash
curl -X POST "http://localhost:8080/usertasks/instance/<task-id>/transition?user=admin" \
  -H "accept: application/json" \
  -H "Content-Type: application/json" \
  -d '{
  "transitionId": "complete",
  "data": {
    "approved": true
  }
}'
```
### Issue 2 Invalid Transition Error

**Error:**
```
Invalid transition claim from UserTaskState [name=Reserved]
```

**Cause:** Task is already claimed/reserved by another user or in wrong state.

**Solution:**
```bash
# Check current task state
curl -X GET "http://localhost:8080/usertasks/instance?user=admin&group=reviewers" \
  -H "accept: application/json"

# Check available transitions
curl -X GET "http://localhost:8080/usertasks/instance/<task-id>/transition?user=admin" \
  -H "accept: application/json"

# Use the correct transition based on current state
```

### Issue 3: Authorization Error

**Error:**
```
user X with roles [] not authorized
```

**Cause:** User doesn't have required groups configured.

**Solution:** Verify user groups in [`application.properties`](v9-app/src/main/resources/application.properties):
```properties
%dev.bamoe.devui.users.admin.groups=reviewers
%dev.bamoe.devui.users.supervisor.groups=reviewers,management
```

### Issue 4: Tasks Not Appearing in Queries

**Symptom:** Task query returns empty array even though task exists.

**Cause:** User not in correct group or ActorId mismatch.

**Solution:**
1. Verify user groups in application.properties
2. Check BPMN ActorId and GroupId assignments
3. Use correct query parameters: `?user=X&group=Y`

### Issue 5: Deadline Handlers Not Triggering

**Symptom:** Deadlines don't fire after specified time, or email notifications not received.

**Cause:** Jobs Service not properly configured, Kafka/MailHog not running, or task already in progress.

**Solution:**
1. Verify Kafka and MailHog are running: `docker-compose ps`
2. Check Jobs Service dependencies in [`pom.xml`](v9-app/pom.xml)
3. Verify [`application.properties`](v9-app/src/main/resources/application.properties) configuration:
   - `kogito.jobs-service.url` is set correctly
   - Kafka configuration is correct (`kafka.bootstrap.servers=localhost:9092`)
   - MailHog SMTP is configured (`quarkus.mailer.host=localhost`, `quarkus.mailer.port=1025`)
4. Ensure task remains in initial state (don't claim/activate before deadline)
5. Check application logs for job execution and Kafka message publishing
6. Access MailHog UI at `http://localhost:8025` to verify emails are being sent

---

## Summary

### What You've Accomplished

- Migrated human task configuration from v8 XML to v9 properties
- Configured deadline handlers (NotStartedReassign, NotCompletedReassign, NotCompletedNotify)
- Tested both WS-Human Task and default Kogito lifecycles
- Used User Task Subsystem REST API for task management
- Implemented escalation rules with embedded Jobs Service

### Key Takeaways

1. **v9 simplifies configuration** - Single `application.properties` replaces multiple XML files
2. **Flexible lifecycle options** - Choose between default Kogito (simpler) or WS-Human Task (advanced)
3. **Built-in deadline processing** - Embedded Jobs Service handles escalations automatically
4. **REST API standardization** - User Task Subsystem provides consistent API across all processes

### Migration Checklist

- [ ] Update [`pom.xml`](v9-app/pom.xml) with required dependencies
- [ ] Create [`application.properties`](v9-app/src/main/resources/application.properties) with Kogito configuration
- [ ] Set up [`docker-compose.yaml`](docker-compose.yaml) for Kafka and MailHog
- [ ] Start required services with `docker-compose up -d`
- [ ] Migrate BPMN process with deadline handlers
- [ ] Configure user groups for task assignment
- [ ] Test with appropriate lifecycle (WS-Human Task or default Kogito)
- [ ] Verify deadline handlers trigger correctly
- [ ] Stop services when done with `docker-compose down`


### Additional Resources

- [BAMOE v9 Documentation](https://www.ibm.com/docs/en/ibamoe)