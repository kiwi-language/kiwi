package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.AddObjectParamDTO;
import tech.metavm.flow.rest.FieldParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.meta.Type;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public class AddObjectNode extends NodeRT<AddObjectParamDTO> {

    private final List<FieldParam> fields = new ArrayList<>();

    public AddObjectNode(NodeDTO nodeDTO, AddObjectParamDTO param, ScopeRT scope) {
        super(nodeDTO, scope.getTypeFromContext(param.typeId()), scope);
        setParam(param);
    }

    public AddObjectNode(NodePO nodePO, AddObjectParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        setParam(param);
    }

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
        setOutputType(getTypeFromContext(param.typeId()));
        fields.clear();
        fields.addAll(NncUtils.map(
                param.fieldParams(),
                fp -> new FieldParam(fp, getContext(), getParsingContext())
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
