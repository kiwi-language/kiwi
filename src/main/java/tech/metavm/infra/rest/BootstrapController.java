package tech.metavm.infra.rest;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.dto.Result;
import tech.metavm.entity.Bootstrap;
import tech.metavm.infra.RegionManager;
import tech.metavm.job.IndexRebuildGlobalJob;
import tech.metavm.job.JobManager;
import tech.metavm.job.JobScheduler;
import tech.metavm.tenant.TenantManager;
import tech.metavm.tenant.rest.dto.TenantCreateRequest;
import tech.metavm.util.Constants;

@RestController
@RequestMapping("/bootstrap")
public class BootstrapController {

    private final Bootstrap bootstrap;
    private final RegionManager regionManager;
    private final JobScheduler jobScheduler;
    private final JobManager jobManager;
    private final TenantManager tenantManager;

    public BootstrapController(Bootstrap bootstrap,
                               RegionManager regionManager,
                               JobScheduler jobScheduler,
                               JobManager jobManager,
                               TenantManager tenantManager) {
        this.bootstrap = bootstrap;
        this.regionManager = regionManager;
        this.jobScheduler = jobScheduler;
        this.jobManager = jobManager;
        this.tenantManager = tenantManager;
    }

    @PostMapping
    public Result<Void> boot(@RequestParam(value = "saveIds", defaultValue = "true") boolean saveIds) {
        initRegions();
        save(saveIds);
        initScheduler();
        initRootTenant();
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
        jobManager.addGlobalJob(new IndexRebuildGlobalJob());
        return Result.voidSuccess();
    }

    @PostMapping("/init-root-tenant")
    public Result<Void> initRootTenant() {
        tenantManager.createRoot();
        return Result.voidSuccess();
    }

}
