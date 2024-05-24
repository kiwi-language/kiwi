package tech.metavm.autograph;

import tech.metavm.object.type.AllocatorStore;
import tech.metavm.object.type.ColumnStore;
import tech.metavm.object.type.TypeTagStore;
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

    public CompilerContext(String home, TypeClient typeClient, AllocatorStore allocatorStore, ColumnStore columnStore, TypeTagStore typeTagStore) {
        NncUtils.ensureDirectoryExists(home);
        diskTreeStore = new DiskTreeStore(home + File.separator + "trees");
        localIndexSource = new LocalIndexSource(typeClient.getAppId(), diskTreeStore, home);
        contextFactory = new CompilerInstanceContextFactory(diskTreeStore, localIndexSource, typeClient);
        localIndexSource.setContextFactory(contextFactory);
        metaVersionStore = new MetaVersionStore(home + File.separator + "metaVersion");
        treeLoader = new TreeLoader(metaVersionStore, diskTreeStore, localIndexSource, typeClient);
        bootstrap = new CompilerBootstrap(contextFactory, allocatorStore, columnStore, typeTagStore);
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
