package org.metavm.server;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class PathPattern {

    private final Pattern pattern;
    private final List<String> pathVariables = new ArrayList<>();

    public PathPattern(String pathPattern) {
        pattern = compilePattern(pathPattern);
    }

    Map<String, String> match(String path) {
        var m = pattern.matcher(path);
        if (m.matches()) {
            Map<String, String> pathVars;
            if (pathVariables.isEmpty()) {
                pathVars = Map.of();
            } else {
                pathVars = new HashMap<>();
                // FIX: Regex capturing groups are 1-indexed. Group 0 is the entire match.
                // This loop now correctly maps variable names to their captured values.
                for (int i = 0; i < pathVariables.size(); i++) {
                    pathVars.put(pathVariables.get(i), m.group(i + 1));
                }
            }
            return pathVars;
        }
        else
            return null;
    }

    private Pattern compilePattern(String pathPattern) {
        if (pathPattern == null || !pathPattern.startsWith("/")) {
            throw new IllegalArgumentException("Path pattern must not be empty and must start with '/'");
        }

        // Special case for the root path, which matches "/" or an empty path.
        if (pathPattern.equals("/")) {
            return Pattern.compile("^/?$");
        }

        // Clear any previous state in case of reuse.
        this.pathVariables.clear();

        var regex = new StringBuilder("^");

        // Normalize path by removing any trailing slash to simplify segment processing.
        // The logic will add an optional trailing slash back at the end.
        String normalizedPath = pathPattern.endsWith("/")
                ? pathPattern.substring(0, pathPattern.length() - 1)
                : pathPattern;

        String[] segments = normalizedPath.split("/");

        // Start from 1 to skip the empty string before the first '/'
        for (int i = 1; i < segments.length; i++) {
            String segment = segments[i];
            if (segment.isEmpty()) {
                // This handles cases like "/users//profile" which is treated as "/users/profile"
                continue;
            }

            regex.append("/");

            if (segment.equals("**")) {
                if (i != segments.length - 1) {
                    throw new IllegalArgumentException("/** wildcard can only be at the end of the path pattern");
                }
                // Matches the rest of the path.
                // The pattern `(?:/.*)?` makes it match `/` followed by anything, or nothing at all.
                // This lets `/static/**` match `/static`, `/static/`, and `/static/css/style.css`.
                regex.setLength(regex.length() - 1); // Remove the just-added '/'
                regex.append("(?:/.*)?");

            } else if (segment.equals("*")) {
                // `*` matches a single path segment (any character except '/')
                regex.append("[^/]+");

            } else if (segment.startsWith("{") && segment.endsWith("}")) {
                // Path variable like `{id}`
                String varName = segment.substring(1, segment.length() - 1);
                if (varName.isEmpty() || !varName.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                    throw new IllegalArgumentException("Invalid path variable name: '" + varName + "'. Must be a valid Java identifier.");
                }
                pathVariables.add(varName);
                regex.append("([^/]+)"); // Capturing group for the variable

            } else if (segment.contains("*") || segment.contains("{") || segment.contains("}")) {
                throw new IllegalArgumentException("Invalid characters in literal path segment: " + segment);
            } else {
                // Literal path segment, quote it to escape special regex characters.
                regex.append(Pattern.quote(segment));
            }
        }

        // Allow an optional trailing slash, unless the path ends with `/**`
        // which has its own logic to handle trailing paths.
        if (!pathPattern.endsWith("/**")) {
            regex.append("/?");
        }

        regex.append("$");

        return Pattern.compile(regex.toString());
    }



}
