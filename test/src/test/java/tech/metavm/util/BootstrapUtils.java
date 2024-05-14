package tech.metavm.util;

import tech.metavm.entity.*;
import tech.metavm.entity.natives.NativeFunctions;
import tech.metavm.event.MockEventQueue;
import tech.metavm.flow.Function;
import tech.metavm.object.instance.MemInstanceSearchServiceV2;
import tech.metavm.object.instance.core.BooleanInstance;
import tech.metavm.object.instance.core.NullInstance;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.log.TaskHandler;
import tech.metavm.object.instance.log.VersionHandler;
import tech.metavm.object.instance.search.InstanceSearchService;
import tech.metavm.object.type.*;
import tech.metavm.system.IdService;
import tech.metavm.system.RegionManager;
import tech.metavm.system.persistence.MemBlockMapper;
import tech.metavm.system.persistence.MemRegionMapper;
import tech.metavm.task.JobSchedulerStatus;
import tech.metavm.task.TaskSignal;

import java.util.Collection;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class BootstrapUtils {

    private static volatile BootState state;

    private static EntityContextFactory createEntityContextFactory(EntityIdProvider idProvider,
                                                                   MemInstanceStore instanceStore,
                                                                   InstanceSearchService instanceSearchService) {
        var instanceContextFactory =
                TestUtils.getInstanceContextFactory(idProvider, instanceStore);
        var entityContextFactory = new EntityContextFactory(instanceContextFactory, instanceStore.getIndexEntryMapper());
        entityContextFactory.setInstanceLogService(
                new InstanceLogServiceImpl(entityContextFactory, instanceSearchService, instanceStore, List.of(
                        new TaskHandler(entityContextFactory, new MockTransactionOperations()),
                        new VersionHandler(new MockEventQueue())
                ))
        );
        entityContextFactory.setDefaultAsyncLogProcess(false);
        return entityContextFactory;
    }

    public static BootstrapResult bootstrap() {
        if (state != null) {
            var defContext = state.defContext();
            ModelDefRegistry.setDefContext(defContext);
            StandardTypes.setEntityKlass(defContext.getClassType(Entity.class).resolve());
            StandardTypes.setEnumKlass(defContext.getClassType(Enum.class).resolve());
            StandardTypes.setThrowableKlass(defContext.getClassType(Throwable.class).resolve());
            StandardTypes.setRuntimeExceptionKlass(defContext.getClassType(RuntimeException.class).resolve());
            StandardTypes.setCollectionKlass(defContext.getClassType(Collection.class).resolve());
            StandardTypes.setListKlass(defContext.getClassType(MetaList.class).resolve());
            StandardTypes.setReadWriteListKlass(defContext.getClassType(ReadWriteMetaList.class).resolve());
            StandardTypes.setChildListKlass(defContext.getClassType(ChildMetaList.class).resolve());
            StandardTypes.setSetKlass(defContext.getClassType(MetaSet.class).resolve());
            StandardTypes.setMapKlass(defContext.getClassType(MetaMap.class).resolve());
            StandardTypes.setIteratorImplKlass(defContext.getClassType(IteratorImpl.class).resolve());
            StandardTypes.setIteratorKlass(defContext.getClassType(MetaIterator.class).resolve());
            StandardTypes.setIterableKlass(defContext.getClassType(MetaIterable.class).resolve());
            StandardTypes.setRecordKlass(defContext.getClassType(Record.class).resolve());
            StandardTypes.setExceptionKlass(defContext.getClassType(Exception.class).resolve());
            StandardTypes.setIllegalArgumentExceptionKlass(defContext.getClassType(IllegalArgumentException.class).resolve());
            StandardTypes.setIllegalStateExceptionKlass(defContext.getClassType(IllegalStateException.class).resolve());
            StandardTypes.setNullPointerExceptionKlass(defContext.getClassType(NullPointerException.class).resolve());
            NativeFunctions.setIsSourcePresent(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "isSourcePResent"
            )));
            NativeFunctions.setSetSourceFunc(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "setSource"
            )));
            NativeFunctions.setGetSourceFunc(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "getSource"
            )));
            NativeFunctions.setFunctionToInstance(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "functionToInstance"
            )));
            NativeFunctions.setSendEmail(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "sendEmail"
            )));
            NativeFunctions.setGetSessionEntry(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "getSessionEntry"
            )));
            NativeFunctions.setSetSessionEntry(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "setSessionEntry"
            )));
            NativeFunctions.setRemoveSessionEntry(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "removeSessionEntry"
            )));
            NativeFunctions.setTypeCast(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "typeCast"
            )));
            NativeFunctions.setPrint(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "print"
            )));
            NativeFunctions.setDelete(requireNonNull(defContext.selectFirstByKey(
                    Function.UNIQUE_IDX_CODE, "delete"
            )));
            NativeFunctions.setEmailSender(MockEmailSender.INSTANCE);

            var state = BootstrapUtils.state.copy();
            var instanceStore = new MemInstanceStore(
                    state.instanceMapper(),
                    state.indexEntryMapper(),
                    state.referenceMapper()
            );
            var idProvider = new IdService(state.blockMapper(), new RegionManager(state.regionMapper()));
            var instanceSearchService = state.instanceSearchService();
            var entityContextFactory = createEntityContextFactory(idProvider, instanceStore, instanceSearchService);
            entityContextFactory.setDefContext(defContext);
            TestUtils.doInTransactionWithoutResult(() -> {
                try (var context = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                    context.bind(new JobSchedulerStatus());
                    context.bind(new TaskSignal(TestConstants.APP_ID));
                    context.finish();
                }
            });
            return new BootstrapResult(
                    entityContextFactory,
                    idProvider,
                    state.blockMapper(),
                    state.regionMapper(),
                    instanceStore,
                    instanceSearchService,
                    state.allocatorStore()
            );
        } else {
            NativeFunctions.setEmailSender(MockEmailSender.INSTANCE);
            var regionMapper = new MemRegionMapper();
            var regionManager = new RegionManager(regionMapper);
            regionManager.initialize();
            var blockMapper = new MemBlockMapper();
            var idProvider = new IdService(blockMapper, regionManager);
            var instanceStore = new MemInstanceStore();
            var instanceSearchService = new MemInstanceSearchServiceV2();
            var entityContextFactory = createEntityContextFactory(idProvider, instanceStore, instanceSearchService);
            var allocatorStore = new MemAllocatorStore();
            var columnStore = new MemColumnStore();
            var stdIdStore = new MemoryStdIdStore();
            var bootstrap = new Bootstrap(
                    entityContextFactory,
                    new StdAllocators(allocatorStore),
                    columnStore,
                    stdIdStore
            );
            bootstrap.boot();
            TestUtils.doInTransactionWithoutResult(() -> bootstrap.save(true));
            state = new BootState(
                    ModelDefRegistry.getDefContext(),
                    instanceStore.getInstanceMapper().copy(),
                    instanceStore.getReferenceMapper().copy(),
                    instanceStore.getIndexEntryMapper().copy(),
                    regionMapper.copy(),
                    blockMapper.copy(),
                    columnStore.copy(),
                    stdIdStore.copy(),
                    allocatorStore.copy(),
                    instanceSearchService.copy()
            );
            TestUtils.doInTransactionWithoutResult(() -> {
                try (var context = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                    context.bind(new JobSchedulerStatus());
                    context.bind(new TaskSignal(TestConstants.APP_ID));
                    context.finish();
                }
            });
            return new BootstrapResult(
                    entityContextFactory,
                    idProvider,
                    blockMapper,
                    regionMapper,
                    instanceStore,
                    instanceSearchService,
                    allocatorStore
            );
        }
    }

}
