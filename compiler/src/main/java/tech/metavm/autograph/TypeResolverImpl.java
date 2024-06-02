package tech.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.builtin.Password;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.flow.Flow;
import tech.metavm.object.type.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static com.intellij.lang.jvm.types.JvmPrimitiveTypeKind.*;
import static java.util.Objects.requireNonNull;
import static tech.metavm.object.type.ResolutionStage.*;

public class TypeResolverImpl implements TypeResolver {

    public static final Logger logger = LoggerFactory.getLogger(TypeResolverImpl.class);

    public static final Set<String> PRIM_CLASS_NAMES = Set.of(
            String.class.getName(),
            Date.class.getName()
    );

    @SuppressWarnings("UnstableApiUsage")
    public static final Map<JvmPrimitiveTypeKind, Class<?>> KIND_2_PRIM_CLASS = Map.of(
            INT, int.class,
            SHORT, short.class,
            BYTE, byte.class,
            BOOLEAN, boolean.class,
            LONG, long.class,
            CHAR, char.class,
            FLOAT, float.class,
            DOUBLE, double.class,
            VOID, void.class
    );

    private PsiClassType parameterizedEnumType;

    private final CodeGenerator codeGenerator;

    private final Set<TypeDef> generatedTypeDefs = new HashSet<>();

    private final MyTypeFactory typeFactory = new MyTypeFactory();

    private final Map<Klass, PsiClass> psiClassMap = new HashMap<>();

    private final Map<PsiCapturedWildcardType, CapturedType> capturedTypeMap = new IdentityHashMap<>();

    private final Map<CapturedType, PsiCapturedWildcardType> capturedTypeReverseMap = new HashMap<>();

    private final IEntityContext context;

    private static final Map<PsiClassType, Supplier<Klass>> STANDARD_CLASSES = Map.ofEntries(
            Map.entry(TranspileUtil.createClassType(Entity.class), StandardTypes::getEntityKlass),
            Map.entry(TranspileUtil.createClassType(Enum.class), StandardTypes::getEnumKlass),
            Map.entry(TranspileUtil.createClassType(Record.class), StandardTypes::getRecordKlass),
            Map.entry(TranspileUtil.createClassType(Throwable.class), StandardTypes::getThrowableKlass),
            Map.entry(TranspileUtil.createClassType(Exception.class), StandardTypes::getExceptionKlass),
            Map.entry(TranspileUtil.createClassType(RuntimeException.class), StandardTypes::getRuntimeExceptionKlass),
            Map.entry(TranspileUtil.createClassType(IllegalArgumentException.class), StandardTypes::getIllegalArgumentExceptionKlass),
            Map.entry(TranspileUtil.createClassType(IllegalStateException.class), StandardTypes::getIllegalStateExceptionKlass),
            Map.entry(TranspileUtil.createClassType(NullPointerException.class), StandardTypes::getNullPointerExceptionKlass),
            Map.entry(TranspileUtil.createClassType(IteratorImpl.class), StandardTypes::getIteratorImplKlass),
            Map.entry(TranspileUtil.createClassType(Iterator.class), StandardTypes::getIteratorKlass),
            Map.entry(TranspileUtil.createClassType(Iterable.class), StandardTypes::getIterableKlass),
            Map.entry(TranspileUtil.createClassType(List.class), StandardTypes::getListKlass),
            Map.entry(TranspileUtil.createClassType(ArrayList.class), StandardTypes::getReadWriteListKlass),
            Map.entry(TranspileUtil.createClassType(LinkedList.class), StandardTypes::getReadWriteListKlass),
            Map.entry(TranspileUtil.createClassType(tech.metavm.util.LinkedList.class), StandardTypes::getReadWriteListKlass),
            Map.entry(TranspileUtil.createClassType(ChildList.class), StandardTypes::getChildListKlass),
            Map.entry(TranspileUtil.createClassType(ValueList.class), StandardTypes::getValueListKlass),
            Map.entry(TranspileUtil.createClassType(Set.class), StandardTypes::getSetKlass),
            Map.entry(TranspileUtil.createClassType(Collection.class), StandardTypes::getCollectionKlass),
            Map.entry(TranspileUtil.createClassType(Consumer.class), StandardTypes::getConsumerKlass),
            Map.entry(TranspileUtil.createClassType(Predicate.class), StandardTypes::getPredicateKlass)
    );

