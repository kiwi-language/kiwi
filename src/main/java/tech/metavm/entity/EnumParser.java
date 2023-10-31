package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.*;
import tech.metavm.util.NncUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static tech.metavm.util.ReflectUtils.getMetaTypeName;

public class EnumParser<T extends Enum<?>> implements DefParser<T, Instance, EnumDef<T>> {

    private final Class<T> javaClass;
    private final ValueDef<Enum<?>> superDef;
    private EnumDef<T> enumDef;
    private final DefMap defMap;
    private final TypeFactory typeFactory;

    public EnumParser(Class<T> enumType, ValueDef<Enum<?>> superDef, DefMap defMap) {
        this.javaClass = enumType;
        this.superDef = superDef;
        this.defMap = defMap;
        typeFactory = new DefTypeFactory(defMap);
    }

    private void parseEnumConstant(T value, EnumDef<T> enumDef) {
        new EnumConstantDef<>(value, enumDef);
    }

    @Override
    public EnumDef<T> create() {
        var interfaceDefs = getInterfaceDefs();
        enumDef = new EnumDef<>(
                javaClass,
                superDef,
                ClassBuilder.newBuilder(getMetaTypeName(javaClass), javaClass.getSimpleName())
                        .superType(superDef.getType())
                        .category(TypeCategory.ENUM)
                        .interfaces(NncUtils.map(interfaceDefs, InterfaceDef::getType))
                        .source(ClassSource.REFLECTION)
                        .build(),
                typeFactory.getNullType(),
                typeFactory.getStringType()
        );
        return enumDef;
    }

    private List<InterfaceDef<? super T>> getInterfaceDefs() {
        //noinspection unchecked
        return NncUtils.map(
                javaClass.getGenericInterfaces(),
                it -> (InterfaceDef<? super T>) defMap.getDef(it)
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
