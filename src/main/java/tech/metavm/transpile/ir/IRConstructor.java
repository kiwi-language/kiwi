package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import java.util.List;

public record IRConstructor(List<Modifier> modifiers,
                            List<IRAnnotation> annotations,
                            List<TypeVariable<IRConstructor>> typeParameters,
                            IRClass declaringClass,
                            List<IRParameter> parameters,
                            CodeBlock block)
        implements InternalGenericDeclaration<IRConstructor>, IRFunction {

    public IRConstructor {
        declaringClass.addConstructor(this);
    }

    public List<IRType> parameterTypes() {
        return NncUtils.map(parameters, IRParameter::type);
    }

    @Override
    public FunctionType functionType() {
        return new FunctionType(parameterTypes(), declaringClass);
    }

    @Override
    public void addTypeParameter(TypeVariable<IRConstructor> typeVariable) {
        typeParameters.add(typeVariable);
    }

    public FunctionType getFunctionType() {
        return new FunctionType(
                parameterTypes(),
                declaringClass
        );
    }

}
