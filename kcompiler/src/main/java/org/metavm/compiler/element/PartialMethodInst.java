package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.Nullable;
import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

@Slf4j
public class PartialMethodInst extends ElementBase implements MethodRef, FuncInst, Constant, MemberRef, GenericDecl {

    private final Method method;
    private final ClassType declType;
    private FuncType type;
    private List<TypeVar> typeParams = List.nil();
    private final Map<List<Type>, MethodInst> insts = new HashMap<>();
    private final TypeSubst subst2;

    public PartialMethodInst(ClassType declType, Method method) {
        this.declType = declType;
        this.method = method;
        var subst = declType.getSubstitutor();
        method.getTypeParams().forEach(tv ->
                new TypeVar(tv.getName(), tv.getBound().accept(subst), this)
        );
        subst2 = TypeSubst.create(method.getTypeParams(), typeParams);
        this.type = computeType();
        method.addPartialInst(this);
    }

    private FuncType computeType() {
        return (FuncType) method.getType().accept(declType.getSubstitutor()).accept(subst2);
    }

    @Override
    public Name getName() {
        return method.getName();
    }

    @Override
    public void load(Code code, Env env) {
        if (isStatic())
            code.getStaticMethod(this);
        else
            code.getMethod(this);
    }

    @Override
    public void store(Code code, Env env) {
        throw new AnalysisException("Cannot assign to a method: " + method.getQualName());
    }


    public void onMethodTypeChange() {
        type = computeType();
        insts.values().forEach(MethodInst::onMethodTypeChange);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitPartialMethodInst(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {

    }

    @Override
    public void write(ElementWriter writer) {
        writer.writeType(declType);
        writer.write(".");
        writer.write(method.getName());
        writer.write("(");
        writer.writeTypes(type.getParamTypes());
        writer.write(") -> ");
        writer.writeType(type.getRetType());
    }

    @Override
    public FuncRef getFunc() {
        return method;
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.METHOD_REF);
        declType.write(output);
        Elements.writeReference(method, output);
        Utils.require(typeParams.isEmpty(), "Cannot write an uninstantiated method ref");
        output.writeInt(0);
    }

    @Override
    public List<Type> getTypeArgs() {
        return List.into(typeParams);
    }

    @Override
    public List<Type> getParamTypes() {
        return type.getParamTypes();
    }

    @Override
    public Type getRetType() {
        return type.getRetType();
    }

    @Override
    public FuncType getType() {
        return type;
    }

    @Override
    public List<TypeVar> getTypeParams() {
        return typeParams;
    }

    @Override
    public void addTypeParam(TypeVar typeVar) {
        typeParams = typeParams.append(typeVar);
    }

    @Override
    public Object getInternalName(@Nullable Func current) {
        throw new UnsupportedOperationException();
    }

    @Override
    public MethodRef getInst(List<Type> typeArguments) {
        if (typeArguments.equals(getTypeArgs()))
            return this;
        return insts.computeIfAbsent(typeArguments, k ->
                new MethodInst(this, typeArguments));
    }

    @Override
    public ClassType getDeclType() {
        return declType;
    }

    @Override
    public Access getAccess() {
        return method.getAccess();
    }

    @Override
    public boolean isInit() {
        return method.isInit();
    }

    @Override
    public boolean isStatic() {
        return method.isStatic();
    }

    @Override
    public void invoke(Code code, Env env) {
        if (method == ArrayType.appendMethod)
            code.arrayAdd();
        else if (method == ArrayType.forEachMethod) {
            var project = env.getProject();
            var func = project.getRootPackage().getFunction(NameTable.instance.forEach);
            var funcInst = func.getInst(declType.getTypeArguments());
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
        return method;
    }
}
