# Tutorial 01: Migrating the Evaluation Process from BAMOE v8 to v9

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

---

## Introduction

This tutorial demonstrates how to migrate the **Evaluation Process** application from BAMOE v8.0.x to BAMOE v9.3.x using the official BAMOE Accelerators repository.

### What You'll Learn

- How to use the bamoe-accelerators repository to create v9 projects
- Converting from kjar packaging to Quarkus-based projects
- Updating BPMN process definitions for v9 compatibility
- Fixing runtime issues (package naming)
- Testing the migrated application

## Prerequisites

### Required Software

1. **Java 17 or later** (CRITICAL)
   ```bash
   java -version  
   ```

2. **Maven 3.8.1 or later**
   ```bash
   mvn -version
   ```

3. **Git** (for cloning bamoe-accelerators)

4. **IDE** (VS Code recommended with BAMOE developer tools extension)


---

### v8 Application Structure

The v8 Evaluation Process includes:

**Process Details:**
- **Process ID:** `evaluation`
- **Process Variables:** `employee`, `reason`, `performance`, `initiator`
- **Human Tasks:** Self Evaluation, PM Evaluation, HR Evaluation
- **Gateway:** Parallel gateway for concurrent PM and HR evaluations

**File Structure:**
```
v8-app/
├── pom.xml (packaging: kjar)
├── src/main/resources/
│   ├── evaluation.bpmn
│   ├── evaluation-taskform.frm
│   ├── PerformanceEvaluation-taskform.frm
│   ├── evaluation-svg.svg (process diagram)
│   └── META-INF/
│       ├── kmodule.xml
│       ├── persistence.xml
│       └── kie-deployment-descriptor.xml
```

**v9 Application Structure:**
```
v9-app/
├── pom.xml (packaging: jar, Quarkus)
├── src/main/resources/
│   ├── evaluation.bpmn
│   ├── application.properties
│   ├── custom-forms-dev/ (generated forms)
│   │   ├── evaluation.tsx
│   │   ├── evaluation.html
│   │   └── evaluation_PerformanceEvaluation.tsx
│   └── META-INF/
│       └── processSVG/
│           └── evaluation.svg (generated from BPMN)
└── src/test/java/
    └── testscenario/
        └── TestScenarioJunitActivatorTest.java
```

### Key Architecture Changes: v8 vs v9

| Aspect | v8 (RHPAM/jBPM) | v9 (BAMOE/Kogito) |
|--------|-----------------|-------------------|
| **Runtime or Platform** | JBoss EAP | Quarkus |
| **Packaging** | kjar | jar |
| **Build Plugin** | kie-maven-plugin | quarkus-maven-plugin |
| **Process Package** | Path-based (e.g., `Evaluation.src.main.resources`) | Java convention (e.g., `com.example.evaluation`) |
| **Persistence** | JPA with kmodule.xml | Quarkus datasource config |
| **Configuration** | XML files (kmodule, persistence, deployment) | application.properties |
| **Dev Tools** | Business Central | Quarkus Dev UI + VS Code |
| **API Style** | KIE Server REST API | Quarkus REST endpoints |

---

## Step-by-Step Migration

### Step 1: Create Project Using BAMOE Canvas Quarkus Accelerator

