package tech.metavm.object.type;

import tech.metavm.entity.CollectionDef;
import tech.metavm.entity.DefContext;
import tech.metavm.entity.DirectDef;

public class DefTypeFactory extends TypeFactory {

    private final DefContext defContext;

    public DefTypeFactory(DefContext defContext) {
        this.defContext = defContext;
    }

    @Override
    public void putType(java.lang.reflect.Type javaType, Type type) {
        if(type instanceof ArrayType arrayType) {
            defContext.addDef(new CollectionDef<>(
                    arrayType.getKind().getEntityClass(),
                    javaType,
                    arrayType,
                    defContext.getDef(arrayType.getElementType()),
                    defContext));
        }
        else {
            defContext.addDef(new DirectDef<>(javaType, type));
        }
    }

    @Override
    public boolean isPutTypeSupported() {
        return true;
    }

    @Override
    public Type getType(java.lang.reflect.Type javaType) {
        return defContext.getType(javaType);
    }

    @Override
    public java.lang.reflect.Type getJavaType(Type type) {
        return defContext.getDef(type).getJavaType();
    }

    @Override
    public boolean containsJavaType(java.lang.reflect.Type javaType) {
        return defContext.containsDef(javaType);
    }
}
