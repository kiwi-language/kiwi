package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.api.Value;
import org.metavm.api.ValueStruct;
import org.metavm.api.builtin.Password;
import org.metavm.entity.*;
import org.metavm.flow.Flow;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.type.*;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;
import static org.metavm.object.type.ResolutionStage.*;
import static org.metavm.object.type.Types.getIntersectionType;

public class TypeResolverImpl implements TypeResolver {

    private static final String API_PKG_PREFIX = "org.metavm.api.";

    public static final String JAVA_PKG_PREFIX = "java.";

    private static final String LANG_PKG_PREFIX = "org.metavm.api.lang.";

    private static final Logger logger = LoggerFactory.getLogger(TypeResolverImpl.class);

    private static final Set<String> PRIM_CLASS_NAMES = Set.of(
            String.class.getName(),
            Date.class.getName()
    );

    public static final Set<Class<?>> FORBIDDEN_CLASSES = Set.of(
            System.class
    );

    private PsiClassType parameterizedEnumType;

    private final CodeGenerator codeGenerator;

    private final Set<Klass> generatedKlasses = new HashSet<>();

    private final Map<Klass, PsiClass> psiClassMap = new HashMap<>();

    private final Map<PsiCapturedWildcardType, CapturedType> capturedTypeMap = new HashMap<>();

    private final Map<CapturedType, PsiCapturedWildcardType> capturedTypeReverseMap = new HashMap<>();

    private static final Map<PsiClassType, Supplier<Klass>> STANDARD_CLASSES;

    static {
        var map = new HashMap<PsiClassType, Supplier<Klass>>();
        for (StdKlass def : StdKlass.values()) {
            map.put(TranspileUtils.createClassType(def.getJavaClass()), def::get);
        }
        map.put(TranspileUtils.createClassType(LinkedList.class), StdKlass.arrayList::get);
        STANDARD_CLASSES = Collections.unmodifiableMap(map);
    }

