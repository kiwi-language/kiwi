package org.metavm.object.type;

import org.metavm.util.InstanceOutput;

public interface TypeOrTypeKey {

    boolean isArray();

    void write(InstanceOutput output);

    int getTypeTag();

}
