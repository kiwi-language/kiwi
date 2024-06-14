package org.metavm.util;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

public class PojoMatcher<T> extends BaseMatcher<T> {

    public static <T> PojoMatcher<T> of(T value) {
        return new PojoMatcher<>(value);
    }

    private final T value;

    private PojoMatcher(T value) {
        this.value = value;
    }

    @Override
    public boolean matches(Object actual) {
        return !DiffUtils.isPojoDifferent(value, actual);
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(value.toString());
    }
}
