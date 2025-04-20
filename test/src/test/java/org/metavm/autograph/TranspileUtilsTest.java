package org.metavm.autograph;

import com.intellij.psi.JavaRecursiveElementVisitor;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiThisExpression;
import com.intellij.psi.PsiType;
import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.metavm.api.Enum;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StdKlass;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.type.KlassType;
import org.metavm.object.type.TypeVariable;
import org.metavm.object.type.Types;
import org.metavm.object.type.UncertainType;
import org.metavm.util.ReflectionUtils;
import org.metavm.util.TestUtils;
import org.metavm.util.Utils;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

@Slf4j
public class TranspileUtilsTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        TranspileTestTools.touch();
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
    }

    public void testGetTemplateType() {
        Visitor visitor = new Visitor();
        var foo = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.PTypeFoo");
        foo.accept(visitor);
    }

    public void testMatchMethod() {
        var method = ReflectionUtils.getMethod(List.class, "get", int.class);
        var psiClass = TranspileTestTools.getPsiClass("org.metavm.autograph.mocks.TypeFoo");
        var psiMethod = Utils.findRequired(psiClass.getMethods(), m -> m.getName().equals("get"));
        Utils.require(TranspileUtils.matchMethod(psiMethod, method));
    }

    public void testGetSignature() {
        var listClass = Objects.requireNonNull(TranspileUtils.createClassType(List.class).resolve());
        var getMethod = Utils.findRequired(listClass.getMethods(), method -> method.getName().equals("get"));
        var signature = TranspileUtils.getSignature(getMethod, null);
        Assert.assertEquals(
                new MethodSignature(
                        TranspileUtils.createClassType(List.class), false, "get", List.of(
                        TranspileUtils.createPrimitiveType(int.class))),
                signature
        );
    }

    public void testGetInternalName() {
        var psiClass = TranspileTestTools.getPsiClass("org.metavm.autograph.mocks.SignatureFoo");
        var listClass = Objects.requireNonNull(TranspileUtils.createType(psiClass).resolve());
        var getMethod = Utils.findRequired(listClass.getMethods(), method -> method.getName().equals("add"));
        var sig = TranspileUtils.getInternalName(getMethod);

        var fooType = TestUtils.newKlassBuilder("org.metavm.autograph.mocks.SignatureFoo").build();

        var addMethod = MethodBuilder.newBuilder(fooType, "add").build();
        var typeVar = new TypeVariable(fooType.nextChildId(), "T", addMethod);
        addMethod.setTypeParameters(List.of(typeVar));

        addMethod.setParameters(List.of(
                new Parameter(fooType.nextChildId(), "list",
                        Types.getNullableType(
                                KlassType.create(StdKlass.list.get(), List.of(
                                        new UncertainType(typeVar.getType(), Types.getAnyType())))
                        ), addMethod
                ),
                new Parameter(fooType.nextChildId(), "element", Types.getNullableType(typeVar.getType()), addMethod)
        ));
        var sig2 = addMethod.getInternalName(null);
        Assert.assertEquals(sig, sig2);
    }

    public void testGetInternalNameWithImplicitTypes() {
        var psiClass = TranspileTestTools.getPsiClass("org.metavm.autograph.mocks.SignatureFoo");
        var klass = Objects.requireNonNull(TranspileUtils.createType(psiClass).resolve());
        var testMethod = Utils.findRequired(klass.getMethods(), method -> method.getName().equals("test"));
        var internalName = TranspileUtils.getInternalName(testMethod);
        Assert.assertEquals("org.metavm.autograph.mocks.SignatureFoo.test(Any|Null)", internalName);
    }

    public void testIsStruct() {
        var file = TranspileTestTools.getPsiJavaFileByName("org.metavm.autograph.mocks.RecordFoo");
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

    public void testIsAnnotationPresent() {
        var k = TranspileTestTools.getPsiClass("org.metavm.EnumAnnotatedFoo");
        Assert.assertTrue(TranspileUtils.isAnnotationPresent(k, Enum.class));
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