package com.buschmais.jqassistant.core.analysis.impl;

import java.util.Collection;
import java.util.Map;

import com.buschmais.jqassistant.core.analysis.api.Analyzer;
import com.buschmais.jqassistant.core.analysis.api.AnalyzerContext;
import com.buschmais.jqassistant.core.analysis.api.RuleInterpreterPlugin;
import com.buschmais.jqassistant.core.analysis.api.configuration.Analyze;
import com.buschmais.jqassistant.core.report.api.ReportPlugin;
import com.buschmais.jqassistant.core.rule.api.executor.RuleSetExecutor;
import com.buschmais.jqassistant.core.rule.api.executor.RuleVisitor;
import com.buschmais.jqassistant.core.rule.api.model.RuleException;
import com.buschmais.jqassistant.core.rule.api.model.RuleSelection;
import com.buschmais.jqassistant.core.rule.api.model.RuleSet;
import com.buschmais.jqassistant.core.store.api.Store;

import org.slf4j.Logger;

/**
 * Implementation of the {@link Analyzer}.
 */
public class AnalyzerImpl implements Analyzer {

    private final Analyze configuration;

    private final AnalyzerContext analyzerContext;

    private final Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins;
    private final ReportPlugin reportPlugin;

    /**
     * Constructor.
     *
     * @param configuration
     *            The configuration.
     * @param classLoader
     *            The plugin {@link ClassLoader}
     * @param store
     *            The store
     * @param ruleInterpreterPlugins
     *            The {@link RuleInterpreterPlugin}s.
     * @param reportPlugin
     *            The report wrtier.
     * @param log
     *            The {@link Logger}.
     */
    public AnalyzerImpl(Analyze configuration, ClassLoader classLoader, Store store, Map<String, Collection<RuleInterpreterPlugin>> ruleInterpreterPlugins,
            ReportPlugin reportPlugin, Logger log) {
        this.configuration = configuration;
        this.analyzerContext = new AnalyzerContextImpl(configuration, classLoader, store, log);
        this.ruleInterpreterPlugins = ruleInterpreterPlugins;
        this.reportPlugin = reportPlugin;
    }

    @Override
    public Analyze getConfiguration() {
        return configuration;
    }

    @Override
    public void execute(RuleSet ruleSet, RuleSelection ruleSelection) throws RuleException {
        RuleVisitor visitor = new TransactionalRuleVisitor(
                new AnalyzerRuleVisitor(configuration, analyzerContext, ruleInterpreterPlugins, reportPlugin), analyzerContext.getStore());
        RuleSetExecutor executor = new RuleSetExecutor(visitor, configuration.rule());
        executor.execute(ruleSet, ruleSelection);
    }
}
