package org.metavm.compiler.element;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;

public interface MemberRef {

    SymName getName();

    void load(Code code);

    void store(Code code);

    void invoke(Code code);

    Type getType();

    boolean isStatic();
}
