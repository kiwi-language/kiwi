package org.metavm.system.rest;

import org.metavm.application.ApplicationManager;
import org.metavm.common.Result;
import org.metavm.entity.Bootstrap;
import org.metavm.task.Scheduler;
import org.metavm.task.TaskManager;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bootstrap")
public class BootstrapController {

    private final Bootstrap bootstrap;
    private final Scheduler scheduler;
    private final TaskManager jobManager;
    private final ApplicationManager applicationManager;

    public BootstrapController(Bootstrap bootstrap,
                               Scheduler scheduler,
                               TaskManager jobManager,
                               ApplicationManager applicationManager) {
        this.bootstrap = bootstrap;
        this.scheduler = scheduler;
        this.jobManager = jobManager;
        this.applicationManager = applicationManager;
    }

    @PostMapping
    public Result<Void> boot(@RequestParam(value = "saveIds", defaultValue = "true") boolean saveIds) {
        initSystemEntities();
        initBuiltinApplications();
        return Result.success(null);
    }

//    @PostMapping("/save")
//    public Result<Void> save(@RequestParam(value = "saveIds", defaultValue = "true") boolean saveIds) {
//        bootstrap.save(saveIds);
//        return Result.success(null);
//    }

    @PostMapping("/init-system-entities")
    public Result<Void> initSystemEntities() {
        bootstrap.initSystemEntities();
        return Result.success(null);
    }

    @PostMapping("/rebuild-index")
    public Result<Void> rebuildIndex()  {
        jobManager.addIndexRebuildGlobalTask();
        return Result.voidSuccess();
    }

    @PostMapping("/init-builtin-applications")
    public Result<Void> initBuiltinApplications() {
        applicationManager.createRoot();
        applicationManager.createPlatform();
        return Result.voidSuccess();
    }

}
