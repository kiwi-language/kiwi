package org.metavm.meta.processor;

import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import junit.framework.TestCase;
import lombok.SneakyThrows;
import org.metavm.object.instance.core.TmpId;

import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class MetaProcessorTest extends TestCase {

    @SneakyThrows
    public void test() {
        var source = """
                package org.metavm.api;
                
                import org.metavm.wire.Wire;
                import java.util.List;
                import org.metavm.api.Entity;
                import org.metavm.object.type.Klass;
                import javax.annotation.Nullable;
                
                @Wire
                @Entity(since = 1, searchable = true)
                public class Dummy<T> {
                    String name;
                    @Nullable
                    T value;
                    long version;
 
                    static Klass __klass__;
                    
                    Dummy(String name, @Nullable T value, long version) {
                        this.name = name;
                        this.value = value;
                        this.version = version;
                    }
                    
                    public @Nullable T getValue() {
                        return value;
                    }
                    
                    public void incVersion() {
                        version++;
                    }
 
                    public Dummy<T> copy() {
                        return new Dummy<>(name, value, version);
                    }
                    
                }
                """;

        var c = Compiler.javac()
                .withProcessors(createProcessor())
                .compile(JavaFileObjects.forSourceString("org.metavm.api.Dummy", source));

        assertThat(c).succeeded();

        assertThat(c).generatedSourceFile("org.metavm.api.Dummy__KlassBuilder__");
        var file = c.generatedSourceFile("org.metavm.api.Dummy__KlassBuilder__").orElseThrow();
        System.out.println(file.getCharContent(true));
        assertThat(c).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/org.metavm.entity.StdKlassBuilder")
                .contentsAsUtf8String().contains("org.metavm.api.Dummy__KlassBuilder__");
    }

    @SneakyThrows
    public void testIndexDef() {
        var source = """
                import org.metavm.entity.Entity;
                import org.metavm.object.instance.core.Id;
                import java.util.List;
                import org.metavm.entity.IndexDef;
                import org.metavm.util.Instances;
                
                @org.metavm.api.Entity
                public abstract class Dummy extends Entity {
                
                    private String name;
                
                    public static final IndexDef<Dummy> nameIdx = IndexDef.createUnique(Dummy.class, 1, d -> List.of(Instances.stringInstance(d.name)));
                    
                    public Dummy(Id id) {
                        super(id);
                    }
                    
                }
                """;

        var c = Compiler.javac()
                .withProcessors(createProcessor())
                .compile(JavaFileObjects.forSourceString("Dummy", source));

        assertThat(c).succeeded();

        assertThat(c).generatedSourceFile("Dummy__KlassBuilder__")
                .contentsAsUtf8String().contains("IndexDef");
        var file = c.generatedSourceFile("Dummy__KlassBuilder__").orElseThrow();
        System.out.println(file.getCharContent(true));
        assertThat(c).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/org.metavm.entity.StdKlassBuilder")
                .contentsAsUtf8String().contains("Dummy__KlassBuilder__");
    }

    @SneakyThrows
    public void testEntityFlowAnnotation() {
        var source = """
                import org.metavm.api.EntityFlow;
                import org.metavm.api.Entity;
                
                @Entity
                public class Dummy {
                    String name;
                    long version;
                    
                    
                    Dummy(String name, long version) {
                        this.name = name;
                        this.version = version;
                    }
                    
                    @EntityFlow
                    void incVersion() {
                        version++;
                    }
                    
                }
                """;

        var c = Compiler.javac()
                .withProcessors(createProcessor())
                .compile(JavaFileObjects.forSourceString("Dummy", source));

        assertThat(c).succeeded();

        assertThat(c).generatedSourceFile("Dummy__KlassBuilder__").contentsAsUtf8String().contains("incVersion");
        var file = c.generatedSourceFile("Dummy__KlassBuilder__").orElseThrow();
        System.out.println(file.getCharContent(true));
        assertThat(c).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/org.metavm.entity.StdKlassBuilder")
                .contentsAsUtf8String().contains("Dummy__KlassBuilder__");
    }

    private MetaProcessor createProcessor() {
        var processor = new MetaProcessor();
        processor.setIdStoresCreator(env -> new MockIdStores());
        return processor;
    }

    private static class MockIdStores implements IdStores {

        private int nextTypeTag;

        @Override
        public String getId(String type, String name) {
            return TmpId.randomString();
        }

        @Override
        public int getTypeTag(String className) {
            return nextTypeTag++;
        }
    }

}