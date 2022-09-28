package tech.metavm.flow.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;

@Mapper
public interface ScopeMapper {

    int batchDelete(Collection<Long> ids);

    void batchInsert(Collection<ScopePO> records);

    List<ScopePO> batchSelect(Collection<Long> ids);

    List<ScopePO> selectByFlowIds(Collection<Long> flowIds);

    List<ScopePO> selectByIds(Collection<Long> ids);

}