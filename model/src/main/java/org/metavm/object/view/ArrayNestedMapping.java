package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;

import java.util.List;
import java.util.Objects;

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
    public Value generateMappingCode(Value source, ScopeRT scope) {
        var targetArray = Nodes.newArray(
                scope.nextNodeName("array"),
                null,
                targetType,
                null,
                null,
                scope
        );
        var setSourceFunc = StdFunction.setSource.get();
        Nodes.functionCall(
                scope.nextNodeName("setSource"),
                scope,
                setSourceFunc,
                List.of(
                        Nodes.argument(setSourceFunc, 0, Values.node(targetArray)),
                        Nodes.argument(setSourceFunc, 1, source)
                )
        );
        Nodes.forEach(
                source,
                (bodyScope, element, index) -> {
                    var targetElement = elementNestedMapping.generateMappingCode(element,
                            bodyScope);
                    Nodes.addElement(
                            scope.nextNodeName("add"),
                            null,
                            Values.node(targetArray),
                            targetElement,
                            bodyScope
                    );
                },
                scope
        );
        return Values.node(targetArray);
    }

    @Override
    public Value generateUnmappingCode(Value view, ScopeRT scope) {
        var isSourcePresent = Nodes.functionCall(scope.nextNodeName("isSourcePresent"), scope, StdFunction.isSourcePresent.get(),
                List.of(Nodes.argument(StdFunction.isSourcePresent.get(), 0, view)));
        var ifNode = Nodes.if_(Values.node(Nodes.eq(Values.node(isSourcePresent), Values.constantFalse(), scope)),
                null,
                scope
        );
        var existingSource = Nodes.functionCall(scope.nextNodeName("source"), scope,
                StdFunction.getSource.get(),
                List.of(Nodes.argument(StdFunction.getSource.get(), 0, view)));
        var i = scope.nextVariableIndex();
        Nodes.store(i, Values.node(existingSource), scope);
        var g = Nodes.goto_(scope);
        var newSource = Nodes.newArray(scope.nextNodeName("newSource"), null,
                sourceType, null, null, scope);
        ifNode.setTarget(newSource);
        Nodes.store(i, Values.node(newSource), scope);
        g.setTarget(Nodes.noop(scope));
        var source = Values.node(Nodes.load(i, sourceType, scope));
        Nodes.clearArray(scope.nextNodeName("clearArray"), null, source, scope);
        Nodes.forEach(
                view,
                (bodyScope, element, index) -> {
                    var getSourceElement = elementNestedMapping.generateUnmappingCode(element, bodyScope);
                    Nodes.addElement(scope.nextNodeName("addElement"), null,
                            source, getSourceElement, bodyScope);
                },
                scope
        );
        return source;
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
