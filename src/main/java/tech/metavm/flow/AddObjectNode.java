package tech.metavm.flow;

import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.InstanceContext;
import tech.metavm.entity.EntityType;
import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.AddObjectParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.IdConstants;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.List;

@EntityType("新增记录节点")
public class AddObjectNode extends NodeRT<AddObjectParamDTO> {

    @EntityField("字段")
    private final Table<FieldParam> fields = new Table<>();

    public AddObjectNode(NodeDTO nodeDTO, Type type, ScopeRT scope) {
        super(nodeDTO, type, scope);
        setParam(nodeDTO.getParam());
    }

//    public AddObjectNode(NodeDTO nodeDTO, AddObjectParamDTO param, ScopeRT scope) {
//        super(nodeDTO, scope.getContext().getType(param.typeId()), scope);
//        setParam(param);
//    }

    public List<FieldParam> getFields() {
        return fields;
    }

    @Override
    protected AddObjectParamDTO getParam(boolean persisting) {
        return new AddObjectParamDTO(
                getOutputType().getId(),
                NncUtils.map(fields, fp -> fp.toDTO(persisting))
        );
    }

    @Override
    protected void setParam(AddObjectParamDTO param) {
//        setOutputType(context.getType(param.typeId()));
        fields.clear();
        fields.addAll(NncUtils.map(
                param.fieldParams(),
                fp -> new FieldParam(getOutputType().getField(fp.fieldId()), fp.value(), getParsingContext())
        ));
    }

    @Override
    public void execute(FlowFrame frame) {
        frame.setResult(
            frame.addInstance(
                InstanceDTO.valueOf(
                    NncUtils.get(getOutputType(), Type::getId),
                    NncUtils.map(fields, fp -> fp.evaluate(frame))
                )
            )
        );
    }

}
