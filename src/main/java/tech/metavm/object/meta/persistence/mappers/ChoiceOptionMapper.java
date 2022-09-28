package tech.metavm.object.meta.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.meta.persistence.IdentityPO;
import tech.metavm.object.meta.persistence.ChoiceOptionPO;

import java.util.List;

@Mapper
public interface ChoiceOptionMapper {

    int batchDelete(@Param("tenantId") long tenantId,
                    @Param("ids") List<Long> ids);

    int batchInsert(List<ChoiceOptionPO> records);

    ChoiceOptionPO selectById(IdentityPO id);

    int batchUpdate(List<ChoiceOptionPO> records);

    List<ChoiceOptionPO> selectByFieldIds(@Param("tenantId") long tenantId,
                                          @Param("fieldIds") List<Long> fieldIds);

    int deleteByFieldIds(@Param("tenantId") long tenantId,
                         @Param("fieldIds") List<Long> fieldIds);
}