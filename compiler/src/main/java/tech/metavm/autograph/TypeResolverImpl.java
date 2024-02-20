package tech.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
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
import java.util.function.Supplier;

import static com.intellij.lang.jvm.types.JvmPrimitiveTypeKind.*;
import static java.util.Objects.requireNonNull;
import static tech.metavm.object.type.ResolutionStage.*;

public class TypeResolverImpl implements TypeResolver {

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

    private final Set<Type> generatedTypes = new HashSet<>();

    private final MyTypeFactory typeFactory = new MyTypeFactory();

    private final Map<ClassType, PsiClass> psiClassMap = new HashMap<>();

    private final IEntityContext context;

    private static final Map<PsiClassType, Supplier<ClassType>> STANDARD_CLASSES = Map.ofEntries(
            Map.entry(TranspileUtil.createType(Entity.class), StandardTypes::getEntityType),
            Map.entry(TranspileUtil.createType(Enum.class), StandardTypes::getEnumType),
            Map.entry(TranspileUtil.createType(Record.class), StandardTypes::getRecordType),
            Map.entry(TranspileUtil.createType(Throwable.class), StandardTypes::getThrowableType),
            Map.entry(TranspileUtil.createType(Exception.class), StandardTypes::getExceptionType),
            Map.entry(TranspileUtil.createType(RuntimeException.class), StandardTypes::getRuntimeExceptionType),
            Map.entry(TranspileUtil.createType(IteratorImpl.class), StandardTypes::getIteratorImplType),
            Map.entry(TranspileUtil.createType(Iterator.class), StandardTypes::getIteratorType),
            Map.entry(TranspileUtil.createType(Iterable.class), StandardTypes::getIterableType),
            Map.entry(TranspileUtil.createType(List.class), StandardTypes::getListType),
            Map.entry(TranspileUtil.createType(ArrayList.class), StandardTypes::getReadWriteListType),
            Map.entry(TranspileUtil.createType(LinkedList.class), StandardTypes::getReadWriteListType),
            Map.entry(TranspileUtil.createType(tech.metavm.util.LinkedList.class), StandardTypes::getReadWriteListType),
            Map.entry(TranspileUtil.createType(ChildList.class), StandardTypes::getChildListType),
            Map.entry(TranspileUtil.createType(Set.class), StandardTypes::getSetType),
            Map.entry(TranspileUtil.createType(Collection.class), StandardTypes::getCollectionType),
            Map.entry(TranspileUtil.createType(Consumer.class), StandardTypes::getConsumerType)
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
                var klass = ReflectionUtils.getBoxedClass(KIND_2_PRIM_CLASS.get(primitiveType.getKind()));
                yield context.getType(klass);
            }
            case PsiClassType classType -> resolveClassType(classType, stage);
            case PsiWildcardType wildcardType -> resolveWildcardType(wildcardType, stage);
            case PsiArrayType arrayType -> resolveArrayType(arrayType, stage);
            case null, default -> throw new InternalException("Invalid PsiType: " + psiType);
        };
    }

    private ArrayType resolveArrayType(PsiArrayType psiArrayType, ResolutionStage stage) {
        try (var entry = ContextUtil.getProfiler().enter("resolveArrayType: " + stage)) {
            return context.getArrayType(resolve(psiArrayType.getComponentType(), stage), ArrayKind.READ_WRITE);
        }
    }

    private Type resolveWildcardType(PsiWildcardType wildcardType, ResolutionStage stage) {
        try (var entry = ContextUtil.getProfiler().enter("resolveWildcardType: " + stage)) {
            if (wildcardType.isBounded()) {
                if (wildcardType.isExtends()) {
                    return context.getUncertainType(
                            StandardTypes.getNeverType(), resolve(wildcardType.getExtendsBound(), stage)
                    );
                } else {
                    return context.getUncertainType(
                            resolve(wildcardType.getSuperBound(), stage),
                            StandardTypes.getNullableAnyType()
                    );
                }
            } else {
                return context.getUncertainType(
                        StandardTypes.getNeverType(),
                        StandardTypes.getNullableAnyType()
                );
            }
        }
    }

    private Type resolveClassType(PsiClassType classType, ResolutionStage stage) {
        try(var entry = ContextUtil.getProfiler().enter("resolveClassType: " + stage)) {
            for (var collClass : COLLECTION_CLASSES) {
                if (TranspileUtil.createType(collClass).isAssignableFrom(classType)) {
                    classType = TranspileUtil.getSuperType(classType, collClass);
                    break;
                }
            }
            if (TranspileUtil.createType(Map.class).isAssignableFrom(classType)) {
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
                ClassType type;
                var rawType = classType.rawType();
                if (STANDARD_CLASSES.containsKey(rawType))
                    type = STANDARD_CLASSES.get(rawType).get();
                else
                    type = resolvePojoClass(psiClass, stage);
                if (classType.getParameters().length > 0) {
                    List<Type> typeArgs = NncUtils.map(
                            classType.getParameters(), this::resolveTypeOnly
                    );
                    return context.getGenericContext().getParameterizedType(type, typeArgs, stage);
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
                        context.getNewCompositeTypes(),
                        context.getGenericContext().getNewTypes()
                )
        );
    }

    public Set<Flow> getGeneratedParameterizedFlows() {
        return context.getGenericContext().getNewFlows();
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
        return NncUtils.findRequired(type.getMethods(), f ->
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
        ClassType type = (ClassType) resolve(declaringType);
        return type.findFieldByCode(field.getName());
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
    public TypeVariable resolveTypeVariable(PsiTypeParameter typeParameter) {
        var builtInTypeVar = tryResolveBuiltinTypeVar(typeParameter);
        if (builtInTypeVar != null)
            return builtInTypeVar;
        var typeVariable = typeParameter.getUserData(Keys.TYPE_VARIABLE);
        if (typeVariable != null)
            return typeVariable;
        var genericDeclaration = tryResolveGenericDeclaration(typeParameter.getOwner());
        if (genericDeclaration != null)
            typeVariable = NncUtils.find(genericDeclaration.getTypeParameters(),
                    tv -> Objects.equals(tv.getCode(), typeParameter.getName()));
        if (typeVariable == null)
            typeVariable = new TypeVariable(null, Objects.requireNonNull(typeParameter.getName()), typeParameter.getName(),
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


    @Nullable
    private TypeVariable tryResolveBuiltinTypeVar(PsiTypeParameter typeParameter) {
        if (typeParameter.getOwner() instanceof PsiClass psiClass) {
            var ownerType = TranspileUtil.createType(psiClass);
            var listType = TranspileUtil.createType(List.class);
            if (listType.isAssignableFrom(ownerType)) {
                return StandardTypes.getListType().getTypeParameters().get(0);
            }
            var setType = TranspileUtil.createType(Set.class);
            if (setType.isAssignableFrom(ownerType)) {
                return StandardTypes.getSetType().getTypeParameters().get(0);
            }
            var mapType = TranspileUtil.createType(Map.class);
            if (mapType.isAssignableFrom(ownerType)) {
                int index = NncUtils.requireNonNull(psiClass.getTypeParameterList())
                        .getTypeParameterIndex(typeParameter);
                return StandardTypes.getMapType().getTypeParameters().get(index);
            }
            var enumType = TranspileUtil.createType(Enum.class);
            if (ownerType.equals(enumType)) {
                return StandardTypes.getEnumType().getTypeParameters().get(0);
            }
        }
        return null;
    }

    private ClassType createMetaClass(PsiClass psiClass) {
        try(var entry = ContextUtil.getProfiler().enter("createMetaClass")) {
            var name = TranspileUtil.getBizClassName(psiClass);
            var category = getTypeCategory(psiClass);
            boolean isTemplate = psiClass.getTypeParameterList() != null
                    && psiClass.getTypeParameterList().getTypeParameters().length > 0;
            var classType = NncUtils.first(context.query(
                    EntityIndexQueryBuilder
                            .newBuilder(ClassType.UNIQUE_CODE)
                            .addEqItem(0, psiClass.getQualifiedName())
                            .build()));
            if (classType != null) {
                if (classType.getCategory() != category)
                    throw new BusinessException(ErrorCode.CHANGING_CATEGORY);
                if (classType.isTemplate() != isTemplate)
                    throw new BusinessException(ErrorCode.CHANGING_IS_TEMPLATE);
            } else {
                classType = ClassTypeBuilder.newBuilder(name, psiClass.getQualifiedName())
                        .category(category)
                        .ephemeral(TranspileUtil.isEphemeral(psiClass))
                        .isTemplate(isTemplate)
                        .build();
                context.bind(classType);
            }
            psiClass.putUserData(Keys.MV_CLASS, classType);
            psiClassMap.put(classType, psiClass);
            generatedTypes.add(classType);
            for (PsiTypeParameter typeParameter : psiClass.getTypeParameters()) {
                resolveTypeVariable(typeParameter).setGenericDeclaration(classType);
            }
            if (psiClass.getSuperClass() != null &&
                    !Objects.equals(psiClass.getSuperClass().getQualifiedName(), Object.class.getName())) {
                classType.setSuperClass((ClassType) resolveTypeOnly(TranspileUtil.getSuperClassType(psiClass)));
            }
            classType.setInterfaces(
                    NncUtils.map(
                            TranspileUtil.getInterfaceTypes(psiClass),
                            it -> (ClassType) resolveTypeOnly(it)
                    )
            );
            codeGenerator.transform(psiClass);
            return classType;
        }
    }

    private TypeCategory getTypeCategory(PsiClass psiClass) {
        return psiClass.isEnum() ? TypeCategory.ENUM
                : (psiClass.isInterface() ? TypeCategory.INTERFACE : TypeCategory.CLASS);
    }

    private ClassType resolvePojoClass(PsiClass psiClass, final ResolutionStage stage) {
        try(var entry = ContextUtil.getProfiler().enter("resolvePojoClass: " + stage)) {
            var metaClass = psiClass.getUserData(Keys.MV_CLASS);
            if (metaClass == null) {
                metaClass = createMetaClass(psiClass);
            }
            processClassType(metaClass, psiClass, stage);
            return metaClass;
        }
    }

    @Override
    public void ensureDeclared(ClassType classType) {
        processClassType(classType, DECLARATION);
    }

    @Override
    public void ensureCodeGenerated(ClassType classType) {
        processClassType(classType, DEFINITION);
    }

    private void processClassType(ClassType metaClass, final ResolutionStage stage) {
        var template = metaClass.getEffectiveTemplate();
        if (template != metaClass && template.getStage().isAfterOrAt(stage))
            context.getGenericContext().getParameterizedType(template, metaClass.getTypeArguments(), stage);
        else if (template.tryGetId() == null) {
            var psiClass = NncUtils.requireNonNull(psiClassMap.get(template));
            processClassType(template, psiClass, stage);
        }
    }

    private void processClassType(ClassType metaClass, PsiClass psiClass, final ResolutionStage stage) {
        try(var ignored = ContextUtil.getProfiler().enter("processClassType: " + stage)) {
            if (stage == INIT) {
                return;
            }
            for (PsiClassType superType : psiClass.getSuperTypes())
                resolve(superType, stage);
            for (ClassType superType : metaClass.getSuperTypes())
                processClassType(superType, stage);
            if (stage.isAfterOrAt(DECLARATION) && metaClass.getStage().isBefore(DECLARATION)) {
                codeGenerator.generateDecl(psiClass, this);
                context.getGenericContext().generateDeclarations(metaClass);
            }
            if (stage.isAfterOrAt(DEFINITION) && metaClass.getStage().isBefore(DEFINITION) && !metaClass.isInterface()) {
                codeGenerator.generateCode(psiClass, this);
                context.getGenericContext().generateCode(metaClass);
            }
        }
//        metaClass.setStage(stage);
    }

    private class MyTypeFactory extends TypeFactory {

        private final Set<Type> types = new HashSet<>();

        @Override
        public Type getType(java.lang.reflect.Type javaType) {
            return context.getDefContext().getType(javaType);
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
