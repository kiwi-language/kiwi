package org.metavm.system;

import org.metavm.object.instance.core.Id;
import org.metavm.object.type.TypeCategory;
import org.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;

import static org.metavm.object.type.IdConstants.*;

public class RegionConstants {
    static final Map<TypeCategory, RegionInfo> VALUE_MAP = new HashMap<>();
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

    public static boolean isArrayId(Id id) {
        return id.isArray();
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
}
