package org.metavm.compiler.apigen;

import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.element.Method;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.util.List;
import org.metavm.util.NamingUtils;

import static org.metavm.compiler.apigen.ApiGenUtils.*;

public class ApiGenerator {

    private final ApiWriter apiWriter = new ApiWriter();

    public String generate(List<Clazz> rootClasses) {
        writeImport(collectAllClasses(rootClasses));
        apiWriter.writeln(Templates.CALL_API);
        apiWriter.writeln("export const api = {\n");
        apiWriter.indent();
        for (var cls : rootClasses) {
            generateClass(cls);
        }
        apiWriter.deIndent();
        apiWriter.write("}");
        return apiWriter.toString();
    }

    private List<String> collectAllClasses(List<Clazz> rootClasses) {
        var classes = List.<String>builder();
        for (var cls : rootClasses) {
            collectClasses(cls, classes);
        }
        return classes.build();
    }

    private void collectClasses(Clazz clazz, List.Builder<String> buf) {
        if (!clazz.isPublic())
            return;
        if (!clazz.isBean())
            buf.append(getApiClass(clazz));
        if (!clazz.isEntity())
            return;
        if (clazz.isTopLevel() && !clazz.isBean())
            buf.append(getApiClass(clazz) + "SearchRequest");
        for (Method method : clazz.getMethods()) {
            if (method.isPublic() && !method.isStatic() && !method.isInit())
                buf.append(getRequestClsName(method));
        }
        for (Clazz cls : clazz.getClasses()) {
            collectClasses(cls, buf);
        }
    }

    public void generateClass(Clazz clazz) {
        if (clazz.isEnum() || clazz.isInterface() || clazz.isValue() || !clazz.isPublic())
            return;
        if (clazz.isTopLevel() && !clazz.isBean()) {
            generateSave(clazz);
            generateRetrieve(clazz);
            generateDelete(clazz);
            generateSearch(clazz);
        }
        clazz.forEachClass(this::generateClass);
        clazz.getMethods().forEach(m -> {
            if (m.isPublic() && !m.isAbstract() && !m.isStatic() && !m.isInit())
                generateInvoke(m);
        });
    }

    private void writeImport(List<String> classes) {
        apiWriter.write("import { Result, SearchResult");
        for (String cls : classes) {
            apiWriter.write(", ");
            apiWriter.write(cls);
        }
        apiWriter.writeln(" } from './types'");
    }

    private String toPath(Name name) {
        return NamingUtils.nameToPath(name.toString());
    }

    private void generateRetrieve(Clazz clazz) {
        apiWriter.writeln(String.format(
                """
                get%s: (id: string): Promise<%s> => {
                    return callApi<%s>(`/api/%s/${id}`, 'GET')
                },
                """,
                getApiClass(clazz),
                getApiClass(clazz),
                getApiClass(clazz),
                toPath(clazz.getQualName())
        ));
    }

    private void generateSave(Clazz clazz) {
        apiWriter.writeln(String.format("""
                save%s: (%s: %s): Promise<string> => {
                    return callApi<string>('/api/%s', 'POST', %s)
                },
                """,
                getApiClass(clazz),
                NamingUtils.firstCharsToLowerCase(getApiClass(clazz)),
                getApiClass(clazz),
                NamingUtils.nameToPath(clazz.getQualName().toString()),
                NamingUtils.firstCharsToLowerCase(getApiClass(clazz))
        ));
    }

    private void generateDelete(Clazz clazz) {
        apiWriter.writeln(String.format("""
                delete%s: (id: string) => {
                    return callApi<null>(`/api/%s/{id}`, 'DELETE')
                },
                """,
                getApiClass(clazz),
                toPath(clazz.getQualName())
        ));
    }

    private void generateSearch(Clazz clazz) {
        apiWriter.writeln(String.format("""
                search%ss: (request: %sSearchRequest): Promise<SearchResult<%s>> => {
                    return callApi<SearchResult<%s>>('/api/%s/_search', 'POST', request)
                },
                """,
                getApiClass(clazz),
                getApiClass(clazz),
                getApiClass(clazz),
                getApiClass(clazz),
                toPath(clazz.getQualName())
        ));
    }

    private void generateInvoke(Method method) {
        apiWriter.writeln(String.format(
                """
                %s: (request: %s): Promise<%s> => {
                    return callApi<%s>('/api/%s', 'POST', request)
                },
                """,
                method.getName().toString(),
                getRequestClsName(method),
                getApiType(method.getRetType()),
                getApiType(method.getRetType()),
                toPath(method.getQualName())
        ));
    }


}
