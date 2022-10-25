package tech.metavm.flow.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.metavm.dto.ErrorCode;
import tech.metavm.dto.Page;
import tech.metavm.dto.Result;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.FlowExecutionException;

@RestController
@RequestMapping("/flow")
public class FlowController {

    @Autowired
    private FlowManager flowManager;

    @Autowired
    private FlowExecutionService flowExecutionService;

    @GetMapping("/{id:[0-9]+}")
    public Result<FlowDTO> get(@PathVariable("id") long id) {
        return Result.success(flowManager.get(id));
    }

    @GetMapping
    public Result<Page<FlowSummaryDTO>> list(
            @RequestParam("typeId") long typeId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "searchText", required = false) String searchText)
    {
        return Result.success(flowManager.list(typeId, page, pageSize, searchText));
    }

    @PostMapping
    public Result<Long> save(@RequestBody FlowDTO flow) {
        if(flow.id() == null || flow.id() == 0L) {
            return Result.success(flowManager.create(flow));
        }
        else {
            flowManager.update(flow);
            return Result.success(flow.id());
        }
    }

    @DeleteMapping("{id:[0-9]+}")
    public Result<Void> delete(@PathVariable("id") long id) {
        flowManager.delete(id);
        return Result.success(null);
    }

    @PostMapping("/node")
    public Result<NodeDTO> saveNode(@RequestBody NodeDTO node) {
        if(node.id() == null || node.id() == 0L) {
            return Result.success(flowManager.createNode(node));
        }
        else {
            return Result.success(flowManager.updateNode(node));
        }
    }

    @GetMapping("/node/{id:[0-9]+}")
    public Result<NodeDTO> getNode(@PathVariable("id") long nodeId) {
        return Result.success(flowManager.getNode(nodeId));
    }

    @DeleteMapping("/node/{id:[0-9]+}")
    public Result<Void> deleteNode(@PathVariable("id") long nodeId) {
        flowManager.deleteNode(nodeId);
        return Result.success(null);
    }

    @PostMapping("/node/{ownerId:[0-9]+}/branch")
    public Result<BranchDTO> saveBranch(@PathVariable("ownerId") long ownerId, @RequestBody BranchDTO branchDTO) {
        if(branchDTO.id() == null || branchDTO.id() == 0L) {
            return Result.success(flowManager.createBranch(ownerId, branchDTO));
        }
        else {
            return Result.success(flowManager.updateBranch(ownerId, branchDTO));
        }
    }

    @DeleteMapping("/node/{ownerId:[0-9]+}/branch/{id:[0-9]+}")
    public Result<Void> deleteBranch(@PathVariable("ownerId") long ownerId,
                                     @PathVariable("id") long id) {
        flowManager.deleteBranch(ownerId, id);
        return Result.success(null);
    }

    @PostMapping("/execute")
    public Result<InstanceDTO> execute(@RequestBody FlowExecutionRequest request) {
        try {
            return Result.success(flowExecutionService.execute(request));
        }
        catch (FlowExecutionException e) {
            return Result.failure(ErrorCode.FLOW_EXECUTION_FAILURE, new Object[] {e.getMessage()});
        }
    }

}
