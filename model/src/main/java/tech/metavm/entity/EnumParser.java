package tech.metavm.entity;

import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.ClassBuilder;
import tech.metavm.object.type.ClassSource;
import tech.metavm.object.type.TypeCategory;
import tech.metavm.util.NncUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class EnumParser<T extends Enum<?>> implements DefParser<T, Instance, EnumDef<T>> {

    private final Class<T> javaClass;
    private final ValueDef<Enum<?>> superDef;
    private EnumDef<T> enumDef;
    private final DefContext defContext;
    private final Function<Object, Long> getId;

    public EnumParser(Class<T> enumType, ValueDef<Enum<?>> superDef, DefContext defContext, Function<Object, Long> getId) {
        this.javaClass = enumType;
        this.superDef = superDef;
        this.defContext = defContext;
        this.getId = getId;
    }

    private void parseEnumConstant(T value, EnumDef<T> enumDef) {
        new EnumConstantDef<>(value, enumDef, getId);
    }

    @Override
    public EnumDef<T> create() {
        var interfaceDefs = getInterfaceDefs();
        enumDef = new EnumDef<>(
                javaClass,
                superDef,
                ClassBuilder.newBuilder(EntityUtils.getMetaTypeName(javaClass), javaClass.getSimpleName())
                        .superClass(superDef.getType())
                        .category(TypeCategory.ENUM)
                        .interfaces(NncUtils.map(interfaceDefs, InterfaceDef::getType))
                        .source(ClassSource.BUILTIN)
                        .build(),
                defContext
        );
        return enumDef;
    }

    private List<InterfaceDef<? super T>> getInterfaceDefs() {
        //noinspection unchecked
        return NncUtils.map(
                javaClass.getGenericInterfaces(),
                it -> (InterfaceDef<? super T>) defContext.getDef(it)
        );
    }


    @Override
    public void initialize() {
        Arrays.stream(javaClass.getEnumConstants()).forEach(ec -> parseEnumConstant(ec, enumDef));
    }

    @Override
    public List<Type> getDependencyTypes() {
        return new ArrayList<>(Arrays.asList(javaClass.getGenericInterfaces()));
    }

}
