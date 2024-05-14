package tech.metavm.object.type;

import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.instance.ColumnKind;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.TypeKeyCodes;
import tech.metavm.object.type.rest.dto.UnionTypeKey;
import tech.metavm.object.type.rest.dto.UnionTypeParam;
import tech.metavm.util.InstanceInput;
import tech.metavm.util.InstanceOutput;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@EntityType("联合类型")
public class UnionType extends CompositeType {

    public static UnionType create(Type...types) {
        return new UnionType(Set.of(types));
    }

    public static final IndexDef<UnionType> MEMBER_IDX = IndexDef.create(UnionType.class, "members");

    @ChildEntity("成员集合")
    private final ChildArray<Type> members = addChild(new ChildArray<>(Type.class), "members");

    private transient Set<Type> memberSet;

    public UnionType(Set<Type> members) {
        super(getName(members), getCode(members), false, false, TypeCategory.UNION);
        if(members.isEmpty())
            throw new IllegalArgumentException("members can not be empty");
        // maintain a relatively deterministic order so that the ModelIdentities of the members are not changed between reboots
        // the order is not fully deterministic but it's sufficient for bootstrap because only binary-nullable union types are involved.
        var sortedMembers = members.stream().map(Type::copy)
                .sorted(Comparator.comparingInt(Type::getTypeKeyCode))
                .toList();
        this.members.addChildren(sortedMembers);
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
        return new HashSet<>(members.toList());
    }

    @Override
    public TypeKey toTypeKey(Function<TypeDef, String> getTypeDefId) {
        return new UnionTypeKey(NncUtils.mapUnique(members, type -> type.toTypeKey(getTypeDefId)));
    }

    @Override
    public Type getConcreteType() {
        if (isNullable()) {
            return NncUtils.findRequired(members, t -> !t.isNull()).getConcreteType();
        }
        return this;
    }

    public ChildArray<Type> getDeclaredMembers() {
        return members;
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        return NncUtils.anyMatch(members, m -> m.isAssignableFrom(that));
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitUnionType(this, s);
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
    protected boolean equals0(Object obj) {
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
        return List.of(Types.getLeastUpperBound(memberSet()));
    }

    @Override
    public String getTypeDesc() {
        return NncUtils.join(members, Type::getTypeDesc, "|");
    }

    @Nullable
    @Override
    public String getCode() {
        if(NncUtils.anyMatch(members, t -> t.getCode() == null))
            return null;
        return NncUtils.join(members, Type::getCode, "|");
    }

    @Override
    public TypeCategory getCategory() {
        return TypeCategory.UNION;
    }

    @Override
    public boolean isEphemeral() {
        return false;
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
    public String getName() {
        return NncUtils.join(members, Type::getName, "|");
    }

    @Override
    public UnionType copy() {
        return new UnionType(NncUtils.mapUnique(members, Type::copy));
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<TypeDef, String> getTypeDefExpr) {
        return NncUtils.join(members, type -> type.toExpression(serializeContext, getTypeDefExpr), "|");
    }

    @Override
    public int getTypeKeyCode() {
        return TypeKeyCodes.UNION;
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
        return new UnionType(members);
    }

    private Set<Type> memberSet() {
        if(memberSet == null)
            memberSet = new HashSet<>(members.toList());
        return memberSet;
    }

}