Create a new BAMOE v9 project using the Quarkus Full accelerator (supports BPMN, DMN, and DRL). See the [main README - Creating BAMOE v9 Projects with Accelerators](../../v8-to-v9-migration-tutorials/README.md#creating-bamoe-v9-projects-with-accelerators) section for detailed instructions on both Git clone and Canvas web interface options.

For this tutorial, use the Quarkus Full accelerator for BPMN processes:
```bash
git clone git@github.com:IBM/bamoe-canvas-quarkus-accelerator.git -b 9.3.1-ibm-0006-quarkus-full evaluation-process-v9
cd evaluation-process-v9
```

### Step 2: Add Required Dependencies
#### 2.1: Add H2 Database Dependency

The accelerator template includes PostgreSQL by default. For development mode with H2 in-memory database, add the H2 dependency to `pom.xml`:

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-jdbc-h2</artifactId>
</dependency>
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-agroal</artifactId>
</dependency>
```

#### 2.2: Add BAMOE Dev UI Dependency

**Required for Dev UI Task Action Buttons**

To enable Complete, Release, and Skip buttons in the Dev UI, add the BAMOE Dev UI dependency.

Open `pom.xml` and locate the Testing section (around line 198):

```xml
<!-- Testing -->
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-junit5</artifactId>
  <scope>test</scope>
</dependency>
```

Add the BAMOE Dev UI dependency right before the Testing section:

```xml
<!-- BAMOE Dev UI -->
<dependency>
  <groupId>com.ibm.bamoe</groupId>
  <artifactId>bamoe-quarkus-devui</artifactId>
</dependency>
```

This dependency enables the Dev UI task management interface. See [IBM's Workflow Services documentation](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=workflows-workflow-services-quarkus) for more details.

### Step 3: Copy BPMN Process from v8

Copy the BPMN file from your v8 application:

```bash
cp ../v8-app/src/main/resources/evaluation.bpmn src/main/resources/
```

**Note:** The accelerator template already has the correct directory structure. You don't need to create any directories.

### Step 4: Update BPMN Package Name (Best Practice)

The v8 BPMN file uses a file-path style package name. While this works in v9, it's recommended to update it to follow Java package naming conventions.

**Optional but Recommended:** Edit `src/main/resources/evaluation.bpmn` and update the package name (around line 29):

**Before (v8 style - still works in v9):**
```xml
<bpmn2:process id="evaluation" drools:packageName="Evaluation.src.main.resources" drools:version="1" name="Evaluation" isExecutable="true">
```

**After (v9 best practice):**
```xml
<bpmn2:process id="evaluation" drools:packageName="com.example.evaluation" drools:version="1.0" name="Evaluation" isExecutable="true">
```

**Why Update?**
- Follows Java package naming conventions
- Better alignment with v9 architecture
- Improved code organization and maintainability
- **Note:** Both formats work correctly in v9


### Step 5: Configure application.properties

**Note:** The BAMOE accelerator template's `application.properties` is minimal and doesn't include stateful workflow configurations. We need to add properties based on IBM's stateful workflow documentation.

Edit `src/main/resources/application.properties` and add the following configuration:

```properties
# Kogito Configuration
kogito.service.url=http://${quarkus.http.host}:${quarkus.http.port}
kogito.jobs-service.url=http://${quarkus.http.host}:${quarkus.http.port}
kogito.data-index.url=http://${quarkus.http.host}:${quarkus.http.port}

# Persistence Configuration
kogito.persistence.type=jdbc

# Development Mode - H2 Database
%dev.kie.flyway.enabled=true
%dev.quarkus.datasource.db-kind=h2
%dev.quarkus.datasource.username=kogito
%dev.quarkus.datasource.jdbc.url=jdbc:h2:mem:default;NON_KEYWORDS=VALUE,KEY

# Production Mode - PostgreSQL
%prod.quarkus.datasource.db-kind=postgresql
%prod.quarkus.datasource.username=${POSTGRES_USER}
%prod.quarkus.datasource.password=${POSTGRES_PASSWORD}
%prod.quarkus.datasource.jdbc.url=jdbc:postgresql://${POSTGRES_HOST}:5432/${POSTGRES_DB}
```

**Reference:** [IBM Stateful Workflows Documentation](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=developing-stateful-workflows)

**Why is this needed?**
- Enables JDBC persistence for process state
- Configures embedded services (Data Index, Jobs Service)
- Sets up H2 for development and PostgreSQL for production
- Enables Flyway database migrations

### Step 6: Configure User Groups in application.properties

**Required for Dev UI Task Management**

The BAMOE Dev UI requires user group configuration to display and manage tasks. **CRITICAL:** The groups configured here must match the group assignments in your BPMN file.

**Understanding the Connection:**

In the Evaluation process BPMN file, tasks are assigned to groups:
- **PM Evaluation** task → Assigned to `PM` group (line 174 in evaluation.bpmn)
- **HR Evaluation** task → Assigned to `HR` group (line 242 in evaluation.bpmn)

Users must be configured with these same group names to see and work on the tasks.

Edit `src/main/resources/application.properties` and add:

```properties
# User Groups Configuration for Dev UI
# Pattern: %dev.bamoe.devui.users.<username>.groups=<comma separated list of groups>
# You can define multiple users to simulate roles in Dev UI by listing their groups as comma-separated values.

%dev.bamoe.devui.users.jdoe.groups=PM,HR
```

**Why is this needed?**
- Enables task visibility in Dev UI
- Allows task assignment based on user groups
- Required for Complete, Release, and Skip buttons to work
- **Groups must match BPMN GroupId assignments** (PM, HR in this example)
- Without this, tasks won't be visible in Dev UI

**To add more users:**
```properties
%dev.bamoe.devui.users.jdoe.groups=jdoe,PM,HR        # Can work on both PM and HR tasks
%dev.bamoe.devui.users.manager.groups=manager,PM     # Can only work on PM tasks
%dev.bamoe.devui.users.hradmin.groups=hradmin,HR     # Can only work on HR tasks
```

**Key Rule:** If your BPMN uses `GroupId: admin`, you MUST configure a user with `groups=...,admin,...` in application.properties.

### Step 7: Build the Application

```bash
mvn clean install
```

If you are using Gradle, use the following command:

```bash
gradle clean build
```

Expected output:
```
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  40-45 seconds
[INFO] ------------------------------------------------------------------------
```

### Step 8: Generate SVG from BPMN

**Generate Process Diagram for Dev UI**

BAMOE can automatically generate SVG diagrams from your BPMN files for visualization in the Dev UI. This is useful for seeing the process flow and current state during execution.

**Using BAMOE Developer Tools:**

1. Open your BPMN file in VS Code (e.g., `evaluation.bpmn`)
2. Open Command Palette: `Cmd+Shift+P` (Mac) or `Ctrl+Shift+P` (Windows/Linux)
3. Type: `>svg` and select **"BAMOE Developer Tools: Generate BPMN Editor preview SVG"**
4. The SVG will be automatically generated

**Generated Location:**
```
src/main/resources/META-INF/processSVG/evaluation.svg
```

**What This Provides:**
- Visual process diagram in Dev UI
- Shows current process state during execution
- Highlights active nodes and completed paths
- Helps with debugging and monitoring

**Alternative: Automatic Generation**

SVG files are also automatically generated during the build process when you run `mvn clean install`. The generated SVG will be placed in the same location.

If you are using Gradle, use the following command:

```bash
gradle clean build
```

**Note:** SVG generation is optional but highly recommended for better process visualization in Dev UI.

### Step 9: Generate Forms

**Generate Form Code**

Use BAMOE Developer Tools to generate TypeScript React forms:

1. Open Command Palette: `Cmd+Shift+P` (Mac) or `Ctrl+Shift+P` (Windows/Linux)
2. Type: "BAMOE Developer Tools: Generate form code for User Tasks"
3. Select your project directory
4. Choose UI framework: **PatternFly** or **Bootstrap 4**
5. Select: **All tasks** / **Specific tasks**

Generated files will be in `src/main/resources/custom-forms-dev/`:
```
custom-forms-dev/
├── evaluation.tsx
├── evaluation.html
├── evaluation.config
├── evaluation_PerformanceEvaluation.tsx
├── evaluation_PerformanceEvaluation.html
└── evaluation_PerformanceEvaluation.config
```

**Note:** Form generation is optional. The Dev UI provides full task management capabilities without custom forms.

### Step 10: Run in Development Mode

```bash
mvn clean install
```

```bash
mvn quarkus:dev -Pdevelopment
```

The application will start and display:
```
Listening for transport dt_socket at address: 5005
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
INFO  [io.quarkus] (Quarkus Main Thread) your-bamoe-business-service 1.0.0-SNAPSHOT on JVM
```

If you are using Gradle, use the following commands:

```bash
gradle clean build
```

```shell script
gradle clean quarkusDev
```

### Step 11: Access the Application

Open your browser and navigate to:

- **Dev UI:** http://localhost:8080/q/dev-ui
- **Swagger UI:** http://localhost:8080/q/swagger-ui
- **Health Check:** http://localhost:8080/q/health

---

## Testing

### Test 1: Start Process Instance via Swagger UI

1. Navigate to http://localhost:8080/q/swagger-ui
2. Find the `POST /evaluation` endpoint
3. Click "Try it out"
4. Use this sample request body:

```json
{
  "employee": "John Doe",
  "reason": "Annual Performance Review",
  "performance": 0,
  "initiator": "manager@company.com"
}
```

5. Click "Execute"

**Expected Response:**
```json
{
  "id": "ae184d19-82aa-49ca-9c46-638565a3d5c6",
  "reason": "Annual Performance Review",
  "performance": 0,
  "initiator": "manager@company.com",
  "employee": "John Doe"
}
```

The response includes the generated process instance ID and echoes back the input data.

---

### Test 2: Start Process Instance via curl

```bash
curl -X POST http://localhost:8080/evaluation \
  -H "Content-Type: application/json" \
  -d '{
    "employee": "John Doe",
    "reason": "Annual Performance Review",
    "performance": 0,
    "initiator": "manager@company.com"
  }'
