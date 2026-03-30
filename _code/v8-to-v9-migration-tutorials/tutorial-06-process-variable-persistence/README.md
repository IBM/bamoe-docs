# Tutorial 6: Process Variable Persistence Migration from BAMOE v8 to v9

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

---

## Introduction

This tutorial demonstrates how to migrate process variable persistence and database schema configurations from BAMOE v8 to v9. Process variable persistence is critical for long-running processes that need to survive server restarts and maintain state across transactions.

### What You'll Learn

- How persistence works differently in v8 vs v9
- Migrating JPA entity classes from `javax.persistence` to `jakarta.persistence`
- Converting v8 `persistence.xml` to v9 Quarkus configuration
- Understanding database schema generation and marshalling strategies
- Configuring Hibernate ORM in Quarkus
- Testing persistent process variables

### What You'll Migrate

**v8 Configuration:**
- `persistence.xml` with JTA configuration
- `javax.persistence` annotations
- JBoss/WildFly-specific settings
- `kie-deployment-descriptor.xml` persistence settings

**v9 Configuration:**
- `application.properties` with Quarkus datasource
- `jakarta.persistence` annotations
- Quarkus Hibernate ORM configuration
- Kogito persistence add-ons

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

3. **IDE** (VS Code with BAMOE Developer Tools extension recommended)

### Required Knowledge

- Basic understanding of JPA/Hibernate
- Familiarity with database concepts
- Understanding of process variables in BPMN
- Basic knowledge of Quarkus configuration

---

### Understanding Process Variable Persistence

### v8 Persistence Architecture

In BAMOE v8 (based on jBPM 7.x), persistence is configured through:

1. **persistence.xml** - Defines persistence units, datasources, and Hibernate properties
2. **kie-deployment-descriptor.xml** - Specifies persistence mode and marshalling strategies
3. **JTA Transactions** - Uses Java Transaction API with application server support
4. **javax.persistence** - Uses Java EE persistence annotations

**v8 Persistence Flow:**
```
Process Variable → Marshaller → JPA Entity → JTA Transaction → Database
                                    ↓
                            persistence.xml config
```

### v9 Persistence Architecture

In BAMOE v9 (based on Kogito), persistence is configured through:

1. **application.properties** - Quarkus-style configuration for datasource and Hibernate
2. **Kogito Add-ons** - `kie-addons-quarkus-persistence-jdbc` for process persistence
3. **Quarkus Transactions** - Uses Quarkus transaction management
4. **jakarta.persistence** - Uses Jakarta EE persistence annotations

**v9 Persistence Flow:**
```
Process Variable → JSON/Protobuf → Database (via Kogito add-on)
                        ↓
                JPA Entities (optional, for custom data)
                        ↓
                application.properties config
```

### Key Differences

| Aspect | v8 | v9 |
|--------|----|----|
| **Configuration** | `persistence.xml` | `application.properties` |
| **Annotations** | `javax.persistence.*` | `jakarta.persistence.*` |
| **Transaction Management** | JTA (application server) | Quarkus transactions |
| **Persistence Provider** | Hibernate (configured in XML) | Hibernate ORM (Quarkus extension) |
| **Process State Storage** | Database tables via jBPM | Kogito persistence add-on |
| **Marshalling** | Configurable strategies | JSON/Protobuf (automatic) |
| **Datasource** | JNDI lookup | Quarkus datasource |

---

## Step-by-Step Migration

### Step 1: Analyze v8 Persistence Configuration

First, examine your v8 [`persistence.xml`](v8-app/src/main/resources/META-INF/persistence.xml):

```xml
<persistence-unit name="com.example:persistence-demo:1.0.0-SNAPSHOT" transaction-type="JTA">
    <provider>org.hibernate.jpa.HibernatePersistenceProvider</provider>
    <jta-data-source>java:jboss/datasources/ExampleDS</jta-data-source>
    
    <!-- Entity classes -->
    <class>com.example.persistence.Order</class>
    <class>com.example.persistence.OrderItem</class>
    
    <properties>
        <property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>
        <property name="hibernate.hbm2ddl.auto" value="update"/>
        <property name="hibernate.show_sql" value="false"/>
    </properties>
</persistence-unit>
```

