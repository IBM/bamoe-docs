# Tutorial 10: Multi-Module Maven Projects - BAMOE v8 to v9 Migration

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

---

## Introduction

This tutorial demonstrates how to migrate a multi-module Maven project from BAMOE v8.0.x to BAMOE v9.3.x. Multi-module projects are common in enterprise applications where code is organized into separate, reusable modules with clear dependencies.

### What You'll Learn

- How to structure multi-module projects in v8 and v9
- Maven dependency management across modules
- Module interdependencies and build order
- Packaging differences (kjar vs jar)
- Parent POM configuration for both versions
- Best practices for modular architecture

### What You'll Migrate

This tutorial includes a three-module project structure:

1. **data-model** - Shared domain objects and entities
2. **business-rules** - DRL rules and decision logic
3. **process-service** - BPMN processes and service layer

### Why This Scenario Matters

- **Code Reusability**: Shared modules can be used across multiple projects
- **Separation of Concerns**: Clear boundaries between data, logic, and processes
- **Team Collaboration**: Different teams can work on different modules
- **Dependency Management**: Centralized version control through parent POM
- **Build Efficiency**: Only changed modules need to be rebuilt

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

3. **IDE** (VS Code recommended with BAMOE developer tools extension)

### Required Knowledge

- Maven multi-module project structure
- Maven dependency management
- Basic understanding of Maven reactor build
- Familiarity with BAMOE/jBPM concepts

---

## Understanding Multi-Module Projects

### v8 Multi-Module Structure

```
v8-app/
├── pom.xml (parent)
├── data-model/
│   ├── pom.xml
│   └── src/main/java/
│       └── com/example/multimodule/model/
│           └── Order.java (javax.persistence)
├── business-rules/
│   ├── pom.xml (packaging: kjar)
│   └── src/main/resources/
│       ├── META-INF/kmodule.xml
│       └── com/example/multimodule/rules/
│           └── order-validation.drl (ruleflow-group)
└── process-service/
    ├── pom.xml (packaging: war)
    └── src/main/resources/
        └── META-INF/kie-deployment-descriptor.xml
```

### v9 Multi-Module Structure

```
v9-app/
├── pom.xml (parent)
├── data-model/
│   ├── pom.xml
│   └── src/main/java/
│       └── com/example/multimodule/model/
│           └── Order.java (jakarta.persistence)
├── business-rules/
│   ├── pom.xml (packaging: jar)
│   ├── src/main/java/
│   │   └── com/example/multimodule/rules/
│   │       └── OrderValidationUnit.java (Rule Unit)
│   └── src/main/resources/
│       └── com/example/multimodule/rules/
│           └── OrderValidationUnit.drl (unit syntax)
└── process-service/
    ├── pom.xml (packaging: jar, Quarkus)
    └── src/main/resources/
        └── application.properties
```

### Key Architectural Differences

| Aspect | v8 | v9 |
|--------|----|----|
| **Parent POM** | KIE BOM | Quarkus BOM |
| **Java Version** | 1.8 | 17 |
| **Packaging** | kjar, war | jar |
| **Rules Module** | kjar with kmodule.xml | jar with Rule Units |
| **Service Module** | WAR for app server | Quarkus JAR |
| **Persistence** | javax.persistence | jakarta.persistence |
| **Configuration** | XML-based | Properties-based |

---

## Step-by-Step Migration

### Step 1: Analyze v8 Multi-Module Structure

First, understand your v8 project structure:

```bash
cd v8-app
mvn dependency:tree
```

**Key observations:**
- Parent POM defines common dependencies
- Modules have specific packaging types
- Inter-module dependencies are declared
- Build order is determined by Maven reactor

### Step 2: Update Parent POM for v9

**Key Changes in [`pom.xml`](v9-app/pom.xml):**

1. **Update Java version:**
   ```xml
   <maven.compiler.release>17</maven.compiler.release>
   ```

2. **Add Quarkus BOM:**
   ```xml
    <dependencyManagement>
        <dependencies>
        <dependency>
            <groupId>com.ibm.bamoe</groupId>
            <artifactId>bamoe-quarkus-bom</artifactId>
            <version>3.20.3</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
        </dependencies>
    </dependencyManagement>
    ```

