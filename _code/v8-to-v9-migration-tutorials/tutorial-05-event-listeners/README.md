# Tutorial 05: Event Listeners Migration from BAMOE v8 to v9

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

---

## Introduction

This tutorial demonstrates migrating **Event Listeners** from BAMOE v8 to v9.

### Key Changes: v8 vs v9

| Aspect | BAMOE v8 | BAMOE v9 |
|--------|----------|----------|
| **Registration** | XML (`kie-deployment-descriptor.xml`) | Annotation-based (`@ApplicationScoped`) |
| **Process Listener** | `ProcessEventListener` | `DefaultKogitoProcessEventListener` |
| **Task Listener** | `TaskLifeCycleEventListener` | `UserTaskEventListener` |
| **Rule Listener** | `AgendaEventListener` | `DefaultAgendaEventListener` + `RuleEventListenerConfig` |

---

## Prerequisites

- Java 17+
- Maven 3.8.1+
- BAMOE 9.3.1+

---

## Step-by-Step Migration

### Step 1: Remove XML Configuration

**Delete**: `src/main/resources/META-INF/kie-deployment-descriptor.xml`

Event listeners are now registered automatically using `@ApplicationScoped` annotation.

### Step 2: Migrate Process Event Listener

#### What Changed

1. **Registration**: Add `@ApplicationScoped` annotation instead of XML configuration
2. **Base class**: Extend `DefaultKogitoProcessEventListener` instead of implementing `ProcessEventListener`
3. **Imports**: Update to use `jakarta.enterprise.context.ApplicationScoped`
4. **Variable events**: Access both old and new values via `event.getOldValue()` and `event.getNewValue()`
5. **New methods**: v9 adds `onSignal()`, `onMessage()`, `onError()`, `onMigration()`, `onProcessStateChanged()`, `onNodeStateChanged()`

#### Migration Steps

**Step 2.1: Update Class Declaration**

In v8, your listener implements the interface:
```java
public class ProcessEventLogger implements ProcessEventListener {
```

In v9, extend the base class and add CDI annotation:
```java
@ApplicationScoped
public class ProcessEventLogger extends DefaultKogitoProcessEventListener {
```

**Step 2.2: Update Package Imports**

Replace v8 imports:
```java
import org.kie.api.event.process.ProcessEventListener;
```

With v9 imports:
```java
import jakarta.enterprise.context.ApplicationScoped;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
```

**Step 2.3: Update Variable Change Event Handling**

In v8, you access the new value from the event:
```java
public void afterVariableChanged(ProcessVariableChangedEvent event) {
    Object newValue = event.getNewValue();
}
```

In v9, you can access both old and new values:
```java
@Override
public void afterVariableChanged(ProcessVariableChangedEvent event) {
    Object oldValue = event.getOldValue();
    Object newValue = event.getNewValue();
    // Compare old vs new to detect what changed
}
```

**Step 2.4: Implement New v9 Methods (Optional)**

v9 adds new lifecycle methods you can override:
- `onSignal()` - Process received a signal
- `onMessage()` - Process received a message
- `onError()` - Error occurred during process execution
- `onMigration()` - Process instance was migrated
- `onProcessStateChanged()` - Process state changed
- `onNodeStateChanged()` - Node state changed

**See**: [`ProcessEventLogger.java`](v9-app/src/main/java/com/example/listeners/ProcessEventLogger.java) for complete implementation

### Step 3: Migrate User Task Event Listener

#### What Changed

1. **Interface**: Replace `TaskLifeCycleEventListener` with `UserTaskEventListener`
2. **Method pattern**: Single callback methods instead of before/after pairs
3. **Event objects**: Events contain both old and new states
4. **Registration**: Use `@ApplicationScoped` annotation

#### BAMOE 8 to BAMOE 9 Method Mapping