**Key elements to migrate:**
- Persistence unit name → Not needed in v9
- JTA datasource → Quarkus datasource configuration
- Entity classes → Automatically discovered in v9
- Hibernate properties → Quarkus properties

### Step 2: Create v9 Project Structure

Create a new Quarkus-based project structure:

```bash
v9-app/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/example/persistence/
│       │       ├── Order.java
│       │       └── OrderItem.java
│       └── resources/
│           ├── application.properties
│           └── orderProcess.bpmn
```

### Step 3: Update Maven Dependencies

Replace v8 [`pom.xml`](v8-app/pom.xml) with v9 Quarkus dependencies in [`pom.xml`](v9-app/pom.xml):

**v8 Dependencies (remove):**
```xml
<packaging>kjar</packaging>
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-maven-plugin</artifactId>
</dependency>
<dependency>
    <groupId>javax.persistence</groupId>
    <artifactId>javax.persistence-api</artifactId>
</dependency>
```

**v9 Dependencies (add):**
```xml
<packaging>jar</packaging>

<!-- Kogito/BAMOE Core -->
<dependency>
    <groupId>org.jbpm</groupId>
    <artifactId>jbpm-with-drools-quarkus</artifactId>
</dependency>

<!-- Persistence Add-on -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-addons-quarkus-persistence-jdbc</artifactId>
</dependency>

<!-- Database -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jdbc-h2</artifactId>
</dependency>

<!-- Hibernate ORM -->
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-hibernate-orm</artifactId>
</dependency>
```

### Step 4: Migrate JPA Entity Classes

Update entity annotations from `javax` to `jakarta`:

**v8 Order.java:**
```java
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.OneToMany;
import javax.persistence.CascadeType;
import javax.persistence.FetchType;

@Entity
@Table(name = "orders")
public class Order implements Serializable {
    @Id
    @Column(name = "order_id")
    private String orderId;
    
    // ... other fields
}
```

**v9 Order.java (migrated):**
```java
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.persistence.OneToMany;
import jakarta.persistence.CascadeType;
import jakarta.persistence.FetchType;

@Entity
@Table(name = "orders")
public class Order implements Serializable {
    @Id
    @Column(name = "order_id")
    private String orderId;
    
    // ... other fields (no changes needed)
}
```

**Migration Steps:**
1. Open each entity class ([`Order.java`](v8-app/src/main/java/com/example/persistence/Order.java), [`OrderItem.java`](v8-app/src/main/java/com/example/persistence/OrderItem.java))
2. Find and replace all imports:
   - `javax.persistence` → `jakarta.persistence`
3. No other code changes required!

**Automated Migration:**
```bash
# Find all Java files with javax.persistence imports
find src/main/java -name "*.java" -exec grep -l "javax.persistence" {} \;

# Replace in all files (Linux/Mac)
find src/main/java -name "*.java" -exec sed -i 's/javax\.persistence/jakarta.persistence/g' {} \;

# Replace in all files (Mac with BSD sed)
find src/main/java -name "*.java" -exec sed -i '' 's/javax\.persistence/jakarta.persistence/g' {} \;
```

### Step 5: Create Quarkus Configuration

Replace [`persistence.xml`](v8-app/src/main/resources/META-INF/persistence.xml) with [`application.properties`](v9-app/src/main/resources/application.properties):

```properties
# Data Source Configuration (H2 Database)
quarkus.datasource.db-kind=h2
quarkus.datasource.username=sa
quarkus.datasource.password=
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE

# Hibernate ORM Configuration
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.log.sql=false
quarkus.hibernate-orm.sql-load-script=no-file

# Explicitly list packages to scan for entities
quarkus.hibernate-orm.packages=com.example.persistence

# Kogito Persistence Configuration
kogito.persistence.type=jdbc
kogito.persistence.proto.marshaller=false

# Process Management Add-on
kogito.addon.process-management.enabled=true
```

**Configuration Mapping:**

| v8 persistence.xml | v9 application.properties |
|-------------------|---------------------------|
| `<jta-data-source>java:jboss/datasources/ExampleDS</jta-data-source>` | `quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb` |
| `<property name="hibernate.dialect" value="org.hibernate.dialect.H2Dialect"/>` | `quarkus.datasource.db-kind=h2` (auto-detected) |
| `<property name="hibernate.hbm2ddl.auto" value="update"/>` | `quarkus.hibernate-orm.database.generation=update` |
| `<property name="hibernate.show_sql" value="false"/>` | `quarkus.hibernate-orm.log.sql=false` |
| `<class>com.example.persistence.Order</class>` | `quarkus.hibernate-orm.packages=com.example.persistence` |

