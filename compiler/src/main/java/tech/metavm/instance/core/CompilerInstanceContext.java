package tech.metavm.instance.core;

import tech.metavm.entity.IdInitializer;
import tech.metavm.entity.VersionSource;
import tech.metavm.event.EventQueue;
import tech.metavm.flow.ParameterizedFlowProvider;
import tech.metavm.object.instance.IndexSource;
import tech.metavm.object.instance.TreeSource;
import tech.metavm.object.instance.core.BufferingInstanceContext;
import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.instance.core.IInstanceContext;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.Type;
import tech.metavm.object.type.TypeProvider;
import tech.metavm.object.view.MappingProvider;

import javax.annotation.Nullable;
import java.util.List;

public class CompilerInstanceContext extends BufferingInstanceContext {

    public CompilerInstanceContext(long appId,
                                   List<TreeSource> treeSources,
                                   VersionSource versionSource,
                                   IdInitializer idService,
                                   IndexSource indexSource,
                                   IInstanceContext parent,
                                   TypeProvider typeProvider,
                                   MappingProvider mappingProvider,
                                   ParameterizedFlowProvider parameterizedFlowProvider,
                                   boolean readonly) {
        super(appId,
                treeSources, versionSource,
                indexSource, idService,
                parent,
                typeProvider,
                mappingProvider, parameterizedFlowProvider, readonly);
    }

    @Override
    public IInstanceContext createSame(long appId) {
        return null;
    }

    @Override
    public List<DurableInstance> getByType(Type type, @Nullable DurableInstance startExclusive, long limit) {
        return null;
    }

    @Override
    public List<DurableInstance> scan(DurableInstance startExclusive, long limit) {
        return null;
    }

    @Override
    public boolean existsInstances(Type type, boolean persistedOnly) {
        return false;
    }

    @Override
    public List<DurableInstance> getByReferenceTargetId(long targetId, DurableInstance startExclusive, long limit) {
        return null;
    }

    @Override
    protected void finishInternal() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isFinished() {
        return false;
    }

    @Override
    public void initIds() {
        throw new UnsupportedOperationException();
    }

    @Nullable
    @Override
    public EventQueue getEventQueue() {
        return null;
    }

}
