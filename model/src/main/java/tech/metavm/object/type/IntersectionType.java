package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.rest.dto.IntersectionTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeParam;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

@EntityType("类型交集")
public class IntersectionType extends CompositeType {

    public static final IndexDef<IntersectionType> KEY_IDX = new IndexDef<>(IntersectionType.class, "key");

    @ChildEntity("类型列表")
    private final ReadWriteArray<Type> types = addChild(new ReadWriteArray<>(Type.class), "types");

    public IntersectionType(Long tmpId, Set<Type> types) {
        super(getName(types), getCode(types), false, false, TypeCategory.INTERSECTION);
        setTmpId(tmpId);
        this.types.addAll(NncUtils.map(types, Type::copy));
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
    public TypeKey getTypeKey() {
        return new IntersectionTypeKey(new HashSet<>(NncUtils.map(types, Type::getTypeKey)));
    }

    @Override
    protected boolean isAssignableFrom0(Type that, @Nullable Map<TypeVariable, ? extends Type> typeMapping) {
        return NncUtils.allMatch(this.types, t -> t.isAssignableFrom(that, typeMapping));
    }

    @Override
    public List<Type> getComponentTypes() {
        return Collections.unmodifiableList(types);
    }

    @Override
    protected TypeParam getParamInternal() {
        return null;
    }

    @Override
    public List<? extends Type> getSuperTypes() {
        return Collections.unmodifiableList(types);
    }

    @Override
    public boolean equals(Type that, @Nullable Map<TypeVariable, ? extends Type> mapping) {
        if (that instanceof IntersectionType thatIntersectionType)
            return NncUtils.equalsIgnoreOrder(types, thatIntersectionType.types, (t1,t2) -> t1.equals(t2, mapping));
        else
            return false;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        List<String> memberCanonicalNames = NncUtils.mapAndSort(
                types,
                object -> context.getModelName(object,this),
                String::compareTo
        );
        return String.join("&", memberCanonicalNames);
    }

    @Override
    public String getTypeDesc() {
        return NncUtils.join(types, Type::getTypeDesc, "&");
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        var names = NncUtils.mapAndSort(types, type -> type.getInternalName(current), String::compareTo);
        return NncUtils.join(names, "&");
    }

    public Set<Type> getTypes() {
        return new HashSet<>(types);
    }

    @Override
    protected String getKey() {
        return getKey(types);
    }

    public static String getKey(List<Type> componentTypes) {
        return CompositeType.getKey(NncUtils.sort(componentTypes, Comparator.comparing(Entity::getId)));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIntersectionType(this);
    }

    @Override
    public IntersectionType copy() {
        return new IntersectionType(null, NncUtils.mapUnique(types, Type::copy));
    }

    @Override
    public String toTypeExpression(SerializeContext serializeContext) {
        return NncUtils.join(types, type -> type.toTypeExpression(serializeContext), "&");
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
        return new IntersectionType(null, types);
    }

}