```

**Expected Response:**
```json
{
  "id": "0ea9c172-b3ca-4114-a223-5f6d1cd24329",
  "reason": "Annual Performance Review",
  "performance": 0,
  "initiator": "manager@company.com",
  "employee": "John Doe"
}
```

The response includes a unique process instance ID (UUID format) along with the process variables.

---

### Test 3: Query All Process Instances

```bash
curl http://localhost:8080/evaluation
```

**Expected Response:**
```json
[
  {
    "id": "0ea9c172-b3ca-4114-a223-5f6d1cd24329",
    "reason": "Annual Performance Review",
    "performance": 0,
    "initiator": "manager@company.com",
    "employee": "John Doe"
  },
  {
    "id": "ae184d19-82aa-49ca-9c46-638565a3d5c6",
    "reason": "Annual Performance Review",
    "performance": 0,
    "initiator": "manager@company.com",
    "employee": "John Doe"
  }
]
```

Returns an array of all active process instances with their current state and variables.

---

### Test 4: Get Specific Process Instance

```bash
curl http://localhost:8080/evaluation/{processInstanceId}
```

**Expected Result:** Details of the specific process instance

---

### Test 5: Use Dev UI

1. Navigate to http://localhost:8080/q/dev-ui
2. Click on "Process Instances" in the left menu
3. You should see your running process instance
4. Click on the instance to view details and complete tasks

**Expected Result:** Visual interface showing process instances and tasks

---

### Test 6: Complete User Tasks

Via Dev UI:
1. Navigate to Process Instances
2. Click on a running instance
3. View and complete available tasks

Via REST API:
```bash
# Get tasks for a process instance
curl http://localhost:8080/evaluation/{processInstanceId}/tasks

