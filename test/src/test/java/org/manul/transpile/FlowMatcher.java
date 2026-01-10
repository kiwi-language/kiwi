package org.manul.transpile;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.manul.flow.Flow;

public class FlowMatcher extends BaseMatcher<Flow> {

    private final Flow expected;

    public FlowMatcher(Flow expected) {
        this.expected = expected;
    }

    @Override
    public boolean matches(Object actual) {
        return false;
    }

    @Override
    public void describeTo(Description description) {

    }
}
