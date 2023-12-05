package tech.metavm.object.instance.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
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
            @Param("targetId") long targetId,
            @Param("startIdExclusive") long startIdExclusive,
            @Param("limit") long limit
    );

    @Nullable
    ReferencePO selectFirstStrongReference(
            @Param("appId") long appId,
            @Param("targetIds") Collection<Long> targetIds,
            @Param("excludedSourceIds") Collection<Long> excludedSourceIds
    );

    List<ReferencePO> selectAllStrongReferences(
            @Param("appId") long appId,
            @Param("ids") Collection<Long> ids,
            @Param("excludedSourceIds") Collection<Long> excludedSourceIds
    );

    void batchInsert(Collection<ReferencePO> records);

    void batchUpdate(Collection<ReferencePO> records);

    void batchDelete(Collection<ReferencePO> records);

}
