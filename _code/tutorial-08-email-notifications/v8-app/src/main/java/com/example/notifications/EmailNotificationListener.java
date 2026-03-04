package com.example.notifications;

import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.TaskEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.util.Properties;

/**
 * Custom Email Notification Listener for BAMOE v8
 * 
 * This listener sends email notifications when task deadlines are reached.
 * It demonstrates the v8 approach using TaskLifeCycleEventListener.
 * 
 * Configuration:
 * - Registered in kie-deployment-descriptor.xml
 * - Uses JavaMail API for sending emails
 * - Configured with SMTP server settings
 */
public class EmailNotificationListener implements TaskLifeCycleEventListener {
    
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationListener.class);
    
    // Email configuration
    private String smtpHost = "localhost";
    private String smtpPort = "1025"; // MailHog default port
    private String fromEmail = "test@kogito.com";
    private boolean enableAuth = false;
    
    public EmailNotificationListener() {
        // Default constructor
        logger.info("EmailNotificationListener initialized");
    }
    
    public EmailNotificationListener(String smtpHost, String smtpPort, String fromEmail) {
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.fromEmail = fromEmail;
        logger.info("EmailNotificationListener initialized with custom settings: {}:{}", smtpHost, smtpPort);
    }
    
    @Override
    public void beforeTaskActivatedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskClaimedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskSkippedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskStartedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskStoppedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskCompletedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskFailedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskAddedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskExitedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskReleasedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskResumedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskSuspendedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskForwardedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskDelegatedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void beforeTaskNominatedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskActivatedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskClaimedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskSkippedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskStartedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskStoppedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskCompletedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskFailedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskAddedEvent(TaskEvent event) {
        logger.info("Task added: {} - Checking for deadline notifications", event.getTask().getName());
        
        // In v8, deadline notifications would be triggered here
        // This is a simplified example - actual implementation would check for deadlines
        if (event.getTask().getTaskData().getDeadlines() != null && 
            !event.getTask().getTaskData().getDeadlines().isEmpty()) {
            
            String taskName = event.getTask().getName();
            String taskOwner = event.getTask().getTaskData().getActualOwner() != null ? 
                              event.getTask().getTaskData().getActualOwner().getId() : "unassigned";
            
            sendDeadlineNotification(taskName, taskOwner, "jdoe@kogito.com");
        }
    }
    
    @Override
    public void afterTaskExitedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskReleasedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskResumedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskSuspendedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskForwardedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskDelegatedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    @Override
    public void afterTaskNominatedEvent(TaskEvent event) {
        // Not used for deadline notifications
    }
    
    /**
     * Send email notification for task deadline
     */
    private void sendDeadlineNotification(String taskName, String taskOwner, String toEmail) {
        try {
            logger.info("Sending deadline notification email for task: {} to: {}", taskName, toEmail);
            
            Properties props = new Properties();
            props.put("mail.smtp.host", smtpHost);
            props.put("mail.smtp.port", smtpPort);
            props.put("mail.smtp.auth", String.valueOf(enableAuth));
            
            Session session = Session.getInstance(props);
            
            Message message = new MimeMessage(session);
            message.setFrom(new InternetAddress(fromEmail));
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail));
            message.setSubject("Task Deadline Notification: " + taskName);
            
            String emailBody = String.format(
                "Dear User,\n\n" +
                "This is a notification that the following task has a deadline:\n\n" +
                "Task Name: %s\n" +
                "Task Owner: %s\n\n" +
                "Please complete this task as soon as possible.\n\n" +
                "Best regards,\n" +
                "BAMOE Workflow System",
                taskName, taskOwner
            );
            
            message.setText(emailBody);
            
            Transport.send(message);
            
            logger.info("Email sent successfully to: {}", toEmail);
            
        } catch (MessagingException e) {
            logger.error("Failed to send email notification", e);
        }
    }
    
    // Getters and setters for configuration
    public void setSmtpHost(String smtpHost) {
        this.smtpHost = smtpHost;
    }
    
    public void setSmtpPort(String smtpPort) {
        this.smtpPort = smtpPort;
    }
    
    public void setFromEmail(String fromEmail) {
        this.fromEmail = fromEmail;
    }
    
    public void setEnableAuth(boolean enableAuth) {
        this.enableAuth = enableAuth;
    }
}


