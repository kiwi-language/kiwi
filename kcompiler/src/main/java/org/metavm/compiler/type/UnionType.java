package org.metavm.compiler.type;

import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.element.ConstantTags;
import org.metavm.compiler.element.ElementTable;
import org.metavm.compiler.element.ElementWriter;
import org.metavm.compiler.element.Func;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.syntax.UnionTypeNode;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

public final class UnionType implements Type, Comparable<UnionType> {
    private final List<Type> alternatives;
    private @Nullable Closure closure;
    private @Nullable Type lub;

    UnionType(List<Type> alternatives) {
        this.alternatives = alternatives;
    }

    @Override
    public void writeType(ElementWriter writer) {
        writer.writeTypes(alternatives, "|");
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        if (this == type)
            return true;
        return alternatives.anyMatch(t -> t.isAssignableFrom(type));
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitUnionType(this);
    }

    @Override
    public int getTag() {
        return TypeTags.TAG_UNION;
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return alternatives.stream().map(t -> t.getInternalName(current))
                .sorted()
                .collect(Collectors.joining("|"));
    }

    @Override
    public int compareTo(@NotNull UnionType o) {
        if (this == o)
            return 0;
        if (o instanceof UnionType that)
            return Types.instance.compareTypes(alternatives, that.alternatives);
        else
            return Integer.compare(getTag(), o.getTag());
    }

    public List<Type> alternatives() {
        return alternatives;
    }

    @Override
    public String toString() {
        return "UnionType[" +
                "alternatives=" + alternatives + ']';
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.UNION_TYPE);
        output.writeList(alternatives, t -> t.write(output));
    }

    @Override
    public Type getUnderlyingType() {
        var t1 = alternatives.head();
        var tail = alternatives.tail();
        assert tail.nonEmpty();
        if (tail.tail().isEmpty()) {
            var t2 = tail.head();
            if (t1 == PrimitiveType.NULL)
                return t2;
            if (t2 == PrimitiveType.NULL)
                return t1;
        }
        return this;
    }

    @Override
    public TypeNode makeNode() {
        var node = new UnionTypeNode(alternatives.map(Type::makeNode));
        node.setType(this);
        return node;
    }

    @Override
    public Closure getClosure() {
        if (closure == null) {
            var cl = Closure.nil;
            for (Type alternative : alternatives) {
                cl = cl.union(alternative.getClosure());
            }
            closure = cl;
        }
        return closure;
    }

    public Type getLUB() {
        if (lub == null)
            lub = Types.getLUB(alternatives);
        return lub;
    }

    @Override
    public ElementTable getTable() {
        return getLUB().getTable();
    }
}
