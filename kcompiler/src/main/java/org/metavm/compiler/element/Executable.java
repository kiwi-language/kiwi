package org.metavm.compiler.element;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;


public interface Executable extends Element {

    List<Param> getParams();

    Type getRetType();

    void addParam(Param param);

    Name getQualName();

    @Nullable Code getCode();

    ConstPool getConstPool();
}
