package tech.metavm.autograph;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.IEntityContext;

public class Compiler {

    public static final Logger LOGGER = LoggerFactory.getLogger(Compiler.class);

    void transform(PsiClass psiClass) {
        executeCommand(() -> {
            psiClass.accept(new BreakTransformer());
            psiClass.accept(new ContinueTransformer());
            psiClass.accept(new ForTransformer());
        });
    }

    void generateDecl(PsiFile file, TypeResolver typeResolver, IEntityContext context) {
        file.accept(new QnResolver());
        file.accept(new ActivityAnalyzer());
        var astToCfg = new AstToCfg();
        file.accept(astToCfg);
        file.accept(new ReachingDefAnalyzer(astToCfg.getGraphs()));
        file.accept(new LivenessAnalyzer(astToCfg.getGraphs()));
        file.accept(new Declarator(typeResolver, context));
    }

    private void executeCommand(Runnable command) {
        CommandProcessor.getInstance().executeCommand(
                null,
                () -> {
                    try {
                        command.run();
                    } catch (RuntimeException e) {
                        LOGGER.error("Fail to run compile command", e);
                        throw e;
                    }
                },
                null, null
        );
    }

    void generateCode(PsiFile file, TypeResolver typeResolver, IEntityContext context) {
        file.accept(new Generator(typeResolver, context));
    }

}
