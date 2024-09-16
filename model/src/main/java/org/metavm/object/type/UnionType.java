package org.metavm.object.type;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ReadonlyArray;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.ValueArray;
import org.metavm.flow.Flow;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.object.type.rest.dto.TypeKeyCodes;
import org.metavm.object.type.rest.dto.UnionTypeKey;
import org.metavm.util.InstanceInput;
import org.metavm.util.InstanceOutput;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@EntityType
public class UnionType extends CompositeType {

    public static final UnionType nullableAnyType = new UnionType(Set.of(PrimitiveType.nullType, AnyType.instance));

    public static UnionType create(Type...types) {
        return new UnionType(Set.of(types));
    }

    private final ValueArray<Type> members;

    private transient Set<Type> memberSet;

    public UnionType(Set<Type> members) {
        super();
        if(members.isEmpty())
            throw new IllegalArgumentException("members can not be empty");
        // maintain a relatively deterministic order so that the ModelIdentities of the members are not changed between reboots
        // the order is not fully deterministic but it's sufficient for bootstrap because only binary-nullable union types are involved.
        var sortedMembers = members.stream()
                .sorted(Comparator.comparingInt(Type::getTypeKeyCode))
                .toList();
        this.members = new ValueArray<>(Type.class, sortedMembers);
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
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new UnionTypeKey(NncUtils.mapUnique(members, type -> type.toTypeKey(getTypeDefId)));
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
    protected boolean isConvertibleFrom0(Type that) {
        return NncUtils.anyMatch(members, m -> m.isConvertibleFrom(that));
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitUnionType(this, s);
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
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return NncUtils.join(members, type -> type.toExpression(serializeContext, getTypeDefExpr), "|");
    }

    @Override
    public int getTypeKeyCode() {
        return TypeKeyCodes.UNION;
    }

    @Override
    public void write(InstanceOutput output) {
        output.write(TypeKeyCodes.UNION);
        output.writeInt(members.size());
        members.forEach(t -> t.write(output));
    }

    public static UnionType read(InstanceInput input, TypeDefProvider typeDefProvider) {
        var numMembers = input.readInt();
        var members = new HashSet<Type>(numMembers);
        for (int i = 0; i < numMembers; i++)
            members.add(Type.readType(input, typeDefProvider));
        return new UnionType(members);
    }

    private Set<Type> memberSet() {
        if(memberSet == null)
            memberSet = new HashSet<>(members.toList());
        return memberSet;
    }

}
