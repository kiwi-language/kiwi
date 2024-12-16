package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.ReadonlyArray;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.ValueArray;
import org.metavm.flow.Flow;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.object.type.rest.dto.UnionTypeKey;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.NncUtils;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

@Entity
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
        return members.size() == 2 && (members.get(0).isNull() || members.get(1).isNull());
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
    public String getName() {
        return NncUtils.join(members, Type::getName, "|");
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        return NncUtils.join(members, type -> {
            var memberExpr = type.toExpression(serializeContext, getTypeDefExpr);
            if(type.getPrecedence() >= getPrecedence())
                memberExpr = "("+ memberExpr + ")";
            return memberExpr;
        }, "|");
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.UNION_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.UNION_TYPE);
        output.writeInt(members.size());
        members.forEach(t -> t.write(output));
    }

    public static UnionType read(MvInput input) {
        var numMembers = input.readInt();
        var members = new HashSet<Type>(numMembers);
        for (int i = 0; i < numMembers; i++)
            members.add(Type.readType(input));
        return new UnionType(members);
    }

    public Set<Type> memberSet() {
        if(memberSet == null)
            memberSet = new HashSet<>(members.toList());
        return memberSet;
    }

    @Override
    public int getPrecedence() {
        return 3;
    }

    public UnionType flatten() {
        var flattenedMembers = new HashSet<Type>();
        for (Type member : members) {
            if(member instanceof UnionType u)
                flattenedMembers.addAll(u.getMembers());
            else
                flattenedMembers.add(member);
        }
        return new UnionType(flattenedMembers);
    }

    @Override
    public boolean isNullable() {
        return NncUtils.anyMatch(members, Type::isNullable);
    }
}
