package org.manul.system.rest;

import org.manul.application.ApplicationManager;
import org.manul.context.http.Controller;
import org.manul.context.http.Mapping;
import org.manul.context.http.PathVariable;
import org.manul.context.http.Post;
import org.manul.entity.Bootstrap;
import org.manul.task.TaskManager;

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

    @Post("/reindex/{appId}")
    public void reindexPlatform(@PathVariable("appId") long appId) {
        applicationManager.reindex(appId);
    }

    @Post("/reindex-all")
    public void reindexAll() {
        applicationManager.reindexAll();
    }


}
