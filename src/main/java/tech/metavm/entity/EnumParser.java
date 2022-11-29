package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.EnumConstantRT;
import tech.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.function.Function;

public class EnumParser<T extends Enum<?>> {

    public static <T extends Enum<?>> EnumDef<T> parse(Class<T> enumType, ValueDef<Enum<?>> parentDef, Function<Object, Instance> getInstance, ModelMap modelMap, DefMap defMap) {
        return new EnumParser<>(enumType, parentDef, getInstance, modelMap, defMap).parse();
    }

    private final Class<T> enumType;
    private final ValueDef<Enum<?>> parentDef;
    private EnumDef<T> enumDef;
    private DefMap defMap;
    private final Function<Object, Instance> getInstance;
    private final ModelMap modelMap;

    public EnumParser(Class<T> enumType, ValueDef<Enum<?>> parentDef, Function<Object, Instance> getInstance, ModelMap modelMap, DefMap defMap) {
        this.enumType = enumType;
        this.parentDef = parentDef;
        this.getInstance = getInstance;
        this.modelMap = modelMap;
        this.defMap = defMap;
    }

    public EnumDef<T> parse() {
        Instance enumTypeInst = getInstance.apply(enumType);
        enumDef =  new EnumDef<>(
                enumType,
                parentDef,
                NncUtils.get(enumTypeInst, modelMap::getType)
        );
        defMap.putDef(enumType, enumDef);
        Arrays.stream(enumType.getEnumConstants()).forEach(ec -> parseEnumConstant(ec, enumDef));
        return enumDef;
    }

    private void parseEnumConstant(T value, EnumDef<T> enumDef) {
        Instance instance = getInstance.apply(value);
        EnumConstantRT enumConstant = NncUtils.get(instance, modelMap::getEnumConstant);
        new EnumConstantDef<>(value, enumDef, enumConstant, instance);
    }

}
