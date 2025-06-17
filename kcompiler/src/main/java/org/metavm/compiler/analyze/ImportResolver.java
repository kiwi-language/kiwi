package org.metavm.compiler.analyze;

import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.Project;
import org.metavm.compiler.syntax.File;

public class ImportResolver {

    public static void resolve(File file, Project project, Log log) {
        file.getImports().forEach(imp -> imp.resolve(project, log));
    }

}