    private static final List<Class<?>> COLLECTION_CLASSES = List.of(
            IteratorImpl.class,
            Iterator.class,
            ChildList.class,
            ArrayList.class,
            LinkedList.class,
            tech.metavm.util.LinkedList.class,
            List.class,
            Set.class,
            Collection.class
    );

    public TypeResolverImpl(IEntityContext context) {
        this.context = context;
        codeGenerator = new CodeGenerator(context);
    }

    @Override
    public Type resolveTypeOnly(PsiType psiType) {
        return resolve(psiType, INIT);
    }

    @Override
    public Type resolveDeclaration(PsiType psiType) {
        return resolve(psiType, DECLARATION);
    }

    @Override
    public Type resolve(PsiType psiType) {
        return resolve(psiType, DEFINITION);
    }

    @SuppressWarnings("UnstableApiUsage")
    public Type resolve(PsiType psiType, ResolutionStage stage) {
        return switch (psiType) {
            case PsiPrimitiveType primitiveType -> {
                if (primitiveType.getName().equals("null"))
                    yield StandardTypes.getNullType();
                var klass = ReflectionUtils.getBoxedClass(KIND_2_PRIM_CLASS.get(primitiveType.getKind()));
                yield context.getType(klass);
            }
            case PsiClassType classType -> resolveClassType(classType, stage);
            case PsiWildcardType wildcardType -> resolveWildcardType(wildcardType, stage);
            case PsiArrayType arrayType -> resolveArrayType(arrayType, stage);
            case PsiCapturedWildcardType capturedWildcardType -> resolveCapturedType(capturedWildcardType, stage);
            case null, default -> throw new InternalException("Invalid PsiType: " + psiType);
        };
    }

    private ArrayType resolveArrayType(PsiArrayType psiArrayType, ResolutionStage stage) {
        try (var entry = ContextUtil.getProfiler().enter("resolveArrayType: " + stage)) {
            return new ArrayType(resolve(psiArrayType.getComponentType(), stage), ArrayKind.READ_WRITE);
        }
    }

    private UncertainType resolveWildcardType(PsiWildcardType wildcardType, ResolutionStage stage) {
        try (var entry = ContextUtil.getProfiler().enter("resolveWildcardType: " + stage)) {
            if (wildcardType.isBounded()) {
                if (wildcardType.isExtends()) {
                    return new UncertainType(StandardTypes.getNeverType(), resolve(wildcardType.getExtendsBound(), stage));
                } else {
                    return new UncertainType(
                            resolve(wildcardType.getSuperBound(), stage),
                            StandardTypes.getNullableAnyType()
                    );
                }
            } else {
                return new UncertainType(
                        StandardTypes.getNeverType(),
                        StandardTypes.getNullableAnyType()
                );
            }
        }
    }

    private Type resolveCapturedType(PsiCapturedWildcardType psiCapturedType, ResolutionStage stage) {
        var resolved = capturedTypeMap.get(psiCapturedType);
        if (resolved != null)
            return resolved;
        var psiMethod = Objects.requireNonNull(TranspileUtil.getParent(psiCapturedType.getContext(), PsiMethod.class));
        var method = Objects.requireNonNull(psiMethod.getUserData(Keys.Method));
        var capturedTypeVar = new CapturedTypeVariable(
                resolveWildcardType(psiCapturedType.getWildcard(), stage),
                method
        );
        generatedTypeDefs.add(capturedTypeVar);
        var capturedType = capturedTypeVar.getType();
        capturedTypeMap.put(psiCapturedType, capturedType);
        capturedTypeReverseMap.put(capturedType, psiCapturedType);
        return capturedType;
//        return requireNonNull(capturedTypeMap.get(psiCapturedType),
//                () -> "Captured type not specified for " + psiCapturedType.getContext().getText());
    }

    public PsiCapturedWildcardType getPsiCapturedType(CapturedType capturedType) {
        return requireNonNull(capturedTypeReverseMap.get(capturedType),
                () -> "Can not find PsiCapturedWildcardType for captured type " + capturedType);
    }

