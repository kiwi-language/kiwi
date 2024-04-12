package tech.metavm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.Flow;
import tech.metavm.flow.InputNode;
import tech.metavm.flow.Method;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.ClassType;

import java.util.Objects;

public class DebugEnv {

    public static volatile boolean DEBUG_ON = false;

    public static final Logger LOGGER = LoggerFactory.getLogger("Debug");

    public static volatile ClassInstance target;

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
        if (!nodes.isEmpty()) {
            var inputNode = (InputNode) nodes.get(0);
            var nodeType = inputNode.getType();
            var field = nodeType.getFieldByName("iterable");
            return Objects.requireNonNull(field.getCopySource()).isRemoved();
        } else
            return false;
    }

    public static volatile Method m0;

    public static void checkBug(String position, ClassType t1) {
        var t0 = Objects.requireNonNull(t1.getTemplate());
        var m0 = NncUtils.find(t0.getMethods(), m -> Objects.equals(m.getCode(), "fromView"));
        var m1 = NncUtils.find(t1.getMethods(), m -> Objects.equals(m.getCode(), "fromView"));
        if(m0 != null && m1 != null && !m0.getRootScope().isEmpty() && !m1.getRootScope().isEmpty()) {
            DebugEnv.m0 = m0;
            var f0 = m0.getInputNode().getType().getFields().get(0);
            var f1 = m1.getInputNode().getType().getFields().get(0);
            LOGGER.info("checkBug at {}. f1.copySource == f0: {}, f0: {}, f1: {}", position, f1.getCopySource() == f0,
                    f0, f1);
        }
    }

    public static void onM0NodesChange() {
        LOGGER.info("m0 nodes changed");
    }

}
