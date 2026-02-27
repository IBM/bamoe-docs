package com.example.listeners;

import java.util.Collections;
import java.util.List;

import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
import org.kie.kogito.rules.RuleEventListenerConfig;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import io.quarkus.runtime.Startup;

/**
 * Rule Event Listener Configuration for BAMOE 9.x
 *
 * This configuration class registers rule event listeners with the Drools engine.
 * In BAMOE 9, rule event listeners are registered via CDI by implementing
 * RuleEventListenerConfig and returning lists of AgendaEventListener and
 * RuleRuntimeEventListener instances.
 *
 * Key features:
 * - Automatically registered via CDI (@ApplicationScoped)
 * - Returns list of AgendaEventListener instances
 * - Returns list of RuleRuntimeEventListener instances
 * - Supports multiple rule event listeners
 *
 * Migration from BAMOE 8:
 * - In BAMOE 8, listeners were registered in kie-deployment-descriptor.xml
 * - In BAMOE 9, use CDI-based registration via this config class
 */
@Startup
@ApplicationScoped
public class CustomRuleEventListenerConfig implements RuleEventListenerConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomRuleEventListenerConfig.class);

    @Inject
    RuleEventLogger ruleEventLogger;

    @PostConstruct
    public void init() {
        LOGGER.info(">>>>> [RULE-LISTENER-CONFIG] CustomRuleEventListenerConfig registered successfully via CDI");
    }

    @Override
    public List<AgendaEventListener> agendaListeners() {
        return Collections.singletonList(ruleEventLogger);
    }

    @Override
    public List<RuleRuntimeEventListener> ruleRuntimeListeners() {
        return Collections.emptyList();
    }
}


