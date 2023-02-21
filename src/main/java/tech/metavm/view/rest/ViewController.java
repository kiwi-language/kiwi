package tech.metavm.view.rest;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.metavm.dto.Result;
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

}