# Complete a task
curl -X POST http://localhost:8080/evaluation/{processInstanceId}/tasks/{taskId} \
  -H "Content-Type: application/json" \
  -d '{
    "performance": 85
  }'
```

---

---

---

### Issue: Java Version Error

**Symptom:**
```
UnsupportedClassVersionError: class file version 55.0
```

**Solution:** Install Java 17+ (see Prerequisites section)

---

### Issue: "Unable to find JDBC driver for h2"

**Cause:** The H2 dependency is missing from pom.xml

**Solution:** Add the `quarkus-jdbc-h2` dependency as shown in Step 4

---

### Issue: Build fails with dependency errors

**Cause:** Maven cache issues or network problems

**Solution:**
```bash
# Clean Maven cache and rebuild
mvn clean install -U
```

If you are using Gradle, use the following command:

```bash
gradle clean build
```

---

### Issue: Task Shows "Reserved" Status - No Action Buttons

**Symptom:**
- Task appears in Dev UI with "Reserved" status
- No Complete/Release/Skip buttons visible
- Task is assigned to specific user (e.g., from `employee` variable)

**Root Cause:**
According to the [WS-HumanTask standard](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=workflows-stateful-compact-architecture) section 4.10 "Human Task Behavior and State Transitions", tasks in "Reserved" state don't show action buttons in Dev UI. The task went directly to "Reserved" because it has a `potentialOwner` assigned to a specific user (`#{employee}`).

