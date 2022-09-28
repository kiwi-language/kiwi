package tech.metavm.object.meta.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.meta.persistence.FieldPO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface FieldMapper {

    int deleteByOwnerId(@Param("tenantId") long tenantId,
                        @Param("ownerId") long ownerId);

    void batchInsert(Collection<FieldPO> records);

    int batchUpdate(List<FieldPO> records);

    void updateAsTitle(@Param("tenantId") long tenantId,
                       @Param("id") long id,
                       @Param("asTitle") boolean asTitle
    );

    FieldPO selectById(@Param("tenantId") long tenantId,
                       @Param("id") long id);

    List<FieldPO> selectByIds(Collection<Long> ids);

    List<FieldPO> selectByOwnerIds(@Param("tenantId") long tenantId,
                                   @Param("ownerIds") List<Long> ownerIds);

    List<FieldPO> selectTitleFields(@Param("tenantId") long tenantId,
                                    @Param("ownerIds") List<Long> ownerIds);

    void batchDelete(List<Long> ids);

    int countByTargetId(@Param("tenantId") long tenantId,
                        @Param("targetId") long targetId);

    List<FieldPO> selectByTargetId(@Param("tenantId") long tenantId,
                                   @Param("targetId") long targetId,
                                   @Param("limit") int limit
    );
}