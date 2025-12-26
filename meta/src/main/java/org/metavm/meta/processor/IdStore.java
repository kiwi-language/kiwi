package org.metavm.meta.processor;

import lombok.SneakyThrows;

import javax.tools.FileObject;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

public class IdStore {
    private final String type;
    private final Map<String, String> ids = new HashMap<>();

    @SneakyThrows
    public IdStore(String type, FileObject file) {
        this.type = type;
        var props = new Properties();
        props.load(new StringReader(
                file.getCharContent(true).toString()
        ));
        props.forEach((key, value) -> {
            var name = (String) key;
            if (name.startsWith("$"))
                return;
            var strValue = (String) value;
            var idx = strValue.lastIndexOf(':');
            var id = idx == -1 ? strValue : strValue.substring(0, idx);
            ids.put(name, id);
        });
    }

    public String getId(String name) {
        return Objects.requireNonNull(ids.get(name), () -> "Id not found for " + type + "." + name);
    }
}
