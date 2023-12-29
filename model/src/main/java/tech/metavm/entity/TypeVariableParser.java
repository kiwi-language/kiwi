package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.lang.reflect.TypeVariable;

import static tech.metavm.object.type.ResolutionStage.INIT;

public class TypeVariableParser extends DefParser<Object, ClassInstance, TypeVariableDef> {

    private final TypeVariable<?> javaTypeVariable;
    private final Class<?> declaringClass;
    private final DefContext defMap;
    private tech.metavm.object.type.TypeVariable type;

    public TypeVariableParser(TypeVariable<?> javaTypeVariable, DefContext defMap) {
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

    private tech.metavm.object.type.TypeVariable createType() {
        var declaringType = defMap.getPojoDef(declaringClass, INIT).getType();
        return type = new tech.metavm.object.type.TypeVariable(
                null,
                EntityUtils.getMetaTypeVariableName(javaTypeVariable),
                javaTypeVariable.getName(),
                declaringType);
    }

    @Override
    public void generateSignature() {
        type.setBounds(NncUtils.map(javaTypeVariable.getBounds(), b -> defMap.getType(b, INIT)));
    }

    @Override
    public void generateDeclaration() {

    }

    @Override
    public void generateDefinition() {

    }

}
