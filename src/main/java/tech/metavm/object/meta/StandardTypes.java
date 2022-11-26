package tech.metavm.object.meta;

public class StandardTypes {

    public static final Type OBJECT = new Type(
            "对象",
            null,
            TypeCategory.CLASS
    );

    public static final Type ENTITY = new Type(
            "实体", OBJECT, TypeCategory.CLASS
    );

    public static final Type ENUM = new Type(
            "枚举",
            OBJECT,
            TypeCategory.CLASS
    );

    public static final Type INT = new Type(
            "整数",
            OBJECT,
            TypeCategory.INT
    );

    public static final Type LONG = new Type(
            "长整数",
            OBJECT,
            TypeCategory.LONG
    );

    public static final Type DOUBLE = new Type(
            "浮点数",
            OBJECT,
            TypeCategory.DOUBLE
    );

    public static final Type BOOL = new Type(
            "布尔",
            OBJECT,
            TypeCategory.BOOL
    );

    public static final Type STRING = new Type(
            "字符串",
            OBJECT,
            TypeCategory.STRING
    );


    public static final Type PASSWORD = new Type(
            "密码",
            OBJECT,
            TypeCategory.PASSWORD
    );

    public static final Type TIME = new Type(
            "时间",
            OBJECT,
            TypeCategory.TIME
    );

    public static final Type ARRAY = new Type(
            "数组",
            OBJECT,
            TypeCategory.ARRAY
    );

    public static final Type NULL = new Type(
            "空",
            null,
            TypeCategory.NULL
    );

    static {
        OBJECT.initId(IdConstants.OBJECT);
        ENTITY.initId(IdConstants.ENTITY);
        ENUM.initId(IdConstants.ENUM.ID);
        STRING.initId(IdConstants.STRING);
        INT.initId(IdConstants.INT);
        LONG.initId(IdConstants.LONG);
        BOOL.initId(IdConstants.BOOL);
        TIME.initId(IdConstants.TIME);
        DOUBLE.initId(IdConstants.DOUBLE);
        ARRAY.initId(IdConstants.ARRAY);
        NULL.initId(IdConstants.NULL);
    }

    private StandardTypes() {}

}
