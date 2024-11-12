package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.NodeRT;
import org.metavm.flow.Nodes;
import org.metavm.flow.ScopeRT;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Type;
import org.metavm.object.type.Types;
import org.metavm.util.Instances;

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
    public Type generateMappingCode(Supplier<NodeRT> getSource, ScopeRT scope) {
        var targetKlass = targetType.resolve();
        var constructor = targetKlass.isEffectiveAbstract() ?
                StdKlass.arrayList.get().getParameterized(List.of(targetKlass.getFirstTypeArgument())).getDefaultConstructor() :
                targetKlass.getDefaultConstructor();
        Nodes.newObject(scope, constructor, false, true);
        var targetListVar = scope.nextVariableIndex();
        Nodes.store(targetListVar, scope);
        var setSourceFunc = StdFunction.setSource.get();
        Nodes.load(targetListVar, targetType, scope);
        getSource.get();
        Nodes.functionCall(scope, setSourceFunc);
        Nodes.listForEach(
                getSource,
                sourceType,
                (bodyScope, getElement, getIndex) -> {
                    var addMethod = targetKlass.getMethodByCodeAndParamTypes("add", List.of(
                            Types.getNullableType(targetType.getFirstTypeArgument())
                    ));
                    Nodes.load(targetListVar, targetType, scope);
                    elementNestedMapping.generateMappingCode(getElement, bodyScope);
                    Nodes.methodCall(addMethod, bodyScope);
                    Nodes.pop(scope);
                },
                scope
        );
        Nodes.load(targetListVar, targetType, scope);
        return targetType;
    }

    @Override
    public Type generateUnmappingCode(Supplier<NodeRT> viewSupplier, ScopeRT scope) {
        var sourceKlass = sourceType.resolve();
        viewSupplier.get();
        Nodes.functionCall(scope, StdFunction.isSourcePresent.get());
        Nodes.loadConstant(Instances.falseInstance(), scope);
        Nodes.eq(scope);
        var ifNode = Nodes.if_(null, scope);
        viewSupplier.get();
        Nodes.functionCall(scope, StdFunction.getSource.get());
        var i = scope.nextVariableIndex();
        Nodes.store(i, scope);
        var g = Nodes.goto_(scope);
        var newSource = Nodes.newObject(
                scope,
                sourceType.isChildList() ?
                        sourceKlass.getDefaultConstructor() :
                        StdKlass.arrayList.get().getParameterized(List.of(sourceKlass.getFirstTypeArgument())).getDefaultConstructor(),
                false,
                true
        );
        ifNode.setTarget(newSource);
        Nodes.store(i, scope);
        g.setTarget(Nodes.noop(scope));
        var clearMethod = sourceKlass.getMethodByCodeAndParamTypes("clear", List.of());
        Nodes.load(i, sourceType, scope);
        Nodes.methodCall(clearMethod, scope);
        Nodes.listForEach(
                viewSupplier,
                targetType,
                (bodyScope, getElement, getIndex) -> {
                    Nodes.load(i, sourceType, scope);
                    elementNestedMapping.generateUnmappingCode(getElement, bodyScope);
                    var addMethod = sourceKlass.getMethodByCodeAndParamTypes("add",
                            List.of(Types.getNullableType(sourceType.getFirstTypeArgument())));
                    Nodes.methodCall(addMethod, bodyScope);
                    Nodes.pop(scope);
                },
                scope
        );
        Nodes.load(i, sourceType, scope);
        return sourceType;
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