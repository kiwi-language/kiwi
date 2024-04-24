package tech.metavm.object.type;

import org.jetbrains.annotations.NotNull;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.UnionTypeKey;
import tech.metavm.object.type.rest.dto.UnionTypeParam;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;

@EntityType("联合类型")
public class UnionType extends CompositeType {

    public static final IndexDef<UnionType> KEY_IDX = IndexDef.createUnique(UnionType.class, "key");

    public static final IndexDef<UnionType> MEMBER_IDX = IndexDef.create(UnionType.class, "members");

    @ChildEntity("成员集合")
    private final ReadWriteArray<Type> members;

    private transient Set<Type> memberSet;

    public UnionType(Long tmpId, Set<Type> members) {
        super(getName(members), getCode(members), false, false, TypeCategory.UNION);
        setTmpId(tmpId);
        this.members = addChild(new ReadWriteArray<>(Type.class, NncUtils.map(members, Type::copy)), "members");
    }

    private static String getName(Set<Type> members) {
        return NncUtils.join(members, Type::getName, "|");
    }

    private static @Nullable String getCode(Set<Type> members) {
        if (NncUtils.allMatch(members, m -> m.getCode() != null))
            return NncUtils.join(members, Type::getCode, "|");
        else
            return null;
    }

    public Set<Type> getMembers() {
        return new HashSet<>(members);
    }

    @Override
    public TypeKey toTypeKey() {
        return new UnionTypeKey(NncUtils.mapUnique(members, Type::toTypeKey));
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
        try (var serContext = SerializeContext.enter()) {
            return new UnionTypeParam(
                    NncUtils.map(members, serContext::getId)
            );
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof UnionType that && memberSet().equals(that.memberSet());
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberSet());
    }

    @Override
    protected String toString0() {
        List<String> memberNames = NncUtils.mapAndSort(members, Type::getName, String::compareTo);
        return "UnionType " + String.join("|", memberNames);
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        List<String> memberCanonicalNames = NncUtils.mapAndSort(
                members,
                object -> context.getModelName(object, this),
                String::compareTo
        );
        return String.join("|", memberCanonicalNames);
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        var names = NncUtils.mapAndSort(members, type -> type.getInternalName(current), String::compareTo);
        return NncUtils.join(names, "|");
    }

    @Override
    public boolean isBinaryNullable() {
        return members.size() == 2 && isNullable();
    }

    @Override
    public Type getUnderlyingType() {
        if (members.size() != 2)
            return this;
        Type t1 = members.get(0), t2 = members.get(1);
        if (t1.isNull())
            return t2;
        else if (t2.isNull())
            return t1;
        return this;
    }

    @Override
    public ColumnKind getSQLType() {
        if (isBinaryNullable()) {
            return getUnderlyingType().getSQLType();
        } else {
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
        return CompositeType.getKey(NncUtils.sort(componentTypes, Comparator.comparing(Entity::getId)));
    }

    @Override
    public String getTypeDesc() {
        return NncUtils.join(members, Type::getTypeDesc, "|");
    }

    @Override
    public List<Type> getComponentTypes() {
        return members.toList();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnionType(this);
    }

    @Override
    public boolean isViewType(Type type) {
        if (super.isViewType(type))
            return true;
        var types = type instanceof UnionType unionType ? unionType.getMembers() : List.of(type);
        for (Type member : members) {
            boolean hasView = false;
            for (Type t : types) {
                if (member.isViewType(t)) {
                    hasView = true;
                    break;
                }
            }
            if (!hasView)
                return false;
        }
        return true;
    }

    @Override
    public UnionType copy() {
        return new UnionType(null, NncUtils.mapUnique(members, Type::copy));
    }

    @Override
    public String toTypeExpression(SerializeContext serializeContext) {
        return NncUtils.join(members, type -> type.toTypeExpression(serializeContext), "|");
    }

    @Override
    public void write0(InstanceOutput output) {
        output.writeInt(members.size());
        members.forEach(t -> t.write(output));
    }

    public static UnionType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        var members = new HashSet<Type>();
        for (int i = 0; i < input.readInt(); i++)
            members.add(Type.readType(input, typeDefProvider));
        return new UnionType(null, members);
    }

    private Set<Type> memberSet() {
        if(memberSet == null)
            memberSet = new HashSet<>(members);
        return memberSet;
    }

}
