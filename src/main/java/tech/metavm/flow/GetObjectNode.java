//package tech.metavm.flow;
//
//import tech.metavm.entity.EntityContext;
//import tech.metavm.entity.EntityType;
//import tech.metavm.flow.persistence.NodePO;
//import tech.metavm.flow.rest.GetObjectParamDTO;
//import tech.metavm.flow.rest.NodeDTO;
//import tech.metavm.object.instance.IInstance;
//import tech.metavm.object.meta.Type;
//
//@EntityType("查询对象节点")
//public class GetObjectNode extends NodeRT<GetObjectParamDTO> {
//
//    private Type objectType;
//    private Value objectId;
//
//    public GetObjectNode(NodeDTO nodeDTO, GetObjectParamDTO param, ScopeRT scope) {
//        super(nodeDTO, scope.getContext().getType(param.typeId()), scope);
//        setParam(param);
//    }
//
//    public GetObjectNode(NodePO nodePO, GetObjectParamDTO param, EntityContext context) {
//        super(nodePO, context);
//        setParam(param);
//    }
//
//    public Type getObjectType() {
//        return objectType;
//    }
//
//    public Value getObjectId() {
//        return objectId;
//    }
//
//    @Override
//    protected void setParam(GetObjectParamDTO param) {
//        objectType = context.getType(param.typeId());
//        objectId = ValueFactory.getValue(param.id(), getParsingContext());
//    }
//
//    @Override
//    protected GetObjectParamDTO getParam(boolean persisting) {
//        return new GetObjectParamDTO(objectType.getId(), objectId.toDTO(persisting));
//    }
//
//    @Override
//    public void execute(FlowFrame frame) {
//        long id = (long) objectId.evaluate(frame);
//        IInstance instance = frame.getInstance(id);
//        frame.setResult(instance);
//    }
//}
