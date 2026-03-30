# Tutorial 02: Migrating the Mortgage Application from BAMOE v8 to v9

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

---

## Introduction

This tutorial demonstrates how to migrate the **Mortgage Process Application** from BAMOE v8.0.x to BAMOE v9.3.x. This is a complex stateful workflow that combines BPMN processes with DRL business rules.

### What You'll Learn

- Two approaches for creating v9 projects (clone accelerator OR use Canvas + apply accelerator)
- How to migrate complex stateful workflows (BPMN + Rules)
- Converting legacy DRL rules to v9 format (removing insert/retract calls)
- Using backward-compatible ruleflow-group approach (no rule units needed)
- Converting GDST (Guided Decision Tables) to DRL
- Understanding process ID naming conventions
- Handling business rule task integration with ruleflow-group
- Testing stateful workflows in v9

**Note:** This tutorial uses the `ruleflow-group` approach which is **fully supported in BAMOE v9** and provides the easiest migration path from v8 with minimal code changes.

### Two Ways to Create Your v9 Project

You can create your BAMOE v9 project using either:
1. **Git clone** - Clone the accelerator repository directly with the specific branch
2. **BAMOE Canvas** - Create assets in Canvas, download, and apply the accelerator

For this tutorial, use the Full accelerator (supports BPMN + DMN + DRL):

```bash
git clone git@github.com:IBM/bamoe-canvas-quarkus-accelerator.git -b 9.3.1-ibm-0006-quarkus-full mortgage-application-v9
cd mortgage-application-v9
```

**For detailed instructions on both approaches**, see the [main README - Migration Approaches](../README.md) section.

### What Makes This Tutorial Important

This tutorial covers the most complex migration scenario:
- **Stateful Workflows**: BPMN processes that maintain state across multiple business rule evaluations
- **BPMN + DRL Integration**: Business rule tasks embedded within process flows
- **Backward-Compatible Migration**: Using ruleflow-group approach (no rule units needed)
- **Complex Data Models**: Multiple interconnected Java classes
- **Guided Decision Tables**: Converting GDST to DRL (GDST files are not supported in v9)

### v8 Application Structure

The v8 Mortgage Application includes:

**Project Information:**
- **Group ID:** `mortgage-process`
- **Artifact ID:** `mortgage-process`
- **Packaging:** `kjar` (KIE JAR)

**Process Details:**
- **Process ID:** `Mortgage_Process.MortgageApprovalProcess` (Dots and hyphens are supported in v9)
- **Process Variables:** `application`, `inlimit`, `incdownpayment`
- **Human Tasks:** Qualify, Final Approval, Correct Data, Increase Down Payment
- **Business Rule Tasks:** Validation, Retract Validation, Mortgage Calculation

**Assets:**
- 1 BPMN process with 4 human tasks and 3 business rule tasks
- 4 Java data model classes (Applicant, Application, Property, ValidationErrorDO)
- 2 DRL rule files (ValidateDownPayment, RetractValidationErr)
- 1 Guided Decision Table (MortgageDecisionTable.gdst) - **not supported in v9, must convert**
- 5 Legacy rule files (RDRL format - not supported in v9)
- 8 Form files (.frm - not supported in v9)

**Directory Structure:**
```
v8-app/
├── pom.xml (kjar packaging)
├── src/main/java/com/myspace/mortgage_app/
│   ├── Applicant.java
│   ├── Application.java
│   ├── Property.java
│   └── ValidationErrorDO.java
└── src/main/resources/com/myspace/mortgage_app/
    ├── MortgageApprovalProcess.bpmn
    ├── MortgageDecisionTable.gdst
    ├── Validate Down Payment.rdrl
    ├── RetractValidationErr.rdrl
    └── *.frm (form files)
```

### v9 Application Structure

The v9 Mortgage Application includes:

**Project Information:**
- **Group ID:** `org.acme`
- **Artifact ID:** `your-bamoe-business-service`
- **Version:** `1.0.0-SNAPSHOT`
- **Packaging:** `jar` (Quarkus application)
- **Java Version:** 17
- **BAMOE Version:** 9.3.1-ibm-0006
- **Quarkus Version:** 3.20.3

**Process Details:**
- **Process ID:** `MortgageApprovalProcess` 
- **Process Variables:** Same as v8
- **Human Tasks:** Same as v8 (forms auto-generated as TSX)
- **Business Rule Tasks:** Use ruleflow-group (backward-compatible approach)