### Step 6: Remove v8-Specific Configuration Files

Delete these v8 configuration files (not needed in v9):

```bash
rm src/main/resources/META-INF/persistence.xml
rm src/main/resources/META-INF/kmodule.xml
rm src/main/resources/META-INF/kie-deployment-descriptor.xml
```

**Why remove these files?**
- **persistence.xml**: Replaced by `application.properties`
- **kmodule.xml**: Not used in Kogito/v9
- **kie-deployment-descriptor.xml**: Replaced by Quarkus configuration

### Step 7: Migrate BPMN Process (Updated Structure)

The BPMN process file has been updated to follow BAMOE v9 best practices based on the [`bamoe-examples/process-persistence`](../../bamoe-examples/process-persistence) reference implementation.

**Key Changes from v8 to v9:**

1. **Updated XML Structure:**
   - Added proper namespace declarations following Apache License header
   - Included `bpsim` namespace for simulation support
   - Added `drools:packageName`, `drools:version`, and `drools:adHoc` attributes

2. **Enhanced Item Definitions:**
   - Comprehensive item definitions for all user task inputs/outputs
   - Proper structureRef for custom types: `com.example.persistence.Order`
   - Added metadata for task parameters (Skippable, Priority, TaskName, etc.)

3. **Process Variable Metadata:**
   - Added `customTags` metadata to distinguish variable types:
     - `input,required` - for input variables like `order`
     - `internal` - for internal variables like `approvalDecision`
   - This helps with API generation and validation

4. **User Task Configuration:**
   - Proper `ioSpecification` with typed inputs/outputs
   - Data input/output associations with explicit mappings
   - TaskName assignment for proper task identification

5. **Improved Diagram Layout:**
   - Better positioning of BPMN shapes for clarity
   - Proper edge routing between elements
   - Consistent spacing and alignment

**Process Structure:**
```
Start → Initialize Order → Approve Order (User Task) → Gateway
                                                          ├→ Approved → Mark Approved → End
                                                          └→ Rejected → Mark Rejected → End
```

**Important Variable Definitions:**

```xml
<!-- Process Variables with Metadata -->
<bpmn2:property id="order" itemSubjectRef="_orderItem" name="order">
  <bpmn2:extensionElements>
    <drools:metaData name="customTags">
      <drools:metaValue><![CDATA[input,required]]></drools:metaValue>
    </drools:metaData>
  </bpmn2:extensionElements>
</bpmn2:property>

<bpmn2:property id="approvalDecision" itemSubjectRef="_approvalDecisionItem" name="approvalDecision">
  <bpmn2:extensionElements>
    <drools:metaData name="customTags">
      <drools:metaValue><![CDATA[internal]]></drools:metaValue>
    </drools:metaData>
  </bpmn2:extensionElements>
</bpmn2:property>
```

**User Task Data Mapping:**
```xml
<!-- Input: Pass order to task -->
<bpmn2:dataInputAssociation>
  <bpmn2:sourceRef>order</bpmn2:sourceRef>
  <bpmn2:targetRef>_ApproveOrder_orderInputX</bpmn2:targetRef>
</bpmn2:dataInputAssociation>

<!-- Output: Capture decision from task -->
<bpmn2:dataOutputAssociation>
  <bpmn2:sourceRef>_ApproveOrder_decisionOutputX</bpmn2:sourceRef>
  <bpmn2:targetRef>approvalDecision</bpmn2:targetRef>
</bpmn2:dataOutputAssociation>
```

**Why These Changes Matter:**
- Ensures compatibility with Kogito code generation
- Enables proper REST API generation for process instances
- Supports custom forms generation
- Improves process monitoring and debugging
- Follows Apache/IBM BAMOE best practices

### Step 8: Configure Database Schema Generation

In v9, Hibernate automatically generates database schema based on your entities.

**Schema Generation Options:**

