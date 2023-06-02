package tech.metavm.transpile.ir.gen2;

import tech.metavm.transpile.ObjectClass;
import tech.metavm.transpile.ir.*;
import tech.metavm.transpile.ir.gen.XType;
import tech.metavm.util.NncUtils;

import java.util.HashSet;
import java.util.Set;

public class XNode extends Node {

    private IRType min;
    private IRType max;
    private final Set<Node> superNodes;
    private IRType value;

    public XNode(XType type) {
        this(type, IRAnyType.getInstance(), ObjectClass.getInstance(), null, new HashSet<>());
    }

    public XNode(IRType type, IRType min, IRType max, IRType value, Set<Node> superNodes) {
        super(type);
        this.min = min;
        this.max = max;
        this.value = value;
        this.superNodes = new HashSet<>(superNodes);
    }

    @Override
    public XType getType() {
        return (XType) super.getType();
    }

    public boolean addSuperNode(Node node) {
        superNodes.add(node);
        return node.addLowerBound(getLowerBound());
    }

    private IRType getLowerBound() {
        return min;
    }

    @Override
    public IRType solve() {
        if(value != null) {
            return value;
        }
        return value = min;
    }

    @Override
    public boolean addLowerBound(IRType type) {
        if(value != null) {
            return value.isAssignableFrom(type);
        }
        if(max.isAssignableFrom(type)) {
            min = TypeUnion.of(min, type);
            return NncUtils.allMatch(superNodes, s -> s.addLowerBound(type));
        }
        else {
            return false;
        }
    }

    @Override
    public boolean addUpperBound(IRType type) {
        if(value != null) {
            return type.isAssignableFrom(value);
        }
        if(type.isAssignableFrom(min)) {
            max = TypeIntersection.of(max, type);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    protected boolean assign(IRType type) {
        if(type instanceof IRWildCardType wct) {
            type = new CaptureType(wct);
        }
        if(value != null) {
            return value.typeEquals(type);
        }
        if(!max.isAssignableFrom(type) || !type.isAssignableFrom(min)) {
            return false;
        }
        value = type;
        return true;
    }

    @Override
    protected XNode copy(Graph graphCopy) {
        return new XNode(getType(), min, max, value, NncUtils.mapUnique(superNodes, graphCopy::copyNode));
    }

}
