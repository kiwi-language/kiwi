package tech.metavm.user.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.metavm.dto.Page;
import tech.metavm.dto.Result;
import tech.metavm.user.RoleManager;
import tech.metavm.user.rest.dto.RoleDTO;

@RestController
@RequestMapping("/role")
public class RoleController {

    @Autowired
    private RoleManager roleManager;

    @PostMapping
    public Result<Long> save(@RequestBody RoleDTO roleDTO) {
        return Result.success(roleManager.save(roleDTO));
    }

    @GetMapping("/{id}")
    public Result<RoleDTO> get(@PathVariable("id") long id) {
        return Result.success(roleManager.get(id));
    }

    @GetMapping
    public Result<Page<RoleDTO>> list(@RequestParam(value = "page", defaultValue = "1") int page,
                                      @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
                                      @RequestParam(value = "searchText", required = false) String searchText) {
        return Result.success(roleManager.list(page, pageSize, searchText));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") long id) {
        roleManager.delete(id);
        return Result.voidSuccess();
    }

}
