package tech.metavm.system;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.system.persistence.RegionMapper;
import tech.metavm.system.persistence.RegionPO;
import tech.metavm.object.type.TypeCategory;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static tech.metavm.object.type.IdConstants.*;

@Component
public class RegionManager {

    private final RegionMapper regionMapper;

    public RegionManager(RegionMapper regionMapper) {
        this.regionMapper = regionMapper;
    }

    @Transactional
    public void initialize() {
        List<Integer> codes = NncUtils.map(RegionConstants.VALUE_MAP.keySet(), TypeCategory::code);
        Map<Integer, RegionPO> existing = NncUtils.toMap(
                regionMapper.selectByTypeCategories(codes),
                RegionPO::getTypeCategory
        );
        List<RegionPO> toInserts = new ArrayList<>();
        for (RegionInfo region : RegionConstants.VALUE_MAP.values()) {
            if(!existing.containsKey(region.typeCategory().code())) {
                toInserts.add(
                        new RegionPO(
                                region.typeCategory().code(),
                                region.start(),
                                region.end(),
                                region.start() + SYSTEM_RESERVE_PER_REGION
                        )
                );
            }
        }
        if(NncUtils.isNotEmpty(toInserts)) {
            regionMapper.batchInsert(toInserts);
        }
    }

    public @Nullable RegionRT get(TypeCategory typeCategory) {
        try(var ignored= ContextUtil.getProfiler().enter("RegionManager.get")) {
            List<RegionPO> regionPOs = regionMapper.selectByTypeCategories(List.of(typeCategory.code()));
            NncUtils.requireNotEmpty(regionPOs, "No region found for type category: " + typeCategory);
            return new RegionRT(regionPOs.get(0));
        }
    }

    public void inc(TypeCategory typeCategory, long inc) {
        try(var ignored= ContextUtil.getProfiler().enter("RegionManager.inc")) {
            regionMapper.inc(typeCategory.code(), inc);
        }
    }
}
