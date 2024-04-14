package tech.metavm.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DebugEnv {

    public static volatile boolean debugging = false;

    public static volatile boolean buildPatchLog = false;

    public static boolean gettingBufferedTrees;

    public static final Logger logger = LoggerFactory.getLogger("Debug");

}
