package tech.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.meta.ArrayKind;
import tech.metavm.object.meta.ArrayType;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.IteratorImpl;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import static com.intellij.lang.jvm.types.JvmPrimitiveTypeKind.*;
import static java.util.Objects.requireNonNull;

public class TypeResolverImpl implements TypeResolver {

    public static final Set<String> PRIM_CLASS_NAMES = Set.of(
            String.class.getName(),
            Date.class.getName()
    );

    @SuppressWarnings("UnstableApiUsage")
    public static final Map<JvmPrimitiveTypeKind, Class<?>> KIND_2_PRIM_CLASS = Map.of(
            INT, int.class,
            SHORT, short.class,
            BOOLEAN, boolean.class,
            LONG, long.class,
            CHAR, char.class,
            FLOAT, float.class,
            DOUBLE, double.class,
            VOID, void.class
    );

    private PsiClassType parameterizedEnumType;

    private final Compiler compiler;

    private final Set<Type> generatedTypes = new HashSet<>();

    private final MyTypeFactory typeFactory = new MyTypeFactory();

    private final Map<ClassType, PsiClass> psiClassMap = new HashMap<>();

    private final IEntityContext context;

    private static final Map<Class<?>, Supplier<Type>> STANDARD_CLASSES = Map.of(
            Enum.class, StandardTypes::getEnumType,
            Throwable.class, StandardTypes::getThrowableType,
            Exception.class, StandardTypes::getExceptionType,
            RuntimeException.class, StandardTypes::getRuntimeExceptionType
    );

    private static final Map<Class<?>, BiFunction<Type, IEntityContext, ClassType>> COLLECTION_CLASSES = Map.of(
            Iterator.class, TypeUtils::getIteratorType,
            Collection.class, TypeUtils::getCollectionType,
            List.class, TypeUtils::getListType,
            Set.class, TypeUtils::getSetType,
            IteratorImpl.class, TypeUtils::getIteratorImplType
    );

    public TypeResolverImpl(IEntityContext context) {
        this.context = context;
        compiler = new Compiler(context);
        context.getGenericContext().addCallback(typeFactory::addType);
    }

    @Override
    public Type resolveTypeOnly(PsiType psiType) {
        return resolve(psiType, 0);
    }

    @Override
    public Type resolveDeclaration(PsiType psiType) {
        return resolve(psiType, 1);
    }

    @Override
    public Type resolve(PsiType psiType) {
        return resolve(psiType, 2);
    }

    @SuppressWarnings("UnstableApiUsage")
    public Type resolve(PsiType psiType, int stage) {
        return switch (psiType) {
            case PsiPrimitiveType primitiveType -> {
                var klass = ReflectUtils.getBoxedClass(KIND_2_PRIM_CLASS.get(primitiveType.getKind()));
                yield context.getType(klass);
            }
            case PsiClassType classType -> resolveClassType(classType, stage);
            case PsiWildcardType wildcardType -> resolveWildcardType(wildcardType, stage);
            case PsiArrayType arrayType -> resolveArrayType(arrayType, stage);
            case null, default -> throw new InternalException("Invalid PsiType: " + psiType);
        };
    }

    private ArrayType resolveArrayType(PsiArrayType psiArrayType, int stage) {
        return context.getArrayType(resolve(psiArrayType.getComponentType(), stage), ArrayKind.READ_WRITE);
    }

    private Type resolveWildcardType(PsiWildcardType wildcardType, int stage) {
        if (wildcardType.isBounded()) {
            if (wildcardType.isExtends()) {
                return context.getUncertainType(
                        StandardTypes.getNothingType(), resolve(wildcardType.getExtendsBound(), stage)
                );
            } else {
                return context.getUncertainType(
                        resolve(wildcardType.getSuperBound(), stage),
                        StandardTypes.getObjectArrayType()
                );
            }
        } else {
            return context.getUncertainType(
                    StandardTypes.getNothingType(),
                    StandardTypes.getNullableObjectType()
            );
        }
    }