3. **Manage internal module versions centrally:**
   ```xml
   <dependencyManagement>
       <dependencies>
           <dependency>
               <groupId>com.example.multimodule</groupId>
               <artifactId>data-model</artifactId>
               <version>${project.version}</version>
           </dependency>
       </dependencies>
   </dependencyManagement>
   ```

### Step 3: Migrate Data Model Module

**Changes in [`data-model/pom.xml`](v9-app/data-model/pom.xml):**

1. **Add Jakarta dependencies (managed by Quarkus BOM):**
   ```xml
   <!-- Jakarta Persistence API -->
   <dependency>
       <groupId>jakarta.persistence</groupId>
       <artifactId>jakarta.persistence-api</artifactId>
   </dependency>

   <!-- Jakarta Validation API -->
   <dependency>
       <groupId>jakarta.validation</groupId>
       <artifactId>jakarta.validation-api</artifactId>
   </dependency>

   <!-- Jackson for JSON serialization -->
   <dependency>
       <groupId>com.fasterxml.jackson.core</groupId>
       <artifactId>jackson-annotations</artifactId>
   </dependency>
   ```

**Changes in Entity Classes (e.g., Order.java):**

1. **Update imports from javax to jakarta:**
   ```java
   // v8
   import javax.persistence.Entity;
   import javax.validation.constraints.NotNull;
   
   // v9
   import jakarta.persistence.Entity;
   import jakarta.validation.constraints.NotNull;
   ```

**Note:** Only imports change; class structure remains the same.

### Step 4: Migrate Business Rules Module

**Changes in [`business-rules/pom.xml`](v9-app/business-rules/pom.xml):**

1. **Change packaging from kjar to jar:**
   ```xml
   <!-- v8 -->
   <packaging>kjar</packaging>
   
   <!-- v9 -->
   <packaging>jar</packaging>
   ```

2. **Remove kie-maven-plugin:**
   ```xml
   <!-- Remove this entire plugin section -->
   <plugin>
       <groupId>org.kie</groupId>
       <artifactId>kie-maven-plugin</artifactId>
       <extensions>true</extensions>
   </plugin>
   ```

3. **Update dependencies:**
   ```xml
   <!-- Add data-model dependency -->
   <dependency>
       <groupId>com.example.multimodule</groupId>
       <artifactId>data-model</artifactId>
   </dependency>

   <!-- Add Kogito Drools (includes rule units support) -->
   <dependency>
       <groupId>org.kie.kogito</groupId>
       <artifactId>kogito-drools</artifactId>
   </dependency>

   <!-- MicroProfile OpenAPI for Schema annotations (optional, for API documentation) -->
   <dependency>
       <groupId>org.eclipse.microprofile.openapi</groupId>
       <artifactId>microprofile-openapi-api</artifactId>
       <scope>provided</scope>
   </dependency>
   ```

**Create Rule Unit Class:**

Create [`OrderValidationService.java`](v9-app/business-rules/src/main/java/com/example/multimodule/rules/OrderValidationService.java) with:
- Implements `RuleUnitData`
- Contains `DataStore<Order> orders` field for order data

**Detailed Purpose and Architecture:**

The `OrderValidationService` class is a **Rule Unit** - the v9 replacement for v8's kmodule.xml configuration. It serves multiple critical purposes:

1. **Data Structure Definition**: Defines the data contract between Java code and DRL rules
2. **Type-Safe Binding**: Provides compile-time type safety for rule execution
3. **REST API Generation**: Enables Quarkus to auto-generate REST endpoints
4. **OpenAPI Documentation**: Generates Swagger/OpenAPI specs for the rules API

**Key Components:**

```java
public class OrderValidationService implements RuleUnitData {
    
    // DataStore for managing Order objects during rule execution
    @Schema(implementation = Order[].class)  // For OpenAPI spec generation
    private DataStore<Order> orders = DataSource.createStore();
    
    // Getter and setter for Quarkus integration
    public DataStore<Order> getOrders() { return orders; }
    public void setOrders(DataStore<Order> orders) { this.orders = orders; }
}
```

**What This Enables:**

