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
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/Shopping.kiwi";
        process(source);
    }

    public void testEnum() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/enum.kiwi";
        process(source);
    }

    public void testLambda() {
        var source = "/Users/leen/workspace/object/test/src/test/resources/kiwi/Lambda.kiwi";
        process(source);
    }

    public void testInnerClass() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/inner_klass.kiwi");
    }

    public void testInnerClass2() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/inner_class.kiwi");
    }

    public void testGenericOverloading() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/GenericOverloading.kiwi");
    }

    public void testDDL() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/ddl_after.kiwi");
    }

    public void testEnumConversion() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/value_to_enum_ddl_after.kiwi");
    }

    public void testLab() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/lab.kiwi");
    }

    public void testAssign() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/assign.kiwi");
    }

    public void testConditional() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/conditional.kiwi");
    }

    public void testIntersectionType() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/intersection_type.kiwi");
    }

    public void testFieldInit() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/field_init.kiwi");
    }

    public void testForeach() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/foreach.kiwi");
    }

    public void testArrayInitializer() {
        process("/Users/leen/workspace/object/lab/src/main/basics/arrayinitializer/ArrayInitializerFoo.kiwi");
    }

    public void testBitset() {
        process("/Users/leen/workspace/object/lab/src/main/basics/bitset/BitSet.kiwi");
    }

    public void testSmallInt() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/smallint.kiwi");
    }

    public void testBranching() {
        process("/Users/leen/workspace/object/lab/src/main/basics/branching/BranchingFoo.kiwi");
    }

    public void testBreak() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/break.kiwi");
    }

    public void testContinue() {
        process("/Users/leen/workspace/object/lab/src/main/basics/continue_/ContinueFoo.kiwi");
    }

    public void testRange() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/range.kiwi");
    }

    public void testAnonymousClass() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/anonymous_class.kiwi");
    }

    public void testString() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/string.kiwi");
    }

    public void testLocalClass() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/local_class.kiwi");
    }

    public void testNew() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/new.kiwi");
    }

    public void testSuperclassField() {
        process("/Users/leen/workspace/object/lab/src/main/basics/anonymous_class/SuperclassFieldFoo.kiwi");
    }

    public void testDDL2() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/bean_ddl_before.kiwi");
    }

    public void testTryCatch() {
        process("/Users/leen/workspace/object/lab/src/main/basics/exception/CatchUnionExceptionType.kiwi");
    }

    public void testTypePtn() {
        process("/Users/leen/workspace/object/lab/src/main/basics/hashcode/HashCodeFoo.kiwi");
    }

    public void testBindingVars() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/binding_var.kiwi");
    }

    public void testLowerIndexCreation() {
        process("/Users/leen/workspace/object/lab/src/main/basics/index/IndexSelectFoo.kiwi");
    }

    public void testMethodRef() {
        process("/Users/leen/workspace/object/test/src/test/resources/kiwi/method_ref.kiwi");
    }

    public void testIs() {
        process("/Users/leen/workspace/object/lab/src/main/basics/instanceof_/InstanceOfFoo.kiwi");
    }

    public void testMethodCallWithinLambda() {
        process("/Users/leen/workspace/object/lab/src/main/basics/lambda/MethodCallWithinLambda.kiwi");
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
