# Tutorial 9: Complex Data Models with JPA Entities and Hibernate Validation

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

---

## Introduction

This tutorial demonstrates migrating complex data models with JPA entities and Hibernate validation from BAMOE v8.0.x to BAMOE v9.3.x.

### What You'll Learn

- Migrate `javax.persistence` to `jakarta.persistence`
- Migrate `javax.validation` to `jakarta.validation`
- Configure Hibernate ORM in Quarkus
- Handle JPA entity relationships in v9

### Migration Overview

The primary change is the **namespace migration** from `javax.*` to `jakarta.*`. All JPA and validation annotations must be updated, but business logic remains unchanged.

---

## Prerequisites

### Required Software

1. **Java 17 or later**
   ```bash
   java -version  # Must show 17+
   ```

2. **Maven 3.8.1 or later**
   ```bash
   mvn -version
   ```

3. **IDE** (VS Code recommended with BAMOE developer tools extension)

### Required Knowledge

- Basic understanding of JPA and Hibernate
- Familiarity with entity relationships
- Basic Maven operations

---

## Step-by-Step Migration

### Step 1: Understand the Key Changes

| Aspect | v8 (RHPAM/jBPM) | v9 (BAMOE/Kogito) |
|--------|-----------------|-------------------|
| **JPA Namespace** | `javax.persistence.*` | `jakarta.persistence.*` |
| **Validation Namespace** | `javax.validation.*` | `jakarta.validation.*` |
| **Configuration** | `persistence.xml` | `application.properties` |
| **Hibernate Version** | 5.x | 6.x |

### Step 2: Compare Entity Class Changes

#### v8 Customer Entity (OLD)

```java
package com.example.datamodels;

// v8 imports - MUST CHANGE
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.validation.constraints.Email;
import javax.validation.constraints.Min;

@Entity
@Table(name = "customers")
public class Customer implements java.io.Serializable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotNull(message = "Customer name is required")
    @Size(min = 2, max = 100)
    private String name;
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Min(value = 18)
    private Integer age;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();
    
    // Getters, setters, constructors...
}
```

#### v9 Customer Entity (NEW)

```java
package com.example.datamodels;

// v9 imports - UPDATED to jakarta
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;

@Entity
@Table(name = "customers")
public class Customer implements java.io.Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    
    @NotNull(message = "Customer name is required")
    @Size(min = 2, max = 100)
    private String name;
    
    @Email(message = "Email must be valid")
    private String email;
    
    @Min(value = 18)
    private Integer age;
    
    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL)
    private List<Order> orders = new ArrayList<>();
    
    // Getters, setters, constructors...
}
```

**Key Changes:**
- Replace `javax.persistence` → `jakarta.persistence`
- Replace `javax.validation` → `jakarta.validation`
- Add `serialVersionUID` for process variable serialization
- Business logic remains **identical**

See complete implementations:
- [`Customer.java`](v9-app/src/main/java/com/example/datamodels/Customer.java)
- [`Order.java`](v9-app/src/main/java/com/example/datamodels/Order.java)
- [`Product.java`](v9-app/src/main/java/com/example/datamodels/Product.java)

### Step 3: Update pom.xml Dependencies

Add required Quarkus extensions to [`pom.xml`](v9-app/pom.xml):

```xml
<dependencies>
    <!-- Agroal (Connection Pool) -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-agroal</artifactId>
    </dependency>
    
    <!-- H2 Database (for development) -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-jdbc-h2</artifactId>
    </dependency>
</dependencies>
```

### Step 4: Configure application.properties

Replace `persistence.xml` with [`application.properties`](v9-app/src/main/resources/application.properties):

#### v8 Configuration (persistence.xml)

```xml
<persistence-unit name="com.example.datamodels">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <jta-data-source>java:jboss/datasources/ExampleDS</jta-data-source>
    <class>com.example.datamodels.Customer</class>
    <class>com.example.datamodels.Order</class>
    <class>com.example.datamodels.Product</class>
</persistence-unit>
```

#### v9 Configuration (application.properties)

```properties
# Hibernate ORM - Entity Package Configuration
quarkus.hibernate-orm.packages=com.example.datamodels

# Development Mode - H2 Database
%dev.quarkus.datasource.db-kind=h2
%dev.quarkus.datasource.username=sa
%dev.quarkus.datasource.password=
%dev.quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb

# Hibernate ORM Configuration
%dev.quarkus.hibernate-orm.database.generation=drop-and-create
%dev.quarkus.hibernate-orm.log.sql=true
```

