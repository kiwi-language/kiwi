package tech.metavm.transpile;

import java.util.List;

public record MethodSignature(
        String name,
        List<String> parameterTypeNames
) {

}
