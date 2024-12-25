package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.entity.ValueArray;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.IntersectionTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.NncUtils;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@Entity
public class IntersectionType extends CompositeType {

    private final ValueArray<Type> types;

    private transient Set<Type> typeSet;

    public IntersectionType(Set<Type> types) {
        super();
        if(types.isEmpty())
            throw new IllegalArgumentException("types can not be empty");
        this.types = new ValueArray<>(Type.class, types);
    }

    private IntersectionType(List<Type> types) {
        this.types = new ValueArray<>(Type.class, types);
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new IntersectionTypeKey(new HashSet<>(NncUtils.map(types, type -> type.toTypeKey(getTypeDefId))));
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return NncUtils.allMatch(this.types, t -> t.isAssignableFrom(that));
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitIntersectionType(this, s);
    }

    @Override
    public List<Type> getComponentTypes() {
        return Collections.unmodifiableList(types.toList());
    }

    @Override
    public List<? extends Type> getSuperTypes() {
        return Collections.unmodifiableList(types.toList());
    }

    @Override
    public String getName() {
        return NncUtils.join(types, Type::getName, "&");
    }

    @Override
    public String getTypeDesc() {
        return NncUtils.join(types, Type::getTypeDesc, "&");
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.INTERSECTION;
    }

    @Override
    public Type getType() {
        return StdKlass.intersectionType.type();
    }

    @Override
    public boolean isEphemeral() {
        return false;
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        var names = NncUtils.mapAndSort(types, type -> type.getInternalName(current), String::compareTo);
        return NncUtils.join(names, "&");
    }

    public Set<Type> getTypes() {
        return new HashSet<>(types.toList());
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIntersectionType(this);
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return NncUtils.join(types, type -> {
            var memberExpr = type.toExpression(serializeContext, getTypeDefExpr);
            if(type.getPrecedence() >= getPrecedence())
                memberExpr = "("+ memberExpr + ")";
            return memberExpr;
        }, "&");
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.INTERSECTION_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.INTERSECTION_TYPE);
        output.writeInt(types.size());
        types.forEach(t -> t.write(output));
    }

    @Override
    public int getPrecedence() {
        return 2;
    }

    public static IntersectionType read(MvInput input) {
        var numTypes = input.readInt();
        var types = new ArrayList<Type>(numTypes);
        for (int i = 0; i < numTypes; i++)
            types.add(input.readType());
        return new IntersectionType(types);
    }

    @Override
    protected boolean equals0(Object obj) {
        return obj instanceof IntersectionType that && typeSet().equals(that.typeSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeSet());
    }

    private Set<Type> typeSet() {
        if(typeSet == null)
            typeSet = new HashSet<>();
        return typeSet;
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
        return NncUtils.allMatch(types, Type::isNullable);
    }
}