- **Automatic REST Endpoint**: `POST /OrderValidationService`
- **Direct Rule Execution**: Call rules without BPMN process
- **Multi-Module Integration**: Links business-rules module with data-model module
- **BPMN Integration**: Used by Business Rule Tasks in process-service module

**Migration Context:**

| Aspect | v8 | v9 |
|--------|----|----|
| Configuration | kmodule.xml | Rule Unit class |
| Data Binding | Untyped | Type-safe with generics |
| API Generation | Manual | Automatic |
| Documentation | Manual | Auto-generated OpenAPI |

**CRITICAL: Required Methods**

The Rule Unit class MUST include both getter and setter:

```java
// REQUIRED: Getter for rule execution
public DataStore<Order> getOrders() {
    return orders;
}

// REQUIRED: Setter for Quarkus deserialization
public void setOrders(DataStore<Order> orders) {
    this.orders = orders;
}
```

**Common Error - Missing Setter:**

If the `setOrders()` method is missing, you'll encounter:
- JSON deserialization failures
- "Cannot set field" errors from Quarkus
- REST API calls failing with 400/500 errors

The setter is required for:
- JSON request deserialization from REST API calls
- Proper data binding during rule execution
- Integration with Quarkus CDI container

- Uses `@Schema(implementation = Order[].class)` annotation for OpenAPI documentation

**Update DRL Files:**

Keep the traditional ruleflow-group syntax (v9 supports both approaches):

```drl
package com.example.multimodule.rules;

import com.example.multimodule.model.Order;
import java.math.BigDecimal;

rule "High Value Order Requires Approval"
    ruleflow-group "order-validation"
    when
        $order: Order(totalAmount.compareTo(new BigDecimal("1000.00")) > 0, status == "PENDING")
    then
        $order.setStatus("REQUIRES_APPROVAL");
        update($order);
end

rule "Low Value Order Auto Approval"
    ruleflow-group "order-validation"
    when
        $order: Order(totalAmount.compareTo(new BigDecimal("1000.00")) <= 0, status == "PENDING")
    then
        $order.setStatus("APPROVED");
        update($order);
end
```

**Remove Configuration Files:**
- Delete `META-INF/kmodule.xml` (not needed in v9)

### Step 5: Migrate Process Service Module

**Changes in [`process-service/pom.xml`](v9-app/process-service/pom.xml):**

1. **Change packaging from war to jar:**
   ```xml
   <!-- v8 -->
   <packaging>war</packaging>
   
   <!-- v9 -->
   <packaging>jar</packaging>
   ```

2. **Replace KIE Server dependencies with Quarkus:**
   ```xml
   <!-- Remove v8 dependency -->
   <dependency>
       <groupId>org.kie.server</groupId>
       <artifactId>kie-server</artifactId>
   </dependency>
   
   <!-- Add v9 dependencies -->
   <dependency>
       <groupId>io.quarkus</groupId>
       <artifactId>quarkus-arc</artifactId>
   </dependency>
   <dependency>
       <groupId>io.quarkus</groupId>
       <artifactId>quarkus-resteasy-reactive</artifactId>
   </dependency>
   <dependency>
       <groupId>com.ibm.bamoe</groupId>
       <artifactId>bamoe-quarkus</artifactId>
   </dependency>
   <dependency>
       <groupId>org.kie.kogito</groupId>
       <artifactId>kogito-quarkus-processes</artifactId>
   </dependency>
   <dependency>
       <groupId>org.kie.kogito</groupId>
       <artifactId>kogito-quarkus-rules</artifactId>
   </dependency>
   ```

3. **Add Quarkus Maven plugin:**
   ```xml
   <build>
       <plugins>
           <plugin>
               <groupId>io.quarkus</groupId>
               <artifactId>quarkus-maven-plugin</artifactId>
               <extensions>true</extensions>
           </plugin>
       </plugins>
   </build>
   ```

**Replace XML Configuration with Properties:**

1. **Delete:** `META-INF/kie-deployment-descriptor.xml`

2. **Create:** [`application.properties`](v9-app/process-service/src/main/resources/application.properties) with:
   ```properties
   quarkus.application.name=multi-module-process-service-v9
   quarkus.http.port=8080
   kogito.service.url=http://localhost:8080
   
   # Swagger UI
   quarkus.swagger-ui.always-include=true
   quarkus.swagger-ui.path=/q/swagger-ui
   
   # Disable dev services
   quarkus.kogito.devservices.enabled=false
   ```

