package tech.metavm.flow;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.flow.rest.ReturnParamDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;

@EntityType("结束节点")
public class ReturnNode extends NodeRT<ReturnParamDTO> {

    public static ReturnNode create(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope, IEntityContext entityContext) {
        ReturnNode node = new ReturnNode(nodeDTO, prev, scope);
        node.setParam(nodeDTO.getParam(), entityContext);
        return node;
    }

//    @ChildEntity("字段值列表")
//    private final Table<FieldParam> fieldParams = new Table<>(FieldParam.class, true);

    @ChildEntity("结果")
    private @Nullable Value value;

    public ReturnNode(NodeDTO nodeDTO, NodeRT<?> prev, ScopeRT scope) {
        super(
                nodeDTO.tmpId(),
                nodeDTO.name(),
                scope.getFlow().getReturnType(),
                prev,
                scope
        );
    }

    public ReturnNode(Long tmpId, String name, NodeRT<?> prev, ScopeRT scope) {
        super(tmpId, name,  scope.getFlow().getReturnType(), prev, scope);
    }

    public void setValue(@Nullable Value value) {
        this.value = value;
    }

    @Override
    protected void setParam(ReturnParamDTO param, IEntityContext context) {
        if(param.value() != null) {
            value = ValueFactory.create(param.value(), getParsingContext(context));
        }
//        fieldParams.clear();
//        Map<Long, OutputFieldDTO> outFieldMap = NncUtils.toMap(param.fields(), OutputFieldDTO::id);
//        ParsingContext parsingContext = getParsingContext(entityContext);
//        for (Field field : getType().getFields()) {
//            OutputFieldDTO outputFieldDTO = outFieldMap.get(field.getId());
//            if (outputFieldDTO != null) {
//                fieldParams.add(new FieldParam(field, outputFieldDTO.value(), parsingContext));
//            }
//        }
    }

    @Nullable
    public Value getValue() {
        return value;
    }

    @Override
    protected ReturnParamDTO getParam(boolean persisting) {
        return new ReturnParamDTO(
                NncUtils.get(value, v -> v.toDTO(persisting))
        );
//        try (var context = SerializeContext.enter()) {
//            ClassType type = getType();
//            List<OutputFieldDTO> outputFields = new ArrayList<>();
//            for (Field field : type.getFields()) {
//                Value value = fieldValue(field);
//                outputFields.add(new OutputFieldDTO(
//                        context.getRef(field),
//                        field.getName(),
//                        context.getRef(field.getType()),
//                        NncUtils.get(value, v -> v.toDTO(persisting))
//                ));
//            }
//            return new ReturnParamDTO(outputFields);
//        }
    }

//    public void setField(Field field, Value value) {
//        var param = fieldParams.get(FieldParam::getField, field);
//        if (param == null) {
//            param = new FieldParam(field, value);
//            fieldParams.add(param);
//        } else param.setValue(value);
//    }

//    private Value fieldValue(Field field) {
//        return NncUtils.get(
//                fieldParams.get(FieldParam::getField, field),
//                FieldParam::getValue
//        );
//    }

    @Override
    public void execute(FlowFrame frame) {
//        Instance instance = new ClassInstance(
//                getFieldValues(frame),
//                getType()
//        );
        Instance instance = value != null ? value.evaluate(frame) : null;
        frame.ret(instance);
    }

//    private Map<Field, Instance> getFieldValues(FlowFrame frame) {
//        Map<Field, Instance> fieldValues = new HashMap<>();
//        for (Field field : getType().getFields()) {
//            Value value = fieldValue(field);
//            fieldValues.put(
//                    field,
//                    NncUtils.get(value, v -> v.evaluate(frame))
//            );
//        }
//        return fieldValues;
//    }

    @Override
    public boolean isExit() {
        return true;
    }

}
