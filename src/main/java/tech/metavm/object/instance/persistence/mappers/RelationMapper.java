package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.persistence.RelationPO;
import tech.metavm.object.instance.persistence.RelationTarget;

import java.util.List;

@Mapper
public interface RelationMapper {

    int batchDelete(List<RelationPO> record);

    int batchInsert(List<RelationPO> records);

    List<RelationPO> selectBySourceIds(@Param("tenantId") long tenantId,
                                       @Param("sourceIds") List<Long> sourceIds);

    List<RelationPO> selectByTargets(@Param("tenantId") long tenantId,
                                     @Param("targets") List<RelationTarget> targets);

}