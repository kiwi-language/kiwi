package org.metavm.compiler.element;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.type.Types;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;

import java.util.Objects;

import static org.metavm.flow.Method.*;

@Slf4j
public class Method extends Func implements Member, Executable, Comparable<Method> {

    private Access access;
    private final Clazz declaringClass;
    private boolean static_;
    private boolean abstract_;
    private boolean constructor;

    public Method(String name, Access access, boolean static_, boolean abstract_, boolean constructor, Clazz declaringClass) {
        this(SymNameTable.instance.get(name), access, static_, abstract_, constructor, declaringClass);
    }

    public Method(SymName name, Access access, boolean static_, boolean abstract_, boolean constructor, Clazz declaringClass) {
        super(name);
        this.access = access;
        this.static_ = static_;
        this.abstract_ = abstract_;
        this.constructor = constructor;
        this.declaringClass = declaringClass;
        declaringClass.addMethod(this);
    }

    @Override
    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public Clazz getDeclaringClass() {
        return declaringClass;
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

    public SymName getQualifiedName() {
        return declaringClass.getName().concat("." + getName());
    }

    public String getInternalName(@Nullable Func current) {
        if (current == this)
            return "this";
        return declaringClass.getQualifiedName() + "." + getLegacyName() + "(" +
                getParameters().map(p -> p.getType().getInternalName(this)).join(",") + ")";
    }

    public SymName getLegacyName() {
        return isConstructor() ? getDeclaringClass().getName() :  getName();
    }

    @Override
    public int compareTo(@NotNull Method o) {
        if (this == o)
            return 0;
        var r = declaringClass.compareTo(o.declaringClass);
        if (r != 0)
            return r;
        r = getName().compareTo(o.getName());
        if (r != 0)
            return r;
        return Types.instance.compareTypes(getParameterTypes(), o.getParameterTypes());
    }

    public boolean isStatic() {
        return static_;
    }

    public int getFlags() {
        int flags = super.getFlags();
        if(constructor) flags |= FLAG_CONSTRUCTOR;
        if(abstract_) flags |= FLAG_ABSTRACT;
        if(static_) flags |= FLAG_STATIC;
//        if(hidden) flags |= FLAG_HIDDEN;
        return flags;
    }

    @Override
    public MethodInst getInstance() {
        return (MethodInst) Objects.requireNonNull(declaringClass.getType().getTable().lookupFirst(getName(),
                e -> e instanceof MethodInst m && m.getFunction() == this));
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

    public boolean isConstructor() {
        return constructor;
    }

    public void setConstructor(boolean constructor) {
        this.constructor = constructor;
    }

    public List<Attribute> getAttributes() {
        return List.nil();
    }

    public List<CapturedType> getCapturedTypeVariables() {
        return List.nil();
    }

}
