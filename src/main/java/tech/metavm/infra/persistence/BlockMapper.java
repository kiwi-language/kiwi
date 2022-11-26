package tech.metavm.infra.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface BlockMapper {

    List<BlockPO> selectByIds(Collection<Long> ids);

    int batchInsert(List<BlockPO> record);

    BlockPO selectForUpdate(Integer bulkNum);

    List<BlockPO> selectActive(Collection<Long> typeIds);

    void batchUpdate(Collection<BlockPO> ranges);

    BlockPO selectByPoint(@Param("point") long point);

    void increaseNextId(
            @Param("id") int id,
            @Param("inc") long inc
    );

    void inc(List<RangeInc> incs);

    List<BlockPO> selectActiveRanges(@Param("tenantId") long tenantId,
                                     @Param("typeIds") Collection<Long> typeIds);
}