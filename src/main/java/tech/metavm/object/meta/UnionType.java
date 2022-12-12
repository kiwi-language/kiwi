package tech.metavm.object.meta;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IndexDef;
import tech.metavm.object.meta.rest.dto.UnionTypeParamDTO;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Objects;
import java.util.Set;

@EntityType("联合类型")
public class UnionType extends Type {

    public static final IndexDef<UnionType> UNIQUE_TYPE_ELEMENTS = new IndexDef<>(
            UnionType.class, "typeMembers"
    );

    @EntityField("成员类型集合")
    private final Table<Type> typeMembers;

    public UnionType(Set<Type> typeMembers) {
        super(getTypeName(typeMembers), false, false, TypeCategory.UNION);
        this.typeMembers = new Table<>(Type.class, typeMembers);
    }

    private static String getTypeName(Set<Type> typeMembers) {
        return NncUtils.join(typeMembers, Type::getName, "|");
    }

    public Set<Type> getTypeMembers() {
        return new IdentitySet<>(typeMembers);
    }

    @Override
    public boolean contains(Type that) {
        return typeMembers.contains(that);
    }

    @Override
    public Type getConcreteType() {
        if(isNullable()) {
            return NncUtils.findRequired(typeMembers, t -> !t.isNull()).getConcreteType();
        }
        return this;
    }

    public Table<Type> getDeclaredTypeMembers() {
        return typeMembers;
    }

    @Override
    public boolean isAssignableFrom(Type that) {
        if(typeMembers.contains(that)) {
            return true;
        }
        if(that instanceof UnionType thatUnionType) {
            return typeMembers.containsAll(thatUnionType.typeMembers);
        }
        return false;
    }


    @Override
    protected UnionTypeParamDTO getParam() {
        return new UnionTypeParamDTO(
                NncUtils.map(typeMembers, Type::toDTO)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnionType unionType = (UnionType) o;
        return Objects.equals(typeMembers, unionType.typeMembers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(typeMembers);
    }

    @Override
    public String toString() {
        return "UnionType " + NncUtils.join(typeMembers, Type::getName,"|");
    }
}
