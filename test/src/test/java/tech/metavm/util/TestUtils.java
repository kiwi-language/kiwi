package tech.metavm.util;

import com.alibaba.druid.pool.DruidDataSource;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.hamcrest.MatcherAssert;
import org.slf4j.Logger;
import tech.metavm.entity.*;
import tech.metavm.event.MockEventQueue;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.MethodParam;
import tech.metavm.flow.rest.MethodRefDTO;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.cache.MockCache;
import tech.metavm.object.instance.core.StructuralVisitor;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.log.InstanceLogService;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.FieldDTO;
import tech.metavm.object.type.rest.dto.GetTypeRequest;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.view.rest.dto.ObjectMappingDTO;

import javax.sql.DataSource;
import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.function.Supplier;

public class TestUtils {

    public static final String TEST_RESOURCE_ROOT = "/Users/leen/workspace/object/src/test/resources";

    public static final String TEST_RESOURCE_TARGET_ROOT = "/Users/leen/workspace/object/target/test-classes";

    public static final ObjectMapper INDENT_OBJECT_MAPPER = new ObjectMapper();

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

    public static Id getTypeId(TypeDTO typeDTO) {
        return Id.parse(typeDTO.id());
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
                if(!e.hasPhysicalId()) {
                    var type =
                            ModelDefRegistry.isDefContextPresent() ? ModelDefRegistry.getType(e) : new AnyType();
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
        var visitor = new GraphVisitor() {
            @Override
            public Void visitDurableInstance(DurableInstance instance) {
                var root = instance.getRoot();
                if (!root.isIdInitialized())
                    roots.add(root);
                return super.visitDurableInstance(instance);
            }
        };
        instances.forEach(visitor::visit);
        roots.forEach(r -> {
            var treeId = idProvider.allocateOne(TestConstants.APP_ID, r.getType());
            var nodeIdRef = new Object() {
                long nextNodeId;
            };
            r.accept(new StructuralVisitor() {

                @Override
                public Void visitDurableInstance(DurableInstance instance) {
                    instance.initId(PhysicalId.of(treeId, nodeIdRef.nextNodeId++, instance.getType()));
                    return super.visitDurableInstance(instance);
                }
            });
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

    public static String getFieldIdByCode(TypeDTO typeDTO, String fieldCode) {
        return NncUtils.findRequired(typeDTO.getClassParam().fields(), f -> fieldCode.equals(f.code())).id();
    }

    public static FieldDTO getFieldByName(TypeDTO typeDTO, String name) {
        return NncUtils.findRequired(typeDTO.getClassParam().fields(), f -> name.equals(f.name()));
    }

    public static TypeDTO getViewType(TypeDTO type, TypeManager typeManager) {
        var defaultMapping = getDefaultMapping(type);
        var targetKlassId = TypeExpressions.extractKlassId(defaultMapping.targetType());
        return typeManager.getType(new GetTypeRequest(targetKlassId, false)).type();
    }

    public static ObjectMappingDTO getDefaultMapping(TypeDTO typeDTO) {
        return NncUtils.findRequired(
                typeDTO.getClassParam().mappings(),
                m -> m.id().equals(typeDTO.getClassParam().defaultMappingId())
        );
    }

    public static String getDefaultViewType(TypeDTO typeDTO) {
        return getDefaultMapping(typeDTO).targetType();
    }

    public static String getDefaultViewKlassId(TypeDTO typeDTO) {
        return TypeExpressions.extractKlassId(getDefaultViewType(typeDTO));
    }

    public static String getMethodIdByCode(TypeDTO typeDTO, String methodCode) {
        return NncUtils.findRequired(typeDTO.getClassParam().flows(), f -> methodCode.equals(f.code())).id();
    }

    public static MethodRefDTO getMethodRefByCode(TypeDTO typeDTO, String methodCode) {
        return new MethodRefDTO(
                TypeExpressions.getClassType(typeDTO.id()),
                getMethodIdByCode(typeDTO, methodCode),
                List.of()
        );
    }

    public static MethodRefDTO createMethodRef(String klassId, String methodId) {
        return new MethodRefDTO(TypeExpressions.getClassType(klassId), methodId, List.of());
    }

    public static FlowDTO getMethodByCode(TypeDTO typeDTO, String methodCode) {
        return NncUtils.findRequired(typeDTO.getClassParam().flows(), f -> methodCode.equals(f.code()));
    }

    public static String getStaticMethodIdByCode(TypeDTO typeDTO, String methodCode) {
        return NncUtils.findRequired(typeDTO.getClassParam().flows(),
                f -> methodCode.equals(f.code()) && ((MethodParam) f.param()).isStatic()
        ).id();
    }

    public static MethodRefDTO getStaticMethodRefByCode(TypeDTO typeDTO, String methodCode) {
        var method = NncUtils.findRequired(typeDTO.getClassParam().flows(),
                f -> methodCode.equals(f.code()) && ((MethodParam) f.param()).isStatic()
        );
        return createMethodRef(typeDTO.id(), method.id());
    }

    public static String getStaticMethod(TypeDTO typeDTO, String code, String... parameterTypes) {
        var paramTypeList = List.of(parameterTypes);
        return NncUtils.findRequired(typeDTO.getClassParam().flows(),
                f -> code.equals(f.code()) &&
                        ((MethodParam) f.param()).isStatic() &&
                        paramTypeList.equals(NncUtils.map(f.parameters(), ParameterDTO::type)),
                () -> new InternalException("Can not find static method " + code + "(" + String.join(",", paramTypeList) + ") in type " + typeDTO.name())
        ).id();
    }

    public static MethodRefDTO getStaticMethodRef(TypeDTO typeDTO, String code, String... parameterTypes) {
        return new MethodRefDTO(TypeExpressions.getClassType(typeDTO.id()), getStaticMethod(typeDTO, code, parameterTypes), List.of());
    }

    public static String getMethodId(TypeDTO typeDTO, String code, String... parameterTypeIds) {
        var paramTypeidList = List.of(parameterTypeIds);
        return NncUtils.findRequired(typeDTO.getClassParam().flows(),
                f -> code.equals(f.code()) && paramTypeidList.equals(
                        NncUtils.map(f.parameters(), ParameterDTO::type)
                )
        ).id();
    }

    public static MethodRefDTO getMethodRef(TypeDTO typeDTO, String code, String... parameterTypeIds) {
        return new MethodRefDTO(TypeExpressions.getClassType(typeDTO.id()), getMethodId(typeDTO, code, parameterTypeIds), List.of());
    }

    public static String getEnumConstantIdByName(TypeDTO typeDTO, String name) {
        return NncUtils.findRequired(typeDTO.getClassParam().enumConstants(), f -> name.equals(f.title())).id();
    }

    public static InstanceDTO getEnumConstantByName(TypeDTO typeDTO, String name) {
        return NncUtils.findRequired(typeDTO.getClassParam().enumConstants(), f -> name.equals(f.title()));
    }

    private static final Klass mockKlass = ClassTypeBuilder.newBuilder("TestUtilsMock", "TestUtilsMock").build();

    public static ClassType mockClassType() {
        return mockKlass.getType();
    }

    public static void printIdClassName(String id) {
        System.out.println(Id.parse(id).getClass().getName());
    }

}
