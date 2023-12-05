package tech.metavm.system.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.common.Result;
import tech.metavm.entity.Bootstrap;
import tech.metavm.system.RegionManager;
import tech.metavm.task.IndexRebuildGlobalTask;
import tech.metavm.task.TaskManager;
import tech.metavm.task.Scheduler;
import tech.metavm.application.ApplicationManager;

@RestController
@RequestMapping("/bootstrap")
public class BootstrapController {

    private final Bootstrap bootstrap;
    private final RegionManager regionManager;
    private final Scheduler jobScheduler;
    private final TaskManager jobManager;
    private final ApplicationManager applicationManager;

    public BootstrapController(Bootstrap bootstrap,
                               RegionManager regionManager,
                               Scheduler jobScheduler,
                               TaskManager jobManager,
                               ApplicationManager applicationManager) {
        this.bootstrap = bootstrap;
        this.regionManager = regionManager;
        this.jobScheduler = jobScheduler;
        this.jobManager = jobManager;
        this.applicationManager = applicationManager;
    }

    @PostMapping
    public Result<Void> boot(@RequestParam(value = "saveIds", defaultValue = "true") boolean saveIds) {
        initRegions();
        save(saveIds);
        initScheduler();
        initBuiltinApplications();
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

    @PostMapping("/rebuild-index")
    public Result<Void> rebuildIndex()  {
        jobManager.addGlobalTask(new IndexRebuildGlobalTask());
        return Result.voidSuccess();
    }

    @PostMapping("/init-builtin-applications")
    public Result<Void> initBuiltinApplications() {
        applicationManager.createRoot();
        applicationManager.createPlatform();
        return Result.voidSuccess();
    }

}
