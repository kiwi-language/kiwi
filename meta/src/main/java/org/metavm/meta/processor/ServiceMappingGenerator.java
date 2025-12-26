package org.metavm.meta.processor;

import javax.lang.model.element.TypeElement;
import java.util.Collection;
import java.util.HashSet;

public class ServiceMappingGenerator extends AbstractGenerator {

    public String generate(String existingContent, Collection<TypeElement> classes) {
        var lines = existingContent.split("\n");
        var visited = new HashSet<String>();
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                visited.add(line);
                writeln(line);
            }
        }
        for (TypeElement clazz : classes) {
            var builderName = clazz.getQualifiedName() + "__KlassBuilder__";
            if (visited.add(builderName))
                writeln(builderName);
        }
        return toString();
    }

}
