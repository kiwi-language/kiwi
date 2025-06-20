package org.metavm.compiler.apigen;

import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.element.Field;
import org.metavm.compiler.element.Method;
import org.metavm.compiler.element.Param;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.util.NamingUtils;
import org.metavm.util.Utils;

import static org.metavm.compiler.apigen.ApiGenUtils.*;
import static org.metavm.util.NamingUtils.firstCharToLowerCase;

public class TypeGenerator {
    private final ApiWriter apiWriter = new ApiWriter();

    public String generate(List<Clazz> classes) {
        apiWriter.writeln(Templates.COMMON_DATA_STRUCTURES);
        for (Clazz cls : classes) {
            if (cls.isPublic())
                generateClass(cls);
        }
        return apiWriter.toString();
    }

    public void generateClass(Clazz cls) {
        assert cls.isPublic();
        if (cls.isEnum())
            generateEnumClass(cls);
        else
            generateOrdinaryClass(cls);
    }

    public void generateEnumClass(Clazz cls) {
        apiWriter.writeln("export type " + getApiClass(cls) + " = "
                + Utils.join(cls.getEnumConstants(),  f -> "\"" + f.getName().toString() + "\"", " | "));
        apiWriter.writeln();
    }

    public void generateOrdinaryClass(Clazz cls) {
        if (!cls.isBean()) {
            apiWriter.writeln("export interface " + getApiClass(cls) + " {");
            apiWriter.indent();
            if (!cls.isValue())
                apiWriter.writeln("id?: string");
            for (Field field : cls.getFields()) {
                if (field.isPublic() && !field.isStatic()) {
                    if (isEntityType(field.getType()))
                        apiWriter.writeln(field.getName() + "Id: string");
                    else
                        apiWriter.writeln(field.getName() + ": " + getApiType(field.getType()));
                }
            }
            for (Clazz innerCls : cls.getClasses()) {
                if (innerCls.isPublic() && innerCls.isEntity()) {
                    var fieldName = firstCharToLowerCase(innerCls.getName().toString()) + "s";
                    apiWriter.writeln(fieldName + ": " + getApiType(innerCls) + "[]");
                }
            }
            apiWriter.deIndent();
            apiWriter.writeln("}");
            apiWriter.writeln();
        }
        if (cls.isValue())
            return;
        if (cls.isTopLevel() && !cls.isBean())
            generateSearchRequest(cls);
        for (Method method : cls.getMethods()) {
            if (method.isPublic() && !method.isStatic() && !method.isInit())
                generateInvokeRequest(method);
        }
        for (Clazz innerCls : cls.getClasses()) {
            if (innerCls.isPublic())
                generateClass(innerCls);
        }
    }

    private boolean isEntityType(Type type) {
        var ut = type.getUnderlyingType();
        return ut != Types.instance.getStringType() && ut  instanceof ClassType ct && ct.getClazz().isEntity();
    }

    private boolean isNumericType(Type type) {
        return type == PrimitiveType.BYTE ||
               type == PrimitiveType.SHORT ||
               type == PrimitiveType.INT ||
               type == PrimitiveType.LONG ||
               type == PrimitiveType.FLOAT ||
               type == PrimitiveType.DOUBLE;
    }

    private void generateSearchRequest(Clazz cls) {
        apiWriter.writeln("export interface " + getApiClass(cls) + "SearchRequest {");
        apiWriter.indent();
        for (Field field : cls.getFields()) {
            if (field.isPublic() && !field.isStatic() && isSearchable(field.getType())) {
                var ut = field.getType().getUnderlyingType();
                if (isNumericType(ut)) {
                    var fName = NamingUtils.firstCharToUpperCase(field.getName().toString());
                    apiWriter.writeln("min" + fName + "?: number");
                    apiWriter.writeln("max" + fName + "?: number");
                }
                else if (isEntityType(ut))
                    apiWriter.writeln(field.getName() + "Id?: string");
                else
                    apiWriter.writeln(field.getName() + "?: " + getApiType(ut));
            }
        }
        apiWriter.writeln("page?: number");
        apiWriter.writeln("pageSize?: number");
        apiWriter.deIndent();
        apiWriter.writeln("}");
        apiWriter.writeln();
    }

    private boolean isSearchable(Type type) {
        var ut = type.getUnderlyingType();
        if (ut instanceof ClassType ct && ct.getClazz().isValue())
            return false;
        return !(ut instanceof ArrayType);
    }

    private void generateInvokeRequest(Method method) {
        apiWriter.writeln("export interface " + getRequestClsName(method) + " {");
        apiWriter.indent();
        if (!method.getDeclType().getClazz().isBean()) {
            var idField = firstCharToLowerCase(getApiClass(method.getDeclClass())) + "Id";
            apiWriter.writeln(idField + ": string");
        }
        for (Param param : method.getParams()) {
            apiWriter.writeln(param.getName() + ": " + getApiType(param.getType()));
        }
        apiWriter.deIndent();
        apiWriter.writeln("}");
        apiWriter.writeln();
    }

}
