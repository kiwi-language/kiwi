package org.manul.object.type;

import org.manul.util.MvOutput;

public interface TypeOrTypeKey {

    boolean isArray();

    void write(MvOutput output);

    int getTypeTag();

}
