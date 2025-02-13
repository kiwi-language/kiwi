package org.metavm.util;

import org.metavm.entity.SystemDefContext;
import org.metavm.flow.Function;
import org.metavm.flow.Method;
import org.metavm.flow.InvokeVirtualNode;
import org.metavm.flow.MethodRef;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.IInstanceContext;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.type.Klass;
import org.metavm.object.type.Type;
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

    public static volatile boolean flag = false;

    public static final List<Klass> list = new CopyOnWriteArrayList<>();

    public static volatile AtomicInteger counter = new AtomicInteger();

    public static int count = 0;

    public static volatile boolean saveCompileResult = false;

    public static volatile String stringId;

    public static volatile Instance instance;

    public static volatile ClassInstance classInstance;

    public static boolean gettingBufferedTrees;

    public static Method method;

    public static final Logger logger = LoggerFactory.getLogger("Debug");

    public static volatile InvokeVirtualNode target;

    public static volatile boolean flag2;

    public static volatile boolean flag3;

    public static volatile Id id;

    public static volatile long treeId;

    public static final LinkedList<String> path = new LinkedList<>();

    public static volatile IInstanceContext context;

    public static volatile Id inventoryId;
    public static List<Type> types;
    public static boolean traceInstanceRemoval = false;

    public static void enterPathItem(String pathItem) {
        path.addLast(pathItem);
    }

    public static volatile Klass klass;

    public static volatile String traceKlassName = Character.class.getName();

    public static volatile Object object;

    public static volatile SystemDefContext defContext;

    public static volatile boolean saveClassFile = true;

    public static volatile MethodRef methodRef;

    public static volatile boolean dumpContextAfterFinish = false;

    public static volatile boolean dumpClassAfterContextFinish = false;

    public static volatile boolean traceDifference = false;

    public static volatile boolean traceStaticFieldTableCreation = false;

    public static volatile boolean traceInstanceIO = false;

    public static volatile boolean traceDeployment = false;

    public static volatile boolean traceContextBinding = true;

    public static volatile boolean traceCrawling = false;

    public static volatile boolean traceDDL = false;

    public static volatile boolean traceClassFileIO = false;

    public static volatile boolean traceClassDefinition = false;

    public static volatile boolean traceMemoryIndex = false;

    public static volatile boolean traceMethodResolution = true;

    public static volatile boolean traceContextActivity = false;

    public static volatile boolean traceCompilation = false;

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

}
