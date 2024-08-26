package org.metavm.entity;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.jetbrains.annotations.NotNull;
import org.metavm.ddl.Commit;
import org.metavm.flow.CallableRef;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.FieldRef;
import org.metavm.object.type.Klass;
import org.metavm.object.type.TypeDef;
import org.metavm.object.view.ObjectMappingRef;
import org.metavm.util.InternalException;
import org.metavm.util.ParameterizedStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Nullable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class MetaContextCache extends EntityContextFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(MetaContextCache.class);

    public static final int MAX_SIZE = 16;

    private final LoadingCache<CacheKey, IEntityContext> cache = CacheBuilder.newBuilder()
            .maximumSize(MAX_SIZE)
            .build(new CacheLoader<>() {

                @Override
                public @NotNull IEntityContext load(@NotNull CacheKey key) {
                    return createMetaContext(key);
                }
            });

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    public MetaContextCache(EntityContextFactory entityContextFactory) {
        super(entityContextFactory);
        Commit.META_CONTEXT_INVALIDATE_HOOK = this::invalidate;
    }

    public IEntityContext get(long appId) {
        return get(appId, null);
    }

    public IEntityContext get(long appId, @Nullable Id walId) {
        try {
            var context = cache.get(new CacheKey(appId, walId));
            ParameterizedStore.setMap(context.getParameterizedMap());
            return context;
        } catch (ExecutionException e) {
            throw new InternalException(e);
        }
    }

    public void invalidate(long appId, @Nullable Id walId) {
        cache.invalidate(new CacheKey(appId, walId));
    }

    private IEntityContext createMetaContext(CacheKey key) {
        IEntityContext context;
        if(key.walId != null) {
            try(var outerContext = newContext(key.appId)) {
                var wal = outerContext.getEntity(WAL.class, key.walId);
                context = newContext(key.appId, builder -> builder.readWAL(wal));
            }
        }
        else
            context = newContext(key.appId);
        loadAllTypeDefs(context);
        context.setParameterizedMap(ParameterizedStore.getMap());
        return context;
    }

    private void loadAllTypeDefs(IEntityContext context) {
//        var future = executor.submit(() -> {
        var typeDefs = context.selectByKey(Klass.IDX_ALL_FLAG, true);
        for (TypeDef typeDef : typeDefs) {
            EntityUtils.ensureTreeInitialized(typeDef);
            if(typeDef instanceof Klass klass && klass.isEnum()) {
                klass.getEnumConstants();
            }
        }
        for (TypeDef typeDef : typeDefs) {
            EntityUtils.forEachMember(typeDef, e -> {
                switch (e) {
                    case ClassType classType -> classType.resolve();
                    case FieldRef fieldRef -> fieldRef.resolve();
                    case CallableRef callableRef -> callableRef.resolve();
                    case ObjectMappingRef objectMappingRef -> objectMappingRef.resolve();
                    default -> {}
                }
            });
        }
        for (TypeDef typeDef : typeDefs) {
            if(typeDef instanceof Klass klass) {
                klass.rebuildMethodTable();
                klass.forEachParameterized(Klass::rebuildMethodTable);
            }
        }
//        });
//        try {
//            future.get();
//        } catch (InterruptedException | ExecutionException e) {
//            throw new RuntimeException(e);
//        }
    }

    private record CacheKey(long appId, @Nullable Id walId) {
    }

}