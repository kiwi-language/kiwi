package org.metavm.compiler.element;

import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.FunctionType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.type.TypeSubstitutor;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public final class MethodInst implements FuncInst, Constant, MemberRef {
    private final ClassType declaringType;
    private final Method method;
    private final List<Type> typeArguments;
    private final FunctionType type;
    private final Map<List<Type>, MethodInst> instances = new HashMap<>();


    public MethodInst(ClassType declaringType,
                      Method method,
                      List<Type> typeArguments,
                      FunctionType type) {
        this.declaringType = declaringType;
        this.method = method;
        this.typeArguments = typeArguments;
        this.type = type;
        instances.put(typeArguments, this);
    }

    @Override
    public SymName getName() {
        return method.getName();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMethodInst(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {
    }

    @Override
    public void write(ElementWriter writer) {
        writer.writeType(declaringType);
        writer.write(".");
        if (!typeArguments.isEmpty()) {
            writer.write("<");
            writer.writeTypes(typeArguments);
            writer.write(">");
        }
        writer.write(method.getName());
        writer.write("(");
        writer.writeTypes(type.getParameterTypes());
        writer.write(") -> ");
        writer.writeType(type.getReturnType());
    }

    public ClassType getDeclaringType() {
        return declaringType;
    }

    public Method getFunction() {
        return method;
    }

    public List<Type> getTypeArguments() {
        return typeArguments;
    }

    public List<Type> getParameterTypes() {
        return type.getParameterTypes();
    }

    public Type getReturnType() {
        return type.getReturnType();
    }

    public MethodInst getInstance(List<Type> typeArguments) {
        if (typeArguments.isEmpty())
            return this;
        var subst = new TypeSubstitutor(this.typeArguments, typeArguments);
        return instances.computeIfAbsent(typeArguments, k ->
                new MethodInst(declaringType, method, typeArguments,
                        (FunctionType) type.accept(subst)
                ));
    }

    @Override
    public String toString() {
        return "MethodInst " + getText();
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.METHOD_REF);
        declaringType.write(output);
        Elements.writeReference(method, output);
        output.writeList(typeArguments, t -> t.write(output));
    }

    public boolean isStatic() {
        return method.isStatic();
    }

    public Access getAccess() {
        return method.getAccess();
    }

    public boolean isConstructor() {
        return method.isConstructor();
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void load(Code code) {
        if (isStatic())
            code.getStaticMethod(this);
        else
            code.getMethod(this);
    }

    @Override
    public void store(Code code) {
        throw new AnalysisException("Cannot assign to a method: " + method.getQualifiedName());
    }

    @Override
    public void invoke(Code code) {
        if (isStatic())
            code.invokeStatic(this);
        else if(getAccess() == Access.PRIVATE || isConstructor())
            code.invokeSpecial(this);
        else
            code.invokeVirtual(this);
    }
}
