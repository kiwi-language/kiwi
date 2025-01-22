package org.metavm.autograph;

import org.metavm.entity.Bootstraps;
import org.metavm.object.type.AllocatorStore;
import org.metavm.object.type.ColumnStore;
import org.metavm.object.type.StdAllocators;
import org.metavm.object.type.TypeTagStore;
import org.metavm.util.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class CompilerBootstrap {

    public static final Logger logger = LoggerFactory.getLogger(CompilerBootstrap.class);
    private final StdAllocators stdAllocators;
    private final ColumnStore columnStore;
    private final TypeTagStore typeTagStore;
    private volatile boolean boot;

    CompilerBootstrap(AllocatorStore allocatorStore, ColumnStore columnStore, TypeTagStore typeTagStore) {
        stdAllocators = new StdAllocators(allocatorStore);
        this.columnStore = columnStore;
        this.typeTagStore = typeTagStore;
    }

    public synchronized void boot() {
        if(boot)
            throw new IllegalStateException("Already boot");
        try(var ignored = ContextUtil.getProfiler().enter("CompilerBootstrap.boot")) {
            boot = true;
            Bootstraps.boot(
                    stdAllocators,
                    columnStore,
                    typeTagStore,
                    Set.of(),
                    Set.of(),
                    false
            );
            ContextUtil.resetLoginInfo();
        }
    }

}
