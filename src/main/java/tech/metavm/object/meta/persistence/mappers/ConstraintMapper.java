package tech.metavm.object.meta.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.persistence.FieldPO;

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
}