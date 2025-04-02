package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public final class MethodInst extends ElementBase implements MethodRef, FuncInst, Constant, MemberRef {
    private final ClassType declType;
    private final Method method;
    private final List<Type> typeArgs;
    private FuncType type;
    private final Map<List<Type>, MethodInst> insts = new HashMap<>();
    private final TypeSubst subst;

    public MethodInst(ClassType declType,
                      Method method,
                      List<Type> typeArgs) {
        this.declType = declType;
        this.method = method;
        this.typeArgs = typeArgs;
        subst = new TypeSubst(method.getTypeParams(), typeArgs);
        this.type = computeType();
    }

    @Override
    public Name getName() {
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
        writer.writeType(declType);
        writer.write(".");
        writer.write(method.getName());
        writer.write("<");
        writer.writeTypes(typeArgs);
        writer.write(">");
        writer.write("(");
        writer.writeTypes(type.getParamTypes());
        writer.write(") -> ");
        writer.writeType(type.getRetType());
    }

    public ClassType getDeclType() {
        return declType;
    }

    public Method getFunc() {
        return method;
    }

    public List<Type> getTypeArgs() {
        return typeArgs;
    }

    public List<Type> getParamTypes() {
        return type.getParamTypes();
    }

    public Type getRetType() {
        return type.getRetType();
    }

    public FuncRef getInst(List<Type> typeArguments) {
        if (typeArguments.equals(getTypeArgs()))
            return this;
        return insts.computeIfAbsent(typeArguments, k ->
                new MethodInst(declType, method, typeArguments));
    }

    @Override
    public String toString() {
        return "MethodInst " + getText();
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.METHOD_REF);
        declType.write(output);
        Elements.writeReference(method, output);
        output.writeList(typeArgs, t -> t.write(output));
    }

    public boolean isStatic() {
        return method.isStatic();
    }

    public Access getAccess() {
        return method.getAccess();
    }

    public boolean isInit() {
        return method.isInit();
    }

    public void onMethodTypeChange() {
        type = computeType();
        insts.values().forEach(MethodInst::onMethodTypeChange);
    }

    private FuncType computeType() {
        return (FuncType) method.getType().accept(declType.getSubstitutor()).accept(subst);
    }

    @Override
    public FuncType getType() {
        return type;
    }

    @Override
    public void load(Code code, Env env) {
        if (method == ArrayType.appendMethod)
            throw new UnsupportedOperationException();
        else if (isStatic())
            code.getStaticMethod(this);
        else
            code.getMethod(this);
    }

    @Override
    public void store(Code code, Env env) {
        throw new AnalysisException("Cannot assign to a method: " + method.getQualName());
    }

    @Override
    public void invoke(Code code, Env env) {
        if (method == ArrayType.appendMethod)
            code.arrayAdd();
        else if (isStatic())
            code.invokeStatic(this);
        else if(getAccess() == Access.PRIVATE || isInit())
            code.invokeSpecial(this);
        else
            code.invokeVirtual(this);
    }
}
