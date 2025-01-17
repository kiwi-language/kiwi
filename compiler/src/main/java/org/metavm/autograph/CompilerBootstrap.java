package org.metavm.autograph;

import org.metavm.entity.Bootstraps;
import org.metavm.entity.SystemDefContext;
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

    private final CompilerInstanceContextFactory contextFactory;
    private final StdAllocators stdAllocators;
    private final ColumnStore columnStore;
    private final TypeTagStore typeTagStore;
    private volatile boolean boot;

    CompilerBootstrap(CompilerInstanceContextFactory contextFactory, AllocatorStore allocatorStore, ColumnStore columnStore, TypeTagStore typeTagStore) {
        this.contextFactory = contextFactory;
        stdAllocators = new StdAllocators(allocatorStore);
        this.columnStore = columnStore;
        this.typeTagStore = typeTagStore;
    }

    public synchronized void boot() {
        if(boot)
            throw new IllegalStateException("Already boot");
        try(var ignored = ContextUtil.getProfiler().enter("CompilerBootstrap.boot")) {
            boot = true;
            var bootResult = Bootstraps.boot(
                    stdAllocators,
                    columnStore,
                    typeTagStore,
                    Set.of(),
                    Set.of(),
                    false
            );
            contextFactory.setStdContext(bootResult.defContext());
            contextFactory.setDefContext((SystemDefContext) bootResult.defContext());
            ContextUtil.resetLoginInfo();
        }
    }

}
