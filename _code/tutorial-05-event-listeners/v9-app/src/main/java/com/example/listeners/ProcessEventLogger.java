package com.example.listeners;

import org.kie.api.event.process.ProcessCompletedEvent;
import org.kie.api.event.process.ProcessNodeLeftEvent;
import org.kie.api.event.process.ProcessNodeTriggeredEvent;
import org.kie.api.event.process.ProcessStartedEvent;
import org.kie.api.event.process.ProcessVariableChangedEvent;
import org.kie.kogito.internal.process.event.DefaultKogitoProcessEventListener;
import org.kie.kogito.internal.process.runtime.KogitoWorkflowProcessInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.runtime.Startup;

/**
 * Process Event Listener for BAMOE 9 - tracks process lifecycle events.
 * Automatically registered via CDI (@ApplicationScoped).
 */
@Startup
@ApplicationScoped
public class ProcessEventLogger extends DefaultKogitoProcessEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessEventLogger.class);
    private static final String PREFIX = ">>>>> [PROCESS-LISTENER] ";

    @PostConstruct
    public void init() {
        LOGGER.info(PREFIX + "ProcessEventLogger registered successfully via CDI");
    }

    @Override
    public void beforeProcessStarted(ProcessStartedEvent event) {
        LOGGER.info(PREFIX + "beforeProcessStarted: processId={}",
                event.getProcessInstance().getProcessId());
    }

    @Override
    public void afterProcessStarted(ProcessStartedEvent event) {
        LOGGER.info(PREFIX + "afterProcessStarted: processId={}, state={}",
                event.getProcessInstance().getProcessId(),
                event.getProcessInstance().getState());
    }

    @Override
    public void beforeProcessCompleted(ProcessCompletedEvent event) {
        LOGGER.info(PREFIX + "beforeProcessCompleted: processId={}",
                event.getProcessInstance().getProcessId());
    }

    @Override
    public void afterProcessCompleted(ProcessCompletedEvent event) {
        LOGGER.info(PREFIX + "afterProcessCompleted: processId={}, state={}",
                event.getProcessInstance().getProcessId(),
                event.getProcessInstance().getState());
    }

    @Override
    public void beforeNodeTriggered(ProcessNodeTriggeredEvent event) {
        LOGGER.info(PREFIX + "beforeNodeTriggered: nodeName={}",
                event.getNodeInstance().getNodeName());
        
        // Example: Access process instance details
        if (event.getProcessInstance() instanceof KogitoWorkflowProcessInstance) {
            KogitoWorkflowProcessInstance processInstance = 
                (KogitoWorkflowProcessInstance) event.getProcessInstance();
            LOGGER.debug(PREFIX + "Process variables: {}", processInstance.getVariables());
        }
    }

    @Override
    public void afterNodeTriggered(ProcessNodeTriggeredEvent event) {
        LOGGER.info(PREFIX + "afterNodeTriggered: nodeName={}",
                event.getNodeInstance().getNodeName());
    }

    @Override
    public void beforeNodeLeft(ProcessNodeLeftEvent event) {
        LOGGER.info(PREFIX + "beforeNodeLeft: nodeName={}",
                event.getNodeInstance().getNodeName());
    }

    @Override
    public void afterNodeLeft(ProcessNodeLeftEvent event) {
        LOGGER.info(PREFIX + "afterNodeLeft: nodeName={}",
                event.getNodeInstance().getNodeName());
    }

    @Override
    public void beforeVariableChanged(ProcessVariableChangedEvent event) {
        // v9: Access both old and new values
        LOGGER.info(PREFIX + "beforeVariableChanged: variableId={}, OLD_VALUE={}, NEW_VALUE={}",
                event.getVariableId(),
                event.getOldValue(),
                event.getNewValue());
    }

    @Override
    public void afterVariableChanged(ProcessVariableChangedEvent event) {
        LOGGER.info(PREFIX + "afterVariableChanged: variableId={}, OLD_VALUE={}, NEW_VALUE={}",
                event.getVariableId(),
                event.getOldValue(),
                event.getNewValue());
    }
}


