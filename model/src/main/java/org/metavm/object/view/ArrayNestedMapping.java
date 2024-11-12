package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.NodeRT;
import org.metavm.flow.Nodes;
import org.metavm.flow.ScopeRT;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;

import java.util.Objects;
import java.util.function.Supplier;

@EntityType
public class ArrayNestedMapping extends NestedMapping {

    private final ArrayType sourceType;
    private final ArrayType targetType;
    @ChildEntity
    private final NestedMapping elementNestedMapping;

    public ArrayNestedMapping(ArrayType sourceType, ArrayType targetType, NestedMapping elementNestedMapping) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.elementNestedMapping = addChild(elementNestedMapping, "elementNestedMapping");
    }

    @Override
    public Type generateMappingCode(Supplier<NodeRT> getSource, ScopeRT scope) {
        Nodes.newArray(targetType, scope);
        var targetArrayVar = scope.nextVariableIndex();
        Nodes.store(targetArrayVar, scope);
        var setSourceFunc = StdFunction.setSource.get();
        Nodes.load(targetArrayVar, targetType, scope);
        getSource.get();
        Nodes.functionCall(scope, setSourceFunc);
        Nodes.forEach(
                getSource,
                (bodyScope, getElement, getIndex) -> {
                    Nodes.load(targetArrayVar, targetType, bodyScope);
                    elementNestedMapping.generateMappingCode(getElement, bodyScope);
                    Nodes.addElement(bodyScope);
                },
                scope
        );
        Nodes.load(targetArrayVar, targetType, scope);
        return targetType;
    }

    @Override
    public Type generateUnmappingCode(Supplier<NodeRT> getView, ScopeRT scope) {
        getView.get();
        Nodes.functionCall(scope, StdFunction.isSourcePresent.get());
        Nodes.loadConstant(Instances.falseInstance(), scope);
        Nodes.eq(scope);
        var ifNode = Nodes.if_(null, scope);
        getView.get();
        Nodes.functionCall(scope, StdFunction.getSource.get());
        var sourceVar = scope.nextVariableIndex();
        Nodes.store(sourceVar, scope);
        var g = Nodes.goto_(scope);
        var newSource = Nodes.newArray(sourceType, scope);
        ifNode.setTarget(newSource);
        Nodes.store(sourceVar, scope);
        g.setTarget(Nodes.noop(scope));
        Nodes.load(sourceVar, sourceType, scope);
        Nodes.clearArray(scope);
        Nodes.forEach(
                getView,
                (bodyScope, element, index) -> {
                    Nodes.load(sourceVar, sourceType, bodyScope);
                    elementNestedMapping.generateUnmappingCode(element, bodyScope);
                    Nodes.addElement(bodyScope);
                },
                scope
        );
        Nodes.load(sourceVar, sourceType, scope);
        return sourceType;
    }

    @Override
    public Type getTargetType() {
        return targetType;
    }

    @Override
    public String getText() {
        return "{\"kind\": \"Array\", \"sourceType\": \"" + sourceType.getTypeDesc() + "\", \"targetType\": \"" + targetType.getTypeDesc()
                + "\", \"elementMapping\": " + elementNestedMapping.getText() + "}";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ArrayNestedMapping that)) return false;
        return Objects.equals(sourceType, that.sourceType) && Objects.equals(targetType, that.targetType) && Objects.equals(elementNestedMapping, that.elementNestedMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType, targetType, elementNestedMapping);
    }
}
