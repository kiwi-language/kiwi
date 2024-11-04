package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.ChildArray;
import org.metavm.entity.LoadAware;
import org.metavm.flow.*;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Type;
import org.metavm.object.type.UnionType;
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
        Map<GotoNode, Value> exit2value = new HashMap<>();
        var source = Nodes.value(scope.nextNodeName("source"), getSource.get(), scope);
        JumpNode lastIfNode = null;
        GotoNode lastGoto = null;
        for (var memberMapping : memberMappings) {
            var ifNode = Nodes.ifNot(scope.nextNodeName("ifNot"),
                     Values.node(
                             Nodes.instanceOf(getSource.get(), memberMapping.sourceType(), scope)
                     ),
                    null,
                    scope
            );
            if(lastIfNode != null)
                lastIfNode.setTarget(lastGoto.getSuccessor());
            lastIfNode = ifNode;
            var castSource = Nodes.castNode(scope.nextNodeName("castSource"),
                    memberMapping.sourceType(), scope, Values.node(source));
            var value = memberMapping.nestedMapping().generateMappingCode(() -> Values.node(castSource), scope).get();
            exit2value.put(lastGoto = Nodes.goto_(scope.nextNodeName("goto"), scope), value);
        }
        Objects.requireNonNull(lastIfNode).setTarget(
                Nodes.raise(scope.nextNodeName("invalidTypeError"), scope, Values.constantString("Invalid type"))
        );
        var join = Nodes.join(scope.nextNodeName("join"), scope);
        exit2value.keySet().forEach(g -> g.setTarget(join));
        var valueField = FieldBuilder.newBuilder("value", null, join.getKlass(), targetType).build();
        new JoinNodeField(valueField, join, exit2value);
        var value = Nodes.nodeProperty(join, valueField, scope);
        return () -> Values.node(value);
    }

    @Override
    public Supplier<Value> generateUnmappingCode(Supplier<Value> getView, ScopeRT scope) {
        var exit2value = new HashMap<GotoNode, Value>();
        var view = Nodes.value(scope.nextNodeName("view"), getView.get(), scope);
        JumpNode lastIfNode = null;
        GotoNode lastGoto = null;
        for (var memberMapping : memberMappings) {
            var ifNode = Nodes.ifNot(scope.nextNodeName("ifNot"),
                    Values.node(
                            Nodes.instanceOf(getView.get(), memberMapping.targetType(), scope)
                    ),
                    null, scope
            );
            if(lastIfNode != null)
                lastIfNode.setTarget(lastGoto.getSuccessor());
            lastIfNode = ifNode;
            var castView =  Nodes.cast(scope.nextNodeName("castView"), memberMapping.targetType(), Values.node(view), scope);
            var value = memberMapping.nestedMapping().generateUnmappingCode(() -> Values.node(castView), scope).get();
            exit2value.put(lastGoto = Nodes.goto_(scope.nextNodeName("goto"), scope), value);
        }
        Objects.requireNonNull(lastIfNode).setTarget(
            Nodes.raise(scope.nextNodeName("invalidTypeError"), scope, Values.constantString("invalid type"))
        );
        var join = Nodes.join(scope.nextNodeName("join"), scope);
        exit2value.keySet().forEach(g -> g.setTarget(join));
        var valueField = FieldBuilder.newBuilder("value", null, join.getType().resolve(), sourceType).build();
        new JoinNodeField(valueField, join, exit2value);
        var value = Nodes.nodeProperty(join, valueField, scope);
        return () -> Values.node(value);
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
