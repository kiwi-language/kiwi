package tech.metavm.util;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.persistence.*;

import java.util.*;

@SuppressWarnings("unused")
public class PersistenceUtil {

    public static final String KEY_ID = "id";

    public static final String KEY_KIND = "kind";

    public static final String KEY_TYPE_ID = "typeId";

    public static final String KEY_TITLE = "title";

    public static final String KEY_VALUE = "value";

    public static final String KEY_PARENT_ID = "parentId";

    public static final String KEY_PARENT_FIELD_ID = "parentFieldId";

//    public static List<InstancePO> convertForPersisting(Collection<InstancePO> records) {
//        return NncUtils.map(records, PersistenceUtil::convertForPersisting);
//    }

//    public static List<InstancePO> convertForLoading(Collection<? extends InstancePO> records) {
//        return NncUtils.map(records, PersistenceUtil::convertForLoading);
//    }

//    private static Map<String, Map<String, Object>> writeInstanceData(Map<String, Map<String, Object>> instanceData) {
//        Map<String, Map<String, Object>> dataForPersisting = new HashMap<>();
//        for (var entry : instanceData.entrySet()) {
//            Map<String, Object> subMapForPersisting = new HashMap<>();
//            dataForPersisting.put(entry.getKey(), subMapForPersisting);
//            entry.getValue().forEach((k, v) ->
//                    subMapForPersisting.put(k, writeValue(v))
//            );
//        }
//        return dataForPersisting;
//    }
//
//    public static InstancePO convertForPersisting(InstancePO instancePO) {
//        if (instancePO instanceof InstanceArrayPO arrayPO) {
//            List<Object> elementsForPersisting = new ArrayList<>();
//            for (Object element : arrayPO.getElements()) {
//                elementsForPersisting.add(writeValue(element));
//            }
//            return new InstanceArrayPO(
//                    arrayPO.getId(),
//                    arrayPO.getTypeId(),
//                    arrayPO.getTenantId(),
//                    arrayPO.getLength(),
//                    elementsForPersisting,
//                    arrayPO.getParentId(),
//                    arrayPO.getParentFieldId(),
//                    arrayPO.getVersion(),
//                    arrayPO.getSyncVersion()
//            );
//        } else {
//            return new InstancePO(
//                    instancePO.getTenantId(),
//                    instancePO.getId(),
//                    instancePO.getTypeId(),
//                    instancePO.getTitle(),
//                    writeInstanceData(instancePO.getData()),
//                    instancePO.getParentId(),
//                    instancePO.getParentFieldId(),
//                    instancePO.getVersion(),
//                    instancePO.getSyncVersion()
//            );
//        }
//    }

//    public static InstancePO convertForLoading(InstancePO instancePO) {
//        if (instancePO instanceof InstanceArrayPO arrayPO) {
//            List<Object> elements = new ArrayList<>();
//            for (Object element : arrayPO.getElements()) {
//                elements.add(readValue(instancePO.getTenantId(), element));
//            }
//            return new InstanceArrayPO(
//                    arrayPO.getId(),
//                    arrayPO.getTypeId(),
//                    arrayPO.getTenantId(),
//                    arrayPO.getLength(),
//                    elements,
//                    arrayPO.getParentId(),
//                    arrayPO.getParentFieldId(),
//                    arrayPO.getVersion(),
//                    arrayPO.getSyncVersion()
//            );
//        } else {
//            Map<String, Map<String, Object>> data = readInstanceData(instancePO.getTenantId(), instancePO.getData());
//            return new InstancePO(
//                    instancePO.getTenantId(),
//                    instancePO.getId(),
//                    instancePO.getTypeId(),
//                    instancePO.getTitle(),
//                    data,
//                    instancePO.getParentId(),
//                    instancePO.getParentFieldId(),
//                    instancePO.getVersion(),
//                    instancePO.getSyncVersion()
//            );
//        }
//    }

//    public static Object writeValue(Object value) {
//        return switch (value) {
//            case IdentityPO identityPO -> Map.of(
//                    KEY_KIND, ValueKind.REF.code,
//                    KEY_VALUE, identityPO.id()
//            );
//            case TimePO timePO -> Map.of(
//                    KEY_KIND, ValueKind.TIME.code,
//                    KEY_VALUE, timePO.time()
//            );
//            case PasswordPO passwordPO -> Map.of(KEY_KIND, ValueKind.PASSWORD.code, KEY_VALUE, passwordPO.value());
//            case InstanceArrayPO arrayPO -> {
//                List<Object> elements = arrayPO.getElements();
//                List<Object> elementCols = NncUtils.map(elements, PersistenceUtil::writeValue);
//                if (arrayPO.getId() != null) {
//                    yield Map.of(
//                            KEY_KIND, ValueKind.ARRAY.code,
//                            KEY_ID, arrayPO.getId(),
//                            KEY_TYPE_ID, arrayPO.getTypeId(),
//                            KEY_VALUE, elementCols,
//                            KEY_PARENT_ID, arrayPO.getParentId(),
//                            KEY_PARENT_FIELD_ID, arrayPO.getParentFieldId()
//                    );
//                } else {
//                    yield Map.of(
//                            KEY_KIND, ValueKind.ARRAY.code,
//                            KEY_TYPE_ID, arrayPO.getTypeId(),
//                            KEY_VALUE, elementCols,
//                            KEY_PARENT_ID, arrayPO.getParentId(),
//                            KEY_PARENT_FIELD_ID, arrayPO.getParentFieldId()
//                    );
//                }
//            }
//            case InstancePO instancePO -> {
//                Map<String, Map<String, Object>> data = writeInstanceData(instancePO.getData());
//                if (instancePO.getId() != null) {
//                    yield Map.of(
//                            KEY_KIND, ValueKind.INSTANCE.code,
//                            KEY_ID, instancePO.getId(),
//                            KEY_TITLE, instancePO.getTitle(),
//                            KEY_TYPE_ID, instancePO.getTypeId(),
//                            KEY_VALUE, data,
//                            KEY_PARENT_ID, instancePO.getParentId(),
//                            KEY_PARENT_FIELD_ID, instancePO.getParentFieldId()
//                    );
//                } else {
//                    yield Map.of(
//                            KEY_KIND, ValueKind.INSTANCE.code,
//                            KEY_TITLE, instancePO.getTitle(),
//                            KEY_TYPE_ID, instancePO.getTypeId(),
//                            KEY_VALUE, data,
//                            KEY_PARENT_ID, instancePO.getParentId(),
//                            KEY_PARENT_FIELD_ID, instancePO.getParentFieldId()
//                    );
//                }
//            }
//            case null, default -> value;
//        };
//    }

//    public static Object readValue(long tenantId, Object columnValue) {
//        if (columnValue == null) {
//            return null;
//        }
//        if (columnValue instanceof Map<?, ?> map) {
//            ValueWrap wrap = new ValueWrap(map);
//            ValueKind kind = ValueKind.getByCode(wrap.getIntRequired(KEY_KIND));
//            return switch (kind) {
//                case REF -> new IdentityPO(wrap.getLong(KEY_VALUE));
//                case ARRAY -> readArray(tenantId, wrap);
//                case INSTANCE -> readInstance(tenantId, wrap);
//                case TIME -> new TimePO(wrap.getLong(KEY_VALUE));
//                case PASSWORD -> new PasswordPO(wrap.getString(KEY_VALUE));
//            };
//        } else if (columnValue instanceof Integer integer) {
//            return integer.longValue();
//        } else {
//            return columnValue;
//        }
//    }

//    private static InstanceArrayPO readArray(long tenantId, ValueWrap wrap) {
//        Collection<?> elements = wrap.getArray(KEY_VALUE);
//        return new InstanceArrayPO(
//                wrap.getLong(KEY_ID),
//                wrap.getLong(KEY_TYPE_ID),
//                tenantId,
//                elements.size(),
//                NncUtils.map(elements, element -> readValue(tenantId, element)),
//                wrap.getLong(KEY_PARENT_ID),
//                wrap.getLong(KEY_PARENT_FIELD_ID),
//                0L,
//                0L
//        );
//    }

//    private static Map<String, Map<String, Object>> readInstanceData(long tenantId, Map<String, Map<String, Object>> data) {
//        var readData = new HashMap<String, Map<String, Object>>();
//        for (var entry : data.entrySet()) {
//            var subMap = new HashMap<String, Object>();
//            readData.put(entry.getKey(), subMap);
//            entry.getValue().forEach((k, v) -> subMap.put(k, readValue(tenantId, v)));
//        }
//        return readData;
//    }

//    private static InstancePO readInstance(long tenantId, ValueWrap wrap) {
//        Map<String, Map<String, Object>> value = wrap.get(new TypeReference<>() {
//        }, KEY_VALUE);
//        Map<String, Map<String, Object>> data = readInstanceData(tenantId, value);
//
//        return new InstancePO(
//                tenantId,
//                wrap.getLong(KEY_ID),
//                wrap.getLong(KEY_TYPE_ID),
//                wrap.getString(KEY_TITLE),
//                data,
//                wrap.getLong(KEY_PARENT_ID),
//                wrap.getLong(KEY_PARENT_FIELD_ID),
//                0L,
//                0L
//        );
//    }

