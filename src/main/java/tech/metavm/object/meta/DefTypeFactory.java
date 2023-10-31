package tech.metavm.object.meta;

import tech.metavm.entity.CollectionDef;
import tech.metavm.entity.DefMap;
import tech.metavm.entity.DirectDef;
import tech.metavm.object.instance.ArrayType;

public class DefTypeFactory extends TypeFactory {

    private final DefMap defMap;

    public DefTypeFactory(DefMap defMap) {
        this.defMap = defMap;
    }

    @Override
    public void putType(java.lang.reflect.Type javaType, Type type) {
        if(type instanceof ArrayType arrayType) {
            defMap.addDef(new CollectionDef<>(
                    arrayType.kind().getEntityClass(),
                    javaType,
                    arrayType,
                    defMap.getDef(arrayType.getElementType())
            ));
        }
        else {
            defMap.addDef(new DirectDef<>(javaType, type));
        }
    }

    @Override
    public boolean isPutTypeSupported() {
        return true;
    }

    @Override
    public Type getType(java.lang.reflect.Type javaType) {
        return defMap.getType(javaType);
    }

    @Override
    public java.lang.reflect.Type getJavaType(Type type) {
        return defMap.getDef(type).getJavaType();
    }

    @Override
    public boolean containsJavaType(java.lang.reflect.Type javaType) {
        return defMap.containsDef(javaType);
    }
}
