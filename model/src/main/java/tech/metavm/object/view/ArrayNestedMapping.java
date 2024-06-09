package tech.metavm.object.view;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expressions;
import tech.metavm.flow.*;
import tech.metavm.object.type.ArrayType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.object.type.Type;

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
        var setSourceFunc = NativeFunctions.setSource();
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
        var isSourcePresent = Nodes.functionCall(scope.nextNodeName("isSourcePresent"), scope, NativeFunctions.isSourcePresent(),
                List.of(Nodes.argument(NativeFunctions.isSourcePresent(), 0, getView.get())));
        Map<Branch, Value> branch2sourceNode = new HashMap<>();
        var sourceFieldRef = new Object() {
            Field sourceField;
        };
        Nodes.branch(
                scope.nextNodeName("branch"),
                null,
                scope,
                Values.expression(Expressions.eq(Expressions.node(isSourcePresent), Expressions.trueExpression())),
                trueBranch -> {
                    var source = Nodes.functionCall(scope.nextNodeName("source"), trueBranch.getScope(),
                            NativeFunctions.getSource(),
                            List.of(Nodes.argument(NativeFunctions.getSource(), 0, getView.get())));
                    branch2sourceNode.put(trueBranch, Values.node(source));
                },
                falseBranch -> {
                    var source = Nodes.newArray(scope.nextNodeName("newSource"), null,
                            sourceType, null, null, falseBranch.getScope());
                    branch2sourceNode.put(falseBranch, Values.node(source));
                },
                mergeNode -> {
                    sourceFieldRef.sourceField = FieldBuilder.newBuilder("source", null, mergeNode.getType().resolve(), sourceType)
                            .build();
                    new MergeNodeField(sourceFieldRef.sourceField, mergeNode, branch2sourceNode);
                }
        );
        var mergeNode = scope.getLastNode();
        var sourceField = sourceFieldRef.sourceField;
        Nodes.clearArray(scope.nextNodeName("clearArray"), null, Values.nodeProperty(mergeNode, sourceField),
                scope);
        Nodes.forEach(
                scope.nextNodeName("iterate"), getView,
                (bodyScope, getElement, getIndex) -> {
                    var getSourceElement = elementNestedMapping.generateUnmappingCode(getElement, bodyScope);
                    Nodes.addElement(scope.nextNodeName("addElement"), null,
                            Values.nodeProperty(mergeNode, sourceField), getSourceElement.get(), bodyScope);
                },
                scope
        );
        return () -> Values.nodeProperty(mergeNode, sourceField);
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