**Assets:**
- 1 BPMN process 
- 4 Java classes (data models only, no rule unit class)
- 3 DRL files (using ruleflow-group, including converted GDST)
- 5 TSX form files (auto-generated)

**Directory Structure:**
```
v9-app/
├── pom.xml (Quarkus)
├── src/main/java/com/myspace/mortgage_app/
│   ├── Applicant.java
│   ├── Application.java
│   ├── Property.java
│   └── ValidationErrorDO.java
├── src/main/resources/
│   ├── application.properties
│   ├── com/myspace/mortgage_app/
│   │   ├── MortgageApprovalProcess.bpmn
│   │   ├── MortgageDecisionTable.drl  ← Converted from GDST
│   │   ├── ValidateDownPayment.drl
│   │   └── RetractValidationErr.drl
│   └── custom-forms-dev/ (auto-generated TSX forms)
└── src/test/java/testscenario/
    └── TestScenarioJunitActivatorTest.java
```

The `MortgageDecisionTable.gdst` file has been successfully converted to `MortgageDecisionTable.drl` using the ruleflow-group approach. The DRL file contains the same business logic as the original GDST with two rules:
1. **Mortgage Calculation - Urban High Income**: For applicants with income 100k-200k and urban properties
2. **Mortgage Calculation - Rural Low Income**: For applicants with income 50k-99,999 and rural properties

### What Gets Migrated

**Supported in v9 (present in this Mortgage project):**
- BPMN processes (with updates)
- Java data models (with annotation changes)
- DRL rules (using ruleflow-group, no rule units)
- Human tasks (forms auto-generated)

**Not Supported in v9 (present in this Mortgage project):**
- `.gdst` files (Guided Decision Tables - convert to DRL)
- `.rdrl` files (legacy rule format - convert to DRL)
- `.frm` files (replaced by TSX forms)
- `kmodule.xml` (replaced by application.properties)

---

## Prerequisites

### Required Software

1. **Java 17 or later** 
   ```bash
   java -version  
   ```

2. **Maven 3.8.1 or later**
   ```bash
   mvn -version
   ```

3. **Git** (for cloning bamoe-accelerators)

4. **IDE** (VS Code recommended with BAMOE developer tools extension)

### Required Knowledge

- Basic understanding of BPMN 2.0
- Familiarity with Drools rules (DRL)
- Java programming basics
- Maven project structure

---

## Step-by-Step Migration

### Step 1: Create Project Using BAMOE Canvas Quarkus Accelerator

