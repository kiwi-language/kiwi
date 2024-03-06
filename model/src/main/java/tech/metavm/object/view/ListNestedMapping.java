package tech.metavm.object.view;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expressions;
import tech.metavm.flow.*;
import tech.metavm.object.type.ClassType;
import tech.metavm.object.type.Field;
import tech.metavm.object.type.FieldBuilder;
import tech.metavm.object.type.Type;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@EntityType("数组嵌套映射")
public class ListNestedMapping extends NestedMapping {

    @EntityField("来源类型")
    private final ClassType sourceType;
    @EntityField("目标类型")
    private final ClassType targetType;
    @EntityField("来源读写列表类型")
    private final ClassType sourceReadWriteListType;
    @EntityField("目标读写列表类型")
    private final ClassType targetReadWriteListType;
    @ChildEntity("元素嵌套映射")
    private final NestedMapping elementNestedMapping;

    public ListNestedMapping(ClassType sourceType, ClassType targetType, ClassType sourceReadWriteListType,
                             ClassType targetReadWriteListType, NestedMapping elementNestedMapping) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.sourceReadWriteListType = sourceReadWriteListType;
        this.targetReadWriteListType = targetReadWriteListType;
        this.elementNestedMapping = addChild(elementNestedMapping, "elementNestedMapping");
    }

    @Override
    public Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope) {
        var sourceElementType = sourceType.getListElementType();
        var constructor = targetType.isEffectiveAbstract() ? targetReadWriteListType.getDefaultConstructor() :
                targetType.getDefaultConstructor();
        var targetList = Nodes.newObject(
                targetType.getName() + "列表",
                null,
                scope,
                constructor,
                List.of(),
                true
        );
        var setSourceFunc = NativeFunctions.setSource();
        Nodes.functionCall(
                "设置来源" + targetType.getName(),
                scope,
                setSourceFunc,
                List.of(
                        Nodes.argument(setSourceFunc, 0, Values.node(targetList)),
                        Nodes.argument(setSourceFunc, 1, getSource.get())
                )
        );
        Nodes.listForEach(
                "遍历" + sourceType.getName(),
                getSource,
                (bodyScope, getElement, getIndex) -> {
                    var getTargetElement = elementNestedMapping.generateMappingCode(getElement,
                            bodyScope);
                    var addMethod = targetType.getMethodByCodeAndParamTypes("add", List.of(
                            targetType.getListElementType()
                    ));
                    Nodes.methodCall(
                            "添加" + sourceElementType.getName(),
                            bodyScope,
                            Values.node(targetList),
                            addMethod,
                            List.of(
                                    Nodes.argument(
                                            addMethod,
                                            0,
                                            getTargetElement.get()
                                    )
                            )
                    );
                },
                scope
        );
        return () -> Values.node(targetList);
    }

    @Override
    public Supplier<Value> generateUnmappingCode(Supplier<Value> getView, ScopeRT scope) {
        var isSourcePresent = Nodes.functionCall("来源是否存在", scope, NativeFunctions.isSourcePresent(),
                List.of(Nodes.argument(NativeFunctions.isSourcePresent(), 0, getView.get())));
        var branch2sourceNode = new HashMap<Branch, Value>();
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
                    var source = Nodes.newObject(
                            sourceType.getName() + "新建来源",
                            null,
                            falseBranch.getScope(),
                            sourceReadWriteListType.getDefaultConstructor(),
                            List.of(),
                            true
                    );
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
        var clearMethod = sourceType.getMethodByCodeAndParamTypes("clear", List.of());
        Nodes.methodCall(
                "清空" + sourceType.getName(),
                scope,
                Values.nodeProperty(mergeNode, sourceField),
                clearMethod,
                List.of()
        );
        Nodes.listForEach(
                "遍历" + targetType.getName(), getView,
                (bodyScope, getElement, getIndex) -> {
                    var getSourceElement = elementNestedMapping.generateUnmappingCode(getElement, bodyScope);
                    var addMethod = sourceType.getMethodByCodeAndParamTypes("add", List.of(sourceType.getListElementType()));
                    Nodes.methodCall(
                            "添加元素" + sourceType.getName(),
                            bodyScope,
                            Values.nodeProperty(mergeNode, sourceField),
                            addMethod,
                            List.of(
                                    Nodes.argument(
                                            addMethod,
                                            0,
                                            getSourceElement.get()
                                    )
                            )
                    );
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
        if (!(object instanceof ListNestedMapping that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(sourceType, that.sourceType) && Objects.equals(targetType, that.targetType) && Objects.equals(elementNestedMapping, that.elementNestedMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sourceType, targetType, elementNestedMapping);
    }
}
