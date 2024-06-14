package org.metavm.view.rest;

import org.springframework.web.bind.annotation.*;
import org.metavm.common.Result;
import org.metavm.object.view.rest.dto.ObjectMappingDTO;
import org.metavm.view.ViewManager;
import org.metavm.view.rest.dto.ListViewDTO;

@RestController
@RequestMapping("/view")
public class ViewController {

    private final ViewManager viewManager;

    public ViewController(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    @GetMapping("/get-list-view-type-id")
    public Result<String> getListViewTypeId() {
        return Result.success(viewManager.getListViewTypeId());
    }

    @GetMapping("/get-default-list-view")
    public Result<ListViewDTO> getListView(@RequestParam("typeId") String typeId) {
        return Result.success(viewManager.getDefaultListView(typeId));
    }

    @PostMapping("/mapping")
    public Result<String> saveViewMapping(@RequestBody ObjectMappingDTO viewMapping) {
        return Result.success(viewManager.saveMapping(viewMapping));
    }

    @DeleteMapping("/mapping/{id}")
    public Result<Void> removeViewMapping(@PathVariable("id") String id) {
        viewManager.removeMapping(id);
        return Result.voidSuccess();
    }

    @PostMapping("/mapping/{id}/set-default")
    public Result<Void> saveViewMapping(@PathVariable("id") String id) {
        viewManager.setDefaultMapping(id);
        return Result.voidSuccess();
    }

}
