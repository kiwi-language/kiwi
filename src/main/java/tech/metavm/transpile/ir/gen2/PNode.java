package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.ObjectClass;
import tech.metavm.transpile.ir.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;

public class PNode extends Node {

    @Nullable
    private final Node owner;
    private final CNode raw;
    private final List<Node> arguments;
    private final Graph graph;

    public PNode(PType type, Graph graph) {
        super(type);
        this.owner = NncUtils.get(type.getOwnerType(), graph::getNode);
        this.raw = (CNode) graph.getNode(type.getRawClass());
        this.arguments = NncUtils.map(type.getTypeArguments(), graph::getNode);
        this.graph = graph;
    }

    public PNode(IRType type, @Nullable Node owner, CNode raw, List<Node> arguments, Graph graph) {
        super(type);
        this.owner = owner;
        this.raw = raw;
        this.arguments = arguments;
        this.graph = graph;
    }

    @Override
    public IRType solve() {
        return new PType(
                NncUtils.get(owner, Node::solve),
                rawClass(),
                NncUtils.map(arguments, Node::solve)
        );
    }

    @Override
    public boolean addLowerBound(IRType type) {
        if(type instanceof IRAnyType) {
            return true;
        }
        if(rawClass().isAssignableFrom(type)) {
            var pType = (PType) IRUtil.getAncestorType(type, rawClass());
            if(owner != null && !owner.addLowerBound(pType.getOwnerType())) {
                return false;
            }
            return assignArguments(pType.getTypeArguments());
        }
        else {
            return false;
        }
    }

    @Override
    public boolean addUpperBound(IRType type) {
        if(type instanceof ObjectClass) {
            return true;
        }
        var thatRawClass = IRUtil.getRawClass(type);
        if(thatRawClass.equals(rawClass())) {
            if(type instanceof PType pType) {
                if (owner != null && !owner.addUpperBound(pType.getOwnerType())) {
                    return false;
                }
                for (int i = 0; i < arguments.size(); i++) {
                    if(!arguments.get(i).within(pType.getTypeArgument(i))) {
                        return false;
                    }
                }
                return true;
            }
            else if(type instanceof IRClass) {
                return true;
            }
            else {
                throw new InternalException("Unexpected upper bound for PType " + type);
            }
        }
        else if(thatRawClass.isAssignableFrom(rawClass())) {
            var ancestor = IRUtil.getAncestorType(getType(), thatRawClass);
            return graph.getNode(ancestor).addUpperBound(type);
        }
        else {
            return false;
        }
    }

    private boolean assignArguments(List<IRType> argumentAssignments) {
        return NncUtils.allTrue(
                NncUtils.biMap(
                    arguments,
                    argumentAssignments,
                    Node::assign
                )
        );
    }

    private IRClass rawClass() {
        return (IRClass) raw.getType();
    }

    @Override
    protected boolean assign(IRType type) {
        if(type instanceof PType pType) {
            if(rawClass().equals(pType.getRawClass())) {
                return false;
            }
            if(owner != null && (pType.getOwnerType() == null || !owner.assign(pType.getOwnerType()))) {
                return false;
            }
            return assignArguments(pType.getTypeArguments());
        }
        else {
            return false;
        }
    }


    @Override
    protected PNode copy(Graph graphCopy) {
        return new PNode(
                getType(),
                NncUtils.get(owner, graphCopy::copyNode),
                (CNode) graphCopy.copyNode(raw),
                NncUtils.map(arguments, graphCopy::copyNode),
                graphCopy
        );
    }

}
