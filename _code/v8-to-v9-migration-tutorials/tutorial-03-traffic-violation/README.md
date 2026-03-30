# Tutorial 3: Traffic Violation (DMN) Migration

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Common Issues](#common-issues)
5. [Testing](#testing)
6. [Summary](#summary)

---

## 1. Introduction

This tutorial demonstrates how to migrate the **Traffic Violation** DMN (Decision Model and Notation) application from BAMOE v8.0.x to BAMOE v9.x. This scenario is important because it showcases DMN decision table migration, which is a common use case for business rules and decision automation.

**What you'll migrate:**
- DMN decision model with decision tables
- Speed violation rules and fine calculations
- Driver suspension logic based on points and violations

**DMN Model Details:**

The Traffic Violation DMN includes two key decisions:

1. **Fine Decision Table**
   - **Hit Policy:** UNIQUE (only one rule matches per evaluation)
   - **Return Type:** Context with two fields (`Amount` and `Points`)
   - **Input Expression:** Uses minus operation in header: `Violation.Actual Speed - Violation.Speed Limit`
   - **Unary Checks:** Uses comparison operators like `>= 30` for speed threshold checks
   - **Rules:** Evaluates violation type and speed difference to determine fine amount and points

2. **Should the driver be suspended? Context**
   - **Return Type:** String ("Yes" or "No")
   - **Logic:** Uses context with non-empty `result` row
   - **Calculation:** Computes `Total Points = Driver.Points + Fine.Points`
   - **Decision:** Suspends driver if `Total Points >= 20`

**Why this scenario matters:**
- DMN is widely used for business decision automation
- Demonstrates decision table migration patterns
- Shows how to test DMN decisions in v9
- Highlights key differences in DMN handling between v8 and v9

**What you'll learn:**
- Handling DMN filename requirements (no spaces allowed)
- Adding H2 database dependency for development
- Testing DMN decisions using Swagger UI

## 2. Prerequisites

### Required Tools
- **Java 17** or later
- **Maven 3.8.1** or later
- A text editor or IDE

### Required Knowledge
- Basic understanding of DMN
- Basic Maven and command-line operations

### Source Application (v8-app)

The v8 Traffic Violation DMN includes:
- **Decision Name:** `Traffic Violation`
- **Input Data:** Driver information, Violation details
- **Decisions:** Fine calculation, Driver suspension determination
- **Business Knowledge Models:** Speed violation rules

## 3. Step-by-Step Migration

### Step 1: Create Project Using BAMOE Canvas Quarkus Accelerator

Create a new BAMOE v9 project using the Quarkus DMN accelerator. See the [main README - Creating BAMOE v9 Projects with Accelerators](../README.md#creating-bamoe-v9-projects-with-accelerators) section for detailed instructions on both Git clone and Canvas web interface options.

For this tutorial, use the DMN accelerator:
```bash
git clone git@github.com:IBM/bamoe-canvas-quarkus-accelerator.git -b 9.3.1-ibm-0006-quarkus-dmn traffic-violation-v9
cd traffic-violation-v9
```

### Step 2: Add H2 Database Dependency

Open `pom.xml` and add the H2 dependency for development mode.

Locate the dependencies section and add:

```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-jdbc-h2</artifactId>
</dependency>
```

**Why is this needed?**

The `application.properties` is configured to use H2 in dev mode:
```properties
%dev.quarkus.datasource.db-kind=h2
```

### Step 3: Copy and Rename DMN File from v8

**Important:** DMN files in v9 cannot have spaces in their filenames.

Copy the DMN file from v8 and rename it (remove spaces):

```bash
# From the v9-app directory
cp ../v8-app/src/main/resources/org/kie/example/traffic/traffic_violation/Traffic\ Violation.dmn \
   src/main/resources/TrafficViolation.dmn
```

**Filename Requirements:**
- `Traffic Violation.dmn` (has space - will cause issues)
- `TrafficViolation.dmn` (camelCase - recommended)
- `Trafficviolation.dmn` (lowercase - also works)
- `traffic-violation.dmn` (kebab-case - also works)

### Step 4: (Optional) Migrate Test Scenarios

If you want to migrate test scenarios from v8, follow these steps:

**6a. Copy the test scenario file:**

Copy the test scenario file from v8-app to the v9-app directory structure:

```
v9-app/
└── src/
    └── test/
        ├── java/
        │   └── testscenario/
        └── resources/
            └── ViolationScenarios.scesim  ← Copy here
```

**Note:** Remove spaces from the filename when copying to v9 (e.g., `Violation Scenarios.scesim` → `ViolationScenarios.scesim`).

**6b. Update the DMN file path in the test scenario:**

The key difference between v8 and v9 is the DMN file path:

**v8 uses absolute path:**
```xml
<dmnFilePath>src/main/resources/org/kie/example/traffic/traffic_violation/Traffic Violation.dmn</dmnFilePath>
```

**v9 uses relative path:**
```xml
<dmnFilePath>../../main/resources/TrafficViolation.dmn</dmnFilePath>
```

Open [`src/test/resources/ViolationScenarios.scesim`](v9-app/src/test/resources/ViolationScenarios.scesim) **in a text editor** (not the visual editor) and update the `<settings>` section:

```xml
<settings>
  <dmnFilePath>../../main/resources/TrafficViolation.dmn</dmnFilePath>
  <type>DMN</type>
  <dmnNamespace>https://github.com/kiegroup/drools/kie-dmn/_A4BCA8B8-CF08-433F-93B2-A2598F19ECFF</dmnNamespace>
  <dmnName>Traffic Violation</dmnName>
  <skipFromBuild>false</skipFromBuild>
  <stateless>false</stateless>
</settings>
```

**Note:** You need to edit the XML directly in a text editor (not the visual Test Scenario editor) to modify the `<dmnFilePath>` setting. In VS Code, right-click the file and select "Open With..." → "Text Editor".

**Important:**
- `<dmnFilePath>` must be a relative path
- `<dmnName>` must match the DMN file's `name` attribute exactly (with space if present in the DMN)

**6c. Running Test Scenarios:**

Test scenarios can be executed using JUnit. The v9-app includes [`TestScenarioJunitActivatorTest.java`](v9-app/src/test/java/testscenario/TestScenarioJunitActivatorTest.java) which uses the `@TestScenarioActivator` annotation (available since BAMOE 9.2.1):

```java
@TestScenarioActivator
public class TestScenarioJunitActivatorTest {
}
```

Run tests with:
```bash
mvn test
```

**Note about test execution:**
- **BAMOE 9.2.1+:** Uses `@TestScenarioActivator` annotation (shown above)
- **Before 9.2.1:** Used `@RunWith(ScenarioJunitRunner.class)` - this approach is deprecated and will be removed in future versions

For more information on authoring and running test scenarios, see: [BAMOE Test Scenarios Documentation](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=scenarios-authoring-unit-tests-test)

### Step 5: Build the Application

```bash
mvn clean install
```

Expected output:
```
[INFO] BUILD SUCCESS
```

If you are using Gradle, use the following command:

```bash
gradle clean build
```

### Step 6: Run in Development Mode

```bash
mvn quarkus:dev
```

The application will start and display:
```
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
INFO  [io.quarkus] your-bamoe-business-service 1.0.0-SNAPSHOT on JVM started
INFO  [io.quarkus] Profile dev activated. Live Coding activated.
```

If you are using Gradle, use the following command:

```shell script
gradle clean quarkusDev
```

### Step 7: Access Swagger UI

Open your browser: http://localhost:8080/q/swagger-ui/

You should see the `POST /TrafficViolation` endpoint.

## 4. Testing

### Editing DMN in VS Code

You can open and edit the DMN file using BAMOE Developer Tools for VS Code:

1. Open the v9-app project in VS Code
2. Navigate to `src/main/resources/TrafficViolation.dmn`
3. Open the DMN in **"Canvas"** or **"Developer Tools"**
4. The DMN editor will open, allowing you to:
   - View and edit decision tables
   - Modify input/output data types
   - Update business logic
   - Validate the DMN model

For more information about authoring decisions, running decisions, and testing, see the [IBM BAMOE Documentation: Developing Decisions, Rules, and Test Scenarios](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=developing-decisions-rules-test-scenarios).

#### When to Use Canvas vs Developer Tools

**Canvas** - Web-based, collaborative editing with immediate DMN Runner testing

**Developer Tools** - Local VS Code editing with IDE integration and offline support

#### Validation

**Canvas** - Real-time validation while editing

**Developer Tools** - Validation on save and during Maven build

**Build-time** - Full validation during `mvn clean install`. If you are using, Gradle, use `gradle clean build` command.

**Benefits:**
- Visual editing of decision tables
- Real-time validation
- Integrated with your development workflow
- Changes are immediately reflected when you rebuild

### Testing with Swagger UI

1. Navigate to http://localhost:8080/q/swagger-ui/
2. Find the `POST /TrafficViolation` endpoint
3. Click "Try it out"
4. Use this sample request:

```json
{
  "Driver": {
    "Name": "John Doe",
    "Age": 25,
    "State": "CA",
    "City": "Los Angeles",
    "Points": 5
  },
  "Violation": {
    "Code": "SPEED",
    "Date": "2024-01-15",
    "Type": "speed",
    "Speed Limit": 55,
    "Actual Speed": 75
  }
}
```

5. Click "Execute"

### Expected Response

```json
{
  "Violation": {
    "Type": "speed",
    "Speed Limit": 55,
    "Actual Speed": 75,
    "Code": "SPEED",
    "Date": "2024-01-15"
  },
  "Driver": {
    "Points": 5,
    "State": "CA",
    "City": "Los Angeles",
    "Age": 25,
    "Name": "John Doe"
  },
  "Fine": {
    "Points": 3,
    "Amount": 500
  },
  "Should the driver be suspended?": "No"
}
```

Use the data below for additional testing via Swagger UI.

### Test Scenarios

#### Scenario 1: Minor Speeding (Below Fine Threshold)

**Request:**
```json
{
  "Driver": {
    "Name": "Jane Smith",
    "Age": 30,
    "Points": 2
  },
  "Violation": {
    "Type": "speed",
    "Speed Limit": 65,
    "Actual Speed": 72
  }
}
```

**Expected Response:**
```json
{
  "Violation": {
    "Type": "speed",
    "Speed Limit": 65,
    "Actual Speed": 72,
    "Code": null,
    "Date": null
  },
  "Driver": {
    "Points": 2,
    "State": null,
    "City": null,
    "Age": 30,
    "Name": "Jane Smith"
  },
  "Fine": null,
  "Should the driver be suspended?": "No"
}
```

**Explanation:** Speed difference is only 7 mph, which is below the threshold for a fine. No fine is assessed, and the driver is not suspended.

#### Scenario 2: Major Speeding (Above Fine Threshold)

**Request:**
```json
{
  "Driver": {
    "Name": "Jane Johnson",
    "Age": 22,
    "Points": 15
  },
  "Violation": {
    "Type": "speed",
    "Speed Limit": 55,
    "Actual Speed": 95
  }
}
```

**Expected Response:**
```json
{
  "Violation": {
    "Type": "speed",
    "Speed Limit": 55,
    "Actual Speed": 95,
    "Code": null,
    "Date": null
  },
  "Driver": {
    "Points": 15,
    "State": null,
    "City": null,
    "Age": 22,
    "Name": "Jane Johnson"
  },
  "Fine": {
    "Points": 7,
    "Amount": 1000
  },
  "Should the driver be suspended?": "Yes"
}
```

**Explanation:** Speed difference is 40 mph over the limit, resulting in a large fine (1000) and 7 points. Combined with the driver's existing 15 points (15 + 7 = 22), the total exceeds 20 points, triggering a suspension recommendation.

## 5. Common Issues

### Issue 1: "Unable to find JDBC driver for h2"

**Cause:** H2 dependency missing from [`pom.xml`](v9-app/pom.xml)

**Solution:** Add the H2 dependency:
```xml
<dependency>
  <groupId>io.quarkus</groupId>
  <artifactId>quarkus-jdbc-h2</artifactId>
</dependency>
```

### Issue 2: DMN file not recognized

**Cause:** Filename contains spaces

**Solution:** Rename the file to remove spaces:
```bash
mv "Traffic Violation.dmn" TrafficViolation.dmn
```

### Issue 3: Port 8080 already in use

**Cause:** Another application is using port 8080

**Solution:** Kill the process or change the port in [`application.properties`](v9-app/src/main/resources/application.properties):
```properties
quarkus.http.port=8081
```

### Issue 4: Build fails

**Cause:** Maven cannot download dependencies

**Solution:**
1. Check internet connection
2. Clear Maven cache: `rm -rf ~/.m2/repository`
3. Rebuild: `mvn clean install -U`. If you are using, Gradle, use `gradle clean build` command.

## 6. Recap

### What You Accomplished

1. Created a BAMOE v9 project using the Canvas Quarkus accelerator (via Git clone or Canvas web interface)
2. Added H2 database dependency to [`pom.xml`](v9-app/pom.xml)
3. Copied DMN file from v8 to v9 (removed spaces from filename)
4. Built the application with `mvn clean install`. If you are using, Gradle, use `gradle clean build` command.
5. Ran the application with `mvn quarkus:dev`. If you are using, Gradle, use `gradle clean quarkusDev` command.
6. Tested the DMN decision using Swagger UI

### Key Differences: v8 vs v9

| Aspect | v8 | v9 |
|--------|----|----|
| **Runtime or Platform** | JBoss EAP | Quarkus |
| **Packaging** | kjar | jar |
| **DMN Filename** | Spaces allowed | No spaces allowed |
| **Testing** | Business Central | Swagger UI |
| **Project Setup** | Manual configuration | Accelerator-based (Git or Canvas) |

### Migration Steps Summary

The migration is straightforward:
1. Clone the BAMOE Canvas Quarkus accelerator (or use Canvas web interface)
2. Add H2 dependency to pom.xml
3. Copy DMN file and remove spaces from filename
4. Build and run

### Next Steps

- Customize the project in [`pom.xml`](v9-app/pom.xml)
- Add more DMN decision tables
- Deploy to OpenShift/Kubernetes
- Migrate Test Scenario (scesim) files

#### Migrating Test Scenario Files

The v8 project includes a Test Scenario file ([`Violation Scenarios.scesim`](v8-app/src/test/resources/org/kie/example/traffic/traffic_violation/Violation%20Scenarios.scesim)) that was not migrated in this tutorial. You have several options for handling scesim files:

**Option 1: Create Test Scenario from scratch**
- Use VS Code BAMOE Developer Tools to create a new Test Scenario file
- Manually recreate the test cases based on the v8 scesim file
- This ensures compatibility with BAMOE v9 Test Scenario format

**Option 2: Reference BAMOE documentation**
- Consult the [BAMOE Test Scenario documentation](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=scenarios-authoring-unit-tests-test) for guidance on creating and migrating Test Scenarios
- Follow best practices for DMN test scenario design in v9

**Option 3: Copy and adjust the scesim file**
- Copy the scesim file to the appropriate location in the v9 project: `src/test/resources/`
- The file may require adjustments to work with BAMOE v9:
  - Update namespace references if needed
  - Verify DMN model references match the migrated DMN file
  - Test and fix any compatibility issues
- Add the Test Scenario JUnit activator class to `src/test/java/`:
  ```java
  package testscenario;
  
  @org.junit.runner.RunWith(org.drools.scenariosimulation.backend.runner.ScenarioJunitActivator.class)
  public class ScenarioJunitActivatorTest {
  }
  ```

**Recommendation:** For production migrations, Option 1 (creating from scratch) is recommended to ensure full compatibility with BAMOE v9, while Option 3 (copy and adjust) can be faster for simple scenarios if the file structure is compatible.

### Additional Resources

#### IBM BAMOE Documentation
- [BAMOE v8 to v9 Upgrade Guide](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-from-80x)
- [Authoring Unit Tests with Test Scenarios](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=scenarios-authoring-unit-tests-test)
- [Developing Decisions, Rules, and Test Scenarios](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=developing-decisions-rules-test-scenarios)

#### GitHub Resources
- [BAMOE Canvas Quarkus Accelerator](https://github.com/IBM/bamoe-canvas-quarkus-accelerator)

