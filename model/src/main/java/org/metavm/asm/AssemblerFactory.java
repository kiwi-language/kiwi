package org.metavm.asm;

import org.metavm.entity.StandardTypes;
import org.metavm.object.type.TypeDef;

import java.util.List;

public class AssemblerFactory {

    public static Assembler createWithStandardTypes() {
        return new Assembler(getStandardTypeDefs());
    }

    private static List<TypeDef> getStandardTypeDefs() {
        return List.of(
                StandardTypes.getChildListKlass(),
                StandardTypes.getReadWriteListKlass(),
                StandardTypes.getValueListKlass(),
                StandardTypes.getListKlass(),
                StandardTypes.getEnumKlass(),
                StandardTypes.getRuntimeExceptionKlass(),
                StandardTypes.getIterableKlass(),
                StandardTypes.getIteratorKlass(),
                StandardTypes.getPredicateKlass(),
                StandardTypes.getConsumerKlass()
        );
    }

}