### Step 6: Build the Multi-Module Project

#### Build v9 Project

```bash
cd v9-app

# Clean and build all modules
mvn clean install

# Build specific module
mvn clean install -pl data-model

# Build module and its dependencies
mvn clean install -pl process-service -am
```

If you are using Gradle, use the following command:

```bash
gradle clean build
```

**Expected Output:**
```
[INFO] Reactor Summary:
[INFO] 
[INFO] Multi-Module Project - BAMOE v9 Parent ............ SUCCESS
[INFO] Data Model Module - v9 ............................ SUCCESS
[INFO] Business Rules Module - v9 ........................ SUCCESS
[INFO] Process Service Module - v9 ....................... SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
```

### Step 7: Run the Application

```bash
cd process-service
mvn quarkus:dev
```

**Expected Output:**
```
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   

INFO  [io.quarkus] multi-module-process-service-v9 1.0.0-SNAPSHOT on JVM started
INFO  [io.quarkus] Profile dev activated. Live Coding activated.
INFO  [io.quarkus] Installed features: [bamoe-quarkus, kogito-processes, kogito-rules, ...]
```

---



## Testing

### Test 1: Verify Module Build Order

```bash
cd v9-app
mvn clean install -X | grep "Building"
```

**Expected Output:**
```
[INFO] Building Multi-Module Project - BAMOE v9 Parent 1.0.0-SNAPSHOT
[INFO] Building Data Model Module - v9 1.0.0-SNAPSHOT
[INFO] Building Business Rules Module - v9 1.0.0-SNAPSHOT
[INFO] Building Process Service Module - v9 1.0.0-SNAPSHOT
```

### Test 2: Verify Module Dependencies

```bash
cd process-service
mvn dependency:tree
```

**Expected Output:**
```
[INFO] com.example.multimodule:process-service:jar:1.0.0-SNAPSHOT
[INFO] +- com.example.multimodule:data-model:jar:1.0.0-SNAPSHOT:compile
[INFO] +- com.example.multimodule:business-rules:jar:1.0.0-SNAPSHOT:compile
[INFO] +- io.quarkus:quarkus-arc:jar:3.2.12.Final-redhat-00003:compile
[INFO] +- com.ibm.bamoe:bamoe-quarkus:jar:9.3.1-ibm-0001:compile
```

### Test 3: Run Application and Check Modules

```bash
cd process-service
mvn quarkus:dev
```

Check logs for module loading:
```
INFO  [org.kie.kogito] Loading rule unit: OrderValidationUnit
INFO  [org.kie.kogito] Loaded data model: Order
```

### Test 4: Access Swagger UI and Test APIs

#### Accessing Swagger UI

1. Start application: `mvn quarkus:dev`
2. Navigate to: http://localhost:8080/q/swagger-ui
3. The Swagger UI provides interactive documentation for all REST endpoints

#### Testing the Process API

Test the complete order validation workflow:

1. Expand **POST /orderValidation**
2. Click "Try it out"
3. Use this sample request:

```json
{
  "order": {
    "orderNumber": "ORD-001",
    "customerName": "John Doe",
    "orderDate": "2024-01-15T10:00:00Z",
    "totalAmount": 1500.00,
    "status": "PENDING"
  }
}
```

4. Click "Execute"
5. Expected response (status: `REQUIRES_APPROVAL` for orders > $1000):

```json
{
  "id": "7b193c1a-1173-41bf-aae7-35ec6a1f82e7",
  "order": {
    "orderNumber": "ORD-001",
    "customerName": "John Doe",
    "orderDate": "2024-01-15T10:00:00Z",
    "totalAmount": 1500.00,
    "status": "REQUIRES_APPROVAL"
  }
}
```



**Rule Logic:**
- Orders > $1000 → Status: `REQUIRES_APPROVAL`
- Orders ≤ $1000 → Status: `APPROVED`
- Orders ≤ $0 → Status: `REJECTED`
- Missing customer name → Status: `REJECTED`

#### Key Differences: Process vs Rule Unit APIs

