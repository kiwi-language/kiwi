package tech.metavm.util;

import tech.metavm.flow.Flow;
import tech.metavm.flow.InputNode;

import java.util.Objects;

public class DebugEnv {

    public static volatile boolean DEBUG_LOG_ON = false;

    public static volatile Flow target;

    public static volatile InputNode inputNode;

    public static boolean isCopySourceRemoved(InputNode inputNode) {
        var typeCopySource = Objects.requireNonNull(inputNode.getType().getCopySource());
        var copySource = (InputNode) Objects.requireNonNull(typeCopySource.getParentEntity());
        return !copySource.getScope().getNodes().contains(copySource);
    }

    public static boolean isTargetFlow(Flow flow) {
        return flow.getEffectiveHorizontalTemplate().getName().equals("find") && flow.getTypeArguments().get(0).isCaptured();
    }

    public static boolean isBugPresent(Flow flow) {
        var nodes = flow.getRootScope().getNodes();
        if(!nodes.isEmpty()) {
            var inputNode = (InputNode) nodes.get(0);
            var nodeType = inputNode.getType();
            var field = nodeType.getFieldByName("iterable");
            return Objects.requireNonNull(field.getCopySource()).isRemoved();
        }
        else
            return false;
    }

}