### Step 5: Create BPMN Process Using Customer Entity

The project includes [`customerOrder.bpmn`](v9-app/src/main/resources/customerOrder.bpmn) that demonstrates using the migrated Customer entity in a BPMN process:

**Process Overview:**
- **Process ID**: `customerOrder`
- **Process Variables**:
  - `customer` (Customer entity with jakarta.* annotations)
  - `orderNumber` (String)
  - `isValid` (Boolean)

**Process Flow:**
1. Start → Validate Customer (Script Task)
2. Is Valid? (Gateway)
3. Valid → Process Order → End
4. Invalid → Reject Order → End

**Key Points:**
- Demonstrates Customer entity usage as process variable
- Shows validation logic in script tasks
- Proves migrated entities work in BPMN processes

### Step 6: Build and Run

```bash
cd v9-app
mvn clean install
mvn quarkus:dev
```

If you are using Gradle, use the following commands:

```bash
gradle clean build
gradle clean quarkusDev
```

**Expected Output:**
```
INFO  [io.quarkus] complex-data-models-v9 1.0.0-SNAPSHOT on JVM started
Hibernate: create table customers (...)
Hibernate: create table orders (...)
Hibernate: create table products (...)
```

---

## Testing

### Test 1: Build Verification

Verify the project builds successfully:

```bash
mvn clean install
```
If you are using Gradle, use the following command:

```bash
gradle clean build
```

**Expected Result:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45-60 seconds
```

### Test 3: Application Startup

Start the application and verify entities are loaded:

```bash
mvn quarkus:dev
```

**Verify in logs:**
- Hibernate schema generation for `customers`, `orders`, `products` tables
- No import errors for `jakarta.persistence` or `jakarta.validation`
- Application starts on port 8080

### Test 4: Test BPMN Process via REST API

Start the application and test the customer order process:

```bash
# Start the application
mvn quarkus:dev

# Test with valid customer
curl -X POST http://localhost:8080/customerOrder \
  -H "Content-Type: application/json" \
  -d '{
    "customer": {
      "name": "John Doe",
      "email": "john.doe@example.com",
      "age": 25
    },
    "orderNumber": "ORD-001"
  }'
```

**Expected Response:**
```json
{
  "id": "02855678-0a28-48a3-b818-08475ffd188e",
  "orderNumber": "ORD-001",
  "isValid": true,
  "customer": {
    "id": null,
    "name": "John Doe",
    "email": "john.doe@example.com",
    "age": 25,
    "phone": null,
    "address": null,
    "creditScore": null,
    "active": null,
    "orders": []
  }
}
```

**Expected Logs:**
```
2026-02-19 11:29:38,082 INFO  [org.kie.kog.qua.pro.dev.DevModeWorkflowLogger] (executor-thread-1) Triggered node 'Process Order' for process 'customerOrder' (02855678-0a28-48a3-b818-08475ffd188e)
Processing order ORD-001 for customer: John Doe
Customer email: john.doe@example.com
Customer age: 25
2026-02-19 11:29:38,086 INFO  [org.kie.kog.qua.pro.dev.DevModeWorkflowLogger] (executor-thread-1) Triggered node 'End (Success)' for process 'customerOrder' (02855678-0a28-48a3-b818-08475ffd188e)
2026-02-19 11:29:38,089 INFO  [org.kie.kog.qua.pro.dev.DevModeWorkflowLogger] (executor-thread-1) Workflow 'customerOrder' (02855678-0a28-48a3-b818-08475ffd188e) completed
```

```bash
# Test with invalid customer (age < 18)
curl -X POST http://localhost:8080/customerOrder \
  -H "Content-Type: application/json" \
  -d '{
    "customer": {
      "name": "Jane Smith",
      "email": "jane@example.com",
      "age": 16
    },
    "orderNumber": "ORD-002"
  }'
