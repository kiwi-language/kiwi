package org.manul.compiler.apigen;

import org.manul.compiler.element.*;
import org.manul.compiler.type.*;
import org.manul.compiler.util.List;
import org.manul.entity.AttributeNames;
import org.manul.util.InflectUtil;
import org.manul.util.NamingUtils;
import org.manul.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.manul.compiler.apigen.ApiGenUtils.getApiClass;
import static org.manul.compiler.apigen.ApiGenUtils.getApiType;
import static org.manul.util.NamingUtils.firstCharToUpperCase;
import static org.manul.util.NamingUtils.firstCharsToLowerCase;

public class ApiGeneratorV3 implements ApiGenerator {

    private final Set<String> generatedTypes = new HashSet<>();
    private final Set<String> generatedFuncs = new HashSet<>();
    private final ApiWriter apiWriter = new ApiWriter();

    public String generate(List<Clazz> rootClasses) {
        var buf = List.<Clazz>builder();
        rootClasses.forEach(cl -> collectAllClasses(cl, buf));
        var allClasses = buf.build();
        writeComments();
        generateImports();
        generateTypes(allClasses);
        generateFuncs(allClasses);
        return apiWriter.toString();
    }

    private void collectAllClasses(Clazz clazz, List.Builder<Clazz> result) {
        result.append(clazz);
        clazz.forEachClass(cl -> collectAllClasses(cl, result));
    }

    private void writeComments() {
        apiWriter.writeln("// This file is generated. DO NOT change.");
    }

    private void generateImports() {
        apiWriter.writeln("import { APP_ID } from './env'");
        apiWriter.writeln();
    }

    private void generateFuncs(List<Clazz> rootClasses) {
        apiWriter.writeln("const RETURN_FULL_OBJECT = true");
        apiWriter.writeln(Templates.CALL_API);
        apiWriter.writeln(Templates.UPLOAD_API);
        for (var cls : rootClasses) {
            generateFuncs(cls);
        }
        apiWriter.deIndent();
    }

    public void generateTypes(List<Clazz> classes) {
        apiWriter.writeln(Templates.COMMON_DATA_STRUCTURES);
        for (Clazz cls : classes) {
            generateTypes(cls);
        }
    }

    public void generateTypes(Clazz cls) {
        if (!cls.isPublic() || cls.isInterface())
            return;
        if (cls.isEnum())
            generateEnumClass(cls);
        else
            generateOrdinaryClass(cls);
    }

    public void generateEnumClass(Clazz cls) {
        apiWriter.writeln("export type " + getApiClass(cls) + " = "
                + Utils.join(cls.getEnumConstants(), f -> "\"" + f.getName().toString() + "\"", " | "));
        apiWriter.writeln();
    }

    public void generateOrdinaryClass(Clazz cls) {
        if (!cls.isBean() && generatedTypes.add(getApiClass(cls))) {
            var initParamsNames = Utils.mapToSet(requireNonNull(cls.getPrimaryInit()).getParams(), LocalVar::getName);
            apiWriter.writeln("export interface " + getApiClass(cls) + " {");
            apiWriter.indent();
            if (!cls.isValue())
                apiWriter.writeln("id?: string");
            var fieldNames = new HashSet<String>();
            for (Field field : cls.getFields()) {
                if (field.isPublic() && !field.isStatic()) {
                    var comment = !initParamsNames.contains(field.getName()) ? "Not needed for creation" : null;
                    writeVariable(field, false, fieldNames, false, comment);
                    if (field.getType().getUnderlyingType() instanceof ClassType ct && isEntityType(ct)) {
                        var nullable = field.getType() instanceof UnionType;
                        var summaryField = ct.getClazz().getSummaryField();
                        if (summaryField != null) {
                            var fName = field.getName() + firstCharToUpperCase(summaryField.getName().toString());
                            if (fieldNames.add(fName)) {
                                apiWriter.write(fName + (nullable ? "?: string" : ": string"));
                                apiWriter.writeln(" // Available in response but not needed in request");
                            }
                        }
                    }
                }
            }
            for (Clazz innerCls : cls.getClasses()) {
                if (innerCls.isPublic() && innerCls.isEntity()) {
                    var fieldName = InflectUtil.pluralize(firstCharsToLowerCase(innerCls.getName().toString()));
                    if (fieldNames.add(fieldName))
                        apiWriter.writeln(fieldName + ": " + getApiClass(innerCls) + "[]");
                }
            }
            apiWriter.deIndent();
            apiWriter.writeln("}");
            apiWriter.writeln();
        }
        if (cls.isValue())
            return;
        if (cls.isTopLevel() && !cls.isBean()) {
            generateSearchRequest(cls);
            generateListView(cls);
        }
    }

