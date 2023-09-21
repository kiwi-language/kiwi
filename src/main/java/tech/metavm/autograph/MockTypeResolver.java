package tech.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.entity.ModelIdentity;
import tech.metavm.object.instance.ArrayType;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.Table;

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

    private static final Map<Class<?>, Class<?>> CLASS_MAP = Map.of(
            Short.class, Long.class,
            Byte.class, Long.class,
            Integer.class, Long.class,
            Float.class, Double.class,
            List.class, Table.class
    );

    public static final Map<Class<?>, PrimitiveType> PRIMITIVE_TYPE_MAP = Map.of(
            Integer.class, new PrimitiveType(PrimitiveKind.LONG),
            Long.class, new PrimitiveType(PrimitiveKind.LONG),
            Double.class, new PrimitiveType(PrimitiveKind.DOUBLE),
            String.class, new PrimitiveType(PrimitiveKind.STRING),
            Boolean.class, new PrimitiveType(PrimitiveKind.BOOLEAN),
            Date.class, new PrimitiveType(PrimitiveKind.TIME),
            Void.class, new PrimitiveType(PrimitiveKind.VOID)
    );

    private final StdAllocators stdAllocators;

    private final Compiler compiler = new Compiler();

    public MockTypeResolver(StdAllocators stdAllocators) {
        this.stdAllocators = stdAllocators;
    }

    @Override
    public Type resolveTypeOnly(PsiType psiType, IEntityContext context) {
        return resolve(psiType, 0, context);
    }

    @Override
    public Type resolveDeclaration(PsiType psiType, IEntityContext context) {
        return resolve(psiType, 1, context);
    }

    @Override
    public Type resolve(PsiType psiType, IEntityContext context) {
        return resolve(psiType, 2, context);
    }

    public Type resolve(PsiType psiType, int stage, IEntityContext context) {
        Type type;
        String className;
        boolean primitive;
        if (psiType instanceof PsiPrimitiveType primitiveType) {
            var klass = ReflectUtils.getBoxedClass(KIND_2_PRIM_CLASS.get(primitiveType.getKind()));
            return context.getType(klass);
//            if (CLASS_MAP.containsKey(klass)) {
//                klass = CLASS_MAP.get(klass);
//            }
//            className = klass.getName();
//            primitive = true;
//            type = PRIMITIVE_TYPE_MAP.get(klass);
        } else if (psiType instanceof PsiClassType classType) {
            var psiClass = requireNonNull(classType.resolve());
            var psiTye = TranspileUtil.createType(psiClass);
            className = NncUtils.requireNonNull(psiClass.getQualifiedName());
            if (ReflectUtils.isPrimitiveBoxClassName(psiClass.getQualifiedName())
                    || PRIM_CLASS_NAMES.contains(psiClass.getQualifiedName())) {
//                type = PRIMITIVE_TYPE_MAP.get(ReflectUtils.classForName(psiClass.getQualifiedName()));
//                primitive = true;
                return context.getType(ReflectUtils.classForName(psiClass.getQualifiedName()));
            }
            else if(className.equals(Object.class.getName())) {
//                type = ModelDefRegistry.getType(Object.class);
//                primitive = false;
                return StandardTypes.getObjectType();
            }
            else if (TranspileUtil.createType(List.class).isAssignableFrom(psiType)) {
                var listType = TranspileUtil.getSuperType(psiType, List.class);
                type = TypeUtil.getListType(resolve(listType.getParameters()[0], context), context);
                primitive = false;
            }
            else if(TranspileUtil.createType(Set.class).isAssignableFrom(psiType)) {
                var setType = TranspileUtil.getSuperType(psiType, Set.class);
                type = TypeUtil.getSetType(resolve(setType.getParameters()[0], context), context);
                primitive = false;
            }
            else if(TranspileUtil.createType(Map.class).isAssignableFrom(psiType)) {
                var mapType = TranspileUtil.getSuperType(psiType, Map.class);
                type = TypeUtil.getMapType(
                        resolve(mapType.getParameters()[0], context),
                        resolve(mapType.getParameters()[1], context),
                        context
                );
                primitive = false;
            }
            else {
                primitive = false;
                type = resolvePojoClass(psiClass, stage, context);
            }
        } else if (psiType instanceof PsiArrayType arrayType) {
            var componentType = resolve(arrayType.getComponentType(), stage, context);
            type = TypeUtil.getArrayType(componentType);
            className = null;
            primitive = false;
        } else {
            throw new InternalException("Resolution for " + psiType.getClass().getName() + " is not yet supported");
        }
        if (type.getId() == null && className != null) {
            Long id = getTypeId(className, primitive);
            if (id != null) {
                type.initId(id);
            }
            Long nullableTypeId = stdAllocators.getId(new ModelIdentity(
                    UnionType.class, className + "|tech.metavm.util.Null"
            ));
            if (nullableTypeId != null) {
                TypeUtil.getNullableType(type).initId(nullableTypeId);
            }
            Long arrayTypeId = stdAllocators.getId(new ModelIdentity(
                    ArrayType.class, className + "[]"
            ));
            if (arrayTypeId != null) {
                TypeUtil.getArrayType(type).initId(arrayTypeId);
            }
        }
        return type;
    }

    private boolean isArrayType(PsiType psiType) {
        if (psiType instanceof PsiClassType classType) {
            var psiClass = requireNonNull(classType.resolve());
            return Objects.equals(psiClass.getQualifiedName(), List.class.getName());
        }
        return false;
    }

    @Override
    public Field resolveField(PsiField field, IEntityContext context) {
        PsiType declaringType = TranspileUtil.getPsiElementFactory().createType(
                requireNonNull(field.getContainingClass()));
        ClassType type = (ClassType) resolve(declaringType, context);
        return type.getFieldByCode(field.getName());
    }

    private Long getTypeId(String className, boolean primitive) {
        if (stdAllocators == null) {
            return null;
        }
        if (primitive) {
            return stdAllocators.getId(new ModelIdentity(PrimitiveType.class, className));
        } else {
            return stdAllocators.getId(new ModelIdentity(ClassType.class, className));
        }
    }

    private ClassType createMetaClass(PsiClass psiClass, IEntityContext context) {
        var name = TranspileUtil.getBizClassName(psiClass);
        List<ClassType> interfaces = NncUtils.map(
                psiClass.getInterfaces(),
                it -> (ClassType) resolveTypeOnly(TranspileUtil.createType(it), context)
        );
        ClassType metaClass = ClassBuilder.newBuilder(name, psiClass.getName())
                                .superType(psiClass.isEnum() ? ModelDefRegistry.getClassType(Enum.class) : null)
                                .interfaces(interfaces)
                                .category(psiClass.isEnum() ?
                                        TypeCategory.ENUM
                                        : (psiClass.isInterface() ? TypeCategory.INTERFACE : TypeCategory.CLASS))
                                .build();
        compiler.transform(psiClass);
        psiClass.putUserData(Keys.META_CLASS, metaClass);
        psiClass.putUserData(Keys.RESOLVE_STAGE, 0);
        return metaClass;
    }

    private ClassType resolvePojoClass(PsiClass psiClass, final int stage, IEntityContext context) {
        var metaClass = psiClass.getUserData(Keys.META_CLASS);
        if (metaClass == null) {
            metaClass = createMetaClass(psiClass, context);
        }
        if (stage == 0) {
            return metaClass;
        }
        int currentStage = NncUtils.requireNonNull(psiClass.getUserData(Keys.RESOLVE_STAGE));
        var file = psiClass.getContainingFile();
        if (currentStage < 1) {
            compiler.generateDecl(file, this, context);
        }
        if (stage == 1) {
            return metaClass;
        }
        if (currentStage < 2 && !metaClass.isInterface()) {
            compiler.generateCode(file, this, context);
        }
        return metaClass;
    }

}
