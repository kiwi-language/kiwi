package org.metavm.compiler;

import junit.framework.TestCase;
import org.metavm.compiler.analyze.*;
import org.metavm.compiler.diag.DefaultLog;
import org.metavm.compiler.diag.Diag;
import org.metavm.compiler.diag.DiagFactory;
import org.metavm.compiler.element.Project;
import org.metavm.compiler.file.DummySourceFile;
import org.metavm.compiler.syntax.File;
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
                dummy.kiwi:3: Cannot resolve function with given argument types: int, string
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

    public void testTuples() {
        compile("""
                class Product(var name: string, var price: double) {
                
                    fn func() -> (Lab) -> any {
                         return l -> (l.name, l.price)
                    }
                
                }
                """);
        log.flush();
    }

    public void testSelfRefVariableInit() {
        compile("""
                class Lab {
                    var a = a
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:2: Cannot resolve symbol
                        var a = a
                                ^""", log.getDiags().getFirst().toString());
    }

    public void testIllegalCast() {
        compile("""
                class Product(var name: string) {
                
                    class SKU(var variant: string, var stock: int)
                    
                    fn getSkus() -> SKU[] {
                        return children as SKU[]
                    }
                
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:6: Illegal cast from any[] to Product.SKU[]
                            return children as SKU[]
                                            ^""",
                log.getDiags().getFirst().toString());
    }

    public void testFieldNotInitialized() {
        compile("""
                class User(var name: string) {
                    var passwordHash: string
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:2: Field not initialized
                        var passwordHash: string
                            ^""",
                log.getDiags().getFirst().toString()
        );
    }

    public void testIllegalUseOfType() {
        compile("""
                class Lab {
                    
                    fn getClass() -> any {
                        return Lab
                    }
                    
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:4: Illegal use of type
                            return Lab
                                   ^""",
                log.getDiags().getFirst().toString()
                );
    }

    public void testLtOperandTypeCheck() {
        compile("""
                class Lab {
                
                    fn le(s1: string, s2: string) -> bool {
                        return s1 < s2
                    }
                
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:4: Operator '<' cannot be applied to 'string', 'string'
                            return s1 < s2
                                   ^""",
                log.getDiags().getFirst().toString());
    }

    public void testAddOperandTypeCheck() {
        compile("""
                class Lab {
                
                    fn add(s1: any, s2: any) -> any {
                        return s1 + s2
                    }
                
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:4: Operator '+' cannot be applied to 'any', 'any'
                            return s1 + s2
                                   ^""",
                log.getDiags().getFirst().toString());
    }

    public void testPrefixOpTypeCheck() {
        compile("""
                class Lab {
                
                    fn inc(s: string) -> string {
                        return !s
                    }
                
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:4: Operator '!' cannot be applied to 'string'
                            return !s
                                    ^""",
                log.getDiags().getFirst().toString());
    }

    public void testPostfixOpTypeCheck() {
        compile("""
                class Lab {
                
                    fn inc(s: string) -> string {
                        return s++
                    }
                
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:4: Operator '++' cannot be applied to 'string'
                            return s++
                                   ^""",
                log.getDiags().getFirst().toString());
    }

    public void testIncompatibleTypes() {
        compile("""
                class Lab {
                
                    fn test() -> int {
                        return set()
                    }
                    
                    fn set() {}
                    
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:4: Incompatible types: void cannot be converted to int
                            return set()
                                   ^""", log.getDiags().getFirst().toString());
    }

    public void testVoidInitializer() {
        compile("""
                class Lab {
                
                    fn test() {
                        var a = set()
                    }
                    
                    fn set() {}
                    
                }
                """);
        log.flush();
    }

    public void testAiLint() {
        compileWithAiLint("""
                class Lab(
                    var lab: string,
                    var labId: string,
                    var labs: string
                ) {
                }
                """);
        assertEquals(3, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:4: Variable name matches the plural form of a class name but its type is not the array type of the class, such confusing naming is not allowed
                        var labs: string
                            ^""",
                log.getDiags().getFirst().toString());
        assertEquals("""
                dummy.kiwi:3: Variable name ending with 'Id' is not allowed
                        var labId: string,
                            ^""",
                log.getDiags().get(1).toString());
        assertEquals("""
                dummy.kiwi:2: Variable name matches a class name but its type is not the class, such confusing naming is not allowed
                        var lab: string,
                            ^""",
                log.getDiags().getLast().toString());
    }

    public void testIncompatibleLambdaBodyExpr() {
        compile("""
                class Lab(var tags: int[]) {
                    
                    static val tagIdx = Index<int, Lab>(false, l -> l.tags)
                    
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:3: Incompatible types: int[] cannot be converted to int
                        static val tagIdx = Index<int, Lab>(false, l -> l.tags)
                                                                          ^""",
                log.getDiags().getFirst().toString());
    }

    public void testDupVariableDef() {
        compile("""
                class Lab {
                    var name: string?
                    var name: string?
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:3: Variable 'name' is already defined in the scope
                        var name: string?
                            ^""",  log.getDiags().getFirst().toString());
    }

    public void testDupLocalVar() {
        compile("""
                class Lab {
                    
                    fn test() {
                        {
                            var a = 1
                        }
                        var a = 1
                        {
                            var a = 1
                        }
                    }
                    
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:9: Variable 'a' is already defined in the scope
                                var a = 1
                                    ^""", log.getDiags().getFirst().toString());
    }

    public void testDupMethod() {
        compile("""
                class Lab {
                    
                    fn test(s: string) {}
                    
                    fn test(s: string) {}
                
                }
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:3: Function 'test(Class java.lang.String)' is already defined in the scope
                        fn test(s: string) {}
                           ^""", log.getDiags().getFirst().toString());
    }

    public void testCyclicInheritance() {
        compile("""
                class Lab: Lab {}
                """);
        assertEquals(1, log.getDiags().size());
        assertEquals("""
                dummy.kiwi:1: Cyclic inheritance
                    class Lab: Lab {}
                               ^""", log.getDiags().getFirst().toString());
    }

    private List<Diag> compileWithAiLint(String text) {
        var file = parse(text);
        compile(file);
        SenseLint.run(List.of(file), log);
        return log.getDiags();
    }

    private List<Diag> compile(String text) {
        compile(parse(text));
        return log.getDiags();
    }

    private File parse(String text) {
        log.setSourceFile(new DummySourceFile(text));
        var parser = new Parser(
                log,
                new Lexer(log, text.toCharArray(), text.length())
        );
        return parser.file();
    }

    private void compile(File file) {
        var project = new Project();
        MockEnter.enterStandard(project);
        new Enter(project, log).enter(List.of(file));
        file.accept(new Meta());
        ImportResolver.resolve(file, project, log);
        file.accept(new TypeResolver(project, log));
        file.accept(new IdentAttr(project, log));
        file.accept(new Attr(project, log));
        file.accept(new Check(project, log));
    }

}
