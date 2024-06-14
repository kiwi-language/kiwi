package org.metavm.autograph;

import com.intellij.lang.java.parser.JavaParser;
import com.intellij.lang.java.parser.JavaParserUtil;
import com.intellij.pom.java.LanguageLevel;
import com.intellij.psi.PsiBinaryExpression;
import com.intellij.psi.PsiManager;
import com.intellij.psi.PsiReferenceExpression;
import com.intellij.psi.impl.source.CharTableImpl;
import com.intellij.psi.impl.source.DummyHolderFactory;
import com.intellij.psi.impl.source.JavaDummyElement;
import com.intellij.psi.impl.source.SourceTreeToPsiMap;
import com.intellij.util.CharTable;

public class ParserLab {

    public static void main(String[] args) {
        final JavaParserUtil.ParserWrapper EXPRESSION = JavaParser.INSTANCE.getExpressionParser()::parse;
        CharTable charTable = new CharTableImpl();
        var holder = DummyHolderFactory.createHolder(
                PsiManager.getInstance(TranspileTestTools.getProject()),
                new JavaDummyElement("a + b", EXPRESSION, LanguageLevel.HIGHEST),
                null,
                charTable
        );
        PsiBinaryExpression expr = (PsiBinaryExpression) SourceTreeToPsiMap.treeElementToPsi(holder.getTreeElement().getFirstChildNode());
        assert expr != null;
        var op = expr.getOperationSign();
        PsiReferenceExpression first = (PsiReferenceExpression) expr.getLOperand(),
                second = (PsiReferenceExpression) expr.getROperand();
        System.out.println(expr);
    }

}
