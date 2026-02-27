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

import org.kie.api.event.process.*;

/**
 * Process Event Listener for v8
 * 
 * This listener monitors process lifecycle events and logs them.
 * In v8, event listeners are registered via XML configuration in kie-deployment-descriptor.xml.
 * - Uses System.out for logging (or custom logger)
 */
public class ProcessEventLogger implements ProcessEventListener {
    
    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        System.out.println("=== Process Starting ===");
        System.out.println("Process ID: " + event.getProcessInstance().getId());
        System.out.println("Process Name: " + event.getProcessInstance().getProcessName());
    }
    
    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        System.out.println("=== Process Started ===");
        System.out.println("Process ID: " + event.getProcessInstance().getId());
        System.out.println("State: " + event.getProcessInstance().getState());
    }
    
    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        System.out.println("=== Process Completing ===");
        System.out.println("Process ID: " + event.getProcessInstance().getId());
    }
    
    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        System.out.println("=== Process Completed ===");
        System.out.println("Process ID: " + event.getProcessInstance().getId());
    }
    
    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        System.out.println("Node Triggering: " + event.getNodeInstance().getNodeName());
    }
    
    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        System.out.println("Node Triggered: " + event.getNodeInstance().getNodeName());
    }
    
    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        System.out.println("Node Leaving: " + event.getNodeInstance().getNodeName());
    }
    
    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        System.out.println("Node Left: " + event.getNodeInstance().getNodeName());
    }
    
    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        System.out.println("Variable Changing: " + event.getVariableId() + " = " + event.getNewValue());
    }
    
    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        System.out.println("Variable Changed: " + event.getVariableId() + 
            " = " + event.getNewValue() + " (was: " + event.getOldValue() + ")");
    }
}

// v8 Implementation


