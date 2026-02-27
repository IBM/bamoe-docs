package com.example.listeners;

import org.kie.kogito.usertask.UserTaskEventListener;
import org.kie.kogito.usertask.events.UserTaskAssignmentEvent;
import org.kie.kogito.usertask.events.UserTaskStateEvent;
import org.kie.kogito.usertask.events.UserTaskVariableEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.runtime.Startup;

/**
 * User Task Event Listener for BAMOE 9 - tracks user task lifecycle events.
 * Uses single callback methods with old/new values instead of v8's before/after pairs.
 */
@Startup
@ApplicationScoped
public class UserTaskEventLogger implements UserTaskEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserTaskEventLogger.class);
    private static final String PREFIX = ">>>>> [USERTASK-LISTENER] ";

    @PostConstruct
    public void init() {
        LOGGER.info(PREFIX + "UserTaskEventLogger registered successfully via CDI");
    }

    @Override
    public void onUserTaskState(UserTaskStateEvent event) {
        String taskName = event.getUserTaskInstance().getTaskName();
        String oldStatus = event.getOldStatus() != null ? event.getOldStatus().getName() : "null";
        String newStatus = event.getNewStatus() != null ? event.getNewStatus().getName() : "null";
        
        LOGGER.info(PREFIX + "onUserTaskState: taskName={}, OLD_STATUS={}, NEW_STATUS={}",
                taskName, oldStatus, newStatus);
        
        // Detect specific state transitions
        if ("Ready".equals(oldStatus) && "Reserved".equals(newStatus)) {
            LOGGER.info(PREFIX + "Task claimed: {}", taskName);
        } else if ("Reserved".equals(oldStatus) && "InProgress".equals(newStatus)) {
            LOGGER.info(PREFIX + "Task started: {}", taskName);
        } else if ("InProgress".equals(oldStatus) && "Completed".equals(newStatus)) {
            LOGGER.info(PREFIX + "Task completed: {}", taskName);
        }
    }

    @Override
    public void onUserTaskAssignment(UserTaskAssignmentEvent event) {
        String taskName = event.getUserTaskInstance().getTaskName();
        String oldUsers = event.getOldUsersId() != null ? String.join(",", event.getOldUsersId()) : "null";
        String newUsers = event.getNewUsersId() != null ? String.join(",", event.getNewUsersId()) : "null";
        
        LOGGER.info(PREFIX + "onUserTaskAssignment: taskName={}, assignmentType={}, OLD_USERS={}, NEW_USERS={}",
                taskName, event.getAssignmentType(), oldUsers, newUsers);
    }

    @Override
    public void onUserTaskInputVariable(UserTaskVariableEvent event) {
        String variableName = event.getVariableName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        
        LOGGER.info(PREFIX + "onUserTaskInputVariable: variableName={}, OLD_VALUE={}, NEW_VALUE={}",
                variableName, oldValue, newValue);
    }

    @Override
    public void onUserTaskOutputVariable(UserTaskVariableEvent event) {
        String variableName = event.getVariableName();
        Object oldValue = event.getOldValue();
        Object newValue = event.getNewValue();
        
        LOGGER.info(PREFIX + "onUserTaskOutputVariable: variableName={}, OLD_VALUE={}, NEW_VALUE={}",
                variableName, oldValue, newValue);
    }
}


