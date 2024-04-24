package tech.metavm.object.view;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.expression.Expressions;
import tech.metavm.flow.*;
import tech.metavm.object.type.*;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

@EntityType("数组嵌套映射")
public class ListNestedMapping extends NestedMapping {

    @EntityField("来源类型")
    private final Klass sourceKlass;
    @EntityField("目标类型")
    private final Klass targetKlass;
    @EntityField("来源读写列表类型")
    private final Klass sourceReadWriteListType;
    @EntityField("目标读写列表类型")
    private final Klass targetReadWriteListType;
    @ChildEntity("元素嵌套映射")
    private final NestedMapping elementNestedMapping;

    public ListNestedMapping(Klass sourceKlass, Klass targetKlass, Klass sourceReadWriteListType,
                             Klass targetReadWriteListType, NestedMapping elementNestedMapping) {
        this.sourceKlass = sourceKlass;
        this.targetKlass = targetKlass;
        this.sourceReadWriteListType = sourceReadWriteListType;
        this.targetReadWriteListType = targetReadWriteListType;
        this.elementNestedMapping = addChild(elementNestedMapping, "elementNestedMapping");
    }

    @Override
    public Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope, CompositeTypeFacade compositeTypeFacade) {
        var sourceElementType = sourceKlass.getListElementType();
        var constructor = targetKlass.isEffectiveAbstract() ? targetReadWriteListType.getDefaultConstructor() :
                targetKlass.getDefaultConstructor();
        var targetList = Nodes.newObject(
                targetKlass.getName() + "列表",
                null,
                scope,
                constructor,
                List.of(),
                false,
                true
        );
        var setSourceFunc = NativeFunctions.setSource();
        Nodes.functionCall(
                "设置来源" + targetKlass.getName(),
                scope,
                setSourceFunc,
                List.of(
                        Nodes.argument(setSourceFunc, 0, Values.node(targetList)),
                        Nodes.argument(setSourceFunc, 1, getSource.get())
                ),
                compositeTypeFacade
        );
        Nodes.listForEach(
                "遍历" + sourceKlass.getName(),
                getSource,
                (bodyScope, getElement, getIndex) -> {
                    var getTargetElement = elementNestedMapping.generateMappingCode(getElement,
                            bodyScope, compositeTypeFacade);
                    var addMethod = targetKlass.getMethodByCodeAndParamTypes("add", List.of(
                            targetKlass.getListElementType()
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
                            ),
                            compositeTypeFacade
                    );
                },
                scope
        );
        return () -> Values.node(targetList);
    }

    @Override
    public Supplier<Value> generateUnmappingCode(Supplier<Value> getView, ScopeRT scope, CompositeTypeFacade compositeTypeFacade) {
        var isSourcePresent = Nodes.functionCall("来源是否存在", scope, NativeFunctions.isSourcePresent(),
                List.of(Nodes.argument(NativeFunctions.isSourcePresent(), 0, getView.get())), compositeTypeFacade);
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
                    var source = Nodes.functionCall(sourceKlass.getName() + "来源", trueBranch.getScope(),
                            NativeFunctions.getSource(),
                            List.of(Nodes.argument(NativeFunctions.getSource(), 0, getView.get())), compositeTypeFacade);
                    branch2sourceNode.put(trueBranch, Values.node(source));
                },
                falseBranch -> {
                    var source = Nodes.newObject(
                            sourceKlass.getName() + "新建来源",
                            null,
                            falseBranch.getScope(),
                            sourceKlass.isChildList() ?
                                    sourceKlass.getDefaultConstructor() :
                                    sourceReadWriteListType.getDefaultConstructor(),
                            List.of(),
                            false,
                            true
                    );
                    branch2sourceNode.put(falseBranch, Values.node(source));
                },
                mergeNode -> {
                    sourceFieldRef.sourceField = FieldBuilder.newBuilder("来源", null, mergeNode.getType().resolve(), sourceKlass.getType())
                            .build();
                    new MergeNodeField(sourceFieldRef.sourceField, mergeNode, branch2sourceNode);
                }
        );
        var mergeNode = scope.getLastNode();
        var sourceField = sourceFieldRef.sourceField;
        var clearMethod = sourceKlass.getMethodByCodeAndParamTypes("clear", List.of());
        Nodes.methodCall(
                "清空" + sourceKlass.getName(),
                scope,
                Values.nodeProperty(mergeNode, sourceField),
                clearMethod,
                List.of(),
                compositeTypeFacade
        );
        Nodes.listForEach(
                "遍历" + targetKlass.getName(), getView,
                (bodyScope, getElement, getIndex) -> {
                    var getSourceElement = elementNestedMapping.generateUnmappingCode(getElement, bodyScope, compositeTypeFacade);
                    var addMethod = sourceKlass.getMethodByCodeAndParamTypes("add", List.of(sourceKlass.getListElementType()));
                    Nodes.methodCall(
                            "添加元素" + sourceKlass.getName(),
                            bodyScope,
                            Values.nodeProperty(mergeNode, sourceField),
                            addMethod,
                            List.of(
                                    Nodes.argument(
                                            addMethod,
                                            0,
                                            getSourceElement.get()
                                    )
                            ),
                            compositeTypeFacade
                    );
                },
                scope
        );
        return () -> Values.nodeProperty(mergeNode, sourceField);
    }

    @Override
    public Type getTargetType() {
        return targetKlass.getType();
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ListNestedMapping that)) return false;
        if (!super.equals(object)) return false;
        return Objects.equals(sourceKlass, that.sourceKlass) && Objects.equals(targetKlass, that.targetKlass) && Objects.equals(elementNestedMapping, that.elementNestedMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), sourceKlass, targetKlass, elementNestedMapping);
    }
}
