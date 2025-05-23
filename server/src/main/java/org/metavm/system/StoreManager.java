//package org.metavm.system;
//
//import org.metavm.object.instance.cache.LocalCache;
//import org.metavm.object.instance.persistence.mappers.InstanceMapper;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Nullable;
//import java.util.Map;
//
//import static org.metavm.util.BytesUtils.convertToJSON;
//
//@Component
//public class StoreManager {
//
//    private final InstanceMapper instanceMapper;
//
////    private final RedisCache cache;
//    private final LocalCache cache;
//
//    public StoreManager(InstanceMapper instanceMapper, LocalCache cache) {
//        this.instanceMapper = instanceMapper;
//        this.cache = cache;
//    }
//
//    public @Nullable Object getCached(long id) {
//        var bytes = cache.get(id);
//        if(bytes == null)
//            return null;
//        return convertToJSON(bytes, true);
//    }
//
//    public @Nullable Object getInstance(long id) {
//        var instance = instanceMapper.selectById(id);
//        if (instance == null)
//            return null;
//        return Map.of(
//                "version", instance.getVersion(),
//                "data", convertToJSON(instance.getData(), false)
//        );
//    }
//
//
//}
//