    private Type resolveClassType(PsiClassType classType, ResolutionStage stage) {
        try (var entry = ContextUtil.getProfiler().enter("resolveClassType: " + stage)) {
            for (var collClass : COLLECTION_CLASSES) {
                if (TranspileUtil.createClassType(collClass).isAssignableFrom(classType)) {
                    classType = TranspileUtil.getSuperType(classType, collClass);
                    break;
                }
            }
            if (TranspileUtil.createClassType(Map.class).isAssignableFrom(classType)) {
                classType = TranspileUtil.getSuperType(classType, Map.class);
            }
            var psiClass = requireNonNull(classType.resolve());
            if (psiClass instanceof PsiTypeParameter typeParameter)
                return resolveTypeVariable(typeParameter);
            else if (TranspileUtil.isObjectClass(psiClass))
                return StandardTypes.getAnyType();
            else if (TranspileUtil.matchType(classType, Password.class))
                return StandardTypes.getPasswordType();
            else if (ReflectionUtils.isPrimitiveBoxClassName(psiClass.getQualifiedName())
                    || PRIM_CLASS_NAMES.contains(psiClass.getQualifiedName()))
                return context.getType(ReflectionUtils.classForName(psiClass.getQualifiedName()));
            else {
                Klass type;
                var rawType = classType.rawType();
                if (STANDARD_CLASSES.containsKey(rawType))
                    type = STANDARD_CLASSES.get(rawType).get();
                else
                    type = resolvePojoClass(psiClass, stage);
                if (classType.getParameters().length > 0) {
                    List<Type> typeArgs = NncUtils.map(
                            classType.getParameters(), this::resolveTypeOnly
                    );
                    return type.getParameterized(typeArgs, stage).getType();
                } else {
                    return type.getType();
                }
            }
        }
    }

    public Set<TypeDef> getGeneratedTypeDefs() {
        return generatedTypeDefs;
    }

    public Flow resolveFlow(PsiMethod method) {
        var klass = ((ClassType) resolveDeclaration(TranspileUtil.createType(method.getContainingClass()))).resolve();
        return NncUtils.findRequired(klass.getMethods(), f ->
                f.getInternalName(null).equals(TranspileUtil.getInternalName(method)));
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
        Klass klass = ((ClassType) resolve(declaringType)).resolve();
        return klass.findFieldByCode(field.getName());
    }

    private GenericDeclaration tryResolveGenericDeclaration(PsiTypeParameterListOwner typeParameterListOwner) {
        if (typeParameterListOwner instanceof PsiClass psiClass) {
            var psiType = TranspileUtil.createType(psiClass);
            var stdTypeSupplier = STANDARD_CLASSES.get(psiType);
            if (stdTypeSupplier != null)
                return stdTypeSupplier.get();
            return psiClass.getUserData(Keys.MV_CLASS);
        }
        if (typeParameterListOwner instanceof PsiMethod method) {
            var psiClass = method.getContainingClass();
            var stdTypeSupplier = STANDARD_CLASSES.get(TranspileUtil.createType(psiClass));
            if (stdTypeSupplier != null) {
                var classType = stdTypeSupplier.get();
                return NncUtils.findRequired(classType.getMethods(), m ->
                        m.getInternalName(null).equals(TranspileUtil.getInternalName(method)));
            }
            return method.getUserData(Keys.Method);
        }
        throw new InternalException("Unexpected type parameter owner: " + typeParameterListOwner);
    }

    private GenericDeclaration resolveGenericDeclaration(PsiTypeParameterListOwner typeParameterOwner) {
        if (typeParameterOwner instanceof PsiClass psiClass) {
            return (GenericDeclaration) resolveTypeOnly(TranspileUtil.createType(psiClass));
        } else if (typeParameterOwner instanceof PsiMethod method) {
            return resolveFlow(method);
        } else {
            throw new InternalException("Unexpected type parameter owner: " + typeParameterOwner);
        }
    }

