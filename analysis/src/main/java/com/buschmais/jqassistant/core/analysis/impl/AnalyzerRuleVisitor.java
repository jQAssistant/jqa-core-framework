package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.analysis.api.model.ConceptDescriptor;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.report.api.model.Result;
import com.buschmais.jqassistant.core.rule.api.executor.AbstractRuleVisitor;
import com.buschmais.jqassistant.core.rule.api.model.*;

import io.smallrye.config.ConfigMapping;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.StopWatch;

import static com.buschmais.jqassistant.core.analysis.api.configuration.Analyze.EXECUTE_APPLIED_CONCEPTS;

/**
 * Implementation of a rule visitor for analysis execution.
 */
@Slf4j
public class AnalyzerRuleVisitor extends AbstractRuleVisitor {

    private Analyze configuration;
    private AnalyzerContext analyzerContext;
    private ReportPlugin reportPlugin;
    private Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins;

    /**
     * Constructor.
     *
     * @param configuration
     *     The configuration
     * @param ruleInterpreterPlugins
     *     The {@link RuleInterpreterPlugin}s.
     * @param reportPlugin
     *     The report writer.
     */
    AnalyzerRuleVisitor(Analyze configuration, AnalyzerContext analyzerContext, Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins,
        ReportPlugin reportPlugin) {
        this.configuration = configuration;
        this.analyzerContext = analyzerContext;
        this.ruleInterpreterPlugins = ruleInterpreterPlugins;
        this.reportPlugin = reportPlugin;
    }

    @Override
    public void beforeRules() throws RuleException {
        reportPlugin.begin();
    }

    @Override
    public void afterRules() throws RuleException {
        reportPlugin.end();
    }

    @Override
    public boolean visitConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
        ConceptDescriptor conceptDescriptor = analyzerContext.getStore()
            .find(ConceptDescriptor.class, concept.getId());
        Result.Status status;
        boolean isExecuteAppliedConcepts = configuration.executeAppliedConcepts();
        if (conceptDescriptor == null || isExecuteAppliedConcepts) {
            log.info("Applying concept '{}' with severity: '{}'.", concept.getId(), effectiveSeverity.getInfo(concept.getSeverity()));
            reportPlugin.beginConcept(concept);
            Result<Concept> result = execute(concept, effectiveSeverity);
            reportPlugin.setResult(result);
            status = result.getStatus();
            if (conceptDescriptor == null) {
                conceptDescriptor = analyzerContext.getStore()
                    .create(ConceptDescriptor.class);
                conceptDescriptor.setId(concept.getId());
                conceptDescriptor.setStatus(status);
            }
            reportPlugin.endConcept();
        } else {
            if (!isExecuteAppliedConcepts) {
                log.info("Concept '{}' has already been applied, skipping (activate '{}.{}' to force execution).", concept.getId(),
                        Analyze.class.getAnnotation(ConfigMapping.class)
                            .prefix(), EXECUTE_APPLIED_CONCEPTS);
            }
            status = conceptDescriptor.getStatus();
        }
        return Result.Status.SUCCESS.equals(status);
    }

    @Override
    public void skipConcept(Concept concept, Severity effectiveSeverity) throws RuleException {
        reportPlugin.beginConcept(concept);
        Result<Concept> result = Result.<Concept>builder()
            .rule(concept)
            .status(Result.Status.SKIPPED)
            .severity(effectiveSeverity)
            .build();
        reportPlugin.setResult(result);
        reportPlugin.endConcept();
    }

    @Override
    public void visitConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
        log.info("Validating constraint '" + constraint.getId() + "' with severity: '" + effectiveSeverity.getInfo(constraint.getSeverity()) + "'.");
        reportPlugin.beginConstraint(constraint);
        reportPlugin.setResult(execute(constraint, effectiveSeverity));
        reportPlugin.endConstraint();
    }

    @Override
    public void skipConstraint(Constraint constraint, Severity effectiveSeverity) throws RuleException {
        reportPlugin.beginConstraint(constraint);
        Result<Constraint> result = Result.<Constraint>builder()
            .rule(constraint)
            .status(Result.Status.SKIPPED)
            .severity(effectiveSeverity)
            .build();
        reportPlugin.setResult(result);
        reportPlugin.endConstraint();
    }

    @Override
    public void beforeGroup(Group group, Severity effectiveSeverity) throws RuleException {
        log.info("Executing group '" + group.getId() + "'");
        reportPlugin.beginGroup(group);
    }

    @Override
    public void afterGroup(Group group) throws RuleException {
        reportPlugin.endGroup();
    }

    private <T extends ExecutableRule> Result<T> execute(T executableRule, Severity severity) throws RuleException {
        Map<String, Object> ruleParameters = getRuleParameters(executableRule);
        Executable<?> executable = executableRule.getExecutable();
        Collection<RuleInterpreterPlugin> languagePlugins = ruleInterpreterPlugins.get(executable.getLanguage());
        if (languagePlugins == null) {
            throw new RuleException("Could not determine plugin to execute " + executableRule);
        }
        for (RuleInterpreterPlugin languagePlugin : languagePlugins) {
            if (languagePlugin.accepts(executableRule)) {
                Result<T> result = execute(executableRule, severity, ruleParameters, languagePlugin);
                if (result != null) {
                    return result;
                }
            }
        }
        throw new RuleException("No plugin for language '" + executable.getLanguage() + "' returned a result for " + executableRule);
    }

    private <T extends ExecutableRule> Result<T> execute(T executableRule, Severity severity, Map<String, Object> ruleParameters,
        RuleInterpreterPlugin languagePlugin) throws RuleException {
        StopWatch stopWatch = StopWatch.createStarted();
        try {
            Result<T> result = languagePlugin.execute(executableRule, ruleParameters, severity, analyzerContext);
            if (result != null) {
                return result;
            }
        } finally {
            stopWatch.stop();
            long ruleExecutionTime = stopWatch.getTime(TimeUnit.SECONDS);
            if (ruleExecutionTime > configuration.warnOnExecutionTimeSeconds()) {
                log.warn("Execution of rule with id '{}' took {} seconds.", executableRule.getSource()
                    .getId(), ruleExecutionTime);
            }
        }
        return null;
    }

    private Map<String, Object> getRuleParameters(ExecutableRule executableRule) throws RuleException {
        Map<String, Object> ruleParameters = new HashMap<>();
        Map<String, Parameter> parameters = executableRule.getParameters();
        for (Map.Entry<String, Parameter> entry : parameters.entrySet()) {
            String parameterName = entry.getKey();
            Parameter parameter = entry.getValue();
            Object parameterValue;
            String parameterValueAsString = configuration.ruleParameters()
                .get(parameterName);
            if (parameterValueAsString != null) {
                try {
                    parameterValue = parameter.getType()
                        .parse(parameterValueAsString);
                } catch (RuleException e) {
                    throw new RuleException("Cannot determine value for parameter " + parameterName + "' of rule '" + executableRule + "'.");
                }
            } else {
                parameterValue = parameter.getDefaultValue();
            }
            if (parameterValue == null) {
                throw new RuleException("No value or default value defined for required parameter '" + parameterName + "' of rule '" + executableRule + "'.");
            }
            ruleParameters.put(parameterName, parameterValue);
        }
        return ruleParameters;
    }

}
