package tech.metavm.autograph;

import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiMethodCallExpression;
import com.intellij.psi.PsiRecordComponent;
import tech.metavm.entity.EntityIndex;
import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.StandardTypes;
import tech.metavm.expression.ThisExpression;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.flow.Values;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.FunctionType;
import tech.metavm.object.type.Index;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class IndexDefiner extends VisitorBase {

    private Index currentIndex;
    private final TypeResolver typeResolver;
    private final IEntityContext context;
    private MethodGenerator builder;

    public IndexDefiner(TypeResolver typeResolver, IEntityContext context) {
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
    public void visitMethod(PsiMethod psiMethod) {
        var psiClass = requireNonNull(psiMethod.getContainingClass());
        if (TranspileUtil.getAnnotation(psiClass, EntityIndex.class) != null && psiMethod.isConstructor()) {
            var dummyType = ClassTypeBuilder.newBuilder("IndexDummy", "IndexDummy").build();
            var dummyMethod = MethodBuilder.newBuilder(
                            dummyType,
                            "dummy", "dummy"
                    )
                    .staticType(new FunctionType(null, List.of(dummyType.getType()), StandardTypes.getVoidType()))
                    .type(new FunctionType(null, List.of(), StandardTypes.getVoidType()))
                    .build();
            builder = new MethodGenerator(dummyMethod, typeResolver, context, this);
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