    private Type resolveClassType(PsiClassType classType, int stage) {
        var psiClass = requireNonNull(classType.resolve());
        if (psiClass instanceof PsiTypeParameter typeParameter) {
            return resolveTypeVariable(typeParameter);
        } else {
            for (var entry : STANDARD_CLASSES.entrySet()) {
                if (TranspileUtil.createType(entry.getKey()).equals(classType)) {
                    return entry.getValue().get();
                }
            }
            for (var entry : COLLECTION_CLASSES.entrySet()) {
                var collClass = entry.getKey();
                if (TranspileUtil.createType(collClass).isAssignableFrom(classType)) {
                    var collType = TranspileUtil.getSuperType(classType, collClass);
                    return entry.getValue().apply(resolve(collType.getParameters()[0], stage), context);
                }
            }
            if (ReflectUtils.isPrimitiveBoxClassName(psiClass.getQualifiedName())
                    || PRIM_CLASS_NAMES.contains(psiClass.getQualifiedName())) {
                return context.getType(ReflectUtils.classForName(psiClass.getQualifiedName()));
            } else if (TranspileUtil.isObjectClass(psiClass)) {
                return StandardTypes.getObjectType();
            } else if (TranspileUtil.createType(Map.class).isAssignableFrom(classType)) {
                var mapType = TranspileUtil.getSuperType(classType, Map.class);
                return TypeUtils.getMapType(
                        resolve(mapType.getParameters()[0], stage),
                        resolve(mapType.getParameters()[1], stage),
                        context
                );
            } else if (createParameterizedEnumType().equals(classType)) {
                return StandardTypes.getParameterizedEnumType();
            } else {
                ClassType type;
                if (TranspileUtil.matchClass(psiClass, Enum.class)) {
                    type = StandardTypes.getEnumType();
                } else {
                    type = resolvePojoClass(psiClass, stage);
                }
                if (classType.getParameters().length > 0) {
                    List<Type> typeArgs = NncUtils.map(
                            classType.getParameters(), this::resolveTypeOnly
                    );
                    return context.getParameterizedType(type, typeArgs);
                } else {
                    return type;
                }
            }
        }
    }

    public Set<Type> getGeneratedTypes() {
        return NncUtils.mergeSets(
                List.of(
                        generatedTypes,
                        typeFactory.getGeneratedTypes(),
                        context.getNewCompositeTypes()
                )
        );
    }

    private PsiClassType createParameterizedEnumType() {
        if (parameterizedEnumType != null) {
            return parameterizedEnumType;
        }
        var psiEnumClass = TranspileUtil.createType(Enum.class).resolve();
        var typeArg = TranspileUtil.createType(
                requireNonNull(requireNonNull(psiEnumClass).getTypeParameterList()).getTypeParameters()[0]
        );
        return parameterizedEnumType = TranspileUtil.createType(Enum.class, typeArg);
    }

    public Flow resolveFlow(PsiMethod method) {
        var type = (ClassType) resolveDeclaration(TranspileUtil.createType(method.getContainingClass()));
        return type.getFlowByCodeAndParamTypes(
                method.getName(),
                NncUtils.map(
                        requireNonNull(method.getParameterList().getParameters()),
                        param -> resolveTypeOnly(param.getType())
                )
        );
    }

    private boolean isArrayType(PsiType psiType) {
        if (psiType instanceof PsiClassType classType) {
            var psiClass = requireNonNull(classType.resolve());
            return Objects.equals(psiClass.getQualifiedName(), List.class.getName());
        }
        return false;
    }

    @Override
    public Field resolveField(PsiField field) {
        PsiType declaringType = TranspileUtil.getElementFactory().createType(
                requireNonNull(field.getContainingClass()));
        ClassType type = (ClassType) resolve(declaringType);
        return type.getFieldByCode(field.getName());
    }

    private GenericDeclaration resolveGenericDeclaration(PsiTypeParameterListOwner typeParameterOwner,
                                                         IEntityContext context) {
        if (typeParameterOwner instanceof PsiClass psiClass) {
            return (GenericDeclaration) resolveTypeOnly(TranspileUtil.createType(psiClass));
        } else if (typeParameterOwner instanceof PsiMethod method) {
            return resolveFlow(method);
        } else {
            throw new InternalException("Unexpected type parameter owner: " + typeParameterOwner);
        }
    }

