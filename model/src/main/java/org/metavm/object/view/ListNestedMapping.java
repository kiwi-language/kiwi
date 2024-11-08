package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;

import java.util.List;
import java.util.Objects;

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
    public Value generateMappingCode(Value source, ScopeRT scope) {
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
                        Nodes.argument(setSourceFunc, 1, source)
                )
        );
        Nodes.listForEach(
                source,
                (bodyScope, element, index) -> {
                    var getTargetElement = elementNestedMapping.generateMappingCode(element, bodyScope);
                    var addMethod = targetKlass.getMethodByCodeAndParamTypes("add", List.of(
                            Types.getNullableType(targetType.getFirstTypeArgument())
                    ));
                    Nodes.methodCall(
                            scope.nextNodeName("add"),
                            Values.node(targetList), addMethod, List.of(
                                    Nodes.argument(
                                            addMethod,
                                            0,
                                            getTargetElement
                                    )
                            ), bodyScope
                    );
                },
                scope
        );
        return Values.node(targetList);
    }

    @Override
    public Value generateUnmappingCode(Value view, ScopeRT scope) {
        var isSourcePresent = Nodes.functionCall(scope.nextNodeName("isSourcePresent"), scope, StdFunction.isSourcePresent.get(),
                List.of(Nodes.argument(StdFunction.isSourcePresent.get(), 0, view)));
        var sourceKlass = sourceType.resolve();
        var ifNode = Nodes.if_(Values.node(Nodes.eq(Values.node(isSourcePresent), Values.constantFalse(), scope)),
                null, scope);
        var existingSource = Nodes.functionCall(scope.nextNodeName("source"), scope,
                StdFunction.getSource.get(),
                List.of(Nodes.argument(StdFunction.getSource.get(), 0, view)));
        var i = scope.nextVariableIndex();
        Nodes.store(i, Values.node(existingSource), scope);
        var g = Nodes.goto_(scope);
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
        Nodes.store(i, Values.node(newSource), scope);
        g.setTarget(Nodes.noop(scope));
        var clearMethod = sourceKlass.getMethodByCodeAndParamTypes("clear", List.of());
        var source = Values.node(Nodes.load(i, sourceType, scope));
        Nodes.methodCall(source, clearMethod, List.of(), scope);
        Nodes.listForEach(
                view,
                (bodyScope, element, index) -> {
                    var getSourceElement = elementNestedMapping.generateUnmappingCode(element, bodyScope);
                    var addMethod = sourceKlass.getMethodByCodeAndParamTypes("add",
                            List.of(Types.getNullableType(sourceType.getFirstTypeArgument())));
                    Nodes.methodCall(source, addMethod, List.of(getSourceElement), bodyScope);
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