| BAMOE 8 Method(s) | BAMOE 9 Method | State Transition |
|-------------------|----------------|------------------|
| `beforeTaskClaimedEvent` / `afterTaskClaimedEvent` | `onUserTaskState` | Ready → Reserved |
| `beforeTaskStartedEvent` / `afterTaskStartedEvent` | `onUserTaskState` | Reserved → InProgress |
| `beforeTaskCompletedEvent` / `afterTaskCompletedEvent` | `onUserTaskState` | InProgress → Completed |
| `beforeTaskDelegatedEvent` / `afterTaskDelegatedEvent` | `onUserTaskAssignment` | Assignment change |
| `beforeTaskInputVariableChangedEvent` / `afterTaskInputVariableChangedEvent` | `onUserTaskInputVariable` | Variable change |
| `beforeTaskOutputVariableChangedEvent` / `afterTaskOutputVariableChangedEvent` | `onUserTaskOutputVariable` | Variable change |

#### BAMOE 8 to BAMOE 9 TaskInstanceView Field Mapping

When accessing task data from events, the field names have changed:

| BAMOE 8 Field | BAMOE 9 Field | Description |
|---------------|---------------|-------------|
| `event.getTask().getId()` | `event.getUserTaskInstance().getId()` | Task ID |
| `event.getTask().getName()` | `event.getUserTaskInstance().getTaskName()` | Task name |
| `event.getTask().getDescription()` | `event.getUserTaskInstance().getTaskDescription()` | Task description |
| `event.getTask().getPriority()` | `event.getUserTaskInstance().getTaskPriority()` | Task priority |
| `event.getTask().getTaskData().getActualOwner()` | `event.getUserTaskInstance().getActualOwner()` | Current owner |
| `event.getTask().getPeopleAssignments().getPotentialOwners()` | `event.getUserTaskInstance().getPotentialUsers()` | Potential owners (users) |
| N/A | `event.getUserTaskInstance().getPotentialGroups()` | Potential owners (groups) |
| `event.getTask().getPeopleAssignments().getExcludedOwners()` | `event.getUserTaskInstance().getExcludedUsers()` | Excluded users |
| `event.getTask().getPeopleAssignments().getBusinessAdministrators()` | `event.getUserTaskInstance().getAdminUsers()` | Business administrators (users) |
| N/A | `event.getUserTaskInstance().getAdminGroups()` | Business administrators (groups) |
| `event.getTask().getTaskData().getTaskInputVariables()` | `event.getUserTaskInstance().getInputs()` | Input variables (Map) |
| `event.getTask().getTaskData().getTaskOutputVariables()` | `event.getUserTaskInstance().getOutputs()` | Output variables (Map) |
| `event.getTask().getTaskData().getProcessInstanceId()` | `event.getUserTaskInstance().getProcessInfo().getProcessInstanceId()` | Process instance ID |
| `event.getTask().getTaskData().getProcessId()` | `event.getUserTaskInstance().getProcessInfo().getProcessId()` | Process definition ID |
| N/A | `event.getUserTaskInstance().getProcessInfo().getProcessVersion()` | Process version |
| N/A | `event.getUserTaskInstance().getAttachments()` | Task attachments |
| N/A | `event.getUserTaskInstance().getComments()` | Task comments |
| N/A | `event.getUserTaskInstance().getMetadata()` | Task metadata |
| N/A | `event.getUserTaskInstance().getSlaDueDate()` | SLA due date |

#### Migration Steps

**Step 3.1: Update Class Declaration**

In v8, your listener implements the interface:
```java
public class TaskLifeCycleListener implements TaskLifeCycleEventListener {
```

In v9, implement the new interface and add CDI annotation:
```java
@ApplicationScoped
public class UserTaskEventLogger implements UserTaskEventListener {
```

**Step 3.2: Update Package Imports**

Replace v8 imports:
```java
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.TaskEvent;
```