```properties
# Option 1: Update schema (recommended for development)
quarkus.hibernate-orm.database.generation=update

# Option 2: Drop and create (WARNING: deletes all data)
quarkus.hibernate-orm.database.generation=drop-and-create

# Option 3: Validate only (production)
quarkus.hibernate-orm.database.generation=validate

# Option 4: None (manual schema management)
quarkus.hibernate-orm.database.generation=none
```

**For Production:**
```properties
# Use Flyway or Liquibase for schema migrations
quarkus.hibernate-orm.database.generation=none
quarkus.flyway.migrate-at-start=true
```

### Step 9: Understanding Variable Marshalling

**v8 Marshalling:**
- Configured in `kie-deployment-descriptor.xml`
- Uses custom marshalling strategies
- Requires explicit configuration

```xml
<marshalling-strategies>
    <marshalling-strategy>
        <resolver>mvel</resolver>
        <identifier>new org.jbpm.marshalling.impl.ProcessInstanceResolverStrategy()</identifier>
    </marshalling-strategy>
</marshalling-strategies>
```

**v9 Marshalling:**
- Automatic JSON serialization for process variables
- Optional Protobuf for performance
- No explicit configuration needed

```properties
# Use JSON marshalling (default, easier debugging)
kogito.persistence.proto.marshaller=false

# Use Protobuf marshalling (better performance)
kogito.persistence.proto.marshaller=true
```

**When to use Protobuf:**
- High-volume process instances
- Performance-critical applications
- Large process variable payloads

**When to use JSON:**
- Development and debugging
- Human-readable persistence
- Easier troubleshooting

### Step 10: Build the v9 Application

```bash
cd v9-app
mvn clean install
```

If you are using Gradle, use the following command:

```bash
gradle clean build
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45.123 s
```

**Common Build Issues:**

1. **Java version mismatch:**
   ```
   [ERROR] Source option 8 is no longer supported. Use 17 or later.
   ```
   **Solution:** Ensure Java 17+ is installed and configured

2. **Missing dependencies:**
   ```
   [ERROR] Could not resolve dependencies for project
   ```
   **Solution:** Check Red Hat Maven repository configuration

3. **Entity scanning issues:**
   ```
   [WARN] No entities found in package
   ```
   **Solution:** Verify `quarkus.hibernate-orm.packages` property

---

## Testing

### Test 1: Verify Database Schema Creation

Start the application in dev mode:

```bash
cd v9-app
mvn quarkus:dev
```

### Test 2: Start a Process with Persistent Variables

**Using Swagger UI:**
1. Open http://localhost:8080/q/swagger-ui
2. Find **POST `/orderProcess`** 
4. Click "Try it out"
5. Enter the request body and click "Execute"

**Using curl - Create a test order:**

```bash
curl -X POST http://localhost:8080/orderProcess \
  -H "Content-Type: application/json" \
  -d '{
    "order": {
      "orderId": "ORD-001",
      "customerName": "John Doe",
      "totalAmount": 1500.00,
      "status": "PENDING",
      "approved": false
    }
  }'
```

**Expected Response:**
```json
{"id":"96c97e40-5728-406f-8efd-d5c800416801"}
```

### Test 3: Verify Process Variable Persistence

**Query the database to verify persistence:**

```sql
-- Check if order is persisted
SELECT * FROM orders WHERE order_id = 'ORD-001';

-- Check process instance
SELECT * FROM process_instances;
```

**Restart the application:**
```bash
# Stop with Ctrl+C
# Restart
mvn quarkus:dev
```

**Verify process state is restored:**
```bash
curl http://localhost:8080/orderProcess
```

**Expected:** Process instances and variables are restored from database

### Test 4: Complete User Task and Verify Updates

**Get active tasks:**
```bash
curl http://localhost:8080/orderProcess/{processId}/tasks
```

**Complete the approval task:**
```bash
curl -X POST http://localhost:8080/orderProcess/{processId}/Approve_Order/{taskId} \
  -H "Content-Type: application/json" \
  -d '{
    "Decision": "APPROVE"
  }'
```

**Verify order status updated in database:**
```sql
SELECT * FROM orders WHERE order_id = 'ORD-001';
-- Should show: status='APPROVED', approved=true
```

### Test 5: Performance Testing

**Test variable marshalling performance:**

