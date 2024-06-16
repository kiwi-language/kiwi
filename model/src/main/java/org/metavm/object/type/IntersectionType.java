package org.metavm.object.type;

import org.metavm.entity.ElementVisitor;
import org.metavm.api.EntityType;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.ValueArray;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.IntersectionTypeKey;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.object.type.rest.dto.TypeKeyCodes;
import org.metavm.object.type.rest.dto.TypeParam;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@EntityType
public class IntersectionType extends CompositeType {

    private final ValueArray<Type> types;

    private transient Set<Type> typeSet;

    public IntersectionType(Set<Type> types) {
        super(getName(types), getCode(types), false, false, TypeCategory.INTERSECTION);
        if(types.isEmpty())
            throw new IllegalArgumentException("types can not be empty");
        this.types = new ValueArray<>(Type.class, types);
    }

    private static String getName(Iterable<Type> types) {
        return NncUtils.join(types, Type::getName, "&");
    }

    private static @Nullable String getCode(Iterable<Type> types) {
        if(NncUtils.allMatch(types, t -> t.getCode() != null))
            return NncUtils.join(types, Type::getCode, "&");
        else
            return null;
    }

    @Override
    public TypeKey toTypeKey(Function<TypeDef, Id> getTypeDefId) {
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
    protected TypeParam getParamInternal() {
        return null;
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

    @Nullable
    @Override
    public String getCode() {
        if(NncUtils.anyMatch(types, t -> t.getCode() == null))
            return null;
        return NncUtils.join(types, Type::getCode, "&");
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
    public String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr) {
        return NncUtils.join(types, type -> type.toExpression(serializeContext, getTypeDefExpr), "&");
    }

    @Override
    public int getTypeKeyCode() {
        return TypeKeyCodes.INTERSECTION;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.INTERSECTION);
        output.writeInt(types.size());
        types.forEach(t -> t.write(output));
    }

    public static IntersectionType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        var numTypes = input.readInt();
        var types = new HashSet<Type>(numTypes);
        for (int i = 0; i < numTypes; i++)
            types.add(Type.readType(input, typeDefProvider));
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

}
