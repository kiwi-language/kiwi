package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.rest.dto.IntersectionTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
public class IntersectionType extends CompositeType {

    @SuppressWarnings("unused")
    private static Klass __klass__;
    private final Type[] types;

    public IntersectionType(Set<Type> types) {
        super();
        if(types.isEmpty())
            throw new IllegalArgumentException("types can not be empty");
        this.types = types.toArray(Type[]::new);
    }

    private IntersectionType(Type[] types) {
        this.types = types;
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new IntersectionTypeKey(new HashSet<>(Utils.map(types, type -> type.toTypeKey(getTypeDefId))));
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        for (Type type : types) {
            if (!type.isAssignableFrom(that)) return false;
        }
        return true;
    }

    @Override
    public List<Type> getComponentTypes() {
        return List.of(types);
    }

    @Override
    public List<? extends Type> getSuperTypes() {
        return List.of(types);
    }

    @Override
    public String getName() {
        return Utils.join(List.of(types), Type::getName, "&");
    }

    @Override
    public String getTypeDesc() {
        return Utils.join(List.of(types), Type::getTypeDesc, "&");
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.INTERSECTION;
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        var names = Utils.mapAndSort(List.of(types), type -> type.getInternalName(current), String::compareTo);
        return Utils.join(names, "&");
    }

    public Set<Type> getTypes() {
        return new HashSet<>(List.of(types));
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return Arrays.stream(types).map(type -> {
            var memberExpr = type.toExpression(serializeContext, getTypeDefExpr);
            if(type.getPrecedence() >= getPrecedence())
                memberExpr = "("+ memberExpr + ")";
            return memberExpr;
        }).sorted().collect(Collectors.joining("&"));
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.INTERSECTION_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.INTERSECTION_TYPE);
        output.writeArray(types, t -> t.write(output));
    }

    @Override
    public int getPrecedence() {
        return 2;
    }

    public static IntersectionType read(MvInput input) {
        return new IntersectionType(input.readArray(input::readType, Type[]::new));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof IntersectionType that) {
            var m1 = types;
            var m2 = that.types;
            if (m1.length == m2.length) {
                out: for (Type t1 : m1) {
                    for (Type t2 : m2) {
                        if (t1.equals(t2)) continue out;
                    }
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Type obj : types) {
            if (obj != null)
                h += obj.hashCode();
        }
        return h;
    }

    public IntersectionType flatten() {
        var flattenedTypes = new HashSet<Type>();
        for (Type t : types) {
            if(t instanceof IntersectionType i)
                flattenedTypes.addAll(i.getTypes());
            else
                flattenedTypes.add(t);
        }
        return new IntersectionType(flattenedTypes);
    }

    @Override
    public boolean isNullable() {
        return Utils.allMatch(List.of(types), Type::isNullable);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIntersectionType(this);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitIntersectionType(this, s);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        for (Type type : types) {
            type.accept(visitor);
        }
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        for (var types_ : types) types_.forEachReference(action);
    }
}
