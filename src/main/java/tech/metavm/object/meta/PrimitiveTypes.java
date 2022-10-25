package tech.metavm.object.meta;

import tech.metavm.object.instance.SQLColumnType;
import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class PrimitiveTypes {

    public static final PrimitiveType OBJECT = PrimitiveTypeFactory.create(1, "对象", null);

    public static final PrimitiveType INT = PrimitiveTypeFactory.create(4, "INT", SQLColumnType.INT32);

    public static final PrimitiveType LONG = PrimitiveTypeFactory.create(5, "整数", SQLColumnType.INT64);

    public static final PrimitiveType DOUBLE = PrimitiveTypeFactory.create(7, "数值", SQLColumnType.FLOAT);

    public static final PrimitiveType BOOL = PrimitiveTypeFactory.create(8, "是/否", SQLColumnType.BOOL);

    public static final PrimitiveType STRING = PrimitiveTypeFactory.create(9, "文本", SQLColumnType.VARCHAR64);

    public static final PrimitiveType TIME = PrimitiveTypeFactory.create(10, "时间", SQLColumnType.INT64);

    public static final PrimitiveType DATE = PrimitiveTypeFactory.create(11, "日期", SQLColumnType.INT64);

    public static final PrimitiveType ARRAY = PrimitiveTypeFactory.create(18, "数组", null);

    public static final PrimitiveType NULLABLE = PrimitiveTypeFactory.create(19, "可空", null);

    private static final Map<Long, PrimitiveType> MAP;

    static {
        Map<Long, PrimitiveType> map = new HashMap<>();
        java.lang.reflect.Field[] fields = PrimitiveTypes.class.getFields();
        for (Field field : fields) {
            if(field.getType() == PrimitiveType.class && Modifier.isStatic(field.getModifiers())) {
                PrimitiveType typeDTO = (PrimitiveType) ReflectUtils.get(null, field);
                map.put(typeDTO.id(), typeDTO);
            }
        }
        MAP = Collections.unmodifiableMap(map);
    }

    public static PrimitiveType get(long id) {
        PrimitiveType typeDTO = MAP.get(id);
        Objects.requireNonNull(typeDTO, "Primitive type not found for id: " + id);
        return typeDTO;
    }

    public static boolean isPrimitiveTypeId(long id) {
        return MAP.containsKey(id);
    }

}
