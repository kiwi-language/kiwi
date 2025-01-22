package org.metavm.autograph;

import org.metavm.object.type.AllocatorStore;
import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.TypeTagStore;
import org.metavm.util.Utils;

public class CompilerContext {

    private final CompilerBootstrap bootstrap;

    public CompilerContext(String home, AllocatorStore allocatorStore, ColumnStore columnStore, TypeTagStore typeTagStore) {
        Utils.ensureDirectoryExists(home);
        bootstrap = new CompilerBootstrap(allocatorStore, columnStore, typeTagStore);
    }

    public CompilerBootstrap getBootstrap() {
        return bootstrap;
    }

}
