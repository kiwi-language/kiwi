package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.UnionTypeKey;
import tech.metavm.object.type.rest.dto.UnionTypeParam;
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
        return NncUtils.anyMatch(members, m -> m.isAssignableFrom(that));
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
    protected String toString0() {
        List<String> memberNames = NncUtils.mapAndSort(members, Type::getName, String::compareTo);
        return "UnionType " + String.join("|", memberNames);
    }

    @Override
    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
        List<String> memberCanonicalNames = NncUtils.mapAndSort(
                members,
                m -> m.getKey(getJavaType),
                String::compareTo
        );
        return String.join("|", memberCanonicalNames);
    }

    @Override
    public boolean isBinaryNullable() {
        return members.size() == 2 && isNullable();
    }

    @Override
    public Type getUnderlyingType() {
        if(members.size() != 2)
            return this;
        Type t1 = members.get(0), t2 = members.get(1);
        if(t1.isNull())
            return t2;
        else if(t2.isNull())
            return t1;
        return this;
    }

    @Override
    public ColumnKind getSQLType() {
        if(isBinaryNullable()) {
            return getUnderlyingType().getSQLType();
        }
        else {
            return super.getSQLType();
        }
    }

    @Override
    public List<? extends Type> getSuperTypes() {
        return List.of(Types.getLeastUpperBound(members));
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
