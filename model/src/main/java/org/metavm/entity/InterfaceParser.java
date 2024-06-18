package org.metavm.entity;

import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.TypeCategory;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;

public class InterfaceParser<T> extends PojoParser<T, InterfaceDef<T>> {

    public InterfaceParser(Class<T> javaClass, Type javaType, DefContext defContext, ColumnStore columnStore) {
        super(javaClass, javaType, defContext, columnStore);
    }

    @Override
    protected InterfaceDef<T> createDef(PojoDef<? super T> superDef) {
        return new InterfaceDef<>(
                javaClass, javaType, superDef, createKlass(), defContext
        );
    }

    @Override
    protected TypeCategory getTypeCategory() {
        return TypeCategory.INTERFACE;
    }

    @Override
    public void generateDeclaration() {
        super.generateDeclaration();
        if (isSystemAPI()) {
            for (Method javaMethod : javaClass.getMethods()) {
                if(Modifier.isStatic(javaMethod.getModifiers()) || javaMethod.isDefault())
                    continue;
                createMethod(javaMethod, false);
            }
        }
    }

}
