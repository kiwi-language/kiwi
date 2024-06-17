package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.TypeCategory;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.List;

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
                var returnType = defContext.getType(javaMethod.getGenericReturnType());
                if(ReflectionUtils.isAnnotatedWithNullable(javaMethod))
                    returnType = StandardTypes.getNullableType(returnType);
                var method = MethodBuilder.newBuilder(get().klass, javaMethod.getName(), javaMethod.getName())
                        .parameters(NncUtils.map(javaMethod.getParameters(), this::createParameter))
                        .typeParameters(NncUtils.map(javaMethod.getTypeParameters(), this::createTypeVariable))
                        .returnType(returnType)
                        .build();
                NncUtils.biForEach(
                        List.of(javaMethod.getTypeParameters()),
                        method.getTypeParameters(),
                        (javaTypeVar, typeVar) -> typeVar.setBounds(NncUtils.map(javaTypeVar.getBounds(), defContext::getType))
                );
            }
        }
    }

}
