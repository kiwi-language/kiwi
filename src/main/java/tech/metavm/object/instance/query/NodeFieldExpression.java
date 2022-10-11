//package tech.metavm.object.instance.query;
//
//import tech.metavm.flow.NodeRT;
//import tech.metavm.object.meta.Type;
//
//public class NodeFieldExpression extends Expression {
//
//    private final NodeRT<?> node;
//    private final FieldExpression field;
//
//    public NodeFieldExpression(NodeRT<?> node, FieldExpression fieldExpression) {
//        this.node = node;
//        this.field = fieldExpression;
//    }
//
//    public NodeRT<?> getNode() {
//        return node;
//    }
//
//    public FieldExpression getField() {
//        return field;
//    }
//
//    @Override
//    public String buildSelf(VarType symbolType) {
//        String fieldExpr = field.build(symbolType, false);
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
//        return field.getType();
//    }
//
//}
