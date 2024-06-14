package org.metavm.system.rest;

import org.springframework.web.bind.annotation.*;
import org.metavm.common.Result;
import org.metavm.system.BlockManager;
import org.metavm.system.rest.dto.BlockDTO;
import org.metavm.system.rest.dto.GetActiveBlocksRequest;

import java.util.List;

@RestController
@RequestMapping("/block")
public class BlockController {

    private final BlockManager blockManager;

    public BlockController(BlockManager blockManager) {
        this.blockManager = blockManager;
    }

    @GetMapping("/containing/{id:[0-9]+}")
    public Result<BlockDTO> getContainingBlock(@PathVariable("id") long id) {
        return Result.success(blockManager.getContaining(id));
    }

    @GetMapping("/active")
    public Result<List<BlockDTO>> getActiveBlocks(@RequestBody GetActiveBlocksRequest request) {
        return Result.success(blockManager.getActive(request.typeIds()));
    }


}
