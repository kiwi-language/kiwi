package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static org.metavm.flow.Method.*;

@Slf4j
public class Method extends Func implements MethodRef, Member, Executable, Comparable<Method> {

    private Access access;
    private final Clazz declClass;
    private boolean static_;
    private boolean abstract_;
    private boolean init;
    private final Map<List<Type>, MethodInst> insts = new HashMap<>();

    public Method(String name, Access access, boolean static_, boolean abstract_, boolean init, Clazz declClass) {
        this(NameTable.instance.get(name), access, static_, abstract_, init, declClass);
    }

    public Method(Name name, Access access, boolean static_, boolean abstract_, boolean init, Clazz declClass) {
        super(name);
        this.access = access;
        this.static_ = static_;
        this.abstract_ = abstract_;
        this.init = init;
        this.declClass = declClass;
        declClass.addMethod(this);
    }

    @Override
    public ClassType getDeclType() {
        return declClass;
    }

    @Override
    public void load(Code code, Env env) {
        if (this == ArrayType.appendMethod)
            throw new UnsupportedOperationException();
        else if (isStatic())
            code.getStaticMethod(this);
        else
            code.getMethod(this);
    }

    @Override
    public void store(Code code, Env env) {
        throw new AnalysisException("Cannot assign to a method: " + getQualName());
    }

    @Override
    public Access getAccess() {
        return access;
    }

    @Override
    public void invoke(Code code, Env env) {
        if (this == ArrayType.appendMethod)
            code.arrayAdd();
        else if (isStatic())
            code.invokeStatic(this);
        else if(getAccess() == Access.PRIVATE || isInit())
            code.invokeSpecial(this);
        else
            code.invokeVirtual(this);
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public Clazz getDeclClass() {
        return declClass;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitMethod(this);
    }

    @Override
    public void write(ElementWriter writer) {
        if (access != Access.PACKAGE) {
            writer.write(access.name().toLowerCase());
            writer.write(" ");
        }
        super.write(writer);
    }

    public Name getQualName() {
        return declClass.getName().concat("." + getName());
    }

    public String getInternalName(@Nullable Func current) {
        if (current == this)
            return "this";
        return declClass.getQualName() + "." + getLegacyName() + "(" +
                getParams().map(p -> p.getType().getInternalName(this)).join(",") + ")";
    }

    public Name getLegacyName() {
        return isInit() ? getDeclClass().getName() :  getName();
    }

    @Override
    public int compareTo(@NotNull Method o) {
        if (this == o)
            return 0;
        var r = declClass.compareTo(o.declClass);
        if (r != 0)
            return r;
        r = getName().compareTo(o.getName());
        if (r != 0)
            return r;
        return Types.instance.compareTypes(getParamTypes(), o.getParamTypes());
    }

    public boolean isStatic() {
        return static_;
    }

    public int getFlags() {
        int flags = super.getFlags();
        if(init) flags |= FLAG_CONSTRUCTOR;
        if(abstract_) flags |= FLAG_ABSTRACT;
        if(static_) flags |= FLAG_STATIC;
//        if(hidden) flags |= FLAG_HIDDEN;
        return flags;
    }

    @Override
    public void addParam(Param param) {
        super.addParam(param);
        for (var instance : insts.values()) {
            instance.onMethodTypeChange();
        }
    }

    @Override
    public void setParams(List<Param> params) {
        super.setParams(params);
        for (var instance : insts.values()) {
            instance.onMethodTypeChange();
        }
    }

    public boolean hasBody() {
        return !abstract_;
    }

    public void setStatic(boolean static_) {
        this.static_ = static_;
    }

    public boolean isAbstract() {
        return abstract_;
    }

    public void setAbstract(boolean abstract_) {
        this.abstract_ = abstract_;
    }

    @Override
    public boolean isInit() {
        return init;
    }

    public void setInit(boolean init) {
        this.init = init;
    }

    public List<Attribute> getAttributes() {
        return List.nil();
    }

    public List<CapturedType> getCapturedTypeVariables() {
        return List.nil();
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.METHOD_REF);
        this.getDeclType().write(output);
        Elements.writeReference(this, output);
        output.writeList(getTypeParams(), t -> t.write(output));
    }

    @Override
    public FuncRef getInst(List<Type> typeArguments) {
        if (typeArguments.equals(getTypeParams()))
            return this;
        return insts.computeIfAbsent(typeArguments, k ->
                new MethodInst(declClass, this, typeArguments));
    }

    @Override
    public String toString() {
        return getQualName().toString();
    }
}
