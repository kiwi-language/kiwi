package tech.metavm.entity;

import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.NullInstance;
import tech.metavm.object.type.AnyType;
import tech.metavm.object.type.NeverType;
import tech.metavm.object.type.PrimitiveKind;
import tech.metavm.object.type.PrimitiveType;
import tech.metavm.util.Instances;

public class MockStandardTypesInitializer {

    public static void init() {
        StandardTypes.anyType = new AnyType();
        StandardTypes.neverType = new NeverType();
        StandardTypes.nullType = new PrimitiveType(PrimitiveKind.NULL);
        StandardTypes.timeType = new PrimitiveType(PrimitiveKind.TIME);
        StandardTypes.booleanType = new PrimitiveType(PrimitiveKind.BOOLEAN);
        StandardTypes.longType = new PrimitiveType(PrimitiveKind.LONG);
        StandardTypes.stringType = new PrimitiveType(PrimitiveKind.STRING);
        StandardTypes.voidType = new PrimitiveType(PrimitiveKind.VOID);
        StandardTypes.passwordType = new PrimitiveType(PrimitiveKind.PASSWORD);
        StandardTypes.doubleType = new PrimitiveType(PrimitiveKind.DOUBLE);

        Instances.nullInstance = new NullInstance(StandardTypes.getNullType());
        Instances.trueInstance = new BooleanInstance(true, StandardTypes.getBooleanType());
        Instances.falseInstance = new BooleanInstance(false, StandardTypes.getBooleanType());
    }

}