```

**Expected Response (Validation Error at REST API Level):**
```json
{
  "exception": null,
  "propertyViolations": [],
  "classViolations": [],
  "parameterViolations": [
    {
      "constraintType": "PARAMETER",
      "path": "createResource_customerOrder.arg3.customer.age",
      "message": "Customer must be at least 18 years old",
      "value": "16"
    }
  ],
  "returnValueViolations": []
}
```

**To Test Process-Level Validation:**

If you want to see the process validation logic execute (including the "rejected" path), you need to test with data that passes Jakarta validation but fails business rules. For example:

Temporarily remove the `@Min(18)` annotation from the Customer entity to allow invalid ages to reach the process validation logic.

### Test 5: Access Swagger UI

1. Start the application: `mvn quarkus:dev`
2. Navigate to: http://localhost:8080/q/swagger-ui
3. Find the `POST /customerOrder` endpoint
4. Test with different customer data:
   - Valid: name="John Doe", email="john@example.com", age=25
   - Invalid age: age=16
   - Invalid email: email="invalid-email"
5. Verify validation logic works correctly

The entities include validation constraints that can be tested:

**Customer Validation Rules:**
- Name: Required, 2-100 characters
- Email: Required, valid email format
- Age: Minimum 18 years

**Order Validation Rules:**
- Order number: Required
- Order date: Required
- Total amount: Required, positive value

**Product Validation Rules:**
- Product name: Required
- Price: Required, positive value
- Stock quantity: Non-negative
---

## Common Issues

### Issue 1: Import Errors - javax.persistence Not Found

**Symptom:**
```
[ERROR] cannot find symbol
[ERROR] symbol: class Entity
[ERROR] location: package javax.persistence
```

**Solution:**
Replace all `javax` imports with `jakarta`

### Issue 2: Database Connection Issues

**Symptom:**
```
Unable to create requested service [org.hibernate.engine.jdbc.env.spi.JdbcEnvironment]
```

**Solution:**
Ensure you have the correct JDBC driver and Agroal connection pool in [`pom.xml`](v9-app/pom.xml):
```xml
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-agroal</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jdbc-h2</artifactId>
</dependency>
```

### Issue 3: Entity Not Discovered

**Symptom:**
```
org.hibernate.MappingException: Unknown entity: com.example.datamodels.Customer
```

**Solution:**
Add to [`application.properties`](v9-app/src/main/resources/application.properties):
```properties
quarkus.hibernate-orm.packages=com.example.datamodels
```

### Issue 4: Serialization Issues

**Symptom:**
```
java.io.NotSerializableException: com.example.datamodels.Customer
```

**Solution:**
Ensure all entities implement `Serializable`:
```java
public class Customer implements java.io.Serializable {
    private static final long serialVersionUID = 1L;
    // ...
}
```

### Issue 5: Lazy Loading Exception

**Symptom:**
```
org.hibernate.LazyInitializationException: could not initialize proxy - no Session
```

**Solution:**
Use `@Transactional` annotation or change fetch type:
```java
import jakarta.transaction.Transactional;

@Transactional
public void processCustomerOrders(Long customerId) {
    // Access lazy-loaded relationships here
}
```

---

## Summary

### Migration Checklist

- Replace `javax.persistence` with `jakarta.persistence`
- Replace `javax.validation` with `jakarta.validation`
- Add Quarkus Agroal (connection pool) dependency
- Add JDBC driver dependency (H2, PostgreSQL, etc.)
- Configure [`application.properties`](v9-app/src/main/resources/application.properties)
- Ensure entities implement `Serializable`
- Test build and startup
- Verify entity loading

### Key Takeaways

1. **Namespace Change is Mandatory**: All `javax.*` imports must change to `jakarta.*`
2. **Only Imports Change**: Business logic and validation rules remain identical
3. **Configuration Simplified**: `application.properties` replaces `persistence.xml`
4. **Dependencies Matter**: Ensure all required Quarkus extensions are included

### Files Migrated

| v8 File | v9 File | Changes |
|---------|---------|---------|
| Customer.java | [`Customer.java`](v9-app/src/main/java/com/example/datamodels/Customer.java) | javax → jakarta imports |
| Order.java | [`Order.java`](v9-app/src/main/java/com/example/datamodels/Order.java) | javax → jakarta imports |
| Product.java | [`Product.java`](v9-app/src/main/java/com/example/datamodels/Product.java) | javax → jakarta imports |
| persistence.xml | [`application.properties`](v9-app/src/main/resources/application.properties) | Configuration format |

### Quick Reference: Import Changes

```java
// v8 (OLD)                          // v9 (NEW)
javax.persistence.Entity         →   jakarta.persistence.Entity
javax.persistence.Table          →   jakarta.persistence.Table
javax.persistence.Id             →   jakarta.persistence.Id
javax.persistence.Column         →   jakarta.persistence.Column
javax.persistence.OneToMany      →   jakarta.persistence.OneToMany
javax.persistence.ManyToOne      →   jakarta.persistence.ManyToOne
javax.validation.constraints.*   →   jakarta.validation.constraints.*
```

### Additional Resources

- [IBM BAMOE v8 to v9 Upgrade Guide](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-from-80x)

---
