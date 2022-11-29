package tech.metavm.object.meta;

import tech.metavm.entity.EntityIdProvider;
import tech.metavm.entity.EntityTypeRegistry;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.WildcardType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static tech.metavm.util.ReflectUtils.getQualifiedFieldName;

public class StdAllocators implements EntityIdProvider {

    private static final String ID_FILE_DIR = "/id";
    private static final Pattern ID_FILE_NAME_PTN = Pattern.compile("([^.\\s]+)\\.properties");
    private static final long NUM_IDS_PER_ALLOCATOR = 1000L;

    private final Map<Class<?>, StdAllocator> allocatorMap = new HashMap<>();
    private long nextBaseId = 1000L;
    private final String saveDir;

    public StdAllocators(String saveDir) {
        this.saveDir = saveDir;
        try(InputStream input = StdAllocators.class.getResourceAsStream(ID_FILE_DIR)) {
            if(input != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(input));
                String line;
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = ID_FILE_NAME_PTN.matcher(line);
                    if (matcher.matches()) {
                        StdAllocator allocator = new StdAllocator(saveDir, ID_FILE_DIR + "/" + line);
                        allocatorMap.put(allocator.getJavaType(), allocator);
                        nextBaseId = Math.max(nextBaseId, allocator.getBase() + NUM_IDS_PER_ALLOCATOR);
                    }
                }
            }
        } catch (IOException e) {
            throw new InternalException("Fail to read id files", e);
        }
    }

    public Long getId(Object object) {
        if(object instanceof java.lang.reflect.Field field) {
            return getId0(Field.class, getQualifiedFieldName(field));
        }
        if(object instanceof java.lang.reflect.Type type) {
            return getId0(Type.class, getTypeCode(type));
        }
        if(object instanceof Enum<?> enumConstant) {
            return getId0(enumConstant.getClass(), enumConstant.name());
        }
        throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
    }

    public void putId(Object object, long id) {
        if(object instanceof java.lang.reflect.Field field) {
            putId0(Field.class, getQualifiedFieldName(field), id);
        }
        else if(object instanceof java.lang.reflect.Type type) {
            putId0(Type.class, getTypeCode(type), id);
        }
        else if(object instanceof Enum<?> enumConstant) {
            putId0(enumConstant.getClass(), enumConstant.name(), id);
        }
        else {
            throw new InternalException("Can not allocate id for object: " + object + ". Unsupported type.");
        }
    }

    private Long getId0(Class<?> entityType, String entityCode) {
        StdAllocator allocator = getAllocator(entityType);
        return allocator.getId(entityCode);
    }

    private void putId0(Class<?> entityType, String entityCode, long id) {
        allocatorMap.get(entityType).putId(entityCode, id);
    }

    @Override
    public long getTypeId(long id) {
        StdAllocator typeAllocator = getTypeAllocator();
        for (StdAllocator allocator : allocatorMap.values()) {
            if(allocator.contains(id)) {
                return typeAllocator.getId(allocator.getJavaType().getName());
            }
        }
        throw new InternalException("Can not found typeId for id: " + id);
    }

    @Override
    public Map<Type, List<Long>> allocate(long tenantId, Map<Type, Integer> typeId2count) {
        Map<Type, List<Long>> result = new HashMap<>();
        typeId2count.forEach((type, count) -> {
            Class<?> javaType = EntityTypeRegistry.getJavaType(type);
            result.put(type, getAllocator(javaType).allocate(count));
        });
        return result;
    }

    private StdAllocator getAllocator(Class<?> javaType) {
        return allocatorMap.computeIfAbsent(javaType, this::createAllocator);
    }

    private StdAllocator createAllocator(Class<?> javaType) {
        StdAllocator allocator =  new StdAllocator(
                saveDir,
                getIdFileName(javaType.getSimpleName()),
                javaType,
                nextBaseId
        );
        nextBaseId += NUM_IDS_PER_ALLOCATOR;
        return allocator;
    }

    private static String getIdFileName(String code) {
        return ID_FILE_DIR + "/" + code + ".properties";
    }

    private StdAllocator getTypeAllocator() {
        return allocatorMap.get(Type.class);
    }

    private String getTypeCode(java.lang.reflect.Type type) {
        if(type instanceof Class<?> klass) {
            return klass.getName();
        }
        if(type instanceof ParameterizedType pType) {
            return getTypeCode(pType.getRawType()) + "<" +
                    NncUtils.join(pType.getActualTypeArguments(), this::getTypeCode) + ">";
        }
        if(type instanceof WildcardType wildcardType) {
            if(ReflectUtils.isAllWildCardType(wildcardType)) {
                return "?";
            }
        }
        throw new InternalException("Can not get code for type: " + type);
    }

    public void save() {
        for (StdAllocator allocator : allocatorMap.values()) {
            allocator.save();
        }
    }

}
