package org.metavm.compiler.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.element.ConstantTags;
import org.metavm.compiler.element.ElementTable;
import org.metavm.compiler.element.ElementWriter;
import org.metavm.compiler.element.Func;
import org.metavm.compiler.syntax.IntersectionTypeNode;
import org.metavm.compiler.syntax.TypeNode;
import org.metavm.compiler.util.List;
import org.metavm.util.MvOutput;

import javax.annotation.Nullable;
import java.util.stream.Collectors;

@Slf4j
public final class IntersectionType implements Type, Comparable<IntersectionType> {
    private final List<Type> bounds;
    private @Nullable Closure closure;
    private @Nullable ElementTable table;

    IntersectionType(List<Type> bounds) {
        this.bounds = bounds;
    }

    @Override
    public void writeType(ElementWriter writer) {
        writer.writeTypes(bounds, " & ");
    }

    @Override
    public boolean isAssignableFrom(Type type) {
        return this == type || type == PrimitiveType.NEVER || bounds.allMatch(t -> t.isAssignableFrom(type));
    }

    @Override
    public <R> R accept(TypeVisitor<R> visitor) {
        return visitor.visitIntersectionType(this);
    }

    @Override
    public int getTag() {
        return TypeTags.TAG_INTERSECTION;
    }

    @Override
    public String getInternalName(@Nullable Func current) {
        return bounds.stream().map(t -> t.getInternalName(current)).sorted().collect(Collectors.joining("&"));
    }

    @Override
    public TypeNode makeNode() {
        return new IntersectionTypeNode(bounds.map(Type::makeNode));
    }

    @Override
    public Closure getClosure() {
        if (closure == null) {
            var cl = Closure.nil;
            for (Type bound : bounds) {
                cl = cl.intersection(bound.getClosure());
            }
            closure = cl;
        }
        return closure;
    }

    @Override
    public int compareTo(@NotNull IntersectionType o) {
        if (this == o)
            return 0;
        if (o instanceof IntersectionType that)
            return Types.instance.compareTypes(bounds, that.bounds);
        else
            return Integer.compare(getTag(), o.getTag());
    }

    public List<Type> getBounds() {
        return bounds;
    }

    @Override
    public String toString() {
        return "IntersectionType[" +
                "bounds=" + bounds + ']';
    }

    @Override
    public void write(MvOutput output) {
        output.write(ConstantTags.INTERSECTION_TYPE);
        output.writeList(bounds, t -> t.write(output));
    }

    @Override
    public ElementTable getTable() {
        if (table == null) {
            table = new ElementTable();
            bounds.forEachBackwards(t -> table.addAll(t.getTable()));
        }
        return table;
    }
}
