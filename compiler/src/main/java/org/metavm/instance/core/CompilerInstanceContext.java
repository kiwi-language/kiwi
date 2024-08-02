package org.metavm.instance.core;

import org.metavm.entity.IdInitializer;
import org.metavm.entity.VersionSource;
import org.metavm.event.EventQueue;
import org.metavm.object.instance.IndexSource;
import org.metavm.object.instance.TreeSource;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.RedirectStatusProvider;
import org.metavm.object.type.TypeDefProvider;
import org.metavm.object.view.MappingProvider;

import javax.annotation.Nullable;
import java.util.List;

public class CompilerInstanceContext extends BufferingInstanceContext {

    public CompilerInstanceContext(long appId,
                                   List<TreeSource> treeSources,
                                   VersionSource versionSource,
                                   IdInitializer idService,
                                   IndexSource indexSource,
                                   IInstanceContext parent,
                                   TypeDefProvider typeDefProvider,
                                   MappingProvider mappingProvider,
                                   RedirectStatusProvider redirectStatusProvider,
                                   boolean readonly) {
        super(appId,
                treeSources, versionSource,
                indexSource, idService,
                parent,
                typeDefProvider,
                mappingProvider, redirectStatusProvider, readonly, 0);
    }

    @Override
    public IInstanceContext createSame(long appId) {
        return null;
    }

    @Override
    public ScanResult scan(long start, long limit) {
        throw new UnsupportedOperationException();
    }

//    @Override
//    public List<DurableInstance> getByType(Type type, @Nullable DurableInstance startExclusive, long limit) {
//        return null;
//    }

//    @Override
//    public List<DurableInstance> scan(DurableInstance startExclusive, long limit) {
//        return null;
//    }

//    @Override
//    public boolean existsInstances(Type type, boolean persistedOnly) {
//        return false;
//    }

    @Override
    public List<Instance> getByReferenceTargetId(Id targetId, long startExclusive, long limit) {
        return null;
    }

    @Override
    public List<Instance> getRelocated() {
        return List.of();
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
