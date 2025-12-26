package org.metavm.flow;

import java.util.List;

public record FunctionSignature(String name, List<String> typeParameterNames, List<String> parameterNames) {
}
