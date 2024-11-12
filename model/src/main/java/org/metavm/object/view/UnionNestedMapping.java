package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.ChildArray;
import org.metavm.entity.LoadAware;
import org.metavm.flow.*;
import org.metavm.object.type.Type;
import org.metavm.object.type.UnionType;
import org.metavm.util.Instances;
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

    @Override
    public Type generateMappingCode(Supplier<NodeRT> getSource, ScopeRT scope) {
        JumpNode lastIfNode = null;
        GotoNode lastGoto = null;
        int i = -1;
        var gotoNodes = new ArrayList<GotoNode>();
        for (var memberMapping : memberMappings) {
            getSource.get();
            Nodes.instanceOf(memberMapping.sourceType(), scope);
            var ifNode = Nodes.ifNot(null, scope);
            if(lastIfNode != null)
                lastIfNode.setTarget(lastGoto.getSuccessor());
            lastIfNode = ifNode;
            memberMapping.nestedMapping().generateMappingCode(
                    () -> {
                        getSource.get();
                        return Nodes.castNode(memberMapping.sourceType(), scope);
                    }, scope);
            if(i == -1)
                i = scope.nextVariableIndex();
            Nodes.store(i, scope);
            gotoNodes.add(lastGoto = Nodes.goto_(scope));
        }
        Objects.requireNonNull(lastIfNode).setTarget(
                Nodes.loadConstant(Instances.stringInstance("Invalid type"), scope)
        );
        Nodes.raiseWithMessage(scope);
        var exit = Nodes.load(i, targetType, scope);
        gotoNodes.forEach(g -> g.setTarget(exit));
        return targetType;
    }

    @Override
    public Type generateUnmappingCode(Supplier<NodeRT> viewSupplier, ScopeRT scope) {
        var gotoNodes = new ArrayList<GotoNode>();
        JumpNode lastIfNode = null;
        GotoNode lastGoto = null;
        var i = -1;
        for (var memberMapping : memberMappings) {
            viewSupplier.get();
            Nodes.instanceOf(memberMapping.targetType(), scope);
            var ifNode = Nodes.ifNot(null, scope);
            if(lastIfNode != null)
                lastIfNode.setTarget(lastGoto.getSuccessor());
            lastIfNode = ifNode;
            var value = memberMapping.nestedMapping().generateUnmappingCode(
                    () -> {
                        viewSupplier.get();
                        return  Nodes.cast(memberMapping.targetType() , scope);
                    }, scope);
            if (i == -1)
                i = scope.nextVariableIndex();
            Nodes.store(i, scope);
            gotoNodes.add(lastGoto = Nodes.goto_(scope));
        }
        Objects.requireNonNull(lastIfNode).setTarget(
                Nodes.loadConstant(Instances.stringInstance("Invalid type"), scope)
        );
        Nodes.raiseWithMessage(scope);
        var exit = Nodes.load(i, sourceType, scope);
        gotoNodes.forEach(g -> g.setTarget(exit));
        return sourceType;
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
