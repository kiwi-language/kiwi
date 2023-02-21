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

    @GetMapping
    public Result<Page<InstanceDTO>> list(@RequestParam("typeId") long typeId,
                                          @RequestParam(value = "searchText", required = false) String searchText,
                                          @RequestParam(value = "page", defaultValue = "1") int page,
                                          @RequestParam(value = "pageSize", defaultValue = "20") int pageSize) {
        return Result.success(instanceManager.query(new InstanceQueryDTO(typeId, searchText, page, pageSize)));
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
    public Result<Long> upsert(@RequestBody InstanceDTO instance) {
        if(instance.id() == null || instance.id() == 0L) {
            return Result.success(instanceManager.create(instance, false));
        }
        else {
            instanceManager.update(instance, false);
            return Result.success(instance.id());
        }
    }

    @GetMapping("/{id:[0-9]+}")
    public Result<InstanceDTO> get(@PathVariable("id") long id,
                                   @RequestParam(value = "depth", defaultValue = "1") int depth) {
        return Result.success(instanceManager.get(id, depth));
    }

    @PostMapping("/batch-get")
    public Result<List<InstanceDTO>> batchGet(@RequestBody BatchGetInstancesRequest request) {
        return Result.success(instanceManager.batchGet(request.ids(), request.depth()));
    }

    @DeleteMapping("/{id:[0-9]+}")
    public Result<Void> delete(@PathVariable("id") long id) {
        instanceManager.delete(id, false);
        return Result.success(null);
    }

    @PostMapping("/load-by-paths")
    public Result<List<InstanceDTO>> loadByPaths(@RequestBody LoadInstancesByPathsRequest request) {
        return Result.success(instanceManager.loadByPaths(request));
    }

}
