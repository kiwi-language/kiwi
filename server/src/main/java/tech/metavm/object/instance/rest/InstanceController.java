package tech.metavm.object.instance.rest;

import org.springframework.web.bind.annotation.*;
import tech.metavm.common.Page;
import tech.metavm.common.Result;
import tech.metavm.object.instance.InstanceManager;

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

    @PostMapping("/versions")
    public Result<List<InstanceVersionDTO>> getVersions(@RequestBody InstanceVersionsRequest request) {
        return Result.success(instanceManager.getVersions(request.ids()));
    }

    @PutMapping
    public Result<String> create(@RequestBody InstanceDTO instance) {
        return Result.success(instanceManager.create(instance));
    }

    @GetMapping("/default-view/{id}")
    public Result<GetInstanceResponse> getDefaultView(@PathVariable("id") String id) {
        return Result.success(instanceManager.getDefaultView(id));
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

    @PostMapping("/delete-by-types")
    public Result<Void> deleteByTypes(@RequestBody List<String> typeIds) {
        instanceManager.deleteByTypes(typeIds);
        return Result.voidSuccess();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") String id) {
        instanceManager.delete(id);
        return Result.success(null);
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<String> ids) {
        instanceManager.batchDelete(ids);
        return Result.success(null);
    }

    @PostMapping("/load-by-paths")
    public Result<List<InstanceDTO>> loadByPaths(@RequestBody LoadInstancesByPathsRequest request) {
        return Result.success(instanceManager.loadByPaths(request));
    }

    @GetMapping("/reference-chain/{id}")
    public Result<List<String>> getReferenceChain(@PathVariable("id") String id,
                                                  @RequestParam(defaultValue = "1") int rootMode) {
        return Result.success(instanceManager.getReferenceChain(id, rootMode));
    }

}
