package tech.metavm.flow.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tech.metavm.common.ErrorCode;
import tech.metavm.common.Page;
import tech.metavm.common.Result;
import tech.metavm.flow.FlowExecutionService;
import tech.metavm.flow.FlowManager;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.type.rest.dto.MovePropertyRequest;
import tech.metavm.util.FlowExecutionException;

import java.util.List;

@RestController
@RequestMapping("/flow")
public class FlowController {

    @Autowired
    private FlowManager flowManager;

    @Autowired
    private FlowExecutionService flowExecutionService;

    @PostMapping("/get")
    public Result<GetFlowResponse> get(@RequestBody GetFlowRequest request) {
        return Result.success(flowManager.get(request));
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

    @PostMapping("/{id:[0-9]+}/check")
    public Result<GetFlowResponse> check(@PathVariable long id) {
        return Result.success(flowManager.check(id));
    }

    @PostMapping
    public Result<Long> save(@RequestBody FlowDTO flow) {
        return Result.success(flowManager.save(flow).getIdRequired());
    }

    @DeleteMapping("/{id:[0-9]+}")
    public Result<Void> delete(@PathVariable("id") long id) {
        flowManager.remove(id);
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

    @PostMapping("/move")
    public Result<Void> move(@RequestBody MovePropertyRequest request) {
        flowManager.moveMethod(request.id(), request.ordinal());
        return Result.voidSuccess();
    }

    @PostMapping("/try-node")
    public Result<List<NodeDTO>> createGuardNode(@RequestBody NodeDTO node) {
        return Result.success(flowManager.createTryNode(node));
    }

    @PostMapping("/branch-node")
    public Result<List<NodeDTO>> createBranchNode(@RequestBody NodeDTO node) {
        return Result.success(flowManager.createBranchNode(node));
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

    @PostMapping("/node/branch")
    public Result<BranchDTO> saveBranch(@RequestBody BranchDTO branchDTO) {
        if(branchDTO.index() == null || branchDTO.index() == 0L) {
            return Result.success(flowManager.createBranch(branchDTO));
        }
        else {
            return Result.success(flowManager.updateBranch(branchDTO));
        }
    }

    @DeleteMapping("/node/branch/{ownerId:[0-9]+}/{id:[0-9]+}")
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
