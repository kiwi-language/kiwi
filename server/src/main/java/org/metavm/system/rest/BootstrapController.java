package org.metavm.system.rest;

import org.metavm.application.ApplicationManager;
import org.metavm.context.http.Controller;
import org.metavm.context.http.Mapping;
import org.metavm.context.http.Post;
import org.metavm.entity.Bootstrap;
import org.metavm.task.TaskManager;

@Controller
@Mapping("/bootstrap")
public class BootstrapController {

    private final Bootstrap bootstrap;
    private final TaskManager jobManager;
    private final ApplicationManager applicationManager;

    public BootstrapController(Bootstrap bootstrap,
                               TaskManager jobManager,
                               ApplicationManager applicationManager) {
        this.bootstrap = bootstrap;
        this.jobManager = jobManager;
        this.applicationManager = applicationManager;
    }

    @Post
    public void boot() {
        initSystemEntities();
        initBuiltinApplications();
    }

//    @Post("/save")
//    public void save(@RequestParam(value = "saveIds", defaultValue = "true") boolean saveIds) {
//        bootstrap.save(saveIds);
//    }

    @Post("/init-system-entities")
    public void initSystemEntities() {
        bootstrap.initSystemEntities();
    }

    @Post("/rebuild-index")
    public void rebuildIndex()  {
        jobManager.addIndexRebuildGlobalTask();
    }

    @Post("/init-builtin-applications")
    public void initBuiltinApplications() {
        applicationManager.createRoot();
        applicationManager.createPlatform();
    }

}
