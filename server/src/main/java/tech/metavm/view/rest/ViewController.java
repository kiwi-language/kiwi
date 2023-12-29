package tech.metavm.view.rest;

import org.springframework.web.bind.annotation.*;
import tech.metavm.common.Result;
import tech.metavm.object.view.rest.dto.ObjectMappingDTO;
import tech.metavm.view.ViewManager;
import tech.metavm.view.rest.dto.ListViewDTO;

@RestController
@RequestMapping("/view")
public class ViewController {

    private final ViewManager viewManager;

    public ViewController(ViewManager viewManager) {
        this.viewManager = viewManager;
    }

    @GetMapping("/get-list-view-type-id")
    public Result<Long> getListViewTypeId() {
        return Result.success(viewManager.getListViewTypeId());
    }

    @GetMapping("/get-default-list-view")
    public Result<ListViewDTO> getListView(@RequestParam("typeId") long typeId) {
        return Result.success(viewManager.getDefaultListView(typeId));
    }

    @PostMapping("/mapping")
    public Result<Long> saveViewMapping(@RequestBody ObjectMappingDTO viewMapping) {
        return Result.success(viewManager.saveMapping(viewMapping));
    }

    @DeleteMapping("/mapping/{id:[0-9]+}")
    public Result<Void> removeViewMapping(@PathVariable("id") long id) {
        viewManager.removeMapping(id);
        return Result.voidSuccess();
    }

    @PostMapping("/mapping/{id:[0-9]+}/set-default")
    public Result<Void> saveViewMapping(@PathVariable("id") long id) {
        viewManager.setDefaultMapping(id);
        return Result.voidSuccess();
    }

}
