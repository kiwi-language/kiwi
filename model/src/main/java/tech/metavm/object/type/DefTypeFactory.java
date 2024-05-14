package tech.metavm.object.type;

import tech.metavm.entity.DefContext;
import tech.metavm.entity.DirectDef;

public class DefTypeFactory extends TypeFactory {

    private final DefContext defContext;

    public DefTypeFactory(DefContext defContext) {
        this.defContext = defContext;
    }

    @Override
    public void putType(java.lang.reflect.Type javaType, TypeDef typeDef) {
        defContext.addDef(new DirectDef<>(javaType, typeDef));
    }

}
