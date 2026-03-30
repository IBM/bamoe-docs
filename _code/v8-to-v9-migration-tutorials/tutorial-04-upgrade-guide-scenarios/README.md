
# Tutorial 04: Upgrade Guide Scenarios - BAMOE v8 to v9 Migration

This tutorial demonstrates **11 common migration scenarios** when upgrading from BAMOE v8 to v9, with practical examples and solutions for each issue.

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

---

## Introduction

This tutorial provides a comprehensive guide for migrating BAMOE v8 applications to v9, covering the most common issues encountered during migration. Each scenario includes:

- **Problem description** - What breaks in v9
- **v8 code example** - How it worked before
- **v9 solution** - How to fix it
- **Test files** - Validation examples

### What's Included

- **v8-app/** - Original BAMOE v8 application demonstrating common patterns
- **v9-app/** - Migrated BAMOE v9 application with all fixes applied
- **Test scenarios** - BPMN and Java files demonstrating each migration issue

### Migration Scenarios Covered

1. Process ID naming constraints (hyphens → camelCase)
2. javax → jakarta package migration
3. Context variable (use `kcontext`)
4. Java List type parameters (raw types → parameterized)
5. Boolean getter naming (`getActive()` → `isActive()`)
6. @Entity annotation handling
7. NPE in error handling blocks
8. Legacy rules → Rule units migration
9. Process variable type declarations
10. null$ values in output
11. Package name changes

---

## Prerequisites

### Required Software

- **Java 17 or later** (v8 used Java 11)
- **Maven 3.8.1 or later**
- **BAMOE 9.3.1 or later**
- **Git** (for version control)

### Knowledge Requirements

- Basic understanding of BPMN processes
- Java programming fundamentals
- Maven project structure
- JPA/Hibernate basics (for persistence scenarios)

### Before You Start

1. **Backup your v8 project** - Keep the original for reference
2. **Review the v8 application** - Understand current functionality
3. **Read BAMOE v9 documentation** - Familiarize yourself with new features
4. **Set up development environment** - Ensure all tools are installed

---

## Step-by-Step Migration

### Step 1: Project Structure Migration

#### 1.1 Update POM.xml

**v8 Configuration:**
```xml
<packaging>kjar</packaging>
<kie.version>7.67.2.Final-redhat-00017</kie.version>
<maven.compiler.source>11</maven.compiler.source>
```

**v9 Configuration:**
```xml
<packaging>jar</packaging>
<version.bamoe>9.3.1-ibm-0006</version.bamoe>
<version.quarkus>3.20.3</version.quarkus>
<maven.compiler.release>17</maven.compiler.release>
```

**Action Items:**
- Change packaging from `kjar` to `jar`
- Update Java version from 11 to 17
- Replace KIE dependencies with BAMOE BOM
- Add Quarkus dependencies

#### 1.2 Add Quarkus Dependencies

Add to [`v9-app/pom.xml`](v9-app/pom.xml):

```xml
<dependency>
  <groupId>org.jbpm</groupId>
  <artifactId>jbpm-with-drools-quarkus</artifactId>
</dependency>
<dependency>
  <groupId>org.kie</groupId>
  <artifactId>kie-addons-quarkus-persistence-jdbc</artifactId>
</dependency>
```

### Step 2: Scenario-by-Scenario Migration

#### Scenario 1: Process ID Naming

**Issue:** Process IDs with hyphens/ dots work but are not recommended in v9.

**v8 Pattern:**
```xml
<bpmn2:process id="upgrade-demo-process" name="Upgrade Demo">
```

**v9 Solution:**
```xml
<bpmn2:process id="upgradeDemoProcess" name="Upgrade Demo">
```

**Migration Steps:**
1. Identify all process IDs with hyphens/ dots
2. Convert to camelCase naming
3. Update all references in code and configuration
4. Test process instantiation

---

#### Scenario 2: javax → jakarta Migration

**Issue:** v9 uses Jakarta EE, requiring package name changes.

**v8 Code:**
```java
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
```

**v9 Solution:**
```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
```


**Migration Steps:**
1. Find all `javax.*` imports
2. Replace with `jakarta.*` equivalents
3. Update dependency versions in POM
4. Rebuild and test

**Common Packages to Update:**
- `javax.persistence.*` → `jakarta.persistence.*`
- `javax.validation.*` → `jakarta.validation.*`
- `javax.ws.rs.*` → `jakarta.ws.rs.*`
- `javax.inject.*` → `jakarta.inject.*`

---

#### Scenario 3: Context Variable placement

**Issue:** `context` variable no longer available in v9 script tasks.

**v8 Pattern:**
```javascript
// In script task
var processId = kcontext.getProcessInstance().getProcessId();
```

**v9 Solution:**
```javascript
// Use kcontext 
var processId = kcontext.getProcessInstance().getProcessId();
```


**Migration Steps:**
1. Use `kcontext.` in all BPMN files
3. Test all script tasks
4. Verify process execution

---

#### Scenario 4: Java List Type Parameters

**Issue:** Raw types cause Jandex indexing errors in v9.

**v8 Code:**
```java
private List items;  // Raw type
private Map properties;  // Raw type

public List getItems() {
    return items;
}
```

**v9 Solution:**
```java
private List<String> items;  // Parameterized
private Map<String, String> properties;  // Parameterized

public List<String> getItems() {
    return items;
}
```
**Migration Steps:**
1. Identify all raw collection types
2. Add appropriate type parameters
3. Update method signatures
4. Use diamond operator `<>` for instantiation

---

#### Scenario 5: Boolean Getter Naming

**Issue:** v9 enforces JavaBeans naming conventions for boolean primitives.

**v8 Code:**
```java
private boolean active;

public boolean getActive() {  // Works in v8
    return active;
}
```

**v9 Solution:**
```java
private boolean active;

public boolean isActive() {  // Required in v9
    return active;
}
```

**Migration Steps:**
1. Find all boolean getters using `get` prefix
2. Rename to `is` prefix for primitives
3. Keep `get` prefix for Boolean wrapper objects
4. Update all references in BPMN expressions

---

#### Scenario 6: @Entity Annotation Handling

**Issue:** @Entity on process variables requires proper Hibernate configuration.

**v8 Behavior:**
```java
@Entity  // Works without configuration
public class UserEntity {
    // Used as process variable
}
```

**v9 Solution Option 1 - Configure Hibernate:**
```properties
# In application.properties
quarkus.hibernate-orm.database.generation=none
quarkus.hibernate-orm.validate-in-dev-mode=false
```

**v9 Solution Option 2 - Remove @Entity:**
```java
// Remove @Entity if only used as process variable
public class UserEntity implements Serializable {
    // No JPA annotations needed
}
```

**Migration Steps:**
1. Identify entities used as process variables
2. Choose configuration or removal approach
3. Update application.properties if keeping @Entity
4. Test process variable serialization

---

#### Scenario 7: NPE in Error Handling

**Issue:** Null pointer exceptions in catch blocks require explicit null checks.

**v8 Pattern:**
```java
try {
    // Process logic
} catch (Exception e) {
    String message = e.getMessage();  // May be null
}
```

**v9 Solution:**
```java
try {
    // Process logic
} catch (Exception e) {
    String message = e.getMessage() != null ? 
        e.getMessage() : "Unknown error";
}
```

**Migration Steps:**
1. Review all error handling blocks
2. Recommended to add null checks for exception properties


---

#### Scenario 8: Legacy Rules → Rule Units

**Issue:** While v9 still supports `ruleflow-group` attributes for backward compatibility, it's recommended to migrate to Rule Units for better organization, type safety, and modern features. **Important:** You cannot mix `ruleflow-group` and Rule Units in the same project - choose one approach consistently.

**v8 Pattern (with ruleflow-group):**
```drl
package com.example.rules;

rule "Validate Order"
    ruleflow-group "order-validation"
    salience 10
when
    $order : Order(amount > 1000)
then
    $order.setStatus("APPROVED");
end

rule "Check Credit"
    ruleflow-group "order-validation"
    salience 5
when
    $order : Order(approved == true)
    $customer : Customer(creditScore < 600)
then
    $order.setApproved(false);
end
```

**v9 Solution (Rule Unit):**

First, create the Rule Unit class ([`OrderValidationUnit.java`](v9-app/src/main/java/com/example/upgrade/OrderValidationUnit.java)):
```java
package com.example.upgrade;

import org.drools.ruleunits.api.DataSource;
import org.drools.ruleunits.api.DataStore;
import org.drools.ruleunits.api.RuleUnitData;
import org.eclipse.microprofile.openapi.annotations.media.Schema;

public class OrderValidationUnit implements RuleUnitData {
    
    @Schema(implementation = Order[].class)
    private DataStore<Order> orders = DataSource.createStore();
    
    public OrderValidationUnit() {
    }
    
    public DataStore<Order> getOrders() {
        return orders;
    }
    
    public void setOrders(DataStore<Order> orders) {
        this.orders = orders;
    }
}
```

Then, convert the DRL to use Rule Unit syntax ([`OrderValidationRules.drl`](v9-app/src/main/resources/OrderValidationRules.drl)):
```drl
package com.example.upgrade;

unit OrderValidationUnit;

import com.example.upgrade.Order;

rule "Validate High Value Order"
    salience 10
when
    $order : /orders[amount > 1000, status == null]
then
    modify($order) {
        setStatus("APPROVED"),
        setValidated(true)
    };
end

rule "Reject Low Credit Order"
    salience 5
when
    $order : /orders[creditScore < 600, status == null]
then
    modify($order) {
        setStatus("REJECTED"),
        setValidated(true),
        setRejectionReason("Poor credit score")
    };
end
```

**BPMN Integration - Business Rule Task:**

In v8, the Business Rule Task references the ruleflow-group:
```xml
<bpmn2:businessRuleTask id="validateTask"
    drools:ruleFlowGroup="order-validation"
    name="Validate Order">
```

In v9, reference the Rule Unit with `unit:` prefix:
```xml
<bpmn2:businessRuleTask id="validateTask"
    drools:ruleFlowGroup="unit:com.example.upgrade.OrderValidationUnit"
    name="Validate Order">
  <bpmn2:ioSpecification>
    <bpmn2:dataInput id="ordersInputX"
        drools:dtype="com.example.upgrade.Order"
        name="orders"/>
  </bpmn2:ioSpecification>
  <bpmn2:dataInputAssociation>
    <bpmn2:sourceRef>order</bpmn2:sourceRef>
    <bpmn2:targetRef>ordersInputX</bpmn2:targetRef>
  </bpmn2:dataInputAssociation>
</bpmn2:businessRuleTask>
```

**Key Differences:**

| v8 (ruleflow-group) | v9 (Rule Unit - Recommended) |
|---------------------|------------------------------|
| `ruleflow-group "name"` | `unit UnitName` |
| Facts inserted globally | Facts in DataStore/DataSource |
| `$fact : FactType()` | `$fact : /dataStore[conditions]` |
| No type safety | Type-safe with Java class |
| Manual fact management | Automatic lifecycle management |

**Note:** v9 still supports `ruleflow-group`, but Rule Units are the recommended approach for new development.

**Files:**
- v8 DRL: [`v8-app/src/main/resources/OrderValidationRules.drl`](v8-app/src/main/resources/OrderValidationRules.drl)
- v9 Rule Unit: [`v9-app/src/main/java/com/example/upgrade/OrderValidationUnit.java`](v9-app/src/main/java/com/example/upgrade/OrderValidationUnit.java)
- v9 DRL: [`v9-app/src/main/resources/OrderValidationRules.drl`](v9-app/src/main/resources/OrderValidationRules.drl)
- v9 BPMN: [`v9-app/src/main/resources/orderValidationProcess.bpmn`](v9-app/src/main/resources/orderValidationProcess.bpmn)
- Order Model: [`v9-app/src/main/java/com/example/upgrade/Order.java`](v9-app/src/main/java/com/example/upgrade/Order.java)
- Reference: [BAMOE Example](../../../bamoe-examples/process-business-rules-quarkus)

**Migration from v8 ruleflow-group to v9 Rule Units - Step by Step:**

Migrating from v8 to v9 requires three main changes. Here's a simple breakdown:

**Step 1: Create a Rule Unit Java Class (NEW in v9)**

In v8, you didn't need a Java class - rules were grouped by name only. In v9, you must create a Java class:

```java
// NEW FILE: OrderValidationUnit.java
public class OrderValidationUnit implements RuleUnitData {
    private DataStore<Order> orders = DataSource.createStore();
    
    // Getters and setters
}
```

**What to do:**
- Create one Java class per `ruleflow-group` from v8
- Implement `RuleUnitData` interface
- Add `DataStore<YourType>` fields for each fact type you use in rules
- Use `DataStore` for objects that rules can modify
- Use `SingletonStore` for single objects (not collections)

**Step 2: Update Your DRL Rules File**

In v8, rules used `ruleflow-group`. In v9, they use `unit` declaration:

```drl
// v8 - OLD WAY
rule "Validate Order"
    ruleflow-group "order-validation"  // Remove this
when
    $order : Order(amount > 1000)      // Old pattern syntax
then
    $order.setStatus("APPROVED");      // Direct setter
end

// v9 - NEW WAY
unit OrderValidationUnit;              // Add this at top of file

rule "Validate Order"
    // No ruleflow-group needed
when
    $order : /orders[amount > 1000]    // New OOPath syntax
then
    modify($order) {                   // Use modify block
        setStatus("APPROVED")
    };
end
```

**What to do:**
- Add `unit YourUnitName;` at the top of the DRL file
- Remove all `ruleflow-group` attributes from rules
- Change `$var : Type(condition)` to `$var : /dataStoreName[condition]`
- Replace direct setters with `modify($var) { setter() };`

**Step 3: Update BPMN Business Rule Task**

In v8, the BPMN referenced the group name. In v9, it references the Java class:

```xml
<!-- v8 - OLD WAY -->
<bpmn2:businessRuleTask
    drools:ruleFlowGroup="order-validation"  <!-- Just the group name -->
    name="Validate Order">
</bpmn2:businessRuleTask>

<!-- v9 - NEW WAY -->
<bpmn2:businessRuleTask
    drools:ruleFlowGroup="unit:com.example.OrderValidationUnit"  <!-- Full class name with unit: prefix -->
    name="Validate Order">
  <bpmn2:ioSpecification>
    <!-- Add input/output mappings -->
    <bpmn2:dataInput id="ordersInputX" name="orders"/>
    <bpmn2:dataOutput id="ordersOutputX" name="orders"/>
  </bpmn2:ioSpecification>
  <bpmn2:dataInputAssociation>
    <bpmn2:sourceRef>order</bpmn2:sourceRef>      <!-- Process variable -->
    <bpmn2:targetRef>ordersInputX</bpmn2:targetRef>  <!-- Rule Unit field -->
  </bpmn2:dataInputAssociation>
  <bpmn2:dataOutputAssociation>
    <bpmn2:sourceRef>ordersOutputX</bpmn2:sourceRef>
    <bpmn2:targetRef>order</bpmn2:targetRef>
  </bpmn2:dataOutputAssociation>
</bpmn2:businessRuleTask>
```

**What to do:**
- Change `drools:ruleFlowGroup="group-name"` to `drools:ruleFlowGroup="unit:full.package.ClassName"`
- Add `<bpmn2:ioSpecification>` with input and output definitions
- Map process variables to Rule Unit DataStore fields using `dataInputAssociation` and `dataOutputAssociation`

**Quick Migration Checklist:**
- [ ] Create Rule Unit Java class for each v8 ruleflow-group
- [ ] Add `unit ClassName;` to top of DRL file
- [ ] Remove `ruleflow-group` from all rules
- [ ] Change rule patterns to OOPath syntax (`/dataStore[...]`)
- [ ] Use `modify()` blocks instead of direct setters
- [ ] Update BPMN with `unit:` prefix and full class name
- [ ] Add input/output mappings in BPMN
- [ ] Test each migrated rule group


**Important Notes:**
- **v9 supports both `ruleflow-group` and Rule Units**, but you **cannot mix them in the same project**
- Rule Units are the recommended approach for new development in v9
- Each `ruleflow-group` typically becomes one Rule Unit during migration
- Use `DataStore` for mutable facts, `DataSource` for read-only facts
- OOPath syntax (`/dataStore[condition]`) replaces traditional patterns in Rule Units
- The `unit:` prefix in BPMN is required to reference Rule Units
- Use `@Schema` annotation for proper OpenAPI spec generation

#### Scenario 9: Process Variable Types

**Issue:** v9 recommends explicit type declarations for process variables.

**v8 Pattern:**
```xml
<bpmn2:property id="employee" name="employee"/>
```

**v9 Solution:**
```xml
<bpmn2:property id="employee" name="employee" 
                itemSubjectRef="_employeeItem"/>
<bpmn2:itemDefinition id="_employeeItem" 
                      structureRef="java.lang.String"/>
```

**Migration Steps:**
1. Identify untyped process variables
2. Add itemDefinition elements
3. Link properties to item definitions
4. Test variable serialization

---


### Step 3: Configuration Migration

#### 3.1 Create application.properties

Create [`v9-app/src/main/resources/application.properties`](v9-app/src/main/resources/application.properties):

```properties
# Persistence
kogito.persistence.type=jdbc
%dev.kie.flyway.enabled=true
%dev.quarkus.datasource.db-kind=h2
%dev.quarkus.datasource.jdbc.url=jdbc:h2:mem:default;MODE=PostgreSQL

# Hibernate Configuration
%dev.quarkus.hibernate-orm.database.generation=none
%dev.quarkus.hibernate-orm.validate-in-dev-mode=false

# Security (disabled in dev)
%dev.kogito.auth.enabled=false
%dev.quarkus.oidc.enabled=false

# Dev UI
%dev.quarkus.swagger-ui.always-include=true
%dev.bamoe.devui.users.jdoe.groups=jdoe,PM,HR
```

#### 3.2 Remove v8 Configuration Files

Delete these v8-specific files:
- `META-INF/kie-deployment-descriptor.xml`
- `META-INF/kmodule.xml` (unless using legacy rules)
- `META-INF/persistence.xml` (replaced by application.properties)

---

### Step 4: Build and Verify

#### 4.1 Build the v9 Application

```bash
cd v9-app
mvn clean package
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45.123 s
```

#### 4.2 Run in Development Mode

```bash
mvn clean quarkus:dev -Pdevelopment
```

If you are using Gradle, use the following command:

```shell script
gradle clean quarkusDev
```

**Verify:**
- Application starts without errors
- Dev UI accessible at http://localhost:8080/q/dev-ui
- Swagger UI at http://localhost:8080/q/swagger-ui

---

## Testing

### Test Strategy

The v9-app includes a comprehensive BPMN process demonstrating all migration scenarios:

**How to Run:**
```bash
cd v9-app
mvn clean quarkus:dev -Pdevelopment
```

If you are using Gradle, use the following command:

```shell script
gradle clean quarkusDev
```

Then start the process via Dev UI or API:
```bash
curl -X POST http://localhost:8080/upgradeDemoProcess \
  -H "Content-Type: application/json" \
  -d '{
    "employeeName": "John Doe",
    "department": "Engineering"
  }'
```

**Expected Output:**
{"id":"c5261f02-03ad-4c95-9b0d-1cec46c8972c","processInfo":"Processing for: John Doe in Engineering","employeeName":"John Doe","department":"Engineering","salary":75000,"isActive":true,"status":"completed"}


### Running Tests

#### 1. Unit Tests

```bash
cd v9-app
mvn test
```

#### 2. Integration Tests

```bash
mvn verify
```

#### 3. Manual Testing via Dev UI

1. Start application: `mvn quarkus:dev -Pdevelopment`
2. Navigate to http://localhost:8080/q/dev-ui
3. Test process instances:
   - Create new instances
   - Complete tasks
   - Verify data persistence

#### 4. API Testing

**Start Process Instance:**
```bash
curl -X POST http://localhost:8080/upgradeDemoProcess \
  -H "Content-Type: application/json" \
  -d '{
    "employee": "John Doe",
    "reason": "Annual Review"
  }'
```

#### 5. Rule Unit Testing with orderValidationProcess

The `orderValidationProcess` demonstrates the Rule Unit migration (Scenario 8). Test the rule unit with different order scenarios:

**Test 1: High Value Order (Should be APPROVED)**
```bash
curl -X POST http://localhost:8080/orderValidationProcess \
  -H "Content-Type: application/json" \
  -d '{
    "order": {
      "orderId": "ORD-001",
      "amount": 1500.00,
      "creditScore": 700
    }
  }'
```

**Expected Output:**
```json
{
  "id": "...",
  "order": {
    "orderId": "ORD-001",
    "amount": 1500.0,
    "creditScore": 700,
    "status": "APPROVED",
    "validated": true,
    "rejectionReason": null
  }
}
```

**Test 2: Low Credit Score Order (Should be REJECTED)**
```bash
curl -X POST http://localhost:8080/orderValidationProcess \
  -H "Content-Type: application/json" \
  -d '{
    "order": {
      "orderId": "ORD-002",
      "amount": 500.00,
      "creditScore": 550
    }
  }'
```

**Expected Output:**
```json
{
  "id": "...",
  "order": {
    "orderId": "ORD-002",
    "amount": 500.0,
    "creditScore": 550,
    "status": "REJECTED",
    "validated": true,
    "rejectionReason": "Poor credit score"
  }
}
```

**Test 3: Standard Order (Should be APPROVED)**
```bash
curl -X POST http://localhost:8080/orderValidationProcess \
  -H "Content-Type: application/json" \
  -d '{
    "order": {
      "orderId": "ORD-003",
      "amount": 800.00,
      "creditScore": 650
    }
  }'
```

**Expected Output:**
```json
{
  "id": "...",
  "order": {
    "orderId": "ORD-003",
    "amount": 800.0,
    "creditScore": 650,
    "status": "APPROVED",
    "validated": true,
    "rejectionReason": null
  }
}
```

**Test 4: Edge Case - Exactly 1000 Amount**
```bash
curl -X POST http://localhost:8080/orderValidationProcess \
  -H "Content-Type: application/json" \
  -d '{
    "order": {
      "orderId": "ORD-004",
      "amount": 1000.00,
      "creditScore": 700
    }
  }'
```

**Expected Output:**
```json
{
  "id": "...",
  "order": {
    "orderId": "ORD-004",
    "amount": 1000.0,
    "creditScore": 700,
    "status": "APPROVED",
    "validated": true,
    "rejectionReason": null
  }
}
```

**Test 5: Edge Case - Exactly 600 Credit Score**
```bash
curl -X POST http://localhost:8080/orderValidationProcess \
  -H "Content-Type: application/json" \
  -d '{
    "order": {
      "orderId": "ORD-005",
      "amount": 500.00,
      "creditScore": 600
    }
  }'
```

**Expected Output:**
```json
{
  "id": "...",
  "order": {
    "orderId": "ORD-005",
    "amount": 500.0,
    "creditScore": 600,
    "status": "APPROVED",
    "validated": true,
    "rejectionReason": null
  }
}
```

**Rule Unit Validation Points:**
- Rule Unit syntax: `unit OrderValidationUnit` in DRL
- OOPath pattern: `/orders[...]` instead of `Order(...)`
- Business Rule Task: `drools:ruleFlowGroup="unit:com.example.upgrade.OrderValidationUnit"`
- DataStore usage: `DataStore<Order>` in OrderValidationUnit class
- Modify syntax: Using `modify($order) {...}` for fact updates

---

## Common Issues

### Issue 1: Build Fails with Jandex Error

**Symptom:**
```
[ERROR] Jandex indexing failed
[ERROR] Raw type 'List' is not allowed
```

**Solution:**
Add type parameters to all collections:
```java
// Before
private List items;

// After
private List<String> items;
```

**Reference:** [Scenario 4](#scenario-4-java-list-type-parameters)

---

### Issue 2: Process Fails to Start

**Symptom:**
```
Process instance could not be created: context is not defined
```

**Solution:**
Replace `context` with `kcontext` in all script tasks.

**Reference:** [Scenario 3](#scenario-3-context-variable-replacement)

---

### Issue 3: Hibernate Validation Error

**Symptom:**
```
[ERROR] Schema validation failed
[ERROR] Table 'users' not found
```

**Solution:**
Configure Hibernate in application.properties:
```properties
%dev.quarkus.hibernate-orm.database.generation=none
%dev.quarkus.hibernate-orm.validate-in-dev-mode=false
```


---

### Issue 4: Boolean Property Not Found

**Symptom:**
```
Property 'active' not found on UserEntity
```

**Solution:**
Rename boolean getter from `getActive()` to `isActive()`.


---

### Issue 5: Package Not Found

**Symptom:**
```
[ERROR] package javax.persistence does not exist
```

**Solution:**
Update imports from `javax.*` to `jakarta.*`.

---

## Summary

### Migration Checklist

1. **Dependencies**: Update POM.xml to Quarkus v9 dependencies
2. **Java Version**: Change to Java 17
3. **Imports**: Replace javax with jakarta packages
4. **Context**: Replace all `context` references with `kcontext`
5. **Collections**: Add type parameters to all List/Map/Set declarations
6. **Booleans**: Rename getters from `getX()` to `isX()` for boolean properties
7. **Entities**: Configure @Entity annotations properly
8. **Variables**: Initialize all variables in scripts
9. **Rules**: Migrate to Rule Units
10. **Configuration**: Set up application.properties for Quarkus

### Key Takeaways

1. **Java 17 Required** - v9 requires Java 17 minimum
2. **Quarkus-Based** - v9 uses Quarkus instead of traditional Java EE
3. **Jakarta EE** - All javax packages moved to jakarta
4. **Type Safety** - Raw types no longer allowed
5. **JavaBeans Compliance** - Boolean getters must use `is` prefix
6. **Explicit Configuration** - More configuration in application.properties



