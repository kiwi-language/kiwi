package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.StdKlass;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.NodeRT;
import org.metavm.flow.Nodes;
import org.metavm.flow.Code;
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
    public Type generateMappingCode(Supplier<NodeRT> getSource, Code code) {
        var targetKlass = targetType.resolve();
        var constructor = targetKlass.isEffectiveAbstract() ?
                StdKlass.arrayList.get().getParameterized(List.of(targetKlass.getFirstTypeArgument())).getDefaultConstructor() :
                targetKlass.getDefaultConstructor();
        Nodes.newObject(code, constructor, false, true);
        var targetListVar = code.nextVariableIndex();
        Nodes.store(targetListVar, code);
        var setSourceFunc = StdFunction.setSource.get();
        Nodes.load(targetListVar, targetType, code);
        getSource.get();
        Nodes.functionCall(code, setSourceFunc);
        Nodes.listForEach(
                getSource,
                sourceType,
                (getElement, getIndex) -> {
                    var addMethod = targetKlass.getMethodByNameAndParamTypes("add", List.of(
                            Types.getNullableType(targetType.getFirstTypeArgument())
                    ));
                    Nodes.load(targetListVar, targetType, code);
                    elementNestedMapping.generateMappingCode(getElement, code);
                    Nodes.methodCall(addMethod, code);
                    Nodes.pop(code);
                },
                code
        );
        Nodes.load(targetListVar, targetType, code);
        return targetType;
    }

    @Override
    public Type generateUnmappingCode(Supplier<NodeRT> viewSupplier, Code code) {
        var sourceKlass = sourceType.resolve();
        viewSupplier.get();
        Nodes.functionCall(code, StdFunction.isSourcePresent.get());
        Nodes.loadConstant(Instances.falseInstance(), code);
        Nodes.eq(code);
        var ifNode = Nodes.if_(null, code);
        viewSupplier.get();
        Nodes.functionCall(code, StdFunction.getSource.get());
        var i = code.nextVariableIndex();
        Nodes.store(i, code);
        var g = Nodes.goto_(code);
        var newSource = Nodes.newObject(
                code,
                sourceType.isChildList() ?
                        sourceKlass.getDefaultConstructor() :
                        StdKlass.arrayList.get().getParameterized(List.of(sourceKlass.getFirstTypeArgument())).getDefaultConstructor(),
                false,
                true
        );
        ifNode.setTarget(newSource);
        Nodes.store(i, code);
        g.setTarget(Nodes.noop(code));
        var clearMethod = sourceKlass.getMethodByNameAndParamTypes("clear", List.of());
        Nodes.load(i, sourceType, code);
        Nodes.methodCall(clearMethod, code);
        Nodes.listForEach(
                viewSupplier,
                targetType,
                (getElement, getIndex) -> {
                    Nodes.load(i, sourceType, code);
                    elementNestedMapping.generateUnmappingCode(getElement, code);
                    var addMethod = sourceKlass.getMethodByNameAndParamTypes("add",
                            List.of(Types.getNullableType(sourceType.getFirstTypeArgument())));
                    Nodes.methodCall(addMethod, code);
                    Nodes.pop(code);
                },
                code
        );
        Nodes.load(i, sourceType, code);
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