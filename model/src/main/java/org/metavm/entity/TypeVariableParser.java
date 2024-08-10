package org.metavm.entity;

import org.metavm.util.InternalException;
import org.metavm.util.NncUtils;

import java.lang.reflect.TypeVariable;

import static org.metavm.object.type.ResolutionStage.INIT;

public class TypeVariableParser extends DefParser<Object, TypeVariableDef> {

    private final TypeVariable<?> javaTypeVariable;
    private final Class<?> declaringClass;
    private final SystemDefContext defMap;
    private org.metavm.object.type.TypeVariable type;

    public TypeVariableParser(TypeVariable<?> javaTypeVariable, SystemDefContext defMap) {
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

    @Override
    public TypeVariableDef get() {
        return null;
    }

    private org.metavm.object.type.TypeVariable createType() {
        var declaringType = defMap.getPojoDef(declaringClass, INIT).getKlass();
        return type = new org.metavm.object.type.TypeVariable(
                null,
                EntityUtils.getMetaTypeVariableName(javaTypeVariable),
                javaTypeVariable.getName(),
                declaringType);
    }

    @Override
    public void generateSignature() {
        type.setBounds(NncUtils.map(javaTypeVariable.getBounds(), b -> defMap.getType(b)));
    }

    @Override
    public void generateDeclaration() {

    }

    @Override
    public void generateDefinition() {

    }

}
