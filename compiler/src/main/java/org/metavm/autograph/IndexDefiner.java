package org.metavm.autograph;

import com.intellij.psi.*;
import org.metavm.api.EntityIndex;
import org.metavm.entity.IEntityContext;
import org.metavm.expression.ThisExpression;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Values;
import org.metavm.object.type.FunctionType;
import org.metavm.object.type.Index;
import org.metavm.object.type.KlassBuilder;
import org.metavm.object.type.Types;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class IndexDefiner extends SkipDiscardedVisitor {

    private final PsiClass psiClass;
    private Index currentIndex;
    private final TypeResolver typeResolver;
    private final IEntityContext context;
    private MethodGenerator builder;

    public IndexDefiner(PsiClass psiClass, TypeResolver typeResolver, IEntityContext context) {
        this.psiClass = psiClass;
        this.typeResolver = typeResolver;
        this.context = context;
    }

    @Override
    public void visitField(PsiField field) {
    }

    @Override
    public void visitRecordComponent(PsiRecordComponent recordComponent) {
    }

    @Override
    public void visitClass(PsiClass aClass) {
        if(TranspileUtils.getAnnotation(aClass, EntityIndex.class) != null || aClass == this.psiClass)
            super.visitClass(aClass);
    }

    @Override
    public void visitMethod(PsiMethod psiMethod) {
        var psiClass = requireNonNull(psiMethod.getContainingClass());
        if (TranspileUtils.getAnnotation(psiClass, EntityIndex.class) != null && psiMethod.isConstructor()) {
            var dummyType = KlassBuilder.newBuilder("IndexDummy", "IndexDummy").build();
            var dummyMethod = MethodBuilder.newBuilder(
                            dummyType,
                            "dummy", "dummy"
                    )
                    .staticType(new FunctionType(List.of(dummyType.getType()), Types.getVoidType()))
                    .type(new FunctionType(List.of(), Types.getVoidType()))
                    .build();
            builder = new MethodGenerator(dummyMethod, typeResolver, this);
            currentIndex = requireNonNull(psiClass.getUserData(Keys.INDEX));
            var param = psiMethod.getParameterList().getParameters()[0];
            builder.setVariable(param.getName(), new ThisExpression(currentIndex.getDeclaringType().getType()));
            super.visitMethod(psiMethod);
        }
    }

    @Override
    public void visitMethodCallExpression(PsiMethodCallExpression expression) {
        var args = requireNonNull(expression.getArgumentList().getExpressions());
        for (int i = 0; i < args.length; i++) {
            var indexField = currentIndex.getFields().get(i);
            var expr = builder.getExpressionResolver().resolve(args[i]);
            indexField.setValue(Values.expression(expr));
        }
    }
}
