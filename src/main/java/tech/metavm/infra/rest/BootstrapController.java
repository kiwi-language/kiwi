package tech.metavm.infra.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.dto.Result;
import tech.metavm.entity.Bootstrap;
import tech.metavm.infra.RegionManager;

@RestController
@RequestMapping("/bootstrap")
public class BootstrapController {

    private final Bootstrap bootstrap;

    private final RegionManager regionManager;

    public BootstrapController(Bootstrap bootstrap, RegionManager regionManager) {
        this.bootstrap = bootstrap;
        this.regionManager = regionManager;
    }

    @PostMapping("/save")
    public Result<Void> save() {
        bootstrap.save();
        return Result.success(null);
    }

    @PostMapping("/region")
    public Result<Void> initRegions() {
        regionManager.initialize();
        return Result.success(null);
    }

}
