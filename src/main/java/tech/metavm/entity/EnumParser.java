package tech.metavm.entity;

import tech.metavm.object.meta.TypeFactory;
import tech.metavm.util.ReflectUtils;

import java.util.Arrays;

public class EnumParser<T extends Enum<?>> {

    public static <T extends Enum<?>> EnumDef<T> parse(Class<T> enumType, ValueDef<Enum<?>> parentDef, DefMap defMap) {
        return new EnumParser<>(enumType, parentDef, defMap).parse();
    }

    private final Class<T> enumType;
    private final ValueDef<Enum<?>> parentDef;
    private EnumDef<T> enumDef;
    private final DefMap defMap;
    private final TypeFactory typeFactory;

    public EnumParser(Class<T> enumType, ValueDef<Enum<?>> parentDef, DefMap defMap) {
        this.enumType = enumType;
        this.parentDef = parentDef;
        this.defMap = defMap;
         typeFactory = new TypeFactory(defMap::getType);
    }

    public EnumDef<T> parse() {
        enumDef =  new EnumDef<>(
                enumType,
                parentDef,
                typeFactory.createEnum(ReflectUtils.getMetaTypeName(enumType), false, parentDef.getType())
        );
        Arrays.stream(enumType.getEnumConstants()).forEach(ec -> parseEnumConstant(ec, enumDef));
        defMap.addDef(enumDef);
        return enumDef;
    }

    private void parseEnumConstant(T value, EnumDef<T> enumDef) {
        new EnumConstantDef<>(value, enumDef);
    }

}