    private void generateListView(Clazz cls) {
        apiWriter.writeln("export interface " + getApiClass(cls) + "ListView {");
        apiWriter.indent();
        apiWriter.writeln("id: string");
        var fieldNames = new HashSet<String>();
        for (Field field : cls.getFields()) {
            if (field.isPublic() && !field.isStatic()) {
                writeVariable(field, fieldNames);
                if (field.getType().getUnderlyingType() instanceof ClassType ct && isEntityType(ct)) {
                    var nullable = field.getType() instanceof UnionType;
                    var summaryField = ct.getClazz().getSummaryField();
                    if (summaryField != null) {
                        var fName = field.getName() + firstCharToUpperCase(summaryField.getName().toString());
                        if (fieldNames.add(fName))
                            apiWriter.writeln(fName + (nullable ? "?: string" : ": string"));
                    }
                }
            }
        }
        apiWriter.deIndent();
        apiWriter.writeln("}");
        apiWriter.writeln();
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
        var typeName = "Search" + getApiClass(cls) + "Request";
        if (!generatedTypes.add(typeName))
            return;
        apiWriter.writeln("export interface " + typeName +  " {");
        apiWriter.indent();
        var fieldNames = new HashSet<String>();
        for (Field field : cls.getFields()) {
            if (field.isPublic() && !field.isStatic() && isSearchable(field.getType())) {
                var ut = field.getType().getUnderlyingType();
                if (isNumericType(ut)) {
                    var fName = firstCharToUpperCase(field.getName().toString());
                    apiWriter.writeln("min" + fName + "?: number");
                    apiWriter.writeln("max" + fName + "?: number");
                }
                else
                    writeVariable(field, true, fieldNames, true, null);
            }
        }
        apiWriter.writeln("page?: number // 1-based page number");
        apiWriter.writeln("pageSize?: number");
        apiWriter.deIndent();
        apiWriter.writeln("}");
        apiWriter.writeln();
    }

    private boolean isSearchable(Type type) {
        var ut = type.getUnderlyingType();
        if (ut instanceof ClassType ct && ct.getClazz().isValue() && ct != Types.instance.getStringType())
            return false;
        return !(ut instanceof ArrayType);
    }

    private void writeSelfVar(Clazz clazz) {
        apiWriter.writeln(firstCharsToLowerCase(getApiClass(clazz)) + "Id: string");
    }

    private void writeVariable(Variable variable, Set<String> fieldName) {
        writeVariable(variable, false, fieldName, false, null);
    }

    private void writeVariable(Variable variable, boolean optional, Set<String> fieldNames, boolean forSearch, @Nullable String comment) {
        var varType = variable.getType();
        var varName = transformFieldName(variable.getName().toString(), varType);
        if (fieldNames.add(varName)) {
            var t = getApiType(forSearch ? varType.getUnderlyingType() : varType, false);
            if (forSearch && varType.getUnderlyingType() instanceof ClassType ct && !ct.isValue())
                t = t + " | " + ApiGenUtils.toTsArrayType(varType.getUnderlyingType(), false);
            apiWriter.write(varName + (optional ? "?: " : ": ") + t);
            var comments = new ArrayList<String>();
            if (isEntityType(varType))
                comments.add("ID of " + getApiType(varType.getUnderlyingType(), true));
            if (!comments.isEmpty())
                apiWriter.write(" // " + String.join(". ", comments));
            if (comment != null)
                comments.add(comment);
            apiWriter.writeln();
        }
    }

    private String transformFieldName(String name, Type type) {
        if (isEntityType(type))
            return name + "Id";
        else if (type.getUnderlyingType() instanceof ArrayType arrayType && isEntityType(arrayType.getElementType()))
            return InflectUtil.singularize(name) + "Ids";
        else
            return name;
    }

