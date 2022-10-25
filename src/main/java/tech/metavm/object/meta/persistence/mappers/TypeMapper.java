package tech.metavm.object.meta.persistence.mappers;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.persistence.query.TypeQuery;

import java.util.Collection;
import java.util.List;

@Mapper
public interface TypeMapper {

    List<TypePO> getPrimitiveTypes();

    TypePO selectByCode(int code);

    List<TypePO> selectByIds(Collection<Long> ids);

    TypePO selectByName(@Param("tenantId") long tenantId, @Param("name") String name);

    TypePO selectParameterized(
            @Param("tenantId") long tenantId,
            @Param("rawTypeId") long rawTypeId,
            @Param("typeArgumentIds") List<Long> typeArgumentIds
        );

    List<TypePO> selectByRawTypeId(
            @Param("tenantId") long tenantId,
            @Param("rawTypeId") long rawTypeId
    );


    List<TypePO> selectByTypeArgumentId(
            @Param("tenantId") long tenantId,
            @Param("typeArgumentId") long typeArgumentId
    );

    void batchInsert(List<TypePO> typePOs);

    int batchUpdate(List<TypePO> typePOs);

    int batchDelete(List<Long> ids);

    long count(TypeQuery query);

    List<TypePO> query(TypeQuery query);
}