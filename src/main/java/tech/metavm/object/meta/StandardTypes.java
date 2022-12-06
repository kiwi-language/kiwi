package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.object.instance.Instance;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class StandardTypes {

    private final static List<Type> TYPES = new ArrayList<>();

    private final static List<Entity> ENTITIES = new ArrayList<>();

    public static final Type OBJECT = createType(
            IdConstants.OBJECT,
            "对象",
            null,
            TypeCategory.VALUE
    );

    public static final Type ENTITY = createType(
            IdConstants.ENTITY,
            "实体", OBJECT,
            TypeCategory.CLASS
    );

    public static final Type RECORD = createType(
            IdConstants.RECORD,
            "对象",
            null,
            TypeCategory.VALUE
    );


    public static final Type ENUM = createType(
            IdConstants.ENUM.ID,
            "枚举",
            OBJECT,
            TypeCategory.VALUE
    );

    public static final Type INT = createType(
            IdConstants.INT,
            "整数",
            OBJECT,
            TypeCategory.INT
    );

    public static final Type LONG = createType(
            IdConstants.LONG,
            "长整数",
            OBJECT,
            TypeCategory.LONG
    );

    public static final Type DOUBLE = createType(
            IdConstants.DOUBLE,
            "浮点数",
            OBJECT,
            TypeCategory.DOUBLE
    );

    public static final Type BOOL = createType(
            IdConstants.BOOL,
            "布尔",
            OBJECT,
            TypeCategory.BOOL
    );

    public static final Type STRING = createType(
            IdConstants.STRING,
            "字符串",
            OBJECT,
            TypeCategory.STRING
    );

    public static final Type PASSWORD = createType(
            IdConstants.PASSWORD,
            "密码",
            OBJECT,
            TypeCategory.PASSWORD
    );

    public static final Type TIME = createType(
            IdConstants.TIME,
            "时间",
            OBJECT,
            TypeCategory.TIME
    );

    public static final Type ARRAY = createType(
            IdConstants.ARRAY,
            "数组",
            OBJECT,
            TypeCategory.ARRAY
    );

    public static final Type NULL = createType(
            IdConstants.NULL,
            "空",
            null,
            TypeCategory.NULL
    );

    public static final Field ENUM_NAME = createField(
            IdConstants.ENUM.NAME_ID,
            "名称",
            ENUM,
            STRING,
            true
    );

    public static final Field ENUM_ORDINAL = createField(
            IdConstants.ENUM.ORDINAL_ID,
            "序号",
            ENUM,
            INT,
            false
    );

    private StandardTypes() {}

    private static Type createType(long id, String name, Type superType, TypeCategory category) {
        Type type = new Type(name, superType, category);
        type.initId(id);
        TYPES.add(type);
        ENTITIES.add(type);
        return type;
    }

    private static Field createField(long id, String name, Type declaringType, Type type, boolean asTitle) {
        Field field = new Field(
                name,
                declaringType,
                Access.GLOBAL,
                false,
                asTitle,
                null,
                type,
                false
        );
        field.initId(id);
        ENTITIES.add(field);
        return field;
    }

    public static List<Entity> entities() {
        return ENTITIES;
    }

    public static final Set<Object> BLACK_MODELS = new IdentitySet<>();

    public static final Set<Instance> BLACK_INSTANCES = new IdentitySet<>();

    static {
        long nextArrayId = IdConstants.ARRAY_REGION_BASE;
        for (Type type : TYPES) {
            type.getDeclaredFields().initId(nextArrayId++);
            type.getDeclaredConstraints().initId(nextArrayId++);
            BLACK_MODELS.add(type.getDeclaredFields());
            BLACK_MODELS.add(type.getDeclaredConstraints());
        }
    }

}
