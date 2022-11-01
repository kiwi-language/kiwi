package tech.metavm.tenant.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.metavm.dto.Page;
import tech.metavm.dto.Result;
import tech.metavm.tenant.TenantManager;
import tech.metavm.tenant.rest.dto.TenantCreateRequest;
import tech.metavm.tenant.rest.dto.TenantDTO;

@RestController
@RequestMapping("/tenant")
public class TenantController {

    @Autowired
    private TenantManager tenantManager;

    @GetMapping
    public Result<Page<TenantDTO>> list(@RequestParam(value = "page", defaultValue = "1") int page,
                                        @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                        @RequestParam(value = "searchText", required = false) String searchText) {
        return Result.success(tenantManager.list(page, pageSize, searchText));
    }

    @PostMapping("/create")
    public Result<Long> create(@RequestBody TenantCreateRequest request) {
        return Result.success(tenantManager.create(request));
    }

    @GetMapping("/{id:[0-9]+}")
    public Result<TenantDTO> get(@PathVariable("id") long id) {
        return Result.success(tenantManager.get(id));
    }

    @PostMapping
    public Result<Void> update(@RequestBody TenantDTO tenantDTO) {
        tenantManager.update(tenantDTO);
        return Result.voidSuccess();
    }

    @DeleteMapping("/{id:[0-9]+}")
    public Result<Void> delete(@PathVariable("id") long id) {
        tenantManager.delete(id);
        return Result.voidSuccess();
    }

}
