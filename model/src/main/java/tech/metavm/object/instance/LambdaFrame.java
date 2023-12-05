package tech.metavm.object.instance;

import tech.metavm.flow.*;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassType;

import java.util.List;

public class LambdaFrame extends MetaFrame {

    private final MetaFrame containingFrame;

    public LambdaFrame(NodeRT<?> entry, ClassType declaringType,
                       List<Instance> arguments, IInstanceContext context, MetaFrame containingFrame) {
        super(entry, declaringType, containingFrame.getSelf(), arguments, context);
        this.containingFrame = containingFrame;
    }

    @Override
    public Instance getOutput(NodeRT<?> node) {
        var result = super.getOutput(node);
        if(result != null) {
            return result;
        }
        return containingFrame.getOutput(node);
    }
}
