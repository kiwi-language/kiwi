package tech.metavm.transpile.ir.gen;

import tech.metavm.transpile.ObjectClass;
import tech.metavm.transpile.ir.*;
import tech.metavm.util.NncUtils;

import java.util.HashSet;
import java.util.Set;

public class XNode extends NodeBase {

    public static final Set<Class<? extends IRType>> CAPTURE_CLASS_BLACKLIST = Set.of(
      XType.class, SolvingType.class, IRWildCardType.class
    );

    private IRType solution;

    private final Set<INode> captures = new HashSet<>();
    private final Set<INode> maximums = new HashSet<>();
    private final Set<INode> minimums = new HashSet<>();

    public XNode(IRType type, TypeGraph graph) {
        super(type, graph);
    }

    @Override
    public final INode getLowerBound() {
        return this;
    }

    @Override
    public final INode getUpperBound() {
        return this;
    }

    @Override
    public XType getType() {
        return (XType) super.getType();
    }

    @Override
    public boolean assign(INode captured) {
        captures.add(captured);
        captured.addListener(this::ensureAllCapturesEqual);
//        ensureValidCapture(captured);
//        if(this.value != null) {
//            return false;
//        }
//        if(!canCapture(captured)) {
//            return false;
//        }
//        this.value = captured;
        return ensureAllCapturesEqual();
    }

    private boolean ensureAllCapturesEqual() {
        IRType type = null;
        for (INode capture : captures) {
            if(type == null) {
                type = capture.get();
            }
            else {
                if(!type.typeEquals(capture.get())) {
                    return false;
                }
            }
        }
        return true;
    }

    private void ensureValidCapture(IRType captured) {
        if(CAPTURE_CLASS_BLACKLIST.contains(captured.getClass())) {
            throw new RuntimeException(captured + " can not be used for capture");
        }
    }

    private INode firstCapture() {
        return captures.iterator().next();
    }

    private boolean hasCapture() {
        return !captures.isEmpty();
    }

    @Override
    public IRType getMax() {
        if(hasCapture()) {
            return firstCapture().getMax();
        }
        if(maximums.isEmpty()) {
            return ObjectClass.getInstance();
        }
        return TypeIntersection.of(NncUtils.map(maximums, INode::getMax));
    }

    @Override
    public IRType getMin() {
        if(hasCapture()) {
            return firstCapture().getMin();
        }
        if(minimums.isEmpty()) {
            return IRAnyType.getInstance();
        }
        return TypeUnion.of(NncUtils.map(minimums, INode::getMin));
    }

    public Set<INode> getMinimums() {
        return minimums;
    }

    public Set<INode> getMaximums() {
        return maximums;
    }

    @Override
    public boolean ge(INode node) {
        if(hasCapture()) {
            return NncUtils.allMatch(captures, c -> c.ge(node));
        }
        if(minimums.contains(node)) {
            return true;
        }
        minimums.add(node);
        if(isSolvable()) {
            return onChange();
        }
        else {
            return false;
        }
    }

    @Override
    public boolean le(INode node) {
        if(!captures.isEmpty()) {
            return NncUtils.allMatch(captures, c -> c.le(node));
        }
        if(maximums.contains(node)) {
            return true;
        }
        maximums.add(node);
        if(isSolvable()) {
            return onChange();
        }
        else {
            return false;
        }
    }

    private boolean isSolvable() {
        return getMax().isAssignableFrom(getMin());
    }

    @Override
    public IRType solve() {
        if(hasCapture()) {
            return firstCapture().solve();
        }
        return solution = getMin();
    }

    @Override
    public IRType get() {
        if(solution != null) {
            return solution;
        }
        if(hasCapture()) {
            return firstCapture().get();
        }
        return new SolvingType(getType().getName(), getMin(), getMax());
    }

}
