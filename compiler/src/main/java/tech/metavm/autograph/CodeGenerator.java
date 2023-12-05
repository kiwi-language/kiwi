package tech.metavm.autograph;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.IEntityContext;

public class CodeGenerator {

    public static final Logger LOGGER = LoggerFactory.getLogger(CodeGenerator.class);

    private final IEntityContext context;

    public CodeGenerator(IEntityContext context) {
        this.context = context;
    }

    void transform(PsiClass psiClass) {
        executeCommand(() -> {
            psiClass.accept(new SwitchExpressionTransformer());
            psiClass.accept(new SwitchLabelStatementTransformer());
            psiClass.accept(new ForTransformer());
            psiClass.accept(new BreakTransformer());
            psiClass.accept(new ContinueTransformer());
        });
    }

    void generateDecl(PsiClass psiClass, TypeResolver typeResolver) {
        psiClass.accept(new QnResolver());
        psiClass.accept(new ActivityAnalyzer());
        var astToCfg = new AstToCfg();
        psiClass.accept(astToCfg);
        psiClass.accept(new ReachingDefAnalyzer(astToCfg.getGraphs()));
        psiClass.accept(new LivenessAnalyzer(astToCfg.getGraphs()));
        psiClass.accept(new Declarator(typeResolver, context));
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

    void generateCode(PsiClass psiClass, TypeResolver typeResolver) {
        psiClass.accept(new Generator(typeResolver, context));
    }

}
