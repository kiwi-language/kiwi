package org.metavm.compiler.element;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;

public interface Variable extends ValueElement {

     SymName getName();

    void setName(SymName name);

    Type getType();

    void load(Code code);

    void store(Code code);

}
