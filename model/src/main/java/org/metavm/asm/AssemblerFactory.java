package org.metavm.asm;

import org.metavm.entity.IEntityContext;
import org.metavm.entity.StdKlass;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeDef;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import java.util.List;

public class AssemblerFactory {

    public static Assembler createWithStandardTypes(IEntityContext context) {
        return new Assembler(code -> context.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(code)));
    }

    private static List<TypeDef> getStandardTypeDefs() {
        return Utils.map(StdKlass.values(), StdKlass::get);
    }

}
