package org.metavm.object.instance;

import org.metavm.flow.MetaFrame;
import org.metavm.flow.NodeRT;
import org.metavm.object.instance.core.InstanceRepository;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;

import java.util.List;

public class LambdaFrame extends MetaFrame {

    private final MetaFrame containingFrame;

    public LambdaFrame(NodeRT entry, Klass declaringType,
                       List<? extends Value> arguments, InstanceRepository instanceRepository,
                       MetaFrame containingFrame) {
        super(entry, declaringType, containingFrame.getSelf(), arguments, instanceRepository);
        this.containingFrame = containingFrame;
    }

    @Override
    public Value getOutput(NodeRT node) {
        var result = super.getOutput(node);
        if(result != null)
            return result;
        return containingFrame.getOutput(node);
    }
}
