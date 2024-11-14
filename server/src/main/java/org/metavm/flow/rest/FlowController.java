package org.metavm.flow.rest;

import org.metavm.common.ErrorCode;
import org.metavm.common.Page;
import org.metavm.common.Result;
import org.metavm.flow.FlowExecutionService;
import org.metavm.flow.FlowManager;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.util.FlowExecutionException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/flow")
public class FlowController {

    private final FlowManager flowManager;

    private final FlowExecutionService flowExecutionService;

    public FlowController(FlowManager flowManager, FlowExecutionService flowExecutionService) {
        this.flowManager = flowManager;
        this.flowExecutionService = flowExecutionService;
    }

    @PostMapping("/get")
    public Result<GetFlowResponse> get(@RequestBody GetFlowRequest request) {
        return Result.success(flowManager.get(request));
    }

    @PostMapping("/get-parameterized")
    public Result<String> getParameterized(@RequestBody GetParameterizedFlowRequest request) {
        return Result.success(flowManager.getParameterizedFlow(request).getStringId());
    }

    @GetMapping
    public Result<Page<FlowSummaryDTO>> list(
            @RequestParam("typeId") String typeId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "pageSize", defaultValue = "20") int pageSize,
            @RequestParam(value = "searchText", required = false) String searchText)
    {
        return Result.success(flowManager.list(typeId, page, pageSize, searchText));
    }

    @PostMapping("/{id}/check")
    public Result<GetFlowResponse> check(@PathVariable String id) {
        return Result.success(flowManager.check(id));
    }

    @PostMapping
    public Result<String> save(@RequestBody FlowDTO flow) {
        return Result.success(flowManager.save(flow).getStringId());
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") String id) {
        flowManager.remove(id);
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
