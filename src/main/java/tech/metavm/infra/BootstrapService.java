package tech.metavm.infra;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.object.meta.StdTypeManager;

@Component
public class BootstrapService {

    private final RegionManager regionManager;
    private final StdIdBockManager stdIdBockManager;
    private final StdTypeManager stdTypeManager;

    public BootstrapService(RegionManager regionManager, StdIdBockManager stdIdBockManager, StdTypeManager stdTypeManager) {
        this.regionManager = regionManager;
        this.stdIdBockManager = stdIdBockManager;
        this.stdTypeManager = stdTypeManager;
    }

    @Transactional
    public void boot() {
        regionManager.initialize();
        stdIdBockManager.initialize();
        stdTypeManager.initialize();
    }

}
