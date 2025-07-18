package org.metavm.object.instance.search;

import java.util.ArrayList;
import java.util.List;

public class SearchUtil {

    public static boolean prefixMatch(String source, String query) {
        var srcTokens = tokenize(source);
        var prefix = query.toLowerCase();
        for (String t : srcTokens) {
            if (t.startsWith(prefix))
                return true;
        }
        return false;
    }

    public static boolean match(String source, String query) {
        var srcTokens = tokenize(source).iterator();
        var queryTokens = tokenize(query);
        out: for (String t : queryTokens) {
            while (srcTokens.hasNext()) {
                if (srcTokens.next().equals(t))
                    continue out;
            }
            return false;
        }
        return true;
    }

    public static List<String> tokenize(String input) {
        var tokens = new ArrayList<String>();
        var chars = input.toCharArray();
        var sb = new StringBuilder();
        for (char c : chars) {
            if (isDelimiter(c)) {
                if (!sb.isEmpty()) {
                    tokens.add(sb.toString());
                    sb.setLength(0);
                }
            }
            else
                sb.append(Character.toLowerCase(c));
        }
        if (!sb.isEmpty())
            tokens.add(sb.toString());
        return tokens;
    }

    public static boolean containsDelimiter(String s) {
        return s.chars().anyMatch(SearchUtil::isDelimiter);
    }

    private static boolean isDelimiter(int c) {
        return switch (c) {
            case ' ', '.', ',', ';', ':', '!', '?', '"', '\'', '`', '(', ')', '[', ']', '{', '}',
                    '=', '/', '\\', '|', '<', '>', '@', '#', '$', '%', '^', '&', '*', '~', '+', '-' -> true;
            default -> false;
        };
    }

}
