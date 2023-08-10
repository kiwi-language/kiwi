package tech.metavm.autograph;

import com.intellij.lang.java.parser.JavaParser;
import com.intellij.lang.java.parser.JavaParserUtil;
import com.intellij.openapi.project.Project;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.*;
import com.intellij.psi.impl.source.*;
import com.intellij.util.CharTable;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Templates {

    private static Templates INSTANCE;

    private final Project project;
    private final CharTable charTable = new CharTableImpl();

    public Templates(Project project) {
        this.project = project;
    }

    public static Templates getInstance() {
        return INSTANCE;
    }

    public PsiLiteralExpression createLiteral(String template) {
        return (PsiLiteralExpression) replaceAsExpression(template, Map.of());
    }

    public PsiReferenceExpression createReference(String name) {
        return (PsiReferenceExpression) replaceAsExpression(name, Map.of());
    }

    public PsiExpression replaceAsExpression(String template, Map<String, PsiElement> replacements) {
        JavaParserUtil.ParserWrapper parser = JavaParser.INSTANCE.getExpressionParser()::parse;
        var holder = createDummyHolder(template, parser);
        PsiElement expr = SourceTreeToPsiMap.treeToPsiNotNull(holder.getTreeElement().getFirstChildNode());
        Map<String, List<PsiElement>> replacementMap = new HashMap<>();
        replacements.forEach((k, v) -> replacementMap.put(k, List.of(v)));
        expr.accept(new ReplaceVisitor(replacementMap));
        return (PsiExpression) expr;
    }

    public List<PsiStatement> replaceAsStatements(String template, Map<String, List<PsiElement>> replacements) {
        JavaParserUtil.ParserWrapper parser = JavaParser.INSTANCE.getStatementParser()::parseStatements;
        var holder = createDummyHolder(template, parser);
        var children = holder.getChildren();
        List<PsiStatement> statements = new ArrayList<>();
        for (PsiElement child : children) {
            if(child instanceof PsiStatement stmt) {
                stmt.accept(new ReplaceVisitor(replacements));
                statements.add(stmt);
            }
        }
        return statements;
    }

    public PsiMethod replaceAsMethod(String template, Map<String, List<PsiElement>> replacements) {
        JavaParserUtil.ParserWrapper parser = builder ->
                JavaParser.INSTANCE.getDeclarationParser().parseClassBodyDeclarations(builder, false);
        var holder = createDummyHolder(template, parser);
        PsiMethod method = SourceTreeToPsiMap.treeToPsiNotNull(holder.getTreeElement().getFirstChildNode());
        method.accept(new ReplaceVisitor(replacements));
        return method;
    }

    private DummyHolder createDummyHolder(String template, JavaParserUtil.ParserWrapper parser) {
        return DummyHolderFactory.createHolder(
                PsiManager.getInstance(project),
                new JavaDummyElement(template, parser, LanguageLevel.HIGHEST),
                null,
                charTable
        );
    }

    private static class ReplaceVisitor extends JavaRecursiveElementVisitor {

        private final Map<String, List<PsiElement>> replacements;

        private ReplaceVisitor(Map<String, List<PsiElement>> replacements) {
            this.replacements = replacements;
        }

        @Override
        public void visitElement(@NotNull PsiElement element) {
            for (int i = 0; i < element.getChildren().length; i++) {
                element.getChildren()[i].accept(this);
            }
        }

        @Override
        public void visitReferenceExpression(PsiReferenceExpression expression) {
            var replacement = replacements.get(expression.getQualifiedName());
            if (replacement != null) {
                var firstRep = replacement.get(0);
                PsiElement replaced = expression.replace(firstRep);
                for (int i = 1; i < replacement.size(); i++) {
                    replaced = replaced.getParent().addAfter(replaced, replacement.get(i));
                }
            }
        }
    }

}
