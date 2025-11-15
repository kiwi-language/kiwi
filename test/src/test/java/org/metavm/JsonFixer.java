package org.metavm;

import org.jsonk.Jsonk;
import org.metavm.util.TestUtils;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class JsonFixer {

    public static final String FILE = "/Users/leen/workspace/kiwi/input.json";
    //"/Users/leen/workspace/front/src/type/__mocks__/ClassType.json";

    public static final String OUTPUT = "/Users/leen/workspace/kiwi/test.json";

    private static final LinkedList<String> nameStack = new LinkedList<>();

    public static void main(String[] args) throws IOException {
        char[] buffer = new char[1024 * 1024];
        try (var reader = new FileReader(FILE); var writer = new FileWriter(OUTPUT)) {
            int n = reader.read(buffer);
            String jsonStr = new String(buffer, 0, n);
            var map = Jsonk.fromJson(jsonStr, Map.class);
            fixMap(map);
            writer.write(TestUtils.toJSONString(map));
        }
    }

    private static void fixMap(Map<String, Object> map) {
        var entryIt = map.entrySet().iterator();
        boolean fixing = Objects.equals(nameStack.peek(), "fields");
        Map<String, Object> add = new HashMap<>();
        while (entryIt.hasNext()) {
            var entry = entryIt.next();
            var key = entry.getKey();
            nameStack.push(key);
            var value = entry.getValue();
            if (/*fixing && */key.equals("typeId")) {
                entryIt.remove();
                add.put("typeRef", Map.of("id", value));
            }
            if (value instanceof Map<?, ?> subMap) {
                //noinspection unchecked
                fixMap((Map<String, Object>) subMap);
            } else if (value instanceof Collection<?> collection) {
                for (Object ele : collection) {
                    if (ele instanceof Map<?, ?> subMap) {
                        //noinspection unchecked
                        fixMap((Map<String, Object>) subMap);
                    }
                }
            }
            nameStack.pop();
        }
        map.putAll(add);
    }

}
