package tech.metavm.infra;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.infra.persistence.RegionMapper;
import tech.metavm.infra.persistence.RegionPO;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static tech.metavm.object.meta.IdConstants.*;

@Component
public class RegionManager {

    private final RegionMapper regionMapper;

    public RegionManager(RegionMapper regionMapper) {
        this.regionMapper = regionMapper;
    }

    @Transactional
    public void initialize() {
        List<Integer> codes = NncUtils.map(VALUE_MAP.keySet(), TypeCategory::code);
        Map<Integer, RegionPO> existing = NncUtils.toMap(
                regionMapper.selectByTypeCategories(codes),
                RegionPO::getTypeCategory
        );
        List<RegionPO> toInserts = new ArrayList<>();
        for (RegionInfo region : VALUE_MAP.values()) {
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

    private static final Map<TypeCategory, RegionInfo> VALUE_MAP = new HashMap<>();

    static {
        create(TypeCategory.CLASS, CLASS_REGION_BASE, CLASS_REGION_END);
        create(TypeCategory.ENUM, ENUM_REGION_BASE, ENUM_REGION_END);
        create(TypeCategory.ARRAY, ENUM_REGION_BASE, ENUM_REGION_END);
    }

    private static void create(TypeCategory typeCategory, long start, long end) {
        RegionInfo region = new RegionInfo(typeCategory, start, end);
        VALUE_MAP.put(typeCategory, region);
    }

    public static @Nullable RegionInfo getRegionStatic(TypeCategory typeCategory) {
        return VALUE_MAP.get(typeCategory);
    }

    public @Nullable RegionRT getRegion(TypeCategory typeCategory) {
        List<RegionPO> regionPOs = regionMapper.selectByTypeCategories(List.of(typeCategory.code()));
        NncUtils.requireNotEmpty(regionPOs, "No region found for type category: " + typeCategory);
        return new RegionRT(regionPOs.get(0));
    }

    public void inc(TypeCategory typeCategory, long inc) {
        regionMapper.inc(typeCategory.code(), inc);
    }
}
