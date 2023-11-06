package tech.metavm.object.instance;

import tech.metavm.flow.*;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.meta.ClassType;

import java.util.List;

public class LambdaFrame extends MetaFrame {

    private final MetaFrame containingFrame;

    public LambdaFrame(NodeRT<?> entry, ClassType declaringType,
                       List<Instance> arguments, FlowStack stack, MetaFrame containingFrame) {
        super(entry, declaringType, containingFrame.getSelf(), arguments, stack);
        this.containingFrame = containingFrame;
    }

    @Override
    public Instance getResult(NodeRT<?> node) {
        var result = super.getResult(node);
        if(result != null) {
            return result;
        }
        return containingFrame.getResult(node);
    }
}
