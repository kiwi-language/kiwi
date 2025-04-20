package org.metavm.compiler.generate;

import junit.framework.TestCase;
import lombok.extern.slf4j.Slf4j;
import org.metavm.classfile.ClassFileReader;
import org.metavm.compiler.CompilerTestUtils;
import org.metavm.compiler.analyze.Lower;
import org.metavm.compiler.syntax.ClassDecl;
import org.metavm.entity.MockStandardTypesInitializer;
import org.metavm.entity.StdKlass;
import org.metavm.entity.mocks.MockEntityRepository;
import org.metavm.flow.KlassInput;
import org.metavm.flow.Method;
import org.metavm.object.type.Klass;
import org.metavm.util.TestUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

@Slf4j
public class ClassFileWriterTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        TestUtils.ensureStringKlassInitialized();
        MockStandardTypesInitializer.init();
    }

    public void test() {
        var source = TestUtils.getResourcePath("kiwi/Shopping.kiwi");
        process(source);
    }

    public void testEnum() {
        var source = TestUtils.getResourcePath("kiwi/enum.kiwi");
        process(source);
    }

    public void testLambda() {
        var source = TestUtils.getResourcePath("kiwi/Lambda.kiwi");
        process(source);
    }

    public void testInnerClass() {
        process(TestUtils.getResourcePath("kiwi/inner_klass.kiwi"));
    }

    public void testInnerClass2() {
        process(TestUtils.getResourcePath("kiwi/inner_class.kiwi"));
    }

    public void testGenericOverloading() {
        process(TestUtils.getResourcePath("kiwi/GenericOverloading.kiwi"));
    }

    public void testDDL() {
        process(TestUtils.getResourcePath("kiwi/ddl_after.kiwi"));
    }

    public void testEnumConversion() {
        process(TestUtils.getResourcePath("kiwi/value_to_enum_ddl_after.kiwi"));
    }

    public void testLab() {
        process(TestUtils.getResourcePath("kiwi/lab.kiwi"));
    }

    public void testAssign() {
        process(TestUtils.getResourcePath("kiwi/assign.kiwi"));
    }

    public void testConditional() {
        process(TestUtils.getResourcePath("kiwi/conditional.kiwi"));
    }

    public void testIntersectionType() {
        process(TestUtils.getResourcePath("kiwi/intersection_type.kiwi"));
    }

    public void testFieldInit() {
        process(TestUtils.getResourcePath("kiwi/field_init.kiwi"));
    }

    public void testForeach() {
        process(TestUtils.getResourcePath("kiwi/foreach.kiwi"));
    }

    public void testArrayInitializer() {
        process(TestUtils.getResourcePath("kiwi/basics/arrayinitializer/ArrayInitializerFoo.kiwi"));
    }

    public void testBitset() {
        process(TestUtils.getResourcePath("kiwi/basics/bitset/BitSet.kiwi"));
    }

    public void testSmallInt() {
        process(TestUtils.getResourcePath("kiwi/smallint.kiwi"));
    }

    public void testBranching() {
        process(TestUtils.getResourcePath("kiwi/basics/branching/BranchingFoo.kiwi"));
    }

    public void testBreak() {
        process(TestUtils.getResourcePath("kiwi/break.kiwi"));
    }

    public void testContinue() {
        process(TestUtils.getResourcePath("kiwi/basics/continue_/ContinueFoo.kiwi"));
    }

    public void testRange() {
        process(TestUtils.getResourcePath("kiwi/range.kiwi"));
    }

    public void testAnonymousClass() {
        process(TestUtils.getResourcePath("kiwi/anonymous_class.kiwi"));
    }

    public void testString() {
        process(TestUtils.getResourcePath("kiwi/string.kiwi"));
    }

    public void testLocalClass() {
        process(TestUtils.getResourcePath("kiwi/local_class.kiwi"));
    }

    public void testNew() {
        process(TestUtils.getResourcePath("kiwi/new.kiwi"));
    }

    public void testSuperclassField() {
        process(TestUtils.getResourcePath("kiwi/basics/anonymous_class/SuperclassFieldFoo.kiwi"));
    }

    public void testDDL2() {
        process(TestUtils.getResourcePath("kiwi/bean_ddl_before.kiwi"));
    }

    public void testTryCatch() {
        process(TestUtils.getResourcePath("kiwi/basics/exception/CatchUnionExceptionType.kiwi"));
    }

    public void testTypePtn() {
        process(TestUtils.getResourcePath("kiwi/basics/hashcode/HashCodeFoo.kiwi"));
    }

    public void testBindingVars() {
        process(TestUtils.getResourcePath("kiwi/binding_var.kiwi"));
    }

    public void testLowerIndexCreation() {
        process(TestUtils.getResourcePath("kiwi/basics/index/IndexSelectFoo.kiwi"));
    }

    public void testMethodRef() {
        process(TestUtils.getResourcePath("kiwi/method_ref.kiwi"));
    }

    public void testIs() {
        process(TestUtils.getResourcePath("kiwi/basics/instanceof_/InstanceOfFoo.kiwi"));
    }

    public void testMethodCallWithinLambda() {
        process(TestUtils.getResourcePath("kiwi/basics/lambda/MethodCallWithinLambda.kiwi"));
    }

    private void process(String source) {
        var file = CompilerTestUtils.parse(source);
        var project = CompilerTestUtils.attr(file);
        file.accept(new Lower(project));
        var gen = new Gen(project);
        file.accept(gen);

        var bout = new ByteArrayOutputStream();
        var writer = new ClassFileWriter(bout);
        for (ClassDecl classDeclaration : file.getClassDeclarations()) {
            var clazz = classDeclaration.getElement();
            writer.write(clazz);
        }

        var repo = new MockEntityRepository();
        repo.bind(StdKlass.runtimeException.get());
        repo.bind(StdKlass.string.get());
        repo.bind(StdKlass.list.get());
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
            log.debug("{}", clazz.getText());
        }

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
