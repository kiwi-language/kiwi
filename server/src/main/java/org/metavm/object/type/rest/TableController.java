package org.metavm.object.type.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.metavm.common.Page;
import org.metavm.common.Result;
import org.metavm.object.type.TableManager;
import org.metavm.object.type.rest.dto.ColumnDTO;
import org.metavm.object.type.rest.dto.TableDTO;

@RestController
@RequestMapping("/table")
public class TableController {

    @Autowired
    private TableManager tableManager;

    @GetMapping
    public Result<Page<TableDTO>> list(
            @RequestParam(value = "searchText", required = false) String searchText,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize
    ) {
        return Result.success(tableManager.list(searchText, page, pageSize));
    }

    @GetMapping("/{id}")
    public Result<TableDTO> get(@PathVariable("id") String id) {
        return Result.success(tableManager.get(id));
    }

    @PostMapping
    public Result<TableDTO> save(@RequestBody TableDTO type) {
        return Result.success(tableManager.save(type));
    }

    @GetMapping("/column/{id}")
    public Result<ColumnDTO> getColumn(@PathVariable("id") String id) {
        return Result.success(tableManager.getColumn(id));
    }

    @PostMapping("/column")
    public Result<String> saveColumn(@RequestBody ColumnDTO field) {
        return Result.success(tableManager.saveColumn(field));
    }

}
