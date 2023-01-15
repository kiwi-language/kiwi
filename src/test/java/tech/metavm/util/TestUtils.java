package tech.metavm.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.slf4j.Logger;
import tech.metavm.entity.*;
import tech.metavm.object.instance.*;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.persistence.mappers.InstanceMapperGateway;
import tech.metavm.object.instance.persistence.mappers.MemInstanceArrayMapper;
import tech.metavm.object.instance.persistence.mappers.MemInstanceMapper;
import tech.metavm.object.instance.persistence.mappers.MemReferenceMapper;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Field;
import java.util.List;

public class TestUtils {

    public static final String TEST_RESOURCE_ROOT = "/Users/leen/workspace/object/src/test/resources";

    public static final String TEST_RESOURCE_TARGET_ROOT = "/Users/leen/workspace/object/target/test-classes";

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addSerializer(new TypeReference<Class<?>>(){}.getType(), new ReflectClassSerializer());
        module.addSerializer(Field.class, new ReflectFieldSerializer());
        OBJECT_MAPPER.registerModule(module);
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

    public static void clearTestResourceDir(String dirName) {
        File dir = new File(TEST_RESOURCE_ROOT + "/" + dirName);
        if(dir.listFiles() != null) {
            for (File file : NncUtils.requireNonNull(dir.listFiles())) {
                if (!file.delete()) {
                    throw new InternalException("Fail to delete file " + file.getPath());
                }
            }
        }

        File targetDir = new File(TEST_RESOURCE_TARGET_ROOT + "/" + dirName);
        if(targetDir.listFiles() != null) {
            for (File file : NncUtils.requireNonNull(targetDir.listFiles())) {
                if (!file.delete()) {
                    throw new InternalException("Fail to delete file " + file.getPath());
                }
            }
        }
    }

    public static void logJSON(Logger logger, Object object) {
        logJSON(logger, "JSON", object);
    }

    public static void logJSON(Logger logger, String title, Object object) {
        logger.info(title + "\n" + toJSONString(object));
    }

    public static InstanceContextFactory getInstanceContextFactory(EntityIdProvider idProvider) {
        return getInstanceContextFactory(idProvider, new MemInstanceStore());
    }

    public static InstanceContextFactory getInstanceContextFactory(EntityIdProvider idProvider, MemInstanceStore instanceStore) {
        return getInstanceContextFactory(idProvider, instanceStore, new MemInstanceSearchService());
    }

    public static InstanceContextFactory getInstanceContextFactory(EntityIdProvider idProvider,
                                                                   MemInstanceStore instanceStore,
                                                                   MemInstanceSearchService instanceSearchService) {
        InstanceContextFactory instanceContextFactory = new InstanceContextFactory(instanceStore)
                .setIdService(idProvider).setDefaultAsyncProcessing(false);
        InstanceContextFactory.setStdContext(MockRegistry.getInstanceContext());
        instanceContextFactory.setPlugins(List.of(
                new CheckConstraintPlugin(),
                new IndexConstraintPlugin(instanceStore.getIndexEntryMapper()),
                new ChangeLogPlugin(new InstanceLogServiceImpl(
                        instanceSearchService,
                        instanceContextFactory,
                        instanceStore
                ))
        ));
        return instanceContextFactory;
    }

    public static IInstanceStore getMemInstanceStore() {
        return new InstanceStore(
                new InstanceMapperGateway(
                        new MemInstanceMapper(),
                        new MemInstanceArrayMapper()
                ),
                new MemIndexEntryMapper(),
                new MemReferenceMapper()
        );
    }

}
