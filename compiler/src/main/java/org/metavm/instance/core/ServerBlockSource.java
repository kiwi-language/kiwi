package org.metavm.instance.core;

import org.metavm.autograph.TypeClient;
import org.metavm.object.instance.core.TypeId;
import org.metavm.object.instance.core.TypeTag;
import org.metavm.system.BlockRT;
import org.metavm.system.BlockSource;
import org.metavm.system.rest.dto.BlockDTO;
import org.metavm.util.NncUtils;

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
                new TypeId(TypeTag.fromCode(blockDTO.typeTag()), blockDTO.typeId()),
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
