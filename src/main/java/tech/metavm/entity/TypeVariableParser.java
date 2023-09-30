package tech.metavm.entity;

import tech.metavm.object.instance.ClassInstance;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.List;

public class TypeVariableParser implements DefParser<Object, ClassInstance, TypeVariableDef> {

    private final TypeVariable<?> javaTypeVariable;
    private final Class<?> declaringClass;
    private final DefMap defMap;
    private tech.metavm.object.meta.TypeVariable type;

    public TypeVariableParser(TypeVariable<?> javaTypeVariable, DefMap defMap) {
        if(!(javaTypeVariable.getGenericDeclaration() instanceof Class<?>)) {
            throw new InternalException("TypeVariables not declared in classes are not supported yet");
        }
        this.defMap = defMap;
        declaringClass = (Class<?>) javaTypeVariable.getGenericDeclaration();
        this.javaTypeVariable = javaTypeVariable;
    }

    @Override
    public TypeVariableDef create() {
        return new TypeVariableDef(javaTypeVariable, createType());
    }

    private tech.metavm.object.meta.TypeVariable createType() {
        var declaringType = defMap.getPojoDef(declaringClass).getType();
        return type = new tech.metavm.object.meta.TypeVariable(
                null,
                declaringType.getName() + "-" + ReflectUtils.getMetaTypeVariableName(javaTypeVariable),
                declaringType.getCodeRequired() + "-" + javaTypeVariable.getName(),
                declaringType
        );
    }

    @Override
    public void initialize() {
        type.setBounds(NncUtils.map(javaTypeVariable.getBounds(), defMap::getType));
    }

    @Override
    public List<Type> getDependencyTypes() {
        return List.of(declaringClass);
    }

}
