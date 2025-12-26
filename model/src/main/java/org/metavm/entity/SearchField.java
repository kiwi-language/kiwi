package org.metavm.entity;

import lombok.Getter;
import lombok.Setter;
import org.metavm.object.instance.core.Value;

import java.util.function.Function;

public final class SearchField<T> {

    @Setter
    private String prefix;

    @Getter
    private final String column;
    private final boolean asTitle;
    private final Function<T, Value> valueFunc;

    public SearchField(int level, String column, boolean asTitle, Function<T, Value> valueFunc) {
        setLevel(level);
        this.column = column;
        this.asTitle = asTitle;
        this.valueFunc = valueFunc;
    }

    public static <T> SearchField<T> create(int level, String esField, Function<T, Value> valueFunc) {
        return new SearchField<>(level, esField, false, valueFunc);
    }

    public static <T> SearchField<T> createTitle(int level, String esField, Function<T, Value> valueFunc) {
        return new SearchField<>(level, esField, true, valueFunc);
    }

    public String getEsField() {
        return prefix + column;
    }

    public boolean isTitle() {
        return asTitle;
    }

    public Function<T, Value> valueFunc() {
        return valueFunc;
    }

    public void setLevel(int level) {
        setPrefix("l" + level  + ".");
    }

    public Value getValue(T instance) {
        return valueFunc.apply(instance);
    }

}
