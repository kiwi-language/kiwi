package tech.metavm.user.rest.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.metavm.dto.Page;
import tech.metavm.dto.Result;
import tech.metavm.user.UserManager;
import tech.metavm.user.rest.dto.UserDTO;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserManager userManager;

    @GetMapping
    public Result<Page<UserDTO>> list(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "searchText", required = false) String searchText
    ) {
        return Result.success(userManager.list(page, pageSize, searchText));
    }

    @GetMapping("/{id:[0-9]+}")
    public Result<UserDTO> get(@PathVariable("id") long id) {
        return Result.success(userManager.get(id));
    }

    @PostMapping
    public Result<Long> save(@RequestBody UserDTO userDTO) {
        return Result.success(userManager.save(userDTO));
    }

    @DeleteMapping("/{id:[0-9]+}")
    public Result<Void> delete(@PathVariable("id") long id) {
        userManager.delete(id);
        return Result.voidSuccess();
    }

}
