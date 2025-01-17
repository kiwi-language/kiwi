//package org.metavm.ddl;
//
//import com.google.common.cache.Cache;
//import com.google.common.cache.CacheBuilder;
//import org.jetbrains.annotations.NotNull;
//import org.metavm.entity.DefaultIdInitializer;
//import org.metavm.entity.EntityContextFactory;
//import org.metavm.entity.ModelDefRegistry;
//import org.metavm.entity.ReversedDefContext;
//import org.metavm.object.instance.CachingInstanceStore;
//import org.metavm.object.instance.core.EntityInstanceContextBridge;
//import org.metavm.object.instance.core.Id;
//import org.metavm.object.instance.core.InstanceContext;
//import org.metavm.object.instance.core.WAL;
//import org.metavm.object.type.ITypeDef;
//import org.metavm.object.type.TypeDefProvider;
//import org.metavm.util.Constants;
//import org.metavm.util.InternalException;
//import org.metavm.util.NncUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.ExecutionException;
//
//public class DefContextUtils {
//
//    public static final Logger logger = LoggerFactory.getLogger(DefContextUtils.class);
//
//    private static final Cache<CacheKey, ReversedDefContext> cache = CacheBuilder.newBuilder()
//            .weakValues()
//            .build();
//
//    public static ReversedDefContext createReversedDefContext(WAL wal, EntityContextFactory entityContextFactory, List<String> extraKlassIds) {
//        ReversedDefContext defContext;
//        if(wal.tryGetId() == null)
//            defContext = create0(wal, extraKlassIds, entityContextFactory);
//        else {
//            try {
//                defContext = cache.get(new CacheKey(wal.getStringId(), extraKlassIds), () -> create0(wal, extraKlassIds, entityContextFactory));
//            } catch (ExecutionException e) {
//                throw new InternalException("Failed to get ReveredDefContext from cache", e);
//            }
//        }
//        defContext.initEnv();
//        ModelDefRegistry.setLocalDefContext(defContext);
//        return defContext;
//    }
//
//    private static ReversedDefContext create0(WAL wal, List<String> extraKlassIds, EntityContextFactory entityContextFactory) {
//        var sysDefContext = ModelDefRegistry.getDefContext();
//        var dc = new ReversedDefContext(wal, sysDefContext);
//        dc.initializeFrom(sysDefContext, extraKlassIds);
//        return dc;
//    }
//
//    public static WAL getWal(ReversedDefContext reversedDefContext) {
//        return reversedDefContext.getWal();
//    }
//
//    private record CacheKey(
//            @NotNull String walId, List<String> extraKlassIds
//    ) {
//    }
//
//}
