package tech.metavm.object.instance;

import tech.metavm.flow.MetaFrame;
import tech.metavm.flow.NodeRT;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.instance.core.InstanceRepository;
import tech.metavm.object.type.Klass;
import tech.metavm.object.type.CompositeTypeFacade;

import java.util.List;

public class LambdaFrame extends MetaFrame {

    private final MetaFrame containingFrame;

    public LambdaFrame(NodeRT entry, Klass declaringType,
                       List<Instance> arguments, InstanceRepository instanceRepository, ParameterizedFlowProvider parameterizedFlowProvider,
                       CompositeTypeFacade compositeTypeFacade,
                       MetaFrame containingFrame) {
        super(entry, declaringType, containingFrame.getSelf(), arguments, instanceRepository, parameterizedFlowProvider, compositeTypeFacade);
        this.containingFrame = containingFrame;
    }

    @Override
    public Instance getOutput(NodeRT node) {
        var result = super.getOutput(node);
        if(result != null)
            return result;
        return containingFrame.getOutput(node);
    }
}