```bash
# Create 100 process instances
for i in {1..100}; do
  curl -X POST http://localhost:8080/orderProcess \
    -H "Content-Type: application/json" \
    -d "{
      \"order\": {
        \"orderId\": \"ORD-$i\",
        \"customerName\": \"Customer $i\",
        \"totalAmount\": $((RANDOM % 10000)),
        \"status\": \"PENDING\",
        \"approved\": false
      }
    }"
done
```

**Monitor performance:**
- Check application logs for timing
- Query database for row counts
- Monitor memory usage

---

## Common Issues

### Issue 1: Missing Endpoint for Process 
**Symptom:**
- Swagger UI shows many endpoints but **no POST `/orderProcess`** endpoint
- Cannot start process instances via REST API
- Other endpoints (GET, PUT, PATCH, DELETE, task endpoints) are visible and working

**Root Causes:**

#### Issue 1a: BPMN Namespace Declarations 

**Problem:**
The BPMN file was using **v8-style namespace declarations** that prevent v9 endpoint generation.

**Incorrect v8 Style in [`orderProcess.bpmn`](v9-app/src/main/resources/orderProcess.bpmn):**
```xml
<bpmn2:definitions
  xmlns:tns="http://www.jboss.org/drools"
  xmlns="http://www.jboss.org/drools"
  ...>
  <bpmn2:process id="orderProcess" tns:packageName="com.example.persistence" ...>
```

**Correct v9 Style:**
```xml
<bpmn2:definitions
  xmlns:drools="http://www.jboss.org/drools"
  ...>
  <bpmn2:process id="orderProcess" drools:packageName="com.example.persistence" ...>
```

**Key Changes Required:**
- Remove `xmlns:tns="http://www.jboss.org/drools"`
- Remove default `xmlns="http://www.jboss.org/drools"`
- Add `xmlns:drools="http://www.jboss.org/drools"`
- Change `tns:packageName` to `drools:packageName`
- Change `tns:version` to `drools:version`
- Change `tns:adHoc` to `drools:adHoc`

**Why This Matters:**
The v8-style `tns:packageName` attribute and namespace declarations are **not recognized** by the v9 Kogito code generator. This causes:
- Process REST endpoints not being generated
- POST endpoint missing from Swagger UI

#### Issue 1b: Datasource Configuration Pattern

**Problem:**
The application.properties was using explicit JDBC URL configuration instead of profile-specific Quarkus dev services pattern.

**Incorrect Configuration:**
```properties
quarkus.datasource.db-kind=h2
quarkus.datasource.username=sa
quarkus.datasource.password=
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;...
%dev.quarkus.devservices.enabled=false
```

**Correct Configuration :**
```properties
%dev.quarkus.datasource.db-kind=h2
%dev.quarkus.datasource.devservices.properties.NON_KEYWORDS=VALUE,KEY
%dev.quarkus.datasource.jdbc.min-size=1
```

**Why This Works:**
Using `%dev.quarkus.datasource.db-kind=h2` instead of `quarkus.datasource.db-kind=h2` allows Quarkus to:
- Properly initialize the datasource for the dev profile
- Enable dev services with the correct configuration
- Generate all REST endpoints including POST

#### Issue 1c: Missing Swagger and Security Configuration

**Problem:**
Missing configuration properties that ensure Swagger UI and endpoints are always available.

**Required Configuration in [`application.properties`](v9-app/src/main/resources/application.properties):**
```properties
# Swagger UI Configuration
quarkus.http.cors=true
quarkus.http.cors.origins=*
quarkus.dev-ui.cors.enabled=false
quarkus.smallrye-openapi.path=/docs/openapi.json
quarkus.http.test-port=0
quarkus.swagger-ui.always-include=true
quarkus.kogito.data-index.graphql.ui.always-include=true

# Security Configuration (disable for development)
quarkus.oidc.enabled=false
kogito.auth.enabled=false
```

**Why These Are Required:**
- `quarkus.swagger-ui.always-include=true` ensures Swagger UI is available in all modes (dev, test, prod)
- `quarkus.kogito.data-index.graphql.ui.always-include=true` ensures Data Index GraphQL UI is always available
- `quarkus.oidc.enabled=false` and `kogito.auth.enabled=false` disable security for simple examples
- Without these, POST endpoint generation may be incomplete

### Key Takeaways

