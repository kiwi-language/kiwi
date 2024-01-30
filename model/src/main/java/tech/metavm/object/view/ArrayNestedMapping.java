package tech.metavm.object.view;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
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

@EntityType("数组嵌套映射")
public class ArrayNestedMapping extends NestedMapping {

    @EntityField("来源类型")
    private final ArrayType sourceType;
    @EntityField("目标类型")
    private final ArrayType targetType;
    @ChildEntity("元素嵌套映射")
    private final NestedMapping elementNestedMapping;

    public ArrayNestedMapping(ArrayType sourceType, ArrayType targetType, NestedMapping elementNestedMapping) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.elementNestedMapping = addChild(elementNestedMapping, "elementNestedMapping");
    }

    @Override
    public Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope) {
        var sourceElementType = sourceType.getElementType();
        var targetElementType = targetType.getElementType();
        var targetArray = Nodes.newArray(
                targetType.getName() + "数组",
                null,
                targetType,
                null,
                null,
                scope
        );
        var setSourceFunc = NativeFunctions.setSource();
        Nodes.functionCall(
                "设置来源" + targetType.getName(),
                scope,
                setSourceFunc,
                List.of(
                        Nodes.argument(setSourceFunc, 0, Values.node(targetArray)),
                        Nodes.argument(setSourceFunc, 1, getSource.get())
                )
        );
        Nodes.forEach(
                "遍历" + sourceType.getName(),
                getSource,
                (bodyScope, getElement, getIndex) -> {
                    var getTargetElement = elementNestedMapping.generateMappingCode(getElement,
                            bodyScope);
                    Nodes.addElement(
                            "添加" + sourceElementType.getName(),
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
        var isSourcePresent = Nodes.functionCall("来源是否存在", scope, NativeFunctions.isSourcePresent(),
                List.of(Nodes.argument(NativeFunctions.isSourcePresent(), 0, getView.get())));
        Map<Branch, Value> branch2sourceNode = new HashMap<>();
        var sourceFieldRef = new Object() {
            Field sourceField;
        };
        Nodes.branch(
                "分支",
                null,
                scope,
                Values.expression(Expressions.eq(Expressions.node(isSourcePresent), Expressions.trueExpression())),
                trueBranch -> {
                    var source = Nodes.functionCall(sourceType.getName() + "来源", trueBranch.getScope(),
                            NativeFunctions.getSource(),
                            List.of(Nodes.argument(NativeFunctions.getSource(), 0, getView.get())));
                    branch2sourceNode.put(trueBranch, Values.node(source));
                },
                falseBranch -> {
                    var source = Nodes.newArray(sourceType.getName() + "新建来源", null,
                            sourceType, null, null, falseBranch.getScope());
                    branch2sourceNode.put(falseBranch, Values.node(source));
                },
                mergeNode -> {
                    sourceFieldRef.sourceField = FieldBuilder.newBuilder("来源", null, mergeNode.getType(), sourceType)
                            .build();
                    new MergeNodeField(sourceFieldRef.sourceField, mergeNode, branch2sourceNode);
                }
        );
        var mergeNode = scope.getLastNode();
        var sourceField = sourceFieldRef.sourceField;
        Nodes.clearArray("清空数组" + sourceType.getName(), null, Values.nodeProperty(mergeNode, sourceField),
                scope);
        Nodes.forEach(
                "遍历" + targetType.getName(), getView,
                (bodyScope, getElement, getIndex) -> {
                    var getSourceElement = elementNestedMapping.generateUnmappingCode(getElement, bodyScope);
                    Nodes.addElement("添加元素" + sourceType.getName(), null,
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
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ArrayNestedMapping that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(sourceType, that.sourceType) && Objects.equals(targetType, that.targetType) && Objects.equals(elementNestedMapping, that.elementNestedMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sourceType, targetType, elementNestedMapping);
    }
}
