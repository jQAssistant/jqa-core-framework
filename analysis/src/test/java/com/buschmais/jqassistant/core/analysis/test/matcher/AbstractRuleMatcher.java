package com.buschmais.jqassistant.core.analysis.test.matcher;

import com.buschmais.jqassistant.core.rule.api.model.Rule;
import com.buschmais.jqassistant.core.shared.annotation.ToBeRemovedInVersion;

import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

/**
 * Abstract base class for rules matchers.
 *
 * @deprecated This class is replaced by com.buschmais.jqassistant.core.test.matcher.AbstractRuleMatcher.
 */
@Deprecated
@ToBeRemovedInVersion(major = 1, minor = 13)
public class AbstractRuleMatcher<T extends Rule> extends TypeSafeMatcher<T> {

    private Class<T> type;

    private String id;

    /**
     * Constructor.
     *
     * @param type
     *            The rules type.
     * @param id
     *            The expected rules id.
     */
    protected AbstractRuleMatcher(Class<T> type, String id) {
        this.type = type;
        this.id = id;
    }

    @Override
    public boolean matchesSafely(T item) {
        return this.id.equals(item.getId());
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(type.getSimpleName()).appendText("(").appendText(id).appendText(")");
    }

    @Override
    protected void describeMismatchSafely(T item, Description mismatchDescription) {
        mismatchDescription.appendText(item.getClass().getSimpleName()).appendText("(").appendText(item.getId()).appendText(")");
    }
}