1. **Always use v9 BPMN namespace declarations**: Use `xmlns:drools` instead of `xmlns:tns` or default `xmlns`
2. **Use `drools:packageName` not `tns:packageName`**: The v9 code generator only recognizes `drools:` prefix
3. **Use Profile-Specific Datasource Configuration**: Always use `%dev.quarkus.datasource.*` for development datasources
4. **Always Include Swagger UI**: Set `quarkus.swagger-ui.always-include=true` to ensure all endpoints are visible
5. **Always Include Data Index GraphQL UI**: Set `quarkus.kogito.data-index.graphql.ui.always-include=true` for POST endpoint generation
6. **Explicitly Disable Security**: Set `quarkus.oidc.enabled=false` and `kogito.auth.enabled=false` for simple examples
7. **Follow bamoe-examples Pattern**: When in doubt, match the configuration pattern from bamoe-examples
8. **Clean rebuild after BPMN changes**: Always run `mvn clean compile` after modifying BPMN files

---

### Issue 2: H2 Reserved Keywords in Flyway Migration

**Symptom:**
```
org.h2.jdbc.JdbcSQLSyntaxErrorException: Syntax error in SQL statement
"create table definitions_nodes_metadata(...value varchar(255), key varchar(255)...)"
expected "identifier"
```

**Cause:** H2 database treats `VALUE` and `KEY` as reserved keywords, but BAMOE's Flyway migration scripts use them as column names

**Solution:**
Add `NON_KEYWORDS=VALUE,KEY` to the H2 JDBC URL in [`application.properties`](v9-app/src/main/resources/application.properties):
```properties
# NON_KEYWORDS allows H2 to use VALUE and KEY as column names (required for Flyway migrations)
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;NON_KEYWORDS=VALUE,KEY
```

**Why This Happens:**
- BAMOE's internal Flyway migrations create tables with columns named `value` and `key`
- H2 database reserves these as SQL keywords
- The `NON_KEYWORDS` parameter tells H2 to allow these as regular identifiers
- This is specific to H2; PostgreSQL, MySQL, and Oracle don't have this issue

**Alternative Solution - Use PostgreSQL:**
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/kogito
quarkus.datasource.username=kogito
quarkus.datasource.password=kogito
```


### Issue 3: Boolean Getter Method Naming Convention

**Symptom:**
```
[ERROR] Build step org.kie.kogito.quarkus.processes.deployment.ProcessesAssetsProcessor#postGenerationProcessing threw an exception: 
org.kie.memorycompiler.KieMemoryCompilerException: [org/kie/kogito/app/OrderMessageMarshaller.java (47:42) : cannot find symbol
  symbol:   method isApproved()
  location: variable t of type com.example.persistence.Order]
```

**Cause:** The Kogito marshaller code generator expects Boolean properties to have an `isXxx()` getter method, but the entity only has `getXxx()`

**Solution:**
Add both getter methods to Boolean properties in [`Order.java`](v9-app/src/main/java/com/example/persistence/Order.java):
```java
public Boolean getApproved() {
    return approved;
}

// JavaBeans convention: Boolean properties should have both getXxx() and isXxx()
// The Kogito marshaller expects isXxx() for Boolean fields
public Boolean isApproved() {
    return approved;
}

public void setApproved(Boolean approved) {
    this.approved = approved;
}
```

**Why This Happens:**
- JavaBeans specification allows both `getXxx()` and `isXxx()` for Boolean properties
- For primitive `boolean`, `isXxx()` is preferred
- For wrapper `Boolean`, both are valid but some frameworks expect `isXxx()`
- Kogito's code generator creates marshaller code that calls `isXxx()` for Boolean fields
- Without `isXxx()`, the generated code fails to compile

**Best Practice:**
Always provide both `getXxx()` and `isXxx()` for Boolean/boolean properties in entities used as process variables:
```java
// ✅ Correct - provides both methods
private Boolean approved;
public Boolean getApproved() { return approved; }
public Boolean isApproved() { return approved; }
public void setApproved(Boolean approved) { this.approved = approved; }

// ❌ Incorrect - only getXxx() will cause marshaller compilation errors
private Boolean approved;
```

### Issue 4: Process Variables Not Persisting

**Symptom:** Process variables are null after restart

**Cause:** Persistence add-on not configured

**Solution:**
```xml
<!-- Add to pom.xml -->
<dependency>
    <groupId>org.kie</groupId>
    <artifactId>kie-addons-quarkus-persistence-jdbc</artifactId>
