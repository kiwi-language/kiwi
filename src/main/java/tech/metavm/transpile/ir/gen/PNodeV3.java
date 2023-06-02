package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.*;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public class PNodeV3 extends NodeBase {

    @Nullable
    private final INode ownerNode;
    private final INode rawTypeNode;
    private final List<INode> argumentNodes;

    protected PNodeV3(PType type, TypeGraph graph) {
        super(type, graph);
        this.ownerNode = NncUtils.get(type.getOwnerType(), graph::getNode);
        this.rawTypeNode = graph.getNode(type.getRawType());
        this.argumentNodes = NncUtils.map(type.getTypeArguments(), graph::getNode);
    }

    @Override
    public PType getType() {
        return (PType) super.getType();
    }

//    @Override
//    public IRType getMax() {
//        return new PType(
//                NncUtils.get(ownerNode, INode::getMax),
//                rawTypeNode.getMax(),
//                NncUtils.map(argumentNodes, INode::getMaxRange)
//        );
//    }
//
//    @Override
//    public IRType getMin() {
//        return new PType(
//                NncUtils.get(ownerNode, INode::getMin),
//                rawTypeNode.getMin(),
//                NncUtils.map(argumentNodes, INode::getMinRange)
//        );
//    }

    @Override
    public INode getLowerBound() {
        return this;
    }

    @Override
    public INode getUpperBound() {
        return this;
    }

    @Override
    public IRType solve() {
        return new PType(
                NncUtils.get(ownerNode, INode::solve),
                rawTypeNode.solve(),
                NncUtils.map(argumentNodes, INode::solve)
        );
    }

    @Override
    public boolean assign(INode node) {
        if(!rawTypeNode.assign(new ExtensionRawNode(node))) {
            return false;
        }
        if(!rawTypeNode.assign(new ExtensionOwnerNode(node))) {
            return false;
        }
        for (int i = 0; i < argumentNodes.size(); i++) {
            if(argumentNodes.get(i).assign(new ExtensionArgumentNode(node, getTypeVariable(i), false))) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean le(INode value) {
        if(!rawTypeNode.le(new SuperRawNode(value))) {
            return false;
        }
        if(ownerNode != null) {
            ownerNode.le(new SuperOwnerNode(value, (IRClass) rawTypeNode.getType()));
        }
        for (int i = 0; i < argumentNodes.size(); i++) {
            var argNode = argumentNodes.get(i);
            if(!argNode.le(new SuperArgumentNode(value, getTypeVariable(i), true))) {
                return false;
            }
            if(!argNode.ge(new SuperArgumentNode(value, getTypeVariable(i), false))) {
                return false;
            }
        }
        return true;
    }

    private IRClass getRawClass() {
        return (IRClass) rawTypeNode.getType();
    }

    @Override
    public boolean ge(INode node) {
        if (processGe(node)) {
            node.addListener(() -> processGe(node));
            return true;
        }
        return false;
    }

    private boolean processGe(INode node) {
        if(!rawTypeNode.ge(new ExtensionRawNode(node))) {
            return false;
        }
        if(ownerNode != null &&
                !ownerNode.ge(new ExtensionOwnerNode(node))) {
            return false;
        }
        var lowerBounds = AssignUtil.extractLowerBounds(node);
        for (int i = 0; i < argumentNodes.size(); i++) {
            var argNode = argumentNodes.get(i);
            var typeParam = getTypeVariable(i);
            for (INode lowerBound : lowerBounds) {
                if(!argNode.assign(new ExtensionArgumentNode(lowerBound,typeParam, false ))) {
                    return false;
                }
            }
        }
        return true;
    }

    private TypeVariable<IRClass> getTypeVariable(int index) {
        var klass = (IRClass) rawTypeNode.getType();
        return klass.typeParameters().get(index);
    }

    @Override
    public IRType get() {
        return new PType(
                NncUtils.get(ownerNode, INode::get),
                rawTypeNode.get(),
                NncUtils.map(argumentNodes, INode::get)
        );
    }
}
