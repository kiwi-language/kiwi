package tech.metavm.transpile;

import tech.metavm.transpile.ir.IRClass;
import tech.metavm.transpile.ir.IRMethod;
import tech.metavm.transpile.ir.IRType;
import tech.metavm.transpile.ir.TypeStore;
import tech.metavm.util.InternalException;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IRTypeUtil {

    public static final List<String> SOURCE_ROOTS = List.of(
            "/Users/leen/workspace/object/src/main/java/",
            "/Users/leen/workspace/object/src/test/java/"
    );

    private static final String[] TEST_CLASSES = new String[] {
            "tech.metavm.mocks.Foo"
    };

    private static final TypeStore typeStore = new TypeStore(
            SOURCE_ROOTS,
            new HashSet<>(Arrays.asList(TEST_CLASSES)),
            Set.of()
    );

    public static IRClass unbox(IRClass klass) {
        if(klass.equals(fromClass(Long.class))) {
            return longClass();
        }
        if(klass.equals(fromClass(Integer.class))) {
            return intClass();
        }
        if(klass.equals(fromClass(Short.class))) {
            return shortClass();
        }
        if(klass.equals(fromClass(Byte.class))) {
            return byteClass();
        }
        if(klass.equals(fromClass(Double.class))) {
            return doubleClass();
        }
        if(klass.equals(fromClass(Float.class))) {
            return floatClass();
        }
        if(klass.equals(fromClass(Character.class))) {
            return charClass();
        }
        if(klass.equals(fromClass(Boolean.class))) {
            return booleanClass();
        }
        throw new InternalException(klass + " is not a boxed class");
    }

    public static IRClass fromClass(Class<?> klass) {
        return typeStore.fromClass(klass);
    }

    public static IRPrimitiveType fromPrimitiveClass(Class<?> klass) {
        return typeStore.fromPrimitiveClass(klass);
    }


    public static IRClass classForName(String className) {
        return typeStore.fromClassName(className);
    }

    public static IRType fromType(Type type) {
        return typeStore.fromType(type);
    }

    public static IRClass objectClass() {
        return typeStore.fromClass(Object.class);
    }

    public static IRType nullType() {
        return typeStore.nullType();
    }

    public static IRClass voidType() {
        return fromClass(void.class);
    }

    public static IRClass doubleClass() {
        return fromClass(double.class);
    }

    public static IRClass intClass() {
        return fromClass(int.class);
    }

    public static IRClass byteClass() {
        return fromClass(byte.class);
    }

    public static IRClass shortClass() {
        return fromClass(short.class);
    }

    public static IRClass longClass() {
        return fromClass(long.class);
    }

    public static IRClass charClass() {
        return fromClass(char.class);
    }

    public static IRClass booleanClass() {
        return fromClass(boolean.class);
    }

    public static IRClass floatClass() {
        return fromClass(float.class);
    }

    public static IRType getCompatibleType(List<IRType> types) {
        IRType result = null;
        for (IRType type : types) {
            if(result == null) {
                result = type;
            }
            else {
                result = getCompatibleType(result, type);
            }
        }
        return result;
    }

    public static IRType getCompatibleType(IRType type1, IRType type2) {
        if(type1.isAssignableFrom(type2)) {
            return type1;
        }
        if(type2.isAssignableFrom(type1)) {
            return type2;
        }
        if(type1 instanceof IRPrimitiveType primType1 && type2 instanceof IRPrimitiveType primType2) {
            if(primType1.isConvertibleFrom(primType2)) {
                return primType1;
            }
            if(primType2.isConvertibleFrom(primType1)) {
                return primType2;
            }
        }
        throw new InternalException("Can not find compatible type for " + type1 + " and " + type2);
    }

    public static IRClass internClass(IRClass klass) {
        return typeStore.internClass(klass);
    }

    public static void addMethod(IRMethod irMethod, Method method) {
        typeStore.addMethod(irMethod, method);
    }

}
