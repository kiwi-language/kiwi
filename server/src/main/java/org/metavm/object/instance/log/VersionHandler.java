//package org.metavm.object.instance.log;
//
//import org.metavm.entity.EntityContextFactory;
//import org.metavm.object.instance.core.IInstanceContext;
//import org.metavm.event.EventQueue;
//import org.metavm.event.rest.dto.FunctionChangeEvent;
//import org.metavm.event.rest.dto.TypeChangeEvent;
//import org.metavm.object.version.Version;
//import org.springframework.stereotype.Component;
//
//import javax.annotation.Nullable;
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//
//@Component
//public class VersionHandler implements LogHandler<Version> {
//
//    private final EventQueue eventQueue;
//
//    public VersionHandler(EventQueue eventQueue) {
//        this.eventQueue = eventQueue;
//    }
//
//    @Override
//    public Class<Version> getEntityClass() {
//        return Version.class;
//    }
//
//    @Override
//    public void process(List<Version> created, @Nullable String clientId, IInstanceContext context, EntityContextFactory entityContextFactory) {
//        if (!created.isEmpty()) {
//            long maxVersion = 0L;
//            Set<String> typeIds = new HashSet<>();
//            Set<String> functionIds = new HashSet<>();
//            for (Version version : created) {
//                maxVersion = Math.max(maxVersion, version.getVersion());
//                typeIds.addAll(version.getRemovedTypeIds());
//                typeIds.addAll(version.getChangedTypeIds());
//                functionIds.addAll(version.getChangedFunctionIds());
//                functionIds.addAll(version.getRemovedFunctionIds());
//            }
//            if (!typeIds.isEmpty()) {
//                eventQueue.publishAppEvent(
//                        new TypeChangeEvent(context.getAppId(), maxVersion, new ArrayList<>(typeIds), clientId)
//                );
//            }
//            if (!functionIds.isEmpty()) {
//                eventQueue.publishAppEvent(
//                        new FunctionChangeEvent(context.getAppId(), maxVersion, new ArrayList<>(functionIds), clientId)
//                );
//            }
//        }
//    }
//}
