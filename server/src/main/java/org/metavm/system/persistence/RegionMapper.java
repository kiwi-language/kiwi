package org.metavm.system.persistence;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.metavm.util.PrimaryMapper;

import java.util.Collection;
import java.util.List;

@Mapper
public interface RegionMapper extends PrimaryMapper {

    List<RegionPO> selectByTypeCategories(Collection<Integer> typeCategories);

    void inc(@Param("typeCategory") int typeCategory, @Param("inc") long inc);

    void batchInsert(List<RegionPO> regions);

}
