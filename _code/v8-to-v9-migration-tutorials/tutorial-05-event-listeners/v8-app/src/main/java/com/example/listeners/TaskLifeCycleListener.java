/*
 * Copyright IBM Corp. 2026.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.listeners;

import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.TaskEvent;

/**
 * Task Life Cycle Event Listener for v8
 * 
 * This listener monitors human task lifecycle events and logs them.
 * In v8, task event listeners are registered via XML configuration.
 * 
 * Key characteristics of v8:
 * - Uses org.kie.api.task package
 * - Implements TaskLifeCycleEventListener interface
 * - Registered via XML configuration (kie-deployment-descriptor.xml)
 * - TaskEvent provides access to task data
 * - No CDI annotations needed
 * 
 * Reference: https://github.com/kiegroup/jbpm/blob/main/jbpm-human-task/jbpm-human-task-core/src/main/java/org/jbpm/services/task/lifecycle/listeners/TaskLifeCycleEventListener.java
 */
public class TaskLifeCycleListener implements TaskLifeCycleEventListener {
    
    @Override
    public void beforeTaskActivatedEvent(TaskEvent event) {
        System.out.println("=== Task Activating ===");
        System.out.println("Task ID: " + event.getTask().getId());
        System.out.println("Task Name: " + event.getTask().getName());
    }
    
    @Override
    public void afterTaskActivatedEvent(TaskEvent event) {
        System.out.println("=== Task Activated ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskClaimedEvent(TaskEvent event) {
        System.out.println("=== Task Claiming ===");
        System.out.println("Task ID: " + event.getTask().getId());
        System.out.println("User: " + event.getTask().getTaskData().getActualOwner());
    }
    
    @Override
    public void afterTaskClaimedEvent(TaskEvent event) {
        System.out.println("=== Task Claimed ===");
        System.out.println("Task ID: " + event.getTask().getId());
        System.out.println("Owner: " + event.getTask().getTaskData().getActualOwner());
    }
    
    @Override
    public void beforeTaskStartedEvent(TaskEvent event) {
        System.out.println("=== Task Starting ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskStartedEvent(TaskEvent event) {
        System.out.println("=== Task Started ===");
        System.out.println("Task ID: " + event.getTask().getId());
        System.out.println("Status: " + event.getTask().getTaskData().getStatus());
    }
    
    @Override
    public void beforeTaskCompletedEvent(TaskEvent event) {
        System.out.println("=== Task Completing ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskCompletedEvent(TaskEvent event) {
        System.out.println("=== Task Completed ===");
        System.out.println("Task ID: " + event.getTask().getId());
        System.out.println("Final Status: " + event.getTask().getTaskData().getStatus());
    }
    
    @Override
    public void beforeTaskFailedEvent(TaskEvent event) {
        System.out.println("=== Task Failing ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskFailedEvent(TaskEvent event) {
        System.out.println("=== Task Failed ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskAddedEvent(TaskEvent event) {
        System.out.println("=== Task Adding ===");
        System.out.println("Task Name: " + event.getTask().getName());
    }
    
    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        System.out.println("=== Task Added ===");
        System.out.println("Task ID: " + event.getTask().getId());
        System.out.println("Task Name: " + event.getTask().getName());
        System.out.println("Potential Owners: " + event.getTask().getPeopleAssignments().getPotentialOwners());
    }
    
    @Override
    public void beforeTaskSkippedEvent(TaskEvent event) {
        System.out.println("=== Task Skipping ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskSkippedEvent(TaskEvent event) {
        System.out.println("=== Task Skipped ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskReleasedEvent(TaskEvent event) {
        System.out.println("=== Task Releasing ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskReleasedEvent(TaskEvent event) {
        System.out.println("=== Task Released ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskResumedEvent(TaskEvent event) {
        System.out.println("=== Task Resuming ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskResumedEvent(TaskEvent event) {
        System.out.println("=== Task Resumed ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskSuspendedEvent(TaskEvent event) {
        System.out.println("=== Task Suspending ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskSuspendedEvent(TaskEvent event) {
        System.out.println("=== Task Suspended ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskForwardedEvent(TaskEvent event) {
        System.out.println("=== Task Forwarding ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskForwardedEvent(TaskEvent event) {
        System.out.println("=== Task Forwarded ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskDelegatedEvent(TaskEvent event) {
        System.out.println("=== Task Delegating ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskDelegatedEvent(TaskEvent event) {
        System.out.println("=== Task Delegated ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskNominatedEvent(TaskEvent event) {
        System.out.println("=== Task Nominating ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskNominatedEvent(TaskEvent event) {
        System.out.println("=== Task Nominated ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskExitedEvent(TaskEvent event) {
        System.out.println("=== Task Exiting ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskExitedEvent(TaskEvent event) {
        System.out.println("=== Task Exited ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskStoppedEvent(TaskEvent event) {
        System.out.println("=== Task Stopping ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskStoppedEvent(TaskEvent event) {
        System.out.println("=== Task Stopped ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskReassignedEvent(TaskEvent event) {
        System.out.println("=== Task Reassigning ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskReassignedEvent(TaskEvent event) {
        System.out.println("=== Task Reassigned ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskNotificationEvent(TaskEvent event) {
        System.out.println("=== Task Notification ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskNotificationEvent(TaskEvent event) {
        System.out.println("=== Task Notification Sent ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void beforeTaskUpdatedEvent(TaskEvent event) {
        System.out.println("=== Task Updating ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
    @Override
    public void afterTaskUpdatedEvent(TaskEvent event) {
        System.out.println("=== Task Updated ===");
        System.out.println("Task ID: " + event.getTask().getId());
    }
    
}

// v8 Implementation


