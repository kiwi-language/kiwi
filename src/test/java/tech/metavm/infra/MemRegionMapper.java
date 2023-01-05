package tech.metavm.infra;

import tech.metavm.infra.persistence.RegionMapper;
import tech.metavm.infra.persistence.RegionPO;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import java.util.*;

public class MemRegionMapper implements RegionMapper {

    private final Map<Integer, RegionPO> category2region = new HashMap<>();

    @Override
    public List<RegionPO> selectByTypeCategories(Collection<Integer> typeCategories) {
        return NncUtils.mapAndFilter(typeCategories, category2region::get, Objects::nonNull);
    }

    @Override
    public void inc(int typeCategory, long inc) {
        RegionPO regionPO = NncUtils.requireNonNull(
                category2region.get(typeCategory),
                "Can not find the region for category '" + typeCategory + "'"
        );
        long next = regionPO.getNext() + inc;
        if(next > regionPO.getEnd()) {
            throw new InternalException("Region out of space");
        }
        regionPO.setNext(next);
    }

    @Override
    public void batchInsert(List<RegionPO> regions) {
        for (RegionPO region : regions) {
            if(category2region.containsKey(region.getTypeCategory())) {
                throw new InternalException("Region for category '" + region.getTypeCategory() + "' already exists");
            }
            category2region.put(
                    region.getTypeCategory(),
                    new RegionPO(
                            region.getTypeCategory(),
                            region.getStart(),
                            region.getEnd(),
                            region.getNext()
                    )
            );
        }
    }
}