    @Override
    public TypeVariable resolveTypeVariable(PsiTypeParameter typeParameter) {
        var typeVariable = typeParameter.getUserData(Keys.TYPE_VARIABLE);
        if (typeVariable != null) {
            return typeVariable;
        }
        typeVariable = new TypeVariable(null, typeParameter.getName(), typeParameter.getName(),
                DummyGenericDeclaration.INSTANCE);
        typeParameter.putUserData(Keys.TYPE_VARIABLE, typeVariable);
        generatedTypes.add(typeVariable);
        typeVariable.setBounds(NncUtils.map(
                typeParameter.getExtendsListTypes(),
                this::resolveTypeOnly
        ));
//        typeVariable.setGenericDeclaration(resolveGenericDeclaration(typeParameter.getOwner(), context));
        return typeVariable;
    }


    private ClassType createMetaClass(PsiClass psiClass) {
        var name = TranspileUtil.getBizClassName(psiClass);
        ClassType metaClass = ClassBuilder.newBuilder(name, TranspileUtil.getClassCode(psiClass))
                .category(getTypeCategory(psiClass))
                .build();
        psiClass.putUserData(Keys.META_CLASS, metaClass);
        psiClass.putUserData(Keys.RESOLVE_STAGE, 0);
        psiClassMap.put(metaClass, psiClass);
        generatedTypes.add(metaClass);
        metaClass.setInterfaces(
                NncUtils.map(
                        TranspileUtil.getInterfaceTypes(psiClass),
                        it -> (ClassType) resolveTypeOnly(it)
                )
        );
        for (PsiTypeParameter typeParameter : psiClass.getTypeParameters()) {
            resolveTypeVariable(typeParameter).setGenericDeclaration(metaClass);
        }
        if (psiClass.getSuperClass() != null &&
                !Objects.equals(psiClass.getSuperClass().getQualifiedName(), Object.class.getName())) {
            metaClass.setSuperClass((ClassType) resolveTypeOnly(TranspileUtil.getSuperClassType(psiClass)));
        }
        compiler.transform(psiClass);
        return metaClass;
    }

    private TypeCategory getTypeCategory(PsiClass psiClass) {
        return psiClass.isEnum() ? TypeCategory.ENUM
                : (psiClass.isInterface() ? TypeCategory.INTERFACE : TypeCategory.CLASS);
    }

    private ClassType resolvePojoClass(PsiClass psiClass, final int stage) {
        var metaClass = psiClass.getUserData(Keys.META_CLASS);
        if (metaClass == null) {
            metaClass = createMetaClass(psiClass);
        }
        procesClassType(metaClass, psiClass, stage);
        return metaClass;
    }

    @Override
    public void ensureDeclared(ClassType classType) {
        procesClassType(classType, 1);
    }

    @Override
    public void ensureCodeGenerated(ClassType classType) {
        procesClassType(classType, 2);
    }

    private void procesClassType(ClassType metaClass, final int stage) {
        var template = metaClass.getEffectiveTemplate();
        if (template.getId() == null) {
            var psiClass = NncUtils.requireNonNull(psiClassMap.get(template));
            procesClassType(template, psiClass, stage);
        }
    }

    private void procesClassType(ClassType metaClass, PsiClass psiClass, final int stage) {
        if (stage == 0) {
            return;
        }
        for (ClassType superType : metaClass.getSuperTypes()) {
            procesClassType(superType, stage);
        }
        int currentStage = NncUtils.requireNonNull(psiClass.getUserData(Keys.RESOLVE_STAGE));
        if (currentStage < 1) {
            compiler.generateDecl(psiClass, this);
        }
        context.getGenericContext().generateDeclarations(metaClass);

        if (stage == 1) {
            return;
        }
        if (currentStage < 2 && !metaClass.isInterface()) {
            compiler.generateCode(psiClass, this);
        }
        context.getGenericContext().generateCode(metaClass);
    }


    private static class MyTypeFactory extends TypeFactory {

        private final Set<Type> types = new HashSet<>();

        @Override
        public Type getType(java.lang.reflect.Type javaType) {
            return ModelDefRegistry.getType(javaType);
        }

        @Override
        public boolean isAddTypeSupported() {
            return true;
        }

        @Override
        public void addType(Type type) {
            types.add(type);
        }

        public Set<Type> getGeneratedTypes() {
            return Collections.unmodifiableSet(types);
        }

    }

}