Create a new BAMOE v9 project using the Quarkus Full accelerator. See the [main README - Migration Approaches](../README.md#migration-approaches) section for detailed instructions on both Git clone and Canvas web interface options.

For this tutorial, use the Full accelerator (BPMN + DMN + DRL):
```bash
git clone git@github.com:IBM/bamoe-canvas-quarkus-accelerator.git -b 9.3.1-ibm-0006-quarkus-full mortgage-application-v9
cd mortgage-application-v9
```

### Step 2: Migrate Java Data Models

Copy the Java classes from v8 to v9, removing unsupported annotations.

**Files to migrate:**
```
v8-app/src/main/java/com/myspace/mortgage_app/
  ├── Applicant.java          → v9-app/src/main/java/com/myspace/mortgage_app/
  ├── Application.java        → v9-app/src/main/java/com/myspace/mortgage_app/
  ├── Property.java           → v9-app/src/main/java/com/myspace/mortgage_app/
  └── ValidationErrorDO.java  → v9-app/src/main/java/com/myspace/mortgage_app/
```

**Required Change:**
```diff
- @org.kie.api.definition.type.Label("Application")
  public class Application implements java.io.Serializable {
-     @org.kie.api.definition.type.Label("Applicant")
      private com.myspace.mortgage_app.Applicant applicant;
      // ... rest of class unchanged
  }
```

### Step 3: Convert DRL Rules (Remove insert/retract, Keep ruleflow-group)

#### File 1: ValidateDownPayment.drl

**Required Changes:**
```diff
  package com.myspace.mortgage_app;
  
+ import com.myspace.mortgage_app.Application;
+ import com.myspace.mortgage_app.ValidationErrorDO;
+
  rule "Validate Down Payment"
      ruleflow-group "validation"
      when
-         app : Application( downpayment == 0 || downpayment >= app.property.saleprice )
+         $app: Application( downpayment == 0 || downpayment >= property.saleprice )
      then
-         ValidationErrorDO fact0 = new ValidationErrorDO();
-         fact0.setError( "Down payment cannot be 0, greater than, or equal to the property sale price." );
-         insert( fact0 );
-         System.out.println("Executed Rule: " + drools.getRule().getName() );
-         app.setErrors( fact0 );
+         ValidationErrorDO error = new ValidationErrorDO();
+         error.setError("Down payment cannot be 0, greater than, or equal to the property sale price.");
+         $app.setErrors(error);
  end
```

**Key Changes:**
1. **Removed `insert(error)`** - No longer effective in v9 (no shared KIE session)
2. **Kept `ruleflow-group`** - Still supported for backward compatibility
3. **Simplified action** - Set error directly on application object

#### File 2: RetractValidationErr.drl

**Required Changes:**
```diff
  package com.myspace.mortgage_app;
  
  rule "RetractValidationErr"
-     dialect "mvel"
      ruleflow-group "error"
      when
-         vdo : ValidationErrorDO( )
+         // No conditions - this rule does nothing in v9
      then
-         retract( vdo );
+         // No actions needed (no shared KIE session in v9)
  end
```

**Why:** In v9, there's no shared KIE session between business rule tasks, so `retract()` has no effect. Keep the rule as a no-op to maintain BPMN compatibility.

### Step 4: Convert GDST to DRL

Since GDST (Guided Decision Table) files are not supported in BAMOE v9, you must convert `MortgageDecisionTable.gdst` to DRL format.

**Recommended Conversion Approach:**

The easiest way to convert GDST to DRL is to copy the generated rules from the v8 editor's Source tab:

1. **Open the GDST file in BAMOE v8 Business Central**
   - Navigate to the GDST file in your v8 project
   - Click on the "Source" tab to view the auto-generated DRL code
   - Copy the entire DRL content from the Source tab

2. **Create the DRL file in v9**
   - Create a new file: `v9-app/src/main/resources/com/myspace/mortgage_app/MortgageDecisionTable.drl`
   - Paste the copied DRL content
   - Verify the package declaration matches: `package com.myspace.mortgage_app;`
   - Ensure all necessary imports are included

3. **Update for v9 compatibility**
   - Keep the `ruleflow-group` attribute (must match the BPMN business rule task)
   - Remove any `insert()` or `retract()` calls if present
   - Verify rule names are descriptive and unique
   - Test thoroughly to ensure rules produce the same results

**Example Conversion:**

GDST Row:
- Condition: Annual Income > 100000 AND <= 200000
- Condition: Property Locale = "Urban"
- Action: Set Mortgage Amount = 200000

Becomes DRL Rule:
```drl
rule "Mortgage Calculation - Urban High Income"
    ruleflow-group "mortgagecalculation"
    when
        application: Application(
            applicant.annualincome > 100000,
            applicant.annualincome <= 200000,
            property.locale == "Urban"
        )
    then
        application.setMortgageamount(200000);
end
```

**Key Points:**
- Each GDST row becomes a separate rule with a unique name
- The `ruleflow-group` must match the value in your BPMN business rule task
- Use proper Java getter syntax (e.g., `applicant.annualincome` not `applicant.getAnnualincome()`)


See [`MortgageDecisionTable.drl`](v9-app/src/main/resources/com/myspace/mortgage_app/MortgageDecisionTable.drl) for the complete implementation used in this tutorial.

### Step 5: Understanding Rule File Migration

#### Supported in v9:
- ✅ **DRL files** (.drl) - With ruleflow-group (backward compatible)
- ✅ **Excel Decision Tables** (.xlsx) - Supported as-is
- ✅ **DMN files** (.dmn) - Supported as-is

#### NOT Supported in v9 (Must Convert):
- ❌ **GDST** (.gdst), **RDRL** (.rdrl), **RDSLR** (.rdslr), **DSL** (.dsl) → Convert to DRL

**Quick Conversion Guide:**

For RDRL/GDST/DSL files:
1. Rename to `.drl`
2. Keep `ruleflow-group` attribute
3. Remove `insert()` and `retract()` calls
4. Pattern syntax remains the same

**Example:**
```diff
  rule "Underage"
      ruleflow-group "validation"
      when
          $app: Application( applicant.age < 18 )
      then
          ValidationErrorDO error = new ValidationErrorDO();
          error.setCause("Applicant must be 18 years or older");
          $app.setErrors(error);
-         insert(error);
  end
```

**References:**
- [IBM BAMOE - Upgrading Individual Assets](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=assets-guided)

### Step 6: BPMN Integration with Rule Units

**Important:** This tutorial uses `drools:ruleFlowGroup` in BPMN

**v8 version:**
```xml
<bpmn2:businessRuleTask drools:ruleFlowGroup="validation" name="Validation">
```

**v9 version (same as v8):**
```xml
<bpmn2:businessRuleTask drools:ruleFlowGroup="validation" name="Validation">
```

**No BPMN Changes Required!**
- Keep all `drools:ruleFlowGroup` attributes as-is
- The `ruleflow-group` in DRL files connects to the BPMN tasks
- No rule unit classes needed

**How It Works:**
1. BPMN task has `drools:ruleFlowGroup="validation"`
2. DRL file has `ruleflow-group "validation"`
3. Rule engine automatically executes matching rules
   - Executes rules matching the ruleflow-group

#### Process ID Naming in v9

**Important Note:** BAMOE v9 supports process IDs with dots (.) and hyphens (-), so no changes are required for migration.

**Best Practice:** For new projects, use simple alphanumeric names (e.g., `MortgageApprovalProcess`) for better readability, but existing IDs with dots or hyphens work perfectly in v9.

**This tutorial:** Uses the original v8 process ID `Mortgage_Process.MortgageApprovalProcess` to demonstrate backward compatibility.

#### How ruleFlowGroup Works in v9 (Our Approach)

**How it works in v9 (using ruleFlowGroup):**
1. Business rule tasks keep their `drools:ruleFlowGroup` attributes in BPMN
2. DRL files keep their `ruleflow-group` declarations
3. At runtime, v9 automatically:
   - Maps ruleFlowGroup to the corresponding DRL rules
   - Passes process variables via Input/Output data mapping
   - Executes all rules in that unit
   - Returns updated data back to the process

**Example from our BPMN:**
```xml
<!-- Business Rule Task in v9 BPMN -->
<bpmn2:businessRuleTask id="_8E266769-E6A8-4D46-9EEA-D564234BF7E9"
                        drools:ruleFlowGroup="validation"
                        name="Validation">
  <bpmn2:incoming>_FA6E0A27-BCE9-4115-A953-EE15AF02B9A5</bpmn2:incoming>
  <bpmn2:outgoing>_5DBEB9C6-1B5D-4D1E-99A8-12B66B42FFB0</bpmn2:outgoing>
  <!-- Data input/output mappings -->
</bpmn2:businessRuleTask>
```

**Mapping ruleFlowGroup to DRL Rules:**

The BPMN file uses these ruleFlowGroups:
- `validation` → Maps to ValidateDownPayment.drl rules
- `error` → Maps to RetractValidationErr.drl rules
- `mortgagecalculation` → Maps to MortgageDecisionTable.drl rules


### Step 7: Configure Application Properties

Create `v9-app/src/main/resources/application.properties`:

```properties
# Quarkus Configuration
quarkus.http.port=8080
quarkus.http.cors=true

# Database Configuration (H2 for development)
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:testdb
quarkus.datasource.username=sa
quarkus.datasource.password=

# Hibernate Configuration
quarkus.hibernate-orm.database.generation=update
quarkus.hibernate-orm.log.sql=false

# BAMOE Configuration
kogito.service.url=http://localhost:8080
kogito.dataindex.http.url=http://localhost:8080
kogito.jobs-service.url=http://localhost:8080

# Dev UI
quarkus.devservices.enabled=true
```

### Step 8: Build the Application

```bash
cd v9-app
mvn clean package
```

**Expected output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 45.123 s
```
### Step 9: Generate Form Code

Use BAMOE Developer Tools to generate TypeScript React forms:

1. Open Command Palette: `Cmd+Shift+P` (Mac) or `Ctrl+Shift+P` (Windows/Linux)
2. Type: "BAMOE Developer Tools: Generate form code for User Tasks"
3. Select your project directory
4. Choose UI framework: **PatternFly** (recommended) or **Bootstrap 4**
5. Select: **All tasks**

Generated files will be in `src/main/resources/custom-forms-dev/`

### Step 10: Build and Run in Development Mode

```bash
mvn clean install
mvn quarkus:dev -Pdevelopment
```

If you are using Gradle, use the following commands:

```bash
gradle clean build
gradle clean quarkusDev
```

**Expected output:**
```
__  ____  __  _____   ___  __ ____  ______ 
 --/ __ \/ / / / _ | / _ \/ //_/ / / / __/ 
 -/ /_/ / /_/ / __ |/ , _/ ,< / /_/ /\ \   
--\___\_\____/_/ |_/_/|_/_/|_|\____/___/   
                                            
INFO  [io.quarkus] your-bamoe-business-service 1.0.0-SNAPSHOT started in 8.234s
Listening on: http://localhost:8080
```

### Step 11: Generate Process SVG (Optional)

To generate an SVG preview of your BPMN process, open the BPMN file in VS Code and type `>svg` in the command palette, then select **"BAMOE Developer Tools: Generate BPMN Editor preview SVG"**.

### Step 12: Access the Application

1. **Swagger UI**: http://localhost:8080/q/swagger-ui
2. **Dev UI**: http://localhost:8080/q/dev-ui
3. **Process Instances**: http://localhost:8080/q/dev-ui/com.ibm.bamoe.bamoe-quarkus-devui/process-instances

---
## Testing

### Access Points

1. **Swagger UI**: http://localhost:8080/q/swagger-ui
   - View and test all REST APIs
   - Start process instances
   - Complete human tasks

2. **Dev UI**: http://localhost:8080/q/dev-ui
   - View process instances
   - View human tasks
   - Monitor application metrics

3. **Process Instances**: http://localhost:8080/q/dev-ui/com.ibm.bamoe.bamoe-quarkus-devui/process-instances
   - View all running process instances
   - View process variables
   - View process history

### Starting a Process Instance

**Using Swagger UI:**

1. Navigate to http://localhost:8080/q/swagger-ui
2. Find `POST /MortgageApprovalProcess` endpoint
3. Click "Try it out"
4. Enter the request body
5. Click "Execute"

**Using cURL:**

```bash
curl -X POST http://localhost:8080/MortgageApprovalProcess \
  -H "Content-Type: application/json" \
  -d '{
    "application": {
      "applicant": {
        "name": "John Doe",
        "ssn": "123456789",
        "annualincome": 75000,
        "address": "123 Main Street, Boston, MA",
        "creditrating": 650
      },
      "property": {
        "address": "456 Oak Avenue, Boston, MA",
        "saleprice": 300000,
        "age": 15,
        "locale": "urban"
      },
      "downpayment": 60000,
      "mortgageamount": 0,
      "amortization": 0
    }
  }'
