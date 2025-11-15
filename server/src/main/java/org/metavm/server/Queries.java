package org.metavm.server;

import lombok.SneakyThrows;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Queries {

    @SneakyThrows
    public static Map<String, List<String>> parseQuery(String query) {
        Map<String, List<String>> params = new HashMap<>();
        if (query == null || query.isEmpty()) {
            return params;
        }
        for (String pair : query.split("&")) {
            int eq_idx = pair.indexOf("=");
            String key = eq_idx > 0 ? URLDecoder.decode(pair.substring(0, eq_idx), StandardCharsets.UTF_8) : pair;
            if (!params.containsKey(key)) {
                params.put(key, new ArrayList<>());
            }
            String value = eq_idx > 0 && pair.length() > eq_idx + 1 ? URLDecoder.decode(pair.substring(eq_idx + 1), StandardCharsets.UTF_8) : null;
            params.get(key).add(value);
        }
        return params;
    }



}
