package tech.metavm.object.meta.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.meta.persistence.FieldPO;

import java.util.Collection;
import java.util.List;

@Mapper
public interface FieldMapper {

    List<FieldPO> getStandardFields();

    int deleteByDeclaringTypeId(@Param("tenantId") long tenantId,
                                @Param("declaringTypeId") long declaringTypeId);

    void batchInsert(Collection<FieldPO> records);

    int batchUpdate(Collection<FieldPO> records);

    void updateAsTitle(@Param("tenantId") long tenantId,
                       @Param("id") long id,
                       @Param("asTitle") boolean asTitle
    );

    FieldPO selectById(@Param("tenantId") long tenantId,
                       @Param("id") long id);

    List<FieldPO> selectByIds(Collection<Long> ids);

    List<FieldPO> selectByTypeIds(Collection<Long> ids);

    List<FieldPO> selectByDeclaringTypeIds(
            @Param("declaringTypeIds") Collection<Long> declaringTypeIds
    );

    List<FieldPO> selectTitleFields(@Param("tenantId") long tenantId,
                                    @Param("declaringTypeIds") List<Long> declaringTypeIds);

    void batchDelete(Collection<Long> ids);

}