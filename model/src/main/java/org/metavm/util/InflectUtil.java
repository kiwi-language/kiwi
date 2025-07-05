package org.metavm.util;


import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InflectUtil {

    private static final Inflect inflect = new Inflect();

    private static final Map<String, String> singularCache = new ConcurrentHashMap<>();
    private static final Map<String, String> pluralCache = new ConcurrentHashMap<>();

    public static String singularize(String noun) {
        var cached = singularCache.get(noun);
        if (cached != null) {
            return cached;
        }
        var r =  inflect.singular_noun(noun);
        var singular = Boolean.FALSE.equals(r) ? noun : (String) r;
        singularCache.put(noun, singular);
        return singular;
    }

    public static String pluralize(String noun) {
        var cached = pluralCache.get(noun);
        if (cached != null) {
            return cached;
        }
        var plural =  inflect.plural(noun);
        pluralCache.put(noun, plural);
        return plural;
    }

}