With v9 imports:
```java
import jakarta.enterprise.context.ApplicationScoped;
import org.kie.kogito.usertask.UserTaskEventListener;
import org.kie.kogito.usertask.events.UserTaskStateEvent;
import org.kie.kogito.usertask.events.UserTaskAssignmentEvent;
import org.kie.kogito.usertask.events.UserTaskVariableEvent;
```

**Step 3.3: Replace Before/After Method Pairs with Single Callbacks**

In v8, you had separate before/after methods:
```java
@Override
public void beforeTaskClaimedEvent(TaskEvent event) {
    // Before task is claimed
}

@Override
public void afterTaskClaimedEvent(TaskEvent event) {
    // After task is claimed
}
```

In v9, use a single callback that provides both old and new states:
```java
@Override
public void onUserTaskState(UserTaskStateEvent event) {
    String oldStatus = event.getOldStatus() != null ? event.getOldStatus().getName() : null;
    String newStatus = event.getNewStatus() != null ? event.getNewStatus().getName() : null;
    
    // Detect specific transition
    if ("Ready".equals(oldStatus) && "Reserved".equals(newStatus)) {
        // Task was claimed
    }
}
```

**Step 3.4: Migrate Assignment Events**

In v8:
```java
@Override
public void afterTaskDelegatedEvent(TaskEvent event) {
    String newOwner = event.getTask().getTaskData().getActualOwner().getId();
}
```

In v9:
```java
@Override
public void onUserTaskAssignment(UserTaskAssignmentEvent event) {
    Set<String> oldUsers = event.getOldUsersId();
    Set<String> newUsers = event.getNewUsersId();
    // Compare old vs new to detect assignment changes
}
```

**Step 3.5: Migrate Variable Events**

In v8, variable events were part of TaskLifeCycleEventListener (if supported).

In v9, use dedicated variable callbacks:
```java
@Override
public void onUserTaskInputVariable(UserTaskVariableEvent event) {
    String variableName = event.getVariableName();
    Object oldValue = event.getOldValue();
    Object newValue = event.getNewValue();
}

@Override
public void onUserTaskOutputVariable(UserTaskVariableEvent event) {
    String variableName = event.getVariableName();
    Object oldValue = event.getOldValue();
    Object newValue = event.getNewValue();
}
```

**Step 3.6: Access Task Details**

In v8, you accessed task data through TaskEvent:
```java
event.getTask().getId()
event.getTask().getName()
event.getTask().getTaskData().getActualOwner()
```

In v9, access through UserTaskInstance:
```java
event.getUserTaskInstance().getId()
event.getUserTaskInstance().getTaskName()
event.getUserTaskInstance().getActualOwner()
event.getUserTaskInstance().getPotentialUsers()
event.getUserTaskInstance().getInputs()
event.getUserTaskInstance().getOutputs()
```

**Step 3.7: Complete Example - Accessing Full Task Data**

Here's a complete example showing how to access all task data in v9:

```java
@Override
public void onUserTaskState(UserTaskStateEvent event) {
    // Get the UserTaskInstance from the event
    UserTaskInstance task = event.getUserTaskInstance();
    
    // Assignment data (v8: potentialOwners, excludedOwners, businessAdmins)
    LOGGER.info("Potential Users: {}", task.getPotentialUsers());
    LOGGER.info("Potential Groups: {}", task.getPotentialGroups());
    LOGGER.info("Excluded Users: {}", task.getExcludedUsers());
    LOGGER.info("Admin Users: {}", task.getAdminUsers());
    LOGGER.info("Admin Groups: {}", task.getAdminGroups());
    
    // Variable data (v8: inputData, outputData)
    LOGGER.info("Inputs: {}", task.getInputs());
    LOGGER.info("Outputs: {}", task.getOutputs());
    
    // Task metadata
    LOGGER.info("Task ID: {}", task.getId());
    LOGGER.info("Task Name: {}", task.getTaskName());
    LOGGER.info("Task Description: {}", task.getTaskDescription());
    LOGGER.info("Task Priority: {}", task.getTaskPriority());
    
    // Process context
    ProcessInfo info = task.getProcessInfo();
    LOGGER.info("Process Instance ID: {}", info.getProcessInstanceId());
    LOGGER.info("Process ID: {}", info.getProcessId());
    LOGGER.info("Process Version: {}", info.getProcessVersion());
    
    // Current assignment
    LOGGER.info("Actual Owner: {}", task.getActualOwner());
    
    // Additional task data
    LOGGER.info("Attachments: {}", task.getAttachments());
    LOGGER.info("Comments: {}", task.getComments());
    LOGGER.info("Metadata: {}", task.getMetadata());
    LOGGER.info("SLA Due Date: {}", task.getSlaDueDate());
    
    // Status information
    String oldStatus = event.getOldStatus() != null ? event.getOldStatus().getName() : null;
    String newStatus = event.getNewStatus() != null ? event.getNewStatus().getName() : null;
    LOGGER.info("Status transition: {} -> {}", oldStatus, newStatus);
}
```

