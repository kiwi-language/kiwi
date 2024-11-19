package org.metavm.asm;

import org.metavm.entity.IEntityContext;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeDef;
import org.metavm.util.NncUtils;

import java.util.List;

public class AssemblerFactory {

    public static Assembler createWithStandardTypes(IEntityContext context) {
        return new Assembler(code -> context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, code));
    }

    private static List<TypeDef> getStandardTypeDefs() {
        return NncUtils.map(StdKlass.values(), StdKlass::get);
    }

}
