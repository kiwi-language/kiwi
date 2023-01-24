package tech.metavm.util;

import org.jetbrains.annotations.NotNull;
import tech.metavm.object.instance.persistence.InstanceArrayPO;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.IdentityPO;
import tech.metavm.object.instance.persistence.TimePO;

import java.util.*;

@SuppressWarnings("unused")
public class PersistenceUtil {

    public static final String KEY_ID = "id";

    public static final String KEY_KIND = "kind";

    public static final String KEY_TYPE_ID = "typeId";

    public static final String KEY_TITLE = "title";

    public static final String KEY_VALUE = "value";

    public static List<InstancePO> convertForPersisting(Collection<InstancePO> records) {
        return NncUtils.map(records ,PersistenceUtil::convertForPersisting);
    }

    public static List<InstancePO> convertForLoading(Collection<? extends InstancePO> records) {
        return NncUtils.map(records, PersistenceUtil::convertForLoading);
    }

    public static InstancePO convertForPersisting(InstancePO instancePO) {
        if(instancePO instanceof InstanceArrayPO arrayPO) {
            List<Object> elementsForPersisting = new ArrayList<>();
            for (Object element : arrayPO.getElements()) {
                elementsForPersisting.add(writeValue(element));
            }
            return new InstanceArrayPO(
                    arrayPO.getId(),
                    arrayPO.getTypeId(),
                    arrayPO.getTenantId(),
                    arrayPO.getLength(),
                    elementsForPersisting,
                    arrayPO.isElementAsChild(),
                    arrayPO.getVersion(),
                    arrayPO.getSyncVersion()
            );
        }
        else {
            Map<String, Object> dataForPersisting = new HashMap<>();
            instancePO.getData().forEach((k, v) ->
                    dataForPersisting.put(k, writeValue(v))
            );
            return new InstancePO(
                    instancePO.getTenantId(),
                    instancePO.getId(),
                    instancePO.getTypeId(),
                    instancePO.getTitle(),
                    dataForPersisting,
                    instancePO.getVersion(),
                    instancePO.getSyncVersion()
            );
        }
    }

    public static InstancePO convertForLoading(InstancePO instancePO) {
        if(instancePO instanceof InstanceArrayPO arrayPO) {
            List<Object> elements = new ArrayList<>();
            for (Object element : arrayPO.getElements()) {
                elements.add(readValue(instancePO.getTenantId(), element));
            }
            return new InstanceArrayPO(
                    arrayPO.getId(),
                    arrayPO.getTypeId(),
                    arrayPO.getTenantId(),
                    arrayPO.getLength(),
                    elements,
                    arrayPO.isElementAsChild(),
                    arrayPO.getVersion(),
                    arrayPO.getSyncVersion()
            );
        }
        else {
            Map<String, Object> data = new HashMap<>();
            instancePO.getData().forEach((k, v) ->
                data.put(k, readValue(instancePO.getTenantId(), v))
            );
            return new InstancePO(
                    instancePO.getTenantId(),
                    instancePO.getId(),
                    instancePO.getTypeId(),
                    instancePO.getTitle(),
                    data,
                    instancePO.getVersion(),
                    instancePO.getSyncVersion()
            );
        }
    }

    public static Object writeValue(Object value) {
        if(value instanceof IdentityPO identityPO) {
            return Map.of(
                    KEY_KIND, ValueKind.REF.code,
                    KEY_VALUE, identityPO.id()
            );
        }
        else if(value instanceof TimePO timePO) {
            return Map.of(
                    KEY_KIND, ValueKind.TIME.code,
                    KEY_VALUE, timePO.time()
            );
        }
        else if(value instanceof InstanceArrayPO arrayPO) {
            List<Object> elements = arrayPO.getElements();
            List<Object> elementCols = NncUtils.map(elements, PersistenceUtil::writeValue);
            if(arrayPO.getId() != null) {
                return Map.of(
                        KEY_KIND, ValueKind.ARRAY.code,
                        KEY_ID, arrayPO.getId(),
                        KEY_TYPE_ID, arrayPO.getTypeId(),
                        KEY_VALUE, elementCols
                );
            }
            else {
                return Map.of(
                        KEY_KIND, ValueKind.ARRAY.code,
                        KEY_TYPE_ID, arrayPO.getTypeId(),
                        KEY_VALUE, elementCols
                );
            }
        }
        else if(value instanceof InstancePO instancePO) {
            Map<String, Object> data = instancePO.getData();
            Map<String, Object> columValues = new HashMap<>();

            data.forEach((col, v) ->
                    columValues.put(col, writeValue(v))
            );
            if(instancePO.getId() != null) {
                return Map.of(
                        KEY_KIND, ValueKind.INSTANCE.code,
                        KEY_ID, instancePO.getId(),
                        KEY_TITLE, instancePO.getTitle(),
                        KEY_TYPE_ID, instancePO.getTypeId(),
                        KEY_VALUE, columValues
                );
            }
            else {
                return Map.of(
                        KEY_KIND, ValueKind.INSTANCE.code,
                        KEY_TITLE, instancePO.getTitle(),
                        KEY_TYPE_ID, instancePO.getTypeId(),
                        KEY_VALUE, columValues
                );
            }
        }
        else {
            return value;
        }
    }

    public static Object readValue(long tenantId, Object columnValue) {
        if(columnValue == null) {
            return null;
        }
        if(columnValue instanceof Map<?,?> map) {
            ValueWrap wrap = new ValueWrap(map);
            ValueKind kind = ValueKind.getByCode(wrap.getIntRequired(KEY_KIND));
            return switch (kind) {
                case REF ->  new IdentityPO(wrap.getLong(KEY_VALUE));
                case ARRAY -> readArray(tenantId, wrap);
                case INSTANCE -> readInstance(tenantId, wrap);
                case TIME -> new TimePO(wrap.getLong(KEY_VALUE));
            };
        }
        else {
            return columnValue;
        }
    }

    public static InstanceArrayPO readArray(long tenantId, ValueWrap wrap) {
        Collection<?> elements = wrap.getArray(KEY_VALUE);
        return new InstanceArrayPO(
                wrap.getLong(KEY_ID),
                wrap.getLong(KEY_TYPE_ID),
                tenantId,
                elements.size(),
                NncUtils.map(elements, element -> readValue(tenantId, element)),
                false,
                0L,
                0L
        );
    }

    public static InstancePO readInstance(long tenantId, ValueWrap wrap) {
        Map<String, Object> value = wrap.get(new TypeReference<>() {}, KEY_VALUE);
        Map<String, Object> data = new HashMap<>();

        value.forEach((k ,v) ->
                data.put(k, readValue(tenantId, v))
        );

        return new InstancePO(
                tenantId,
                wrap.getLong(KEY_ID),
                wrap.getLong(KEY_TYPE_ID),
                wrap.getString(KEY_TITLE),
                data,
                0L,
                0L
        );
    }

    private enum ValueKind {
        REF(1),
        INSTANCE(2),
        ARRAY(3),
        TIME(4),
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
            if(value == null) {
                return null;
            }
            if(klass == Long.class) {
                if(ValueUtil.isInteger(value)) {
                    return klass.cast(((Number) value).longValue());
                }
            }
            else if (klass.isInstance(value)) {
                return klass.cast(value);
            }
            throw new InternalException("Value " + value + " is not instance of " + klass.getName());
        }
    }

}
