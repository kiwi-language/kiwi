package tech.metavm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.flow.MethodCallNode;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.PhysicalId;

import java.util.List;

public class DebugEnv {

    public static volatile boolean debugging = false;

    public static volatile boolean removeCheckVerbose = false;

    public static volatile boolean recordPath = false;

    public static volatile boolean bootstrapVerbose = false;

    public static volatile boolean buildPatchLog = false;

    public static volatile boolean resolveVerbose = false;

    public static volatile boolean printMapping = false;

    public static volatile boolean flag = false;

    public static volatile boolean saveCompileResult = false;

    public static volatile DurableInstance instance;

    public static boolean gettingBufferedTrees;

    public static final Logger logger = LoggerFactory.getLogger("Debug");

    public static volatile MethodCallNode target;

    public static volatile boolean flag2;

    public static volatile boolean flag3;

    public static volatile PhysicalId id;

    public static final LinkedList<String> path = new LinkedList<>();

    public static void enterPathItem(String pathItem) {
        path.addLast(pathItem);
    }

    public static void exitPathItem() {
        path.removeLast();
    }

    public static boolean checkPath(String pathStr) {
        return path.equals(List.of(pathStr.split("\\.")));
    }

}
