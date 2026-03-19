package com.example.humantask;

import java.util.Optional;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Specializes;

import org.kie.kogito.auth.IdentityProvider;
import org.kie.kogito.usertask.UserTaskInstance;
import org.kie.kogito.usertask.impl.BasicUserTaskAssignmentStrategy;

/**
 * Custom task assignment strategy for dynamic task assignment.
 * 
 * This strategy automatically assigns tasks based on business rules:
 * - Low priority tasks → junior reviewers
 * - Medium priority tasks → senior reviewers
 * - High priority tasks → managers
 * - Amount-based assignment for approval tasks
 */
@Specializes
@ApplicationScoped
public class CustomTaskAssignmentStrategy extends BasicUserTaskAssignmentStrategy {

    @Override
    public String getName() {
        return "CustomTaskAssignmentStrategy";
    }

    @Override
    public Optional<String> computeAssignment(UserTaskInstance userTaskInstance, 
                                              IdentityProvider identityProvider) {
        System.out.println("Computing assignment using custom strategy for task: " 
                          + userTaskInstance.getTaskName());
        
        String taskName = userTaskInstance.getTaskName();
        
        // Assignment based on task name
        switch (taskName) {
            case "review_request":
                return assignReviewTask(userTaskInstance);
            
            case "approval_task":
                return assignApprovalTask(userTaskInstance);
            
            case "escalation_review":
                // Escalated tasks always go to manager
                return Optional.of("manager");
            
            default:
                // Use default assignment strategy
                return super.computeAssignment(userTaskInstance, identityProvider);
        }
    }
    
    /**
     * Assign review tasks based on priority
     */
    private Optional<String> assignReviewTask(UserTaskInstance task) {
        Object priorityObj = task.getInputs().get("Priority");
        
        if (priorityObj != null) {
            int priority = Integer.parseInt(priorityObj.toString());
            
            if (priority >= 8) {
                // High priority → senior reviewer
                return Optional.of("senior_reviewer");
            } else if (priority >= 5) {
                // Medium priority → reviewer
                return Optional.of("reviewer");
            } else {
                // Low priority → junior reviewer
                return Optional.of("junior_reviewer");
            }
        }
        
        // Default to reviewer group
        return Optional.empty();
    }
    
    /**
     * Assign approval tasks based on amount
     */
    private Optional<String> assignApprovalTask(UserTaskInstance task) {
        Object amountObj = task.getInputs().get("amount");
        
        if (amountObj != null) {
            double amount = Double.parseDouble(amountObj.toString());
            
            if (amount > 10000) {
                // High amount → director
                return Optional.of("director");
            } else if (amount > 5000) {
                // Medium amount → manager
                return Optional.of("manager");
            } else {
                // Low amount → supervisor
                return Optional.of("supervisor");
            }
        }
        
        return Optional.empty();
    }
}


