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
import java.util.Date;
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
        if(state != null) {
            var defContext = state.defContext();
            ModelDefRegistry.setDefContext(defContext);
            StandardTypes.setBooleanType((PrimitiveType) defContext.getType(Boolean.class));
            StandardTypes.setDoubleType((PrimitiveType) defContext.getType(Double.class));
            StandardTypes.setLongType((PrimitiveType) defContext.getType(Long.class));
            StandardTypes.setTimeType((PrimitiveType) defContext.getType(Date.class));
            StandardTypes.setPasswordType((PrimitiveType) defContext.getType(Password.class));
            StandardTypes.setStringType((PrimitiveType) defContext.getType(String.class));
            StandardTypes.setVoidType((PrimitiveType) defContext.getType(Void.class));
            StandardTypes.setNullType((PrimitiveType) defContext.getType(Null.class));
            StandardTypes.setAnyType((AnyType) defContext.getType(Object.class));
            StandardTypes.setNeverType((NeverType) defContext.getType(Never.class));
            StandardTypes.setAnyArrayType(
                    defContext.getArrayType(StandardTypes.getAnyType(), ArrayKind.READ_WRITE)
            );
            StandardTypes.setReadonlyAnyArrayType(
                    defContext.getArrayType(StandardTypes.getAnyType(), ArrayKind.READ_ONLY)
            );
            StandardTypes.setNeverArrayType(defContext.getArrayType(StandardTypes.getNeverType(), ArrayKind.READ_WRITE));
            StandardTypes.setNullableAnyType(defContext.getNullableType(StandardTypes.getAnyType()));
            StandardTypes.setEntityType(defContext.getClassType(Entity.class));
            StandardTypes.setEnumType(defContext.getClassType(Enum.class));
            StandardTypes.setThrowableType(defContext.getClassType(Throwable.class));
            StandardTypes.setRuntimeExceptionType(defContext.getClassType(RuntimeException.class));
            StandardTypes.setNullableStringType(defContext.getNullableType(StandardTypes.getStringType()));
            StandardTypes.setCollectionType(defContext.getClassType(Collection.class));
            StandardTypes.setListType(defContext.getClassType(MetaList.class));
            StandardTypes.setReadWriteListType(defContext.getClassType(ReadWriteMetaList.class));
            StandardTypes.setChildListType(defContext.getClassType(ChildMetaList.class));
            StandardTypes.setSetType(defContext.getClassType(MetaSet.class));
            StandardTypes.setMapType(defContext.getClassType(MetaMap.class));
            StandardTypes.setIteratorImplType(defContext.getClassType(IteratorImpl.class));
            StandardTypes.setIteratorType(defContext.getClassType(MetaIterator.class));
            StandardTypes.setRecordType(defContext.getClassType(Record.class));
            StandardTypes.setExceptionType(defContext.getClassType(Exception.class));
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
            Instances.setFalseInstance(new BooleanInstance(false, StandardTypes.getBooleanType()));
            Instances.setTrueInstance(new BooleanInstance(true, StandardTypes.getBooleanType()));
            Instances.setNullInstance(new NullInstance(StandardTypes.getNullType()));

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
                try(var context = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                    context.bind(new JobSchedulerStatus());
                    context.bind(new TaskSignal(TestConstants.APP_ID));
                    context.finish();
                }
            });
            return new BootstrapResult(
                    entityContextFactory,
                    idProvider,
                    state.blockMapper(),
                    instanceStore,
                    instanceSearchService,
                    state.allocatorStore()
            );
        }
        else {
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
            var bootstrap = new Bootstrap(
                    entityContextFactory,
                    new StdAllocators(allocatorStore),
                    columnStore
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
                    allocatorStore.copy(),
                    instanceSearchService.copy()
            );
            TestUtils.doInTransactionWithoutResult(() -> {
                try(var context = entityContextFactory.newContext(Constants.PLATFORM_APP_ID)) {
                    context.bind(new JobSchedulerStatus());
                    context.bind(new TaskSignal(TestConstants.APP_ID));
                    context.finish();
                }
            });
            return new BootstrapResult(
                    entityContextFactory,
                    idProvider,
                    blockMapper,
                    instanceStore,
                    instanceSearchService,
                    allocatorStore
            );
        }
    }

}
