# Tutorial 8: Email Notifications Migration (v8 to v9)

## Table of Contents

1. [Introduction](#introduction)
2. [Prerequisites](#prerequisites)
3. [Step-by-Step Migration](#step-by-step-migration)
4. [Testing](#testing)
5. [Common Issues](#common-issues)
6. [Summary](#summary)

---

## Introduction

Migrate email notification functionality from BAMOE v8 to v9, including support for custom email content.

**v8 Approach:** Custom `EmailNotificationListener` with JavaMail API
**v9 Approach:** Built-in task notification with Quarkus Mailer + Kafka

---

## What Changed from v8 to v9

This tutorial shows how to migrate email notification functionality from BAMOE v8 to v9, specifically focusing on **deadline notifications** for user tasks.

### The BPMN Process

The [`approvalWithDeadline.bpmn`](v9-app/src/main/resources/approvalWithDeadline.bpmn) process:

1. **Starts** with a request and approver information
2. **Creates a User Task** "Approve Request" assigned to the specified approver
3. **Configures Two Deadlines**:
   
   **a - NotStartedNotify** - Triggers if task is not claimed/started:
   - Format: `[from:noreply@example.com|tousers:|togroups:|toemails:jdoe@example.com|replyTo:support@example.com|subject:Task Deadline Notification|body:Task Approve Request requires your attention. Request: #{request}]@[PT2M]`
   - **Trigger**: If task is not started within **2 minutes** (`@[PT2M]`)
   - **Action**: Send email to `jdoe@example.com` with the subject and body
   - **Variable Substitution**: `#{request}` is replaced with the actual request value
   
   **b - NotCompletedNotify** - Triggers if task is started but not completed:
   - Format: `[from:noreply@example.com|tousers:|togroups:|toemails:jdoe@example.com|replyTo:support@example.com|subject:Task Completion Reminder|body:Task Approve Request is still pending completion. Request: #{request}. Please complete this task as soon as possible.]@[PT5M]`
   - **Trigger**: If task is not completed within **5 minutes** (`@[PT5M]`) after being started
   - **Action**: Send reminder email to complete the task
   - **Use Case**: Ensures tasks that are claimed but not finished get follow-up reminders

4. **Sends Emails** automatically when deadlines expire
5. **Completes** when the task is approved

### When Emails are Sent

**NotStartedNotify Email** is sent when:
- The user task remains in "Ready" state (not started/claimed)
- The deadline duration expires (2 minutes in this example)
- The Kafka deadline event is processed by the notification system

**NotCompletedNotify Email** is sent when:
- The user task is in "Reserved" or "InProgress" state (claimed but not completed)
- The deadline duration expires (5 minutes in this example)
- The Kafka deadline event is processed by the notification system

**Key Difference:**
- `NotStartedNotify`: Reminds users to **claim/start** the task
- `NotCompletedNotify`: Reminds users to **finish** a task they've already started

## Prerequisites

### Required Software
- Java 17+
- Maven 3.8.1+
- Docker or Podman

### Required Services

To test email notifications, you need to run the following external services:

**Required Services:**

Email notifications in BAMOE v9 require **both** MailHog (or SMTP server) **and** Kafka:

**1. MailHog (SMTP Testing):**
```bash
docker run -d -p 1025:1025 -p 8025:8025 --name mailhog mailhog/mailhog
```
- SMTP: localhost:1025
- Web UI: http://localhost:8025

**2. Kafka (Deadline Events - REQUIRED):**

**Why Kafka is Required:**
Kafka is mandatory for email notifications because BAMOE v9 uses an event-driven architecture. When a task deadline expires, the system publishes a `UserTaskInstanceDeadlineDataEvent` to Kafka. The notification subsystem consumes these events and triggers email delivery. Without Kafka, deadline events cannot be processed and emails will not be sent.

```bash
# Start Kafka
docker run -d \
  -p 9092:9092 \
  -p 9093:9093 \
  -e KAFKA_NODE_ID=1 \
  -e KAFKA_LISTENER_SECURITY_PROTOCOL_MAP=CONTROLLER:PLAINTEXT,PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT \
  -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT_HOST://localhost:9092,PLAINTEXT://kafka:9093 \
  -e KAFKA_PROCESS_ROLES=broker,controller \
  -e KAFKA_CONTROLLER_QUORUM_VOTERS=1@kafka:29093 \
  -e KAFKA_LISTENERS=CONTROLLER://:29093,PLAINTEXT_HOST://:9092,PLAINTEXT://:9093 \
  -e KAFKA_INTER_BROKER_LISTENER_NAME=PLAINTEXT \
  -e KAFKA_CONTROLLER_LISTENER_NAMES=CONTROLLER \
  -e CLUSTER_ID=4L6g3nShT-eMCtK--X86sw \
  -e KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR=1 \
  -e KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS=0 \
  -e KAFKA_TRANSACTION_STATE_LOG_MIN_ISR=1 \
  -e KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR=1 \
  -e KAFKA_LOG_DIRS=/tmp/kraft-combined-logs \
  --name kafka \
  apache/kafka:3.7.2

# Wait for Kafka to start
sleep 15

# Create the topic (required for deadline events)
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic kogito-deadline-events \
  --partitions 1 \
  --replication-factor 1
```

**Configuration:** The [`application.properties`](v9-app/src/main/resources/application.properties) file configures:
- Quarkus Mailer for MailHog connection
- Kafka connector for deadline events (both outgoing and incoming channels)
- `quarkus.mailer.mock=false` to enable email delivery

**Event Flow:**
```
Task Deadline Expires → Publish to Kafka (kogito-deadline-events)
→ Notification Processor Consumes Event → Quarkus Mailer Sends Email
```

---

## Step-by-Step Migration

### Step 1: Add Dependencies

**From bamoe-examples (process-user-tasks-subsystem):**

```xml
<dependencies>
    <!-- Email support -->
    <dependency>
        <groupId>org.jbpm</groupId>
        <artifactId>jbpm-addons-quarkus-mail</artifactId>
    </dependency>
    
    <!-- Task notification -->
    <dependency>
        <groupId>org.jbpm</groupId>
        <artifactId>jbpm-addons-quarkus-task-notification</artifactId>
    </dependency>
    
    <!-- Kafka for deadline events -->
    <dependency>
        <groupId>io.quarkus</groupId>
        <artifactId>quarkus-messaging-kafka</artifactId>
    </dependency>
</dependencies>
```

**All three dependencies are required.** 

### Step 2: Configure application.properties

Refer to [`application.properties`](v9-app/src/main/resources/application.properties) for the complete configuration including:
- Quarkus Mailer settings (MailHog connection)
- Kafka configuration for deadline events
- User groups configuration

**Key Configuration Note:**
- `%dev.quarkus.mailer.mock=false` is required for actual email delivery

### Step 3: Configure BPMN Deadlines

Add `NotStartedNotify` and/or `NotCompletedNotify` data inputs to your user task. See the complete example in [`approvalWithDeadline.bpmn`](v9-app/src/main/resources/approvalWithDeadline.bpmn).

**Key elements to add for each notification type:**

**1. Define the data inputs (item definitions):**
```xml
<bpmn2:itemDefinition id="__UserTask_Approval_NotStartedNotifyInputXItem" structureRef="String"/>
<bpmn2:itemDefinition id="__UserTask_Approval_NotCompletedNotifyInputXItem" structureRef="String"/>
```

**2. Add data inputs to the user task:**
```xml
<bpmn2:dataInput id="_UserTask_Approval_NotStartedNotifyInputX"
                 drools:dtype="String"
                 itemSubjectRef="__UserTask_Approval_NotStartedNotifyInputXItem"
                 name="NotStartedNotify"/>
<bpmn2:dataInput id="_UserTask_Approval_NotCompletedNotifyInputX"
                 drools:dtype="String"
                 itemSubjectRef="__UserTask_Approval_NotCompletedNotifyInputXItem"
                 name="NotCompletedNotify"/>
```

**3. Add to inputSet:**
```xml
<bpmn2:dataInputRefs>_UserTask_Approval_NotStartedNotifyInputX</bpmn2:dataInputRefs>
<bpmn2:dataInputRefs>_UserTask_Approval_NotCompletedNotifyInputX</bpmn2:dataInputRefs>
```

**4. Configure the deadline notifications:**

**NotStartedNotify (task not claimed):**
```xml
<bpmn2:dataInputAssociation>
  <bpmn2:targetRef>_UserTask_Approval_NotStartedNotifyInputX</bpmn2:targetRef>
  <bpmn2:assignment>
    <bpmn2:from xsi:type="bpmn2:tFormalExpression"><![CDATA[[from:noreply@example.com|tousers:|togroups:|toemails:jdoe@example.com|replyTo:support@example.com|subject:Task Deadline Notification|body:Task Approve Request requires your attention. Request: #{request}]@[PT2M]]]></bpmn2:from>
    <bpmn2:to xsi:type="bpmn2:tFormalExpression"><![CDATA[_UserTask_Approval_NotStartedNotifyInputX]]></bpmn2:to>
  </bpmn2:assignment>
</bpmn2:dataInputAssociation>
```

**NotCompletedNotify (task claimed but not completed):**
```xml
<bpmn2:dataInputAssociation>
  <bpmn2:targetRef>_UserTask_Approval_NotCompletedNotifyInputX</bpmn2:targetRef>
  <bpmn2:assignment>
    <bpmn2:from xsi:type="bpmn2:tFormalExpression"><![CDATA[[from:noreply@example.com|tousers:|togroups:|toemails:jdoe@example.com|replyTo:support@example.com|subject:Task Completion Reminder|body:Task Approve Request is still pending completion. Request: #{request}. Please complete this task.]@[PT5M]]]></bpmn2:from>
    <bpmn2:to xsi:type="bpmn2:tFormalExpression"><![CDATA[_UserTask_Approval_NotCompletedNotifyInputX]]></bpmn2:to>
  </bpmn2:assignment>
</bpmn2:dataInputAssociation>
```

**Notification Format:**

**Full Email Format:**
```
[from:sender@example.com|tousers:user1,user2|togroups:group1|toemails:email1@example.com,email2@example.com|replyTo:reply@example.com|subject:Subject|body:Body text]@[duration]
```

**Simplified User Format :**
```
[users:username|subject:Subject|body:Body text]@[duration]
```

### Step 4: Configure Kafka Channels

**Critical Configuration:** Both outgoing and incoming Kafka channels must be configured:

```properties
# Outgoing channel - publishes deadline events
mp.messaging.outgoing.kogito-deadline-events.connector=smallrye-kafka
mp.messaging.outgoing.kogito-deadline-events.topic=kogito-deadline-events
mp.messaging.outgoing.kogito-deadline-events.value.serializer=org.apache.kafka.common.serialization.StringSerializer

# Incoming channel - consumes deadline events for notifications
mp.messaging.incoming.kogito-deadline-events-consumer.connector=smallrye-kafka
mp.messaging.incoming.kogito-deadline-events-consumer.topic=kogito-deadline-events
mp.messaging.incoming.kogito-deadline-events-consumer.value.deserializer=org.apache.kafka.common.serialization.StringDeserializer
mp.messaging.incoming.kogito-deadline-events-consumer.group.id=kogito-deadline-consumer

kafka.bootstrap.servers=localhost:9092
```

**Important Notes:**
- The incoming channel name must end with `-consumer` (e.g., `kogito-deadline-events-consumer`)
- Both channels must point to the same Kafka topic
- The deserializer must be `StringDeserializer` (not `ObjectMapperDeserializer`)

### Step 5: Remove v8 Components

Delete:
- Custom `EmailNotificationListener.java`
- `kie-deployment-descriptor.xml`
- JavaMail configuration code

---

## Custom Email Notifications

### Overview

If you need to customize email content beyond what's configured in the BPMN (similar to v8's `NotificationListener.onNotification()`), you can create a custom notification listener in v9.

**Use Cases:**
- Modify email subject/body dynamically
- Add custom headers or footers
- Include additional task information
- Format emails as HTML
- Add company branding

### Custom Email Notification Dependencies

For custom email notification implementations, you may need additional dependencies beyond the basic setup. Here's the complete dependency list for custom email notifications:

```xml
<!-- Custom Email Notification -->
<dependency>
    <groupId>org.jbpm</groupId>
    <artifactId>jbpm-addons-mail</artifactId>
    <version>999-SNAPSHOT</version>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-kafka-client</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-mailer</artifactId>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-jackson</artifactId>
</dependency>
<dependency>
    <groupId>org.jbpm</groupId>
    <artifactId>jbpm-addons-quarkus-task-notification</artifactId>
    <version>9.3.1-ibm-0006-patched</version>
</dependency>
<dependency>
    <groupId>io.quarkus</groupId>
    <artifactId>quarkus-messaging-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.slf4j</groupId>
    <artifactId>slf4j-api</artifactId>
</dependency>
```

**Key Differences from Basic Setup:**
- `jbpm-addons-mail` - Direct mail add-on (instead of `jbpm-addons-quarkus-mail`)
- `quarkus-kafka-client` - Kafka client for custom event handling
- `quarkus-mailer` - Explicit Quarkus mailer dependency
- `quarkus-jackson` - JSON processing for custom notification data
- `slf4j-api` - Logging support for custom implementations


### Implementation Pattern

**Key Points:**
1. Observe `UserTaskInstanceDeadlineDataEvent` using CDI `@Observes`
2. Extract notification details from the event
3. Customize subject and body
4. Use `MailSender` to send the customized email

**Example Structure:**
```java
@ApplicationScoped
public class CustomEmailNotificationListener {
    
    private final MailSender mailSender;
     @Incoming("kogito-deadline-my-consumer")
    public void onDeadlineEvent(@Observes UserTaskInstanceDeadlineDataEvent event) {
        // Extract notification info
        var notification = event.getData().getNotification();
        
        // Customize content
        String customSubject = customizeSubject(notification.get("subject"));
        String customBody = customizeBody(notification.get("body"));
        
        // Send customized email
        MailInfo mailInfo = MailInfo.of(
            notification.get("recipients"),
            notification.get("from"),
            customSubject,
            notification.get("replyTo"),
            customBody
        );
        mailSender.sendMail(mailInfo);
    }
}
```

**Complete Implementation with MailSender:**
```java
@ApplicationScoped
public class CustomEmailNotificationListener {

    private final MailSender mailSender;

    public CustomEmailNotificationListener(MailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Incoming("kogito-deadline-my-consumer")
    public void onDeadlineEvent(@Observes UserTaskInstanceDeadlineDataEvent event) {
        // Implementation details
    }
}
```

**Important:** This feature requires BAMOE 9.4.0+ (includes bug fix DBACLD-215387).

---

## Testing Both Notification Types

After updating the BPMN file with both `NotStartedNotify` and `NotCompletedNotify`, follow these steps to test:

### 1. Restart the Application

If the application is already running, restart it to pick up the BPMN changes:

```bash
# Stop the current application (Ctrl+C in the terminal)
# Then restart:
cd v9-app
mvn clean quarkus:dev
```

If you are using Gradle, use the following command:

```shell script
gradle clean quarkusDev
```

### 2. Create a New Process Instance

```bash
curl -X POST http://localhost:8080/approvalWithDeadline \
  -H "Content-Type: application/json" \
  -d '{"request": "Budget Approval Test", "approver": "jdoe"}'
```

### 3. Test NotStartedNotify (2 Minutes)

**Wait 2 minutes without claiming the task**, then check MailHog at http://localhost:8025:

**First Email (NotStartedNotify):**
- **From**: noreply@example.com
- **To**: jdoe@example.com
- **Reply-To**: support@example.com
- **Subject**: Task Deadline Notification
- **Body**: Task Approve Request requires your attention. Request: Budget Approval Test

### 4. Test NotCompletedNotify (5 Minutes)

**Claim the task but don't complete it:**

```bash
# Get the process instance ID from the previous curl response
PROCESS_ID="<your-process-id>"

# Get the task ID
TASK_ID=$(curl -s http://localhost:8080/approvalWithDeadline/$PROCESS_ID/tasks | jq -r '.[0].id')

# Claim the task (but don't complete it)
curl -X POST "http://localhost:8080/approvalWithDeadline/$PROCESS_ID/Approve_Request/$TASK_ID/phases/claim" \
  -H "Content-Type: application/json" \
  -d '{"user": "jdoe", "groups": ["approvers"]}'
```

**Wait 5 minutes after claiming**, then check MailHog for the second email:

**Second Email (NotCompletedNotify):**
- **From**: noreply@example.com
- **To**: jdoe@example.com
- **Reply-To**: support@example.com
- **Subject**: Task Completion Reminder
- **Body**: Task Approve Request is still pending completion. Request: Budget Approval Test. Please complete this task as soon as possible.

**Note**: Both emails will only appear after:
1. The application has been restarted with the updated BPMN
2. A new process instance has been created
3. The respective deadlines have expired

---

## Testing

### Run Application

```bash
cd v9-app
mvn clean quarkus:dev
```

If you are using Gradle, use the following command:

```shell script
gradle clean quarkusDev
```

### Create Process Instance

```bash
curl -X POST http://localhost:8080/approvalWithDeadline \
  -H "Content-Type: application/json" \
  -d '{"request": "Budget Approval", "approver": "jdoe"}'
```

### Wait for Deadline

After 2 minutes, check:

**Application Logs:**
```
INFO  [org.kie.kog.mai.QuarkusMailSender] Sending e-mail...
INFO  [quarkus-mailer] Sending email Task Deadline...
INFO  [org.kie.kog.mai.QuarkusMailSender] Mail sent
```

**MailHog UI:** http://localhost:8025

**Kafka Consumer (Optional):**
```bash
docker exec -it kafka /opt/kafka/bin/kafka-console-consumer.sh \
  --bootstrap-server localhost:9092 \
  --topic kogito-deadline-events \
  --from-beginning
```

---

## Common Issues

### 1. Emails Not Sent

**Cause:** Missing `quarkus.mailer.mock=false`
**Fix:** Add to application.properties:
```properties
%dev.quarkus.mailer.mock=false
```

### 2. Kafka Connection Error

**Symptom:** `Broker may not be available`
**Fix:**
```bash
docker start kafka
sleep 15  # Wait for Kafka to be ready
```

### 3. Variable Substitution Not Working

**Cause:** Wrong syntax or variable doesn't exist
**Fix:** Use `#{variableName}` (not `${variableName}`)

### 4. Emails Not Appearing in MailHog

**Symptoms:**
- Logs show "Mail sent" but no email in MailHog UI
- Application logs show email sending but MailHog is empty

**Possible Causes:**
1. `quarkus.mailer.mock=true` (default in test mode)
2. Wrong MailHog port configuration
3. MailHog not running

**Fix:**
```bash
# Check MailHog is running
docker ps | grep mailhog

# Verify configuration
%dev.quarkus.mailer.host=localhost
%dev.quarkus.mailer.port=1025
%dev.quarkus.mailer.mock=false

# Restart MailHog if needed
docker restart mailhog
```

### 5. Kafka Topic Not Found

**Symptom:** `Unknown topic or partition`
**Fix:**
```bash
# Create the topic manually
docker exec -it kafka /opt/kafka/bin/kafka-topics.sh --create \
  --bootstrap-server localhost:9092 \
  --topic kogito-deadline-events \
  --partitions 1 \
  --replication-factor 1
```

---

## Known Issues

**Issue:** Email notifications not working in BAMOE 9.3.1 due to event name mismatch
**Affected Versions:** 9.3.1 and earlier

**Fix:**
Upgrade to BAMOE 9.4.0 or later


---

## Summary

### Migration Checklist

- [ ] **Verify BAMOE version** - Use 9.4.0+ (9.3.1 has known bug DBACLD-215387)
- [ ] **Add 3 dependencies** to pom.xml:
  - `jbpm-addons-quarkus-mail`
  - `jbpm-addons-quarkus-task-notification`
  - `quarkus-messaging-kafka`
- [ ] **Configure Quarkus Mailer** with `mock=false`
- [ ] **Configure Kafka** for deadline events (both outgoing and incoming channels)
- [ ] **Add notification configuration** to BPMN user tasks (choose based on your needs):
  - `NotStartedNotify` - Reminds users to claim/start tasks
  - `NotCompletedNotify` - Reminds users to complete claimed tasks
  - Both can be used together for comprehensive deadline management
- [ ] **Remove v8 components**:
  - Custom `EmailNotificationListener.java`
  - `kie-deployment-descriptor.xml`
  - JavaMail configuration
- [ ] **Start external services**:
  - MailHog (SMTP server)
  - Kafka broker
- [ ] **Create Kafka topic** `kogito-deadline-events`
- [ ] **Test email delivery** with actual process instance

### Key Points

1. **BAMOE 9.4.0+ Required** - Version 9.3.1 won't support this feature
2. **All 3 Dependencies Required** - Missing any dependency breaks notifications
3. **Kafka is Mandatory** - Email notifications use event-driven architecture via Kafka
4. **Both Kafka Channels Required** - Configure both outgoing (publish) and incoming (consume) channels
5. **MailHog for Testing** - Provides SMTP server and web UI to view sent emails
6. **Custom Notifications Supported** - Can customize email content similar to v8 (requires 9.4.0+)

### How It Works

**Event Flow:**
1. **Process starts** → User task created with deadline configuration (`NotStartedNotify` or `NotCompletedNotify`)
2. **Deadline expires** → Kogito publishes `UserTaskInstanceDeadlineDataEvent` to Kafka topic `kogito-deadline-events`
3. **Notification processor** consumes event from Kafka → Extracts email configuration
4. **Variable substitution** → Replaces `#{variableName}` with actual process variable values
5. **Quarkus Mailer** sends email → Delivers to configured SMTP server (MailHog in dev)
6. **User receives notification** → Email appears in inbox with task details

### Files Changed

**Removed (v8 components):**
- `EmailNotificationListener.java`
- `kie-deployment-descriptor.xml`
- JavaMail dependencies and configuration

**Added (v9 components):**
- BPMN deadline configuration (`NotStartedNotify`/`NotCompletedNotify`)
- Quarkus Mailer properties
- Kafka configuration (outgoing + incoming channels)
- 3 new Maven dependencies

**Optional (for customization):**
- `CustomEmailNotificationListener.java` (observes deadline events)

### Migration Benefits

1. **Simplified Configuration** - No custom Java code required for basic notifications
2. **Built-in Support** - Quarkus Mailer handles SMTP complexity
3. **Event-Driven** - Kafka provides reliable, scalable event processing
4. **Flexible Customization** - Optional custom listener for advanced scenarios
5. **Better Observability** - Kafka events can be monitored and debugged
6. **Production Ready** - Quarkus Mailer supports various SMTP providers
