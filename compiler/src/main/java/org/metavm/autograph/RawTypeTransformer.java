package org.metavm.autograph;

import com.intellij.psi.PsiClassType;
import com.intellij.psi.PsiInstanceOfExpression;
import com.intellij.psi.PsiTypeElement;
import lombok.extern.slf4j.Slf4j;

import java.util.Objects;

@Slf4j
public class RawTypeTransformer extends VisitorBase {

    private boolean insideCheckType;

    @Override
    public void visitTypeElement(PsiTypeElement type) {
        var isCheckType = type.getParent() instanceof PsiInstanceOfExpression;
        if(isCheckType)
            insideCheckType = true;
        try {
            if (insideCheckType) {
                if (type.getType() instanceof PsiClassType classType) {
                    var klass = Objects.requireNonNull(classType.resolve());
                    if (klass.getTypeParameters().length > 0 && classType.getParameterCount() == 0) {
                        var sb = new StringBuilder(Objects.requireNonNull(klass.getQualifiedName())).append('<');
                        for (int i = 0; i < klass.getTypeParameters().length; i++) {
                            if (i > 0)
                                sb.append(',');
                            sb.append('?');
                        }
                        sb.append('>');
                        replace(type, TranspileUtils.createTypeElement(sb.toString(), type.getContext()));
                        return;
                    }
                }
                super.visitTypeElement(type);
            }
        }
        finally {
            if (isCheckType)
                insideCheckType = false;
        }
    }

}
