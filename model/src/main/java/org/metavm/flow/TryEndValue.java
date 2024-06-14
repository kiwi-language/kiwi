package org.metavm.flow;

import org.metavm.entity.Entity;
import org.metavm.entity.EntityType;
import org.metavm.entity.SerializeContext;
import org.metavm.flow.rest.TryEndValueDTO;

@EntityType
public class TryEndValue extends Entity {

    private NodeRT raiseNode;
    private Value value;

    public TryEndValue(NodeRT raiseNode, Value value) {
        this.raiseNode = raiseNode;
        this.value = value;
    }

    public NodeRT getRaiseNode() {
        return raiseNode;
    }

    public void setRaiseNode(NodeRT raiseNode) {
        this.raiseNode = raiseNode;
    }

    public Value getValue() {
        return value;
    }

    public void setValue(Value value) {
        this.value = value;
    }

    public TryEndValueDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return new TryEndValueDTO(serContext.getStringId(raiseNode), value.toDTO());
        }
    }

    public String getText() {
        return raiseNode.getName() + ": " + value.getText();
    }
}
