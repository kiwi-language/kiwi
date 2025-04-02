package org.metavm.compiler.element;

import org.metavm.compiler.analyze.Env;
import org.metavm.compiler.generate.Code;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.Type;

import java.util.function.Consumer;

import static org.metavm.object.type.Field.FLAG_ENUM_CONSTANT;
import static org.metavm.object.type.Field.FLAG_STATIC;

public final class EnumConst extends ElementBase implements FieldRef {

    private Name name;
    private int ordinal;
    private final Clazz declaringClass;
    private Type type;
    private Method initializer;

    public EnumConst(Name name, int ordinal, Clazz declaringClass, Type type) {
        this.name = name;
        this.ordinal = ordinal;
        this.declaringClass = declaringClass;
        this.type = type;
        declaringClass.addEnumConstant(this);
    }

    public Name getName() {
        return name;
    }

    @Override
    public void invoke(Code code, Env env) {
        throw new AnalysisException("Enum constant is not callable");
    }

    @Override
    public boolean isStatic() {
        return true;
    }

    public Clazz getDeclaringClass() {
        return declaringClass;
    }

    @Override
    public ClassType getDeclType() {
        return declaringClass;
    }

    public void setName(Name name) {
        this.name = name;
    }

    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public Element getElement() {
        return this;
    }

    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitEnumConstant(this);
    }

    public void forEachChild(Consumer<Element> action) {
    }

    public void write(ElementWriter writer) {
        writer.write(name);
    }

    public String getQualifiedName() {
        return declaringClass.getName() + "." + name;
    }

    public Integer getSourceTag() {
        return null;
    }

    public int getFlags() {
        return FLAG_STATIC | FLAG_ENUM_CONSTANT;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public int getSince() {
        return 0;
    }

    public Method getInitializer() {
        return initializer;
    }

    public void setInitializer(Method initializer) {
        this.initializer = initializer;
    }
}
