package org.metavm;

import org.metavm.context.ApplicationContext;
import org.metavm.springconfig.KiwiConfig;

public class ObjectApplication {

    public static void main(String[] args) {
        parseArgs(args);
        ApplicationContext.start();
    }

    private static void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-config")) {
                if (i >= args.length - 1) {
                    System.err.println("Invalid options");
                    System.exit(1);
                }
                KiwiConfig.CONFIG_PATH = args[++i];
            }
        }
    }

}
