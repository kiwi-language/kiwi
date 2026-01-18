package org.manul.context;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import junit.framework.TestCase;
import lombok.SneakyThrows;

import javax.tools.StandardLocation;
import java.io.IOException;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

public class ContextProcessorTest extends TestCase {

    @SneakyThrows
    public void test() {
        var source = """
                import org.manul.context.Component;
                
                @Component
                public class Dummy {
                }
                """;

        var c = compile(source);
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
        assertThat(c).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/org.manul.context.BeanDefinition")
                .contentsAsUtf8String()
                .contains("Dummy__BeanDef__");
    }

    public void testTransactional() throws IOException {
        var source = """
                import org.manul.context.Component;
                import org.manul.context.sql.Transactional;
                import org.manul.jdbc.TransactionOperations;
                import org.manul.context.sql.TransactionPropagation;
                import org.manul.context.sql.TransactionIsolation;
                import java.util.function.Supplier;
 
                @Component
                class TransactionTemplate implements TransactionOperations {

                    @Override
                    public <T> T execute(Supplier<T> action, boolean readonly, TransactionPropagation propagation) {
                        return action.get();
                    }

                    @Override
                    public void setIsolationLevel(TransactionIsolation level) {}

                }

                @Component
                public class Dummy {
                
                    @Transactional
                    public void save() {
                    }
                
                }
                """;

        var c = compile(source);
        assertThat(c).succeeded();
        var proxyFile = c.generatedSourceFile("Dummy__Proxy__").orElseThrow();
        System.out.println(proxyFile.getCharContent(true));
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
        assertThat(c).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/org.manul.context.BeanDefinition")
                .contentsAsUtf8String()
                .contains("Dummy__BeanDef__");
    }

    public void testScheduled() throws IOException {
        var source = """
                import org.manul.context.Component;
                import org.manul.context.Scheduled;
                import org.manul.schedule.Scheduler;
                
                @Component
                class MyScheduler implements Scheduler {
                    
                    @Override
                    public void schedule(Runnable run, int delay) {
                        run.run();
                    }
                
                }
                
                @Component
                public class Dummy {
                
                    @Scheduled(fixedDelay=1000)
                    public void schedule() {
                    }
                
                }
                """;

        var c = compile(source);
        var proxyFile = c.generatedSourceFile("Dummy__Proxy__").orElseThrow();
        System.out.println(proxyFile.getCharContent(true));
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
        assertThat(c).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/org.manul.context.BeanDefinition")
                .contentsAsUtf8String()
                .contains("Dummy__BeanDef__");
    }

    public void testController() throws IOException {
        var source = """
                import org.manul.context.http.Controller;
                import org.manul.context.Component;
                import org.manul.context.http.Get;
                import org.manul.context.http.Mapping;
                
                @Controller
                @Mapping("/")
                public class Dummy {
                
                    @Get("/ping")
                    public String ping() {
                        return "pang";
                    }
                
                }
                
                @Component
                class Foo {
                    
                    Foo(org.manul.server.Controller controller) {}
                    
                }
                """;

        var c = compile(source);
        assertThat(c).succeeded();
        var proxyFile = c.generatedSourceFile("Dummy__Proxy__").orElseThrow();
        System.out.println(proxyFile.getCharContent(true));
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));

        var fooDefFile = c.generatedSourceFile("Foo__BeanDef__").orElseThrow();
        System.out.println(fooDefFile.getCharContent(true));

        assertThat(c).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/org.manul.context.BeanDefinition")
                .contentsAsUtf8String()
                .contains("Dummy__BeanDef__");
    }



    public void testValue() throws IOException {
        var source = """
                import org.manul.context.Component;
                import org.manul.context.Value;
                
                @Component
                public class Dummy {
                
                    public Dummy(@Value("${manul.api.verify}") boolean verify) {
                    }
                
                }
                """;

        var c = compile(source);
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
        assertThat(c).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/org.manul.context.BeanDefinition")
                .contentsAsUtf8String()
                .contains("Dummy__BeanDef__");
    }

    public void testConfiguration() throws IOException {
        var source = """
                import org.manul.context.Configuration;
                import org.manul.context.Bean;
                
                @Configuration
                public class Dummy {
                
                    @Bean
                    public Foo foo1() {
                        return new Foo();
                    }
 
                    @Bean
                    public Foo foo2() {
                        return new Foo();
                    }
                
                }
                
                class Foo {}
                """;

        var c = compile(source);
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
        assertThat(c).generatedFile(StandardLocation.CLASS_OUTPUT, "META-INF/services/org.manul.context.BeanDefinition")
                .contentsAsUtf8String()
                .contains("Dummy__BeanDef__");
    }

    public void testDependency() throws IOException {
        var source = """
                import org.manul.context.Component;
                import org.manul.context.Qualifier;
                
                @Component
                public class Dummy {
                
                    public Dummy(Foo foo) {
                    }
                
                }
                
                @Component
                class Foo {
                }
                """;

        var c = compile(source);
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
    }

    public void testListDependency() throws IOException {
        var source = """
                import org.manul.context.Component;
                import java.util.List;
                
                @Component
                public class Dummy {
                
                    public Dummy(List<Speak> speaks) {
                    }
                
                }
                
                interface Speak {
                }
                
                @Component
                class Foo implements Speak {
                }

                @Component
                class Bar implements Speak {
                }
                """;

        var c = compile(source);
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
    }

    public void testQualifier() throws IOException {
        var source = """
                import org.manul.context.Component;
                import org.manul.context.Qualifier;
                
                @Component
                public class Dummy {
                
                    public Dummy(@Qualifier("foo") Foo foo) {
                    }
                
                }
                
                @Component
                class Foo {
                }
                """;

        var c = compile(source);
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
    }

    public void testPrimary() throws IOException {
        var source = """
                import org.manul.context.Component;
                import org.manul.context.Primary;
                
                @Component
                @Primary
                public class Dummy {
                }
                """;

        var c = compile(source);
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
    }

    @SneakyThrows
    public void testSetterAutowire() {
        var source = """
                import org.manul.context.Component;
                import org.manul.context.Autowired;
                
                @Component
                public class Dummy {
                
                    @Autowired
                    void setFoo(Foo foo) {}
                
                }
                
                @Component
                class Foo {}
                """;

        var c = compile(source);
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
    }

    @SneakyThrows
    public void testInit() {
        var source = """
                import org.manul.context.Component;
                import org.manul.context.Init;
                
                @Component
                public class Dummy {
                
                    @Init
                    public void init() {}
                
                }
                """;

        var c = compile(source);
        var file = c.generatedSourceFile("Dummy__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
    }

    @SneakyThrows
    public void testModule() {
        var source = """
                import org.manul.context.Component;
                
                @Component(module = "default")
                class Foo {
                
                    Foo(Bar bar) {}
                
                }
                
                interface Bar {}
                
                @Component(module = "default")
                class Bar1 implements Bar {}
                
                @Component(module = "bar2")
                class Bar2 implements Bar {}
                """;
        var c = compile(source);
        var file = c.generatedSourceFile("Foo__BeanDef__").orElseThrow();
        System.out.println(file.getCharContent(true));
    }

    private Compilation compile(String source) {
        var c = javac().withProcessors(new ContextProcessor())
                .compile(JavaFileObjects.forSourceString("Dummy", source));
        assertThat(c).succeeded();
        return c;
    }

}