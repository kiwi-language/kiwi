package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.TypeIntersection;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class IntersectionNode extends NodeBase {

    private final Set<INode> members;
//    private boolean upperBoundAttached;

    private final Set<INode> unattachedLeValues = new HashSet<>();

    protected IntersectionNode(TypeIntersection type, TypeGraph graph) {
        super(type, graph);
        members = NncUtils.mapUnique(type.getTypes(), graph::getNode);
        for (INode member : members) {
            member.addListener(this::onMemberChange);
        }
//        members.forEach(m -> m.addExtendingNode(lowerBound));
//        upperBound.addListener(this::onUpperBoundChange);
    }

//    private boolean onUpperBoundChange() {
//        //noinspection DuplicatedCode
//        if(upperBoundAttached) {
//            return true;
//        }
//        Queue<INode> queue = new LinkedList<>();
//        for (INode member : members) {
//            if(upperBound.getMax().isAssignableFrom(member.getMin())) {
//                queue.offer(member);
//            }
//        }
//        if(queue.isEmpty()) {
//            return false;
//        }
//        if(queue.size() == 1) {
//            upperBoundAttached = true;
//            return queue.peek().addSuperNode(upperBound);
//        }
//        else {
//            return true;
//        }
//    }

    private boolean onMemberChange() {
        unattachedLeValues.removeIf(this::tryAttachLeValue);
        return true;
    }

    private boolean tryAttachLeValue(INode value) {
        List<INode> matchedMembers = NncUtils.filter(
                members, m -> value.get().isAssignableFrom(m.get())
        );
        if(matchedMembers.isEmpty()) {
            throw new InternalException("No member <= " + value.get());
        }
        if(matchedMembers.size() == 1) {
            matchedMembers.get(0).le(value);
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public TypeIntersection getType() {
        return (TypeIntersection) super.getType();
    }

//    @Override
//    public IRType getMax() {
//        return TypeIntersection.of(NncUtils.map(members, INode::getMax));
//    }

//    @Override
//    public IRType getMin() {
//        return TypeIntersection.of(NncUtils.map(members, INode::getMin));
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
        return TypeIntersection.of(NncUtils.map(members, INode::solve));
    }

    @Override
    public boolean ge(INode value) {
        return NncUtils.allMatch(members, m -> m.ge(value));
    }

    @Override
    public boolean le(INode value) {
        if(value.get().isAssignableFrom(get())) {
            if(!tryAttachLeValue(value)) {
                unattachedLeValues.add(value);
            }
            return true;
        }
        else {
            return false;
        }
    }

    @Override
    public boolean assign(INode type) {
        return NncUtils.allMatch(members, m -> m.assign(type));
    }

    @Override
    public IRType getMaxRange() {
        return TypeIntersection.of(NncUtils.map(members, INode::getMaxRange));
    }

    @Override
    public IRType get() {
        return TypeIntersection.of(NncUtils.map(members, INode::get));
    }

    @Override
    public IRType getMinRange() {
        return TypeIntersection.of(NncUtils.map(members, INode::getMinRange));
    }
}
