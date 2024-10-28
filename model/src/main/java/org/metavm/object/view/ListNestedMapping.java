package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.StdFunction;
import org.metavm.expression.Expressions;
import org.metavm.flow.*;
import org.metavm.object.type.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                StdKlass.arrayList.get().getParameterized(List.of(targetKlass.getFirstTypeArgument())).getDefaultConstructor() :
                targetKlass.getDefaultConstructor();
        var targetList = Nodes.newObject(
                scope.nextNodeName("list"),
                scope,
                constructor,
                List.of(),
                false,
                true
        );
        var setSourceFunc = StdFunction.setSource.get();
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
                            Types.getNullableType(targetType.getFirstTypeArgument())
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
        var isSourcePresent = Nodes.functionCall(scope.nextNodeName("isSourcePresent"), scope, StdFunction.isSourcePresent.get(),
                List.of(Nodes.argument(StdFunction.isSourcePresent.get(), 0, getView.get())));
        var sourceKlass = sourceType.resolve();
        Map<NodeRT, Value> exit2value = new HashMap<>();
        var ifNode = Nodes.if_(scope.nextNodeName("if"),
                Values.node(Nodes.eq(Expressions.node(isSourcePresent), Expressions.falseExpression(), scope)),
                null,
                scope
        );
        var existingSource = Nodes.functionCall(scope.nextNodeName("source"), scope,
                StdFunction.getSource.get(),
                List.of(Nodes.argument(StdFunction.getSource.get(), 0, getView.get())));
        var g = Nodes.goto_(scope.nextNodeName("goto"), scope);
        exit2value.put(g, Values.node(existingSource));
        var newSource = Nodes.newObject(
                scope.nextNodeName("newSource"),
                scope,
                sourceType.isChildList() ?
                        sourceKlass.getDefaultConstructor() :
                        StdKlass.arrayList.get().getParameterized(List.of(sourceKlass.getFirstTypeArgument())).getDefaultConstructor(),
                List.of(),
                false,
                true
        );
        ifNode.setTarget(newSource);
        exit2value.put(newSource, Values.node(newSource));
        var join = Nodes.join(scope.nextNodeName("join"), scope);
        g.setTarget(join);
        var sourceField = FieldBuilder.newBuilder("source", null, join.getKlass(), sourceType).build();
        new JoinNodeField(sourceField, join, exit2value);
        var clearMethod = sourceKlass.getMethodByCodeAndParamTypes("clear", List.of());
        Nodes.methodCall(
                scope.nextNodeName("clear"),
                scope,
                Values.node(Nodes.nodeProperty(join, sourceField, scope)),
                clearMethod,
                List.of()
        );
        Nodes.listForEach(
                scope.nextNodeName("iterate"), getView,
                (bodyScope, getElement, getIndex) -> {
                    var getSourceElement = elementNestedMapping.generateUnmappingCode(getElement, bodyScope);
                    var addMethod = sourceKlass.getMethodByCodeAndParamTypes("add",
                            List.of(Types.getNullableType(sourceType.getFirstTypeArgument())));
                    Nodes.methodCall(
                            scope.nextNodeName("addElement"),
                            bodyScope,
                            Values.node(Nodes.nodeProperty(join, sourceField, scope)),
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
        var source = Nodes.nodeProperty(join, sourceField, scope);
        return () -> Values.node(source);
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