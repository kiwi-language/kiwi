package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.persistence.ReferencePO;
import tech.metavm.object.instance.persistence.TargetPO;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;

@Mapper
public interface ReferenceMapper {

    List<ReferencePO> selectByTargetsWithKind(Collection<TargetPO> targets);

    List<ReferencePO> selectByTargetsWithField(Collection<TargetPO> targets);

    List<ReferencePO> selectByTargetId(
            @Param("appId") long appId,
            @Param("targetId") byte[] targetId,
            @Param("startIdExclusive") byte[] startIdExclusive,
            @Param("limit") long limit
    );

    @Nullable
    ReferencePO selectFirstStrongReference(
            @Param("appId") long appId,
            @Param("targetIds") Collection<Id> targetIds,
            @Param("excludedSourceIds") Collection<Id> excludedSourceIds
    );

    List<ReferencePO> selectAllStrongReferences(
            @Param("appId") long appId,
            @Param("ids") Collection<Id> ids,
            @Param("excludedSourceIds") Collection<Id> excludedSourceIds
    );

    void batchInsert(Collection<ReferencePO> records);

    void batchUpdate(Collection<ReferencePO> records);

    void batchDelete(Collection<ReferencePO> records);

}
