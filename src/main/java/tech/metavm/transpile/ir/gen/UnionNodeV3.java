package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.TypeUnion;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class UnionNodeV3 extends NodeBase {

    private final Set<INode> members;
//    private boolean lowerBoundAttached;
//    private IRType capture;
//    private boolean captureAttached;

    private final Set<INode> unattachedGeValues = new HashSet<>();
    private final Set<INode> unattachedCaptures = new HashSet<>();

    protected UnionNodeV3(TypeUnion type, TypeGraph graph) {
        super(type, graph);
        this.members = NncUtils.mapUnique(type.getTypes(), graph::getNode);
        for (INode member : members) {
//            member.addSuperNode(upperBound);
            member.addListener(this::onMemberChange);
        }
//        lowerBound.addListener(this::onLowerBoundChange);
    }

    @Override
    public TypeUnion getType() {
        return (TypeUnion) super.getType();
    }

//    @Override
//    public IRType getMax() {
//        return TypeUnion.of(NncUtils.map(members, INode::getMax));
//    }

//    @Override
//    public IRType getMin() {
//        return TypeUnion.of(NncUtils.map(members, INode::getMin));
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
        return TypeUnion.of(NncUtils.map(members, INode::solve));
    }

    @Override
    public boolean ge(INode value) {
        if(get().isAssignableFrom(value.get())) {
            if(!tryAttachGe(value)) {
                unattachedGeValues.add(value);
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean le(INode value) {
        return NncUtils.allMatch(members, m -> m.ge(value));
    }

    private boolean tryAttachCapture(INode capture) {
        Queue<INode> queue = new LinkedList<>();
        for (INode member : members) {
            if(member.canCapture(capture)) {
                queue.offer(member);
            }
        }
        if(queue.isEmpty()) {
            throw new InternalException("No member can capture " + capture);
        }
        if(queue.size() == 1) {
            queue.peek().assign(capture);
            return true;
        }
        return false;
    }

    private boolean tryAttachGe(INode value) {
        Queue<INode> queue = new LinkedList<>();
        for (INode member : members) {
            if(member.get().isAssignableFrom(value.get())) {
                queue.offer(member);
            }
        }
        if(queue.isEmpty()) {
            throw new InternalException("No member >= " + value.get());
        }
        if(queue.size() == 1) {
            queue.peek().ge(value);
            return true;
        }
        return false;
    }

    private boolean onMemberChange() {
        unattachedCaptures.removeIf(this::tryAttachCapture);
        unattachedGeValues.removeIf(this::tryAttachGe);
        return true;
    }


    @Override
    public boolean assign(INode type) {
        List<INode> matchedMembers = NncUtils.filter(members, m -> m.assign(type));
        if(matchedMembers.isEmpty()) {
            return false;
        }
        if(matchedMembers.size() == 1) {
            matchedMembers.get(0).assign(type);
        }
        else {
            unattachedCaptures.add(type);
        }
        return true;
    }

    @Override
    public IRType getMaxRange() {
        return TypeUnion.of(NncUtils.map(members, INode::getMaxRange));
    }

    @Override
    public IRType get() {
        return TypeUnion.of(NncUtils.map(members, INode::get));
    }

    @Override
    public IRType getMinRange() {
        return TypeUnion.of(NncUtils.map(members, INode::getMinRange));
    }

}
