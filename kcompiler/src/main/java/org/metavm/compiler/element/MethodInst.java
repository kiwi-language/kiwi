package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;

import java.util.function.Consumer;

@Slf4j
public final class MethodInst extends ElementBase implements MethodRef, FuncInst, Constant, MemberRef {
    private final MethodRef method;
    private final List<Type> typeArgs;
    private FuncType type;
    private final TypeSubst subst;

    public MethodInst(MethodRef method,
                      List<Type> typeArgs) {
        this.method = method;
        this.typeArgs = typeArgs;
        if (!(method instanceof GenericDecl genDecl))
            throw new IllegalArgumentException(method.getName() + " is not a generic method");
        subst = TypeSubst.create(genDecl.getTypeParams(), typeArgs);
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
        writer.writeType(getDeclType());
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
        return method.getDeclType();
    }

    public FuncRef getFunc() {
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

    @Override
    public String toString() {
        return method.getDeclType().getTypeText() + "."
                + method.getName() + "<" + Utils.join(typeArgs, Type::getTypeText) + ">(" +
                Utils.join(getParamTypes(), Type::getTypeText)
                + ")";
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.METHOD_REF);
        getDeclType().write(output);
        var rawMethod = method instanceof PartialMethodInst pm ? pm.getFunc() : method;
        Elements.writeReference(rawMethod, output);
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
    }

    private FuncType computeType() {
        return (FuncType) method.getType().accept(subst);
    }

    @Override
    public FuncType getType() {
        return type;
    }

    @Override
    public void load(Code code, Env env) {
        if (method == ArrayType.appendMethod || method == ArrayType.removeMethod)
            throw new UnsupportedOperationException();
        else if (isStatic())
            code.getStaticMethod(this);
        else
            code.getMethod(this);
    }

    @Override
    public void store(Code code, Env env) {
        throw new AnalysisException("Cannot assign to a method: " + method.getName());
    }

    @Override
    public void invoke(Code code, Env env) {
        if (getRawMethod() == ArrayType.mapMethod) {
            var project = env.getProject();
            var func = project.getRootPackage().getFunction(NameTable.instance.map);
            var typeArgs = List.of(
                    getDeclType().getTypeArguments().getFirst(), getTypeArgs().getFirst()
            );
            var funcInst = func.getInst(typeArgs);
            code.invokeFunction(funcInst);
        }
        else if (isStatic())
            code.invokeStatic(this);
        else if(getAccess() == Access.PRIVATE || isInit())
            code.invokeSpecial(this);
        else
            code.invokeVirtual(this);
    }

    @Override
    public Method getRawMethod() {
        return method.getRawMethod();
    }
}
