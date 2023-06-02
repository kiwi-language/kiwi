package tech.metavm.transpile.ir;

import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class IRMethod implements ISymbol, IRFunction, InternalGenericDeclaration<IRMethod> {
    private final String name;
    private final List<Modifier> modifiers;
    private final List<IRAnnotation> annotations;
    private final List<TypeVariable<IRMethod>> typeParameters = new ArrayList<>();
    private List<IRType> throwsTypes;
    private IRType returnType;
    private List<IRParameter> parameters;
    private final IRClass declaringClass;
    @Nullable
    private CodeBlock body;

    public IRMethod(
            String name,
            List<Modifier> modifiers,
            List<IRAnnotation> annotations,
//            IRType returnType,
//            List<IRParameter> parameters,
//            List<IRType> throwsTypes,
            IRClass declaringClass
    ) {
        this.name = name;
        this.modifiers = modifiers;
        this.annotations = annotations;
//        this.returnType = returnType;
//        this.parameters = parameters;
        this.declaringClass = declaringClass;
//        this.throwsTypes = new ArrayList<>(throwsTypes);
        declaringClass.addMethod(this);
    }

    public void initialize(
            IRType returnType,
            List<IRParameter> parameters,
            List<IRType> throwsTypes
    ) {
        this.returnType = returnType;
        this.parameters = new ArrayList<>(parameters);
        this.throwsTypes = new ArrayList<>(throwsTypes);
    }

    public boolean matches(String name, List<IRType> parameterTypes) {
        return this.name.equals(name) && this.parameterTypes().equals(parameterTypes);
    }

    @Override
    public String name() {
        return name;
    }

    public List<Modifier> modifiers() {
        return modifiers;
    }

    public List<IRAnnotation> annotations() {
        return annotations;
    }

    public IRType returnType() {
        return returnType;
    }

    public List<IRParameter> parameters() {
        return Collections.unmodifiableList(parameters);
    }

    public List<IRType> parameterTypes() {
        return NncUtils.map(parameters, IRParameter::type);
    }

    public IRType parameterType(int index) {
        return parameters.get(index).type();
    }

    public IRClass declaringClass() {
        return declaringClass;
    }

    @Nullable
    public CodeBlock getBody() {
        return body;
    }

    public void setBody(@Nullable CodeBlock body) {
        this.body = body;
    }

    public void addTypeParameter(TypeVariable<IRMethod> typeVariable) {
        this.typeParameters.add(typeVariable);
    }

    public boolean isStatic() {
        return modifiers.contains(Modifier.STATIC);
    }

    public boolean isDefault() {
        return modifiers.contains(Modifier.DEFAULT);
    }

    public String signature() {
        return buildSignature(name, NncUtils.map(parameters, IRParameter::type));
    }

    public List<TypeVariable<IRMethod>> typeParameters() {
        return typeParameters;
    }

    public static String buildSignature(String name, List<IRType> type) {
        return name + "(" + NncUtils.join(type, IRType::getName, ",") + ")";
    }

    public FunctionType functionType() {
        return new FunctionType(parameterTypes(), returnType);
    }
}
