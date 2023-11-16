package tech.metavm.object.type.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.type.persistence.ConstraintPO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ConstraintMapper {

    void batchInsert(Collection<ConstraintPO> records);

    int batchUpdate(Collection<ConstraintPO> records);

    List<ConstraintPO> selectByIds(Collection<Long> ids);

    List<ConstraintPO> selectByTypeIds(Collection<Long> typeIds);

    void batchDelete(Collection<Long> ids);

    List<ConstraintPO> getStandardConstraints();

    long countByTypeId(@Param("typeId") long typeId);

    List<ConstraintPO> selectByTypeId(
            @Param("typeId") long typeId,
            @Param("start") long start,
            @Param("limit") long limit
    );

}