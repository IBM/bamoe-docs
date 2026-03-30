# Tutorial 12: Database Integration — BAMOE v8 to v9 Migration

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Migration Steps](#migration-steps)
4. [Process Overview](#process-overview)
5. [Testing](#testing)
6. [Common Issues](#common-issues)
7. [Summary](#summary)

---

## Introduction

This tutorial shows how to migrate **database integration** from BAMOE v8 to BAMOE v9.

**In v8**, you configured the database via two KIE Server system properties and a `persistence.xml` file:

```properties
# v8 — KIE Server system properties
org.kie.server.persistence.ds=java:jboss/datasources/ExampleDS
org.kie.server.persistence.dialect=org.hibernate.dialect.PostgreSQLDialect
```

**In v9**, your application is a Quarkus microservice. Database configuration is done in `application.properties` using standard Quarkus datasource properties. Each subsystem (Workflow Engine, UserTask Service, Data-Index, Jobs Service) is enabled by adding its Maven dependency — they all share the same datasource automatically.

| Aspect | v8 | v9 |
|--------|----|----|
| Persistence config | `org.kie.server.persistence.ds` + `persistence.xml` | `kogito.persistence.type=jdbc` + `quarkus.datasource.*` |
| Datasource lookup | JNDI (`java:jboss/datasources/ExampleDS`) | Direct JDBC URL — no JNDI |
| Schema management | `hibernate.hbm2ddl.auto=update` in `persistence.xml` | `kie.flyway.enabled=true` (dev) or DDL scripts (prod) |
| Timer/Job scheduling | `org.kie.executor.*` properties | Jobs Service (`kogito-addons-quarkus-embedded-jobs`) |
| Async executor config | `org.kie.executor.pool.size`, `org.kie.executor.retry.count` | **No equivalent** — Jobs Service handles this automatically |

---

## Prerequisites

- Java 17+, Maven 3.8.1+
- A database: **H2** (dev, embedded) or **PostgreSQL** (production recommended)

---

## Migration Steps

### Step 1: Update `pom.xml` Dependencies

**Remove** `persistence.xml` and `kie-deployment-descriptor.xml` from your project.

**Remove** v8 dependencies (`kie-api`, `javax.persistence-api`, etc.).

**Add** the following v9 dependencies — each one enables a subsystem:

```xml
<!-- Workflow Engine JDBC persistence -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-addons-quarkus-persistence-jdbc</artifactId>
</dependency>

<!-- UserTask Service JPA storage -->
<dependency>
    <groupId>org.jbpm</groupId>
    <artifactId>jbpm-addons-quarkus-usertask-storage-jpa</artifactId>
</dependency>

<!-- Data-Index JPA (GraphQL query API) -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kogito-addons-quarkus-data-index-jpa</artifactId>
</dependency>

<!-- Jobs Service (timer/deadline scheduling) — replaces org.kie.executor.* -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kogito-addons-quarkus-embedded-jobs</artifactId>
</dependency>
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kogito-addons-quarkus-embedded-jobs-jpa</artifactId>
</dependency>

<!-- KIE Flyway (automatic schema creation in dev) -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-addons-flyway</artifactId>
</dependency>

<!-- JDBC driver — choose one -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jdbc-h2</artifactId>          <!-- dev -->
</dependency>
<!-- or quarkus-jdbc-postgresql / quarkus-jdbc-mssql / quarkus-jdbc-oracle -->
```

> See the full working file: [`v9-app/pom.xml`](v9-app/pom.xml)

---

### Step 2: Configure `application.properties`

**Remove** all v8 properties:

```properties
# Remove these — no equivalent in v9
org.kie.server.persistence.ds=...
org.kie.server.persistence.dialect=...
org.kie.executor.pool.size=...
org.kie.executor.retry.count=...
```

**Add** the key v9 properties:

```properties
# Replaces org.kie.server.persistence.ds + org.kie.server.persistence.dialect
kogito.persistence.type=jdbc
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;NON_KEYWORDS=VALUE,KEY

# Replaces hibernate.hbm2ddl.auto=update in persistence.xml
kie.flyway.enabled=true

# Required — Hibernate must not try to manage Kogito tables
quarkus.hibernate-orm.database.generation=none

# Dev UI task users — prevents Human Tasks going to Reserved state
%dev.bamoe.devui.users.john.groups=john,managers,users
```

> See the full working file: [`v9-app/src/main/resources/application.properties`](v9-app/src/main/resources/application.properties)

---

### Step 3: Migrate the Work Item Handler

**V8** — `DatabaseWorkItemHandler` extended `AbstractWorkItemHandler` and used `@PersistenceContext` / JNDI for the datasource:

```java
// v8
public class DatabaseWorkItemHandler extends AbstractWorkItemHandler {
    @PersistenceContext
    private EntityManager em;

    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        // ...
        manager.completeWorkItem(workItem.getId(), results);
    }
}
```

**V9** — extend `DefaultKogitoWorkItemHandler`, use CDI `@Inject` for the datasource, and implement `activateWorkItemHandler`:

```java
// v9 — key changes highlighted
@ApplicationScoped
public class DatabaseWorkItemHandler extends DefaultKogitoWorkItemHandler {

    @Inject DataSource dataSource;          // CDI injection — no JNDI

    @Override
    public Optional<WorkItemTransition> activateWorkItemHandler(
            KogitoWorkItemManager manager,
            KogitoWorkItemHandler handler,
            KogitoWorkItem workItem,
            WorkItemTransition transition) {

        String query = (String) workItem.getParameter("Query");
        // ... execute query, build QueryResult ...

        Map<String, Object> output = new HashMap<>();
        output.put("Results", queryResult);     // return object directly
        output.put("RowCount", queryResult.getRowCount());

        return Optional.of(handler.completeTransition(workItem.getPhaseStatus(), output));
    }
}
```

> See the full implementation: [`v9-app/src/main/java/com/example/database/handler/DatabaseWorkItemHandler.java`](v9-app/src/main/java/com/example/database/handler/DatabaseWorkItemHandler.java)

**Register the handler** via a `@Produces` method (v9 replaces `kie-deployment-descriptor.xml`):

```java
// v9 — replaces kie-deployment-descriptor.xml work item handler registration
@ApplicationScoped
public class DatabaseWorkItemHandlerConfig extends DefaultWorkItemHandlerConfig {
    @Inject DatabaseWorkItemHandler handler;

    @PostConstruct
    void init() {
        register("DatabaseTask", handler);
    }
}
```

> See: [`v9-app/src/main/java/com/example/database/config/DatabaseWorkItemHandlerConfig.java`](v9-app/src/main/java/com/example/database/config/DatabaseWorkItemHandlerConfig.java)

---

### Step 4: Process Variable Types

In v9, process variables that are custom types must be **concrete classes** with a no-arg constructor and standard getters/setters. Kogito generates marshallers for them automatically.

The `results` variable in `database-process.bpmn` uses `com.example.database.model.QueryResult`:

```java
// QueryResult — used as BPMN process variable type
public class QueryResult implements Serializable {
    private List<RowData> rows;
    private int rowCount;
    // no-arg constructor + getters + setters required
}

// RowData — each row's columns stored as a JSON string
public class RowData implements Serializable {
    private String data;   // e.g. "{\"column1\":\"value1\"}"
    // no-arg constructor + getters + setters required
}
```

> See: [`v9-app/src/main/java/com/example/database/model/`](v9-app/src/main/java/com/example/database/model/)

---

### Step 5: Build and Run

```bash
cd v9-app
mvn clean quarkus:dev
```

If you are using Gradle, use the following command:

```shell script
gradle clean quarkusDev
```

- **Swagger UI:** `http://localhost:8080/q/swagger-ui`
- **GraphQL UI:** `http://localhost:8080/q/dev-ui` → Data Index GraphQL UI
- **Health:** `http://localhost:8080/q/health`

---

## Process Overview

The [`database-process.bpmn`](v9-app/src/main/resources/database-process.bpmn) demonstrates all four v9 subsystems in a single process:

```
Start
  │
  ▼
Execute Database Query  ← Workflow Engine (JDBC persistence)
  │                       Work Item Handler executes SQL via DataSource
  ▼
ReviewQueryResults ──────────────────────────────────────────────────┐
  │  (Human Task assigned to 'john')                                  │
  │  UserTask Service (JPA storage)                                   │ Timer Boundary
  │                                                                   │ PT30M
  ▼                                                                   ▼
End (COMPLETED)                                              Escalation End
                                                             (process ABORTED
                                                              if task not
                                                              completed in 30 min)
```

### Subsystems demonstrated

| Node | Subsystem | Dependency |
|------|-----------|------------|
| Execute Database Query | Workflow Engine (JDBC) | `kie-addons-quarkus-persistence-jdbc` |
| ReviewQueryResults | UserTask Service | `jbpm-addons-quarkus-usertask-storage-jpa` |
| Timer Boundary (PT30M) | **Jobs Service** | `kogito-addons-quarkus-embedded-jobs` + `kogito-addons-quarkus-embedded-jobs-jpa` |
| GraphQL queries | Data-Index | `kogito-addons-quarkus-data-index-jpa` |

### Timer Boundary Event — v8 vs v9

In **v8**, task deadlines were configured via `org.kie.executor.*` system properties or deadline notifications in the task form. In **v9**, you add a **Timer Boundary Event** directly in the BPMN:

```xml
<!-- BPMN Timer Boundary Event — fires PT30M after ReviewQueryResults starts -->
<bpmn2:boundaryEvent id="_timerBoundary" name="ReviewTimeout"
                     attachedToRef="_reviewTask" cancelActivity="true">
  <bpmn2:timerEventDefinition>
    <bpmn2:timeDuration xsi:type="bpmn2:tFormalExpression">PT30M</bpmn2:timeDuration>
  </bpmn2:timerEventDefinition>
</bpmn2:boundaryEvent>
```

- `cancelActivity="true"` — interrupting: cancels the task when the timer fires
- `PT30M` — ISO 8601 duration: 30 minutes. Use `PT10S` for 10 seconds in testing.
- The Jobs Service persists the scheduled timer to the database and fires it reliably even after a restart.

---

## Testing

### Test 1: Health Check

```bash
curl http://localhost:8080/q/health
```

Expected: `{"status":"UP","checks":[...]}`

---

### Test 2: Start a Process Instance

```bash
curl -X POST http://localhost:8080/database-process \
  -H "Content-Type: application/json" \
  -d '{
    "approved": true,
    "comments": "string",
    "query": "SELECT 1",
    "rowCount": 0,
    "results": {
      "rows": [{"data": "string"}],
      "rowCount": 0
    }
  }'
```

Expected response — process is `ACTIVE`, waiting at the Human Task:

```json
{"id":"374f879d-483f-4e23-8766-fd992da7907b","approved":true,"comments":"string","query":"SELECT 1","rowCount":1,"results":{"rows":[{"data":"{\"?column?\":1}"}],"rowCount":1}}
```

Note the `id` — you will need it for subsequent steps.

---

### Test 3: Query via Data-Index GraphQL

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ ProcessInstances { id state processName } }"}'
```

Expected: `{"data":{"ProcessInstances":[{"id":"374f879d-483f-4e23-8766-fd992da7907b","state":"ACTIVE","processName":"database-process"}]}}`

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ UserTaskInstances { id name state actualOwner } }"}'
```

Expected: `{"data":{"UserTaskInstances":[{"id":"189274d1-29b2-400b-833d-a95d51231da2","name":"ReviewQueryResults","state":"Reserved","actualOwner":"john"}]}}`

---

### Test 4: Verify Timer Job is Scheduled

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ Jobs { id processInstanceId status scheduledId } }"}'
```

Expected — one job scheduled for the timer boundary:

```json
{"data":{"Jobs":[{"id":"d0498c94-4b8b-423f-81b3-29338ca874ab","processInstanceId":"374f879d-483f-4e23-8766-fd992da7907b","status":"SCHEDULED","scheduledId":null}]}}
```

This confirms the Jobs Service persisted the timer to the database.

---

### Test 5: Complete the UserTask (Happy Path)

Go to **BAMOE Dev UI → Tasks** (`http://localhost:8080/q/dev-ui`) and complete the `ReviewQueryResults` task as user `john`.


After completing, the process moves to `COMPLETED` and the timer job is cancelled.

---

### Test 6: Test the Timer Escalation Path

To test the timer without waiting 30 minutes, temporarily change the timer duration in the BPMN to `PT10S` (10 seconds):

```xml
<bpmn2:timeDuration xsi:type="bpmn2:tFormalExpression">PT10S</bpmn2:timeDuration>
```

Start a new process instance (Test 2), wait 10 seconds, then query:

```bash
curl -X POST http://localhost:8080/graphql \
  -H "Content-Type: application/json" \
  -d '{"query": "{ ProcessInstances { id state } }"}'
```

Expected: process state changes to `ABORTED` (timer fired, task cancelled, escalation end reached).

> **Remember to revert** the timer back to `PT30M` after testing.

---

## Common Issues

### Tables not created on startup

`kie.flyway.enabled=true` must be set **and** the add-on dependency must be in `pom.xml`. KIE Flyway only creates tables for subsystems whose dependencies are present.

### Human Task goes to `Reserved` instead of `Ready`

Add dev user configuration to `application.properties`:
```properties
%dev.bamoe.devui.users.john.groups=john,managers,users
```

### `CORRELATION_INSTANCES` table not found

`kogito.persistence.type=jdbc` is missing from `application.properties`.

### `jbpm_user_tasks` table not found

`jbpm-addons-quarkus-usertask-storage-jpa` dependency is missing from `pom.xml`.

### H2 JSONB type error

Add `NON_KEYWORDS=VALUE,KEY` to the H2 JDBC URL:
```properties
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;MODE=PostgreSQL;NON_KEYWORDS=VALUE,KEY
```

### Timer does not fire

Ensure both Jobs Service dependencies are present in `pom.xml`:
```xml
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kogito-addons-quarkus-embedded-jobs</artifactId>
</dependency>
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kogito-addons-quarkus-embedded-jobs-jpa</artifactId>
</dependency>
```

### V8 async executor properties have no effect

`org.kie.executor.*` properties are **not supported in v9**. Remove them. The Jobs Service manages execution automatically.

---

## Summary

| What changed | v8 | v9 |
|---|---|---|
| Datasource config | JNDI + `persistence.xml` | `quarkus.datasource.*` in `application.properties` |
| Schema creation | `hibernate.hbm2ddl.auto=update` | `kie.flyway.enabled=true` |
| Work item handler base class | `AbstractWorkItemHandler` | `DefaultKogitoWorkItemHandler` |
| Datasource injection | `@PersistenceContext` / JNDI | `@Inject DataSource` (CDI) |
| Handler registration | `kie-deployment-descriptor.xml` | `DefaultWorkItemHandlerConfig` `@PostConstruct` |
| Task query API | `GET /kie-server/.../queries/tasks/instances` | `GET /usertasks/v2` or GraphQL |
| Process query API | `GET /kie-server/.../queries/processes/instances` | GraphQL `{ ProcessInstances { ... } }` |
| Timer/deadline scheduling | `org.kie.executor.*` properties | Timer Boundary Event in BPMN + Jobs Service |
| Async executor | `org.kie.executor.*` properties | **No equivalent** — Jobs Service handles automatically |