</dependency>
```

```properties
# Add to application.properties
kogito.persistence.type=jdbc
```

### Issue 5: Database Connection Errors

**Symptom:**
```
Unable to acquire JDBC Connection
```

**Cause:** Datasource misconfigured

**Solution:**
```properties
# Verify datasource configuration
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1

# For PostgreSQL
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/kogito
quarkus.datasource.username=kogito
quarkus.datasource.password=kogito
```

### Issue 5: Transaction Timeout

**Symptom:**
```
Transaction timeout
```

**Cause:** Long-running transactions

**Solution:**
```properties
# Increase transaction timeout
quarkus.transaction-manager.default-transaction-timeout=300s
```
---

## Summary

### What We Migrated

**Configuration:**
- `persistence.xml` → `application.properties`
- JTA datasource → Quarkus datasource
- Hibernate properties → Quarkus Hibernate ORM

**Code:**
- `javax.persistence.*` → `jakarta.persistence.*`
- Entity classes (annotations only)

**Dependencies:**
- `kjar` packaging → `jar` packaging
- KIE Maven plugin → Quarkus Maven plugin
- jBPM persistence → Kogito persistence add-on

### Key Takeaways

1. **Annotation Migration is Simple:** Just replace `javax` with `jakarta`
2. **Configuration is Different:** XML → Properties file
3. **Persistence is Automatic:** Kogito handles process state
4. **Schema Generation Works:** Hibernate creates tables automatically
5. **No Code Changes:** Entity logic remains the same

### Migration Checklist

- [ ] Update Maven dependencies to v9
- [ ] Replace `javax.persistence` with `jakarta.persistence` in all entity classes
- [ ] Create `application.properties` with datasource configuration
- [ ] Configure Hibernate ORM properties
- [ ] Add Kogito persistence add-on dependency
- [ ] Remove v8 XML configuration files
- [ ] Test database schema generation
- [ ] Verify process variable persistence
- [ ] Test process restart and recovery

### Performance Considerations

**v8 vs v9 Persistence Performance:**
- v9 uses optimized JSON/Protobuf marshalling
- Quarkus startup is faster than traditional app servers
- Database connection pooling is more efficient
- Process state queries are optimized

**Best Practices:**
1. Use connection pooling for production
2. Configure appropriate schema generation strategy
3. Monitor database performance
4. Use Protobuf for high-volume scenarios
5. Implement proper indexing on process tables

---

### Additional Resources

### Official Documentation

- [IBM BAMOE v8 to v9 Upgrade Guide](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-from-80x)
- [Quarkus Hibernate ORM Guide](https://quarkus.io/guides/hibernate-orm)
- [Kogito Persistence Documentation](https://docs.kogito.kie.org/latest/html_single/#con-persistence_kogito-developing-process-services)
- [Jakarta Persistence Specification](https://jakarta.ee/specifications/persistence/3.1/)

### Database Configuration Examples

**PostgreSQL:**
```properties
quarkus.datasource.db-kind=postgresql
quarkus.datasource.jdbc.url=jdbc:postgresql://localhost:5432/kogito
quarkus.datasource.username=kogito
quarkus.datasource.password=kogito
quarkus.hibernate-orm.database.generation=update
```

**MySQL:**
```properties
quarkus.datasource.db-kind=mysql
quarkus.datasource.jdbc.url=jdbc:mysql://localhost:3306/kogito
quarkus.datasource.username=kogito
quarkus.datasource.password=kogito
quarkus.hibernate-orm.database.generation=update
```

**Oracle:**
```properties
quarkus.datasource.db-kind=oracle
quarkus.datasource.jdbc.url=jdbc:oracle:thin:@localhost:1521:XE
quarkus.datasource.username=kogito
quarkus.datasource.password=kogito
quarkus.hibernate-orm.database.generation=update
```

### Troubleshooting Commands

```bash
# Check database tables
mvn quarkus:dev
# Then access H2 console at http://localhost:8080/q/dev

# View Hibernate SQL
quarkus.hibernate-orm.log.sql=true

# Enable debug logging
quarkus.log.category."org.hibernate".level=DEBUG
quarkus.log.category."org.kie".level=DEBUG

# Test database connection
curl http://localhost:8080/q/health
```

---
