package org.metavm.object.instance.core;

import org.metavm.util.InstanceOutput;

public interface IInstanceField {

    long getKlassTag();

    int getTag();

    boolean shouldSkipWrite();

    void set(Instance value);

    void clear();

    void writeValue(InstanceOutput output);

    boolean isFieldInitialized();
}
