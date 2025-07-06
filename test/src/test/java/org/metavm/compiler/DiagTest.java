package org.metavm.compiler;

import junit.framework.TestCase;
import org.metavm.compiler.analyze.*;
import org.metavm.compiler.diag.DefaultLog;
import org.metavm.compiler.diag.Diag;
import org.metavm.compiler.diag.DiagFactory;
import org.metavm.compiler.element.Project;
import org.metavm.compiler.file.DummySourceFile;
import org.metavm.compiler.syntax.Lexer;
import org.metavm.compiler.syntax.Parser;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.MockEnter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;

public class DiagTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(DiagTest.class);

    private DefaultLog log;

    @Override
    protected void setUp() throws Exception {
        log = new DefaultLog(new DummySourceFile(""),
                DiagFactory.instance,
                new PrintWriter(System.out),
                new PrintWriter(System.err)
        );
    }

    public void testConstructorNotFound() {
        var diags = compile("""
                enum Option {
                
                    op1(1, "a")
                    
                }
                """);
        assertEquals(1, diags.size());
        assertEquals("""
                dummy.kiwi:3: Cannot find constructor in enum Option with given argument types: int, string
                        op1(1, "a")
                        ^""", diags.head().toString());
    }

    public void testMethodNotFound() {
        var diags = compile("""
                class Foo {
                
                    fn test() {
                        bar()
                    }
                    
                }
                """);
        assertEquals(1, diags.size());
        assertEquals("""
                           dummy.kiwi:4: Symbol not found
                                       bar()
                                       ^""",
                diags.head().toString()
        );
    }

    public void testCantResolveType() {
        var diags = compile("""
                class Foo: Base {
                }
                """);
        assertEquals(1, diags.size());
        assertEquals("""
                dummy.kiwi:1: Symbol not found
                    class Foo: Base {
                               ^""",
                diags.head().toString()
        );
    }

    public void testTypeExpected() {
        var diags = compile("""
                package org.kiwi
                
                class Foo: org.kiwi {}
                """);
        assertEquals(1, diags.size());
        assertEquals(
                """
                        dummy.kiwi:3: type expected
                            class Foo: org.kiwi {}
                                           ^""",
                diags.head().toString()
        );
    }

    public void testDuplicateBindingName() {
        var diags = compile("""
                class Foo {
                
                    fn test(a: any, b: any) {
                        if (a is string s && b is string s) {
                            print(s)
                        }
                    }
                
                }
                """);
        assertEquals(1, diags.size());
    }

    public void testEqInplaceOrAssign() {
        var diags = compile("""
                class Foo {
                    
                    fn test() {
                        var a == 1
                    }
                
                }
                """);
        assertFalse(diags.isEmpty());
    }

    public void testChineseCharacter() {
        var diags = compile("""
                @Label("商品")1
                class Product {
                }
                """);
        assertEquals(1, diags.size());
        assertEquals("""
                dummy.kiwi:1: Unexpected token: 1
                    @Label("商品")1
                            　　  ^""",
                diags.head().toString()
        );
    }

    public void testSymbolNotFoundInImport() {
        var diags = compile("""
                import org.imp
                """);
        assertEquals(1, diags.size());
        assertEquals("""
                dummy.kiwi:1: Symbol not found
                    import org.imp
                               ^""",
                diags.head().toString());
    }

    public void testModifyCapturedVariable() {
        compile("""
                class Lab {
                    
                    fn sum(values: int[]) -> int {
                        var sum = 0
                        values.forEach(i -> sum += i)
                        return sum
                    }
                
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals(
                """
                        dummy.kiwi:5: Cannot modify a captured variable
                                    values.forEach(i -> sum += i)
                                                        ^""",
                log.getDiags().head().toString()
        );
    }

    public void testDeletedInitParam() {
        compile("""
                class Foo(
                    deleted var name: string
                )
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals(
                """
                        dummy.kiwi:2: Modifier 'deleted' is not allowed here
                                deleted var name: string
                                ^""",
                log.getDiags().head().toString()
        );
    }

    public void testMisplacedIndexField() {
        compile("""
                class Order {
                
                    static val itemQuantityIdx = Index<int, Item>(false, i -> i.quantity)
                
                    class Item(val quantity: int)
                
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals(
                """
                        dummy.kiwi:3: Index field must be defined in the class it indexes
                                static val itemQuantityIdx = Index<int, Item>(false, i -> i.quantity)
                                           ^""",
                log.getDiags().head().toString()
        );
    }

    public void testInvalidIndexValueType() {
        compile("""
                class Order {
                
                    static val intIdx = Index<int, int>(false, i -> i)
                
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals(
                """
                        dummy.kiwi:3: Invalid index value type
                                static val intIdx = Index<int, int>(false, i -> i)
                                           ^""",
                log.getDiags().head().toString()
        );
    }

    public void testNonStaticIndexField() {
        compile("""
                class Product(var name: string) {
                
                    val nameIdx = Index<string, Product>(true, p -> p.name)
                
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals(
                """
                        dummy.kiwi:3: Index field must be static
                                val nameIdx = Index<string, Product>(true, p -> p.name)
                                    ^""",
                log.getDiags().head().toString()
        );
    }

    public void testReservedFieldNames() {
        compile("""
                class Foo(val id: string, val parent: any) {
                    val children = new any[]
                }
                """);
        assertEquals(3, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:2: Reserved field name
                        val children = new any[]
                            ^""",
                log.getDiags().head().toString());
        assertEquals(
                """
                        dummy.kiwi:1: Reserved field name
                            class Foo(val id: string, val parent: any) {
                                                          ^""",
                log.getDiags().tail().head().toString()
        );
        assertEquals(
                """
                        dummy.kiwi:1: Reserved field name
                            class Foo(val id: string, val parent: any) {
                                          ^""",
                log.getDiags().tail().tail().head().toString()
        );
    }

    private List<Diag> compile(String text) {
        log.setSourceFile(new DummySourceFile(text));
        var parser = new Parser(
                log,
                new Lexer(log, text.toCharArray(), text.length())
        );
        var file = parser.file();

        var project = new Project();
        MockEnter.enterStandard(project);
        new Enter(project, log).enter(List.of(file));
        file.accept(new Meta());
        ImportResolver.resolve(file, project, log);
        file.accept(new TypeResolver(project, log));
        file.accept(new IdentAttr(project, log));
        file.accept(new Attr(project, log));
        file.accept(new Check(project, log));
        return log.getDiags();
    }

}
