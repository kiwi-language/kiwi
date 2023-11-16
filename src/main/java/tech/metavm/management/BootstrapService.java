package tech.metavm.management;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class BootstrapService {

    private final RegionManager regionManager;

    public BootstrapService(RegionManager regionManager) {
        this.regionManager = regionManager;
    }

    @Transactional
    public void boot() {
        regionManager.initialize();
    }

}
