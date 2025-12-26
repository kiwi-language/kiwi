package org.metavm.meta.processor;

import lombok.SneakyThrows;

import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.tools.StandardLocation;
import java.io.StringReader;
import java.util.*;

public class DefaultIdStores implements IdStores {

    private static final List<String> filePaths = List.of(
            "id/org.metavm.flow.Function.properties",
            "id/org.metavm.flow.Method.properties",
            "id/org.metavm.flow.Parameter.properties",
            "id/org.metavm.object.type.Field.properties",
            "id/org.metavm.object.type.Index.properties",
            "id/org.metavm.object.type.Klass.properties",
            "id/org.metavm.object.type.StaticFieldTable.properties",
            "id/org.metavm.object.type.TypeVariable.properties"
    );

    private static final String typeTagPath = "typeTags/typeTags.properties";

    private final Map<String, IdStore> stores = new HashMap<>();
    private final Map<String, Integer> typeTags = new HashMap<>();

    @SneakyThrows
    public DefaultIdStores(ProcessingEnvironment env) {
        var filer = env.getFiler();
        for (var path : filePaths) {
            var type = path.substring(3, path.lastIndexOf('.'));
            var file = filer.getResource(StandardLocation.CLASS_OUTPUT, "", path);
            if (file.getLastModified() > 0)
                stores.put(type, new IdStore(type, file));
        }
        loadTypeTags(filer);
    }

    @SneakyThrows
    private void loadTypeTags(Filer filer) {
        var file = filer.getResource(StandardLocation.CLASS_OUTPUT, "", typeTagPath);
        if (file.getLastModified() == 0)
            return;
        var props = new Properties();
        props.load(new StringReader(file.getCharContent(true).toString()));
        for (var e : props.entrySet()) {
            var className = (String) e.getKey();
            if (!className.startsWith("$")) {
                typeTags.put(className, Integer.parseInt((String) e.getValue()));
            }
        }
    }

    @Override
    public String getId(String type, String name) {
        var store = Objects.requireNonNull(stores.get(type), () -> "No ID store found for type: " + type);
        return store.getId(name);
    }

    @Override
    public int getTypeTag(String className) {
        return Objects.requireNonNull(typeTags.get(className), () -> "Type tag not found for class: " + className);
    }

}
