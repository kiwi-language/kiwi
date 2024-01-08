package tech.metavm.entity;

import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.ClassSource;
import tech.metavm.object.type.ResolutionStage;
import tech.metavm.object.type.TypeCategory;
import tech.metavm.util.NncUtils;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public class EnumParser<T extends Enum<?>> extends DefParser<T, ClassInstance, EnumDef<T>> {

    private final Class<T> javaClass;
    private final ValueDef<Enum<?>> superDef;
    private EnumDef<T> def;
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
        def = new EnumDef<>(
                javaClass,
                superDef,
                ClassTypeBuilder.newBuilder(EntityUtils.getMetaTypeName(javaClass), javaClass.getSimpleName())
                        .superClass(superDef.getType())
                        .category(TypeCategory.ENUM)
                        .interfaces(NncUtils.map(interfaceDefs, InterfaceDef::getType))
                        .source(ClassSource.BUILTIN)
                        .build(),
                defContext
        );
        return def;
    }

    @Override
    public EnumDef<T> get() {
        return def;
    }

    @Override
    public void generateSignature() {

    }

    @Override
    public void generateDeclaration() {
        Arrays.stream(javaClass.getEnumConstants()).forEach(ec -> parseEnumConstant(ec, def));
    }

    @Override
    public void generateDefinition() {

    }

    private List<InterfaceDef<? super T>> getInterfaceDefs() {
        //noinspection unchecked
        return NncUtils.map(
                javaClass.getGenericInterfaces(),
                it -> (InterfaceDef<? super T>) defContext.getDef(it, ResolutionStage.INIT)
        );
    }

}
