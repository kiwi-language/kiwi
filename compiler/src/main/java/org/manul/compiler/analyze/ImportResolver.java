package org.manul.compiler.analyze;

import org.manul.compiler.diag.Log;
import org.manul.compiler.element.Project;
import org.manul.compiler.syntax.File;

public class ImportResolver {

    public static void resolve(File file, Project project, Log log) {
        file.getImports().forEach(imp -> imp.resolve(project, log));
    }

}
