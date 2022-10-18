package tech.metavm.flow.persistence;

import org.apache.ibatis.annotations.Mapper;
import tech.metavm.flow.rest.FlowQuery;

import java.util.Collection;
import java.util.List;

@Mapper
public interface FlowMapper {

    int batchDelete(List<Long> id);

    int batchInsert(Collection<FlowPO> record);

    List<FlowPO> selectByIds(Collection<Long> ids);

    List<FlowPO> selectByOwnerIds(Collection<Long> ownerIds);

    List<FlowPO> selectByInputTypeIds(Collection<Long> inputTypeIds);

    int batchUpdate(List<FlowPO> record);

    List<FlowPO> query(FlowQuery flowQueryDTO);

    long count(FlowQuery flowQueryDTO);
}