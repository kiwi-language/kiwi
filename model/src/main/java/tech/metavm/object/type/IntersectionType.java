package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.IntersectionTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeKeyCodes;
import tech.metavm.object.type.rest.dto.TypeParam;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@EntityType("类型交集")
public class IntersectionType extends CompositeType {

    @ChildEntity("类型列表")
    private final ChildArray<Type> types = addChild(new ChildArray<>(Type.class), "types");

    private transient Set<Type> typeSet;

    public IntersectionType(Set<Type> types) {
        super(getName(types), getCode(types), false, false, TypeCategory.INTERSECTION);
        if(types.isEmpty())
            throw new IllegalArgumentException("types can not be empty");
        this.types.addChildren(NncUtils.map(types, Type::copy));
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
    public TypeKey toTypeKey(Function<TypeDef, String> getTypeDefId) {
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
    public IntersectionType copy() {
        return new IntersectionType(NncUtils.mapUnique(types, Type::copy));
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
    public void write0(InstanceOutput output) {
        output.writeInt(types.size());
        types.forEach(t -> t.write(output));
    }

    public static IntersectionType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        var types = new HashSet<Type>();
        for (int i = 0; i < input.readInt(); i++)
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