    public void generateFuncs(Clazz clazz) {
        if (clazz.isEnum() || clazz.isInterface() || clazz.isValue() || !clazz.isPublic() || !clazz.isTopLevel())
            return;
        var beanName = clazz.getAttribute(AttributeNames.BEAN_NAME);
        var apiName = beanName != null ? NamingUtils.firstCharsToLowerCase(beanName) :
                NamingUtils.firstCharsToLowerCase(clazz.getName().toString()) + "Api";
        apiWriter.writeln().writeln("export const " + apiName + " = {").writeln();
        apiWriter.indent();
        if (clazz.isTopLevel() && beanName == null) {
            generateSave(clazz);
            generateGet(clazz);
            generateMultiGet(clazz);
            generateDelete(clazz);
            generateSearch(clazz);
        }
        if (beanName != null) {
            clazz.getMethods().forEach(m -> {
                if (m.isPublic() && !m.isAbstract() && !m.isStatic() && !m.isInit())
                    generateInvoke(m, beanName);
            });
        }
        apiWriter.deIndent();
        apiWriter.writeln("}");
    }

    private void generateGet(Clazz clazz) {
        apiWriter.writeln(String.format(
                """
                        get: (id: string): Promise<%s> => {
                            return callApi<%s>(`/%s/${id}`, 'GET')
                        },
                        """,
                getApiClass(clazz),
                getApiClass(clazz),
                getClassPath(clazz)
        ));
    }

    private void generateMultiGet(Clazz clazz) {
        apiWriter.writeln(String.format(
                """
                        getAll: (ids: string[]): Promise<SearchResult<%sListView>> => {
                            return callApi<SearchResult<%sListView>>('/%s', 'GET', {id: ids})
                        },
                        """,
                getApiClass(clazz),
                getApiClass(clazz),
                getClassPath(clazz)
        ));
    }

    private void generateSave(Clazz clazz) {
        apiWriter.writeln(String.format("""
                        save: async (%1$s: %2$s): Promise<string> => {
                            if (%1$s.id) {
                                await callApi(`/%3$s/${%1$s.id}`, 'PATCH', %1$s)
                                return %1$s.id
                            } else {
                                return callApi<string>('/%3$s', 'POST', %1$s)
                            }
                        },
                        """,
                firstCharsToLowerCase(getApiClass(clazz)),
                getApiClass(clazz),
                getClassPath(clazz)
        ));
    }

    private void generateDelete(Clazz clazz) {
        apiWriter.writeln(String.format("""
                        delete: (id: string): Promise<undefined> => {
                            return callApi<undefined>(`/%s/${id}`, 'DELETE')
                        },
                        """,
                getClassPath(clazz)
        ));
    }

    private void generateSearch(Clazz clazz) {
        apiWriter.writeln(String.format("""
                        search: (request: Search%sRequest): Promise<SearchResult<%sListView>> => {
                            return callApi<SearchResult<%sListView>>('/%s', 'GET', request)
                        },
                        """,
                getApiClass(clazz),
                getApiClass(clazz),
                getApiClass(clazz),
                getClassPath(clazz)
        ));
    }

    private void generateInvoke(Method method, String beanName) {
        var funcName = method.getName().toString();
        if (!generatedFuncs.add(funcName))
            return;
        var paramBuf = new StringBuilder();
        var paramNameBuf = new StringBuilder("{");
        var first = true;
        for (Param param : method.getParams()) {
            if (param.getAttributes().anyMatch(a -> a.name().equals(AttributeNames.BUILTIN_PARAM)))
                continue;
            if (first)
                first = false;
            else {
                paramBuf.append(", ");
                paramNameBuf.append(", ");
            }
            var name = transformFieldName(param.getName().toString(), param.getType());
            var type = getApiType(param.getType(), false);
            paramBuf.append(name).append(": ").append(type);
            paramNameBuf.append(name);
        }
        paramNameBuf.append("}");
        apiWriter.writeln(String.format(
                """
                        %s: (%s): Promise<%s> => {
                            return callApi<%s>('/%s', 'POST', %s)
                        },
                        """,
                funcName,
                paramBuf,
                getApiType(method.getRetType(), true),
                getApiType(method.getRetType(), true),
                "api/" + NamingUtils.camelToHyphen(beanName) + "/" + NamingUtils.camelToHyphen(method.getName().toString()),
                paramNameBuf
        ));
    }

    private String getClassPath(Clazz clazz) {
        return "api/" + NamingUtils.nameToPath(clazz.getQualName().toString(), true);
    }

}