```

### Sample Payloads

#### Test Case 1: Valid Application

```json
{
  "application": {
    "applicant": { "name": "John Doe", "ssn": "123456789", "annualincome": 75000, "address": "123 Main St", "creditrating": 650 },
    "property": { "address": "456 Oak Ave", "saleprice": 300000, "age": 15, "locale": "urban" },
    "downpayment": 60000,
    "mortgageamount": 0,
    "amortization": 0
  }
}
```
**Result:** 
```{
  "id": "7422df1e-28c8-4cbb-9de5-3adcb5edac57",
  "inlimit": null,
  "application": {
    "applicant": {
      "name": "John Doe",
      "annualincome": 75000,
      "address": "123 Main Street, Boston, MA",
      "ssn": 123456789,
      "creditrating": 650
    },
    "property": {
      "age": 15,
      "address": "456 Oak Avenue, Boston, MA",
      "locale": "urban",
      "saleprice": 300000
    },
    "errors": null,
    "downpayment": 60000,
    "amortization": 0,
    "mortgageamount": 0
  },
  "incdownpayment": null
}
```

#### Test Case 2: Invalid (Down Payment = 0)

Set `downpayment: 0` in the payload above.

**Result:** `errors.error` = "Down payment cannot be 0, greater than, or equal to the property sale price."

#### Test Case 3: Invalid (Down Payment >= Sale Price)

Set `downpayment: 250000` and `saleprice: 200000` in the payload.

**Result:** Same validation error as Test Case 2

### Test Scenarios

#### Scenario 1: No Errors
1. Start with valid payload (down payment between 0 and sale price)
2. Process flows: Start → Validation → Mortgage Calculation → Qualify
3. User sets `inlimit = true` in Qualify task
4. Process flows to Final Approval
5. User completes Final Approval
6. Process ends

#### Scenario 2: Error Path (Validation Fails)
1. Start with invalid payload (down payment = 0 or >= sale price)
2. Process flows: Start → Validation (fails) → Correct Data
3. User corrects the data in Correct Data task
4. Process flows: Retract Validation → Validation (passes) → Mortgage Calculation → Qualify
5. Continue as happy path

#### Scenario 3: Increase Down Payment Path
1. Start with valid payload
2. Process flows through Validation and Mortgage Calculation
3. User sets `inlimit = false` in Qualify task
4. Process flows to Increase Down Payment task
5. User sets `incdownpayment = true`
6. Process loops back: Retract Validation → Validation → Mortgage Calculation → Qualify
7. User sets `inlimit = true`
8. Process flows to Final Approval and ends

### Completing Human Tasks

**Using Dev UI:**

1. Navigate to http://localhost:8080/q/dev-ui
2. Click on "Process Instances"
3. Find your process instance
4. Click on the human task
5. **IMPORTANT:** Click "Claim" button first to claim the task
6. After claiming, the task form will open
7. Fill in the form fields
8. Click "Complete"

**Using Swagger UI:**

1. Navigate to http://localhost:8080/q/swagger-ui
2. Find `GET /MortgageApprovalProcess/{id}/tasks` to list tasks
3. Find `POST /MortgageApprovalProcess/{id}/{taskName}/{taskId}` to complete task
4. Enter task outputs (e.g., `{"inlimit": true}`)
5. Click "Execute"

### Custom Forms for Complex Data Structures

The v9 application includes **custom forms** for user tasks that work with complex nested objects. These forms ensure proper rendering and functionality in the Dev UI.

#### Why Custom Forms Are Needed

The BAMOE 9.3.x Dev UI auto-generates forms for user tasks based on their input/output data mappings. 

#### Custom Forms Created

**Location:** `src/main/resources/custom-forms-dev/`

#### Testing Custom Forms

1. Start the application: `mvn quarkus:dev -Pdevelopment`
2. Navigate to the Dev UI and log in with any user
3. Start a new process instance
4. When tasks appear, **claim** them first by clicking the "Claim" button
5. After claiming, the task form will open with the "Complete" button visible
6. Fill in the fields and click "Complete"

**User Configuration (for testing convenience):**
All users have access to all groups so any user can claim and complete any task:
- `broker` user: groups = `broker,approver,manager`
- `approver` user: groups = `broker,approver,manager`
- `manager` user: groups = `broker,approver,manager`

**Note:** In production, you would typically restrict users to their specific roles.

### Creating Test Scenarios (Optional)

BAMOE v9 supports **Test Scenarios** (.scesim files) for testing rules and decisions without running the full application. This is useful for unit testing your business logic.

**What are Test Scenarios?**
- Visual tool for testing rules, decisions, and processes
- Define inputs and expected outputs in a table format
- Automatically executed by JUnit when running `mvn test`
- Supported for DRL rules, DMN decisions, and BPMN processes

**Creating a Test Scenario:**

1. In BAMOE Canvas or VS Code, create a new file: `src/test/resources/com/myspace/mortgage_app/MortgageValidation.scesim`

2. Define test cases in the scenario:
   - **GIVEN**: Input data (Application, downpayment, saleprice)
   - **EXPECT**: Expected results (errors field should be null or contain error message)

3. The TestScenarioJunitActivatorTest will automatically discover and run all .scesim files

**Running Test Scenarios:**
```bash
mvn test
```

**References:**
- [Test Scenarios Documentation](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=developing-decisions-rules-test-scenarios)

### Verification Checklist

- [ ] Build succeeds: `mvn clean package`
- [ ] Application starts: `mvn quarkus:dev -Pdevelopment`
- [ ] Swagger UI shows APIs: http://localhost:8080/q/swagger-ui
- [ ] Dev UI accessible: http://localhost:8080/q/dev-ui
- [ ] Process instances visible in Dev UI
- [ ] Rules execute correctly
- [ ] Human tasks appear in Dev UI
- [ ] Validation rules work (test with invalid data)
- [ ] Mortgage calculation rules work
- [ ] Process completes successfully
- [ ] (Optional) Test scenarios pass: `mvn test`

---

## Common Issues

### Issue 1: Runtime Error - "Exception when trying to evaluate constraint in split"

**Symptom:**
```
Exception when trying to evaluate constraint in split: 
return KieFunctions.isTrue(application.getErrors() != null);
```

**Cause:**
Gateway condition uses unsupported `KieFunctions.isTrue()` wrapper.

**Solution:**
Remove the wrapper and use direct boolean expression:
```xml
<!-- Before -->
<bpmn2:conditionExpression>
  return KieFunctions.isTrue(application.getErrors() != null);