    public TypeResolverImpl() {
        codeGenerator = new CodeGenerator();
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

    @Override
    public Type resolveNullable(PsiType psiType, ResolutionStage stage) {
        var type = resolve(psiType, stage);
        if(!(psiType instanceof PsiPrimitiveType))
            type = Types.getNullableType(type);
        return type;
    }

    @Override
    public Type resolve(PsiType psiType, ResolutionStage stage) {
        return switch (psiType) {
            case PsiPrimitiveType primitiveType -> resolvePrimitiveType(primitiveType);
            case PsiClassType classType -> resolveClassType(classType, stage);
            case PsiWildcardType wildcardType -> resolveWildcardType(wildcardType, stage);
            case PsiArrayType arrayType -> resolveArrayType(arrayType, stage);
            case PsiCapturedWildcardType capturedWildcardType -> resolveCapturedType(capturedWildcardType, stage);
            case PsiIntersectionType intersectionType -> resolveIntersectionType(intersectionType, stage);
            case PsiDisjunctionType disjunctionType -> resolveDisjunctionType(disjunctionType, stage);
            case PsiLambdaExpressionType lambdaExpressionType ->
                    resolve(Objects.requireNonNull(lambdaExpressionType.getExpression().getFunctionalInterfaceType()), stage);
            default -> throw new InternalException("Invalid PsiType: " + Objects.requireNonNull(psiType).getClass().getName());
        };
    }

    private Type resolvePrimitiveType(PsiPrimitiveType primitiveType) {
        if (primitiveType.equals(PsiType.NULL))
            return Types.getNullType();
        else if (primitiveType.equals(PsiType.BYTE))
            return Types.getByteType();
        else if (primitiveType.equals(PsiType.SHORT))
            return Types.getShortType();
        else if (primitiveType.equals(PsiType.INT))
            return Types.getIntType();
        else if (primitiveType.equals(PsiType.LONG))
            return Types.getLongType();
        else if (primitiveType.equals(PsiType.FLOAT))
            return Types.getFloatType();
        else if (primitiveType.equals(PsiType.DOUBLE))
            return Types.getDoubleType();
        else if (primitiveType.equals(PsiType.BOOLEAN))
            return Types.getBooleanType();
        else if (primitiveType.equals(PsiType.CHAR))
            return Types.getCharType();
        else if (primitiveType.equals(PsiType.VOID))
            return Types.getVoidType();
        else
            throw new IllegalStateException("Unrecognized primitive type: " + primitiveType.getCanonicalText());
    }

    private ArrayType resolveArrayType(PsiArrayType psiArrayType, ResolutionStage stage) {
        return new ArrayType(
                resolveNullable(psiArrayType.getComponentType(), stage),
                ArrayKind.DEFAULT
        );
    }

    private IntersectionType resolveIntersectionType(PsiIntersectionType psiIntersectionType, ResolutionStage stage) {
        return new IntersectionType(
                Utils.mapToSet(List.of(psiIntersectionType.getConjuncts()), t -> resolve(t, stage))
        );
    }

    private UnionType resolveDisjunctionType(PsiDisjunctionType psiDisjunctionType, ResolutionStage stage) {
        return new UnionType(
                Utils.mapToSet(psiDisjunctionType.getDisjunctions(), t -> resolve(t, stage))
        );
    }

    private UncertainType resolveWildcardType(PsiWildcardType wildcardType, ResolutionStage stage) {
        if (wildcardType.isBounded()) {
            if (wildcardType.isExtends()) {
                return UncertainType.createUpperBounded(resolve(wildcardType.getExtendsBound(), stage));
            } else {
                return new UncertainType(resolve(wildcardType.getSuperBound(), stage), Types.getAnyType());
            }
        } else {
            /*
            The asterisk should ideally resolve to [never, any|null]. However, due to limitations in the current implementation,
            we use [never, any] until the compiler is refactored.
            */
//            return UncertainType.asterisk;
            return new UncertainType(Types.getNeverType(), Types.getAnyType());
        }
    }

    private Type resolveCapturedType(PsiCapturedWildcardType psiCapturedType, ResolutionStage stage) {
        var resolved = capturedTypeMap.get(psiCapturedType);
        if (resolved != null)
            return resolved;
        var psiMethod = Objects.requireNonNull(TranspileUtils.findParent(psiCapturedType.getContext(), PsiMethod.class));
        var method = Objects.requireNonNull(psiMethod.getUserData(Keys.Method));
        var capturedTypeVar = new CapturedTypeVariable(
                TmpId.random(),
                TmpId.randomString(),
                resolveWildcardType(psiCapturedType.getWildcard(), stage),
                resolveTypeVariable(psiCapturedType.getTypeParameter()).getVariable().getReference(),
                method
        );
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

    @Override
    public boolean isBuiltinClass(PsiClass psiClass) {
        var qualifiedName = psiClass.getQualifiedName();
        if (qualifiedName != null && (qualifiedName.startsWith(API_PKG_PREFIX) || qualifiedName.startsWith(JAVA_PKG_PREFIX))) {
            var javaClass = TranspileUtils.getJavaClass(psiClass);
            return ModelDefRegistry.getDefContext().tryGetKlass(javaClass) != null;
        } else
            return false;
    }

    private Type resolveClassType(PsiClassType classType, ResolutionStage stage) {
        try (var entry = ContextUtil.getProfiler().enter("resolveClassType: " + stage)) {
            var classTypeText = classType.getCanonicalText();
            var psiClass = requireNonNull(classType.resolve(),
                    () -> "Failed to resolve class type " + classTypeText);
            var qualName = psiClass.getQualifiedName();
            if (psiClass instanceof PsiTypeParameter typeParameter)
                return resolveTypeVariable(typeParameter);
            else if (TranspileUtils.isObjectClass(psiClass))
                return Types.getAnyType();
            else if (TranspileUtils.matchType(classType, Password.class))
                return Types.getPasswordType();
            else if(TranspileUtils.matchType(classType, Class.class))
                return StdKlass.klass.type();
            else if (qualName != null && (ReflectionUtils.isPrimitiveBoxClassName(qualName)
                    || PRIM_CLASS_NAMES.contains(qualName)))
                return ModelDefRegistry.getType(TranspileUtils.getJavaClass(psiClass));
            else {
                var klass = tryResolveBuiltinClass(classType.rawType());
                if (klass == null)
                    klass = resolvePojoClass(psiClass, stage);
                if (psiClass.getContainingClass() != null || classType.getParameters().length > 0) {
                    GenericDeclarationRef owner;
                    PsiClass enclosingClass = psiClass.getContainingClass();
                    PsiMethod enclosingMethod;
                    if(enclosingClass != null) {
                        var generics = classType.resolveGenerics();
                        var ownerPsiType = generics.getSubstitutor().substitute(TranspileUtils.createTemplateType(enclosingClass));
                        owner = ((ClassType) resolveTypeOnly(ownerPsiType));
                    } else if((enclosingMethod = TranspileUtils.getEnclosingMethod(psiClass)) != null)
                        owner = Objects.requireNonNull(enclosingMethod.getUserData(Keys.Method)).getRef();
                    else
                        owner = null;
                    var typeArgs = new ArrayList<Type>();
                    var templateType = TranspileUtils.createTemplateType(psiClass);
                    if(!Arrays.equals(templateType.getParameters(), classType.getParameters())) {
                        for (int i = 0; i < classType.getParameterCount(); i++) {
                            typeArgs.add(
                                    adjustTypeArgument(
                                            klass.getTypeParameters().get(i),
                                            resolveTypeOnly(classType.getParameters()[i])
                                    )
                            );
                        }
                    }
                    return new KlassType(owner, klass, typeArgs);
                } else {
                    return klass.getType();
                }
            }
        }
    }

    /*
     * It's possible to have a parameterized type with an uncertain type argument that is not within the bound
     * of the type argument. This method adjust such type argument to make it fit into the bound of the
     * type parameter.
     */
    private Type adjustTypeArgument(TypeVariable typeParameter, Type typeArgument) {
        if(typeArgument instanceof UncertainType uncertainType
                && !typeParameter.getUpperBound().isAssignableFrom(uncertainType.getUpperBound())) {
            var adjustedBound = getIntersectionType(List.of(typeParameter.getUpperBound(), uncertainType.getUpperBound()));
            return new UncertainType(uncertainType.getLowerBound(), adjustedBound);
        }
        else
            return typeArgument;
    }

    public Set<Klass> getGeneratedKlasses() {
        return generatedKlasses;
    }

    public Flow resolveFlow(PsiMethod method) {
        var klass = ((ClassType) resolveDeclaration(TranspileUtils.createType(method.getContainingClass()))).getKlass();
        return Utils.findRequired(klass.getMethods(), f ->
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
        Klass klass = ((ClassType) resolve(declaringType)).getKlass();
        return klass.findFieldByName(field.getName());
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
                var found =  Utils.find(builtinKlass.getMethods(), m ->
                                m.getInternalName(null).equals(internalName));
//                        () -> "Can not find method " + internalName + " in class " + builtinKlass.getTypeDesc());
                if(found == null)
                    throw new NullPointerException("Can not find method " + internalName + " in class " + builtinKlass.getTypeDesc());
                return found;
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
            var javaClass = TranspileUtils.getJavaClass(psiClass);
            if(FORBIDDEN_CLASSES.contains(javaClass))
                throw new CompilerException("class '" + javaClass.getName() + "' is not available in MetaVM");
            return ModelDefRegistry.getDefContext().tryGetKlass(javaClass);
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
            typeVariable = Utils.find(genericDeclaration.getTypeParameters(),
                    tv -> Objects.equals(tv.getName(), typeParameter.getName()));
        if (typeVariable == null)
            typeVariable = new TypeVariable(TmpId.random(), Objects.requireNonNull(typeParameter.getName()),
                    genericDeclaration != null ? genericDeclaration : DummyGenericDeclaration.INSTANCE);
        typeParameter.putUserData(Keys.TYPE_VARIABLE, typeVariable);
        typeVariable.setBounds(Utils.map(
                typeParameter.getExtendsListTypes(),
                this::resolveTypeOnly
        ));
//        typeVariable.setGenericDeclaration(resolveGenericDeclaration(typeParameter.getOwner(), context));
        return typeVariable.getType();
    }

    @Override
    public void addGeneratedKlass(Klass klass) {
        generatedKlasses.add(klass);
    }

    @Nullable
    private TypeVariable tryResolveBuiltinTypeVar(PsiTypeParameter typeParameter) {
        if (typeParameter.getOwner() instanceof PsiClass psiClass) {
            var className = psiClass.getQualifiedName();
            if(className != null && (className.startsWith(JAVA_PKG_PREFIX) || className.startsWith(API_PKG_PREFIX))) {
                var javaClass = TranspileUtils.getJavaClass(psiClass);
                var klass = ModelDefRegistry.getDefContext().tryGetKlass(javaClass);
                if(klass != null) {
                    int index = Objects.requireNonNull(psiClass.getTypeParameterList())
                            .getTypeParameterIndex(typeParameter);
                    return klass.getTypeParameters().get(index);
                }
            }
        }
        return null;
    }

    @Override
    public Klass getKlass(PsiClass psiClass) {
        var klass = psiClass.getUserData(Keys.MV_CLASS);
        if (klass == null) {
            klass = createMvClass(psiClass);
            psiClass.putUserData(Keys.MV_CLASS, klass);
        }
        return klass;
    }

    private Klass createMvClass(PsiClass psiClass) {
        try (var ignored = ContextUtil.getProfiler().enter("createMvClass")) {
            var qualName = psiClass.getQualifiedName();
            Utils.require(qualName == null || !qualName.startsWith("org.metavm.api."),
                    () -> "Can not create metavm class for API class: " + qualName);
            var name = TranspileUtils.getBizClassName(psiClass);
            var kind = getClassKind(psiClass);
            boolean isTemplate = psiClass.getTypeParameterList() != null
                    && psiClass.getTypeParameterList().getTypeParameters().length > 0;
            var tag = (int) TranspileUtils.getEntityAnnotationAttr(psiClass, "tag", -1);
            var parent = TranspileUtils.getProperParent(psiClass, Set.of(PsiMethod.class, PsiClass.class));
            Klass declaringKlass = null;
            if(parent instanceof PsiClass)
                declaringKlass = requireNonNull(parent.getUserData(Keys.MV_CLASS));
            var klass = KlassBuilder.newBuilder(TmpId.random(), name, qualName)
                    .kind(kind)
                    .ephemeral(TranspileUtils.isEphemeral(psiClass) || TranspileUtils.isLocalClass(psiClass))
                    .struct(TranspileUtils.isStruct(psiClass))
                    .searchable(TranspileUtils.isSearchable(psiClass))
                    .isTemplate(isTemplate)
                    .scope(declaringKlass)
                    .isAbstract(psiClass.hasModifierProperty(PsiModifier.ABSTRACT))
                    .sourceTag(tag != -1 ? tag : null)
                    .build();
            psiClass.putUserData(Keys.MV_CLASS, klass);
            psiClassMap.put(klass, psiClass);
            addGeneratedKlass(klass);
            for (PsiTypeParameter typeParameter : psiClass.getTypeParameters()) {
                resolveTypeVariable(typeParameter).getVariable().setGenericDeclaration(klass);
            }
            return klass;
        }
    }

    private void updateClassType(Klass classType, PsiClass psiClass) {
        classType.setName(TranspileUtils.getBizClassName(psiClass));
        classType.setStruct(TranspileUtils.isStruct(psiClass));
        classType.setAbstract(psiClass.hasModifierProperty(PsiModifier.ABSTRACT));
    }

    private ClassKind getClassKind(PsiClass psiClass) {
        if (TranspileUtils.isEnum(psiClass))
            return ClassKind.ENUM;
        if (psiClass.isInterface())
            return ClassKind.INTERFACE;
        if (psiClass.hasAnnotation(Value.class.getName()) || psiClass.hasAnnotation(ValueStruct.class.getName()))
            return ClassKind.VALUE;
        return ClassKind.CLASS;
    }

    private Klass resolvePojoClass(PsiClass psiClass, final ResolutionStage stage) {
        try (var entry = ContextUtil.getProfiler().enter("resolvePojoClass: " + stage)) {
            var metaClass = psiClass.getUserData(Keys.MV_CLASS);
            if (metaClass == null)
                metaClass = createMvClass(psiClass);
            else
                updateClassType(metaClass, psiClass);
            processKlass(metaClass, psiClass, stage);
            return metaClass;
        }
    }

    @Override
    public void ensureDeclared(Klass classType) {
        processKlass(classType, DECLARATION);
    }

    @Override
    public void ensureCodeGenerated(Klass classType) {
        processKlass(classType, DEFINITION);
    }

    @Override
    public void mapCapturedType(PsiCapturedWildcardType psiCapturedWildcardType, CapturedType type) {
        capturedTypeMap.put(psiCapturedWildcardType, type);
    }

    private void processKlass(Klass klass, final ResolutionStage stage) {
//        if (klass.tryGetId() == null) {
            var psiClass = Objects.requireNonNull(psiClassMap.get(klass),
                    () -> "Cannot find PsiClass for klass " + klass.getTypeDesc());
            processKlass(klass, psiClass, stage);
//        }
    }

    private void processKlass(Klass klass, PsiClass psiClass, final ResolutionStage stage) {
        try (var ignored = ContextUtil.getProfiler().enter("processKlass: " + stage)) {
            if (stage == INIT) {
                return;
            }
            for (PsiClassType superType : psiClass.getSuperTypes())
                resolve(superType, stage);
            if (stage.isAfterOrAt(DECLARATION) && klass.getStage().isBefore(DECLARATION)) {
                codeGenerator.generateDecl(psiClass, this);
            }
            if (stage.isAfterOrAt(DEFINITION) && klass.getStage().isBefore(DEFINITION)) {
                codeGenerator.generateCode(psiClass, this);
            }
        }
//        metaClass.setStage(stage);
    }

}
