package tech.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.*;
import tech.metavm.object.meta.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.intellij.lang.jvm.types.JvmPrimitiveTypeKind.*;
import static com.intellij.lang.jvm.types.JvmPrimitiveTypeKind.DOUBLE;
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

    @Override
    public Type resolve(PsiType psiType) {
        if (psiType instanceof PsiPrimitiveType primitiveType) {
            var klass = ReflectUtils.getBoxedClass(KIND_2_PRIM_CLASS.get(primitiveType.getKind()));
            if (CLASS_MAP.containsKey(klass)) {
                klass = CLASS_MAP.get(klass);
            }
            return PRIMITIVE_TYPE_MAP.get(klass);
        } else if (psiType instanceof PsiClassType classType) {
            var psiClass = requireNonNull(classType.resolve());
            if (ReflectUtils.isPrimitiveBoxClassName(psiClass.getQualifiedName())) {
                return PRIMITIVE_TYPE_MAP.get(ReflectUtils.classForName(psiClass.getQualifiedName()));
            }
            return resolvePojoClass(psiClass);
        }
        else if(psiType instanceof PsiArrayType arrayType) {
            var componentType = resolve(arrayType.getComponentType());
            return componentType.getArrayType();
        }
        throw new InternalException("Resolution for " + psiType.getClass().getName() + " is not yet supported");
    }

    @Override
    public void addType(String name, ClassType classType) {
        classTypeMap.put(name, classType);
    }

    private ClassType resolvePojoClass(PsiClass psiClass) {
        var metaClass = classTypeMap.get(psiClass.getQualifiedName());
        if(metaClass != null) return metaClass;
        var psiFile = psiClass.getContainingFile();
        AstToFlow astToFlow = new AstToFlow(this);
        psiFile.accept(astToFlow);
        classTypeMap.putAll(astToFlow.getClasses());
        return classTypeMap.get(psiClass.getQualifiedName());
    }

}
