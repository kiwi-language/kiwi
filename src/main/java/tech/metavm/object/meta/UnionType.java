package tech.metavm.object.meta;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;
import tech.metavm.object.meta.rest.dto.UnionTypeParamDTO;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;

@EntityType("联合类型")
public class UnionType extends Type {

    public static final IndexDef<UnionType> UNIQUE_TYPE_ELEMENTS = new IndexDef<>(
            UnionType.class, "members"
    );

    @ChildEntity("成员集合")
    private final Table<Type> members;

    public UnionType(Long tmpId, Set<Type> members) {
        super(getTypeName(members), false, false, TypeCategory.UNION);
        setTmpId(tmpId);
        this.members = new Table<>(Type.class, members);
    }

    private static String getTypeName(Set<Type> members) {
        return NncUtils.join(members, Type::getName, "|");
    }

    public Set<Type> getMembers() {
        return new IdentitySet<>(members);
    }

    @Override
    public boolean contains(Type that) {
        return members.contains(that);
    }

    @Override
    public Type getConcreteType() {
        if(isNullable()) {
            return NncUtils.findRequired(members, t -> !t.isNull()).getConcreteType();
        }
        return this;
    }

    public Table<Type> getDeclaredMembers() {
        return members;
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        List<Type> thatTypes;
        if(that instanceof UnionType thatUnionType) {
            thatTypes = thatUnionType.members;
        }
        else {
            thatTypes = List.of(that);
        }
        for (Type thatType : thatTypes) {
            boolean anyMatch = false;
            for (Type typeMember : members) {
                if(typeMember.isAssignableFrom(thatType)) {
                    anyMatch = true;
                    break;
                }
            }
            if(!anyMatch) {
                return false;
            }
        }
        return true;
    }


    @Override
    protected UnionTypeParamDTO getParam() {
        return new UnionTypeParamDTO(
                NncUtils.map(members, Type::toDTO)
        );
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
}
