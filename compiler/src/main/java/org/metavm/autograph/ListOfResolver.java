package org.metavm.autograph;

import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiModifier;
import org.metavm.entity.StdKlass;
import org.metavm.entity.StdMethod;
import org.metavm.flow.MethodRef;
import org.metavm.flow.Node;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.KlassType;
import org.metavm.util.NncUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class ListOfResolver implements MethodCallResolver {

    private static final List<MethodSignature> SIGNATURES;

    static {
        var listType = TranspileUtils.createClassType(List.class);
        var listClass = requireNonNull(listType.resolve());
        var methods = NncUtils.filter(List.of(listClass.getMethods()),
                m -> m.getModifierList().hasModifierProperty(PsiModifier.STATIC) && m.getName().equals("of"));
        var signatures = new ArrayList<MethodSignature>();
        for (PsiMethod method : methods) {
            signatures.add(TranspileUtils.getSignature(method, listType));
        }
        SIGNATURES = Collections.unmodifiableList(signatures);
    }

    @Override
    public List<MethodSignature> getSignatures() {
        return SIGNATURES;
    }

    @Override
    public Node resolve(PsiMethodCallExpression methodCallExpression,
                        ExpressionResolver expressionResolver,
                        MethodGenerator methodGenerator) {
        var methodGenerics = methodCallExpression.resolveMethodGenerics();
        var method = (PsiMethod) requireNonNull(methodGenerics.getElement());
        var listType = (ClassType) expressionResolver.getTypeResolver().resolve(
                methodGenerics.getSubstitutor().substitute(method.getReturnType()));
        var arrayListKlass = StdKlass.arrayList.get();
        var arrayListType = new KlassType(null, arrayListKlass, List.of(listType.getFirstTypeArgument()));
        var list = methodGenerator.createNew(
                new MethodRef(arrayListType, arrayListKlass.getDefaultConstructor(), List.of()),
                false,
                true);
        var expressions = methodCallExpression.getArgumentList().getExpressions();
        for (PsiExpression psiExpression : expressions) {
            methodGenerator.createDup();
            expressionResolver.resolve(psiExpression);
            methodGenerator.createMethodCall(
                    new MethodRef(arrayListType, StdMethod.arrayListAdd.get(), List.of()),
                    List.of(),
                    List.of()
            );
            methodGenerator.createPop();
        }
        return list;
    }
}
