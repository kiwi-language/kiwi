package org.metavm.wire.processor;

import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import junit.framework.TestCase;

import javax.tools.StandardLocation;

import static com.google.testing.compile.CompilationSubject.assertThat;

public class WireProcessorTest extends TestCase {

    public void test() throws Exception {
        var source = JavaFileObjects.forSourceString(
                "org.metavm.wire.mock.Foo",
                """
                package org.metavm.wire.mock;
                
                import org.metavm.wire.WireInput;
                import org.metavm.wire.Wire;
                import org.metavm.wire.SubType;
                import org.metavm.wire.Parent;
                import org.metavm.wire.WireAdapter;
                import org.metavm.wire.AdapterRegistry;
                import org.metavm.wire.WireInput;
                import org.metavm.wire.WireOutput;
                import org.metavm.wire.WireVisitor;
                import javax.annotation.Nullable;
                import java.util.List;
                import java.util.Date;
 
                @Wire(subTypes = {
                    @SubType(value = 1, type = Foo.class)
                })
                interface It {}
                
                @Wire(subTypes = {
                    @SubType(value = 1, type = Foo.class)
                })
                abstract class Base implements It {
                
                    long version = 1;
                    Baz baz;
                    
                }
                
                @Wire(3)
                record Bar(@Parent Foo parent, long value) {
                }
 
                enum FooKind {
                    FOO(1),
                    BIG_FOO(2),
                    ;
                    
                    private final int code;
                    
                    FooKind(int code) {
                        this.code = code;
                    }
 
                    public int code() {
                        return code;
                    }

                    public static FooKind fromCode(int code) {
                        for (var k : values()) {
                            if (k.code == code)
                                return k;
                        }
                        throw new IllegalArgumentException("Cannot find FooKind for code: " + code);
                    }
                    
                }
                
                @Wire(2)
                public class Foo extends Base {
                    String name;
                    int value;
                    FooKind kind;
                    Bar bar;
                    @Nullable Bar nullableBar;
                    List<Bar> bars;
                    Bar[] barArray;
                    Date createdAt;
                    boolean active;
                    byte[] bytes;
                    Baz baz;
                    transient Object temp;
                    
                    private void onRead() {
                        temp = 1;
                    }
                    
                }
 
                @Wire(adapter = BazAdapter.class)
                class Baz {}
                
                class BazAdapter implements WireAdapter<Baz> {
                                
                    @Override
                    public void init(AdapterRegistry registry) {
                    }
                                
                    @Override
                    public Baz read(WireInput input, @Nullable Object parent) {
                        return null;
                    }
                                
                    @Override
                    public void write(Baz o, WireOutput output) {
                                
                    }
                                
                    @Override
                    public void visit(WireVisitor visitor) {
                                
                    }
                                
                    @Override
                    public List<Class<? extends Baz>> getSupportedTypes() {
                        return List.of(Baz.class);
                    }
                                
                    @Override
                    public int getTag() {
                        return 4;
                    }
                }
                
                """
        );

        var c = Compiler.javac()
                .withProcessors(new WireProcessor())
                .compile(source);

        assertThat(c).succeeded();

        var content = assertThat(c).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/org.metavm.wire.WireAdapter")
                .contentsAsUtf8String();
        content.contains("org.metavm.wire.mock.Foo$__WireAdapter__");
        content.contains("org.metavm.wire.mock.Base$__WireAdapter__");
        content.contains("org.metavm.wire.mock.Bar$__WireAdapter__");
        content.contains("org.metavm.wire.mock.BazAdapter");
    }

    public void testPrimitiveWrapper() {
        compile("""
                @org.metavm.wire.Wire
                class Dummy {
                    Integer value;
                }
                """);
    }

    public void testNonAbstractSuperType() {
        compile("""
                import org.metavm.wire.Wire;
                import org.metavm.wire.SubType;

                @Wire(subTypes = {
                        @SubType(value = 1, type = Bus.class),
                        @SubType(value = 2, type = SchoolBus.class)
                })
                class Bus {
                    int capacity;
                }
                
                @Wire
                class SchoolBus extends Bus {
                    int schoolId;
                }
                """);
    }

    public void testTransient() {
        compile("""
                import org.metavm.wire.Wire;

                @Wire
                class Dummy {
                    final transient int value = 0;
                }
                """);
    }

    private void compile(String source) {
        var file = JavaFileObjects.forSourceString("Dummy", source);
        var c = Compiler.javac().withProcessors(new WireProcessor()).compile(file);
        assertThat(c).succeeded();
    }


}