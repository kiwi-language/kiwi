package tech.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import tech.metavm.entity.GenericDeclaration;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.entity.ModelIdentity;
import tech.metavm.flow.Flow;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.*;

import static com.intellij.lang.jvm.types.JvmPrimitiveTypeKind.*;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnstableApiUsage")
public class MockTypeResolver implements TypeResolver {

    public static final Set<String> PRIM_CLASS_NAMES = Set.of(
            String.class.getName(),
            Date.class.getName()
    );

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

    private final StdAllocators stdAllocators;

    private PsiClassType parameterizedEnumType;

    private final Compiler compiler;

    private final Set<Type> generatedTypes = new HashSet<>();

    private final MyTypeFactory typeFactory = new MyTypeFactory();

    private final Map<ClassType, PsiClass> psiClassMap = new HashMap<>();

    private final IEntityContext context;

    public MockTypeResolver(IEntityContext context, StdAllocators stdAllocators) {
        this.stdAllocators = stdAllocators;
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

    public Type resolve(PsiType psiType, int stage) {
        switch (psiType) {
            case PsiPrimitiveType primitiveType -> {
                var klass = ReflectUtils.getBoxedClass(KIND_2_PRIM_CLASS.get(primitiveType.getKind()));
                return context.getType(klass);
            }
            case PsiClassType classType -> {
                var psiClass = requireNonNull(classType.resolve());
                if (psiClass instanceof PsiTypeParameter typeParameter) {
                    return resolveTypeVariable(typeParameter);
                } else {
                    if (ReflectUtils.isPrimitiveBoxClassName(psiClass.getQualifiedName())
                            || PRIM_CLASS_NAMES.contains(psiClass.getQualifiedName())) {
                        return context.getType(ReflectUtils.classForName(psiClass.getQualifiedName()));
                    } else if (TranspileUtil.isObjectClass(psiClass)) {
                        return StandardTypes.getObjectType();
                    } else if (TranspileUtil.createType(List.class).isAssignableFrom(psiType)) {
                        var listType = TranspileUtil.getSuperType(psiType, List.class);
                        return TypeUtil.getListType(resolve(listType.getParameters()[0]), context);
                    } else if (TranspileUtil.createType(Set.class).isAssignableFrom(psiType)) {
                        var setType = TranspileUtil.getSuperType(psiType, Set.class);
                        return TypeUtil.getSetType(resolve(setType.getParameters()[0]), context);
                    } else if (TranspileUtil.createType(Map.class).isAssignableFrom(psiType)) {
                        var mapType = TranspileUtil.getSuperType(psiType, Map.class);
                        return TypeUtil.getMapType(
                                resolve(mapType.getParameters()[0]),
                                resolve(mapType.getParameters()[1]),
                                context
                        );
                    } else if (TranspileUtil.createType(Enum.class).equals(psiType)) {
                        return StandardTypes.getEnumType();
                    } else if (createParameterizedEnumType().equals(psiType)) {
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
            case PsiArrayType arrayType -> {
                var componentType = resolve(arrayType.getComponentType(), stage);
                return TypeUtil.getArrayType(componentType);
            }
            case null, default -> throw new InternalException("Invalid PsiType: " + psiType);
        }
    }

    public Set<Type> getGeneratedTypes() {
        return NncUtils.mergeSets(generatedTypes, typeFactory.getGeneratedTypes());
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
        return type.getFlow(
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
        PsiType declaringType = TranspileUtil.getPsiElementFactory().createType(
                requireNonNull(field.getContainingClass()));
        ClassType type = (ClassType) resolve(declaringType);
        return type.getFieldByCode(field.getName());
    }

    private Long getTypeId(Class<? extends Type> typeClass, String className) {
        if (stdAllocators == null) {
            return null;
        }
        return stdAllocators.getId(new ModelIdentity(typeClass, className));
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
        typeVariable = new TypeVariable(null, typeParameter.getName(), typeParameter.getName());
        typeParameter.putUserData(Keys.TYPE_VARIABLE, typeVariable);
        generatedTypes.add(typeVariable);
        typeVariable.setBounds(NncUtils.map(
                typeParameter.getExtendsListTypes(),
                this::resolveTypeOnly
        ));
//        typeVar.setGenericDeclaration(resolveGenericDeclaration(typeParameter.getOwner(), context));
        return typeVariable;
    }

    private ClassType createMetaClass(PsiClass psiClass) {
        var name = TranspileUtil.getBizClassName(psiClass);
        ClassType metaClass = ClassBuilder.newBuilder(name, psiClass.getName())
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
            metaClass.setSuperType((ClassType) resolveTypeOnly(TranspileUtil.getSuperClassType(psiClass)));
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
        if(template.getId() == null) {
            var psiClass = NncUtils.requireNonNull(psiClassMap.get(template));
            procesClassType(template, psiClass, stage);
        }
    }

    private void procesClassType(ClassType metaClass, PsiClass psiClass, final int stage) {
        if (stage == 0) {
            return;
        }
        for (ClassType superType : metaClass.getSupers()) {
            procesClassType(superType, stage);
        }
        int currentStage = NncUtils.requireNonNull(psiClass.getUserData(Keys.RESOLVE_STAGE));
        if (currentStage < 1) {
            compiler.generateDecl(psiClass, this);
            context.getGenericContext().generateDeclarations(metaClass);
        }
        if (stage == 1) {
            return;
        }
        if (currentStage < 2 && !metaClass.isInterface()) {
            compiler.generateCode(psiClass, this);
            context.getGenericContext().generateCode(metaClass);
        }
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