**See**: [`UserTaskEventLogger.java`](v9-app/src/main/java/com/example/listeners/UserTaskEventLogger.java) for complete implementation

#### Key Points

- **Event timing**: v9 callbacks are invoked after the state change, but provide access to both old and new states
- **Transaction behavior**: Listeners execute within the same transaction - uncaught exceptions roll back the entire operation
- **State detection**: Check old/new status to identify specific transitions (e.g., Ready→Reserved means task was claimed)

### Step 4: Migrate Rule Event Listener

#### What Changed

1. **Listener class**: Extend `DefaultAgendaEventListener`
2. **Configuration class**: Create a `RuleEventListenerConfig` implementation (NEW in v9)
3. **Registration**: Both classes need `@ApplicationScoped` annotation

#### Migration Steps

**Step 4.1: Create the Listener Class**

In v8, your listener implements the interface:
```java
public class RuleEventLogger implements AgendaEventListener {
    @Override
    public void matchCreated(MatchCreatedEvent event) {
        // Handle match created
    }
    // ... other methods
}
```

In v9, extend the base class and add CDI annotation:
```java
@ApplicationScoped
public class RuleEventLogger extends DefaultAgendaEventListener {
    @Override
    public void matchCreated(MatchCreatedEvent event) {
        // Handle match created
    }
    // ... other methods
}
```

**Step 4.2: Update Package Imports**

Replace v8 imports:
```java
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.MatchCreatedEvent;
```

With v9 imports:
```java
import jakarta.enterprise.context.ApplicationScoped;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.kie.api.event.rule.MatchCreatedEvent;
```

**Step 4.3: Create Configuration Class (NEW in v9)**

This is a new requirement in v9. You must create a configuration class that registers your listener:

```java
@ApplicationScoped
public class CustomRuleEventListenerConfig implements RuleEventListenerConfig {
    
    @Inject
    RuleEventLogger ruleEventLogger;
    
    @Override
    public List<AgendaEventListener> agendaListeners() {
        return Collections.singletonList(ruleEventLogger);
    }
}
```

**Step 4.4: Update Configuration Imports**

Add these imports for the configuration class:
```java
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.kogito.rules.RuleEventListenerConfig;
import java.util.Collections;
import java.util.List;
```
**Why Two Classes?**

In v9, rule listeners require both:
1. **Listener class** - Contains the actual event handling logic
2. **Configuration class** - Registers the listener with the rule engine

This separation allows for better dependency injection and configuration management.


### Step 5:Generate Form Code**

Use BAMOE Developer Tools to generate TypeScript React forms:

1. Open Command Palette: `Cmd+Shift+P` (Mac) or `Ctrl+Shift+P` (Windows/Linux)
2. Type: "BAMOE Developer Tools: Generate form code for User Tasks"

### Step 6: Run in Development Mode
```bash
mvn clean install
```

```bash
mvn quarkus:dev -Pdevelopment
```

If you are using Gradle, use the following commands:

