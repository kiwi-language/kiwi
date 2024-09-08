package org.metavm.system.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.metavm.util.PrimaryMapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface BlockMapper extends PrimaryMapper {

    List<BlockPO> selectByIds(Collection<Long> ids);

    int batchInsert(List<BlockPO> records);

    List<BlockPO> selectActive(Collection<Long> typeIds);

    void batchUpdate(Collection<BlockPO> records);

    BlockPO selectContaining(@Param("point") long point);

    void increaseNextId(
            @Param("id") long id,
            @Param("inc") long inc
    );

    void inc(List<RangeInc> incs);

    List<BlockPO> selectActiveRanges(@Param("appId") long appId,
                                     @Param("typeIds") Collection<Long> typeIds);
}