package tech.metavm.autograph;

import tech.metavm.util.MetaVersionStore;
import tech.metavm.util.NncUtils;

import java.io.File;

public class CompilerContext {

    private final MetaVersionStore metaVersionStore;
    private final CompilerInstanceContextFactory contextFactory;
    private final DiskTreeStore diskTreeStore;
    private final LocalIndexSource localIndexSource;
    private final TreeLoader treeLoader;
    private final CompilerBootstrap bootstrap;

    public CompilerContext(String home) {
        NncUtils.ensureDirectoryExists(home);
        diskTreeStore = new DiskTreeStore(home + File.separator + "trees");
        localIndexSource = new LocalIndexSource(diskTreeStore, home);
        contextFactory = new CompilerInstanceContextFactory(diskTreeStore, localIndexSource);
        localIndexSource.setContextFactory(contextFactory);
        metaVersionStore = new MetaVersionStore(home + File.separator + "metaVersion");
        treeLoader = new TreeLoader(metaVersionStore, diskTreeStore, localIndexSource);
        bootstrap = new CompilerBootstrap(contextFactory);
    }

    public CompilerBootstrap getBootstrap() {
        return bootstrap;
    }

    public MetaVersionStore getMetaVersionStore() {
        return metaVersionStore;
    }

    public TreeLoader getTreeLoader() {
        return treeLoader;
    }

    public CompilerInstanceContextFactory getContextFactory() {
        return contextFactory;
    }
}
