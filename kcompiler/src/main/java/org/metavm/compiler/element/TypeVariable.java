package org.metavm.compiler.element;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.syntax.ClassTypeNode;
import org.metavm.compiler.syntax.Ident;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.function.Consumer;

public class TypeVariable implements Type, Element, Comparable<TypeVariable> {

    private SymName name;
    private Type bound;
    private final GenericDeclaration genericDeclaration;
    private @Nullable Closure closure;

    public TypeVariable(String name, Type bound, GenericDeclaration genericDeclaration) {
        this(SymNameTable.instance.get(name),bound, genericDeclaration);
    }

    public TypeVariable(SymName name, Type bound, GenericDeclaration genericDeclaration) {
        this.name = name;
        this.bound = bound;
        this.genericDeclaration = genericDeclaration;
        genericDeclaration.addTypeParameter(this);
    }

    @Override
    public SymName getName() {
        return name;
    }

    public void setName(SymName name) {
        this.name = name;
    }

    public void setBound(Type bound) {
        this.bound = bound;
    }

    public GenericDeclaration getGenericDeclaration() {
        return genericDeclaration;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTypeVariable(this);
    }

    @Override
    public void forEachChild(Consumer<Element> action) {

    }

    @Override
    public void write(ElementWriter writer) {
        writer.write(name);
        if (bound != PrimitiveType.ANY) {
            writer.write(": ");
            writer.writeType(bound);
        }
    }

    @Override
    public String getText() {
        return Type.super.getText();
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        return type == PrimitiveType.NEVER || type == this;
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitTypeVariable(this);
    }

    @Override
    public int getTag() {
        return TypeTags.TAG_VARIABLE;
    }

    @Override
    public int compareTo(@NotNull TypeVariable that) {
        //noinspection rawtypes,unchecked
        var r = ((Comparable) genericDeclaration).compareTo(that.genericDeclaration);
        if (r != 0)
            return r;
        return name.compareTo(that.name);
    }

    @Override
    public ElementTable getTable() {
        return bound.getTable();
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return genericDeclaration.getInternalName(current) + "." + name;
    }

    @Override
    public TypeNode makeNode() {
        return new ClassTypeNode(new Ident(name));
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.VARIABLE_TYPE);
        Elements.writeReference(this, output);
    }

    public Type getBound() {
        return bound;
    }

    public List<Attribute> getAttributes() {
        return List.nil();
    }

    @Override
    public Closure getClosure() {
        if (closure == null) {
            var cl = bound.getClosure();
            closure = cl.insert(this);
        }
        return closure;
    }
}
