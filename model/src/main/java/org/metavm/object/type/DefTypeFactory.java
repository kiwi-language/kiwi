package org.metavm.object.type;

import org.metavm.entity.DirectDef;
import org.metavm.entity.SystemDefContext;

public class DefTypeFactory extends TypeFactory {

    private final SystemDefContext defContext;

    public DefTypeFactory(SystemDefContext defContext) {
        this.defContext = defContext;
    }

    @Override
    public void putType(java.lang.reflect.Type javaType, TypeDef typeDef) {
        defContext.addDef(new DirectDef<>(javaType, typeDef));
    }

}
