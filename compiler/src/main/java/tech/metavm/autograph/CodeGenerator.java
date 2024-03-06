package tech.metavm.autograph;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.entity.IEntityContext;
import tech.metavm.util.ContextUtil;

public class CodeGenerator {

    public static final Logger LOGGER = LoggerFactory.getLogger(CodeGenerator.class);

    private final IEntityContext context;

    public CodeGenerator(IEntityContext context) {
        this.context = context;
    }

    void transform(PsiClass psiClass) {
        try (var ignored = ContextUtil.getProfiler().enter("CodeGenerator.transform")) {
            executeCommand(() -> {
                resolveQnAndActivity(psiClass);
                psiClass.accept(new VarargsTransformer());
//                psiClass.accept(new ConditionalExpressionTransformer());
//                resolveQnAndActivity(psiClass);
                psiClass.accept(new DefaultConstructorCreator());
                psiClass.accept(new SwitchExpressionTransformer());
                resolveQnAndActivity(psiClass);
                psiClass.accept(new SwitchLabelStatementTransformer());
                psiClass.accept(new DefaultSwitchCaseAppender());
                psiClass.accept(new ForeachTransformer());
                resolveQnAndActivity(psiClass);
                psiClass.accept(new ForTransformer());
                resolveQnAndActivity(psiClass);
                psiClass.accept(new BreakTransformer());
                resolveQnAndActivity(psiClass);
                psiClass.accept(new ContinueTransformer());
                resolveQnAndActivity(psiClass);
                psiClass.accept(new StringConcatTransformer());
            });
        }
    }

    private void resolveQnAndActivity(PsiClass psiClass) {
        psiClass.accept(new QnResolver());
        psiClass.accept(new ActivityAnalyzer());
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
        psiClass.accept(new IndexDefiner(typeResolver, context));
    }

}
