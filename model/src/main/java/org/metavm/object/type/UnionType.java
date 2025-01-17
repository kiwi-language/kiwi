package org.metavm.object.type;

import org.metavm.api.Entity;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.Flow;
import org.metavm.object.instance.ColumnKind;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.type.rest.dto.TypeKey;
import org.metavm.object.type.rest.dto.UnionTypeKey;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.Utils;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
public class UnionType extends CompositeType {

    public static final UnionType nullableAnyType = new UnionType(Set.of(NullType.instance, AnyType.instance));
    @SuppressWarnings("unused")
    private static Klass __klass__;

    public static UnionType create(Type...types) {
        return new UnionType(Set.of(types));
    }

    private final List<Type> members;

    public UnionType(Set<Type> members) {
        super();
        if(members.isEmpty())
            throw new IllegalArgumentException("members can not be empty");
        this.members = new ArrayList<>(members);
    }

    private UnionType(List<Type> members) {
        this.members = members;
    }

    public Set<Type> getMembers() {
        return new HashSet<>(members);
    }

    @Override
    public TypeKey toTypeKey(Function<ITypeDef, Id> getTypeDefId) {
        return new UnionTypeKey(Utils.mapToSet(members, type -> type.toTypeKey(getTypeDefId)));
    }

    @Override
    public Type getConcreteType() {
        if (isNullable()) {
            return Utils.findRequired(members, t -> !t.isNull()).getConcreteType();
        }
        return this;
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        for (Type member : members) {
            if (member.isAssignableFrom(that)) return true;
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof UnionType that) {
            var m1 = members;
            var m2 = that.members;
            if (m1.size() == m2.size()) {
                out: for (Type t1 : m1) {
                    for (Type t2 : m2) {
                        if (t1.equals(t2)) continue out;
                    }
                    return false;
                }
                return true;
            }
        }
        return false;
    }

    @Override
    public int hashCode() {
        int h = 0;
        for (Type obj : members) {
            if (obj != null)
                h += obj.hashCode();
        }
        return h;
    }

    @Override
    public String toString() {
        List<String> memberNames = Utils.mapAndSort(members, Type::getName, String::compareTo);
        return "UnionType " + String.join("|", memberNames);
    }

    @Override
    public String getInternalName(@Nullable Flow current) {
        var names = Utils.mapAndSort(members, type -> type.getInternalName(current), String::compareTo);
        return Utils.join(names, "|");
    }

    @Override
    public boolean isBinaryNullable() {
        return members.size() == 2 && (members.get(0).isNull() || members.get(1).isNull());
    }

    @Override
    public Type getUnderlyingType() {
        if (members.size() != 2)
            return this;
        Type t1 = members.getFirst(), t2 = members.getLast();
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
    public String getTypeDesc() {
        return Utils.join(members, Type::getTypeDesc, "|");
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
        return members;
    }

    @Override
    public String getName() {
        return Utils.join(members, Type::getName, "|");
    }

    @Override
    public String toExpression(SerializeContext serializeContext, @Nullable Function<ITypeDef, String> getTypeDefExpr) {
        var shuffledMembers = new ArrayList<>(members);
        Collections.shuffle(shuffledMembers);
        return (shuffledMembers.stream().map(type -> {
            var memberExpr = type.toExpression(serializeContext, getTypeDefExpr);
            if(type.getPrecedence() >= getPrecedence())
                memberExpr = "("+ memberExpr + ")";
            return memberExpr;
        }).collect(Collectors.joining("|")));
    }

    @Override
    public int getTypeKeyCode() {
        return WireTypes.UNION_TYPE;
    }

    @Override
    public void write(MvOutput output) {
        output.write(WireTypes.UNION_TYPE);
        output.writeList(members, t -> t.write(output));
    }

    public static UnionType read(MvInput input) {
        return new UnionType(input.readList(input::readType));
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
        return Utils.anyMatch(members, Type::isNullable);
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitUnionType(this);
    }

    @Override
    public <R, S> R accept(TypeVisitor<R, S> visitor, S s) {
        return visitor.visitUnionType(this, s);
    }

    @Override
    public ClassType getValueType() {
        return __klass__.getType();
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        for (var member : members) {
            member.accept(visitor);
        }
    }

    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        members.forEach(arg -> arg.forEachReference(action));
    }
}
