package tech.metavm.entity;

import tech.metavm.object.meta.ColumnStore;
import tech.metavm.object.meta.TypeCategory;

import java.lang.reflect.Type;

public class InterfaceParser<T> extends PojoParser<T, InterfaceDef<T>> {

    public InterfaceParser(Class<T> javaClass, Type javaType, DefContext defContext, ColumnStore columnStore) {
        super(javaClass, javaType, defContext, columnStore);
    }

    @Override
    protected InterfaceDef<T> createDef(PojoDef<? super T> superDef) {
        return new InterfaceDef<>(
                javaClass, javaType, superDef, createType(), defContext
        );
    }

    @Override
    protected TypeCategory getTypeCategory() {
        return TypeCategory.INTERFACE;
    }

//    private final Type javaType;
//    private final Class<T> javaClass;
//    protected final DefMap defMap;
//    private final JavaSubstitutor substitutor;
//
//    public InterfaceParser(Class<T> javaClass, Type javaType, DefMap defMap) {
//        this.javaType = javaType;
//        this.javaClass = javaClass;
//        substitutor = ReflectUtils.resolveGenerics(javaType);
//        this.defMap =  defMap;
//    }
//
//    @Override
//    public InterfaceDef<T> create() {
//        //noinspection unchecked
//        List<InterfaceDef<? super T>> superDefs = NncUtils.map(
//            javaClass.getGenericInterfaces(),
//            it -> (InterfaceDef<? super T>) defMap.getDef(substitutor.substitute(it))
//        );
//        List<ClassType> superInterfaces = NncUtils.map(superDefs, InterfaceDef::getType);
//        return new InterfaceDef<>(javaClass, javaType, createType(superInterfaces), superDefs);
//    }
//
//    @Override
//    public void initialize() {
//
//    }
//
//    @Override
//    public List<Type> getDependencyTypes() {
//        return List.of();
//    }
//
//    private ClassType createType(List<ClassType> superInterfaces) {
//        List<>
//
//        return ClassBuilder.newBuilder(ReflectUtils.getMetaTypeName(javaClass), javaClass.getSimpleName())
//                .interfaces(superInterfaces)
//                .source(ClassSource.REFLECTION)
//                .category(TypeCategory.INTERFACE)
//                .build();
//    }
}