    @Override
    public VariableType resolveTypeVariable(PsiTypeParameter typeParameter) {
        var builtInTypeVar = tryResolveBuiltinTypeVar(typeParameter);
        if (builtInTypeVar != null)
            return builtInTypeVar.getType();
        var typeVariable = typeParameter.getUserData(Keys.TYPE_VARIABLE);
        if (typeVariable != null)
            return typeVariable.getType();
        var genericDeclaration = tryResolveGenericDeclaration(typeParameter.getOwner());
        if (genericDeclaration != null)
            typeVariable = NncUtils.find(genericDeclaration.getTypeParameters(),
                    tv -> Objects.equals(tv.getCode(), typeParameter.getName()));
        if (typeVariable == null)
            typeVariable = new TypeVariable(null, Objects.requireNonNull(typeParameter.getName()), typeParameter.getName(),
                    DummyGenericDeclaration.INSTANCE);
        typeParameter.putUserData(Keys.TYPE_VARIABLE, typeVariable);
        generatedTypeDefs.add(typeVariable);
        typeVariable.setBounds(NncUtils.map(
                typeParameter.getExtendsListTypes(),
                this::resolveTypeOnly
        ));
//        typeVariable.setGenericDeclaration(resolveGenericDeclaration(typeParameter.getOwner(), context));
        return typeVariable.getType();
    }


    @Nullable
    private TypeVariable tryResolveBuiltinTypeVar(PsiTypeParameter typeParameter) {
        if (typeParameter.getOwner() instanceof PsiClass psiClass) {
            var ownerType = TranspileUtil.createType(psiClass);
            var childListType = TranspileUtil.createClassType(ChildList.class);
            if (childListType.isAssignableFrom(ownerType))
                return StandardTypes.getChildListKlass().getTypeParameters().get(0);
            var arrayListType = TranspileUtil.createClassType(ArrayList.class);
            var linkedListType = TranspileUtil.createClassType(LinkedList.class);
            if (arrayListType.isAssignableFrom(ownerType) || linkedListType.isAssignableFrom(ownerType))
                return StandardTypes.getReadWriteListKlass().getTypeParameters().get(0);
            var listType = TranspileUtil.createClassType(List.class);
            if (listType.isAssignableFrom(ownerType))
                return StandardTypes.getListKlass().getTypeParameters().get(0);
            var setType = TranspileUtil.createClassType(Set.class);
            if (setType.isAssignableFrom(ownerType))
                return StandardTypes.getSetKlass().getTypeParameters().get(0);
            var mapType = TranspileUtil.createClassType(Map.class);
            if (mapType.isAssignableFrom(ownerType)) {
                int index = NncUtils.requireNonNull(psiClass.getTypeParameterList())
                        .getTypeParameterIndex(typeParameter);
                return StandardTypes.getMapKlass().getTypeParameters().get(index);
            }
            var enumType = TranspileUtil.createClassType(Enum.class);
            if (ownerType.equals(enumType))
                return StandardTypes.getEnumKlass().getTypeParameters().get(0);
        }
        return null;
    }

    private Klass createMetaClass(PsiClass psiClass) {
        try (var ignored = ContextUtil.getProfiler().enter("createMetaClass")) {
            var name = TranspileUtil.getBizClassName(psiClass);
            var kind = getClassKind(psiClass);
            boolean isTemplate = psiClass.getTypeParameterList() != null
                    && psiClass.getTypeParameterList().getTypeParameters().length > 0;
            var klass = NncUtils.first(context.query(
                    EntityIndexQueryBuilder
                            .newBuilder(Klass.UNIQUE_CODE)
                            .eq(new EntityIndexKey(List.of(requireNonNull(psiClass.getQualifiedName()))))
                            .build()));
            if (klass != null) {
                if (klass.getKind() != kind)
                    throw new BusinessException(ErrorCode.CHANGING_CATEGORY);
                if (klass.isTemplate() != isTemplate)
                    throw new BusinessException(ErrorCode.CHANGING_IS_TEMPLATE);
            } else {
                klass = ClassTypeBuilder.newBuilder(name, psiClass.getQualifiedName())
                        .kind(kind)
                        .ephemeral(TranspileUtil.isEphemeral(psiClass))
                        .struct(TranspileUtil.isStruct(psiClass))
                        .isTemplate(isTemplate)
                        .isAbstract(psiClass.hasModifierProperty(PsiModifier.ABSTRACT))
                        .build();
                context.bind(klass);
            }
            psiClass.putUserData(Keys.MV_CLASS, klass);
            psiClassMap.put(klass, psiClass);
            generatedTypeDefs.add(klass);
            for (PsiTypeParameter typeParameter : psiClass.getTypeParameters()) {
                resolveTypeVariable(typeParameter).getVariable().setGenericDeclaration(klass);
            }
            if (psiClass.getSuperClass() != null &&
                    !Objects.equals(psiClass.getSuperClass().getQualifiedName(), Object.class.getName())) {
                klass.setSuperType(((ClassType) resolveTypeOnly(TranspileUtil.getSuperClassType(psiClass))));
            }
            klass.setInterfaces(
                    NncUtils.map(
                            TranspileUtil.getInterfaceTypes(psiClass),
                            it -> ((ClassType) resolveTypeOnly(it))
                    )
            );
            codeGenerator.transform(psiClass);
            return klass;
        }
    }

