package org.metavm.compiler.apigen;

import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.element.Method;
import org.metavm.compiler.type.*;
import org.metavm.object.type.StringType;
import org.metavm.util.NamingUtils;
import org.metavm.util.Utils;

public class ApiGenUtils {

    public static String getApiClass(Clazz clazz) {
        if (clazz.getScope() instanceof Clazz encl && !clazz.getName().toString().startsWith(encl.getName().toString()))
            return getApiClass(encl) + clazz.getName();
        else
            return clazz.getName().toString();
    }

    public static String getApiType(Type type) {
        return switch (type) {
            case PrimitiveType primitiveType -> getApiPrimType(primitiveType);
            case StringType ignored -> "string";
            case ClassType classType -> {
                var apiClsName = classType.getClazz().isEntity() ? "string" : getApiClass(classType.getClazz());
                if (classType.getTypeArguments().isEmpty())
                    yield apiClsName;
                else
                    yield apiClsName + "<" + Utils.join(classType.getTypeArguments(), Type::getTypeText) + ">";
            }
            case ArrayType arrayType -> getApiType(arrayType.getElementType()) + "[]";
            case UnionType unionType -> Utils.join(unionType.alternatives(), ApiGenUtils::getApiType, " | ");
            default -> throw new IllegalStateException("Type " + type.getTypeText() + " is not supported in API");
        };
    }

    private static String getApiPrimType(PrimitiveType primitiveType) {
        return switch (primitiveType) {
            case NULL, VOID, NEVER -> "undefined";
            case BYTE, SHORT, INT, LONG, FLOAT, DOUBLE, TIME -> "number";
            case CHAR, PASSWORD -> "string";
            case BOOL -> "boolean";
            case ANY -> "any";
        };
    }

    public static String getRequestClsName(Method method) {
        return NamingUtils.firstCharToUpperCase(method.getName().toString()) + "Request";
    }

}
