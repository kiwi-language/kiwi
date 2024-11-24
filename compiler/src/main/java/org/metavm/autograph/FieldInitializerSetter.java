package org.metavm.autograph;

import com.intellij.lang.jvm.types.JvmPrimitiveTypeKind;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiPrimitiveType;
import com.intellij.psi.PsiType;

import java.util.Map;
import java.util.Objects;

public class FieldInitializerSetter extends VisitorBase {

    @SuppressWarnings("UnstableApiUsage")
    public static final Map<JvmPrimitiveTypeKind, PsiExpression> defaultExpressions = Map.of(
            JvmPrimitiveTypeKind.BOOLEAN, TranspileUtils.createExpressionFromText("false"),
            JvmPrimitiveTypeKind.BYTE, TranspileUtils.createExpressionFromText("0"),
            JvmPrimitiveTypeKind.SHORT, TranspileUtils.createExpressionFromText("0"),
            JvmPrimitiveTypeKind.INT, TranspileUtils.createExpressionFromText("0"),
            JvmPrimitiveTypeKind.LONG, TranspileUtils.createExpressionFromText("0L"),
            JvmPrimitiveTypeKind.FLOAT, TranspileUtils.createExpressionFromText("0.0f"),
            JvmPrimitiveTypeKind.DOUBLE, TranspileUtils.createExpressionFromText("0.0d"),
            JvmPrimitiveTypeKind.CHAR, TranspileUtils.createExpressionFromText("'\\0'")
    );

    public static final PsiExpression nullExpression = TranspileUtils.createExpressionFromText("null");

    @Override
    public void visitField(PsiField field) {
        super.visitField(field);
        if(field.getInitializer() == null && !TranspileUtils.isFinal(field)) {
            field.setInitializer(getDefaultValue(field.getType()));
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private PsiExpression getDefaultValue(PsiType type) {
        if(type instanceof PsiPrimitiveType primitiveType)
            return Objects.requireNonNull(defaultExpressions.get(primitiveType.getKind()));
        else
            return nullExpression;
    }

}
