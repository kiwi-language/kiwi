package org.metavm.asm;

import javax.annotation.Nullable;
import java.util.Map;

public record AsmCompilationUnit(@Nullable String packageName, Map<String, String> names) {

    public String getReferenceName(String name) {
        return names.getOrDefault(name, getDefinitionName(name));
    }

    public String getDefinitionName(String name) {
        return packageName != null ? packageName + "." + name : name;
    }

}
