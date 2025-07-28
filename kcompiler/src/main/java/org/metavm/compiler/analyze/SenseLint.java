package org.metavm.compiler.analyze;

import org.metavm.compiler.diag.SenseLintErrors;
import org.metavm.compiler.diag.DefaultLog;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.ArrayType;
import org.metavm.compiler.type.ClassType;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;
import org.metavm.util.InflectUtil;

import java.util.HashMap;
import java.util.Map;

public class SenseLint extends StructuralNodeVisitor {

    public static void run(List<File> files, DefaultLog log) {
        var lcName2cls = new HashMap<String, List<Clazz>>();
        var clsNameCollector = new StructuralNodeVisitor() {

            @Override
            public Void visitClassDecl(ClassDecl classDecl) {
                var cls = classDecl.getElement();
                lcName2cls.compute(
                        classDecl.name().toString().toLowerCase(),
                        (k, l) -> l == null ? List.of(cls) : l.prepend(cls)
                );
                return super.visitClassDecl(classDecl);
            }
        };
        for (File file : files) {
            file.accept(clsNameCollector);
        }
        for (File file : files) {
            log.setSourceFile(file.getSourceFile());
            file.accept(new SenseLint(log, lcName2cls));
        }
    }

    private final Log log;
    private final Map<String, List<Clazz>> lcName2cls;

    public SenseLint(Log log, Map<String, List<Clazz>> lcName2cls) {
        this.log = log;
        this.lcName2cls = lcName2cls;
    }

    @Override
    public Void visitFieldDecl(FieldDecl fieldDecl) {
        checkVariableName(fieldDecl);
        return super.visitFieldDecl(fieldDecl);
    }

    @Override
    public Void visitParamDecl(ParamDecl paramDecl) {
        checkVariableName(paramDecl);
        return super.visitParamDecl(paramDecl);
    }

    @Override
    public Void visitClassParamDecl(ClassParamDecl classParamDecl) {
        checkVariableName(classParamDecl);
        return super.visitClassParamDecl(classParamDecl);
    }

    private void checkVariableName(VariableDecl<?> variableDecl) {
        var name = variableDecl.getName().toString();
        if (name.endsWith("Id"))
            log.error(variableDecl, SenseLintErrors.variableNameEndsWithId);
        var classes = lcName2cls.get(name);
        var varType = variableDecl.getElement().getType();
        String sigName;
        if (classes != null && classes.nonMatch(c -> matchClassType(c, varType))) {
            log.error(variableDecl, SenseLintErrors.variableNameMatchesClassName);
        }
        else if (!name.equals((sigName = InflectUtil.singularize(name)))) {
            classes = lcName2cls.get(sigName);
            if (classes != null) {
                if (!(varType.getUnderlyingType() instanceof ArrayType at)
                        || classes.nonMatch(c -> matchClassType(c, at.getElementType())))
                    log.error(variableDecl, SenseLintErrors.variableNameMatchesPluralClassName);
            }
        }
    }

    private boolean matchClassType(Clazz clazz, Type type) {
        return type.getUnderlyingType() instanceof ClassType ct && ct.getClazz() == clazz;
    }

}
