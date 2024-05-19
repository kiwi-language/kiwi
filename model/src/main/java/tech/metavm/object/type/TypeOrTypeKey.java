package tech.metavm.object.type;

import tech.metavm.util.InstanceOutput;

public interface TypeOrTypeKey {

    boolean isArray();

    void write(InstanceOutput output);

    int getTypeTag();

}
