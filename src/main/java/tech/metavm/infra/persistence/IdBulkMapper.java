package tech.metavm.infra.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface IdBulkMapper {

    int batchInsert(List<IdBulk> record);

    IdBulk selectForUpdate(Integer bulkNum);

    void increaseNextId(
            @Param("bulkNum") int bulkNum,
            @Param("inc") long inc
    );

}