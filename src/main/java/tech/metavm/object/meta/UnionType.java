package tech.metavm.object.meta;

import tech.metavm.entity.*;
import tech.metavm.object.instance.SQLType;
import tech.metavm.object.meta.rest.dto.TypeKey;
import tech.metavm.object.meta.rest.dto.UnionTypeKey;
import tech.metavm.object.meta.rest.dto.UnionTypeParam;
import tech.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

@EntityType("联合类型")
public class UnionType extends CompositeType {

    public static final IndexDef<UnionType> KEY_IDX = IndexDef.uniqueKey(UnionType.class, "key");

    public static final IndexDef<UnionType> MEMBER_IDX = IndexDef.normalKey(UnionType.class, "members");

    @ChildEntity("成员集合")
    private final ReadWriteArray<Type> members;

    public UnionType(Long tmpId, Set<Type> members) {
        super(getTypeName(members), false, false, TypeCategory.UNION);
        setTmpId(tmpId);
        this.members = addChild(new ReadWriteArray<>(Type.class, members), "members");
    }

    private static String getTypeName(Set<Type> members) {
        return NncUtils.join(members, Type::getName, "|");
    }

    public Set<Type> getMembers() {
        return new HashSet<>(members);
    }

    @Override
    public TypeKey getTypeKey() {
        return new UnionTypeKey(NncUtils.mapUnique(members, Entity::getRef));
    }

    @Override
    public Type getConcreteType() {
        if (isNullable()) {
            return NncUtils.findRequired(members, t -> !t.isNull()).getConcreteType();
        }
        return this;
    }

    public ReadonlyArray<Type> getDeclaredMembers() {
        return members;
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        Iterable<Type> thatTypes;
        if (that instanceof UnionType thatUnionType) {
            thatTypes = thatUnionType.members;
        } else {
            thatTypes = List.of(that);
        }
        for (Type thatType : thatTypes) {
            boolean anyMatch = false;
            for (Type typeMember : members) {
                if (typeMember.isAssignableFrom(thatType)) {
                    anyMatch = true;
                    break;
                }
            }
            if (!anyMatch) {
                return false;
            }
        }
        return true;
    }


    @Override
    protected UnionTypeParam getParamInternal() {
        try (var context = SerializeContext.enter()) {
            return new UnionTypeParam(
                    NncUtils.map(members, context::getRef)
            );
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnionType unionType = (UnionType) o;
        return Objects.equals(members, unionType.members);
    }

    @Override
    public int hashCode() {
        return Objects.hash(members);
    }

    @Override
    public String toString() {
        List<String> memberNames = NncUtils.mapAndSort(members, Type::getName, String::compareTo);
        return "UnionType " + String.join("|", memberNames);
    }

    @Override
    public String getCanonicalName(Function<Type, java.lang.reflect.Type> getJavaType) {
        List<String> memberCanonicalNames = NncUtils.mapAndSort(
                members,
                m -> m.getCanonicalName(getJavaType),
                String::compareTo
        );
        return String.join("|", memberCanonicalNames);
    }

    public boolean isBinaryNullable() {
        return members.size() == 2 && isNullable();
    }

    @Override
    public SQLType getSQLType() {
        if(isBinaryNullable()) {
            return getUnderlyingType().getSQLType();
        }
        else {
            return super.getSQLType();
        }
    }

    @Override
    public List<? extends Type> getSuperTypes() {
        return List.of(TypeUtils.getLeastUpperBound(members));
    }

    @Override
    protected String getKey() {
        return getKey(members);
    }

    public static String getKey(List<Type> componentTypes) {
        return CompositeType.getKey(NncUtils.sort(componentTypes, Comparator.comparingLong(Entity::getIdRequired)));
    }

    @Override
    public List<Type> getComponentTypes() {
        return members.toList();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnionType(this);
    }
}
