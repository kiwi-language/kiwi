package org.metavm.autograph;

import com.intellij.psi.*;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static org.metavm.autograph.TranspileUtils.createExpressionFromText;

public class EnumSwitchTransformer extends VisitorBase {

    private SwitchInfo currentSwitch;

    @Override
    public void visitSwitchStatement(PsiSwitchStatement statement) {
        enterSwitch(statement);
        super.visitSwitchStatement(statement);
        exitSwitch();
    }

    @Override
    public void visitSwitchExpression(PsiSwitchExpression expression) {
        enterSwitch(expression);
        super.visitSwitchExpression(expression);
        exitSwitch();
    }

    @Override
    public void visitSwitchLabelStatement(PsiSwitchLabelStatement statement) {
        super.visitSwitchLabelStatement(statement);
        var s = currentSwitch();
        if (s.isEnum) {
            //                if (element instanceof PsiTypeTestPattern || element instanceof PsiPatternGuard)
            //                    s.patternUsed = true;
            //                else if (element instanceof PsiLiteralExpression l && l.getValue() == null)
            //                    s.nullCase = element;
            //                else {
            //                    s.coveredCount++;
            //                    var ec = (PsiEnumConstant) Objects.requireNonNull(((PsiReferenceExpression) element).resolve());
            //                    var ordinal = Objects.requireNonNull(ec.getUserData(Keys.ORDINAL));
            //                    element.replace(TranspileUtils.createExpressionFromText(Integer.toString(ordinal)));
            //                }
            s.cases.addAll(Arrays.asList(Objects.requireNonNull(statement.getCaseLabelElementList()).getElements()));
        }
    }

    private void enterSwitch(PsiSwitchBlock switchBlock) {
        currentSwitch = new SwitchInfo(switchBlock, currentSwitch);
    }

    private void exitSwitch() {
        var s = Objects.requireNonNull(currentSwitch);
        if (s.isEnum) {
            boolean intSwitch = true;
            for (PsiCaseLabelElement aCase : s.cases) {
                if (!(aCase instanceof PsiReferenceExpression refExr && refExr.resolve() instanceof PsiEnumConstant)) {
                    intSwitch = false;
                    break;
                }
            }
            if (intSwitch) {
                for (PsiCaseLabelElement aCase : s.cases) {
                    var ordinal = Objects.requireNonNull(((PsiReferenceExpression) aCase).resolve()).getUserData(Keys.ORDINAL);
                    aCase.replace(createExpressionFromText(Objects.requireNonNull(ordinal).toString()));
                }
                var expr = Objects.requireNonNull(s.switchBlock.getExpression());
                expr.replace(createExpressionFromText(expr.getText() + ".ordinal()"));
            } else {
                for (PsiCaseLabelElement aCase : s.cases) {
                    if (aCase instanceof PsiReferenceExpression ref && ref instanceof PsiEnumConstant ec) {
                        aCase.replace(
                                createExpressionFromText(TranspileUtils.getQualifiedName(ec), null)
                        );
                    }
                }
            }

        }
        currentSwitch = s.parent;
    }


    private SwitchInfo currentSwitch() {
        return Objects.requireNonNull(currentSwitch);
    }

    private static class SwitchInfo {
        private final PsiSwitchBlock switchBlock;
        private final boolean isEnum;
        private final @Nullable SwitchInfo parent;
        private final List<PsiCaseLabelElement> cases = new ArrayList<>();

        private SwitchInfo(PsiSwitchBlock switchBlock, @Nullable SwitchInfo parent) {
            this.switchBlock = switchBlock;
            this.isEnum = TranspileUtils.isEnum(Objects.requireNonNull(switchBlock.getExpression()).getType());
            this.parent = parent;
        }
    }

}
