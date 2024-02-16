package tech.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiThisExpression;
import com.intellij.psi.PsiType;
import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.autograph.mocks.PTypeFoo;
import tech.metavm.autograph.mocks.SignatureFoo;
import tech.metavm.autograph.mocks.TypeFoo;
import tech.metavm.entity.DummyGenericDeclaration;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.flow.MethodBuilder;
import tech.metavm.flow.Parameter;
import tech.metavm.object.type.ClassTypeBuilder;
import tech.metavm.object.type.TypeVariable;
import tech.metavm.object.type.UncertainType;
import tech.metavm.object.type.mocks.TypeProviders;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectionUtils;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class TranspileUtilTest extends TestCase {

    private TypeProviders typeProviders;

    @Override
    protected void setUp() throws Exception {
        TranspileTestTools.touch();
        MockStandardTypesInitializer.init();
        typeProviders = new TypeProviders();
    }

    public void testGetTemplateType() {
        Visitor visitor = new Visitor();
        var foo = TranspileTestTools.getPsiJavaFile(PTypeFoo.class);
        foo.accept(visitor);
    }

    public void testMatchMethod() {
        var method = ReflectionUtils.getMethod(List.class, "get", int.class);
        var psiClass = TranspileTestTools.getPsiClass(TypeFoo.class);
        var psiMethod = NncUtils.findRequired(psiClass.getMethods(), m -> m.getName().equals("get"));
        NncUtils.requireTrue(TranspileUtil.matchMethod(psiMethod, method));
    }

    public void testGetSignature() {
        var listClass = Objects.requireNonNull(TranspileUtil.createType(List.class).resolve());
        var getMethod = NncUtils.findRequired(listClass.getMethods(), method -> method.getName().equals("get"));
        var signature = TranspileUtil.getSignature(getMethod, null);
        Assert.assertEquals(
                new MethodSignature(
                        TranspileUtil.createType(List.class), false, "get", List.of(
                        TranspileUtil.createPrimitiveType(int.class))),
                signature
        );
    }

    public void testGetSignatureString() {
        var listClass = Objects.requireNonNull(TranspileUtil.createType(SignatureFoo.class).resolve());
        var getMethod = NncUtils.findRequired(listClass.getMethods(), method -> method.getName().equals("add"));
        var sig = TranspileUtil.getInternalName(getMethod);

        var fooType = ClassTypeBuilder.newBuilder("SignatureFoo", SignatureFoo.class.getName()).build();
        var typeVar = new TypeVariable(null, "T", "T", DummyGenericDeclaration.INSTANCE);

        var addMethod = MethodBuilder.newBuilder(fooType, "add", "add", typeProviders.functionTypeProvider)
                .typeParameters(List.of(typeVar))
                .parameters(
                        new Parameter(null, "list", "list",
                                typeProviders.parameterizedTypeProvider.getParameterizedType(
                                        StandardTypes.getListType(),
                                        List.of(new UncertainType(null, typeVar, StandardTypes.getAnyType()))
                                )
                        ),
                        new Parameter(null, "element", "element", typeVar)
                )
                .build();
        var sig2 = addMethod.getInternalName(null);
        Assert.assertEquals(sig, sig2);
    }

    private static class Visitor extends JavaRecursiveElementVisitor {

        private PsiType templateType;

        @Override
        public void visitThisExpression(PsiThisExpression expression) {
            PsiType selfType = expression.getType();
            Assert.assertEquals(selfType, templateType);
        }

        @Override
        public void visitField(PsiField field) {
            super.visitField(field);
            templateType = TranspileUtil.createTemplateType(requireNonNull(field.getContainingClass()));
            super.visitField(field);
        }
    }

}