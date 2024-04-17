package tech.metavm.asm;

import org.jetbrains.annotations.NotNull;
import tech.metavm.asm.Assembler.*;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.entity.StandardTypes;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.ChildMetaList;
import tech.metavm.util.MetaList;
import tech.metavm.util.MetaSet;
import tech.metavm.util.ReadWriteMetaList;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

public class AssemblerFactory {

    public static Assembler createWithStandardTypes() {
        return new Assembler(getStandardTypeIds());
    }

    @NotNull
    private static HashMap<Assembler.AsmType, String> getStandardTypeIds() {
        var stdTypeIds = new HashMap<Assembler.AsmType, String>();
        stdTypeIds.put(new PrimitiveAsmType(Assembler.AsmPrimitiveKind.LONG), StandardTypes.getLongType().getStringId());
        stdTypeIds.put(new PrimitiveAsmType(Assembler.AsmPrimitiveKind.STRING), StandardTypes.getStringType().getStringId());
        stdTypeIds.put(new PrimitiveAsmType(Assembler.AsmPrimitiveKind.BOOLEAN), StandardTypes.getBooleanType().getStringId());
        stdTypeIds.put(new PrimitiveAsmType(Assembler.AsmPrimitiveKind.DOUBLE), StandardTypes.getDoubleType().getStringId());
        stdTypeIds.put(new PrimitiveAsmType(Assembler.AsmPrimitiveKind.TIME), StandardTypes.getTimeType().getStringId());
        stdTypeIds.put(new PrimitiveAsmType(Assembler.AsmPrimitiveKind.PASSWORD), StandardTypes.getPasswordType().getStringId());
        stdTypeIds.put(new PrimitiveAsmType(Assembler.AsmPrimitiveKind.NULL), StandardTypes.getNullType().getStringId());
        stdTypeIds.put(new PrimitiveAsmType(Assembler.AsmPrimitiveKind.VOID), StandardTypes.getVoidType().getStringId());
        stdTypeIds.put(AnyAsmType.INSTANCE, StandardTypes.getAnyType().getStringId());
        stdTypeIds.put(NeverAsmType.INSTANCE, StandardTypes.getNeverType().getStringId());
        stdTypeIds.put(new UnionAsmType(Set.of(AnyAsmType.INSTANCE, new PrimitiveAsmType(Assembler.AsmPrimitiveKind.NULL))),
                StandardTypes.getNullableAnyType().getStringId());
        stdTypeIds.put(ClassAsmType.create("ChildList"), StandardTypes.getChildListType().getStringId());
        stdTypeIds.put(ClassAsmType.create("List"), StandardTypes.getListType().getStringId());
        stdTypeIds.put(ClassAsmType.create("ReadWriteList"), StandardTypes.getReadWriteListType().getStringId());
        stdTypeIds.put(ClassAsmType.create("Enum"), StandardTypes.getEnumType().getStringId());
        stdTypeIds.put(ClassAsmType.create("RuntimeException"), StandardTypes.getRuntimeExceptionType().getStringId());
        stdTypeIds.put(ClassAsmType.create("Iterable"), StandardTypes.getIterableType().getStringId());
        stdTypeIds.put(ClassAsmType.create("Iterator"), StandardTypes.getIteratorType().getStringId());
        stdTypeIds.put(ClassAsmType.create("Predicate"), StandardTypes.getPredicateType().getStringId());
        var collClasses = List.of(
                MetaList.class, ReadWriteMetaList.class, ChildMetaList.class, MetaSet.class);
        var primitiveClasses = List.of(Long.class, Double.class, Boolean.class, String.class, Date.class);
        for (Class<?> collClass : collClasses) {
            var collType = ModelDefRegistry.getDefContext().getClassType(collClass);
            for (Class<? extends Serializable> primitiveClass : primitiveClasses) {
                var primType = (PrimitiveType) ModelDefRegistry.getDefContext().getType(primitiveClass);
                var pCollType = StandardTypes.getParameterizedType(collType, List.of(primType));
                var asmPrimType = new PrimitiveAsmType(AsmPrimitiveKind.valueOf(primType.getKind().name()));
                stdTypeIds.put(new ClassAsmType(collType.getCode(), List.of(asmPrimType)), pCollType.getStringId());
            }
        }
        return stdTypeIds;
    }

}
