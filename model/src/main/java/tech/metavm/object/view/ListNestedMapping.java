package tech.metavm.object.view;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.StandardTypes;
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

@EntityType
public class ListNestedMapping extends NestedMapping {

    private final ClassType sourceType;
    private final ClassType targetType;
    @ChildEntity
    private final NestedMapping elementNestedMapping;

    public ListNestedMapping(ClassType sourceType, ClassType targetType, NestedMapping elementNestedMapping) {
        this.sourceType = sourceType;
        this.targetType = targetType;
        this.elementNestedMapping = addChild(elementNestedMapping, "elementNestedMapping");
    }

    @Override
    public Supplier<Value> generateMappingCode(Supplier<Value> getSource, ScopeRT scope) {
        var sourceElementType = sourceType.getListElementType();
        var targetKlass = targetType.resolve();
        var constructor = targetKlass.isEffectiveAbstract() ?
                StandardTypes.getReadWriteListKlass(targetKlass.getListElementType()).getDefaultConstructor() :
                targetKlass.getDefaultConstructor();
        var targetList = Nodes.newObject(
                targetType.getName() + " list",
                scope,
                constructor,
                List.of(),
                false,
                true
        );
        var setSourceFunc = NativeFunctions.setSource();
        Nodes.functionCall(
                "setSource " + targetType.getName(),
                scope,
                setSourceFunc,
                List.of(
                        Nodes.argument(setSourceFunc, 0, Values.node(targetList)),
                        Nodes.argument(setSourceFunc, 1, getSource.get())
                )
        );
        Nodes.listForEach(
                "iterate " + sourceType.getName(),
                getSource,
                (bodyScope, getElement, getIndex) -> {
                    var getTargetElement = elementNestedMapping.generateMappingCode(getElement,
                            bodyScope);
                    var addMethod = targetKlass.getMethodByCodeAndParamTypes("add", List.of(
                            targetType.getListElementType()
                    ));
                    Nodes.methodCall(
                            "add " + sourceElementType.getName(),
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
        var isSourcePresent = Nodes.functionCall("isSourcePresent", scope, NativeFunctions.isSourcePresent(),
                List.of(Nodes.argument(NativeFunctions.isSourcePresent(), 0, getView.get())));
        var branch2sourceNode = new HashMap<Branch, Value>();
        var sourceKlass = sourceType.resolve();
        var sourceFieldRef = new Object() {
            Field sourceField;
        };
        Nodes.branch(
                "branch",
                null,
                scope,
                Values.expression(Expressions.eq(Expressions.node(isSourcePresent), Expressions.trueExpression())),
                trueBranch -> {
                    var source = Nodes.functionCall(sourceType.getName() + " source", trueBranch.getScope(),
                            NativeFunctions.getSource(),
                            List.of(Nodes.argument(NativeFunctions.getSource(), 0, getView.get())));
                    branch2sourceNode.put(trueBranch, Values.node(source));
                },
                falseBranch -> {
                    var source = Nodes.newObject(
                            sourceType.getName() + " new source",
                            falseBranch.getScope(),
                            sourceType.isChildList() ?
                                    sourceKlass.getDefaultConstructor() :
                                    StandardTypes.getReadWriteListKlass(sourceKlass.getListElementType()).getDefaultConstructor(),
                            List.of(),
                            false,
                            true
                    );
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
        var clearMethod = sourceKlass.getMethodByCodeAndParamTypes("clear", List.of());
        Nodes.methodCall(
                "clear " + sourceType.getName(),
                scope,
                Values.nodeProperty(mergeNode, sourceField),
                clearMethod,
                List.of()
        );
        Nodes.listForEach(
                "iterate " + targetType.getName(), getView,
                (bodyScope, getElement, getIndex) -> {
                    var getSourceElement = elementNestedMapping.generateUnmappingCode(getElement, bodyScope);
                    var addMethod = sourceKlass.getMethodByCodeAndParamTypes("add", List.of(sourceType.getListElementType()));
                    Nodes.methodCall(
                            "add element " + sourceType.getName(),
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
    public String getText() {
        return "{\"kind\": \"List\", \"sourceType\": \"" + sourceType.getTypeDesc() + "\", \"targetType\": \"" + targetType.getTypeDesc() + "\", \"elementMapping\": "
                + elementNestedMapping.getText() + "}";
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof ListNestedMapping that)) return false;
        return Objects.equals(sourceType, that.sourceType) && Objects.equals(targetType, that.targetType) && Objects.equals(elementNestedMapping, that.elementNestedMapping);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceType, targetType, elementNestedMapping);
    }
}