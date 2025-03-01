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

    private void process(String source) {
        var file = CompilerTestUtils.parse(source);
        var project = CompilerTestUtils.attr(file);
        file.accept(new Lower(project));
        var gen = new Generator();
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
            for (Method method : clazz.getMethods()) {
                method.rebuildNodes();
            }
            log.debug("{}", clazz.getText());
        }

    }

}