| Aspect | Process API (`/orderValidation`) | Rule Unit API (`/OrderValidationService`) |
|--------|----------------------------------|----------------------------------------|
| **Purpose** | Orchestrates complete workflow | Direct rule execution |
| **Input** | Single `order` object | Array of `orders` |
| **Behavior** | Creates process instance, executes Business Rule Task, tracks execution | Executes rules immediately, no process instance |
| **Use Case** | When you need orchestration, tracking, and management | When you only need rule evaluation |

#### Verification Checklist

- [ ] Swagger UI loads at http://localhost:8080/q/swagger-ui
- [ ] Process endpoints (`/orderValidation`) are visible
- [ ] Rule Unit endpoints (`/OrderValidationUnit`) are visible
- [ ] Process management endpoints are visible
- [ ] Can test POST /orderValidation successfully
- [ ] Request/Response schemas show Order model structure
- [ ] Health endpoints return 200 OK
- [ ] OpenAPI spec is accessible at /q/openapi

---

## Common Issues

### Issue 1: Module Not Found During Build

**Symptom:**
```
[ERROR] Failed to execute goal on project process-service: 
Could not resolve dependencies for project com.example.multimodule:process-service:jar:1.0.0-SNAPSHOT: 
Could not find artifact com.example.multimodule:data-model:jar:1.0.0-SNAPSHOT
```

**Cause:** Modules not built in correct order

**Solution:** Build from parent directory or use `-am` flag:

```bash
# Build from parent
cd v9-app
mvn clean install

# Or build with dependencies
mvn clean install -pl process-service -am
```

If you are using Gradle, use the following command:

```bash
gradle clean build
```

### Issue 2: Circular Dependencies

**Symptom:**

### Issue 3: OrderValidationService Missing Setter Method

**Symptom:**
```
[ERROR] Failed to deserialize request body
[ERROR] Cannot set field 'orders' on OrderValidationService
```

**Cause:** The Rule Unit class is missing the `setOrders()` method required for Quarkus deserialization

**Solution:** Ensure the Rule Unit class has BOTH getter and setter:

```java
public class OrderValidationService implements RuleUnitData {
    
    @Schema(implementation = Order[].class)
    private DataStore<Order> orders = DataSource.createStore();
    
    // REQUIRED: Getter for rule execution
    public DataStore<Order> getOrders() {
        return orders;
    }
    
    // REQUIRED: Setter for Quarkus deserialization
    public void setOrders(DataStore<Order> orders) {
        this.orders = orders;
    }
}
```

### Issue 4: Java Errors "Order cannot be resolved to a type"

**Symptom:**
```
[Java Error] The import com.example.multimodule.model cannot be resolved
[Java Error] Order cannot be resolved to a type
```

**Cause:** This is a **multi-module dependency issue** - the business-rules module depends on the data-model module, which hasn't been built yet.

**Solution:** Build modules in the correct order:

```bash
# Option 1: Build from parent directory (recommended)
cd v9-app
mvn clean install

# Option 2: Build data-model first, then business-rules
cd v9-app/data-model
mvn clean install
cd ../business-rules
mvn clean install

# Option 3: Build with dependencies
cd v9-app
mvn clean install -pl business-rules -am
```

If you are using Gradle, use the following command:

```bash
gradle clean build
```


**Why This Happens:**

In multi-module projects:
1. `business-rules` module declares dependency on `data-model` in its pom.xml
2. IDE shows errors until `data-model` is built and installed to local Maven repository
3. Maven reactor resolves this automatically during build

**Verification:**

After building, verify the dependency is resolved:

```bash
cd business-rules
mvn dependency:tree | grep data-model
```

Expected output:
```
[INFO] +- com.example.multimodule:data-model:jar:1.0.0-SNAPSHOT:compile
```

**Cause:** Module A depends on Module B, and Module B depends on Module A

**Solution:** Restructure dependencies to be acyclic:
- Extract common code to a shared module
- Use interfaces to break circular dependencies
- Review module boundaries

### Issue 5: kjar Packaging Not Recognized in v9

**Symptom:**
```
[ERROR] Unknown packaging: kjar
```

**Cause:** v9 doesn't support kjar packaging

**Solution:** Change to `jar` packaging and remove `kie-maven-plugin`:

