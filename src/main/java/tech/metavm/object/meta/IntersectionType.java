package tech.metavm.object.meta;

import tech.metavm.entity.*;
import tech.metavm.object.meta.rest.dto.IntersectionTypeKey;
import tech.metavm.object.meta.rest.dto.TypeKey;
import tech.metavm.object.meta.rest.dto.TypeParam;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

@EntityType("类型交集")
public class IntersectionType extends CompositeType {

    public static final IndexDef<IntersectionType> KEY_IDX = new IndexDef<>(IntersectionType.class, "key");

    @ChildEntity("类型列表")
    private final ReadWriteArray<Type> types = addChild(new ReadWriteArray<>(Type.class), "types");

    public IntersectionType(Long tmpId, Set<Type> types) {
        super(makeName(types), false, false, TypeCategory.INTERSECTION);
        setTmpId(tmpId);
        this.types.addAll(types);
    }

    private static String makeName(Iterable<Type> types) {
        return NncUtils.join(types, Type::getName, "&");
    }

    @Override
    public TypeKey getTypeKey() {
        return new IntersectionTypeKey(new HashSet<>(NncUtils.map(types, Entity::getRef)));
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return NncUtils.allMatch(this.types, t -> t.isAssignableFrom(that));
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
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        throw new UnsupportedOperationException();
    }

    public Set<Type> getTypes() {
        return new HashSet<>(types);
    }

    @Override
    protected String getKey() {
        return getKey(types);
    }

    public static String getKey(List<Type> componentTypes) {
        return CompositeType.getKey(NncUtils.sort(componentTypes, Comparator.comparingLong(Entity::getIdRequired)));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIntersectionType(this);
    }
}
