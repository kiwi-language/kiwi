package tech.metavm.flow.persistence;

import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface NodeMapper {

    List<NodePO> selectByFlowIds(Collection<Long> flowIds);

    int batchDelete(List<Long> id);

    int batchInsert(Collection<NodePO> records);

    List<NodePO> selectByIds(Collection<Long> id);

    int batchUpdate(List<NodePO> records);

}