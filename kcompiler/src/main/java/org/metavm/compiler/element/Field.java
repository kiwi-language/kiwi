package org.metavm.compiler.element;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.FuncType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

import javax.annotation.Nullable;
import java.util.function.Consumer;

import static org.metavm.object.type.Field.FLAG_STATIC;

public final class Field extends ElementBase implements Member, FieldRef {

    private Name name;
    private Type type;
    private Access access;
    private boolean static_;
    private boolean deleted;
    private Integer sourceTag;
    private final Clazz declaringClass;
    private @Nullable Method initializer;
    private List<FieldInst> instances = List.nil();

    public Field(String name, Type type, Access access, boolean static_, Clazz declaringClass) {
        this(NameTable.instance.get(name), type, access, static_, false, declaringClass);
    }

    public Field(Name name, Type type, Access access, boolean static_, boolean deleted, Clazz declaringClass) {
        this.name = name;
        this.type = type;
        this.access = access;
        this.static_ = static_;
        this.deleted = deleted;
        this.declaringClass = declaringClass;
        declaringClass.addField(this);
    }

    @Override
    public Name getName() {
        return name;
    }

    @Override
    public void invoke(Code code, Env env) {
        load(code, env);
        code.call((FuncType) type);
    }

    @Override
    public ClassType getDeclType() {
        return declaringClass;
    }

    @Override
    public void setName(Name name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public Element getElement() {
        return this;
    }

    public void setType(Type type) {
        this.type = type;
        instances.forEach(FieldInst::onFieldTypeChange);
    }

    public Access getAccess() {
        return access;
    }

    public void setAccess(Access access) {
        this.access = access;
    }

    public Clazz getDeclClass() {
        return declaringClass;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitField(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {
    }

    public String getQualifiedName() {
        return declaringClass.getName() + "." + getName();
    }

    @Override
    public void write(ElementWriter writer) {
        if (access != Access.PACKAGE) {
            writer.write(access.name().toLowerCase());
            writer.write(" ");
        }
        writer.write(getName());
        writer.write(": ");
        writer.writeType(getType());
    }

    public boolean isStatic() {
        return static_;
    }

    public void setStatic(boolean static_) {
        this.static_ = static_;
    }

    public Integer getSourceTag() {
        return sourceTag;
    }

    public int getFlags() {
        int flags = 0;
        if (static_) flags |= FLAG_STATIC;
//        if(readonly)
//            flags |= FLAG_READONLY;
//        if (isTransient) flags |= FLAG_TRANSIENT;
//        if (lazy) flags |= FLAG_LAZY;
//        if (isEnumConstant) flags |= FLAG_ENUM_CONSTANT;
        return flags;
    }

    public int getOrdinal() {
        return 0;
    }

    public int getSince() {
        return 0;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public @Nullable Method getInitializer() {
        return initializer;
//        return (MethodInst) declaringClass.getType().getTable().lookupFirst(
//                SymName.from("__" + name.toString() + "__"),
//                e -> e instanceof MethodInst m && m.getParameterTypes().isEmpty()
//                        && m.isStatic() == static_ && type.isAssignableFrom(m.getReturnType())
//        );
    }

    public void setInitializer(@Nullable Method initializer) {
        this.initializer = initializer;
    }

    void addInstance(FieldInst fieldInst) {
        instances = instances.prepend(fieldInst);
    }

}
