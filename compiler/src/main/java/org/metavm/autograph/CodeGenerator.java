package org.metavm.autograph;

import com.intellij.psi.PsiClass;
import org.metavm.util.ContextUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeGenerator {

    public static final Logger logger = LoggerFactory.getLogger(CodeGenerator.class);

    public CodeGenerator() {
    }

    void generateDecl(PsiClass psiClass, TypeResolver typeResolver) {
        try(var ignored = ContextUtil.getProfiler().enter("generateDecl")) {
            psiClass.accept(new Declarator(psiClass, typeResolver));
        }
    }

    void generateCode(PsiClass psiClass, TypeResolver typeResolver) {
        try (var ignored = ContextUtil.getProfiler().enter("generateCode")) {
            psiClass.accept(new Generator(psiClass, typeResolver));
            psiClass.accept(new IndexCreator(typeResolver));
        }
    }

}
