package tech.metavm.util;

import tech.metavm.object.meta.*;

import java.util.List;

public class UTTypes {

    public static final Type FOO = new Type(
            "傻瓜", StandardTypes.ENTITY, TypeCategory.CLASS
    );

    public static final Type BAR = new Type(
            "吧", StandardTypes.ENTITY, TypeCategory.CLASS
    );

    public static final Type BAZ = new Type(
            "巴子", StandardTypes.ENTITY, TypeCategory.CLASS
    );

    public static class ArrayTypes {
        public static final Type BAR_ARRAY = new Type(
                "巴[]",
                StandardTypes.OBJECT,
                TypeCategory.ARRAY,
                false,
                false,
                StandardTypes.ARRAY,
                List.of(BAR),
                null,
                null
        );
    }

    public static class Fields {

        public static final Field FOO_NAME = new Field(
                "名称",
                FOO,
                Access.GLOBAL,
                false,
                true,
                null,
                StandardTypes.STRING,
                false
        );

        public static final Field FOO_BAR = new Field(
                "吧",
                FOO,
                Access.GLOBAL,
                false,
                false,
                null,
                BAR,
                true
        );

        public static final Field BAR_CODE = new Field(
                "编号",
                BAR,
                Access.GLOBAL,
                true,
                true,
                null,
                StandardTypes.STRING,
                false
        );

        public static final Field BAZ_BARS = new Field(
                "巴巴巴巴",
                BAZ,
                Access.GLOBAL,
                true,
                true,
                null,
                ArrayTypes.BAR_ARRAY,
                true
        );

    }


}
