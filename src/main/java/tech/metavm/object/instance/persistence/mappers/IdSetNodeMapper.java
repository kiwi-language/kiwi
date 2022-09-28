package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.persistence.IdSetNodePO;

import java.util.List;

public interface IdSetNodeMapper {

    int batchDelete(List<Long> ids);

    int batchInsert(List<IdSetNodePO> record);

    List<IdSetNodePO> select(@Param("setId") Long setId,
                             @Param("start") Long start,
                             @Param("limit") Long limit,
                             @Param("desc") boolean desc);

    int batchUpdate(IdSetNodePO record);

    int updateByPrimaryKey(IdSetNodePO record);
}