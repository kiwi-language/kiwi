package org.metavm.autograph;

import com.intellij.openapi.command.CommandProcessor;
import com.intellij.psi.PsiClass;
import org.metavm.entity.IEntityContext;
import org.metavm.util.ContextUtil;
import org.metavm.util.DebugEnv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CodeGenerator {

    public static final Logger logger = LoggerFactory.getLogger(CodeGenerator.class);

    private final IEntityContext context;

    public CodeGenerator(IEntityContext context) {
        this.context = context;
    }

    void transform(PsiClass psiClass) {
        try (var ignored = ContextUtil.getProfiler().enter("CodeGenerator.transform")) {
            executeCommand(() -> {
                resolveQnAndActivity(psiClass);
                psiClass.accept(new VarargsTransformer());
                psiClass.accept(new DefaultConstructorCreator());
                resolveQnAndActivity(psiClass);
                psiClass.accept(new SwitchLabelStatementTransformer());
                psiClass.accept(new NullSwitchCaseAppender());
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
        if(DebugEnv.debugging)
            psiClass.accept(new ActivityPrinter());
    }

    void generateDecl(PsiClass psiClass, TypeResolver typeResolver) {
        psiClass.accept(new QnResolver());
        psiClass.accept(new ActivityAnalyzer());
        if(DebugEnv.debugging)
            psiClass.accept(new ActivityPrinter());
        var astToCfg = new AstToCfg();
        psiClass.accept(astToCfg);
        if(DebugEnv.debugging) {
            astToCfg.getGraphs().values().forEach(Graph::log);
        }
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
                        logger.error("Fail to run compile command", e);
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
