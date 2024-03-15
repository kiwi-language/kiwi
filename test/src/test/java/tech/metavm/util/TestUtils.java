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
import tech.metavm.flow.rest.MethodParam;
import tech.metavm.flow.rest.ParameterDTO;
import tech.metavm.object.instance.InstanceManager;
import tech.metavm.object.instance.cache.MockCache;
import tech.metavm.object.instance.core.*;
import tech.metavm.object.instance.log.InstanceLogService;
import tech.metavm.object.instance.persistence.mappers.IndexEntryMapper;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.Types;
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

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private static final char[] buf = new char[64 * 1024 * 1024];

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new TypeReference<Class<?>>() {
        }.getType(), new ReflectClassSerializer());
        module.addSerializer(Field.class, new ReflectFieldSerializer());
        OBJECT_MAPPER.registerModule(module);
        OBJECT_MAPPER.registerModule(new Jdk8Module());
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
        OBJECT_MAPPER.registerModule(new ParameterNamesModule(JsonCreator.Mode.PROPERTIES));
        OBJECT_MAPPER.enable(SerializationFeature.INDENT_OUTPUT);
        OBJECT_MAPPER.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    }

    public static String toJSONString(Object object) {
        try {
            return OBJECT_MAPPER.writeValueAsString(object);
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
        if(value instanceof ListFieldValue listFieldValue)
            return listFieldValue.getElements().get(index);
        else if(value instanceof InstanceFieldValue instanceFieldValue)
            return ((ListInstanceParam) instanceFieldValue.getInstance().param()).elements().get(index);
        else
            throw new InternalException("Invalid value: " + value);
    }

    public static <T> T readJSON(Class<T> klass, String json) {
        try {
            return OBJECT_MAPPER.readValue(json, klass);
        } catch (JsonProcessingException e) {
            throw new InternalException("Fail to read JSON '" + json + "'", e);
        }
    }

    public static <T> T readJSON(Class<T> klass, Reader reader) {
        try {
            return OBJECT_MAPPER.readValue(reader, klass);
        } catch (IOException e) {
            throw new InternalException("Fail to read JSON", e);
        }
    }

    public static void printJSON(Object object) {
        try {
            OBJECT_MAPPER.writeValue(System.out, object);
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    public static <R> R parseJson(String jsonStr, Class<R> klass) {
        return NncUtils.readJSONString(jsonStr, klass);
    }

    public static <R> R readJson(String path, Class<R> klass) {
        try {
            return OBJECT_MAPPER.readValue(readEntireFile(path), klass);
        } catch (JsonProcessingException e) {
            throw new InternalException("JSON deserialization failed", e);
        }
    }

    public static <R> R readJson(String path, com.fasterxml.jackson.core.type.TypeReference<R> typeRef) {
        try {
            return OBJECT_MAPPER.readValue(readEntireFile(path), typeRef);
        } catch (JsonProcessingException e) {
            throw new InternalException("JSON deserialization failed", e);
        }
    }

    public static String readEntireFile(String path) {
        try (FileReader reader = new FileReader(path)) {
            int n = reader.read(buf);
            return new String(buf, 0, n);
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

    public static void initEntityIds(Object root) {
        var ref = new Object() {
            long nextId = 10000L;
        };

        EntityUtils.visitGraph(List.of(root), o -> {
            if (o instanceof Entity entity && entity.isIdNull()) {
                var typeId = Types.getType(entity).getPhysicalId();
                var typeTag = entity instanceof ReadonlyArray<?> ? TypeTag.Array : TypeTag.Class;
                entity.initId(PhysicalId.of(ref.nextId++, typeTag, typeId));
            }
        });
    }

    public static void initEntityIds(Object root, EntityIdProvider idProvider) {
        EntityUtils.visitGraph(List.of(root), o -> {
            if (o instanceof Entity entity && entity.isIdNull()) {
                var type = ModelDefRegistry.getDefContext().getType(EntityUtils.getRealType(entity.getClass()));
                entity.initId(PhysicalId.of(idProvider.allocateOne(TestConstants.APP_ID, type), type));
            }
        });
    }

    public static void initInstanceIds(DurableInstance instance) {
        initInstanceIds(List.of(instance));
    }

    public static void initInstanceIds(List<DurableInstance> instances) {
        initInstanceIds(instances, new MockIdProvider());
    }

    public static void initInstanceIds(List<DurableInstance> instances, EntityIdProvider idProvider) {
//        var ref = new Object() {
//            long nextObjectId = IdConstants.CLASS_REGION_BASE + offset;
//            long nextEnumId = IdConstants.ENUM_REGION_BASE + offset;
//            long nextReadWriteArrayId = IdConstants.READ_WRITE_ARRAY_REGION_BASE + offset;
//            long nextChildArrayId = IdConstants.CHILD_ARRAY_REGION_BASE + offset;
//            long nextReadonlyArrayId = IdConstants.READ_ONLY_ARRAY_REGION_BASE + offset;
//        };
        var visitor = new GraphVisitor() {
            @Override
            public Void visitDurableInstance(DurableInstance instance) {
                if (!instance.isIdInitialized()) {
                    var id = idProvider.allocateOne(TestConstants.APP_ID, instance.getType());
//                    if (instance instanceof ArrayInstance arrayInstance) {
//                        id = switch (arrayInstance.getType().getKind()) {
//                            case READ_WRITE -> ref.nextReadWriteArrayId++;
//                            case CHILD -> ref.nextChildArrayId++;
//                            case READ_ONLY -> ref.nextReadonlyArrayId++;
//                        };
//                    } else if (instance instanceof ClassInstance classInstance) {
//                        var type = classInstance.getType();
//                        if (type.isEnum())
//                            id = ref.nextEnumId++;
//                        else
//                            id = ref.nextObjectId++;
//                    } else
//                        throw new InternalException("Invalid instance: " + instance);
                    instance.initId(PhysicalId.of(id, instance.getType()));
                }
                return super.visitDurableInstance(instance);
            }
        };
        instances.forEach(visitor::visit);
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
        }
        else if(instanceDTO.param() instanceof ListInstanceParam listInstanceParam) {
            for (FieldValue element : listInstanceParam.elements()) {
                if (element instanceof InstanceFieldValue instanceFieldValue)
                    extractDescendantIds(instanceFieldValue.getInstance(), ids);
            }
        }
    }

    public static String getFieldIdByCode(TypeDTO typeDTO, String fieldCode) {
        return NncUtils.findRequired(typeDTO.getClassParam().fields(), f -> fieldCode.equals(f.code())).id();
    }

    public static ObjectMappingDTO getDefaultMapping(TypeDTO typeDTO) {
        return NncUtils.findRequired(
                typeDTO.getClassParam().mappings(),
                m -> m.id().equals(typeDTO.getClassParam().defaultMappingId())
        );
    }

    public static String getDefaultViewTypeId(TypeDTO typeDTO) {
        return getDefaultMapping(typeDTO).targetTypeId();
    }

    public static String getMethodIdByCode(TypeDTO typeDTO, String methodCode) {
        return NncUtils.findRequired(typeDTO.getClassParam().flows(), f -> methodCode.equals(f.code())).id();
    }

    public static String getStaticMethodIdByCode(TypeDTO typeDTO, String methodCode) {
        return NncUtils.findRequired(typeDTO.getClassParam().flows(),
                f -> methodCode.equals(f.code()) && ((MethodParam) f.param()).isStatic()
        ).id();
    }

    public static String getStaticMethod(TypeDTO typeDTO, String code, String...parameterTypeIds) {
        var paramTypeRefList = List.of(parameterTypeIds);
        return NncUtils.findRequired(typeDTO.getClassParam().flows(),
                f -> code.equals(f.code()) &&
                        ((MethodParam) f.param()).isStatic() &&
                        paramTypeRefList.equals(NncUtils.map(f.parameters(), ParameterDTO::typeId))
        ).id();
    }

    public static String getMethodId(TypeDTO typeDTO, String code, String... parameterTypeIds) {
        var paramTypeidList = List.of(parameterTypeIds);
        return NncUtils.findRequired(typeDTO.getClassParam().flows(),
                f -> code.equals(f.code()) && paramTypeidList.equals(
                        NncUtils.map(f.parameters(), ParameterDTO::typeId)
                )
        ).id();
    }

    public static String getEnumConstantIdByName(TypeDTO typeDTO, String name) {
        return NncUtils.findRequired(typeDTO.getClassParam().enumConstants(), f -> name.equals(f.title())).id();
    }

    public static InstanceDTO getEnumConstantByName(TypeDTO typeDTO, String name) {
        return NncUtils.findRequired(typeDTO.getClassParam().enumConstants(), f -> name.equals(f.title()));
    }
}
