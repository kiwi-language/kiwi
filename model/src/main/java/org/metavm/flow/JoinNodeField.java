package org.metavm.flow;

import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.entity.*;
import org.metavm.flow.rest.JoinNodeFieldDTO;
import org.metavm.object.type.Field;
import org.metavm.util.NncUtils;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

public class JoinNodeField extends Entity implements LocalKey {

    private final Field field;
    @ChildEntity
    private final ChildArray<JoinedValue> values = addChild(new ChildArray<>(JoinedValue.class), "values");

    public JoinNodeField(Field field, JoinNode joinNode, Map<? extends NodeRT, Value> values) {
        this(field, joinNode);
        values.forEach((n, v) -> this.values.addChild(new JoinedValue(n, v)));
    }

    public JoinNodeField(Field field, JoinNode joinNode) {
        this.field = field;
        joinNode.addField(this);
    }

    @Override
    public boolean isValidLocalKey() {
        return field.getCode() != null;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return field.getCodeNotNull();
    }

    public String getText() {
        return field.getName() + ": {" + NncUtils.join(values, JoinedValue::getText, ",") + "}";
    }

    public JoinNodeFieldDTO toDTO(SerializeContext serializeContext) {
        return new JoinNodeFieldDTO(
                field.getName(),
                serializeContext.getStringId(field),
                serializeContext.getStringId(field.getType()),
                NncUtils.sortAndMap(values,
                        Comparator.comparing(v -> serializeContext.getId(v.getSourceNode())),
                        v  -> v.toDTO(serializeContext))
        );
    }

    public void setValue(NodeRT sourceNode, Value value) {
        values.addChild(new JoinedValue(sourceNode, value));
    }

    public Field getField() {
        return field;
    }

    public Value getValue(NodeRT sourceNode) {
        if(NncUtils.find(values, v -> v.getSourceNode() == sourceNode) == null) {
            var joinNode = (JoinNode) Objects.requireNonNull(Objects.requireNonNull(getParentEntity()).getParentEntity());
            logger.debug("Value not found. join node: {}, field: {}, source node: {}",
                    joinNode.getName(), field.getName(), sourceNode.getName());
        }
        return NncUtils.findRequired(values, v -> v.getSourceNode() == sourceNode).getValue();
    }

}
