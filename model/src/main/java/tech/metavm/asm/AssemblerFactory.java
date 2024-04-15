package tech.metavm.asm;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.StandardTypes;

import java.util.HashMap;

public class AssemblerFactory {

    public static Assembler createWitStandardTypes() {
        return new Assembler(getStandardTypeIds());
    }

    @NotNull
    private static HashMap<Assembler.AsmType, String> getStandardTypeIds() {
        var stdTypeIds = new HashMap<Assembler.AsmType, String>();
        stdTypeIds.put(new Assembler.PrimitiveAsmType(Assembler.AsmPrimitiveKind.LONG), StandardTypes.getLongType().getStringId());
        stdTypeIds.put(new Assembler.PrimitiveAsmType(Assembler.AsmPrimitiveKind.STRING), StandardTypes.getStringType().getStringId());
        stdTypeIds.put(new Assembler.PrimitiveAsmType(Assembler.AsmPrimitiveKind.BOOLEAN), StandardTypes.getBooleanType().getStringId());
        stdTypeIds.put(new Assembler.PrimitiveAsmType(Assembler.AsmPrimitiveKind.DOUBLE), StandardTypes.getDoubleType().getStringId());
        stdTypeIds.put(new Assembler.PrimitiveAsmType(Assembler.AsmPrimitiveKind.TIME), StandardTypes.getTimeType().getStringId());
        stdTypeIds.put(new Assembler.PrimitiveAsmType(Assembler.AsmPrimitiveKind.PASSWORD), StandardTypes.getPasswordType().getStringId());
        stdTypeIds.put(new Assembler.PrimitiveAsmType(Assembler.AsmPrimitiveKind.NULL), StandardTypes.getNullType().getStringId());
        stdTypeIds.put(new Assembler.PrimitiveAsmType(Assembler.AsmPrimitiveKind.VOID), StandardTypes.getVoidType().getStringId());
        stdTypeIds.put(Assembler.ClassAsmType.create("ChildList"), StandardTypes.getChildListType().getStringId());
        stdTypeIds.put(Assembler.ClassAsmType.create("List"), StandardTypes.getListType().getStringId());
        stdTypeIds.put(Assembler.ClassAsmType.create("ReadWriteList"), StandardTypes.getReadWriteListType().getStringId());
        stdTypeIds.put(Assembler.ClassAsmType.create("Enum"), StandardTypes.getEnumType().getStringId());
        stdTypeIds.put(Assembler.ClassAsmType.create("RuntimeException"), StandardTypes.getRuntimeExceptionType().getStringId());
        return stdTypeIds;
    }

}
