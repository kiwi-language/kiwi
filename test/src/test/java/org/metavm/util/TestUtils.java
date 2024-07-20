package org.metavm.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.hamcrest.MatcherAssert;
import org.metavm.api.Value;
import org.metavm.entity.*;
import org.metavm.event.MockEventQueue;
import org.metavm.flow.FlowExecutionService;
import org.metavm.flow.FlowManager;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.flow.rest.MethodParam;
import org.metavm.flow.rest.MethodRefDTO;
import org.metavm.flow.rest.ParameterDTO;
import org.metavm.object.instance.InstanceManager;
import org.metavm.object.instance.InstanceQueryService;
import org.metavm.object.instance.cache.MockCache;
import org.metavm.object.instance.core.DefaultViewId;
import org.metavm.object.instance.core.DurableInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.log.InstanceLogService;
import org.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import org.metavm.object.instance.rest.*;
import org.metavm.object.type.*;
import org.metavm.object.type.rest.dto.FieldDTO;
import org.metavm.object.type.rest.dto.GetTypeRequest;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.object.version.VersionManager;
import org.metavm.object.view.rest.dto.ObjectMappingDTO;
import org.metavm.task.*;
import org.slf4j.Logger;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class TestUtils {

    public static final String TEST_RESOURCE_ROOT = "/Users/leen/workspace/object/src/test/resources";

    public static final String TEST_RESOURCE_TARGET_ROOT = "/Users/leen/workspace/object/target/test-classes";

    public static final ObjectMapper INDENT_OBJECT_MAPPER = new ObjectMapper()
            .enable(JsonGenerator.Feature.IGNORE_UNKNOWN)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    private static long nextKlassTag = 1000000;

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new TypeReference<Class<?>>() {
        }.getType(), new ReflectClassSerializer());
        module.addSerializer(Field.class, new ReflectFieldSerializer());
        INDENT_OBJECT_MAPPER.registerModule(module);
        INDENT_OBJECT_MAPPER.registerModule(new Jdk8Module());
        INDENT_OBJECT_MAPPER.registerModule(new JavaTimeModule());
        INDENT_OBJECT_MAPPER.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        INDENT_OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        INDENT_OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public static String toJSONString(Object object) {
        try {
            return INDENT_OBJECT_MAPPER.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new InternalException(e);
        }
    }

    private final static byte[] byteBuf = new byte[1024 * 1024];

    public static byte[] readBytes(String path) {
        try (var input = new FileInputStream(path)) {
            int n = input.read(byteBuf);
            return Arrays.copyOfRange(byteBuf, 0, n);
        } catch (IOException e) {
            throw new InternalException(String.format("Fail to read file %s", path), e);
        }
    }

    public static FieldValue getListElement(FieldValue value, int index) {
        if (value instanceof ListFieldValue listFieldValue)
            return listFieldValue.getElements().get(index);
        else if (value instanceof InstanceFieldValue instanceFieldValue)
            return ((ListInstanceParam) instanceFieldValue.getInstance().param()).elements().get(index);
        else
            throw new InternalException("Invalid value: " + value);
    }

    public static <T> T readJSON(Class<T> klass, String json) {
        try {
            return INDENT_OBJECT_MAPPER.readValue(json, klass);
        } catch (JsonProcessingException e) {
            throw new InternalException("Fail to read JSON '" + json + "'", e);
        }
    }

    public static <T> T readJSON(Class<T> klass, Reader reader) {
        try {
            return INDENT_OBJECT_MAPPER.readValue(reader, klass);
        } catch (IOException e) {
            throw new InternalException("Fail to read JSON", e);
        }
    }

    public static void printJSON(Object object) {
        try {
            INDENT_OBJECT_MAPPER.writeValue(System.out, object);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    public static <R> R parseJson(String jsonStr, Class<R> klass) {
        return NncUtils.readJSONString(jsonStr, klass);
    }

    public static <R> R readJson(String path, Class<R> klass) {
        try {
            return INDENT_OBJECT_MAPPER.readValue(readEntireFile(path), klass);
        } catch (JsonProcessingException e) {
            throw new InternalException("JSON deserialization failed", e);
        }
    }

    public static <R> R readJson(String path, com.fasterxml.jackson.core.type.TypeReference<R> typeRef) {
        try {
            return INDENT_OBJECT_MAPPER.readValue(readEntireFile(path), typeRef);
        } catch (JsonProcessingException e) {
            throw new InternalException("JSON deserialization failed", e);
        }
    }

    public static String readEntireFile(String path) {
        try (FileReader reader = new FileReader(path)) {
            var builder = new StringBuilder();
            var buf = new char[256];
            while (true) {
                int n = reader.read(buf);
                if (n == -1)
                    return builder.toString();
                builder.append(buf, 0, n);
            }
        } catch (IOException e) {
            throw new RuntimeException("Fail to read file '" + path + "'", e);
        }
    }

    public static void writeFile(String path, String content) {
        try (var writer = new FileWriter(path)) {
            writer.write(content);
        } catch (IOException e) {
            throw new RuntimeException("Fail to write file '" + path + "'", e);
        }
    }

    public static void writeJson(String path, Object object) {
        writeFile(path, toJSONString(object));
    }

    public static void clearTestResourceDir(String dirName) {
        File dir = new File(TEST_RESOURCE_ROOT + "/" + dirName);
        if (dir.listFiles() != null) {
            for (File file : NncUtils.requireNonNull(dir.listFiles())) {
                if (!file.delete()) {
                    throw new InternalException("Fail to delete file " + file.getPath());
                }
            }
        }

        File targetDir = new File(TEST_RESOURCE_TARGET_ROOT + "/" + dirName);
        if (targetDir.listFiles() != null) {
            for (File file : NncUtils.requireNonNull(targetDir.listFiles())) {
                if (!file.delete()) {
                    throw new InternalException("Fail to delete file " + file.getPath());
                }
            }
        }
    }

    public static Id getTypeId(KlassDTO klassDTO) {
        return Id.parse(klassDTO.id());
    }

    public static String getId(FieldValue fieldValue) {
        return switch (fieldValue) {
            case ReferenceFieldValue refValue -> refValue.getId();
            case InstanceFieldValue instanceValue -> instanceValue.getInstance().id();
            default -> throw new InternalException("Can not get id from value: " + fieldValue);
        };
    }

    public static void doInTransactionWithoutResult(Runnable action) {
        MockTransactionUtils.doInTransactionWithoutResult(action);
    }

    public static <T> T doInTransaction(Supplier<T> action) {
        return MockTransactionUtils.doInTransaction(action);
    }

    public static DataSource createDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUsername("root");
        dataSource.setPassword("85263670");
        dataSource.setMaxActive(1);
        dataSource.setUrl("jdbc:mysql://localhost:3306/object?allowMultiQueries=true");
        dataSource.setDriverClassName("com.mysql.cj.jdbc.Driver");
        return dataSource;
    }

    public static void logJSON(Logger logger, Object object) {
        logJSON(logger, "JSON", object);
    }

    public static void logJSON(Logger logger, String title, Object object) {
        logger.info(title + "\n" + toJSONString(object));
    }

    public static EntityContextFactory getEntityContextFactory(EntityIdProvider idProvider,
                                                               MemInstanceStore instanceStore,
                                                               InstanceLogService instanceLogService,
                                                               IndexEntryMapper indexEntryMapper) {
        var factory = new EntityContextFactory(
                getInstanceContextFactory(idProvider, instanceStore),
                indexEntryMapper
        );
        factory.setInstanceLogService(instanceLogService);
        return factory;
    }

    public static InstanceContextFactory getInstanceContextFactory(EntityIdProvider idProvider,
                                                                   MemInstanceStore instanceStore) {
        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(instanceStore, new MockEventQueue())
                .setIdService(idProvider);
        instanceContextFactory.setCache(new MockCache());
        return instanceContextFactory;
    }

    public static void clearDirectory(File file) {
        if (file.isDirectory()) {
            for (File subFile : Objects.requireNonNull(file.listFiles())) {
                clearDirectory(subFile);
                subFile.delete();
            }
        }
    }

    public static void initEntityIds(Object entry) {
        var nextTreeIdRef = new Object() {
            long value = 10000L;
        };
        var roots = new IdentitySet<Entity>();
        EntityUtils.visitGraph(List.of(entry), o -> {
            if (o instanceof Entity entity)
                roots.add(entity.getRootEntity());
        });
        roots.forEach(r -> {
            long treeId;
            var nextNodeIdRef = new Object() {
                long value;
            };
            if (r.hasPhysicalId()) {
                treeId = r.getTreeId();
                r.forEachDescendant(e -> {
                    if (e.hasPhysicalId())
                        nextNodeIdRef.value = Math.max(nextNodeIdRef.value, e.getId().getNodeId() + 1);
                });
            }
            else
                treeId = nextTreeIdRef.value++;
            r.forEachDescendant(e -> {
                if(!e.isEphemeralEntity() && !(e instanceof Value) && !e.hasPhysicalId()) {
                    var type =
                            ModelDefRegistry.isDefContextPresent() ? ModelDefRegistry.getType(e) : AnyType.instance;
                    e.initId(PhysicalId.of(treeId, nextNodeIdRef.value++, type));
                }
            });
        });
    }

    public static void initInstanceIds(DurableInstance instance) {
        initInstanceIds(List.of(instance));
    }

    public static void initInstanceIds(List<DurableInstance> instances) {
        initInstanceIds(instances, new MockIdProvider());
    }

    public static void initInstanceIds(List<DurableInstance> instances, EntityIdProvider idProvider) {
        var roots = new IdentitySet<DurableInstance>();
        instances.forEach(r -> r.visitGraph(i -> {
            var root = i.getRoot();
            if (!root.isIdInitialized())
                roots.add(root);
            return true;
        }));
        roots.forEach(r -> {
            var treeId = idProvider.allocateOne(TestConstants.APP_ID, r.getType());
            var nodeIdRef = new Object() {
                long nextNodeId;
            };
            r.forEachDescendant(instance -> instance.initId(PhysicalId.of(treeId, nodeIdRef.nextNodeId++, instance.getType())));
        });
    }

    public static Set<String> extractDescendantIds(InstanceDTO instanceDTO) {
        Set<String> ids = new HashSet<>();
        extractDescendantIds(instanceDTO, ids);
        return ids;
    }

    public static InstanceDTO createInstanceWithCheck(InstanceManager instanceManager, InstanceDTO instanceDTO) {
        var id = doInTransaction(() -> instanceManager.create(instanceDTO));
        var loaded = instanceManager.get(id, 1).instance();
        MatcherAssert.assertThat(loaded, new InstanceDTOMatcher(instanceDTO, extractDescendantIds(loaded)));
        return loaded;
    }

    private static void extractDescendantIds(InstanceDTO instanceDTO, Set<String> ids) {
        if (instanceDTO.id() != null)
            ids.add(instanceDTO.id());
        if (instanceDTO.param() instanceof ClassInstanceParam classInstanceParam) {
            for (InstanceFieldDTO field : classInstanceParam.fields()) {
                if (field.value() instanceof InstanceFieldValue instanceFieldValue)
                    extractDescendantIds(instanceFieldValue.getInstance(), ids);
            }
        } else if (instanceDTO.param() instanceof ArrayInstanceParam arrayInstanceParam) {
            for (FieldValue element : arrayInstanceParam.elements()) {
                if (element instanceof InstanceFieldValue instanceFieldValue)
                    extractDescendantIds(instanceFieldValue.getInstance(), ids);
            }
        } else if (instanceDTO.param() instanceof ListInstanceParam listInstanceParam) {
            for (FieldValue element : listInstanceParam.elements()) {
                if (element instanceof InstanceFieldValue instanceFieldValue)
                    extractDescendantIds(instanceFieldValue.getInstance(), ids);
            }
        }
    }

    public static String getFieldIdByCode(KlassDTO klassDTO, String fieldCode) {
        return NncUtils.findRequired(klassDTO.fields(), f -> fieldCode.equals(f.code())).id();
    }

    public static FieldDTO getFieldByName(KlassDTO klassDTO, String name) {
        return NncUtils.findRequired(klassDTO.fields(), f -> name.equals(f.name()));
    }

    public static KlassDTO getViewKlass(KlassDTO type, TypeManager typeManager) {
        var defaultMapping = getDefaultMapping(type);
        var targetKlassId = TypeExpressions.extractKlassId(defaultMapping.targetType());
        return typeManager.getType(new GetTypeRequest(targetKlassId, false)).type();
    }

    public static ObjectMappingDTO getDefaultMapping(KlassDTO klassDTO) {
        return NncUtils.findRequired(
                klassDTO.mappings(),
                m -> m.id().equals(klassDTO.defaultMappingId())
        );
    }

    public static String getDefaultViewType(KlassDTO klassDTO) {
        return getDefaultMapping(klassDTO).targetType();
    }

    public static String getDefaultViewKlassId(KlassDTO klassDTO) {
        return TypeExpressions.extractKlassId(getDefaultViewType(klassDTO));
    }

    public static String getMethodIdByCode(KlassDTO klassDTO, String methodCode) {
        return NncUtils.findRequired(klassDTO.flows(), f -> methodCode.equals(f.code())).id();
    }

    public static MethodRefDTO getMethodRefByCode(KlassDTO klassDTO, String methodCode) {
        return new MethodRefDTO(
                TypeExpressions.getClassType(klassDTO.id()),
                getMethodIdByCode(klassDTO, methodCode),
                List.of()
        );
    }

    public static MethodRefDTO createMethodRef(String klassId, String methodId) {
        return new MethodRefDTO(TypeExpressions.getClassType(klassId), methodId, List.of());
    }

    public static FlowDTO getMethodByCode(KlassDTO klassDTO, String methodCode) {
        return NncUtils.findRequired(klassDTO.flows(), f -> methodCode.equals(f.code()));
    }

    public static String getStaticMethodIdByCode(KlassDTO klassDTO, String methodCode) {
        return NncUtils.findRequired(klassDTO.flows(),
                f -> methodCode.equals(f.code()) && ((MethodParam) f.param()).isStatic()
        ).id();
    }

    public static MethodRefDTO getStaticMethodRefByCode(KlassDTO klassDTO, String methodCode) {
        var method = NncUtils.findRequired(klassDTO.flows(),
                f -> methodCode.equals(f.code()) && ((MethodParam) f.param()).isStatic()
        );
        return createMethodRef(klassDTO.id(), method.id());
    }

    public static String getStaticMethod(KlassDTO klassDTO, String code, String... parameterTypes) {
        var paramTypeList = List.of(parameterTypes);
        return NncUtils.findRequired(klassDTO.flows(),
                f -> code.equals(f.code()) &&
                        ((MethodParam) f.param()).isStatic() &&
                        paramTypeList.equals(NncUtils.map(f.parameters(), ParameterDTO::type)),
                () -> "Can not find static method " + code + "(" + String.join(",", paramTypeList) + ") in type " + klassDTO.name()
        ).id();
    }

    public static MethodRefDTO getStaticMethodRef(KlassDTO klassDTO, String code, String... parameterTypes) {
        return new MethodRefDTO(TypeExpressions.getClassType(klassDTO.id()), getStaticMethod(klassDTO, code, parameterTypes), List.of());
    }

    public static String getMethodId(KlassDTO klassDTO, String code, String... parameterTypeIds) {
        var paramTypeidList = List.of(parameterTypeIds);
        return NncUtils.findRequired(klassDTO.flows(),
                f -> code.equals(f.code()) && paramTypeidList.equals(
                        NncUtils.map(f.parameters(), ParameterDTO::type)
                )
        ).id();
    }

    public static MethodRefDTO getMethodRef(KlassDTO klassDTO, String code, String... parameterTypeIds) {
        return new MethodRefDTO(TypeExpressions.getClassType(klassDTO.id()), getMethodId(klassDTO, code, parameterTypeIds), List.of());
    }

    public static String getEnumConstantIdByName(KlassDTO klassDTO, String name) {
        return NncUtils.findRequired(klassDTO.enumConstants(), f -> name.equals(f.title())).id();
    }

    public static InstanceDTO getEnumConstantByName(KlassDTO klassDTO, String name) {
        return NncUtils.findRequired(klassDTO.enumConstants(), f -> name.equals(f.title()));
    }

    private static final Klass mockKlass = TestUtils.newKlassBuilder("TestUtilsMock", "TestUtilsMock").build();

    public static ClassType mockClassType() {
        return mockKlass.getType();
    }

    public static void printIdClassName(String id) {
        System.out.println(Id.parse(id).getClass().getName());
    }

    public static CommonManagers createCommonManagers(BootstrapResult bootResult) {
        var entityContextFactory = bootResult.entityContextFactory();
        var instanceQueryService = new InstanceQueryService(bootResult.instanceSearchService());
        var entityQueryService = new EntityQueryService(instanceQueryService);
        var transactionOps = new MockTransactionOperations();
        var taskManager = new TaskManager(entityContextFactory, transactionOps);
        var typeManager = new TypeManager(entityContextFactory, entityQueryService, taskManager, new BeanManager());
        var flowExecutionService = new FlowExecutionService(entityContextFactory);
        var instanceManager = new InstanceManager(entityContextFactory, bootResult.instanceStore(), instanceQueryService);
        var flowManager = new FlowManager(entityContextFactory, transactionOps);
        var scheduler = new Scheduler(entityContextFactory, transactionOps);
        var worker = new Worker(entityContextFactory, transactionOps, new DirectTaskRunner());
        flowManager.setTypeManager(typeManager);
        typeManager.setFlowExecutionService(flowExecutionService);
        typeManager.setVersionManager(new VersionManager(entityContextFactory));
        typeManager.setFlowManager(flowManager);
        typeManager.setInstanceManager(instanceManager);
        return new CommonManagers(
                typeManager,
                flowManager,
                instanceManager,
                flowExecutionService,
                scheduler,
                worker
        );
    }

    public static String getSourceId(String viewId) {
        return ((DefaultViewId) Id.parse(viewId)).getSourceId().toString();
    }

    public static void waitForDDLDone(EntityContextFactory entityContextFactory) {
        var transactionOps = new MockTransactionOperations();
        var scheduler = new Scheduler(entityContextFactory, transactionOps);
        var worker = new Worker(entityContextFactory, transactionOps, new DirectTaskRunner());
        waitForTaskDone(scheduler, worker, t -> t instanceof DDL);
    }

    public static void waitForTaskDone(Predicate<Task> predicate, EntityContextFactory entityContextFactory) {
        var transactionOps = new MockTransactionOperations();
        var scheduler = new Scheduler(entityContextFactory, transactionOps);
        var worker = new Worker(entityContextFactory, transactionOps, new DirectTaskRunner());
        waitForTaskDone(scheduler, worker, predicate);
    }

    public static void waitForTaskDone(Scheduler scheduler, Worker worker, Predicate<Task> predicate) {
        scheduler.sendHeartbeat();
        worker.sendHeartbeat();
        for (int i = 0; i < 3; i++) {
            scheduler.schedule();
            if(worker.waitFor(predicate, 5))
                return;
        }
        throw new IllegalStateException("Condition not met after " + 15 + " runs");
    }

    private static long nextKlassTag() {
        return nextKlassTag++;
    }

    public static KlassBuilder newKlassBuilder(Class<?> javaClass) {
        return newKlassBuilder(javaClass.getSimpleName(), javaClass.getName());
    }

    public static KlassBuilder newKlassBuilder(String name) {
        return newKlassBuilder(name, name);
    }

    public static KlassBuilder newKlassBuilder(String name, String code) {
        return KlassBuilder.newBuilder(name, code).tag(nextKlassTag());
    }
}
