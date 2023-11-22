package tech.metavm.management;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tech.metavm.management.persistence.RegionMapper;
import tech.metavm.management.persistence.RegionPO;
import tech.metavm.object.type.TypeCategory;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static RegionInfo CLASS_REGION;
    public static RegionInfo ENUM_REGION;
    public static RegionInfo CHILD_ARRAY_REGION;
    public static RegionInfo READ_WRITE_ARRAY_REGION;
    public static RegionInfo READ_ONLY_ARRAY_REGION;

    static {
        CLASS_REGION = create(TypeCategory.CLASS, CLASS_REGION_BASE, CLASS_REGION_END);
        ENUM_REGION = create(TypeCategory.ENUM, ENUM_REGION_BASE, ENUM_REGION_END);
        READ_WRITE_ARRAY_REGION = create(TypeCategory.READ_WRITE_ARRAY, READ_WRITE_ARRAY_REGION_BASE, READ_WRITE_ARRAY_REGION_END);
        READ_ONLY_ARRAY_REGION = create(TypeCategory.READ_ONLY_ARRAY, READ_ONLY_ARRAY_REGION_BASE, READ_ONLY_ARRAY_REGION_END);
        CHILD_ARRAY_REGION = create(TypeCategory.CHILD_ARRAY, CHILD_ARRAY_REGION_BASE, CHILD_ARRAY_REGION_END);
    }

    private static RegionInfo create(TypeCategory typeCategory, long start, long end) {
        RegionInfo region = new RegionInfo(typeCategory, start, end);
        VALUE_MAP.put(typeCategory, region);
        return region;
    }

    public static boolean isArrayId(long id) {
        return id >= READ_WRITE_ARRAY_REGION_BASE && id <= CHILD_ARRAY_REGION_END;
    }

    public static boolean isSystemId(long id) {
        for (RegionInfo region : VALUE_MAP.values()) {
            if(region.contains(id)) {
                return id - region.start() <= SYSTEM_RESERVE_PER_REGION;
            }
        }
        throw new InternalException(String.format("Invalid id: %d", id));
    }

    public static @Nullable RegionInfo getRegionStatic(TypeCategory typeCategory) {
        return VALUE_MAP.get(typeCategory);
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
