package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiThisExpression;
import com.intellij.psi.PsiType;
import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.autograph.mocks.PTypeFoo;
import org.metavm.autograph.mocks.RecordFoo;
import org.metavm.autograph.mocks.SignatureFoo;
import org.metavm.autograph.mocks.TypeFoo;
import org.metavm.entity.DummyGenericDeclaration;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StandardTypes;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.type.ClassTypeBuilder;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.UncertainType;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import java.util.List;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

public class TranspileUtilTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        TranspileTestTools.touch();
        MockStandardTypesInitializer.init();
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
        var listClass = Objects.requireNonNull(TranspileUtil.createClassType(List.class).resolve());
        var getMethod = NncUtils.findRequired(listClass.getMethods(), method -> method.getName().equals("get"));
        var signature = TranspileUtil.getSignature(getMethod, null);
        Assert.assertEquals(
                new MethodSignature(
                        TranspileUtil.createClassType(List.class), false, "get", List.of(
                        TranspileUtil.createPrimitiveType(int.class))),
                signature
        );
    }

    public void testGetInternalName() {
        var listClass = Objects.requireNonNull(TranspileUtil.createClassType(SignatureFoo.class).resolve());
        var getMethod = NncUtils.findRequired(listClass.getMethods(), method -> method.getName().equals("add"));
        var sig = TranspileUtil.getInternalName(getMethod);

        var fooType = ClassTypeBuilder.newBuilder("SignatureFoo", SignatureFoo.class.getName()).build();
        var typeVar = new TypeVariable(null, "T", "T", DummyGenericDeclaration.INSTANCE);

        var addMethod = MethodBuilder.newBuilder(fooType, "add", "add")
                .typeParameters(List.of(typeVar))
                .parameters(
                        new Parameter(null, "list", "list",
                                StandardTypes.getListKlass().getParameterized(List.of(new UncertainType(typeVar.getType(), StandardTypes.getNullableAnyType()))).getType()
                        ),
                        new Parameter(null, "element", "element", typeVar.getType())
                )
                .build();
        var sig2 = addMethod.getInternalName(null);
        Assert.assertEquals(sig, sig2);
    }

    public void testGetInternalNameWithImplicitTypes() {
        var klass = Objects.requireNonNull(TranspileUtil.createClassType(SignatureFoo.class).resolve());
        var testMethod = NncUtils.findRequired(klass.getMethods(), method -> method.getName().equals("test"));
        var internalName = TranspileUtil.getInternalName(testMethod, List.of(
                TranspileUtil.createType(String.class),
                TranspileUtil.createPrimitiveType(int.class)
        ));
        Assert.assertEquals("org.metavm.autograph.mocks.SignatureFoo.test(String,Long,Any)", internalName);
    }

    public void testIsStruct() {
        var file = TranspileTestTools.getPsiJavaFile(RecordFoo.class);
        Assert.assertTrue(TranspileUtil.isStruct(file.getClasses()[0]));
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