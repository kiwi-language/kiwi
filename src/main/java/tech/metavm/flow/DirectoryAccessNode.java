package tech.metavm.flow;

import tech.metavm.flow.persistence.NodePO;
import tech.metavm.flow.rest.DeleteObjectParamDTO;
import tech.metavm.flow.rest.DirectoryAccessParamDTO;
import tech.metavm.flow.rest.NodeDTO;
import tech.metavm.object.instance.Directory;
import tech.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.List;

public class DirectoryAccessNode extends NodeRT<DirectoryAccessParamDTO> {

    private Directory directory;
    private final List<FieldParam> fieldParams = new ArrayList<>();

    public DirectoryAccessNode(NodeDTO nodeDTO, DirectoryAccessParamDTO param, ScopeRT scope) {
        super(
                nodeDTO,
                scope.getFromContext(Directory.class, param.directoryId()).getType(),
                scope
        );
        setParam(param);
    }

    public DirectoryAccessNode(NodePO nodePO, DirectoryAccessParamDTO param, ScopeRT scope) {
        super(nodePO, scope);
        setParam(param);
    }

    public Directory getDirectory() {
        return directory;
    }

    public List<FieldParam> getFieldParams() {
        return fieldParams;
    }

    @Override
    protected void setParam(DirectoryAccessParamDTO param) {
        this.directory = getFromContext(Directory.class, param.directoryId());
        fieldParams.addAll(NncUtils.map(
                param.fieldParams(),
                fieldParam -> new FieldParam(fieldParam, getContext())
        ));
    }

    @Override
    protected DirectoryAccessParamDTO getParam(boolean forPersistence) {
        return new DirectoryAccessParamDTO(
                directory.getId(),
                NncUtils.map(fieldParams, FieldParam::toDTO)
        );
    }

    @Override
    public void execute(FlowFrame frame) {
        // TODO to implement
    }
}
