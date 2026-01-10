package org.manul.flow;

import java.util.List;

public record FunctionSignature(String name, List<String> typeParameterNames, List<String> parameterNames) {
}
