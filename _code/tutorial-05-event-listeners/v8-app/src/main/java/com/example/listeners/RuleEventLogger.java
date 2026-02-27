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

import org.kie.api.event.rule.*;

/**
 * Rule Event Listener for v8
 * 
 * This listener monitors rule execution events and logs them.
 * In v8, rule event listeners are registered via XML configuration.
 */
public class RuleEventLogger implements AgendaEventListener {
    
    @Override
    public void matchCreated(MatchCreatedEvent event) {
        System.out.println("=== Rule Match Created ===");
        System.out.println("Rule: " + event.getMatch().getRule().getName());
        System.out.println("Package: " + event.getMatch().getRule().getPackageName());
    }
    
    @Override
    public void matchCancelled(MatchCancelledEvent event) {
        System.out.println("=== Rule Match Cancelled ===");
        System.out.println("Rule: " + event.getMatch().getRule().getName());
        System.out.println("Cause: " + event.getCause());
    }
    
    @Override
    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        System.out.println("=== Rule Firing ===");
        System.out.println("Rule: " + event.getMatch().getRule().getName());
    }
    
    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        System.out.println("=== Rule Fired ===");
        System.out.println("Rule: " + event.getMatch().getRule().getName());
    }
    
    @Override
    public void agendaGroupPopped(AgendaGroupPoppedEvent event) {
        System.out.println("Agenda Group Popped: " + event.getAgendaGroup().getName());
    }
    
    @Override
    public void agendaGroupPushed(AgendaGroupPushedEvent event) {
        System.out.println("Agenda Group Pushed: " + event.getAgendaGroup().getName());
    }
    
    @Override
    public void beforeRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        System.out.println("Rule Flow Group Activating: " + event.getRuleFlowGroup().getName());
    }
    
    @Override
    public void afterRuleFlowGroupActivated(RuleFlowGroupActivatedEvent event) {
        System.out.println("Rule Flow Group Activated: " + event.getRuleFlowGroup().getName());
    }
    
    @Override
    public void beforeRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        System.out.println("Rule Flow Group Deactivating: " + event.getRuleFlowGroup().getName());
    }
    
    @Override
    public void afterRuleFlowGroupDeactivated(RuleFlowGroupDeactivatedEvent event) {
        System.out.println("Rule Flow Group Deactivated: " + event.getRuleFlowGroup().getName());
    }
}

// v8 Implementation