```bash
gradle clean build
```

```shell script
gradle clean quarkusDev
```

### Best Practices

1. **Use `@ApplicationScoped`** - Required for automatic CDI registration
2. **Handle exceptions gracefully** - Wrap listener logic in try-catch to prevent transaction rollback
3. **Keep operations lightweight** - Listeners execute synchronously within the transaction
4. **Use async processing for heavy operations** - Inject `ManagedExecutor` and submit heavy tasks asynchronously

---

## Testing

### Test 1: Start a Process Instance

**Request**:
```bash
curl -X POST http://localhost:8080/testProcess \
  -H "Content-Type: application/json" \
  -d '{"message": "hello"}'
```

**Response**:
```json
{
  "id": "c43d9d7a-6f7f-4cfa-9a4d-7f97296c79c7",
  "message": "hello",
  "result":null
}
```

**Expected Logs**:
```
2026-02-18 19:07:01,648 INFO  [com.exa.lis.ProcessEventLogger] (executor-thread-1) >>>>> [PROCESS-LISTENER] beforeProcessStarted: processId=testProcess
2026-02-18 19:07:01,649 INFO  [com.exa.lis.ProcessEventLogger] (executor-thread-1) >>>>> [PROCESS-LISTENER] beforeNodeTriggered: nodeName=Start
2026-02-18 19:07:01,650 INFO  [com.exa.lis.UserTaskEventLogger] (executor-thread-1) >>>>> [USERTASK-LISTENER] Task created: ReviewTask
2026-02-18 19:07:01,651 INFO  [com.exa.lis.UserTaskEventLogger] (executor-thread-1) >>>>> [USERTASK-LISTENER] Task claimed: ReviewTask
2026-02-18 19:07:01,652 INFO  [com.exa.lis.ProcessEventLogger] (executor-thread-1) >>>>> [PROCESS-LISTENER] afterNodeTriggered: nodeName=ReviewTask
2026-02-18 19:07:01,653 INFO  [com.exa.lis.ProcessEventLogger] (executor-thread-1) >>>>> [PROCESS-LISTENER] afterNodeLeft: nodeName=Start
2026-02-18 19:07:01,653 INFO  [com.exa.lis.ProcessEventLogger] (executor-thread-1) >>>>> [PROCESS-LISTENER] afterNodeTriggered: nodeName=Start
2026-02-18 19:07:01,655 INFO  [com.exa.lis.ProcessEventLogger] (executor-thread-1) >>>>> [PROCESS-LISTENER] afterProcessStarted: processId=testProcess, state=1
2026-02-18 19:07:01,655 INFO  [org.kie.kog.qua.pro.dev.DevModeWorkflowLogger] (executor-thread-1) Workflow 'testProcess' (c43d9d7a-6f7f-4cfa-9a4d-7f97296c79c7) was started, now 'Active'
```

**What the Logs Show**:
1. Process starts → `beforeProcessStarted`
2. Start node triggers → `beforeNodeTriggered: Start`
3. User task created → `Task created: ReviewTask`
4. User task auto-claimed → `Task claimed: ReviewTask`
5. ReviewTask node triggers → `afterNodeTriggered: ReviewTask`
6. Start node completes → `afterNodeLeft: Start`
7. Process fully started → `afterProcessStarted: state=1`

### Test 2: Get Process Instance Details

**Request**:
```bash
curl -X GET http://localhost:8080/testProcess/2c59348a-cf47-448c-a006-cb99739247a7
```

**Response**:
```json
{"id":"2c59348a-cf47-448c-a006-cb99739247a7",
"result":null,
"message":"hello"}
```

### Test 3: Complete the User Task
Go to Process instance and move to Tasks and complete it.

