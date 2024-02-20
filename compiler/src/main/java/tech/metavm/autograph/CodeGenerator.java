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
        try(var entry = ContextUtil.getProfiler().enter("CodeGenerator.transform")) {
            executeCommand(() -> {
                try(var ignored1 = ContextUtil.getProfiler().enter("VarargsTransformer")) {
                    psiClass.accept(new VarargsTransformer());
                }
                try(var ignored1 = ContextUtil.getProfiler().enter("DefaultConstructorCreator")) {
                    psiClass.accept(new DefaultConstructorCreator());
                }
                try(var ignored1 = ContextUtil.getProfiler().enter("SwitchExpressionTransformer")) {
                    psiClass.accept(new SwitchExpressionTransformer());
                }
                try(var ignored1 = ContextUtil.getProfiler().enter("SwitchLabelStatementTransformer")) {
                    psiClass.accept(new SwitchLabelStatementTransformer());
                }
                try(var ignored1 = ContextUtil.getProfiler().enter("ForeachTransformer")) {
                    psiClass.accept(new ForeachTransformer());
                }
                try(var ignored1 = ContextUtil.getProfiler().enter("ForTransformer")) {
                    psiClass.accept(new ForTransformer());
                }
                try(var ignored1 = ContextUtil.getProfiler().enter("BreakTransformer")) {
                    psiClass.accept(new BreakTransformer());
                }
                try(var ignored1 = ContextUtil.getProfiler().enter("ContinueTransformer")) {
                    psiClass.accept(new ContinueTransformer());
                }
                try(var ignored1 = ContextUtil.getProfiler().enter("StringConcatTransformer")) {
                    psiClass.accept(new StringConcatTransformer());
                }
            });
        }
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
