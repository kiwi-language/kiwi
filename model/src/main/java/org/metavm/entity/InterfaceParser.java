package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.TypeCategory;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class InterfaceParser<T> extends PojoParser<T, InterfaceDef<T>> {

    public InterfaceParser(Class<T> javaClass, Type javaType, DefContext defContext, ColumnStore columnStore) {
        super(javaClass, javaType, defContext, columnStore);
    }

    @Override
    protected InterfaceDef<T> createDef(PojoDef<? super T> superDef) {
        return new InterfaceDef<>(
                javaClass, javaType, superDef, createType(), defContext
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
            for (Method method : javaClass.getMethods()) {
                var returnType = defContext.getType(method.getGenericReturnType());
                if(ReflectionUtils.isAnnotatedWithNullable(method))
                    returnType = StandardTypes.getNullableType(returnType);
                MethodBuilder.newBuilder(get().klass, method.getName(), method.getName())
                        .parameters(NncUtils.map(method.getParameters(), this::createParameter))
                        .returnType(returnType)
                        .build();
            }
        }
    }

}
