package com.example.listeners;

import org.kie.api.event.rule.AfterMatchFiredEvent;
import org.kie.api.event.rule.BeforeMatchFiredEvent;
import org.kie.api.event.rule.DefaultAgendaEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import io.quarkus.runtime.Startup;

/**
 * Rule Event Listener for BAMOE 9.x
 * 
 * This listener demonstrates how to track Drools rule execution events in BAMOE 9.
 * It extends DefaultAgendaEventListener which provides empty implementations
 * for all AgendaEventListener methods, allowing us to override only what we need.
 */
@Startup
@ApplicationScoped
public class RuleEventLogger extends DefaultAgendaEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RuleEventLogger.class);
    private static final String PREFIX = ">>>>> [RULE-LISTENER] ";

    @PostConstruct
    public void init() {
        LOGGER.info(PREFIX + "RuleEventLogger registered successfully via CDI");
    }

    @Override
    public void beforeMatchFired(BeforeMatchFiredEvent event) {
        LOGGER.info(PREFIX + "beforeMatchFired: ruleName={}",
                event.getMatch().getRule().getName());
    }

    @Override
    public void afterMatchFired(AfterMatchFiredEvent event) {
        LOGGER.info(PREFIX + "afterMatchFired: ruleName={}",
                event.getMatch().getRule().getName());
    }
}


