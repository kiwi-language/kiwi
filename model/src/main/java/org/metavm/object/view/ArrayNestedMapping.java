package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.*;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    public Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope) {
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
                        Nodes.argument(setSourceFunc, 1, getSource.get())
                )
        );
        Nodes.forEach(
                scope.nextNodeName("iterate"),
                getSource,
                (bodyScope, getElement, getIndex) -> {
                    var getTargetElement = elementNestedMapping.generateMappingCode(getElement,
                            bodyScope);
                    Nodes.addElement(
                            scope.nextNodeName("add"),
                            null,
                            Values.node(targetArray),
                            getTargetElement.get(),
                            bodyScope
                    );
                },
                scope
        );
        return () -> Values.node(targetArray);
    }

    @Override
    public Supplier<Value> generateUnmappingCode(Supplier<Value> getView, ScopeRT scope) {
        var isSourcePresent = Nodes.functionCall(scope.nextNodeName("isSourcePresent"), scope, StdFunction.isSourcePresent.get(),
                List.of(Nodes.argument(StdFunction.isSourcePresent.get(), 0, getView.get())));
        Map<NodeRT, Value> exit2value = new HashMap<>();
        var ifNode = Nodes.if_(scope.nextNodeName("if"),
                Values.node(Nodes.eq(Values.node(isSourcePresent), Values.constantFalse(), scope)),
                null,
                scope
        );
        var existingSource = Nodes.functionCall(scope.nextNodeName("source"), scope,
                StdFunction.getSource.get(),
                List.of(Nodes.argument(StdFunction.getSource.get(), 0, getView.get())));
        var g = Nodes.goto_(scope.nextNodeName("goto"), scope);
        exit2value.put(g, Values.node(existingSource));
        var newSource = Nodes.newArray(scope.nextNodeName("newSource"), null,
                sourceType, null, null, scope);
        ifNode.setTarget(newSource);
        exit2value.put(newSource, Values.node(newSource));
        var join = Nodes.join(scope.nextNodeName("join"), scope);
        g.setTarget(join);
        var sourceField = FieldBuilder.newBuilder("source", null, join.getKlass(), sourceType).build();
        new JoinNodeField(sourceField, join, exit2value);
        Nodes.clearArray(scope.nextNodeName("clearArray"), null, Values.node(Nodes.nodeProperty(join, sourceField, scope)),
                scope);
        Nodes.forEach(
                scope.nextNodeName("iterate"), getView,
                (bodyScope, getElement, getIndex) -> {
                    var getSourceElement = elementNestedMapping.generateUnmappingCode(getElement, bodyScope);
                    Nodes.addElement(scope.nextNodeName("addElement"), null,
                            Values.node(Nodes.nodeProperty(join, sourceField, scope)), getSourceElement.get(), bodyScope);
                },
                scope
        );
        var source = Nodes.nodeProperty(join, sourceField, scope);
        return () -> Values.node(source);
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
