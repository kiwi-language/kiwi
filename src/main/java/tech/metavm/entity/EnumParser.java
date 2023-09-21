package tech.metavm.entity;

import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.ClassBuilder;
import tech.metavm.object.meta.ClassSource;
import tech.metavm.object.meta.TypeCategory;
import tech.metavm.object.meta.TypeFactory;

import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static tech.metavm.util.ReflectUtils.getMetaTypeName;

public class EnumParser<T extends Enum<?>> implements DefParser<T, Instance, EnumDef<T>> {

    private final Class<T> enumType;
    private final ValueDef<Enum<?>> superDef;
    private EnumDef<T> enumDef;
    private final DefMap defMap;
    private final TypeFactory typeFactory;

    public EnumParser(Class<T> enumType, ValueDef<Enum<?>> superDef, DefMap defMap) {
        this.enumType = enumType;
        this.superDef = superDef;
        this.defMap = defMap;
        typeFactory = new TypeFactory(defMap::getType);
    }

    private void parseEnumConstant(T value, EnumDef<T> enumDef) {
        new EnumConstantDef<>(value, enumDef);
    }

    @Override
    public EnumDef<T> create() {
        enumDef = new EnumDef<>(
                enumType,
                superDef,
                ClassBuilder.newBuilder(getMetaTypeName(enumType), enumType.getSimpleName())
                        .superType(superDef.getType())
                        .category(TypeCategory.ENUM)
                        .source(ClassSource.REFLECTION)
                        .build(),
                typeFactory.getNullType()
        );
        return enumDef;
    }

    @Override
    public void initialize() {
        Arrays.stream(enumType.getEnumConstants()).forEach(ec -> parseEnumConstant(ec, enumDef));
    }

    @Override
    public List<Type> getDependencyTypes() {
        return List.of();
    }

}
