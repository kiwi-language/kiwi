package tech.metavm.entity;

import tech.metavm.object.instance.ModelInstanceMap;

import java.util.Arrays;
import java.util.function.Function;

public class EnumParser<T extends Enum<?>> {

    public static <T extends Enum<?>> EnumDef<T> parse(Class<T> enumType, ValueDef<Enum<?>> parentDef, Function<Object, Long> getId, ModelInstanceMap modelInstanceMap, DefMap defMap) {
        return new EnumParser<>(enumType, parentDef, getId, modelInstanceMap, defMap).parse();
    }

    private final Class<T> enumType;
    private final ValueDef<Enum<?>> parentDef;
    private EnumDef<T> enumDef;
    private final DefMap defMap;
    private final Function<Object, Long> getId;
    private final ModelInstanceMap modelInstanceMap;

    public EnumParser(Class<T> enumType, ValueDef<Enum<?>> parentDef, Function<Object, Long> getId, ModelInstanceMap modelInstanceMap, DefMap defMap) {
        this.enumType = enumType;
        this.parentDef = parentDef;
        this.getId = getId;
        this.modelInstanceMap = modelInstanceMap;
        this.defMap = defMap;
    }

    public EnumDef<T> parse() {
        Long enumTypeId = getId.apply(enumType);
        enumDef =  new EnumDef<>(
                enumType,
                parentDef,
                enumTypeId
        );
        Arrays.stream(enumType.getEnumConstants()).forEach(ec -> parseEnumConstant(ec, enumDef));
        defMap.addDef(enumDef);
        return enumDef;
    }

    private void parseEnumConstant(T value, EnumDef<T> enumDef) {
        Long enumConstantId = getId.apply(value);
//        EnumConstantRT enumConstant = NncUtils.get(instance, modelMap::getEnumConstant);
        new EnumConstantDef<>(value, enumDef, enumConstantId);
    }

}
