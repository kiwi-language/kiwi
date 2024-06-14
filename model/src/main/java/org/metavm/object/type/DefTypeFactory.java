package org.metavm.object.type;

import org.metavm.entity.DefContext;
import org.metavm.entity.DirectDef;

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
