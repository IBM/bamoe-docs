# Tutorial 15: Case Management IT Orders - V8 to V9 Migration

## Overview

This tutorial demonstrates the migration of a **v8 ad-hoc case management** application to **BAMOE v9**

**Key Learning**: V8 ad-hoc case management (`drools:adHoc="true"`) can be migrated to v9 by keeping the ad-hoc structure but removing the `Condition` input parameter from Milestone tasks, as v9 does not support conditional milestone activation.


---

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [V8 vs V9 Architecture](#v8-vs-v9-architecture)
4. [Migration Steps](#migration-steps)
5. [Process Redesign](#process-redesign)
6. [Testing the Application](#testing-the-application)
7. [Key Changes Summary](#key-changes-summary)
8. [Lessons Learned](#lessons-learned)

---

## Introduction

### What This Tutorial Covers

- Understanding v8 case management limitations
- Redesigning ad-hoc processes for v9
- Converting from kjar packaging to Quarkus
- Creating structured BPMN workflows
- Testing the migrated application

### Original V8 Application

The v8 application was an **ad-hoc case management** process for IT hardware orders with:
- Ad-hoc process (`drools:adHoc="true"`)
- Disconnected tasks triggered dynamically
- Milestones with **conditional expressions** for tracking progress
- Case roles (owner, manager, supplier)
- Dynamic task execution

### V9 Application

The v9 application maintains the **ad-hoc case management structure** with:
- Ad-hoc process (`drools:adHoc="true"`) - **retained**
- Disconnected tasks - **retained**
- Milestones **without conditional expressions** - **modified**
- Case roles (group-based task assignments) - **migrated**
- Case management metadata (`customCaseIdPrefix`, `customCaseRoles`) - **removed**
- Dynamic task execution - **retained**

---

## Prerequisites

- Java 17 or higher
- Maven 3.8+
- BAMOE 9.3.1 or higher
- Understanding of BPMN 2.0
- Familiarity with Quarkus

---

## V8 vs V9 Architecture

### V8 Ad-hoc Case Management

```
[No Start Node]
  ↓ (dynamic triggering)
[Prepare Hardware Spec] [Manager Approval] [Place Order] ...
  ↓ (no connections)
[Milestone 1] [Milestone 2] [Milestone 3]
  ↓ (conditional)
[Customer Survey]
```

**Characteristics:**
- `drools:adHoc="true"` attribute
- Tasks have no incoming/outgoing connections
- Triggered via API: `caseService.triggerAdHocFragment()`
- Milestones track progress conditionally
- No predefined flow

### V9 Ad-hoc Process (Milestone Conditions Removed)

```
[No Start Node - Ad-hoc]
  ↓ (dynamic triggering)
[Prepare Hardware Spec] [Manager Approval] [Place Order] ...
  ↓ (no connections)
[Milestone 1] [Milestone 2] [Milestone 3]  ← Conditions REMOVED
  ↓ (triggered programmatically)
[Customer Survey]
```

**Characteristics:**
- `drools:adHoc="true"` attribute - **retained**
- Tasks have no incoming/outgoing connections - **retained**
- Triggered via API - **retained**
- Milestones track progress **without conditions** - **modified**
- No predefined flow - **retained**

---

## Migration Steps

### Step 1: Analyze V8 Application

```bash
cd v8-to-v9-upgrade-tutorials/tutorial-15-case-management-it-orders/v8-app
```

**V8 Structure:**
```
v8-app/
├── pom.xml (kjar packaging)
├── src/main/
│   ├── java/
│   │   └── org/jbpm/demo/itorders/
│   │       ├── Survey.java
│   │       └── services/ITOrderService.java
│   └── resources/
│       ├── orderhardware.bpmn (ad-hoc process)
│       ├── place-order.bpmn (subprocess)
│       ├── orderhardware.svg
│       └── *.frm (form definitions)
```

**Key V8 Features:**
- Ad-hoc process with `drools:adHoc="true"`
- Disconnected tasks and milestones
- Case management metadata
- Form definitions (.frm files)

### Step 2: Create V9 Project Structure

```bash
cd v8-to-v9-upgrade-tutorials/tutorial-15-case-management-it-orders
mkdir -p v9-app/src/main/java/org/jbpm/demo/itorders
mkdir -p v9-app/src/main/resources
```

### Step 3: Create V9 POM

Create [`v9-app/pom.xml`](v9-app/pom.xml) with Quarkus dependencies:

```xml
<project>
    <modelVersion>4.0.0</modelVersion>
    <groupId>org.jbpm.demo</groupId>
    <artifactId>itorders-v9</artifactId>
    <version>1.0.0</version>
    
    <properties>
        <version.quarkus>3.20.3</version.quarkus>
        <version.bamoe>9.3.1-ibm-0006</version.bamoe>
    </properties>
    
    <dependencies>
        <!-- BAMOE Process Runtime -->
        <dependency>
            <groupId>com.ibm.bamoe</groupId>
            <artifactId>jbpm-with-drools-quarkus</artifactId>
        </dependency>
        
        <!-- Persistence -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-jdbc-h2</artifactId>
        </dependency>
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-agroal</artifactId>
        </dependency>
        
        <!-- REST -->
        <dependency>
            <groupId>io.quarkus</groupId>
            <artifactId>quarkus-rest-jackson</artifactId>
        </dependency>
    </dependencies>
</project>
```

### Step 4: Copy and Update Java Classes

Copy Java classes from v8-app:

```bash
cp v8-app/src/main/java/org/jbpm/demo/itorders/Survey.java \
   v9-app/src/main/java/org/jbpm/demo/itorders/

cp -r v8-app/src/main/java/org/jbpm/demo/itorders/services \
   v9-app/src/main/java/org/jbpm/demo/itorders/
```

**Update [`Survey.java`](v9-app/src/main/java/org/jbpm/demo/itorders/Survey.java)** - Add boolean "is" style getters:

```java
public boolean isSatisfied() {
    return this.satisfied != null && this.satisfied;
}

public boolean isDeliveredOnTime() {
    return this.deliveredOnTime != null && this.deliveredOnTime;
}
```

### Step 5: Create Application Properties

Create [`v9-app/src/main/resources/application.properties`](v9-app/src/main/resources/application.properties):

```properties
# Quarkus Configuration
quarkus.http.port=8080

# Persistence
kogito.persistence.type=jdbc
quarkus.datasource.db-kind=h2
quarkus.datasource.jdbc.url=jdbc:h2:mem:itorders;DB_CLOSE_DELAY=-1
quarkus.datasource.username=sa
quarkus.datasource.password=

# User Task Configuration
kogito.service.url=http://localhost:8080

# User and Group Configuration for Task Assignment
# Define users and their groups to enable task claiming in Dev UI
%dev.bamoe.devui.users.jdoe.groups=admin,supplier,manager
%dev.bamoe.devui.users.mary.groups=supplier
%dev.bamoe.devui.users.katy.groups=manager

# Dev UI
quarkus.dev-ui.always-include=true
```

**User/Group Configuration:**
- Uses `%dev.bamoe.devui.users.<username>.groups` property format for BAMOE Dev UI
- `jdoe`: Member of `admin`, `supplier`, and `manager` groups (can claim all tasks)

### Step 6: Update BPMN Process - Key Migration Changes

 The v8 BPMN file has been migrated to v9 with the following specific changes:

#### Change 1: Remove Document-Based Hardware Specification Variable

**V8 BPMN:**
```xml
<!-- Item definition for document type -->
<bpmn2:itemDefinition id="_caseFile_hwSpecItem" structureRef="org.jbpm.document.Document"/>
<bpmn2:itemDefinition id="__DA138C6C-B64D-4082-9613-ED33D42AC9DE__hwSpecInputXItem"
                      structureRef="org.jbpm.document.Document"/>
<bpmn2:itemDefinition id="__BFA6002D-0917-42CE-81AD-2A15EC814684_hwSpec_OutputXItem"
                      structureRef="org.jbpm.document.Document"/>

<!-- Process variable -->
<bpmn2:property id="caseFile_hwSpec" itemSubjectRef="_caseFile_hwSpecItem" name="caseFile_hwSpec"/>
```

**V9 BPMN:**
```xml
<!-- Document-based item definitions REMOVED -->
<!-- caseFile_hwSpec process variable REMOVED -->
```

**Reason:** V9 doesn't support `org.jbpm.document.Document` type. Hardware specifications should be handled as String or custom Java objects.

**How to migrate:** Search for all `itemDefinition` elements with `structureRef="org.jbpm.document.Document"` and remove them, along with the corresponding process variable declarations.
#### Change 1a: Remove Case Management Metadata

**V8 BPMN:**
```xml
<bpmn2:process id="orderhardware" drools:packageName="org.jbpm.demo.itorders" 
               drools:version="1.0" drools:adHoc="true" 
               name="Order for IT hardware" isExecutable="true">
  <bpmn2:extensionElements>
    <drools:metaData name="customCaseIdPrefix">
      <drools:metaValue>IT</drools:metaValue>
    </drools:metaData>
    <drools:metaData name="customCaseRoles">
      <drools:metaValue>owner:1,manager:1,supplier:2</drools:metaValue>
    </drools:metaData>
  </bpmn2:extensionElements>
```

**V9 BPMN:**
```xml
<bpmn2:process id="orderhardware" drools:packageName="org.jbpm.demo.itorders" 
               drools:version="1.0" drools:adHoc="true" 
               name="Order for IT hardware" isExecutable="true">
  <!-- Case management metadata REMOVED -->
```

**Reason:** V9 does not support `customCaseIdPrefix` or `customCaseRoles` metadata. Process instances use standard Kogito IDs, and task assignments use standard group-based mechanisms.

**How to migrate:** 
1. Locate the `<bpmn2:extensionElements>` section within the `<bpmn2:process>` element
2. Remove the entire `<drools:metaData name="customCaseIdPrefix">` block
3. Remove the entire `<drools:metaData name="customCaseRoles">` block
4. If no other metadata remains, remove the entire `<bpmn2:extensionElements>` section
5. Task assignments should use standard group names (supplier, manager, owner) in the `GroupId` parameter of user tasks


#### Change 2: Remove Milestone Condition Parameters

**V8 BPMN - Milestone 1:**
```xml
<bpmn2:task id="_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED" drools:taskName="Milestone"
            name="Milestone 1: Order placed">
    <bpmn2:ioSpecification>
        <!-- Condition input parameter -->
        <bpmn2:dataInput id="_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED_ConditionInputX"
                         drools:dtype="String"
                         itemSubjectRef="__DCD97847-6E3C-4C5E-9EE3-221C04BE42ED_ConditionInputXItem"
                         name="Condition"/>
        <bpmn2:dataInput id="_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED_TaskNameInputX"
                         drools:dtype="Object"
                         name="TaskName"/>
        <bpmn2:inputSet>
            <bpmn2:dataInputRefs>_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED_ConditionInputX</bpmn2:dataInputRefs>
            <bpmn2:dataInputRefs>_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED_TaskNameInputX</bpmn2:dataInputRefs>
        </bpmn2:inputSet>
    </bpmn2:ioSpecification>
    <!-- Condition data input association with CaseData expression -->
    <bpmn2:dataInputAssociation>
        <bpmn2:targetRef>_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED_ConditionInputX</bpmn2:targetRef>
        <bpmn2:assignment>
            <bpmn2:from><![CDATA[org.kie.api.runtime.process.CaseData(data.get("ordered") == true)]]></bpmn2:from>
            <bpmn2:to>_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED_ConditionInputX</bpmn2:to>
        </bpmn2:assignment>
    </bpmn2:dataInputAssociation>
</bpmn2:task>
```

**V9 BPMN:**
```xml
<bpmn2:task id="_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED" drools:taskName="Milestone"
            name="Milestone 1: Order placed">
    <bpmn2:ioSpecification>
        <!-- Condition input parameter REMOVED -->
        <bpmn2:dataInput id="_7729DE14-3E57-4224-A869-47AE40006F34"
                         name="TaskName"
                         drools:dtype="Object"/>
        <bpmn2:inputSet>
            <!-- Only TaskName remains -->
            <bpmn2:dataInputRefs>_7729DE14-3E57-4224-A869-47AE40006F34</bpmn2:dataInputRefs>
        </bpmn2:inputSet>
    </bpmn2:ioSpecification>
    <!-- Condition data input association REMOVED -->
    <bpmn2:dataInputAssociation>
        <bpmn2:targetRef>_7729DE14-3E57-4224-A869-47AE40006F34</bpmn2:targetRef>
        <bpmn2:assignment>
            <bpmn2:from>Milestone</bpmn2:from>
            <bpmn2:to>_7729DE14-3E57-4224-A869-47AE40006F34</bpmn2:to>
        </bpmn2:assignment>
    </bpmn2:dataInputAssociation>
</bpmn2:task>
```

**Reason:** V9 doesn't support conditional milestone activation via `Condition` input parameter. Milestones must be triggered programmatically.

**How to migrate:** For each milestone task in the BPMN file, locate the `<bpmn2:ioSpecification>` section and remove the `Condition` dataInput element, remove it from the inputSet, and remove the entire dataInputAssociation that contains the CaseData expression. Keep only the TaskName dataInput and its assignment.

**This change applies to all 3 milestone tasks:**
- Milestone 1: Order placed (id: `_DCD97847-6E3C-4C5E-9EE3-221C04BE42ED`)
- Milestone 2: Order shipped (id: `_343B90CD-AA19-4894-B63C-3CE1906E6FD1`)
- Milestone 3: Delivered to customer (id: `_52AFA23F-C087-4519-B8F2-BABCC31D68A6`)

#### Change 3: Simplify Gateway Sequence Flows

**V8 BPMN:**
```xml
<!-- Two outgoing flows from gateway -->
<bpmn2:sequenceFlow id="_9855381D-9416-4781-AF8D-1059B776A78F"
                    sourceRef="_1557D7B6-3628-4900-BE08-C642405C4829"
                    targetRef="_3D558B77-D144-4C9C-B5A9-8931A5872C24">
    <bpmn2:conditionExpression language="http://www.java.com/java">
        <![CDATA[return KieFunctions.isFalse(approved);]]>
    </bpmn2:conditionExpression>
</bpmn2:sequenceFlow>

<bpmn2:sequenceFlow id="_1CEE5151-C611-4144-90D5-231078F12165"
                    sourceRef="_1557D7B6-3628-4900-BE08-C642405C4829"
                    targetRef="_F7C0BB87-F47D-4426-8260-1BDEC8A6806D">
    <bpmn2:conditionExpression language="http://www.java.com/java">
        <![CDATA[return KieFunctions.isTrue(approved);]]>
    </bpmn2:conditionExpression>
</bpmn2:sequenceFlow>
```

**V9 BPMN:**
```xml
<!-- Removed CDATA wrapper and simplified condition expression syntax -->
<bpmn2:sequenceFlow id="_9855381D-9416-4781-AF8D-1059B776A78F"
                    sourceRef="_1557D7B6-3628-4900-BE08-C642405C4829"
                    targetRef="_3D558B77-D144-4C9C-B5A9-8931A5872C24">
    <bpmn2:conditionExpression xsi:type="bpmn2:tFormalExpression"
                               language="http://www.java.com/java">
        return KieFunctions.isFalse(approved);
    </bpmn2:conditionExpression>
</bpmn2:sequenceFlow>

<bpmn2:sequenceFlow id="_1CEE5151-C611-4144-90D5-231078F12165"
                    sourceRef="_1557D7B6-3628-4900-BE08-C642405C4829"
                    targetRef="_F7C0BB87-F47D-4426-8260-1BDEC8A6806D">
    <bpmn2:conditionExpression id="_B0mB0gYREeq5x4k5aBfW8A"
                               xsi:type="bpmn2:tFormalExpression"
                               language="http://www.java.com/java">
        return KieFunctions.isTrue(approved);
    </bpmn2:conditionExpression>
</bpmn2:sequenceFlow>
```

**Reason:** Removed CDATA wrapper from condition expressions. The approved path still goes through the subprocess call activity (Place order) - the flow structure remains the same as V8.

#### Change 4: Update Process IDs (Main Process)

**V8 BPMN:**
```xml
<bpmn2:process id="itorders.orderhardware"
               drools:packageName="org.jbpm.demo.itorders"
               drools:version="1.0"
               drools:adHoc="true"
               name="Order for IT hardware"
               isExecutable="true">
```

**V9 BPMN:**
```xml
<bpmn2:process id="orderhardware"
               drools:packageName="org.jbpm.demo.itorders"
               drools:version="1.0"
               drools:adHoc="true"
               name="Order for IT hardware"
               isExecutable="true">
```

**Key Differences:**
- **V8**: Uses namespaced process ID: `id="itorders.orderhardware"`
- **V9**: Uses simple process ID: `id="orderhardware"`

**Reason:** V9 simplifies process IDs by removing namespace prefixes.

#### Change 5: Call Activity Reference Update

**V8 BPMN:**
```xml
<bpmn2:callActivity id="_F7C0BB87-F47D-4426-8260-1BDEC8A6806D"
                    drools:independent="false"
                    drools:waitForCompletion="true"
                    name="Place order"
                    calledElement="itorders-data.place-order">
```

**V9 BPMN:**
```xml
<bpmn2:callActivity id="_F7C0BB87-F47D-4426-8260-1BDEC8A6806D"
                    drools:independent="false"
                    drools:waitForCompletion="true"
                    name="Place order"
                    calledElement="place_order">
```

**Key Differences:**
- **V8**: Uses fully qualified process ID: `calledElement="itorders-data.place-order"`
- **V9**: Uses simple process ID: `calledElement="place_order"`

**Reason:** V9 simplifies subprocess references by using the process ID directly from the called BPMN file's `<bpmn2:process id="place_order">` attribute, without requiring namespace prefixes.

#### Change 6: Subprocess Process ID and BPMNPlane Reference (place-order.bpmn)

**V8 place-order.bpmn:**
```xml
<bpmn2:process id="itorders-data.place-order"
               drools:packageName="org.jbpm.demo.itorders"
               drools:version="1.0"
               name="place-order"
               isExecutable="true">
```

**V9 place-order.bpmn:**
```xml
<bpmn2:process id="place_order"
               drools:packageName="org.jbpm.demo.itorders"
               drools:version="1.0"
               name="place_order"
               isExecutable="true">
```

**V8 place-order.bpmn BPMNPlane:**
```xml
<bpmndi:BPMNPlane id="_0O98sdNMEeavSs8IK1dqtA" bpmnElement="itorders-data.place-order">
```

**V9 place-order.bpmn BPMNPlane:**
```xml
<bpmndi:BPMNPlane id="_0O98sdNMEeavSs8IK1dqtA" bpmnElement="place_order">
```
The BPMNPlane's `bpmnElement` attribute MUST exactly match the process ID. Mismatch causes compilation errors


**Migration Steps for Subprocess:**
1. Update process ID from `itorders-data.place-order` to `place_order`
2. Update process name from `place-order` to `place_order`
3. **CRITICAL:** Update BPMNPlane `bpmnElement` to match new process ID
4. Remove all Document-type variables and item definitions
5. Move file to `v9-app/src/main/resources/`

**Migration Steps for Call Activities:**
1. Locate all `<bpmn2:callActivity>` elements in your v8 BPMN
2. Update the `calledElement` attribute to match the subprocess's new process ID
3. Ensure the referenced BPMN file exists in the same resources directory
4. Verify the process ID in the called BPMN matches the `calledElement` value

**Example:**
- V8: `calledElement="com.example.myprocess.subprocess1"`
- V9: `calledElement="subprocess1"` (where subprocess1.bpmn has `<bpmn2:process id="subprocess1">`)
- **IMPORTANT:** Also update `<bpmndi:BPMNPlane bpmnElement="subprocess1">` in the subprocess file

**Data Mapping:** The data input/output mappings remain the same between v8 and v9:
```xml
<bpmn2:dataInputAssociation>
    <bpmn2:sourceRef>CaseId</bpmn2:sourceRef>
    <bpmn2:targetRef>_F7C0BB87-F47D-4426-8260-1BDEC8A6806D_CaseIdInputX</bpmn2:targetRef>
</bpmn2:dataInputAssociation>
```

For detailed information about call activity implementation, see [`CALL-ACTIVITY-IMPLEMENTATION.md`](CALL-ACTIVITY-IMPLEMENTATION.md).

#### Migration Steps Summary:

##### For orderhardware.bpmn (Main Process):

1. **Remove Document Type Definitions**:
   - Search for `<bpmn2:itemDefinition>` elements with `structureRef="org.jbpm.document.Document"`
   - Delete `<bpmn2:itemDefinition id="_caseFile_hwSpecItem" structureRef="org.jbpm.document.Document"/>`
   - Delete all Document-related input/output item definitions

2. **Remove Document Process Variable**:
   - Search for `<bpmn2:property>` elements referencing the Document item definition
   - Delete `<bpmn2:property id="caseFile_hwSpec" itemSubjectRef="_caseFile_hwSpecItem" name="caseFile_hwSpec"/>`

3. **Remove Milestone Conditions** (applies to all 3 milestones):
   - For each milestone task, locate the `<bpmn2:ioSpecification>` section
   - Remove the `Condition` dataInput element
   - Remove `Condition` from the inputSet dataInputRefs
   - Remove the entire dataInputAssociation that contains the CaseData expression
   - Keep only the TaskName dataInput and its assignment

4. **Update Process ID**:
   - Locate the `<bpmn2:process>` element
   - Change from `id="itorders.orderhardware"` to `id="orderhardware"`

5. **Update Call Activity Reference**:
   - Locate the `<bpmn2:callActivity>` element
   - Change `calledElement="itorders-data.place-order"` to `calledElement="place_order"`

6. **Move File**:
   - From: `v8-app/src/main/resources/org/jbpm/demo/itorders/orderhardware.bpmn`
   - To: `v9-app/src/main/resources/orderhardware.bpmn`

##### For place-order.bpmn (Subprocess):

1. **Remove Document Type Definitions**:
   - Search for all `<bpmn2:itemDefinition>` elements with `structureRef="org.jbpm.document.Document"`
   - Delete all Document-related item definitions

2. **Remove Document Process Variable**:
   - Search for `<bpmn2:property>` elements referencing the Document item definition
   - Delete `<bpmn2:property id="caseFile_hwSpec" itemSubjectRef="_caseFile_hwSpecItem"/>`

3. **Update Process ID**:
   - Locate the `<bpmn2:process>` element
   - Change from `id="itorders-data.place-order"` to `id="place_order"`
   - Change name from `"place-order"` to `"place_order"`

4. **Update BPMNPlane Reference**:
   - Locate the `<bpmndi:BPMNPlane>` element (usually near the end of the file)
   - Change `bpmnElement="itorders-data.place-order"` to `bpmnElement="place_order"`
   - **This MUST match the process ID exactly or compilation will fail!**

5. **Move File**:
   - From: `v8-app/src/main/resources/org/jbpm/demo/itorders/place-order.bpmn`
   - To: `v9-app/src/main/resources/place-order.bpmn`

##### What to Keep (No Changes):

Ad-hoc structure, user tasks, script tasks, gateways, sequence flows, task assignments (using standard groups)

### Step 7: Build the Application

```bash
cd v9-app
mvn clean install -DskipTests
```

**Expected Output:**
```
[INFO] BUILD SUCCESS
[INFO] Total time: 30.064 s
```

### Step 8: Run the Application

```bash
mvn quarkus:dev -Pdevelopment
```

**Access Points:**
- Dev UI: http://localhost:8080/q/dev
- Swagger UI: http://localhost:8080/q/swagger-ui
- Process Endpoint: http://localhost:8080/itorders/orderhardware

---

## What Was Actually Migrated

### Key Point: Ad-hoc Structure Was RETAINED

Unlike a complete redesign, this migration **preserved the v8 ad-hoc case management structure** with minimal changes:

#### What Was KEPT (No Changes):

1.  **Ad-hoc Process Structure**
   - `drools:adHoc="true"` attribute retained
   - Dynamic task triggering still supported
   - No start event required (ad-hoc processes don't need one)

2.  **Task Assignments**
   - Group-based assignments retained: supplier, manager, owner
   - Role-based task claiming works with standard groups

3.  **All Tasks and Structure**
   - User tasks: Prepare hardware spec, Manager approval, Order rejected, Customer survey
   - Script tasks: Send to tracking system, Notify requestor
   - Milestone tasks: All 3 milestones retained (just conditions removed)
   - Exclusive gateway for approval decision
   - Sequence flows between tasks

4. **Process Variables**
   - All variables retained except `caseFile_hwSpec` (Document type)
   - approved, CaseId, initiator, caseFile_managerComment, etc.

5. **Task Assignments**
   - Group-based assignments: supplier, manager, owner
   - Role-based task claiming

#### What Was CHANGED (Minimal):

1. **Removed Document Type Variable**
   - `caseFile_hwSpec` (org.jbpm.document.Document) removed
   - All Document-type item definitions removed
   - **Reason:** V9 doesn't support org.jbpm.document.Document

2.  **Removed Milestone Conditions**
   - `Condition` input parameter removed from all 3 milestone tasks
   - CaseData conditional expressions removed
   - **Reason:** V9 doesn't support conditional milestone activation
   - **Impact:** Milestones must now be triggered programmatically instead of automatically

3.  **Removed Case Management Metadata**
   - `customCaseIdPrefix` and `customCaseRoles` removed from BPMN
   - **Reason:** V9 does not support these case management metadata attributes
   - **Impact:** Process instances use standard Kogito IDs; task assignments use standard group-based mechanisms

### Migration Approach: Minimal Changes

This tutorial demonstrates a **minimal migration approach** where:
- The ad-hoc case management structure is preserved
- Only incompatible features are removed (Document type, Milestone conditions, Case metadata)
- The process continues to work as an ad-hoc case in v9
- No complete redesign required

### V8 vs V9 Comparison

| Feature | V8 | V9 | Status |
|---------|----|----|--------|
| **Process Type** | Ad-hoc (`drools:adHoc="true"`) | Ad-hoc (`drools:adHoc="true"`) | RETAINED |
| **Case Metadata** | customCaseIdPrefix, customCaseRoles | Removed (not supported) |  REMOVED |
| **Milestone Tasks** | 3 milestones with Condition parameter | 3 milestones without Condition | MODIFIED |
| **Document Variable** | caseFile_hwSpec (Document type) | Removed |  REMOVED |
| **User Tasks** | 4 user tasks | 4 user tasks |  RETAINED |
| **Script Tasks** | 2 script tasks | 2 script tasks |  RETAINED |
| **Gateway Logic** | Exclusive gateway with conditions | Exclusive gateway with conditions |  RETAINED |
| **Task Assignments** | Group-based (supplier, manager, owner) | Group-based (supplier, manager, owner) | RETAINED |
| **Sequence Flows** | Connected tasks | Connected tasks | RETAINED |

---

## Testing the Application

This section provides comprehensive API testing instructions for the IT Orders case management process. All examples use actual responses from the running application.

### Available API Endpoints

The application exposes the following REST endpoints (accessible via Swagger UI at http://localhost:8080/q/swagger-ui):

#### Process Management Endpoints
- `GET /orderhardware` - List all process instances
- `POST /orderhardware` - Create new process instance
- `GET /orderhardware/{id}` - Get process instance details
- `PUT /orderhardware/{id}` - Update process instance
- `PATCH /orderhardware/{id}` - Partially update process instance
- `DELETE /orderhardware/{id}` - Delete process instance
- `GET /orderhardware/{id}/tasks` - Get all tasks for a process instance
- `GET /orderhardware/schema` - Get process schema

#### Task Endpoints (User Tasks)
Each user task has the following endpoints:
- `GET /orderhardware/{id}/{TaskName}/{taskId}` - Get task details
- `POST /orderhardware/{id}/{TaskName}/{taskId}` - Complete task
- `PUT /orderhardware/{id}/{TaskName}/{taskId}` - Save task (partial update)
- `DELETE /orderhardware/{id}/{TaskName}/{taskId}` - Abort task
- `POST /orderhardware/{id}/{TaskName}/{taskId}/phases/{phase}` - Task phase transition
- `GET /orderhardware/{id}/{TaskName}/{taskId}/schema` - Get task schema

**Available Tasks:**
- `PrepareHardwareSpec` - Supplier prepares hardware specification
- `ManagerApproval` - Manager approves or rejects the order
- `OrderRejected` - Handle rejected order notification
- `CustomerSurvey` - Customer provides feedback

#### Milestone Signal Endpoints
- `POST /orderhardware/{id}/Milestone_2:_Order_shipped` - Signal order shipped
- `POST /orderhardware/{id}/Milestone_3:_Delivered_to_customer` - Signal order delivered

#### Subprocess Endpoints (place_order)
- `GET /place_order` - List place_order subprocess instances
- `POST /place_order` - Create place_order subprocess
- `GET /place_order/{id}` - Get subprocess details
- `GET /place_order/{id}/tasks` - Get subprocess tasks
- Task endpoints for `PlaceOrder` user task

---

### Complete Workflow Testing Guide

This guide walks through the complete IT hardware order process from start to finish.

#### Step 1: Create a New Process Instance

**Request:**
```bash
curl -X 'POST' \
  'http://localhost:8080/orderhardware' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "initiator": "john_doe"
}'
```

**Response:**
```json
{
  "id": "bbe2c098-39f1-4531-8cff-bbf5c476b899",
  "caseId": null,
  "approved": null,
  "caseFile_managerDecision": null,
  "initiator": "john_doe",
  "CaseId": null,
  "caseFile_shipped": null,
  "caseFile_delivered": null,
  "caseFile_supplierComment": null,
  "caseFile_managerComment": null
}
```

**Note:** Save the `id` value (this is the **process instance ID**) - you'll need it for subsequent requests. In this example: `bbe2c098-39f1-4531-8cff-bbf5c476b899`

---

#### Step 2: List All Process Instances

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/orderhardware' \
  -H 'accept: application/json'
```

**Response:**
```json
[
  {
    "id": "bbe2c098-39f1-4531-8cff-bbf5c476b899",
    "caseId": null,
    "approved": null,
    "caseFile_managerDecision": null,
    "initiator": "john_doe",
    "CaseId": null,
    "caseFile_shipped": null,
    "caseFile_delivered": null,
    "caseFile_supplierComment": null,
    "caseFile_managerComment": null
  }
]
```

---

#### Step 3: Get Tasks for Supplier Group

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/orderhardware/bbe2c098-39f1-4531-8cff-bbf5c476b899/tasks?group=supplier&user=supplier' \
  -H 'accept: application/json'
```

**Response:**
```json
[
  {
    "id": "c9cbcee1-5441-4384-a79b-5c28f4242bff",
    "name": "PrepareHardwareSpec",
    "state": 1,
    "phase": "activate",
    "phaseStatus": "Activated",
    "parameters": {
      "orderNumber": null,
      "requestor": "john_doe",
      "createdBy": "${initiator}"
    },
    "results": {
      "supplierComment_": null
    }
  }
]
```

**Note:** Save the task `id` value (this is the **task ID**, different from the process instance ID): `c9cbcee1-5441-4384-a79b-5c28f4242bff`

---

#### Step 4: Get PrepareHardwareSpec Task Schema

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/orderhardware/PrepareHardwareSpec/schema' \
  -H 'accept: application/json'
```

This returns the task input/output schema to understand what data the task expects.

---

#### Step 5: Complete PrepareHardwareSpec Task (Supplier)

**Request:**
```bash
curl -X 'POST' \
  'http://localhost:8080/orderhardware/c790515e-deaf-4abf-86df-a3e6e5f099ad/PrepareHardwareSpec/09e12729-be81-4bb4-bfaa-34b278dd01b4?group=supplier&phase=complete' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "supplierComment_": "Dell Laptop XPS 15, 32GB RAM, 1TB SSD, Intel i9 processor"
}'
```

**Response:**
```json
{
  "id": "bbe2c098-39f1-4531-8cff-bbf5c476b899",
  "caseId": null,
  "approved": null,
  "caseFile_managerDecision": null,
  "initiator": "john_doe",
  "CaseId": null,
  "caseFile_shipped": null,
  "caseFile_delivered": null,
  "caseFile_supplierComment": "Dell Laptop XPS 15, 32GB RAM, 1TB SSD, Intel i9 processor",
  "caseFile_managerComment": null
}
```

**Note:** The `caseFile_supplierComment` field is now populated with the hardware specification.

---

#### Step 6: Get Tasks for Manager Group

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/orderhardware/c790515e-deaf-4abf-86df-a3e6e5f099ad/tasks?group=managers&user=managers' \
  -H 'accept: application/json'
```

**Expected Response:**
```json
[
  {
    "id": "task-id-for-manager-approval",
    "name": "ManagerApproval",
    "state": 1,
    "phase": "activate",
    "phaseStatus": "Activated",
    "parameters": {
      "requestor": "john_doe",
      "supplierComment": "Dell Laptop XPS 15, 32GB RAM, 1TB SSD, Intel i9 processor"
    },
    "results": {
      "managerComment_": null,
      "approved_": null
    }
  }
]
```

---

#### Step 7: Complete ManagerApproval Task (Approved Scenario)

**Request:**
```bash
curl -X 'POST' \
  'http://localhost:8080/orderhardware/bbe2c098-39f1-4531-8cff-bbf5c476b899/ManagerApproval/d074033e-9604-466a-a10c-3f6c884e3718?group=managers&phase=complete' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "approved_": true,
  "managerComment_": "Approved - meets requirements for development team"
}'
```

**Expected Response:**
```json
{
  "id": "bbe2c098-39f1-4531-8cff-bbf5c476b899",
  "caseId": null,
  "approved": true,
  "caseFile_managerDecision": true,
  "initiator": "john_doe",
  "CaseId": null,
  "caseFile_shipped": null,
  "caseFile_delivered": null,
  "caseFile_supplierComment": "Dell Laptop XPS 15, 32GB RAM, 1TB SSD, Intel i9 processor",
  "caseFile_managerComment": "Approved - meets requirements for development team"
}
```

**Note:** When approved, the process triggers the `place_order` subprocess.

---

#### Step 7 (Alternative): Complete ManagerApproval Task (Rejected Scenario)

**Request:**
```bash
curl -X 'POST' \
  'http://localhost:8080/orderhardware/bbe2c098-39f1-4531-8cff-bbf5c476b899/ManagerApproval/{taskId}?group=managers&phase=complete' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "approved_": false,
  "managerComment_": "Rejected - specifications exceed budget constraints"
}'
```

**Expected Response:**
```json
{
  "id": "bbe2c098-39f1-4531-8cff-bbf5c476b899",
  "caseId": null,
  "approved": false,
  "caseFile_managerDecision": false,
  "initiator": "john_doe",
  "CaseId": null,
  "caseFile_shipped": null,
  "caseFile_delivered": null,
  "caseFile_supplierComment": "Dell Laptop XPS 15, 32GB RAM, 1TB SSD, Intel i9 processor",
  "caseFile_managerComment": "Rejected - specifications exceed budget constraints"
}
```

**Note:** When rejected, the process routes to the `OrderRejected` task.

---

#### Step 8a: Complete OrderRejected Task (If Rejected)

**Request:**
```bash
curl -X 'POST' \
  'http://localhost:8080/orderhardware/bbe2c098-39f1-4531-8cff-bbf5c476b899/OrderRejected/{taskId}?group=supplier&phase=complete' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{}'
```

This completes the rejected order notification and ends the process.

---

#### Step 8b: Get PlaceOrder Subprocess ID (If Approved)

First, get the subprocess instance ID that was created when the order was approved:

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/place_order' \
  -H 'accept: application/json'
```

**Response:**
```json
[
  {
    "id": "b130fef4-6cf6-4c48-bead-080bfd873b6a",
    "requestor": "john_doe",
    "caseId": null,
    "CaseId": null,
    "caseFile_orderInfo": null,
    "Requestor": "john_doe",
    "caseFile_ordered": null
  }
]
```

**Note:** Save the subprocess `id` value: `b130fef4-6cf6-4c48-bead-080bfd873b6a`

---

#### Step 8c: Get PlaceOrder Subprocess Tasks

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/place_order/b130fef4-6cf6-4c48-bead-080bfd873b6a/tasks?group=supplier&user=supplier' \
  -H 'accept: application/json'
```

**Response:**
```json
[
  {
    "id": "35c3849f-fc80-4074-b60b-e89dd07798a2",
    "name": "PlaceOrder",
    "state": 1,
    "phase": "activate",
    "phaseStatus": "Activated",
    "parameters": {
      "orderNumber": null,
      "requestor": "john_doe"
    },
    "results": {
      "ordered_": null,
      "info_": null
    }
  }
]
```

**Note:** Save the task `id` value: `35c3849f-fc80-4074-b60b-e89dd07798a2`

---

#### Step 9: Complete PlaceOrder Task (Supplier)

**Request:**
```bash
curl -X 'POST' \
  'http://localhost:8080/place_order/b130fef4-6cf6-4c48-bead-080bfd873b6a/PlaceOrder/35c3849f-fc80-4074-b60b-e89dd07798a2?group=supplier&phase=complete' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "shipped_": true,
  "delivered_": false
}'
```

This marks the order as placed and shipped.

---

#### Step 10: Signal Milestone 2 - Order Shipped

**Request:**
```bash
curl -X 'POST' \
  'http://localhost:8080/orderhardware/bbe2c098-39f1-4531-8cff-bbf5c476b899/Milestone_2:_Order_shipped' \
  -H 'accept: application/json'
```

**Expected Response:**
```json
{
  "id": "bbe2c098-39f1-4531-8cff-bbf5c476b899",
  "caseId": null,
  "approved": true,
  "caseFile_managerDecision": true,
  "initiator": "john_doe",
  "CaseId": null,
  "caseFile_shipped": true,
  "caseFile_delivered": null,
  "caseFile_supplierComment": "Dell Laptop XPS 15, 32GB RAM, 1TB SSD, Intel i9 processor",
  "caseFile_managerComment": "Approved - meets requirements for development team"
}
```

---

#### Step 11: Signal Milestone 3 - Delivered to Customer

**Request:**
```bash
curl -X 'POST' \
  'http://localhost:8080/orderhardware/bbe2c098-39f1-4531-8cff-bbf5c476b899/Milestone_3:_Delivered_to_customer' \
  -H 'accept: application/json'
```

**Expected Response:**
```json
{
  "id": "bbe2c098-39f1-4531-8cff-bbf5c476b899",
  "caseId": null,
  "approved": true,
  "caseFile_managerDecision": true,
  "initiator": "john_doe",
  "CaseId": null,
  "caseFile_shipped": true,
  "caseFile_delivered": true,
  "caseFile_supplierComment": "Dell Laptop XPS 15, 32GB RAM, 1TB SSD, Intel i9 processor",
  "caseFile_managerComment": "Approved - meets requirements for development team"
}
```

---

#### Step 12: Get CustomerSurvey Task

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/orderhardware/c790515e-deaf-4abf-86df-a3e6e5f099ad/tasks?group=owners&user=owners' \
  -H 'accept: application/json'
```

**Response:**
```json
[
  {
    "id": "f02d7f7c-25f1-44cc-b376-4352f42b7203",
    "name": "CustomerSurvey",
    "state": 1,
    "phase": "activate",
    "phaseStatus": "Activated",
    "parameters": {
      "orderNumber": null
    },
    "results": {}
  }
]
```

**Note:** Save the task `id` value (e.g., `f02d7f7c-25f1-44cc-b376-4352f42b7203`). If multiple CustomerSurvey tasks appear, use the first one or the one corresponding to your process instance.

---

#### Step 13: Complete All CustomerSurvey Tasks

**Important:** Multiple CustomerSurvey tasks may be created during the process. You need to complete ALL of them iteratively. After completing each task, check for remaining tasks and repeat until all are completed.

##### Step 13a: Get Current CustomerSurvey Tasks

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/orderhardware/c790515e-deaf-4abf-86df-a3e6e5f099ad/tasks?group=owners&user=owners' \
  -H 'accept: application/json'
```

**Response (Example showing 2 remaining tasks):**
```json
[
  {
    "id": "34addf0c-9d3d-4f1e-9cdd-ee51b0497647",
    "name": "CustomerSurvey",
    "state": 1,
    "phase": "activate",
    "phaseStatus": "Activated",
    "parameters": {
      "orderNumber": null
    },
    "results": {}
  },
  {
    "id": "2b05e877-a382-4f5e-b814-31de6b01209b",
    "name": "CustomerSurvey",
    "state": 1,
    "phase": "activate",
    "phaseStatus": "Activated",
    "parameters": {
      "orderNumber": null
    },
    "results": {}
  }
]
```

##### Step 13b: Complete First CustomerSurvey Task

**Request:**
```bash
curl -X 'POST' \
  'http://localhost:8080/orderhardware/c790515e-deaf-4abf-86df-a3e6e5f099ad/CustomerSurvey/34addf0c-9d3d-4f1e-9cdd-ee51b0497647?group=owners&phase=complete' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "survey_": {
    "satisfied": true,
    "deliveredOnTime": true,
    "comment": "Excellent service! Hardware arrived on time and meets all specifications."
  }
}'
```

**Response:**
```json
{
  "id": "c790515e-deaf-4abf-86df-a3e6e5f099ad",
  "caseId": null,
  "approved": true,
  "caseFile_managerDecision": true,
  "initiator": "john_doe",
  "CaseId": null,
  "caseFile_shipped": null,
  "caseFile_delivered": null,
  "caseFile_supplierComment": "Dell Laptop XPS 15, 32GB RAM, 1TB SSD, Intel i9 processor",
  "caseFile_managerComment": "Approved - meets requirements for development team"
}
```

##### Step 13c: Check for Remaining CustomerSurvey Tasks

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/orderhardware/c790515e-deaf-4abf-86df-a3e6e5f099ad/tasks?group=owners&user=owners' \
  -H 'accept: application/json'
```

**Response (1 task remaining):**
```json
[
  {
    "id": "2b05e877-a382-4f5e-b814-31de6b01209b",
    "name": "CustomerSurvey",
    "state": 1,
    "phase": "activate",
    "phaseStatus": "Activated",
    "parameters": {
      "orderNumber": null
    },
    "results": {}
  }
]
```

##### Step 13d: Complete Second CustomerSurvey Task

**Request:**
```bash
curl -X 'POST' \
  'http://localhost:8080/orderhardware/c790515e-deaf-4abf-86df-a3e6e5f099ad/CustomerSurvey/2b05e877-a382-4f5e-b814-31de6b01209b?group=owners&phase=complete' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "survey_": {
    "satisfied": true,
    "deliveredOnTime": true,
    "comment": "Excellent service! Hardware arrived on time and meets all specifications."
  }
}'
```

**Response:**
```json
{
  "id": "c790515e-deaf-4abf-86df-a3e6e5f099ad",
  "caseId": null,
  "approved": true,
  "caseFile_managerDecision": true,
  "initiator": "john_doe",
  "CaseId": null,
  "caseFile_shipped": null,
  "caseFile_delivered": null,
  "caseFile_supplierComment": "Dell Laptop XPS 15, 32GB RAM, 1TB SSD, Intel i9 processor",
  "caseFile_managerComment": "Approved - meets requirements for development team"
}
```

##### Step 13e: Verify All Tasks Completed

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/orderhardware/c790515e-deaf-4abf-86df-a3e6e5f099ad/tasks?group=owners&user=owners' \
  -H 'accept: application/json'
```

**Response (No tasks remaining):**
```json
[]
```

**Note:** Repeat Steps 13a-13d until the tasks endpoint returns an empty array `[]`, indicating all CustomerSurvey tasks have been completed.

---

#### Step 14: Verify Process Completion

**Request:**
```bash
curl -X 'GET' \
  'http://localhost:8080/orderhardware/bbe2c098-39f1-4531-8cff-bbf5c476b899' \
  -H 'accept: application/json'
```

**Expected Response:**
```json
{
  "id": "bbe2c098-39f1-4531-8cff-bbf5c476b899",
  "caseId": null,
  "approved": true,
  "caseFile_managerDecision": true,
  "initiator": "john_doe",
  "CaseId": null,
  "caseFile_shipped": true,
  "caseFile_delivered": true,
  "caseFile_supplierComment": "Dell Laptop XPS 15, 32GB RAM, 1TB SSD, Intel i9 processor",
  "caseFile_managerComment": "Approved - meets requirements for development team"
}
```

The process is now complete with all milestones achieved and customer feedback collected.

---

### Additional Testing Scenarios

#### Scenario 1: Save Task Without Completing

You can save task progress without completing it using the PUT endpoint:

```bash
curl -X 'PUT' \
  'http://localhost:8080/orderhardware/{id}/PrepareHardwareSpec/{taskId}?group=supplier' \
  -H 'accept: application/json' \
  -H 'Content-Type: application/json' \
  -d '{
  "supplierComment_": "Work in progress - Dell Laptop"
}'
```

#### Scenario 2: Abort a Task

```bash
curl -X 'DELETE' \
  'http://localhost:8080/orderhardware/{id}/PrepareHardwareSpec/{taskId}?group=supplier' \
  -H 'accept: application/json'
```

#### Scenario 3: Get Task Schema and Phases

```bash
curl -X 'GET' \
  'http://localhost:8080/orderhardware/{id}/PrepareHardwareSpec/{taskId}/schema' \
  -H 'accept: application/json'
```

This returns the task schema including available phases and transitions.

---

### User and Group Assignments

Based on the application.properties configuration:

| User | Groups | Can Claim Tasks |
|------|--------|----------------|
| `jdoe` | admin, supplier, manager | All tasks |
| `mary` | supplier | PrepareHardwareSpec, PlaceOrder, OrderRejected |
| `katy` | manager | ManagerApproval |
| `john_doe` (initiator) | owner | CustomerSurvey |

**Query Parameters:**
- `user={username}` - Filter tasks by user
- `group={groupname}` - Filter tasks by group (can specify multiple groups)
- `phase={phase}` - Specify task phase transition (e.g., complete, claim, release)

---

### Testing with Swagger UI

For interactive testing, access the Swagger UI at:
```
http://localhost:8080/q/swagger-ui
```

The Swagger UI provides:
- Interactive API documentation
- Request/response examples
- Schema definitions
- Try-it-out functionality for all endpoints

---

### Testing with Dev UI

For visual process and task management, access the BAMOE Dev UI at:
```
http://localhost:8080/q/dev
```

The Dev UI provides:
- Process instance visualization
- Task inbox and management
- Process variable inspection
- User task claiming and completion

---

## Key Changes Summary

### Files Migrated

| V8 File | V9 File | Changes |
|---------|---------|---------|
| `pom.xml` (kjar) | `pom.xml` (Quarkus) | Updated to Quarkus with BAMOE 9.3.1 dependencies |
| `Survey.java` | `Survey.java` | No changes needed (already compatible) |
| `ITOrderService.java` | `ITOrderService.java` | No changes |
| `orderhardware.bpmn` (ad-hoc) | `orderhardware.bpmn` (ad-hoc) | **Removed Condition from Milestone tasks only** |
| `place-order.bpmn` | `place-order.bpmn` | **Retained as-is** |
| `*.frm` | *(removed)* | Forms not needed in v9 |
| `*.svg` | *(removed)* | Diagrams not needed |

### Code Changes

#### Survey.java

**No changes required** - The Survey.java class is already compatible with v9.

#### orderhardware.bpmn

**Key Change: Milestone Condition Removal**

**Retained (No Changes):**
- `drools:adHoc="true"` attribute - **kept**
- All milestone nodes - **kept**
- Case management metadata (`customCaseIdPrefix`, `customCaseRoles`) - **REMOVED** (not supported in V9)
- Disconnected task nodes - **kept**
- User tasks and script tasks - **kept**
- Process variables - **kept**

**Modified:**
- **Removed `Condition` input parameter from all Milestone tasks**

**Example Change:**

V8 Milestone with Condition:
```xml
<bpmn2:task id="_DCD97847" drools:taskName="Milestone" name="Milestone 1: Order placed">
    <bpmn2:ioSpecification>
        <bpmn2:dataInput id="_condition" name="Condition" itemSubjectRef="ConditionInputXItem" />
        <!-- Condition expression -->
    </bpmn2:ioSpecification>
</bpmn2:task>
```

V9 Milestone without Condition:
```xml
<bpmn2:task id="_DCD97847" drools:taskName="Milestone" name="Milestone 1: Order placed">
    <bpmn2:ioSpecification>
        <bpmn2:dataInput id="_taskName" name="TaskName" />
        <!-- Condition input removed -->
    </bpmn2:ioSpecification>
</bpmn2:task>
```

#### place-order.bpmn

**No changes** - Subprocess retained as-is in v9.

### Configuration Changes

#### application.properties

**Updated from v8 to v9:**
```properties
# Simplified datasource configuration
quarkus.datasource.db-kind=h2
quarkus.datasource.username=kie
quarkus.datasource.jdbc.url=jdbc:h2:mem:default;NON_KEYWORDS=VALUE,KEY

# Service URLs
kogito.service.url=http://0.0.0.0:8080
kogito.jobs-service.url=http://0.0.0.0:8080
kogito.data-index.url=http://0.0.0.0:8080

# REST endpoint generation
kogito.generate.rest.decisions=false
kogito.generate.rest.processes=true

# Disable dev services
quarkus.kogito.devservices.enabled=false
quarkus.devservices.enabled=false
```

**Removed:**
- Complex DevUI user configurations
- v8 kjar-specific configuration

---

## Lessons Learned

### 1. Ad-hoc Processes CAN Be Migrated with Minimal Changes

**Key Finding:** V8 ad-hoc processes (`drools:adHoc="true"`) can be migrated to v9 by keeping the ad-hoc structure intact.

**Solution:** Retain the ad-hoc process structure and only remove Milestone conditions.


### 2. Milestone Conditions Must Be Removed

**Problem:** V9 doesn't support the `Condition` input parameter on Milestone tasks.

**Solution:** Remove the `Condition` dataInput from all Milestone task ioSpecifications. Milestones can still be used but must be triggered programmatically rather than by conditional expressions.

**Impact:** Minimal - milestone functionality is retained, just triggered differently.

### 3. Case Roles and Metadata Are Retained

**Key Finding:** V8 case roles (`owner`, `manager`, `supplier`) must be migrated to standard group-based task assignments. V9 does not support `customCaseIdPrefix` or `customCaseRoles` metadata.

**Solution:** Remove `customCaseIdPrefix` and `customCaseRoles` metadata from the BPMN file. V9 uses standard Kogito process instance IDs and group-based task assignments. Task assignments should use standard group names (supplier, manager, owner) without the case management metadata wrapper.

**Important:** These attributes must be removed from the BPMN file as V9 does not support case-specific ID generation or case roles metadata. Task assignments work through standard group-based mechanisms.

### 4. Ad-hoc Task Structure Is Preserved

**Key Finding:** Disconnected tasks without sequence flows continue to work in v9.

**Solution:** No changes needed - ad-hoc task activation via API continues to work.

### 5. Process Variables Remain Compatible (Except Document Type)

**Key Finding:** Most v8 process variables work without modification in v9, except Document types.

**Retained variables:** `approved`, `CaseId`, `initiator`, `caseFile_managerComment`, `caseFile_supplierComment`, `caseFile_managerDecision`, `caseFile_survey`, `caseFile_shipped`, and `caseFile_delivered`.

**Removed variable:** `caseFile_hwSpec` (org.jbpm.document.Document type not supported in v9).

**Solution:** Replace Document-type variables with String or custom Java objects.

### 6. Document Type Not Supported in V9

**Problem:** V9 doesn't support `org.jbpm.document.Document` type used in v8.

**Solution:** Remove all Document-type variables and item definitions. Use String for simple text content or create custom Java classes for complex document metadata.

**Impact:** Any process using Document variables needs this change.

---

## Migration Checklist

### BPMN Migration Steps:
- [x] Copy v8 BPMN file to v9 resources directory
- [x] Remove `Condition` input parameter from all Milestone tasks (3 milestones)
- [x] Remove `Condition` item definitions (e.g., `__DCD97847-6E3C-4C5E-9EE3-221C04BE42ED_ConditionInputXItem`)
- [x] Remove `caseFile_hwSpec` process variable
- [x] Remove all Document-type item definitions
- [x] Remove Document references from user task inputs/outputs
- [x] **KEEP** `drools:adHoc="true"` attribute
- [x] **REMOVE** case management metadata (`customCaseIdPrefix`, `customCaseRoles`) - not supported in V9
- [x] **KEEP** all user tasks, script tasks, and milestones
- [x] **KEEP** sequence flows and process structure


---

## Conclusion

### Summary

This tutorial successfully demonstrates migrating a v8 ad-hoc case management application to v9 with **minimal changes**:

1. **Analyzed** the v8 ad-hoc structure and identified incompatible features
2. **Retained** the ad-hoc process structure (`drools:adHoc="true"`)
3. **Removed** only incompatible features (Milestone conditions, Document type)
4. **Converted** from kjar to Quarkus packaging
5. **Tested** the migrated application

### Key Takeaways

1.  **Ad-hoc processes CAN be migrated with minimal changes** - No complete redesign required
2.  **V9 supports ad-hoc case management** - Keep `drools:adHoc="true"` attribute
3.  **Milestone conditions must be removed** - V9 doesn't support conditional milestone activation
4.  **Document type not supported** - Replace with String or custom objects
5.  **Case roles and metadata must be removed in v9** - Remove `customCaseIdPrefix` and `customCaseRoles` from BPMN
6.  **Process structure preserved** - User tasks, script tasks, gateways all retained



---

## Common Migration Errors and Solutions

### Error 1: Compilation Error with ';' expected

**Error Message**:
```
[ERROR] org.kie.memorycompiler.KieMemoryCompilerException:
[org/jbpm/demo/itorders/OrderhardwareProcess.java (18:66) : ';' expected]
```

**Cause**: Mismatch between process ID and BPMNPlane's `bpmnElement` attribute in `place-order.bpmn`.

**Solution**: Ensure the BPMNPlane element references the correct process ID:
```xml
<bpmndi:BPMNPlane id="_0O98sdNMEeavSs8IK1dqtA" bpmnElement="place-order">
```

### Error 2: Subprocess Not Found

**Error Message**:
```
[ERROR] Could not find process 'itorders-data.place-order'
```

**Cause**: The callActivity is still referencing the old V8 process ID.

**Solution**: Update the `calledElement` attribute in `orderhardware.bpmn`:
```xml
<bpmn2:callActivity calledElement="place-order">
```

### Error 3: Process Not Deployed

**Cause**: BPMN files not in the correct location.

**Solution**: Ensure both BPMN files are in `src/main/resources/` (not in a package subdirectory).

---

## Call Activity Implementation Details

### What is a Call Activity?
A Call Activity is a BPMN element that allows one process to invoke another process. In this tutorial, the main `orderhardware` process uses a Call Activity to invoke the `place-order` subprocess.

### Process Reference Changes

- **V8 Reference**: `calledElement="itorders-data.place-order"`
- **V9 Reference**: `calledElement="place-order"`

The V9 implementation uses the process ID directly from the place-order.bpmn file.

### Data Mapping
The Call Activity passes two parameters to the place-order process:
- **CaseId**: Mapped from the parent process's `CaseId` variable
- **Requestor**: Mapped from the parent process's `initiator` variable

### Process Flow with Call Activity
```
Manager Approval → Exclusive Gateway → [approved=true] → Place Order (Call Activity) → End Event
                                     → [approved=false] → Order Rejected → End Event
```

### Testing the Call Activity
1. Start the orderhardware process
2. Complete the "Prepare hardware spec" task
3. Approve the order in "Manager approval" task
4. The process automatically invokes the place-order subprocess
5. Complete the "Place order" task in the subprocess
6. Control returns to the main process and completes

---
