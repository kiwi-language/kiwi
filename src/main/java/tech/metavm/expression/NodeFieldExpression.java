//package tech.metavm.object.instance.query;
//
//import tech.metavm.flow.NodeRT;
//import tech.metavm.object.meta.Type;
//
//public class NodeFieldExpression extends Expression {
//
//    private final NodeRT<?> node;
//    private final FieldExpression indexItem;
//
//    public NodeFieldExpression(NodeRT<?> node, FieldExpression fieldExpression) {
//        this.node = node;
//        this.indexItem = fieldExpression;
//    }
//
//    public NodeRT<?> getNode() {
//        return node;
//    }
//
//    public FieldExpression getField() {
//        return indexItem;
//    }
//
//    @Override
//    public String buildSelf(VarType symbolType) {
//        String fieldExpr = indexItem.build(symbolType, false);
//        return switch (symbolType) {
//            case ID -> idVarName(node.getId()) + "." + fieldExpr;
//            case NAME -> node.getName() + "." + fieldExpr;
//        };
//    }
//
//    @Override
//    public int precedence() {
//        return 0;
//    }
//
//    @Override
//    public Type getType() {
//        return indexItem.getType();
//    }
//
//}