**Expected Logs**:
```
2026-02-18 19:10:15,123 INFO  [com.exa.lis.UserTaskEventLogger] (executor-thread-2) >>>>> [USERTASK-LISTENER] Task state changed: ReviewTask
2026-02-18 19:10:15,124 INFO  [com.exa.lis.UserTaskEventLogger] (executor-thread-2) >>>>> [USERTASK-LISTENER]   OLD_STATUS=Reserved, NEW_STATUS=Completed
2026-02-18 19:10:15,125 INFO  [com.exa.lis.UserTaskEventLogger] (executor-thread-2) >>>>> [USERTASK-LISTENER] Task output variable changed: result
2026-02-18 19:10:15,126 INFO  [com.exa.lis.UserTaskEventLogger] (executor-thread-2) >>>>> [USERTASK-LISTENER]   OLD_VALUE=null, NEW_VALUE=approved
2026-02-18 19:10:15,127 INFO  [com.exa.lis.ProcessEventLogger] (executor-thread-2) >>>>> [PROCESS-LISTENER] beforeNodeLeft: nodeName=ReviewTask
2026-02-18 19:10:15,128 INFO  [com.exa.lis.ProcessEventLogger] (executor-thread-2) >>>>> [PROCESS-LISTENER] beforeNodeTriggered: nodeName=End
2026-02-18 19:10:15,129 INFO  [com.exa.lis.ProcessEventLogger] (executor-thread-2) >>>>> [PROCESS-LISTENER] beforeProcessCompleted: processId=testProcess
2026-02-18 19:10:15,130 INFO  [com.exa.lis.ProcessEventLogger] (executor-thread-2) >>>>> [PROCESS-LISTENER] afterProcessCompleted: processId=testProcess, state=2
2026-02-18 19:10:15,131 INFO  [org.kie.kog.qua.pro.dev.DevModeWorkflowLogger] (executor-thread-2) Workflow 'testProcess' (c43d9d7a-6f7f-4cfa-9a4d-7f97296c79c7) completed
```

**What the Logs Show**:
1. Task state changes → `OLD_STATUS=Reserved, NEW_STATUS=Completed`
2. Output variable set → `result: null → approved`
3. ReviewTask node completes → `beforeNodeLeft: ReviewTask`
4. End node triggers → `beforeNodeTriggered: End`
5. Process completes → `beforeProcessCompleted` and `afterProcessCompleted: state=2`


---

## Common Issues

### Issue 1: Listeners Not Registered

**Symptom**: No logs appearing

**Solution**: Verify `@ApplicationScoped` annotation is present on all listener classes

### Issue 2: Rule Listener Not Working

**Solution**: Ensure you created both the listener class AND the configuration class

### Issue 3: NullPointerException on getOldStatus()

**Symptom**: `event.getOldStatus()` returns null

**Solution**: Always check for null before accessing old status

---



## Summary

### Migration Checklist

- [ ] Remove `kie-deployment-descriptor.xml`
- [ ] Add `@ApplicationScoped` to all listener classes
- [ ] Change `ProcessEventListener` → `DefaultKogitoProcessEventListener`
- [ ] Change `TaskLifeCycleEventListener` → `UserTaskEventListener`
- [ ] Update task event methods to single-callback pattern
- [ ] Create `RuleEventListenerConfig` for rule listeners
- [ ] Update all imports to Jakarta EE packages

### Key Takeaways

1. **XML → CDI**: Use `@ApplicationScoped` instead of XML registration
2. **Single Callbacks**: Task events use single methods with old/new values instead of before/after pairs
3. **Old/New Values**: All events provide access to both old and new states
4. **Transactional**: Listeners execute within the same transaction
5. **Rule Config Required**: Rule listeners need a separate configuration class
6. **New v9 Methods**: ProcessEventListener includes methods for signals, messages, errors, and migrations

### v8 vs v9 Comparison

| Aspect | BAMOE v8 | BAMOE v9 |
|--------|----------|----------|
| **Timing** | Separate before/after callbacks | Single callback with old/new values |
| **Registration** | XML or Service Loader | CDI (`@ApplicationScoped`) |
| **Transactional** | Yes | Yes |

---
