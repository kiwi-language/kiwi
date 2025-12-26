package org.metavm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class DebugEnv {


    public static volatile boolean debugging = false;

    public static volatile boolean removeCheckVerbose = false;

    public static volatile boolean recordPath = false;

    public static volatile boolean logApiParsing = true;

    public static volatile boolean bootstrapVerbose = false;

    public static volatile boolean buildPatchLog = false;

    public static volatile boolean resolveVerbose = false;

    public static final boolean traceContextFinish = false;

    public static boolean traceInstanceLoading = false;

    public static boolean traceIndexBuild = false;

    public static volatile boolean flag = false;

    public static volatile AtomicInteger counter = new AtomicInteger();

    public static int count = 0;

    public static volatile boolean saveCompileResult = false;

    public static volatile String stringId;

    public static boolean gettingBufferedTrees;


    public static final Logger logger = LoggerFactory.getLogger("Debug");

    public static volatile boolean flag2;

    public static volatile boolean flag3;


    public static volatile long treeId;

    public static final LinkedList<String> path = new LinkedList<>();


    public static boolean traceInstanceRemoval = false;

    public static void enterPathItem(String pathItem) {
        path.addLast(pathItem);
    }


    public static volatile String traceKlassName = Character.class.getName();

    public static volatile Object object;


    public static volatile boolean saveClassFile = true;


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

    public static volatile boolean traceMigration = false;

    public static volatile boolean traceTaskExecution = false;

    public static final boolean dumpMetaContext = false;

    public static void exitPathItem() {
        path.removeLast();
    }

    public static boolean checkPath(String pathStr) {
        return path.equals(List.of(pathStr.split("\\.")));
    }

    public static String getEntityChain(String keyword) {
        keyword = "." + keyword;
        var e = new Exception();
        var sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        var s = sw.toString();
        var lines = s.split("\n");
        var entities = new java.util.LinkedList<String>();
        for (String line : lines) {
            var endIdx = line.indexOf(keyword);
            if (endIdx != -1) {
                var beginIdx = line.substring(0, endIdx).lastIndexOf('.') + 1;
                entities.addFirst(line.substring(beginIdx, endIdx));
            }
        }
        return String.join(" -> ", entities);
    }

}
