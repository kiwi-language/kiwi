package org.metavm.entity;

import org.metavm.object.instance.core.Value;

import java.util.function.Function;

public final class SearchField<T> {

    private String prefix;

    private final String column;
    private final boolean asTitle;
    private final Function<T, Value> valueFunc;

    public SearchField(String column, boolean asTitle, Function<T, Value> valueFunc) {
        this.column = column;
        this.asTitle = asTitle;
        this.valueFunc = valueFunc;
    }

    public static <T> SearchField<T> create(String esField, Function<T, Value> valueFunc) {
        return new SearchField<>(esField, false, valueFunc);
    }

    public static <T> SearchField<T> createTitle(String esField, Function<T, Value> valueFunc) {
        return new SearchField<>(esField, true, valueFunc);
    }

    public String getEsField() {
        return prefix + column;
    }

    public String getColumn() {
        return column;
    }

    public boolean isTitle() {
        return asTitle;
    }

    public Function<T, Value> valueFunc() {
        return valueFunc;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public Value getValue(T instance) {
        return valueFunc.apply(instance);
    }

}
