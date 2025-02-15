package org.metavm.object.instance.rest;

import org.metavm.common.Page;
import org.metavm.common.Result;
import org.metavm.object.instance.InstanceManager;
import org.metavm.object.instance.core.TreeVersion;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instance")
public class InstanceController {

    private final InstanceManager instanceManager;

    public InstanceController(InstanceManager instanceManager) {
        this.instanceManager = instanceManager;
    }

    @PostMapping("/query")
    public Result<QueryInstancesResponse> query(@RequestBody InstanceQueryDTO query) {
        return Result.success(instanceManager.query(query));
    }

    @PostMapping("/select")
    public Result<Page<InstanceDTO[]>> select(@RequestBody SelectRequest request) {
        return Result.success(instanceManager.select(request));
    }

    @PostMapping("/trees")
    public Result<List<TreeDTO>> getTrees(@RequestBody GetTreesRequest request) {
        return Result.success(instanceManager.getTrees(request.ids()));
    }

    @GetMapping("/ping")
    public Result<Boolean> ping() {
        return Result.success(true);
    }

    @PostMapping("/versions")
    public Result<List<TreeVersion>> getVersions(@RequestBody InstanceVersionsRequest request) {
        return Result.success(instanceManager.getVersions(request.ids()));
    }

    @PutMapping
    public Result<String> create(@RequestBody InstanceDTO instance) {
        return Result.success(instanceManager.create(instance));
    }

    @PostMapping
    public Result<String> save(@RequestBody InstanceDTO instance) {
        if (instance.isNew() ) {
            return Result.success(instanceManager.create(instance));
        } else {
            instanceManager.update(instance);
            return Result.success(instance.id());
        }
    }

    @GetMapping("/{id}")
    public Result<GetInstanceResponse> get(
            @PathVariable("id") String id,
            @RequestParam(value = "depth", defaultValue = "1") int depth
    ) {
        return Result.success(instanceManager.get(id, depth));
    }


    @PostMapping("/batch-get")
    public Result<GetInstancesResponse> batchGet(@RequestBody GetInstancesRequest request) {
        return Result.success(instanceManager.batchGet(request.getIds(), request.getDepth()));
    }

    @PostMapping("/load-by-paths")
    public Result<List<InstanceDTO>> loadByPaths(@RequestBody LoadInstancesByPathsRequest request) {
        return Result.success(instanceManager.loadByPaths(request));
    }

}