    private enum ValueKind {
        REF(1),
        INSTANCE(2),
        ARRAY(3),
        TIME(4),
        PASSWORD(5),
        ;

        private final int code;

        ValueKind(int code) {
            this.code = code;
        }

        static ValueKind getByCode(int code) {
            return NncUtils.findRequired(values(), v -> v.code == code);
        }

    }

    private record ValueWrap(Map<?, ?> map) {

        int getIntRequired(String key) {
            return getRequired(Integer.class, key);
        }

        String getString(String key) {
            return get(String.class, key);
        }

        Long getLong(String key) {
            return get(Long.class, key);
        }

        Collection<?> getArray(String key) {
            return get(new TypeReference<>() {
            }, key);
        }

        Map<?, ?> getMap(String key) {
            return get(new TypeReference<>() {
            }, key);
        }

        <T> T get(TypeReference<T> typeReference, String key) {
            return get(typeReference.getType(), key);
        }


        <T> T get(Class<T> klass, String key) {
            return get(klass, key, false);
        }

        @NotNull <T> T getRequired(Class<T> klass, String key) {
            return NncUtils.requireNonNull(get(klass, key, true));
        }

        <T> T get(Class<T> klass, String key, boolean required) {
            Object value = map.get(key);
            if (value == null) {
                return null;
            }
            if (klass == Long.class) {
                if (ValueUtil.isInteger(value)) {
                    return klass.cast(((Number) value).longValue());
                }
            } else if (klass.isInstance(value)) {
                return klass.cast(value);
            }
            throw new InternalException("Value " + value + " is not instance of " + klass.getName());
        }
    }

}