</bpmn2:conditionExpression>

<!-- After -->
<bpmn2:conditionExpression>
  return application.getErrors() != null;
</bpmn2:conditionExpression>
```

### Issue 2: GDST File Not Supported

**Symptom:**
Build fails or GDST file is not recognized.

**Cause:**
GDST (Guided Decision Table) files are not supported in BAMOE v9.

**Solution:**
Convert GDST to Excel Decision Table (.xlsx) or DRL:

**Option 1 - Excel (Recommended):**
1. Use "Convert to XLS" button in v8 editor if available
2. Or manually recreate the decision table in Excel format
3. Place `.xlsx` file in `src/main/resources/com/myspace/mortgage_app/`

**Option 2 - DRL:**
1. Copy DRL content from Source tab in v8 editor
2. Create `.drl` file with package and import declarations
3. Use ruleflow-group for BPMN integration

### Issue 3: MVEL Dialect Not Supported

**Symptom:**
Rules with MVEL dialect fail to compile.

**Cause:**
MVEL dialect is not supported in v9. Only Java dialect is supported.

**Solution:**
Convert MVEL syntax to Java:
```drl
<!-- Before (MVEL) -->
rule "Calculate Mortgage"
    dialect "mvel"
    when
        $app : Application()
    then
        $app.mortgageamount = $app.property.saleprice - $app.downpayment;
