package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.BuiltinKlasses;
import org.metavm.entity.natives.NativeFunctions;
import org.metavm.expression.Expressions;
import org.metavm.flow.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Field;
import org.metavm.object.type.FieldBuilder;
import org.metavm.object.type.Type;

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
        var targetKlass = targetType.resolve();
        var constructor = targetKlass.isEffectiveAbstract() ?
                BuiltinKlasses.arrayList.get().getParameterized(List.of(targetKlass.getListElementType())).getDefaultConstructor() :
                targetKlass.getDefaultConstructor();
        var targetList = Nodes.newObject(
                scope.nextNodeName("list"),
                scope,
                constructor,
                List.of(),
                false,
                true
        );
        var setSourceFunc = NativeFunctions.setSource.get();
        Nodes.functionCall(
                scope.nextNodeName("setSource"),
                scope,
                setSourceFunc,
                List.of(
                        Nodes.argument(setSourceFunc, 0, Values.node(targetList)),
                        Nodes.argument(setSourceFunc, 1, getSource.get())
                )
        );
        Nodes.listForEach(
                scope.nextNodeName("iterate"),
                getSource,
                (bodyScope, getElement, getIndex) -> {
                    var getTargetElement = elementNestedMapping.generateMappingCode(getElement,
                            bodyScope);
                    var addMethod = targetKlass.getMethodByCodeAndParamTypes("add", List.of(
                            targetType.getListElementType()
                    ));
                    Nodes.methodCall(
                            scope.nextNodeName("add"),
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
        var isSourcePresent = Nodes.functionCall(scope.nextNodeName("isSourcePresent"), scope, NativeFunctions.isSourcePresent.get(),
                List.of(Nodes.argument(NativeFunctions.isSourcePresent.get(), 0, getView.get())));
        var branch2sourceNode = new HashMap<Branch, Value>();
        var sourceKlass = sourceType.resolve();
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
                            NativeFunctions.getSource.get(),
                            List.of(Nodes.argument(NativeFunctions.getSource.get(), 0, getView.get())));
                    branch2sourceNode.put(trueBranch, Values.node(source));
                },
                falseBranch -> {
                    var source = Nodes.newObject(
                            scope.nextNodeName("newSource"),
                            falseBranch.getScope(),
                            sourceType.isChildList() ?
                                    sourceKlass.getDefaultConstructor() :
                                    BuiltinKlasses.arrayList.get().getParameterized(List.of(sourceKlass.getListElementType())).getDefaultConstructor(),
                            List.of(),
                            false,
                            true
                    );
                    branch2sourceNode.put(falseBranch, Values.node(source));
                },
                mergeNode -> {
                    sourceFieldRef.sourceField = FieldBuilder.newBuilder(scope.nextNodeName("source"), null, mergeNode.getType().resolve(), sourceType)
                            .build();
                    new MergeNodeField(sourceFieldRef.sourceField, mergeNode, branch2sourceNode);
                }
        );
        var mergeNode = scope.getLastNode();
        var sourceField = sourceFieldRef.sourceField;
        var clearMethod = sourceKlass.getMethodByCodeAndParamTypes("clear", List.of());
        Nodes.methodCall(
                scope.nextNodeName("clear"),
                scope,
                Values.nodeProperty(mergeNode, sourceField),
                clearMethod,
                List.of()
        );
        Nodes.listForEach(
                scope.nextNodeName("iterate"), getView,
                (bodyScope, getElement, getIndex) -> {
                    var getSourceElement = elementNestedMapping.generateUnmappingCode(getElement, bodyScope);
                    var addMethod = sourceKlass.getMethodByCodeAndParamTypes("add", List.of(sourceType.getListElementType()));
                    Nodes.methodCall(
                            scope.nextNodeName("addElement"),
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