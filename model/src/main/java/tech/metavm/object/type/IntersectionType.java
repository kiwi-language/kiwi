package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.object.type.rest.dto.IntersectionTypeKey;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeParam;
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
        this.types.addAll(types);
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
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        List<String> memberCanonicalNames = NncUtils.mapAndSort(
                types,
                object -> context.getModelName(object,this),
                String::compareTo
        );
        return String.join("&", memberCanonicalNames);
    }

    public Set<Type> getTypes() {
        return new HashSet<>(types);
    }

    @Override
    protected String getKey() {
        return getKey(types);
    }

    public static String getKey(List<Type> componentTypes) {
        return CompositeType.getKey(NncUtils.sort(componentTypes, Comparator.comparingLong(Entity::tryGetId)));
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitIntersectionType(this);
    }
}
