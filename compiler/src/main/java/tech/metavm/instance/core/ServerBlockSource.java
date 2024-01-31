package tech.metavm.instance.core;

import tech.metavm.autograph.TypeClient;
import tech.metavm.system.BlockRT;
import tech.metavm.system.BlockSource;
import tech.metavm.system.rest.dto.BlockDTO;
import tech.metavm.util.NncUtils;

import java.util.Collection;
import java.util.List;

public class ServerBlockSource implements BlockSource {

    private final TypeClient typeClient;

    public ServerBlockSource(TypeClient typeClient) {
        this.typeClient = typeClient;
    }

    @Override
    public BlockRT getContainingBlock(long id) {
        return fromDTO(typeClient.getContainingBlock(id));
    }

    @Override
    public List<BlockRT> getActive(List<Long> typeIds) {
        return NncUtils.map(typeClient.getActive(typeIds), this::fromDTO);
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
    public void create(Collection<BlockRT> blocks) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void update(Collection<BlockRT> blocks) {
        throw new UnsupportedOperationException();
    }
}
