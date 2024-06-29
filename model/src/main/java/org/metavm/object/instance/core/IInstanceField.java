package org.metavm.object.instance.core;

import org.metavm.util.InstanceOutput;

public interface IInstanceField {

    long getRecordGroupTag();

    long getRecordTag();

    boolean shouldSkipWrite();

    void writeValue(InstanceOutput output);

}
