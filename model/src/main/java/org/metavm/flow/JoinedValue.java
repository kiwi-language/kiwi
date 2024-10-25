package org.metavm.flow;

import org.metavm.entity.Entity;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.JoinedValueDTO;

public class JoinedValue extends Entity {

    private final NodeRT sourceNode;
    private final Value value;

    public JoinedValue(NodeRT sourceNode, Value value) {
        this.sourceNode = sourceNode;
        this.value = value;
    }

    public NodeRT getSourceNode() {
        return sourceNode;
    }

    public Value getValue() {
        return value;
    }

    public String getText() {
        return sourceNode.getName() + ": " + value.getText();
    }

    public JoinedValueDTO toDTO(SerializeContext serializeContext) {
        return new JoinedValueDTO(
                serializeContext.getStringId(sourceNode),
                value.toDTO()
        );
    }

}