**Why Reserved Tasks Don't Show Buttons in Dev UI:**
- Reserved tasks are already assigned to a specific user
- The WS-HumanTask standard defines Reserved as a "claimed" state
- Dev UI is designed for group task management (Ready state)
- Reserved tasks must be managed via REST API or by the assigned user

**Solution Options:**

**Option A: Use Group Assignment (Recommended)**
Change task assignment from specific user to group:

1. Open `evaluation.bpmn` in BAMOE Canvas
2. Select the user task (e.g., "Self Evaluation")
3. In Properties panel, find "Assignments"
4. Change from:
   ```
   potentialOwner: #{employee}
   ```
   To:
   ```
   Groups: PM,HR
   ```

5. **IMPORTANT:** Update `application.properties` to include users in these groups:
   ```properties
   %dev.bamoe.devui.users.jdoe.groups=jdoe,PM,HR
   %dev.bamoe.devui.users.manager.groups=manager,PM
   %dev.bamoe.devui.users.hradmin.groups=hradmin,HR
   ```

**Note:** The groups in BPMN (`PM`, `HR`) MUST match the groups configured for users in application.properties. This keeps tasks in "Ready" state, showing action buttons for all group members.

**Verification:**
1. Rebuild and restart application
2. Create new process instance with group assignment
3. Task should appear in "Ready" state
4. Complete/Release/Skip buttons should be visible

**Reference:**
- IBM Documentation: [Human Task Behavior and State Transitions](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=workflows-stateful-compact-architecture)
- State diagram shows Reserved state doesn't support Dev UI buttons

---

## Common Issues

1. **BAMOE Dev UI Dependency** - Required for Dev UI task action buttons (Complete, Release, Skip). Add `bamoe-quarkus-devui` dependency to `pom.xml`. See **Step 2.2** for details.

2. **User Groups Configuration** - Required for task assignment and visibility in Dev UI. Configure user groups in `application.properties` with pattern: `%dev.bamoe.devui.users.<username>.groups=<comma separated list of groups>`. See **Step 6** for details.

**Optional Best Practice:**
- **Package Name** - Update BPMN package name from `Evaluation.src.main.resources` to `com.example.evaluation` (Java convention). See **Step 4** for details.


---


## Summary

### Migration Completeness

All migration steps documented with step-by-step instructions

### Key Achievements

- Application migrated from v8 kjar to v9 Quarkus
- Runtime tested and verified working
- Alternative approaches provided for forms and SVG
- Comprehensive troubleshooting guide included

### Files Migrated

- `evaluation.bpmn` - Copied with package name fix
- `application.properties` - Configured for stateful workflows
- `*.frm` files - Use form generation or Dev UI
- `*.svg` files - Auto-generated from BPMN
- XML configs - Replaced by application.properties





