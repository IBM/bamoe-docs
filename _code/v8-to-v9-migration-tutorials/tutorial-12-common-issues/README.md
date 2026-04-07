# Tutorial 13: Common Migration Issues and Solutions - BAMOE v8 to v9

**Complete Self-Contained Reference Guide**

This tutorial consolidates ALL common migration issues from BAMOE v8 to v9, including issues from all other tutorials, test files, and known issues documentation. No external references needed.

## Table of Contents

1. [Introduction](#introduction)
2. [All 11 Core Migration Issues](#all-11-core-migration-issues)
3. [Additional Issues from Tutorials](#additional-issues-from-tutorials)
4. [Quick Reference Tables](#quick-reference-tables)
5. [Migration Workflow](#migration-workflow)
6. [Summary](#summary)

---

## Introduction

This is your **reference** for migrating from BAMOE v8.0.x to v9.3.x. Few known issues are documented here with:
- Problem description
- Error messages
- v8 code examples (that fail)
- v9 solutions (that work)
- Migration steps

### How to Use This Guide

- **Before Migration:** Read all issues to prepare
- **During Migration:** Search for error messages
- **After Migration:** Verify against all issues

---

## All 11 Core Migration Issues

### Issue #1: javax → jakarta Package Migration

**Problem:** All javax.* must become jakarta.*  

**Error:**
```
[ERROR] package javax.persistence does not exist
```

**v8 Code (Fails):**
```java
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;
import javax.inject.Inject;
```

**v9 Solution:**
```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import jakarta.inject.Inject;
```

**Complete Mapping:**
```
javax.persistence.*    → jakarta.persistence.*
javax.validation.*     → jakarta.validation.*
javax.transaction.*    → jakarta.transaction.*
javax.enterprise.*     → jakarta.enterprise.*
javax.inject.*         → jakarta.inject.*
javax.ws.rs.*          → jakarta.ws.rs.*
javax.servlet.*        → jakarta.servlet.*
javax.annotation.*     → jakarta.annotation.*
javax.xml.bind.*       → jakarta.xml.bind.*
javax.mail.*           → jakarta.mail.*
javax.authorization.*  → jakarta.authorization.*
javax.json.*           → jakarta.json.*
```

**Architectural Changes in v9:**

v9 uses a fundamentally different architecture based on Kogito and Quarkus. The v8 service APIs (`org.jbpm.services.api.*`) are not available in v9.

**In v9, use auto-generated REST endpoints** for process interaction (no custom service layer needed).

**Common v8 APIs with v9 Alternatives:**

```
# Query and pagination
org.kie.api.runtime.query.QueryContext               → Use pagination parameters in REST endpoints (page, size)

# v8 Work Item Handlers → v9 Kogito Work Item Handlers
org.kie.api.runtime.process.WorkItemHandler         → org.kie.kogito.internal.process.workitem.KogitoWorkItemHandler
org.kie.api.runtime.process.WorkItemManager         → org.kie.kogito.internal.process.workitem.KogitoWorkItemManager
org.kie.api.runtime.process.WorkItem                → org.kie.kogito.internal.process.workitem.KogitoWorkItem

# v8 User Task APIs → v9 Kogito User Task APIs
org.jbpm.services.task.commands.*                   → org.kie.kogito.usertask.UserTaskService
org.kie.api.task.UserGroupCallback                  → org.kie.kogito.auth.IdentityProvider
org.kie.api.task.model.Task                         → org.kie.kogito.usertask.UserTaskInstance
org.kie.api.task.model.TaskSummary                  → org.kie.kogito.usertask.view.UserTaskView

# v8 Event Listeners → v9 Kogito Event Listeners
org.kie.api.event.process.ProcessEventListener      → org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener
org.kie.api.event.rule.AgendaEventListener          → org.kie.api.event.rule.AgendaEventListener (unchanged)
org.kie.api.event.rule.RuleRuntimeEventListener     → org.kie.api.event.rule.RuleRuntimeEventListener (unchanged)

# v8 Rule Units → v9 Kogito Rule Units
org.kie.api.runtime.rule.RuleUnit                   → org.kie.kogito.rules.RuleUnitData
org.drools.core.command.runtime.rule.InsertObjectCommand → Use DataSource/DataStore in RuleUnitData
```

**Important Notes:**

1. **REST Endpoints**: v8 KIE Server REST endpoints (`org.kie.server.remote.rest.*`) are not directly available in v9. Instead, create Quarkus REST endpoints using `jakarta.ws.rs.*` annotations and `jakarta.ws.rs.core.Response` for response building.

2. **API Documentation**: Both v8 and v9 auto-generate API documentation (Swagger/OpenAPI), so no manual annotation migration is needed.

3. **Work Item Handlers**: v8 work item handler interfaces are replaced with Kogito equivalents:
   - `WorkItemHandler` → `KogitoWorkItemHandler`
   - `WorkItemManager` → `KogitoWorkItemManager`
   - `WorkItem` → `KogitoWorkItem`

4. **User Tasks**: v8 task service APIs are replaced with `org.kie.kogito.usertask.UserTaskService` and related classes.

5. **Security**: v8's `UserGroupCallback` is replaced with `org.kie.kogito.auth.IdentityProvider` for authentication and authorization.

**Quick Fix for javax → jakarta:**
```bash
find . -name "*.java" -exec sed -i 's/import javax\./import jakarta./g' {} \;
```

**References:**
- See **Tutorial 12: Database Integration** for service API migration patterns

---

### Issue #2: Variable Context Does Not Exist

**Problem:** `context` variable not available in v9

**Error:**
```
[ERROR] Variable 'context' does not exist
```


**Solution:**
```xml
<bpmn2:script>
  System.out.println("ID: " + kcontext.getProcessInstance().getId());
</bpmn2:script>
```

**Fix:** Replace `context` with `kcontext`

**Note:** The `kcontext` variable was also available in v8, so using `kcontext` ensures compatibility across both versions.

#### Understanding kcontext

The `kcontext` variable provides access to the process context within script tasks. It is a global context for the process that allows you to access and modify process variables. The `kcontext` variable is an instance of the `KogitoProcessContext` interface (`org.kie.kogito.internal.process.runtime.KogitoProcessContext`).

**Accessing Process Variables:**

You can access process variables in script tasks in two ways:

1. **Directly by name**: Process variables can be accessed directly by their variable name in the script. For example, if you have a process variable named `traveler`, you can access it directly as `traveler.getHotelId()`.

2. **Using `kcontext` methods**: You can also use the `kcontext` variable to access and modify process variables programmatically:
   * `kcontext.getVariable("variableName")` - Reads the value of a process variable
   * `kcontext.setVariable("variableName", value)` - Sets or updates a process variable

**Example:**
```xml
<bpmn2:script>
  // Access process instance information
  System.out.println("Process ID: " + kcontext.getProcessInstance().getId());
  
  // Get a process variable using kcontext
  String customerName = (String) kcontext.getVariable("customerName");
  
  // Set a process variable using kcontext
  kcontext.setVariable("processedBy", "ScriptTask");
  
  // Or access variables directly by name (if defined in process)
  // System.out.println("Customer: " + customerName);
</bpmn2:script>
```

---

### Issue #3: Raw List Type Without Type Parameter

**Problem:** Raw `List` types cause Jandex errors  

**Error:**
```
[ERROR] Raw type 'List' is not allowed. Specify: List<T>
```

**v8 Code (Fails):**
```java
private List items;  // Raw type
public List getItems() { return items; }
```

**v9 Solution:**
```java
private List<OrderItem> items;  // Parameterized
public List<OrderItem> getItems() { return items; }
```

**Fix:** Always specify type parameter: `List<Type>`

---

### Issue #4: Boolean Getter Method Naming

**Problem:** Boolean getters should use `is` prefix instead of `get` for better Java naming conventions

**v8 Code (May Fail):**
```java
private boolean approved;
public boolean getApproved() { return approved; }  // Wrong
```

**v9 Solution:**
```java
private boolean approved;
public boolean isApproved() { return approved; }  // Correct
```

**Fix:** Use `isXxx()` for boolean getters

**Note:** While `getApproved()` is syntactically correct and will compile, `isApproved()` is the preferred way according to JavaBeans naming conventions. Using `is` prefix for boolean getters:
- Improves code readability
- Follows Java best practices
- Ensures better integration with frameworks that rely on JavaBeans conventions
- Makes the code more maintainable and consistent with industry standards

---


### Issue #5: @Entity Annotation on Process Variables

**Problem:** JPA entities as process variables cause Hibernate errors  

**Error:**
```
[ERROR] Hibernate validation failed for entity 'Order'
[ERROR] Entity not mapped to database table
```

**v8 Code (May Fail):**
```java
@Entity
public class Order {  // Used as process variable
    @Id
    private Long id;
}
```

**v9 Solution (Option 1 - DTO):**
```java
// Use DTO for process variables
public class OrderDTO {  // No @Entity
    private Long id;
    private String orderNumber;
}

// Keep entity separate
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue
    private Long id;
}
```

**v9 Solution (Option 2 - Proper Config):**
```java
@Entity
@Table(name = "orders")  // Add @Table
public class Order implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
```

**Fix:** Use DTOs for process variables OR properly configure JPA entities

---

### Issue #6: NPE in Error Handling

**Problem:** Null pointer when accessing error details in catch blocks

**Error:**
```
[ERROR] NullPointerException at line 597
[ERROR] Cannot access error context


**v9 Solution:**
```xml
<bpmn2:scriptTask id="ErrorHandler">
  <bpmn2:script>
    // Check for null before accessing recommended
    Object errorObj = kcontext.getVariable("errorMessage");
    String errorMsg = (errorObj != null) ? errorObj.toString() : "Unknown error";
    System.out.println("Error: " + errorMsg);
  </bpmn2:script>
</bpmn2:scriptTask>
```

**Fix:**
2. Always check for null before accessing error context variables

**Best Practice:**
Always use null-safe access patterns when working with error context variables, and ensure proper data mapping in your BPMN error event definitions.

---

### Issue #7: Legacy Rules with Rule Units

**Problem:** Cannot mix legacy DRL rules with rule units  

**Error:**
```
[ERROR] Cannot mix legacy rules with rule units
```

**v8 Code :**
```drl
package com.example.rules;

rule "Underage"
    ruleflow-group "validation"
    when
        $a : Applicant(age < 18)
    then
        $a.setValid(false);
end
```

**v9 Solution (Rule Unit):**
```drl
package com.example.rules;
unit MortgageRuleUnit;

rule "Underage"
    when
        $a : /applicants[age < 18]
    then
        $a.setValid(false);
end
```

**Rule Unit Class:**
```java
public class MortgageRuleUnit implements RuleUnitData {
    private DataSource<Applicant> applicants;
    
    public DataSource<Applicant> getApplicants() {
        return applicants;
    }
}
```

**Fix:** Convert ALL rules to rule units

---

### Issue #8: null$ Values Being Printed

**Problem:** Uninitialized variables print as "null$"  

**Output:**
```
Process variables: {name=null$, age=25}
```

**v8 Code (Shows null$):**
```java
Map<String, Object> params = new HashMap<>();
params.put("name", null);  // Will show as null$
params.put("age", 25);
```

**v9 Solution:**
```java
Map<String, Object> params = new HashMap<>();
params.put("name", "");  // Empty string instead
params.put("age", 25);

// Or filter nulls
params.entrySet().removeIf(e -> e.getValue() == null);
```

**Fix:** Initialize variables with default values, not null

---

### Issue #9: Null on Omitted Values

**Problem:** Omitted variables written as explicit null in database  

**v8 Behavior:** Omitted fields remain unset  
**v9 Behavior:** Omitted fields written as NULL

**v8 Code:**
```java
Map<String, Object> params = new HashMap<>();
params.put("requiredField", "value");
// optionalField not set - remains unset in v8
```

**v9 Solution:**
```java
Map<String, Object> params = new HashMap<>();
params.put("requiredField", "value");
// Explicitly set defaults for optional fields
params.put("optionalField", "");
params.put("optionalNumber", 0);
params.put("optionalList", new ArrayList<>());
```

**Fix:** Explicitly set default values for all optional fields

---

## Additional Issues from Tutorials

### Issue #10: Form Migration (.frm files)

**Source:** Tutorial 01 (Evaluation Process)

**Problem:** v8 `.frm` form files not supported in v9  

**v8:** Used `.frm` files for task forms  
**v9:** Use form generation or custom React/TypeScript forms

**Solution:**
```bash
# v9 generates forms automatically
# Or create custom forms in src/main/resources/custom-forms-dev/

# Example: evaluation_PerformanceEvaluation.tsx
export const PerformanceEvaluation = () => {
  return (
    <div>
      <h2>Performance Evaluation</h2>
      {/* Custom form fields */}
    </div>
  );
};
```

---

### Issue #11: kmodule.xml Not Needed

**Source:** Tutorial 01, 02

**Problem:** v8 requires `META-INF/kmodule.xml`  
**v9:** Not needed (Quarkus auto-configuration)

**v8 Structure:**
```
src/main/resources/
  META-INF/
    kmodule.xml          ← Not needed in v9
    kie-deployment-descriptor.xml  ← Not needed in v9
    persistence.xml      ← Configure in application.properties
```

**v9 Structure:**
```
src/main/resources/
  application.properties  ← All configuration here
  evaluation.bpmn
  TrafficViolation.dmn
```

**Fix:** Remove kmodule.xml, configure in application.properties

---

### Issue #12: Packaging Change (kjar → jar)

**Source:** Tutorial 10 (Multi-Module)

**Problem:** v8 uses `<packaging>kjar</packaging>`  
**v9:** Uses standard `<packaging>jar</packaging>`

**v8 pom.xml:**
```xml
<packaging>kjar</packaging>
<build>
  <plugins>
    <plugin>
      <groupId>org.kie</groupId>
      <artifactId>kie-maven-plugin</artifactId>
    </plugin>
  </plugins>
</build>
```

**v9 pom.xml:**
```xml
<packaging>jar</packaging>
<build>
  <plugins>
    <plugin>
      <groupId>io.quarkus</groupId>
      <artifactId>quarkus-maven-plugin</artifactId>
    </plugin>
  </plugins>
</build>
```

**Fix:** Change packaging to jar, use Quarkus plugin

---

### Issue #13: ruleflow-group → Rule Units

**Source:** Tutorial 10 (Multi-Module)

**Problem:** v8 uses `ruleflow-group` in DRL  
**v9:** Uses rule units with OOPath

**v8 DRL:**
```drl
rule "Validate Order"
    ruleflow-group "validation"
    when
        $order : Order(amount > 1000)
    then
        $order.setNeedsApproval(true);
end
```

**v9 DRL:**
```drl
unit OrderRuleUnit;

rule "Validate Order"
    when
        $order : /orders[amount > 1000]
    then
        $order.setNeedsApproval(true);
end
```

**Fix:** Convert to rule units with OOPath syntax

---

### Issue #14: Work Item Handlers Registration


**Problem:** v8 registers handlers in kie-deployment-descriptor.xml
**v9:** Register via CDI beans

**v8 Registration:**
```xml
<kie-deployment-descriptor>
  <work-item-handlers>
    <work-item-handler>
      <resolver>mvel</resolver>
      <identifier>new com.example.EmailHandler()</identifier>
      <name>Email</name>
    </work-item-handler>
  </work-item-handlers>
</kie-deployment-descriptor>
```

**v9 Registration:**

In v9, WorkItemHandler needs to be registered using a configuration class that extends `DefaultWorkItemHandlerConfig`:

```java
@ApplicationScoped
public class CustomWorkItemHandlerConfig extends DefaultWorkItemHandlerConfig {
    {
        register("MyConcatDefinitions", new MyConcatWorkItemHandler());
    }
}
```

The work item handler implementation extends `DefaultKogitoWorkItemHandler`:

```java
public class MyConcatWorkItemHandler extends DefaultKogitoWorkItemHandler
```

**Complete Example:**

```java
// Configuration class
@ApplicationScoped
public class EmailHandlerConfig extends DefaultWorkItemHandlerConfig {
    {
        register("Email", new EmailHandler());
    }
}

// Handler implementation
public class EmailHandler extends DefaultKogitoWorkItemHandler {
    
    @Override
    public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
        // Get parameters from work item
        String to = (String) workItem.getParameter("To");
        String subject = (String) workItem.getParameter("Subject");
        
        // Implementation logic
        System.out.println("Sending email to: " + to);
        
        // Complete the work item
        manager.completeWorkItem(workItem.getId(), null);
    }
    
    @Override
    public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
        // Cleanup logic
    }
}
```

**Fix:**
1. Create a configuration class annotated with `@ApplicationScoped` that extends `DefaultWorkItemHandlerConfig`
2. Register handlers in the configuration class using `register("HandlerName", new HandlerInstance())`
3. Implement handlers by extending `DefaultKogitoWorkItemHandler`

---


### Issue #16: Database Configuration

**Source:** Tutorial 12 (Database Integration)

**Problem:** v8 and v9 have different database configuration approaches

#### v8 KIE Server Configuration

In v8, to configure a KIE Server to use a specific database, you need to set two system properties:
- `org.kie.server.persistence.ds` - Datasource JNDI name
- `org.kie.server.persistence.dialect` - Hibernate dialect

The `persistence.xml` file in the project has a different use case - it's for the project's own entities, not for KIE Server runtime persistence.

**v8 System Properties:**
```properties
org.kie.server.persistence.ds=java:jboss/datasources/ExampleDS
org.kie.server.persistence.dialect=org.hibernate.dialect.PostgreSQLDialect
```

#### v9 Database Integration

BAMOE v9 introduces a modular architecture where each subsystem can be configured independently but typically shares the same datasource. The key difference is that each subsystem creates its own set of tables.

**v9 Subsystems and Database Tables:**

1. **Process Runtime** (managed by `kie-flyway` addon):
   - `CORRELATION_INSTANCES` - Process correlation data
   - `PROCESS_INSTANCES` - Process instance state (binary format)

2. **UserTask Service** (embedded, replaces V8 task management):
   - `USER_TASK_INSTANCES` - User task state, assignments, status
   - `USER_TASK_INSTANCES_COMMENTS` - Task comments
   - `USER_TASK_INSTANCES_ATTACHMENTS` - Task attachments
   - `USER_TASK_INSTANCES_ADMIN` - Admin users/groups
   - `USER_TASK_INSTANCES_EXCLUDED` - Excluded users
   - `USER_TASK_INSTANCES_POTENTIAL` - Potential owners

3. **DataIndex Service** (embedded, provides GraphQL API):
   - `PROCESS_INSTANCES_VIEW` - Queryable process data
   - `USER_TASKS_VIEW` - Queryable task data
   - `JOBS_VIEW` - Job information
   - `PROCESS_DEFINITIONS` - Process metadata

4. **Jobs Service** (embedded, handles timers and deadlines):
   - `JOB_DETAILS` - Job execution details
   - `JOB_EXECUTION_HISTORY` - Job execution history

**v9 Configuration (application.properties):**

```properties
# Core Persistence
kogito.persistence.type=jdbc
kogito.persistence.optimistic.lock=true

# Datasource (Quarkus managed) - shared by all subsystems
quarkus.datasource.db-kind=postgresql
quarkus.datasource.username=kie-user
quarkus.datasource.password=kie-pass
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/kie

# Flyway for schema management (REQUIRED for Kogito JDBC persistence)
kie.flyway.enabled=true

# Hibernate for custom entities
quarkus.hibernate-orm.database.generation=update

# Transaction support
kogito.transactionEnabled=true
kogito.processes.transactionEnabled=true
kogito.usertasks.transactionEnabled=true

# Jobs Service (replaces V8 Async Executor)
kogito.jobs-service.enabled=true

# DataIndex (for querying process/task data)
kogito.data-index.quarkus.enabled=true
```

**Required Dependencies:**
```xml
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-addons-quarkus-persistence-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-addons-flyway</artifactId>
</dependency>
<dependency>
    <groupId>org.jbpm</groupId>
    <artifactId>jbpm-addons-quarkus-usertask-storage-jpa</artifactId>
</dependency>
```

**Fix:**
1. In v8, configure KIE Server using system properties (`org.kie.server.persistence.ds` and `org.kie.server.persistence.dialect`)
2. In v9, configure database integration in `application.properties` with:
   - Single shared datasource for all subsystems
   - Enable `kogito.persistence.type=jdbc` for process instances
   - Enable `kie.flyway.enabled=true` for automatic schema management
   - Enable UserTask, DataIndex, and Jobs services as needed
3. Remove `persistence.xml` unless you have custom JPA entities
4. Add required dependencies for JDBC persistence and Flyway

---

### Issue #17: REST Endpoint Changes

**Source:** Multiple tutorials

**Problem:** v8 REST endpoints different from v9  
**v9:** Uses Quarkus REST conventions

**v8 Endpoints:**
```
POST /rest/server/containers/{containerId}/processes/{processId}/instances
GET  /rest/server/containers/{containerId}/processes/instances/{instanceId}
```

**v9 Endpoints:**
```
POST /{processId}
GET  /{processId}/{instanceId}
POST /{processId}/{instanceId}/{taskId}/complete
```

**Fix:** Update client code to use new endpoint structure

---

### Issue #18: BPMN-DRL Integration - insert() and delete() Have No Effect

**Source:** Tutorial 02 (Mortgage Application) - Discovered during DRL verification

**Problem:** In v9, `insert()`, `retract()`, and `delete()` operations in DRL rules have no effect on BPMN processes due to no shared KIE session  

**Error:**
```
No error thrown, but inserted facts are not visible to BPMN process
```

**v8 Code (Appears to work but doesn't in v9):**
```drl
package com.myspace.mortgage_app;

rule "Validate Down Payment"
    ruleflow-group "validation"
    when
        $app : Application( downpayment == 0 || downpayment >= property.saleprice )
    then
        ValidationErrorDO error = new ValidationErrorDO();
        error.setError("Down payment cannot be 0...");
        insert(error);  // Has NO effect in v9 - not visible to BPMN
        $app.setErrors(error);  // This works - modifies process variable
        update($app);  // This works - notifies BPMN
end
```

**v9 Solution:**
```drl
package com.myspace.mortgage_app;

rule "Validate Down Payment"
    ruleflow-group "validation"
    when
        $app : Application( downpayment == 0 || downpayment >= property.saleprice )
    then
        ValidationErrorDO error = new ValidationErrorDO();
        error.setError("Down payment cannot be 0...");
        // REMOVED: insert(error); - Has no effect in v9
        $app.setErrors(error);  // Modify process variable directly
        update($app);  // Notify BPMN of change
end
```

> "In {PRODUCT_SHORT} v9, shared KIE sessions between BPMN workflows and the rule engine are no longer supported. This means that facts inserted or modified within a DRL rule are not accessible to BPMN processes. Functions such as insert(), retract(), update(), and delete() will no longer have any effect from the BPMN perspective."

**Key Points:**
- `insert(fact)` - Does NOT make fact visible to BPMN
- `delete(fact)` - Does NOT remove fact from BPMN
- `retract(fact)` - Does NOT remove fact from BPMN
- `object.setField(value)` - DOES modify process variable
- `update(object)` - DOES notify BPMN of changes

**Migration Strategy:**
1. Remove all `insert()` calls that expect BPMN to see the facts
2. Remove all `delete()`/`retract()` calls
3. Modify process variables directly: `$app.setErrors(error)`
4. Use `update($object)` to notify BPMN of changes
5. Pass data between BPMN and DRL using Input/Output mappings

**Example - Retract Pattern:**
```drl
// v8 - Retract error (doesn't work in v9)
rule "RetractValidationErr"
    ruleflow-group "error"
    when
        $app : Application(errors != null)
        $vdo : ValidationErrorDO()
    then
        $app.setErrors(null);
        update($app);
        delete($vdo);  //  Has no effect in v9
end

// v9 - Clear error from process variable
rule "RetractValidationErr"
    ruleflow-group "error"
    when
        $app : Application(errors != null)
    then
        $app.setErrors(null);  //  Clear from process variable
        update($app);  // Notify BPMN
        // REMOVED: delete($vdo); - Not needed
end
```

**Fix:** Only modify objects passed from BPMN, don't insert/delete facts

---

---

## Quick Reference Tables

### Error Message → Solution

| Error Message | Issue # | Quick Fix |
|---------------|---------|-----------|
| `Invalid Java identifier` | #1 | Use camelCase for process IDs |
| `package javax.persistence does not exist` | #1 | Replace javax with jakarta |
| `package javax.ws.rs does not exist` | #1 | Replace javax.ws.rs with jakarta.ws.rs |
| `package javax.xml.bind does not exist` | #1 | Replace javax.xml.bind with jakarta.xml.bind |
| `package org.kie.server.remote.rest not found` | #1 | Use Quarkus REST with jakarta.ws.rs |
| `package org.jbpm.services.api not found` | #1 | Use Kogito runtime APIs |
| `package io.swagger.annotations not found` | #1 | Use org.eclipse.microprofile.openapi.annotations |
| `Variable 'context' does not exist` | #2 | Use kcontext instead |
| `Raw type 'List' is not allowed` | #3 | Add type parameter: List<Type> |
| `Invalid schema name` | #6 | Remove dots from process ID |
| `Hibernate validation failed` | #5 | Use DTO or configure JPA |
| `Cannot mix legacy rules` | #7 | Convert all to rule units |
| `null$` in output | #8 | Initialize with defaults |
| `.frm files not found` | #10 | Use form generation |
| `kmodule.xml not found` | #11 | Remove it, use application.properties |
| Facts not visible to BPMN | #18 | Remove insert/delete, modify process variables |

---

## Migration Workflow

### Standalone/Embedded Mode Migration

**v8 Standalone Mode:**

```java
// v8 - Embedded KIE Server
KieServices kieServices = KieServices.Factory.get();
KieContainer kieContainer = kieServices.newKieClasspathContainer();
KieSession kieSession = kieContainer.newKieSession();

// Start process
ProcessInstance processInstance = kieSession.startProcess("myProcess");
```

**v9 Standalone Mode:**

```java
// v9 - Embedded Kogito runtime
@ApplicationScoped
public class EmbeddedProcessService {
    
    @Inject
    Process<MyProcessModel> myProcess;
    
    public String startProcess(MyProcessModel model) {
        ProcessInstance<MyProcessModel> instance =
            myProcess.createInstance(model);
        instance.start();
        return instance.id();
    }
}
```

**Understanding MyProcessModel:**

`MyProcessModel` is a type-safe model class that represents the process variables for your BPMN process. In v9, Kogito automatically generates a model class for each process definition based on the process variables defined in your BPMN file.

**Key Points:**
- The model class name is derived from your process ID (e.g., process ID "myProcess" generates `MyProcessModel`)
- It provides type-safe access to process variables instead of using `Map<String, Object>`
- The model is automatically generated during the build process
- You can also use the generic `Model` interface if you don't need type safety

**Required Import Statements:**

```java
// Core Kogito imports for embedded mode
import org.kie.kogito.Model;                    // Generic model interface
import org.kie.kogito.process.Process;          // Process interface
import org.kie.kogito.process.ProcessInstance;  // Process instance interface

// CDI imports for dependency injection
import jakarta.inject.Inject;                   // For @Inject annotation
import jakarta.inject.Named;                    // For @Named annotation (optional)
import jakarta.enterprise.context.ApplicationScoped;  // For @ApplicationScoped

// For working with generic models
import java.util.HashMap;
import java.util.Map;
```

**Complete Example with Generic Model (Alternative Approach):**

If you prefer not to use the generated type-safe model, you can use the generic `Model` interface:

```java
import org.kie.kogito.Model;
import org.kie.kogito.process.Process;
import org.kie.kogito.process.ProcessInstance;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.enterprise.context.ApplicationScoped;
import java.util.HashMap;
import java.util.Map;

@ApplicationScoped
public class EmbeddedProcessService {
    
    @Named("myProcess")  // Use process ID from BPMN
    @Inject
    Process<? extends Model> myProcess;
    
    public String startProcess(Map<String, Object> variables) {
        // Create model from the process
        Model model = myProcess.createModel();
        
        // Set variables using map
        model.fromMap(variables);
        
        // Create and start instance
        ProcessInstance<?> instance = myProcess.createInstance(model);
        instance.start();
        
        return instance.id();
    }
    
    public Map<String, Object> getProcessVariables(String processId) {
        ProcessInstance<?> instance = myProcess.instances()
            .findById(processId)
            .orElseThrow();
        
        Model result = (Model) instance.variables();
        return result.toMap();
    }
}
```


**Key Differences:**

| Aspect | v8 Embedded | v9 Embedded |
|--------|-------------|-------------|
| **Container** | KieContainer | CDI @Inject |
| **Session** | KieSession | Process<T> |
| **Process Start** | startProcess(id) | createInstance(model).start() |
| **Variables** | Map<String, Object> | Type-safe model |
| **Lifecycle** | Manual management | Automatic CDI |

**Configuration for Embedded:**

```properties
# application.properties - Disable REST endpoints if not needed
quarkus.http.port=0  # No HTTP server
kogito.service.url=  # No external URL

# Or keep minimal REST for monitoring
quarkus.http.port=8080
quarkus.http.root-path=/internal
```

---

### Event Streaming with Kafka

**v8 Event Streaming:**

v8 used custom event listeners with manual Kafka integration.

**v9 Event Streaming:**

```xml
<!-- pom.xml - Add Kafka extension -->
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>kie-addons-quarkus-events-kafka</artifactId>
</dependency>
```

**Configuration:**

```properties
# application.properties
# Kafka connection
kafka.bootstrap.servers=localhost:9092

# Process events
mp.messaging.outgoing.kogito-processinstances-events.connector=smallrye-kafka
mp.messaging.outgoing.kogito-processinstances-events.topic=process-instances
mp.messaging.outgoing.kogito-processinstances-events.value.serializer=org.apache.kafka.common.serialization.StringSerializer

# User task events
mp.messaging.outgoing.kogito-usertaskinstances-events.connector=smallrye-kafka
mp.messaging.outgoing.kogito-usertaskinstances-events.topic=user-tasks
mp.messaging.outgoing.kogito-usertaskinstances-events.value.serializer=org.apache.kafka.common.serialization.StringSerializer

# Variable events
mp.messaging.outgoing.kogito-variables-events.connector=smallrye-kafka
mp.messaging.outgoing.kogito-variables-events.topic=process-variables
mp.messaging.outgoing.kogito-variables-events.value.serializer=org.apache.kafka.common.serialization.StringSerializer
```

**Custom Event Processing:**

```java
@ApplicationScoped
public class ProcessEventConsumer {
    
    @Incoming("process-instances")
    public void onProcessEvent(String event) {
        // Process the event
        System.out.println("Process event: " + event);
    }
    
    @Incoming("user-tasks")
    public void onTaskEvent(String event) {
        // Process task event
        System.out.println("Task event: " + event);
    }
}
```

**Event Types:**

- Process instance created/completed/aborted
- User task created/completed/aborted
- Variable changed
- Node entered/left
- Signal received

---

### Case Management to Flexible Processes

**v8 Case Management:**

```xml
<!-- v8 - Case definition -->
<case id="IT_Orders" name="IT Orders">
  <caseFileModel>
    <caseFileItem name="order" type="com.example.Order"/>
  </caseFileModel>
  <caseMilestone id="milestone1" name="Order Approved"/>
  <caseTask id="task1" name="Review Order"/>
</case>
```

**v9 Flexible Processes:**

```xml
<!-- v9 - Ad-hoc subprocess for flexibility -->
<bpmn2:adHocSubProcess id="flexibleOrdering" name="Flexible Order Process">
  <bpmn2:completionCondition xsi:type="bpmn2:tFormalExpression">
    orderApproved == true
  </bpmn2:completionCondition>
  
  <!-- Tasks can be executed in any order -->
  <bpmn2:userTask id="reviewOrder" name="Review Order"/>
  <bpmn2:userTask id="checkInventory" name="Check Inventory"/>
  <bpmn2:userTask id="approveOrder" name="Approve Order"/>
</bpmn2:adHocSubProcess>
```

**Dynamic Task Addition:**

```java
@ApplicationScoped
public class DynamicTaskService {
    
    @Inject
    Process<OrderModel> orderProcess;
    
    public void addDynamicTask(String processInstanceId, String taskName) {
        ProcessInstance<OrderModel> instance = 
            orderProcess.instances().findById(processInstanceId).orElseThrow();
        
        // Add dynamic task using process instance API
        instance.send(Sig.of("AddTask", taskName));
    }
}
```

**Migration Strategy:**

1. Convert case milestones → BPMN milestones
2. Convert case tasks → ad-hoc subprocess tasks
3. Convert case file items → process variables
4. Convert case roles → user task assignments
5. Use signals for dynamic behavior

---

### Production Environment Considerations

#### Deployment Architecture

**v8 Production:**

```
┌─────────────────────────────────────┐
│     Application Server (EAP)        │
│  ┌──────────────────────────────┐   │
│  │    Business Central           │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │    KIE Server (multiple)     │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│     Shared Database                 │
└─────────────────────────────────────┘
```

**v9 Production:**

```
┌─────────────────────────────────────┐
│     Container Orchestration         │
│         (Kubernetes/OpenShift)      │
│  ┌──────────────────────────────┐   │
│  │  Process Service (Pods)      │   │
│  │  - Auto-scaling              │   │
│  │  - Load balanced             │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │  Data Index Service          │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │  Jobs Service                │   │
│  └──────────────────────────────┘   │
│  ┌──────────────────────────────┐   │
│  │  Management Console          │   │
│  └──────────────────────────────┘   │
└─────────────────────────────────────┘
         │
         ▼
┌─────────────────────────────────────┐
│  Separate Databases per Service     │
│  - Process DB                       │
│  - Data Index DB                    │
│  - Jobs Service DB                  │
└─────────────────────────────────────┘
```

#### Production Configuration

```properties
# application.properties - Production profile
%prod.quarkus.datasource.db-kind=postgresql
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://db-host:5432/bamoe
%prod.quarkus.datasource.username=${DB_USERNAME}
%prod.quarkus.datasource.password=${DB_PASSWORD}

# Connection pooling
%prod.quarkus.datasource.jdbc.min-size=5
%prod.quarkus.datasource.jdbc.max-size=20

# Persistence
%prod.quarkus.hibernate-orm.database.generation=validate
%prod.kogito.persistence.type=jdbc

# Security
%prod.quarkus.oidc.enabled=true
%prod.quarkus.oidc.auth-server-url=${OIDC_SERVER_URL}
%prod.quarkus.oidc.client-id=${OIDC_CLIENT_ID}
%prod.quarkus.oidc.credentials.secret=${OIDC_CLIENT_SECRET}

# Monitoring
%prod.quarkus.log.level=INFO
%prod.quarkus.log.console.json=true

# Health checks
%prod.quarkus.smallrye-health.root-path=/health
%prod.quarkus.smallrye-health.liveness-path=/live
%prod.quarkus.smallrye-health.readiness-path=/ready
```

---

### Operational Considerations

#### Database Cleanup with Data Cleanup Add-on

**Add Dependency:**

```xml
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>kie-addons-quarkus-data-cleanup</artifactId>
</dependency>
```

**Configuration:**

```properties
# Automatic cleanup of completed instances
kogito.data-cleanup.enabled=true
kogito.data-cleanup.cron=0 0 2 * * ?  # Daily at 2 AM
kogito.data-cleanup.retention-days=30  # Keep 30 days
kogito.data-cleanup.batch-size=100
```

**Manual Cleanup:**

```bash
# REST endpoint for manual cleanup
curl -X POST http://localhost:8080/management/cleanup \
  -H "Content-Type: application/json" \
  -d '{
    "olderThan": "2024-01-01T00:00:00Z",
    "status": ["COMPLETED", "ABORTED"]
  }'
```

#### Process Instance Migration (PIM)

**v8 PIM:**

Used KIE Server PIM API for migrating running instances through REST endpoints or standalone PIM tool.

**v9 PIM:**

BAMOE v9 provides the **Process Instance Migration (PIM) Add-on** that integrates with Management Console and exposes REST APIs for migrating in-flight process instances to new process definitions.

**Setup:**

1. Add PIM Add-on dependency to your Business Service `pom.xml`:

```xml
<dependency>
  <groupId>com.ibm.bamoe</groupId>
  <artifactId>bamoe-process-instance-migration-addon</artifactId>
</dependency>
```

2. Use Management Console UI or REST API to create migration plans and execute migrations

**Key Features:**
- Visual process definition mapping in Management Console
- REST API for automation
- Preserves process state, variables, and jobs
- Supports node mapping between source and target processes
- Handles User Tasks, Timers, and subprocess migrations

**Note:** The PIM Add-on provides similar capabilities to v8 PIM but with improved performance and UI integration. There is no CLI tool to install; use the Management Console or REST API directly.
```

---

### Performance Optimization

#### v9 Performance Best Practices

**1. Use Reactive Endpoints:**

```java
@Path("/orders")
@ApplicationScoped
public class OrderResource {
    
    @Inject
    Process<OrderModel> orderProcess;
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Uni<Response> createOrder(OrderModel model) {
        return Uni.createFrom().item(() -> {
            ProcessInstance<OrderModel> instance = 
                orderProcess.createInstance(model);
            instance.start();
            return Response.ok(instance.id()).build();
        });
    }
}
```

**2. Enable Native Compilation:**

```bash
# Build native executable
mvn package -Pnative

```

**3. Configure Connection Pooling:**

```properties
# Optimize database connections
quarkus.datasource.jdbc.min-size=10
quarkus.datasource.jdbc.max-size=50
quarkus.datasource.jdbc.acquisition-timeout=5
quarkus.datasource.jdbc.idle-removal-interval=5M
```

**4. Enable Caching:**

```properties
# Process definition caching
kogito.cache.enabled=true
kogito.cache.process-definitions.size=100
kogito.cache.process-definitions.ttl=3600
```

**5. Optimize Persistence:**

```properties
# Batch inserts
quarkus.hibernate-orm.jdbc.statement-batch-size=50

# Second-level cache
quarkus.hibernate-orm.cache."com.example.Order".expiration.max-idle=600
```

---

### Not Supported Features

#### Features Not Supported in v9

The following features from BAMOE v8 are not available in v9. This section provides the replacement or migration path for each.

**1. Business Central:**
- **Status:** Not available in v9
- **Replacement:**
  - **BAMOE Canvas** - Web-based editor for BPMN and DMN
  - **BAMOE Developer Tools for VS Code** - IDE extension for BPMN, DMN, and Test Scenarios
  - **Maven Repository** - For dependency management
- **Impact:** All authoring, deployment, and monitoring must be done through new tools

**2. KIE Server:**
- **Status:** Replaced
- **Replacement:** Quarkus or Spring Boot runtimes
- **Impact:** Applications must be migrated to microservices architecture
- **Migration:** See [Upgrading client-server projects](../../../ibamoe/upgrade/05-02-upgrading-client-server-projects.adoc)

**3. SmartRouter:**
- **Status:** Not available in v9
- **Replacement:** Kubernetes-native service discovery and orchestration
- **Impact:** Use Kubernetes Services, Ingress, or service mesh (e.g., Istio) for routing
- **Alternative:** Any cloud-compatible load balancing solution

**4. Asset Authoring Limitations:**
- **BAMOE Canvas:** Supports BPMN and DMN only
- **BAMOE Developer Tools:** Supports BPMN, DMN, and Test Scenarios
- **Not Supported:** Guided Rules, Guided Decision Tables, Scorecards, DSL editors
- **Migration:** Convert guided assets to DRL or DMN format

**5. Legacy Form Modeler:**
- **Status:** Not supported
- **Replacement:** Form code generation for User Tasks
- **Impact:** All v8 `.frm` files must be recreated
- **Migration:** Use form generation in Developer Tools or create custom React/TypeScript forms
- **Reference:** See [Form code generation](../../../ibamoe/tools/form-generation.adoc)

**6. Custom Dashboards and Reports:**
- **Status:** Not available in v9
- **Replacement:** Integration with Prometheus and Grafana
- **Impact:** Custom monitoring must be implemented using external tools
- **Alternative:** Use Management Console for basic process monitoring

**7. Database Support:**
- **Supported:** Microsoft SQL Server, PostgreSQL, Oracle
- **Not Supported:** Sybase, EDB, and other databases
- **Impact:** Must migrate to supported database if using unsupported one
- **Reference:** See [Supported environments](../../../ibamoe/release-notes/supported-environments.adoc)

**8. Case Management:**
- **Status:** Full CMMN not available
- **Replacement:** Adhoc (Flexible) Processes using BPMN extensions
- **Supported Features:**
  - Ad-hoc processes and subprocesses
  - Milestones
  - Dynamic tasks and processes
  - Signal-based triggering
- **Not Supported:**
  - Case lifecycle operations (close, reopen, cancel, destroy)
  - Case ID generator
  - Case-specific events and listeners
  - Per-case runtime strategy
- **Migration:** See [Upgrading Case Management projects](../../../ibamoe/upgrade/05-10-upgrading-case-management-project.adoc)

**9. Build from UI:**
- **Status:** Not available in BAMOE Canvas
- **Replacement:** Maven command-line builds
- **Impact:** CI/CD pipelines must use Maven
- **Command:** `mvn clean package`

**10. Predefined WorkItemHandlers:**
- **Not Available:** CamelCXFConnector, CamelFTPConnector, KafkaPublishMessages, WebServiceTask
- **Replacement:** Custom WorkItemHandlers or REST service calls
- **Impact:** Must implement custom handlers for these integrations
- **Reference:** See [Upgrading custom work item handlers](../../../ibamoe/upgrade/05-05-upgrading-custom-work-item-handlers.adoc)

**11. DRL-based Test Scenarios:**
- **Status:** Editing not supported in v9 tooling
- **Execution:** Still supported via Maven
- **Replacement:** Use JUnit tests for new test cases
- **Impact:** Cannot edit existing DRL test scenarios in Canvas or Developer Tools
- **Note:** DMN-based Test Scenarios are fully supported

**12. PMML (Predictive Model Markup Language):**
- **Status:** Not available in v9
- **Replacement:** External ML model integration or DMN
- **Impact:** PMML models must be migrated to alternative format
- **Note:** May be reconsidered in future releases

**13. Pluggable Variable Persistence:**
- **Status:** Not supported
- **Impact:** Cannot customize variable persistence strategy
- **Alternative:** Use standard JPA persistence with custom entities

**14. JavaScript as Scripting Language:**
- **Status:** Not supported in v9
- **Replacement:** Java only
- **Impact:** All JavaScript script tasks must be converted to Java
- **Migration:** Rewrite script tasks using Java syntax

**15. Guided Decision Tables (XLS/XLSX):**
- **Status:** Editor not available, but execution supported
- **Workaround:** Edit in external spreadsheet tool, add dependency:
```xml
<dependency>
  <groupId>org.drools</groupId>
  <artifactId>drools-decisiontables</artifactId>
</dependency>
```

**16. Guided Rules:**
- **Status:** Not supported
- **Migration:** Convert to DRL or DMN
- **Example:**
```drl
// Convert guided rule to DRL
rule "Approval Rule"
when
    $order: Order(amount > 1000)
then
    $order.setApprovalRequired(true);
end
```

**17. Scorecards:**
- **Status:** Not supported
- **Migration:** Convert to DMN decision tables

**18. Complex Event Processing (CEP):**
- **Status:** Not supported
- **Replacement:** Kafka Streams or external CEP engine
- **Alternative:** Use event-driven architecture with Kafka

---
**Reference:** [`ibamoe/upgrade/11-not-supported.adoc`](../../../ibamoe/upgrade/11-not-supported.adoc)

---

# Build
mvn clean install

If you are using Gradle, use the following command:

```bash
gradle clean build
```

# Run in dev mode
mvn quarkus:dev -Pdevelopment

If you are using Gradle, use the following command:

```shell script
gradle clean quarkusDev
```

# Access Dev UI
open http://localhost:8080/q/dev

# Test each process
# Test each decision
# Test each rule
```

### Phase 4: Verification

- [ ] All processes start successfully
- [ ] All tasks can be completed
- [ ] All rules execute correctly
- [ ] All decisions return expected results
- [ ] No errors in logs
- [ ] All tests pass

---

## Summary

### Key Takeaways

1. **Process IDs:** Suggested to use camelCase (no hyphens, no dots )
2. **Packages:** All javax → jakarta
3. **Context:** Use kcontext not context
4. **Types:** Always specify List<Type>
5. **Booleans:** Use isXxx() not getXxx()
6. **Rules:** Convert all to rule units
7. **Entities:** Use DTOs for process variables
8. **Forms:** Use generation or custom React forms
9. **Config:** Use application.properties not XML
10. **Testing:** Use Dev UI not Business Central

### Common Patterns

**Before (v8):**
```java
// javax imports
import javax.persistence.Entity;

// Raw types
private List items;

// Legacy rules
ruleflow-group "validation"

// XML configuration
<kie-deployment-descriptor>
```

**After (v9):**
```java
// jakarta imports
import jakarta.persistence.Entity;

// Parameterized types
private List<Item> items;

// Rule units
unit ValidationRuleUnit;

// Properties configuration
quarkus.datasource.db-kind=h2
```

### Final Checklist

- [ ] All core issues addressed
- [ ] Additional issues from tutorials addressed
- [ ] All javax → jakarta
- [ ] All process IDs valid (no hyphens/dots)
- [ ] All List types parameterized
- [ ] All rules converted to rule units
- [ ] All configuration in application.properties
- [ ] Application builds successfully
- [ ] Application runs in dev mode

---

