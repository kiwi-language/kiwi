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

    public void testParentChild() {
        process("kiwi/ParentChild.kiwi");
    }

    public void testLivingBeing() {
        process("kiwi/LivingBeing.kiwi");
    }

    public void test() {
        process("kiwi/Shopping.kiwi");
    }

    public void testEnum() {
        process("kiwi/enum.kiwi");
    }

    public void testLambda() {
        process("kiwi/Lambda.kiwi");
    }

    public void testAnonClassWithArgs() {
        process("kiwi/basics/anonymous_class/AnonymousClassWithArgs.kiwi");

    }

    public void testInnerClass() {
        process("kiwi/inner_klass.kiwi");
    }

    public void testInnerClass2() {
        process("kiwi/inner_class.kiwi");
    }

    public void testGenericOverloading() {
        process("kiwi/GenericOverloading.kiwi");
    }

    public void testDDL() {
        process("kiwi/ddl_after.kiwi");
    }

    public void testEnumConversion() {
        process("kiwi/value_to_enum_ddl_after.kiwi");
    }

    public void testLab() {
        process("kiwi/lab.kiwi");
    }

    public void testAssign() {
        process("kiwi/assign.kiwi");
    }

    public void testConditional() {
        process("kiwi/conditional.kiwi");
    }

    public void testIntersectionType() {
        process("kiwi/intersection_type.kiwi");
    }

    public void testFieldInit() {
        process("kiwi/field_init.kiwi");
    }

    public void testForeach() {
        process("kiwi/foreach.kiwi");
    }

    public void testArrayInitializer() {
        process("kiwi/basics/arrayinitializer/ArrayInitializerFoo.kiwi");
    }

    public void testBitset() {
        process("kiwi/basics/bitset/BitSet.kiwi");
    }

    public void testSmallInt() {
        process("kiwi/smallint.kiwi");
    }

    public void testBranching() {
        process("kiwi/basics/branching/BranchingFoo.kiwi");
    }

    public void testBreak() {
        process("kiwi/break.kiwi");
    }

    public void testContinue() {
        process("kiwi/basics/continue_/ContinueFoo.kiwi");
    }

    public void testRange() {
        process("kiwi/range.kiwi");
    }

    public void testAnonymousClass() {
        process("kiwi/anonymous_class.kiwi");
    }

    public void testString() {
        process("kiwi/string.kiwi");
    }

    public void testLocalClass() {
        process("kiwi/local_class.kiwi");
    }

    public void testNew() {
        process("kiwi/new.kiwi");
    }

    public void testSuperclassField() {
        process("kiwi/basics/anonymous_class/SuperclassFieldFoo.kiwi");
    }

    public void testDDL2() {
        process("kiwi/bean_ddl_before.kiwi");
    }

    public void testTryCatch() {
        process("kiwi/basics/exception/CatchUnionExceptionType.kiwi");
    }

    public void testTypePtn() {
        process("kiwi/basics/hashcode/HashCodeFoo.kiwi");
    }

    public void testBindingVars() {
        process("kiwi/binding_var.kiwi");
    }

    public void testLowerIndexCreation() {
        process("kiwi/basics/index/IndexSelectFoo.kiwi");
    }

    public void testMethodRef() {
        process("kiwi/method_ref.kiwi");
    }

    public void testIs() {
        process("kiwi/basics/instanceof_/InstanceOfFoo.kiwi");
    }

    public void testMethodCallWithinLambda() {
        process("kiwi/basics/lambda/MethodCallWithinLambda.kiwi");
    }

    public void testPrimInit() {
        process("kiwi/prim_init.kiwi");
    }

    public void testWidening() {
        process("kiwi/widening.kiwi");
    }

    public void testInnerEnum() {
        process("kiwi/enums/inner_enum.kiwi");
    }

    public void testCondExpr() {
        process("kiwi/condexpr/condexpr.kiwi");
    }

    private void process(String source) {
        var file = CompilerTestUtils.parse(TestUtils.getResourcePath(source));
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

    public void testEnumConstantImpl() {
        process("kiwi/basics/enums/EnumConstantImplFoo.kiwi");
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
