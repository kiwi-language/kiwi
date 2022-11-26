package tech.metavm.entity;

import tech.metavm.object.instance.IInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.ModelMap;
import tech.metavm.object.meta.EnumConstantRT;
import tech.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.function.Function;

public class EnumParser<T extends Enum<?>> {

    public static <T extends Enum<?>> EnumDef<T> parse(Class<T> enumType, ValueDef<Enum<?>> parentDef, Function<Object, IInstance> getInstance, ModelMap modelMap) {
        return new EnumParser<>(enumType, parentDef, getInstance, modelMap).parse();
    }

    private final Class<T> enumType;
    private final ValueDef<Enum<?>> parentDef;
    private EnumDef<T> enumDef;
    private final Function<Object, IInstance> getInstance;
    private final ModelMap modelMap;

    public EnumParser(Class<T> enumType, ValueDef<Enum<?>> parentDef, Function<Object, IInstance> getInstance, ModelMap modelMap) {
        this.enumType = enumType;
        this.parentDef = parentDef;
        this.getInstance = getInstance;
        this.modelMap = modelMap;
    }

    public EnumDef<T> parse() {
        IInstance enumTypeInst = getInstance.apply(enumType);
        enumDef =  new EnumDef<>(
                enumType,
                parentDef,
                NncUtils.get(enumTypeInst, modelMap::getType)
        );
        Arrays.stream(enumType.getEnumConstants()).forEach(ec -> parseEnumConstant(ec, enumDef));
        return enumDef;
    }

    private void parseEnumConstant(T value, EnumDef<T> enumDef) {
        IInstance instance = getInstance.apply(value);
        EnumConstantRT enumConstant = NncUtils.get(instance, modelMap::getEnumConstant);
        Instance realInstance = NncUtils.get(instance, EntityContext::getRealInstance);
        new EnumConstantDef<>(value, enumDef, enumConstant, realInstance);
    }

}