end

<!-- After (Java) -->
rule "Calculate Mortgage"
    dialect "java"
    when
        $app: /applications
    then
        $app.setMortgageamount($app.getProperty().getSaleprice() - $app.getDownpayment());
end
```

---


## Summary

### What You Accomplished

In this tutorial, successfully migrated a complex stateful workflow from BAMOE v8 to v9:

**Migrated Components:**
- 1 BPMN process with business rule tasks and human tasks
- 4 Java data model classes
- 3 DRL rule files (using ruleflow-group approach)
- 1 Guided decision table (converted to DRL)
- 5 TSX form files (auto-generated)

**Key Changes Made:**
- Removed insert/retract calls from DRL rules
- Converted GDST to DRL with ruleflow-group
- Removed unsupported @Label annotations
- Configured Quarkus application properties
- Used ruleflow-group approach

**Skills Learned:**
- Converting legacy rules to v9 format
- Migrating GDST files to DRL
- Using ruleflow-group for BPMN integration
- Removing insert/retract patterns
- Testing stateful workflows

### Migration Pattern Summary

| Component | v8 Pattern | v9 Pattern (Backward Compatible) |
|-----------|-----------|-----------|
| **Process ID** | `Mortgage_Process.MortgageApprovalProcess` | `MortgageApprovalProcess` | -- recommeneded
| **Rule Organization** | ruleflow-group | ruleflow-group (same) |
| **Data Insertion** | `insert(fact)` | Remove (not needed) |
| **Data Retraction** | `retract(fact)` | Remove or use `delete()` |
| **Rule Declaration** | `ruleflow-group "name"` | `ruleflow-group "name"` (same) |
| **GDST Files** | `.gdst` format | Convert to `.drl` |


### Additional Resources

- [Stateful Workflows Guide](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=developing-stateful-workflows)
- [Upgrading from 8.0.x](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=upgrading-from-80x)
- [Not Supported in BAMOE 9.3](https://www.ibm.com/docs/en/ibamoe/9.3.x?topic=80x-not-supported-in-bamoe-93)
- [GitHub: bamoe-canvas-quarkus-accelerator](https://github.com/IBM/bamoe-canvas-quarkus-accelerator)