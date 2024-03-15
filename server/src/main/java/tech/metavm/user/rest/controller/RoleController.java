package tech.metavm.user.rest.controller;

import org.springframework.web.bind.annotation.*;
import tech.metavm.common.Page;
import tech.metavm.common.Result;
import tech.metavm.user.RoleManager;
import tech.metavm.user.rest.dto.RoleDTO;

@RestController
@RequestMapping("/role")
public class RoleController {

    private final RoleManager roleManager;

    public RoleController(RoleManager roleManager) {
        this.roleManager = roleManager;
    }

    @PostMapping
    public Result<String> save(@RequestBody RoleDTO roleDTO) {
        return Result.success(roleManager.save(roleDTO));
    }

    @GetMapping("/{id}")
    public Result<RoleDTO> get(@PathVariable("id") String id) {
        return Result.success(roleManager.get(id));
    }

    @GetMapping
    public Result<Page<RoleDTO>> list(@RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                      @RequestParam(value = "searchText", required = false) String searchText) {
        return Result.success(roleManager.list(page, pageSize, searchText));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") String id) {
        roleManager.delete(id);
        return Result.voidSuccess();
    }

}