    private void updateClassType(Klass classType, PsiClass psiClass) {
        classType.setName(TranspileUtil.getBizClassName(psiClass));
        classType.setStruct(TranspileUtil.isStruct(psiClass));
        classType.setAbstract(psiClass.hasModifierProperty(PsiModifier.ABSTRACT));
    }

    private ClassKind getClassKind(PsiClass psiClass) {
        if(psiClass.isEnum())
            return ClassKind.ENUM;
        if(psiClass.isInterface())
            return ClassKind.INTERFACE;
        if(psiClass.hasAnnotation(ValueType.class.getName()))
            return ClassKind.VALUE;
        return ClassKind.CLASS;
    }

    private Klass resolvePojoClass(PsiClass psiClass, final ResolutionStage stage) {
        try (var entry = ContextUtil.getProfiler().enter("resolvePojoClass: " + stage)) {
            var metaClass = psiClass.getUserData(Keys.MV_CLASS);
            if (metaClass == null)
                metaClass = createMetaClass(psiClass);
            else
                updateClassType(metaClass, psiClass);
            processClassType(metaClass, psiClass, stage);
            return metaClass;
        }
    }

    @Override
    public void ensureDeclared(Klass classType) {
        processClassType(classType, DECLARATION);
    }

    @Override
    public void ensureCodeGenerated(Klass classType) {
        processClassType(classType, DEFINITION);
    }

    @Override
    public void mapCapturedType(PsiCapturedWildcardType psiCapturedWildcardType, CapturedType type) {
        capturedTypeMap.put(psiCapturedWildcardType, type);
    }

    private void processClassType(Klass metaClass, final ResolutionStage stage) {
        var template = metaClass.getEffectiveTemplate();
        if (template != metaClass && template.getStage().isAfterOrAt(stage))
            template.getParameterized(metaClass.getTypeArguments(), stage);
        else if (template.tryGetId() == null) {
            var psiClass = NncUtils.requireNonNull(psiClassMap.get(template));
            processClassType(template, psiClass, stage);
        }
    }

    private void processClassType(Klass metaClass, PsiClass psiClass, final ResolutionStage stage) {
        try (var ignored = ContextUtil.getProfiler().enter("processClassType: " + stage)) {
            if (stage == INIT) {
                return;
            }
            for (PsiClassType superType : psiClass.getSuperTypes())
                resolve(superType, stage);
            metaClass.forEachSuper(superType -> processClassType(superType, stage));
            if (stage.isAfterOrAt(DECLARATION) && metaClass.getStage().isBefore(DECLARATION)) {
                codeGenerator.generateDecl(psiClass, this);
            }
            if (stage.isAfterOrAt(DEFINITION) && metaClass.getStage().isBefore(DEFINITION) && !metaClass.isInterface()) {
                codeGenerator.generateCode(psiClass, this);
            }
        }
//        metaClass.setStage(stage);
    }

    private class MyTypeFactory extends TypeFactory {

        private final Set<Type> types = new HashSet<>();

        public Set<Type> getGeneratedTypes() {
            return Collections.unmodifiableSet(types);
        }

    }

}
