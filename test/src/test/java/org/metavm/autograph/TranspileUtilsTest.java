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
import org.metavm.entity.StdKlass;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.type.KlassBuilder;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.object.type.UncertainType;
import org.metavm.util.NncUtils;
import org.metavm.util.ReflectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

public class TranspileUtilsTest extends TestCase {

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
        NncUtils.requireTrue(TranspileUtils.matchMethod(psiMethod, method));
    }

    public void testGetSignature() {
        var listClass = Objects.requireNonNull(TranspileUtils.createClassType(List.class).resolve());
        var getMethod = NncUtils.findRequired(listClass.getMethods(), method -> method.getName().equals("get"));
        var signature = TranspileUtils.getSignature(getMethod, null);
        Assert.assertEquals(
                new MethodSignature(
                        TranspileUtils.createClassType(List.class), false, "get", List.of(
                        TranspileUtils.createPrimitiveType(int.class))),
                signature
        );
    }

    public void testGetInternalName() {
        var listClass = Objects.requireNonNull(TranspileUtils.createClassType(SignatureFoo.class).resolve());
        var getMethod = NncUtils.findRequired(listClass.getMethods(), method -> method.getName().equals("add"));
        var sig = TranspileUtils.getInternalName(getMethod);

        var fooType = KlassBuilder.newBuilder("SignatureFoo", SignatureFoo.class.getName()).build();
        var typeVar = new TypeVariable(null, "T", "T", DummyGenericDeclaration.INSTANCE);

        var addMethod = MethodBuilder.newBuilder(fooType, "add", "add")
                .typeParameters(List.of(typeVar))
                .parameters(
                        new Parameter(null, "list", "list",
                                StdKlass.list.get().getParameterized(List.of(new UncertainType(typeVar.getType(), Types.getNullableAnyType()))).getType()
                        ),
                        new Parameter(null, "element", "element", typeVar.getType())
                )
                .build();
        var sig2 = addMethod.getInternalName(null);
        Assert.assertEquals(sig, sig2);
    }

    public void testGetInternalNameWithImplicitTypes() {
        var klass = Objects.requireNonNull(TranspileUtils.createClassType(SignatureFoo.class).resolve());
        var testMethod = NncUtils.findRequired(klass.getMethods(), method -> method.getName().equals("test"));
        var internalName = TranspileUtils.getInternalName(testMethod, List.of(
                TranspileUtils.createType(String.class),
                TranspileUtils.createPrimitiveType(int.class)
        ));
        Assert.assertEquals("org.metavm.autograph.mocks.SignatureFoo.test(String,Long,Any)", internalName);
    }

    public void testIsStruct() {
        var file = TranspileTestTools.getPsiJavaFile(RecordFoo.class);
        Assert.assertTrue(TranspileUtils.isStruct(file.getClasses()[0]));
    }

    public void testGetMethodSignature() {
        var requireNonNull = ReflectionUtils.getMethod(Objects.class, "requireNonNull", Object.class, Supplier.class);
        var signature = TranspileUtils.getMethodSignature(requireNonNull);
        var expectedSignature = new MethodSignature(
                TranspileUtils.createClassType(Objects.class),
                true,
                "requireNonNull",
                List.of(
                        TranspileUtils.createVariableType(requireNonNull, 0),
                        TranspileUtils.createType(
                                Supplier.class,
                                TranspileUtils.createClassType(String.class)
                        )
                )
        );
        Assert.assertEquals(expectedSignature, signature);
    }

    public void testGetMethodSignatureWithVarArgs() {
        var format = ReflectionUtils.getMethod(String.class, "format", String.class, Object[].class);
        var signature = TranspileUtils.getMethodSignature(format);
        var expectedSignature = new MethodSignature(
                TranspileUtils.createClassType(String.class),
                true,
                "format",
                List.of(
                        TranspileUtils.createClassType(String.class),
                        TranspileUtils.createEllipsisType(Object[].class)
                )
        );
        Assert.assertEquals(expectedSignature, signature);
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
            templateType = TranspileUtils.createTemplateType(requireNonNull(field.getContainingClass()));
            super.visitField(field);
        }
    }

}