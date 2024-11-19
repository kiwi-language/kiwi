package org.metavm.object.type;

import org.metavm.util.MvOutput;

public interface TypeOrTypeKey {

    boolean isArray();

    void write(MvOutput output);

    int getTypeTag();

}
