package org.metavm.compiler.apigen;

import org.metavm.compiler.element.*;
import org.metavm.compiler.type.*;
import org.metavm.compiler.util.List;
import org.metavm.entity.AttributeNames;
import org.metavm.util.InflectUtil;
import org.metavm.util.NamingUtils;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import static java.util.Objects.requireNonNull;
import static org.metavm.compiler.apigen.ApiGenUtils.*;
import static org.metavm.util.NamingUtils.firstCharToUpperCase;
import static org.metavm.util.NamingUtils.firstCharsToLowerCase;

public class ApiGeneratorV3 implements ApiGenerator {

    private final Set<String> generatedTypes = new HashSet<>();
    private final Set<String> generatedFuncs = new HashSet<>();
    private final ApiWriter apiWriter = new ApiWriter();

    public String generate(List<Clazz> rootClasses) {
        generateImports();
        generateTypes(rootClasses);
        generateFuncs(rootClasses);
        return apiWriter.toString();
    }

    private void generateImports() {
        apiWriter.writeln("import { APP_ID } from './env'");
        apiWriter.writeln();
    }

    private void generateFuncs(List<Clazz> rootClasses) {
        apiWriter.writeln("const RETURN_FULL_OBJECT = true");
        apiWriter.writeln(Templates.CALL_API);
        apiWriter.writeln("export const api = {\n");
        apiWriter.indent();
        apiWriter.writeln(Templates.UPLOAD_API);
        for (var cls : rootClasses) {
            generateFuncs(cls);
        }
        apiWriter.deIndent();
        apiWriter.write("}");
    }

    public void generateTypes(List<Clazz> classes) {
        apiWriter.writeln(Templates.COMMON_DATA_STRUCTURES);
        for (Clazz cls : classes) {
            if (cls.isPublic())
                generateTypes(cls);
        }
    }

    public void generateTypes(Clazz cls) {
        assert cls.isPublic();
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
        if (!cls.isBean() && !cls.isInterface() && generatedTypes.add(getApiClass(cls))) {
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
        if (cls.isTopLevel() && !cls.isBean() && !cls.isInterface()) {
            generateSearchRequest(cls);
            generateListView(cls);
        }
        for (Clazz innerCls : cls.getClasses()) {
            if (innerCls.isPublic())
                generateTypes(innerCls);
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
        if (clazz.isEnum() || clazz.isInterface() || clazz.isValue() || !clazz.isPublic())
            return;
        if (clazz.isTopLevel() && !clazz.isBean()) {
            generateSave(clazz);
            generateGet(clazz);
            generateMultiGet(clazz);
            generateDelete(clazz);
            generateSearch(clazz);
        }
        clazz.forEachClass(this::generateFuncs);
        if (clazz.isBean()) {
            clazz.getMethods().forEach(m -> {
                if (m.isPublic() && !m.isAbstract() && !m.isStatic() && !m.isInit())
                    generateInvoke(m);
            });
        }
    }

    private String toPath(Name name) {
        return NamingUtils.nameToPath(name.toString());
    }

    private void generateGet(Clazz clazz) {
        var funcName = "get" + getApiClass(clazz);
        if (generatedFuncs.add(funcName)) {
            apiWriter.writeln(String.format(
                    """
                            %s: (id: string): Promise<%s> => {
                                return callApi<%s>(`/api/%s/${id}`, 'GET')
                            },
                            """,
                    funcName,
                    getApiClass(clazz),
                    getApiClass(clazz),
                    toPath(clazz.getQualName())
            ));
        }
    }

    private void generateMultiGet(Clazz clazz) {
        var funcName = "multiGet" + getApiClass(clazz);
        if (generatedFuncs.add(funcName)) {
            apiWriter.writeln(String.format(
                    """
                            %s: (ids: string[]): Promise<%s[]> => {
                                return callApi<%s[]>('/api/%s/_multi-get', 'POST', {ids})
                            },
                            """,
                    funcName,
                    getApiClass(clazz),
                    getApiClass(clazz),
                    toPath(clazz.getQualName())
            ));
        }
    }

    private void generateSave(Clazz clazz) {
        var funcName = "save" + getApiClass(clazz);
        if (generatedFuncs.add(funcName)) {
            apiWriter.writeln(String.format("""
                            %s: (%s: %s): Promise<string> => {
                                return callApi<string>('/api/%s', 'POST', %s)
                            },
                            """,
                    funcName,
                    firstCharsToLowerCase(getApiClass(clazz)),
                    getApiClass(clazz),
                    NamingUtils.nameToPath(clazz.getQualName().toString()),
                    firstCharsToLowerCase(getApiClass(clazz))
            ));
        }
    }

    private void generateDelete(Clazz clazz) {
        var funcName = "delete" + getApiClass(clazz);
        if (generatedFuncs.add(funcName)) {
            apiWriter.writeln(String.format("""
                            %s: (id: string): Promise<undefined> => {
                                return callApi<undefined>(`/api/%s/${id}`, 'DELETE')
                            },
                            """,
                    funcName,
                    toPath(clazz.getQualName())
            ));
        }
    }

    private void generateSearch(Clazz clazz) {
        var funcName = "search" + getApiClass(clazz);
        if (generatedFuncs.add(funcName)) {
            apiWriter.writeln(String.format("""
                            %s: (request: Search%sRequest): Promise<SearchResult<%sListView>> => {
                                return callApi<SearchResult<%sListView>>('/api/%s/_search', 'POST', request)
                            },
                            """,
                    funcName,
                    getApiClass(clazz),
                    getApiClass(clazz),
                    getApiClass(clazz),
                    toPath(clazz.getQualName())
            ));
        }
    }

    private void generateInvoke(Method method) {
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
                            return callApi<%s>('/api/%s', 'POST', %s)
                        },
                        """,
                funcName,
                paramBuf,
                getApiType(method.getRetType(), true),
                getApiType(method.getRetType(), true),
                toPath(method.getQualName()),
                paramNameBuf
        ));
    }

}
