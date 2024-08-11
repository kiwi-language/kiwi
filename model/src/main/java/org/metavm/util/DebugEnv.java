package org.metavm.util;

import org.metavm.entity.SystemDefContext;
import org.metavm.flow.Function;
import org.metavm.flow.InputNode;
import org.metavm.flow.Method;
import org.metavm.flow.MethodCallNode;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.Klass;
import org.metavm.task.Task;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugEnv {

    public static volatile Function func;

    public static volatile boolean debugging = false;

    public static volatile boolean removeCheckVerbose = false;

    public static volatile boolean recordPath = false;

    public static volatile boolean logApiParsing = true;

    public static volatile boolean bootstrapVerbose = false;

    public static volatile boolean buildPatchLog = false;

    public static volatile boolean resolveVerbose = false;

    public static volatile boolean printMapping = false;

    public static volatile boolean flag = false;

    public static final List<Klass> list = new CopyOnWriteArrayList<>();

    public static volatile AtomicInteger counter = new AtomicInteger();

    public static volatile boolean saveCompileResult = false;

    public static volatile String stringId;

    public static volatile Instance instance;

    public static volatile ClassInstance classInstance;

    public static boolean gettingBufferedTrees;

    public static Method method;

    public static final Logger logger = LoggerFactory.getLogger("Debug");

    public static volatile MethodCallNode target;

    public static volatile boolean flag2;

    public static volatile boolean flag3;

    public static volatile PhysicalId id;

    public static volatile long treeId;

    public static final LinkedList<String> path = new LinkedList<>();

    public static final List<InputNode> nodes = new CopyOnWriteArrayList<>();

    public static volatile IInstanceContext context;

    public static volatile Id inventoryId;

    public static void enterPathItem(String pathItem) {
        path.addLast(pathItem);
    }

    public static volatile Klass klass;

    public static volatile Object object;

    public static volatile SystemDefContext defContext;

    public static void exitPathItem() {
        path.removeLast();
    }

    public static boolean checkPath(String pathStr) {
        return path.equals(List.of(pathStr.split("\\.")));
    }

    public static boolean isTarget(Instance instance) {
        return instance.toString().startsWith("MyListbuiltinView<string>-");
    }

    public static Task task;

    public static boolean isTargetKlass(Klass klass) {
        return klass.getTypeDesc().equals("ChildList<AreabuiltinView>");
    }

}
