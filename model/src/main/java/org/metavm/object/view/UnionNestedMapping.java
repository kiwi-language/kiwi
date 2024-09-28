package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.ChildArray;
import org.metavm.entity.LoadAware;
import org.metavm.expression.InstanceOfExpression;
import org.metavm.flow.*;
import org.metavm.object.type.*;
import org.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Supplier;

@EntityType
public class UnionNestedMapping extends NestedMapping implements LoadAware {

    @ChildEntity
    private final ChildArray<MemberTypeNestedMapping> memberMappings =
            addChild(new ChildArray<>(MemberTypeNestedMapping.class), "memberMappings");
    private transient Set<MemberTypeNestedMapping> memberMappingSet;
    private transient UnionType sourceType;
    private transient UnionType targetType;

    public UnionNestedMapping(List<MemberTypeNestedMapping> memberMappings) {
        this.memberMappings.addChildren(memberMappings);
        this.memberMappings.sort(Comparator.comparingInt(m -> m.sourceType().getTypeKeyCode()));
        sourceType = new UnionType(NncUtils.mapUnique(memberMappings, MemberTypeNestedMapping::sourceType));
        targetType = new UnionType(NncUtils.mapUnique(memberMappings, MemberTypeNestedMapping::targetType));
        memberMappingSet = new HashSet<>(memberMappings);
    }

    @Override
    public void onLoadPrepare() {
        sourceType = new UnionType(NncUtils.mapUnique(memberMappings, MemberTypeNestedMapping::sourceType));
        targetType = new UnionType(NncUtils.mapUnique(memberMappings, MemberTypeNestedMapping::targetType));
        memberMappingSet = new HashSet<>(memberMappings.toList());
    }

    @Override
    public void onLoad() {
    }

    private static Type getViewType(Type sourceType, UnionType targetUnionType) {
        return NncUtils.findRequired(targetUnionType.getMembers(), sourceType::isViewType,
                () -> "Cannot find view type of " + sourceType + " in union type " + targetUnionType);
    }

    @Override
    public Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope) {
        var values = new HashMap<Branch, Value>();
        var valueFieldRef = new Object() {
            Field valueField;
        };
        var source = Nodes.value(scope.nextNodeName("source"), getSource.get(), scope);
        Nodes.branch(
                scope.nextNodeName("checkType"),
                null,
                scope,
                NncUtils.map(
                        memberMappings,
                        t -> Values.expression(
                                new InstanceOfExpression(getSource.get().getExpression(), t.sourceType())
                        )
                ),
                NncUtils.map(
                        memberMappings,
                        t -> branch -> {
                            var castSource = Nodes.castNode(scope.nextNodeName("castSource"),
                                    t.sourceType(), branch.getScope(), Values.node(source));
                            values.put(
                                    branch,
                                    t.nestedMapping().generateMappingCode(() -> Values.node(castSource), branch.getScope()).get()
                            );
                        }
                ),
                branch -> Nodes.raise(scope.nextNodeName("invalidTypeError"), branch.getScope(), Values.constantString("Invalid type")),
                mergeNode -> {
                    valueFieldRef.valueField = FieldBuilder.newBuilder("value", null, mergeNode.getType().resolve(), targetType).build();
                    new MergeNodeField(valueFieldRef.valueField, mergeNode, values);
                }
        );
        return () -> Values.nodeProperty(scope.getLastNode(), valueFieldRef.valueField);
    }

    @Override
    public Supplier<Value> generateUnmappingCode(Supplier<Value> getView, ScopeRT scope) {
        var values = new HashMap<Branch, Value>();
        var valueFieldRef = new Object() {
            Field valueField;
        };
        var view = Nodes.value(scope.nextNodeName("view"), getView.get(), scope);
        Nodes.branch(
                scope.nextNodeName("checkType"),
                null,
                scope,
                NncUtils.map(
                        memberMappings,
                        t -> Values.expression(
                                new InstanceOfExpression(getView.get().getExpression(), t.targetType())
                        )
                ),
                NncUtils.map(
                        memberMappings,
                        t -> branch -> {
                            var castView =  Nodes.cast(scope.nextNodeName("castView"), t.targetType(), Values.node(view), branch.getScope());
                            values.put(
                                    branch,
                                    t.nestedMapping().generateUnmappingCode(() -> Values.node(castView), branch.getScope()).get()
                            );
                        }
                ),
                branch -> Nodes.raise(scope.nextNodeName("invalidTypeError"), branch.getScope(), Values.constantString("invalid type")),
                mergeNode -> {
                    valueFieldRef.valueField = FieldBuilder.newBuilder("value", null, mergeNode.getType().resolve(), sourceType).build();
                    new MergeNodeField(valueFieldRef.valueField, mergeNode, values);
                }
        );
        var mergeNode = scope.getLastNode();
        return () -> Values.nodeProperty(mergeNode, valueFieldRef.valueField);
    }

    @Override
    public Type getTargetType() {
        return targetType;
    }

    @Override
    public String getText() {
        return "{\"kind\": \"Union\", \"sourceType\": \"" + sourceType.getTypeDesc() + "\", \"targetType\": \"" + targetType.getTypeDesc()
                + "\", \"memberMappings\": [" + NncUtils.join(memberMappings, t -> t.nestedMapping().getText()) + "]}";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof UnionNestedMapping that)) return false;
        return memberMappingSet.equals(new HashSet<>(that.memberMappingSet));
    }

    @Override
    public int hashCode() {
        return memberMappingSet.hashCode();
    }
}
