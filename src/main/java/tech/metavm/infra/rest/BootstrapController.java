package tech.metavm.infra.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.dto.Result;
import tech.metavm.entity.Bootstrap;
import tech.metavm.infra.RegionManager;
import tech.metavm.job.JobScheduler;

@RestController
@RequestMapping("/bootstrap")
public class BootstrapController {

    private final Bootstrap bootstrap;

    private final RegionManager regionManager;

    private final JobScheduler jobScheduler;

    public BootstrapController(Bootstrap bootstrap, RegionManager regionManager, JobScheduler jobScheduler) {
        this.bootstrap = bootstrap;
        this.regionManager = regionManager;
        this.jobScheduler = jobScheduler;
    }

    @PostMapping
    public Result<Void> boot(@RequestParam(value = "saveIds", defaultValue = "true") boolean saveIds) {
        initRegions();
        save(saveIds);
        initScheduler();
        return Result.success(null);
    }

    @PostMapping("/save")
    public Result<Void> save(@RequestParam(value = "saveIds", defaultValue = "true") boolean saveIds) {
        bootstrap.save(saveIds);
        return Result.success(null);
    }

    @PostMapping("/region")
    public Result<Void> initRegions() {
        regionManager.initialize();
        return Result.success(null);
    }

    @PostMapping("/scheduler")
    public Result<Void> initScheduler() {
        jobScheduler.createSchedulerStatus();
        return Result.success(null);
    }

}
