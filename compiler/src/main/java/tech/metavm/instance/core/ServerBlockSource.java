package tech.metavm.instance.core;

import tech.metavm.system.BlockRT;
import tech.metavm.system.BlockSource;
import tech.metavm.system.rest.dto.BlockDTO;
import tech.metavm.system.rest.dto.GetActiveBlocksRequest;
import tech.metavm.util.HttpUtils;
import tech.metavm.util.NncUtils;
import tech.metavm.util.TypeReference;

import java.util.Collection;
import java.util.List;

public class ServerBlockSource implements BlockSource {

    @Override
    public BlockRT getContainingBlock(long id) {
        var blockDTO = HttpUtils.get("/block/containing/" + id, new TypeReference<BlockDTO>() {});
        return fromDTO(blockDTO);
    }

    private BlockRT fromDTO(BlockDTO blockDTO) {
        return new BlockRT(
                blockDTO.id(),
                blockDTO.appId(),
                blockDTO.typeId(),
                blockDTO.start(),
                blockDTO.end(),
                blockDTO.next()
        );
    }

    @Override
    public List<BlockRT> getActive(List<Long> typeIds) {
        var blockDTOs = HttpUtils.post("/block/active",
                new GetActiveBlocksRequest(typeIds), new TypeReference<List<BlockDTO>>() {});
        return NncUtils.map(blockDTOs, this::fromDTO);
    }

    @Override
    public void create(Collection<BlockRT> blocks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(Collection<BlockRT> blocks) {
        throw new UnsupportedOperationException();
    }
}
