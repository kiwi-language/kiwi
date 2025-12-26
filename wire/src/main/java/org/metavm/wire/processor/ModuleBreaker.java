package org.metavm.wire.processor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;

/**
 * Bypasses Java 9+ Module encapsulation safely by detecting memory offsets.
 * Throws RuntimeException on failure rather than failing silently.
 */
public class ModuleBreaker {

    private static final Set<String> PACKAGES = Set.of(
            "com.sun.tools.javac.api",
            "com.sun.tools.javac.code",
            "com.sun.tools.javac.comp",
            "com.sun.tools.javac.file",
            "com.sun.tools.javac.main",
            "com.sun.tools.javac.model",
            "com.sun.tools.javac.parser",
            "com.sun.tools.javac.processing",
            "com.sun.tools.javac.tree",
            "com.sun.tools.javac.util",
            "com.sun.tools.javac.jvm"
    );

    public static void open() {
        try {
            Class<?> unsafeClass = Class.forName("sun.misc.Unsafe");
            Field theUnsafeField = unsafeClass.getDeclaredField("theUnsafe");
            theUnsafeField.setAccessible(true);
            Object unsafe = theUnsafeField.get(null);

            Method putBooleanVolatile = unsafeClass.getDeclaredMethod("putBooleanVolatile", Object.class, long.class, boolean.class);
            Method getBoolean = unsafeClass.getDeclaredMethod("getBoolean", Object.class, long.class);

            // 1. Safe Offset Detection
            // We use a field inside this class to toggle 'setAccessible' and detect which byte changes in memory.
            Field dummy = ModuleBreaker.class.getDeclaredField("PACKAGES");
            long overrideOffset = -1;

            // Scan standard header offsets (usually 12 or 16).
            // We do NOT write to memory here, we only read, so no SIGBUS/Crash risk.
            for (long i = 8; i < 64; i++) {
                // Force to false via public API
                dummy.setAccessible(false);
                boolean before = (boolean) getBoolean.invoke(unsafe, dummy, i);

                // Force to true via public API
                dummy.setAccessible(true);
                boolean after = (boolean) getBoolean.invoke(unsafe, dummy, i);

                if (before != after) {
                    overrideOffset = i;
                    break;
                }
            }

            if (overrideOffset == -1) {
                throw new IllegalStateException("Could not detect 'override' field offset in AccessibleObject. The memory layout might be unsupported.");
            }

            // 2. Get the jdk.compiler module
            Class<?> moduleClass = Class.forName("java.lang.Module");
            Method getModule = Class.class.getMethod("getModule");
            Object myModule = getModule.invoke(ModuleBreaker.class);

            Class<?> layerClass = Class.forName("java.lang.ModuleLayer");
            Object bootLayer = layerClass.getMethod("boot").invoke(null);
            Method findModule = layerClass.getMethod("findModule", String.class);
            Object compilerModuleOpt = findModule.invoke(bootLayer, "jdk.compiler");

            // Check if module was found
            boolean isPresent = (boolean) compilerModuleOpt.getClass().getMethod("isPresent").invoke(compilerModuleOpt);
            if (!isPresent) {
                throw new IllegalStateException("Module 'jdk.compiler' not found in boot layer.");
            }
            Object compilerModule = compilerModuleOpt.getClass().getMethod("get").invoke(compilerModuleOpt);

            // 3. Hack implAddOpens/implAddExports to be accessible
            // We use the safely detected offset to force accessibility on these internal methods.
            Method implAddExports = moduleClass.getDeclaredMethod("implAddExports", String.class, moduleClass);
            Method implAddOpens = moduleClass.getDeclaredMethod("implAddOpens", String.class, moduleClass);

            // Override the accessibility checks
            putBooleanVolatile.invoke(unsafe, implAddExports, overrideOffset, true);
            putBooleanVolatile.invoke(unsafe, implAddOpens, overrideOffset, true);

            // 4. Open packages
            for (String pkg : PACKAGES) {
                // Exports: allows compilation against public classes
                implAddExports.invoke(compilerModule, pkg, myModule);
                // Opens: allows deep reflection (required for some Javac internals)
                implAddOpens.invoke(compilerModule, pkg, myModule);
            }

        } catch (Exception e) {
            // Rethrow so the build fails visible
            throw new RuntimeException("ModuleBreaker failed to open jdk.compiler modules", e);
        }
    }

    public static void main(String[] args) {
        open();
    }

}