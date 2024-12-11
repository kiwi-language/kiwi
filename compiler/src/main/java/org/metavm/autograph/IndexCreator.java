package org.metavm.autograph;

import com.intellij.psi.PsiMethodReferenceExpression;
import com.intellij.psi.PsiNewExpression;
import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdKlass;
import org.metavm.flow.Values;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Index;
import org.metavm.object.type.IndexField;
import org.metavm.util.NncUtils;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

@Slf4j
public class IndexCreator extends VisitorBase {

    private final TypeResolver typeResolver;

    public IndexCreator(TypeResolver typeResolver) {
        this.typeResolver = typeResolver;
    }

    @Override
    public void visitNewExpression(PsiNewExpression expression) {
        super.visitNewExpression(expression);
        if (typeResolver.resolveDeclaration(requireNonNull(expression.getType())) instanceof ClassType classType
                && classType.getKlass() == StdKlass.index.get()) {
            var valueKlass = ((ClassType) classType.getTypeArguments().get(1)).getKlass();
            var args = requireNonNull(expression.getArgumentList()).getExpressions();
            var name = (String) TranspileUtils.getConstant(args[0]);
            var index = NncUtils.findRequired(valueKlass.getIndices(), idx -> Objects.equals(idx.getName(), name));
            var methodRef = (PsiMethodReferenceExpression) args[2];
            index.setMethod(requireNonNull(requireNonNull(methodRef.resolve()).getUserData(Keys.Method)));
            var keyType = classType.getTypeArguments().get(0);
            if (keyType instanceof ClassType ct && ct.isValue()) {
                ct.foreachField(keyField -> {
                    if (!keyField.isStatic() && !keyField.isTransient()) {
                        var indexField = NncUtils.find(index.getFields(), f -> Objects.equals(f.getName(), keyField.getName()));
                        if (indexField == null)
                            new IndexField(index, keyField.getName(), keyField.getType(), Values.nullValue());
                        else
                            indexField.setType(keyField.getType());
                    }
                });
            } else {
                var indexField = NncUtils.find(index.getFields(), f -> Objects.equals(f.getName(), "value"));
                if (indexField == null)
                    new IndexField(index, "value", keyType, Values.nullValue());
                else
                    indexField.setType(keyType);
            }
        }
    }

}
