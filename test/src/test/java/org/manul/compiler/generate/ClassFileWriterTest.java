package org.manul.compiler.generate;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.manul.classfile.ClassFileReader;
import org.manul.compiler.CompilerTestUtils;
import org.manul.compiler.analyze.Lower;
import org.manul.compiler.diag.DummyLog;
import org.manul.compiler.syntax.ClassDecl;
import org.manul.entity.StdKlass;
import org.manul.entity.mocks.MockEntityRepository;
import org.manul.flow.KlassInput;
import org.manul.flow.Method;
import org.manul.object.type.Klass;
import org.manul.util.TestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
public class ClassFileWriterTest extends TestCase {

    public void testLivingBeing() {
        process("manul/LivingBeing.mnl");
    }

    public void test() {
        process("manul/shopping.mnl");
    }

    public void testEnum() {
        process("manul/enum.mnl");
    }

    public void testLambda() {
        process("manul/Lambda.mnl");
    }

    public void testAnonClassWithArgs() {
        process("manul/basics/anonymous_class/AnonymousClassWithArgs.mnl");

    }

    public void testInnerClass() {
        process("manul/inner_klass.mnl");
    }

    public void testInnerClass2() {
        process("manul/inner_class.mnl");
    }

    public void testGenericOverloading() {
        process("manul/GenericOverloading.mnl");
    }

    public void testDDL() {
        process("manul/ddl_after.mnl");
    }

    public void testEnumConversion() {
        process("manul/value_to_enum_ddl_after.mnl");
    }

    public void testLab() {
        process("manul/lab.mnl");
    }

    public void testAssign() {
        process("manul/assign.mnl");
    }

    public void testConditional() {
        process("manul/conditional.mnl");
    }

    public void testIntersectionType() {
        process("manul/intersection_type.mnl");
    }

    public void testFieldInit() {
        process("manul/field_init.mnl");
    }

    public void testForeach() {
        process("manul/foreach.mnl");
    }

    public void testArrayInitializer() {
        process("manul/basics/arrayinitializer/ArrayInitializerFoo.mnl");
    }

    public void testBitset() {
        process("manul/basics/bitset/BitSet.mnl");
    }

    public void testSmallInt() {
        process("manul/smallint.mnl");
    }

    public void testBranching() {
        process("manul/basics/branching/BranchingFoo.mnl");
    }

    public void testBreak() {
        process("manul/break.mnl");
    }

    public void testContinue() {
        process("manul/basics/continue_/ContinueFoo.mnl");
    }

    public void testRange() {
        process("manul/range.mnl");
    }

    public void testAnonymousClass() {
        process("manul/anonymous_class.mnl");
    }

    public void testString() {
        process("manul/string.mnl");
    }

    public void testLocalClass() {
        process("manul/local_class.mnl");
    }

    public void testNew() {
        process("manul/new.mnl");
    }

    public void testSuperclassField() {
        process("manul/basics/anonymous_class/SuperclassFieldFoo.mnl");
    }

    public void testDDL2() {
        process("manul/bean_ddl_before.mnl");
    }

    public void testTryCatch() {
        process("manul/basics/exception/CatchUnionExceptionType.mnl");
    }

    public void testTypePtn() {
        process("manul/basics/hashcode/HashCodeFoo.mnl");
    }

    public void testBindingVars() {
        process("manul/binding_var.mnl");
    }

    public void testLowerIndexCreation() {
        process("manul/basics/index/IndexSelectFoo.mnl");
    }

    public void testMethodRef() {
        process("manul/method_ref.mnl");
    }

    public void testIs() {
        process("manul/basics/instanceof_/InstanceOfFoo.mnl");
    }

    public void testMethodCallWithinLambda() {
        process("manul/basics/lambda/MethodCallWithinLambda.mnl");
    }

    public void testPrimInit() {
        process("manul/prim_init.mnl");
    }

    public void testWidening() {
        process("manul/widening.mnl");
    }

    public void testInnerEnum() {
        process("manul/enums/inner_enum.mnl");
    }

    public void testCondExpr() {
        process("manul/condexpr/condexpr.mnl");
    }

    public void testRequire() {
        process("manul/require.mnl");
    }

    private void process(String source) {
        var file = CompilerTestUtils.parse(TestUtils.getResourcePath(source));
        var project = CompilerTestUtils.attr(file);
        file.accept(new Lower(project, new DummyLog()));
        var gen = new Gen(project, new DummyLog());
        file.accept(gen);

        var bout = new ByteArrayOutputStream();
        var writer = new ClassFileWriter(bout);
        for (ClassDecl classDeclaration : file.getClassDeclarations()) {
            var clazz = classDeclaration.getElement();
            writer.write(clazz);
        }

        var repo = new MockEntityRepository();
        repo.bind(StdKlass.exception.get());
        repo.bind(StdKlass.string.get());
        repo.bind(StdKlass.enum_.get());

        var bytes = bout.toByteArray();
        var reader = new ClassFileReader(
                new KlassInput(new ByteArrayInputStream(bytes), repo),
                repo,
                null
        );

        var numClasses = file.getClassDeclarations().size();
        for (int i = 0; i < numClasses; i++) {
            var clazz = reader.read();
            rebuildNodes(clazz);
        }

    }

    public void testEnumConstantImpl() {
        process("manul/basics/enums/EnumConstantImplFoo.mnl");
    }

    private void rebuildNodes(Klass clazz) {
        for (Method method : clazz.getMethods()) {
            method.rebuildNodes();
            for (Klass klass : method.getKlasses()) {
                rebuildNodes(klass);
            }
        }
        for (Klass klass : clazz.getKlasses()) {
            rebuildNodes(klass);
        }
    }

}
