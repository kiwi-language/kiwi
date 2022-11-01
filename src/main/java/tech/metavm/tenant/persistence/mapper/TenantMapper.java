package tech.metavm.tenant.persistence.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tech.metavm.tenant.persistence.TenantPO;

import java.util.List;

@Mapper
public interface TenantMapper {

    List<TenantPO> query(@Param("start") long start,
                         @Param("limit") long limit,
                         @Param("searchText") String searchText);

    long count(@Param("searchText") String searchText);

    void insert(TenantPO tenant);

    TenantPO selectById(long id);

    void update(TenantPO tenant);

    void delete(long id);

}
