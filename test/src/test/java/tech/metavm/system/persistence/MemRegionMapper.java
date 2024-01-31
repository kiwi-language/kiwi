package tech.metavm.system.persistence;

import tech.metavm.util.NncUtils;

import java.util.*;

public class MemRegionMapper implements RegionMapper {

    private final Map<Integer, RegionPO> category2region = new HashMap<>();

    @Override
    public List<RegionPO> selectByTypeCategories(Collection<Integer> typeCategories) {
        return NncUtils.filterAndMap(typeCategories, category2region::containsKey,
                c -> category2region.get(c).copy());
    }

    @Override
    public void inc(int typeCategory, long inc) {
        var region = category2region.get(typeCategory);
        if (region != null)
            region.setNextId(region.getNextId() + inc);
    }

    @Override
    public void batchInsert(List<RegionPO> regions) {
        for (RegionPO region : regions) {
            category2region.put(region.getTypeCategory(), region.copy());
        }
    }

    public MemRegionMapper copy() {
        var copy = new MemRegionMapper();
        copy.batchInsert(new ArrayList<>(category2region.values()));
        return copy;
    }

}
