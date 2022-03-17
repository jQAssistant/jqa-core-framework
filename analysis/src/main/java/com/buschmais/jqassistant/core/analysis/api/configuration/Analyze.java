package com.buschmais.jqassistant.core.analysis.api.configuration;

import java.util.Map;

import com.buschmais.jqassistant.core.report.api.configuration.Report;
import com.buschmais.jqassistant.core.rule.api.configuration.Rule;
import com.buschmais.jqassistant.core.shared.annotation.Description;

import io.smallrye.config.WithDefault;

public interface Analyze {

    String PREFIX = "jqassistant.analyze";

    /**
     * The {@link Rule} configuration.
     *
     * @return The {@link Rule} configuration.
     */
    Rule rule();

    /**
     * The {@link Report} configuration.
     *
     * @return The {@link Report} configuration.
     */
    Report report();

    String RULE_PARAMETERS = "rule-parameters";

    @Description("The parameters to be passed to rules.")
    Map<String, String> ruleParameters();

    String EXECUTE_APPLIED_CONCEPTS = "execute-applied-concepts";

    @WithDefault("false")
    boolean executeAppliedConcepts();

    String WARN_ON_EXECUTION_TIME_SECONDS = "warn-on-rule-execution-time-seconds";

    @WithDefault("5")
    int warnOnExecutionTimeSeconds();

}