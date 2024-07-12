package org.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import org.metavm.api.ChildList;
import org.metavm.api.ValueList;
import org.metavm.api.ValueStruct;
import org.metavm.api.ValueType;
import org.metavm.api.builtin.Password;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.flow.Flow;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Supplier;

import static com.intellij.lang.jvm.types.JvmPrimitiveTypeKind.*;
import static java.util.Objects.requireNonNull;
import static org.metavm.object.type.ResolutionStage.*;

public class TypeResolverImpl implements TypeResolver {

    private static final String API_PKG_PREFIX = "org.metavm.api.";

    public static final String JAVA_PKG_PREFIX = "java.";

    private static final String LANG_PKG_PREFIX = "org.metavm.api.lang.";

    private static final Logger logger = LoggerFactory.getLogger(TypeResolverImpl.class);

    private static final Set<String> PRIM_CLASS_NAMES = Set.of(
            String.class.getName(),
            Date.class.getName()
    );

    @SuppressWarnings("UnstableApiUsage")
    private static final Map<JvmPrimitiveTypeKind, Class<?>> KIND_2_PRIM_CLASS = Map.of(
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

    private static final Map<PsiClassType, Supplier<Klass>> STANDARD_CLASSES;

    static {
        var map = new HashMap<PsiClassType, Supplier<Klass>>();
        for (StdKlass def : StdKlass.values()) {
            map.put(TranspileUtils.createClassType(def.getJavaClass()), def::get);
        }
        map.put(TranspileUtils.createClassType(LinkedList.class), StdKlass.arrayList::get);
        STANDARD_CLASSES = Collections.unmodifiableMap(map);
    }

            /*Map.ofEntries(
            Map.entry(TranspileUtils.createClassType(Entity.class), StandardTypes::getEntityKlass),
            Map.entry(TranspileUtils.createClassType(Enum.class), StandardTypes::getEnumKlass),
            Map.entry(TranspileUtils.createClassType(Record.class), StandardTypes::getRecordKlass),
            Map.entry(TranspileUtils.createClassType(Throwable.class), StandardTypes::getThrowableKlass),
            Map.entry(TranspileUtils.createClassType(Exception.class), StandardTypes::getExceptionKlass),
            Map.entry(TranspileUtils.createClassType(RuntimeException.class), StandardTypes::getRuntimeExceptionKlass),
            Map.entry(TranspileUtils.createClassType(IllegalArgumentException.class), StandardTypes::getIllegalArgumentExceptionKlass),
            Map.entry(TranspileUtils.createClassType(IllegalStateException.class), StandardTypes::getIllegalStateExceptionKlass),
            Map.entry(TranspileUtils.createClassType(NullPointerException.class), StandardTypes::getNullPointerExceptionKlass),
            Map.entry(TranspileUtils.createClassType(IteratorImpl.class), StandardTypes::getIteratorImplKlass),
            Map.entry(TranspileUtils.createClassType(Iterator.class), StandardTypes::getIteratorKlass),
            Map.entry(TranspileUtils.createClassType(Iterable.class), StandardTypes::getIterableKlass),
            Map.entry(TranspileUtils.createClassType(List.class), StandardTypes::getListKlass),
            Map.entry(TranspileUtils.createClassType(ArrayList.class), StandardTypes::getReadWriteListKlass),
            Map.entry(TranspileUtils.createClassType(LinkedList.class), StandardTypes::getReadWriteListKlass),
            Map.entry(TranspileUtils.createClassType(org.metavm.util.LinkedList.class), StandardTypes::getReadWriteListKlass),
            Map.entry(TranspileUtils.createClassType(ChildList.class), StandardTypes::getChildListKlass),
            Map.entry(TranspileUtils.createClassType(ValueList.class), StandardTypes::getValueListKlass),
            Map.entry(TranspileUtils.createClassType(Set.class), StandardTypes::getSetKlass),
            Map.entry(TranspileUtils.createClassType(Collection.class), StandardTypes::getCollectionKlass),
            Map.entry(TranspileUtils.createClassType(Consumer.class), StandardTypes::getConsumerKlass),
            Map.entry(TranspileUtils.createClassType(Predicate.class), StandardTypes::getPredicateKlass)
    );*/

    private static final List<Class<?>> COLLECTION_CLASSES = List.of(
            IteratorImpl.class,
            Iterator.class,
            ChildList.class,
            ValueList.class,
            ArrayList.class,
            LinkedList.class,
            org.metavm.util.LinkedList.class,
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
                    yield Types.getNullType();
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
        return new ArrayType(resolve(psiArrayType.getComponentType(), stage), ArrayKind.READ_WRITE);
    }

    private UncertainType resolveWildcardType(PsiWildcardType wildcardType, ResolutionStage stage) {
        if (wildcardType.isBounded()) {
            if (wildcardType.isExtends()) {
                return UncertainType.createUpperBounded(resolve(wildcardType.getExtendsBound(), stage));
            } else {
                return UncertainType.createLowerBounded(resolve(wildcardType.getSuperBound(), stage));
            }
        } else {
            return UncertainType.asterisk;
        }
    }

    private Type resolveCapturedType(PsiCapturedWildcardType psiCapturedType, ResolutionStage stage) {
        var resolved = capturedTypeMap.get(psiCapturedType);
        if (resolved != null)
            return resolved;
        var psiMethod = Objects.requireNonNull(TranspileUtils.getParent(psiCapturedType.getContext(), PsiMethod.class));
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
                if (TranspileUtils.createClassType(collClass).isAssignableFrom(classType)) {
                    classType = TranspileUtils.getSuperType(classType, collClass);
                    break;
                }
            }
            if (TranspileUtils.createClassType(Map.class).isAssignableFrom(classType)) {
                classType = TranspileUtils.getSuperType(classType, Map.class);
            }
            var classTypeText = classType.getCanonicalText();
            var psiClass = requireNonNull(classType.resolve(),
                    () -> "Failed to resolve class type " + classTypeText);
            if (psiClass instanceof PsiTypeParameter typeParameter)
                return resolveTypeVariable(typeParameter);
            else if (TranspileUtils.isObjectClass(psiClass))
                return Types.getAnyType();
            else if (TranspileUtils.matchType(classType, Password.class))
                return Types.getPasswordType();
            else if (ReflectionUtils.isPrimitiveBoxClassName(psiClass.getQualifiedName())
                    || PRIM_CLASS_NAMES.contains(psiClass.getQualifiedName()))
                return context.getType(ReflectionUtils.classForName(psiClass.getQualifiedName()));
            else {
                var klass = tryResolveBuiltinClass(classType.rawType());
                if (klass == null)
                    klass = resolvePojoClass(psiClass, stage);
                if (classType.getParameters().length > 0) {
                    List<Type> typeArgs = NncUtils.map(
                            classType.getParameters(), this::resolveTypeOnly
                    );
                    return klass.getParameterized(typeArgs, stage).getType();
                } else {
                    return klass.getType();
                }
            }
        }
    }

    public Set<TypeDef> getGeneratedTypeDefs() {
        return generatedTypeDefs;
    }

    public Flow resolveFlow(PsiMethod method) {
        var klass = ((ClassType) resolveDeclaration(TranspileUtils.createType(method.getContainingClass()))).resolve();
        return NncUtils.findRequired(klass.getMethods(), f ->
                f.getInternalName(null).equals(TranspileUtils.getInternalName(method)));
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
        PsiType declaringType = TranspileUtils.getElementFactory().createType(
                requireNonNull(field.getContainingClass()));
        Klass klass = ((ClassType) resolve(declaringType)).resolve();
        return klass.findFieldByCode(field.getName());
    }

    private GenericDeclaration tryResolveGenericDeclaration(PsiTypeParameterListOwner typeParameterListOwner) {
        if (typeParameterListOwner instanceof PsiClass psiClass) {
            var psiType = TranspileUtils.createType(psiClass);
            var builtinKlass = tryResolveBuiltinClass(psiType);
            if (builtinKlass != null)
                return builtinKlass;
            return psiClass.getUserData(Keys.MV_CLASS);
        }
        if (typeParameterListOwner instanceof PsiMethod method) {
            var psiClass = method.getContainingClass();
            var builtinKlass = tryResolveBuiltinClass(TranspileUtils.createType(psiClass));
            if (builtinKlass != null) {
                var internalName = TranspileUtils.getInternalName(method);
                return NncUtils.findRequired(builtinKlass.getMethods(), m ->
                                m.getInternalName(null).equals(internalName),
                        () -> "Can not find method " + internalName + " in class " + builtinKlass.getTypeDesc());
            }
            return method.getUserData(Keys.Method);
        }
        throw new InternalException("Unexpected type parameter owner: " + typeParameterListOwner);
    }

    private @Nullable Klass tryResolveBuiltinClass(PsiClassType classType) {
        var standardKlassSupplier = STANDARD_CLASSES.get(classType);
        if (standardKlassSupplier != null)
            return standardKlassSupplier.get();
        var psiClass = TranspileUtils.resolvePsiClass(classType);
        var qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName != null && (qualifiedName.startsWith(API_PKG_PREFIX) || qualifiedName.startsWith(JAVA_PKG_PREFIX))) {
            if (qualifiedName.startsWith(LANG_PKG_PREFIX))
                throw new IllegalArgumentException("Can not resolve class in lang package: " + qualifiedName);
            var javaClass = ReflectionUtils.classForName(qualifiedName);
            return ModelDefRegistry.getDefContext().getKlass(javaClass);
        } else
            return null;
    }

    private GenericDeclaration resolveGenericDeclaration(PsiTypeParameterListOwner typeParameterOwner) {
        if (typeParameterOwner instanceof PsiClass psiClass) {
            return (GenericDeclaration) resolveTypeOnly(TranspileUtils.createType(psiClass));
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
            var ownerType = TranspileUtils.createType(psiClass);
            var childListType = TranspileUtils.createClassType(ChildList.class);
            if (childListType.isAssignableFrom(ownerType))
                return StdKlass.childList.get().getTypeParameters().get(0);
            var valueListType = TranspileUtils.createClassType(ValueList.class);
            if (valueListType.isAssignableFrom(ownerType))
                return StdKlass.valueList.get().getTypeParameters().get(0);
            var arrayListType = TranspileUtils.createClassType(ArrayList.class);
            var linkedListType = TranspileUtils.createClassType(LinkedList.class);
            if (arrayListType.isAssignableFrom(ownerType) || linkedListType.isAssignableFrom(ownerType))
                return StdKlass.arrayList.get().getTypeParameters().get(0);
            var listType = TranspileUtils.createClassType(List.class);
            if (listType.isAssignableFrom(ownerType))
                return StdKlass.list.get().getTypeParameters().get(0);
            var setType = TranspileUtils.createClassType(Set.class);
            if (setType.isAssignableFrom(ownerType))
                return StdKlass.set.get().getTypeParameters().get(0);
            var mapType = TranspileUtils.createClassType(Map.class);
            if (mapType.isAssignableFrom(ownerType)) {
                int index = NncUtils.requireNonNull(psiClass.getTypeParameterList())
                        .getTypeParameterIndex(typeParameter);
                return StdKlass.map.get().getTypeParameters().get(index);
            }
            var enumType = TranspileUtils.createClassType(Enum.class);
            if (ownerType.equals(enumType))
                return StdKlass.enum_.get().getTypeParameters().get(0);
        }
        return null;
    }

    private Klass createMetaClass(PsiClass psiClass) {
        try (var ignored = ContextUtil.getProfiler().enter("createMetaClass")) {
            NncUtils.requireFalse(Objects.requireNonNull(psiClass.getQualifiedName()).startsWith("org.metavm.api."),
                    () -> "Can not create meta class for API class: " + psiClass.getQualifiedName());
            var name = TranspileUtils.getBizClassName(psiClass);
            var kind = getClassKind(psiClass);
            boolean isTemplate = psiClass.getTypeParameterList() != null
                    && psiClass.getTypeParameterList().getTypeParameters().length > 0;
            var klass = NncUtils.first(context.query(
                    EntityIndexQueryBuilder
                            .newBuilder(Klass.UNIQUE_CODE)
                            .eq(new EntityIndexKey(List.of(requireNonNull(psiClass.getQualifiedName()))))
                            .build()));
            if (klass != null) {
                klass.setName(name);
                if (klass.getKind() != kind)
                    throw new BusinessException(ErrorCode.CHANGING_CATEGORY);
                if (klass.isTemplate() != isTemplate)
                    throw new BusinessException(ErrorCode.CHANGING_IS_TEMPLATE);
            } else {
                klass = KlassBuilder.newBuilder(name, psiClass.getQualifiedName())
                        .kind(kind)
                        .ephemeral(TranspileUtils.isEphemeral(psiClass))
                        .struct(TranspileUtils.isStruct(psiClass))
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
                klass.setSuperType(((ClassType) resolveTypeOnly(TranspileUtils.getSuperClassType(psiClass))));
            }
            klass.setInterfaces(
                    NncUtils.map(
                            TranspileUtils.getInterfaceTypes(psiClass),
                            it -> ((ClassType) resolveTypeOnly(it))
                    )
            );
            codeGenerator.transform(psiClass);
            return klass;
        }
    }

    private void updateClassType(Klass classType, PsiClass psiClass) {
        classType.setName(TranspileUtils.getBizClassName(psiClass));
        classType.setStruct(TranspileUtils.isStruct(psiClass));
        classType.setAbstract(psiClass.hasModifierProperty(PsiModifier.ABSTRACT));
    }

    private ClassKind getClassKind(PsiClass psiClass) {
        if (psiClass.isEnum())
            return ClassKind.ENUM;
        if (psiClass.isInterface())
            return ClassKind.INTERFACE;
        if (psiClass.hasAnnotation(ValueType.class.getName()) || psiClass.hasAnnotation(ValueStruct.class.getName()))
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
