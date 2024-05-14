package tech.metavm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public static boolean gettingBufferedTrees;

    public static final Logger logger = LoggerFactory.getLogger("Debug");

}
