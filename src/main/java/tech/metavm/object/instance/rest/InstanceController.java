package tech.metavm.object.instance.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.metavm.dto.Page;
import tech.metavm.dto.Result;
import tech.metavm.object.instance.InstanceManager;

import java.util.List;

@RestController
@RequestMapping("/instance")
public class InstanceController {

    @Autowired
    private InstanceManager instanceManager;

    @PostMapping("/query")
    public Result<QueryInstancesResponse> query(@RequestBody InstanceQuery query) {
        return Result.success(instanceManager.query(query));
    }

    @PostMapping("/select")
    public Result<Page<InstanceDTO[]>> select(@RequestBody SelectRequestDTO request) {
        return Result.success(instanceManager.select(request));
    }

    @PutMapping
    public Result<Long> create(@RequestBody InstanceDTO instance) {
        return Result.success(instanceManager.create(instance, false));
    }

    @PostMapping
    public Result<Long> save(@RequestBody InstanceDTO instance) {
        if (instance.id() == null || instance.id() == 0L) {
            return Result.success(instanceManager.create(instance, false));
        } else {
            instanceManager.update(instance, false);
            return Result.success(instance.id());
        }
    }

    @GetMapping("/{id:[0-9]+}")
    public Result<GetInstanceResponse> get(@PathVariable("id") long id,
                                   @RequestParam(value = "depth", defaultValue = "1") int depth) {
        return Result.success(instanceManager.get(id, depth));
    }

    @PostMapping("/batch-get")
    public Result<GetInstancesResponse> batchGet(@RequestBody GetInstancesRequest request) {
        return Result.success(instanceManager.batchGet(request.ids(), request.depth()));
    }

    @PostMapping("/delete-by-types")
    public Result<Void> deleteByTypes(@RequestBody List<Long> typeIds) {
        instanceManager.deleteByTypes(typeIds);
        return Result.voidSuccess();
    }

    @DeleteMapping("/{id:[0-9]+}")
    public Result<Void> delete(@PathVariable("id") long id) {
        instanceManager.delete(id, false);
        return Result.success(null);
    }

    @PostMapping("/batch-delete")
    public Result<Void> batchDelete(@RequestBody List<Long> ids) {
        instanceManager.batchDelete(ids, false);
        return Result.success(null);
    }

    @PostMapping("/load-by-paths")
    public Result<List<InstanceDTO>> loadByPaths(@RequestBody LoadInstancesByPathsRequest request) {
        return Result.success(instanceManager.loadByPaths(request));
    }

    @GetMapping("/reference-chain/{id:[0-9]+}")
    public Result<List<String>> getReferenceChain(@PathVariable("id") long id,
                                                  @RequestParam(defaultValue = "1") int rootMode) {
        return Result.success(instanceManager.getReferenceChain(id, rootMode));
    }

}
