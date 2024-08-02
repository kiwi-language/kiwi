package org.metavm.object.instance.core;

import org.jetbrains.annotations.NotNull;
import org.metavm.util.InstanceOutput;

public interface IInstanceField extends Comparable<IInstanceField> {

    long getKlassTag();

    int getTag();

    boolean shouldSkipWrite();

    void set(Value value);

    void clear();

    void writeValue(InstanceOutput output);

    @NotNull Value getValue();

    boolean isFieldInitialized();

    @Override
    default int compareTo(@NotNull IInstanceField o) {
        return Integer.compare(getTag(), o.getTag());
    }
}