```xml
<!-- v8 -->
<packaging>kjar</packaging>
<plugin>
    <groupId>org.kie</groupId>
    <artifactId>kie-maven-plugin</artifactId>
</plugin>

<!-- v9 -->
<packaging>jar</packaging>
<!-- No kie-maven-plugin needed -->
```

### Issue 6: javax to jakarta Import Errors

**Symptom:**
```
[ERROR] cannot find symbol: class Entity
[ERROR] location: package javax.persistence
```

**Cause:** Using javax imports in v9

**Solution:** Replace all javax imports with jakarta:

```java
// v8
import javax.persistence.Entity;
import javax.validation.constraints.NotNull;

// v9
import jakarta.persistence.Entity;
import jakarta.validation.constraints.NotNull;
```
### Issue 7: Process API Not Appearing in Swagger

**Symptom:** The `POST /orderValidation` endpoint doesn't appear in Swagger UI

**Cause:** BPMN file missing or process not properly configured

**Solution:**

1. **Verify BPMN file exists:**
   ```bash
   ls v9-app/process-service/src/main/resources/
   # Should show: order-validation-process.bpmn
   ```

2. **Check process ID in BPMN file:**
   - Open the BPMN file
   - Verify: `<bpmn2:process id="orderValidation" ...>`

3. **Ensure kogito-quarkus-processes dependency:**
   ```xml
   <dependency>
       <groupId>org.jbpm</groupId>
       <artifactId>jbpm-with-drools-quarkus</artifactId>
   </dependency>
   ```

---

## Summary

### Migration Checklist

- [] Update parent POM to use Quarkus BOM
- [] Change Java version from 1.8 to 17
- [] Migrate data-model module (javax → jakarta)
- [] Convert business-rules from kjar to jar with Rule Units
- [] Migrate process-service to Quarkus
- [] Update all module POMs
- [] Remove kmodule.xml and kie-deployment-descriptor.xml
- [] Create application.properties
- [] Test multi-module build
- [] Verify module dependencies
- [] Run and test application

### Key Takeaways

1. **Module Structure Preserved**: The modular architecture remains the same
2. **Packaging Changes**: kjar → jar, war → jar
3. **Dependency Management**: Centralized in parent POM
4. **Build Tool**: Maven reactor handles module order
5. **Rule Units Required**: Legacy rules need to be converted
6. **Quarkus Integration**: Process service becomes Quarkus application

### Files Migrated

| v8 File | v9 File | Changes |
|---------|---------|---------|
| [`pom.xml`](v8-app/pom.xml) | [`pom.xml`](v9-app/pom.xml) | BOM updates, Java 17 |
| [`data-model/pom.xml`](v8-app/data-model/pom.xml) | [`data-model/pom.xml`](v9-app/data-model/pom.xml) | Jakarta dependencies |
| [`Order.java`](v8-app/data-model/src/main/java/com/example/multimodule/model/Order.java) | [`Order.java`](v9-app/data-model/src/main/java/com/example/multimodule/model/Order.java) | javax → jakarta |
| [`business-rules/pom.xml`](v8-app/business-rules/pom.xml) | [`business-rules/pom.xml`](v9-app/business-rules/pom.xml) | kjar → jar, Rule Units |
| [`order-validation.drl`](v8-app/business-rules/src/main/resources/com/example/multimodule/rules/order-validation.drl) | [`OrderValidationService.drl`](v9-app/business-rules/src/main/resources/com/example/multimodule/rules/OrderValidationService.drl) | Updated rules with BigDecimal |
| N/A | [`OrderValidationService.java`](v9-app/business-rules/src/main/java/com/example/multimodule/rules/OrderValidationService.java) | Rule Unit data structure |
| [`process-service/pom.xml`](v8-app/process-service/pom.xml) | [`process-service/pom.xml`](v9-app/process-service/pom.xml) | Quarkus dependencies |
| [`kie-deployment-descriptor.xml`](v8-app/process-service/src/main/resources/META-INF/kie-deployment-descriptor.xml) | [`application.properties`](v9-app/process-service/src/main/resources/application.properties) | Configuration format |

### Additional Resources

- [IBM BAMOE v8 to v9 Upgrade Guide](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-from-80x)

---
