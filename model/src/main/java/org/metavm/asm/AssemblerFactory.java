package org.metavm.asm;

import org.metavm.entity.StdKlass;
import org.metavm.object.type.TypeDef;
import org.metavm.util.NncUtils;

import java.util.List;

public class AssemblerFactory {

    public static Assembler createWithStandardTypes() {
        return new Assembler(getStandardTypeDefs());
    }

    private static List<TypeDef> getStandardTypeDefs() {
        return NncUtils.map(StdKlass.values(), StdKlass::get);
    }

}
