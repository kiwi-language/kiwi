package tech.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import tech.metavm.entity.ModelIdentity;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.ReflectUtils;

import java.util.HashMap;
import java.util.Map;

import static com.intellij.lang.jvm.types.JvmPrimitiveTypeKind.*;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("UnstableApiUsage")
public class MockTypeResolver implements TypeResolver {

    public static final Map<JvmPrimitiveTypeKind, Class<?>> KIND_2_PRIM_CLASS = Map.of(
            INT, int.class,
            SHORT, short.class,
            BOOLEAN, boolean.class,
            LONG, long.class,
            CHAR, char.class,
            FLOAT, float.class,
            DOUBLE, double.class
    );

    private static final Map<Class<?>, Class<?>> CLASS_MAP = Map.of(
            Short.class, Integer.class,
            Byte.class, Integer.class,
            Float.class, Double.class
    );

    public static final Map<Class<?>, PrimitiveType> PRIMITIVE_TYPE_MAP = Map.of(
            Integer.class, new PrimitiveType(PrimitiveKind.INT),
            Long.class, new PrimitiveType(PrimitiveKind.LONG),
            Double.class, new PrimitiveType(PrimitiveKind.DOUBLE),
            String.class, new PrimitiveType(PrimitiveKind.STRING),
            Boolean.class, new PrimitiveType(PrimitiveKind.BOOLEAN)
    );

    private final Map<String, ClassType> classTypeMap = new HashMap<>();

    private final StdAllocators stdAllocators;

    public MockTypeResolver() {
        this(null);
    }

    public MockTypeResolver(StdAllocators stdAllocators) {
        this.stdAllocators = stdAllocators;
    }

    @Override
    public Type resolve(PsiType psiType) {
        Type type;
        String className;
        boolean primitive;
        if (psiType instanceof PsiPrimitiveType primitiveType) {
            var klass = ReflectUtils.getBoxedClass(KIND_2_PRIM_CLASS.get(primitiveType.getKind()));
            if (CLASS_MAP.containsKey(klass)) {
                klass = CLASS_MAP.get(klass);
            }
            className = klass.getName();
            primitive = true;
            type = PRIMITIVE_TYPE_MAP.get(klass);
        } else if (psiType instanceof PsiClassType classType) {
            var psiClass = requireNonNull(classType.resolve());
            if (ReflectUtils.isPrimitiveBoxClassName(psiClass.getQualifiedName())) {
                return PRIMITIVE_TYPE_MAP.get(ReflectUtils.classForName(psiClass.getQualifiedName()));
            }
            className = psiClass.getQualifiedName();
            primitive = false;
            type = resolvePojoClass(psiClass);
        } else if (psiType instanceof PsiArrayType arrayType) {
            var componentType = resolve(arrayType.getComponentType());
            type = componentType.getArrayType();
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
        }
        return type;
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

    @Override
    public void addType(String name, ClassType classType) {
        classTypeMap.put(name, classType);
    }

    private ClassType resolvePojoClass(PsiClass psiClass) {
        var metaClass = classTypeMap.get(psiClass.getQualifiedName());
        if (metaClass != null) return metaClass;
        var psiFile = psiClass.getContainingFile();
        AstToFlow astToFlow = new AstToFlow(this);
        psiFile.accept(astToFlow);
        classTypeMap.putAll(astToFlow.getClasses());
        return classTypeMap.get(psiClass.getQualifiedName());
    }

}
