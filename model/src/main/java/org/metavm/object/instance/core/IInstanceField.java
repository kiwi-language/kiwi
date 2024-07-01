package org.metavm.object.instance.core;

import org.metavm.util.InstanceOutput;

public interface IInstanceField {

    long getRecordGroupTag();

    int getRecordTag();

    boolean shouldSkipWrite();

    void set(Instance value);

    void clear();

    void writeValue(InstanceOutput output);

    boolean isFieldInitialized();
}
