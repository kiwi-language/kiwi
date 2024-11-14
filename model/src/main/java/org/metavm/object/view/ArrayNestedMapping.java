package org.metavm.object.view;

import org.metavm.api.ChildEntity;
import org.metavm.api.EntityType;
import org.metavm.entity.natives.StdFunction;
import org.metavm.flow.NodeRT;
import org.metavm.flow.Nodes;
import org.metavm.flow.Code;
import org.metavm.object.type.ArrayType;
import org.metavm.object.type.Type;
import org.metavm.util.Instances;

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
    public Type generateMappingCode(Supplier<NodeRT> getSource, Code code) {
        Nodes.newArray(targetType, code);
        var targetArrayVar = code.nextVariableIndex();
        Nodes.store(targetArrayVar, code);
        var setSourceFunc = StdFunction.setSource.get();
        Nodes.load(targetArrayVar, targetType, code);
        getSource.get();
        Nodes.functionCall(code, setSourceFunc);
        Nodes.forEach(
                getSource,
                (getElement, getIndex) -> {
                    Nodes.load(targetArrayVar, targetType, code);
                    elementNestedMapping.generateMappingCode(getElement, code);
                    Nodes.addElement(code);
                },
                code
        );
        Nodes.load(targetArrayVar, targetType, code);
        return targetType;
    }

    @Override
    public Type generateUnmappingCode(Supplier<NodeRT> getView, Code code) {
        getView.get();
        Nodes.functionCall(code, StdFunction.isSourcePresent.get());
        Nodes.loadConstant(Instances.falseInstance(), code);
        Nodes.eq(code);
        var ifNode = Nodes.if_(null, code);
        getView.get();
        Nodes.functionCall(code, StdFunction.getSource.get());
        var sourceVar = code.nextVariableIndex();
        Nodes.store(sourceVar, code);
        var g = Nodes.goto_(code);
        var newSource = Nodes.newArray(sourceType, code);
        ifNode.setTarget(newSource);
        Nodes.store(sourceVar, code);
        g.setTarget(Nodes.noop(code));
        Nodes.load(sourceVar, sourceType, code);
        Nodes.clearArray(code);
        Nodes.forEach(
                getView,
                (element, index) -> {
                    Nodes.load(sourceVar, sourceType, code);
                    elementNestedMapping.generateUnmappingCode(element, code);
                    Nodes.addElement(code);
                },
                code
        );
        Nodes.load(sourceVar, sourceType, code);
        return sourceType;
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
