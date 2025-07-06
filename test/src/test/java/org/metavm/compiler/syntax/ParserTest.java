package org.metavm.compiler.syntax;

import junit.framework.TestCase;
import org.metavm.compiler.diag.DefaultLog;
import org.metavm.compiler.diag.DiagFactory;
import org.metavm.compiler.diag.DiagSource;
import org.metavm.compiler.diag.Log;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.NameTable;
import org.metavm.compiler.file.DummySourceFile;
import org.metavm.compiler.file.FileManager;
import org.metavm.compiler.file.PathSourceFile;
import org.metavm.compiler.syntax.Parser.LtResult;
import org.metavm.compiler.util.List;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static java.util.Objects.requireNonNull;
import static org.metavm.compiler.syntax.Parser.ParensResult.LAMBDA;
import static org.metavm.compiler.syntax.Parser.ParensResult.PARENS;

public class ParserTest extends TestCase {

    private DefaultLog log;

    @Override
    protected void setUp() throws Exception {
        log = new DefaultLog(new DummySourceFile(""), DiagFactory.instance,
                new PrintWriter(System.out), new PrintWriter(System.err));
        log.setDelayed(false);
    }

    public void test() {
        var text = """
                class Product(
                    var name: string,
                    var price: double,
                    var stock: int
                ) {
                    
                    fn reduceStock(quantity: int) {
                        require(stock >= quantity, "Out of stock")
                        stock -= quantity
                    }
                    
                }
                """;
        var file = file(text);
        var expectedFile = new File(
                null,
                List.nil(),
                List.of(
                        ClassDeclBuilder.builder(Name.from("Product"))
                                .params(List.of(
                                        new ClassParamDecl(
                                                List.of(),
                                                List.of(),
                                                true,
                                                false,
                                                new PrimitiveTypeNode(TypeTag.STRING),
                                                Name.from("name")
                                        ),
                                        new ClassParamDecl(
                                                List.of(),
                                                List.of(),
                                                true,
                                                false,
                                                new PrimitiveTypeNode(TypeTag.DOUBLE),
                                                Name.from("price")
                                        ),
                                        new ClassParamDecl(
                                                List.of(),
                                                List.of(),
                                                true,
                                                false,
                                                new PrimitiveTypeNode(TypeTag.INT),
                                                Name.from("stock")
                                        )
                                ))
                                .members(List.of(
                                        new MethodDecl(
                                                List.of(),
                                                List.of(),
                                                List.of(),
                                                Name.from("reduceStock"),
                                                List.of(
                                                        new ParamDecl(
                                                                List.nil(),
                                                                new PrimitiveTypeNode(TypeTag.INT),
                                                                Name.from("quantity")
                                                        )
                                                ),
                                                null,
                                                new Block(
                                                        List.of(
                                                                new ExprStmt(
                                                                        new Call(
                                                                                new Ident(Name.from("require")),
                                                                                List.of(
                                                                                        new BinaryExpr(
                                                                                                BinOp.GE,
                                                                                                new Ident(Name.from("stock")),
                                                                                                new Ident(Name.from("quantity"))
                                                                                        ),
                                                                                        new Literal("Out of stock")
                                                                                )
                                                                        )
                                                                ),
                                                                new ExprStmt(
                                                                        new AssignExpr(
                                                                                BinOp.SUB,
                                                                                new Ident(Name.from("stock")),
                                                                                new Ident(Name.from("quantity"))
                                                                        )
                                                                )
                                                        )
                                                )
                                        )
                                ))
                                .build()
                )
        );
        assertEquals(expectedFile, file);
    }

    public void testNestedClass() {
        var file = file("""
                class Order {
            
                    class Item {}
                    
                }
                """);
        var expectedFile = new File(
                null,
                List.of(),
                List.of(
                        ClassDeclBuilder.builder(Name.from("Order"))
                                .addMember(
                                        ClassDeclBuilder.builder(Name.from("Item"))
                                        .build()
                                )
                                .build()
                )
        );
        assertEquals(expectedFile, file);
    }

    public void testAnnotatedClass() {
        assertEquals(
                new File(null, List.of(), List.of(
                        ClassDeclBuilder.builder(Name.from("Foo"))
                                .addAnnotation(new Annotation(
                                        Name.from("Tag"),
                                        List.of(new Annotation.Attribute(Name.from("value"), new Literal(1)))
                                ))
                                .build()
                )),
                file("""
                        @Tag(1)
                        class Foo {}
                        """)
        );
    }

    public void testEnum() {
        var expected = ClassDeclBuilder.builder(Name.from("Currency"))
                .tag(ClassTag.ENUM)
                .enumConsts(List.of(
                        new EnumConstDecl(List.of(), Name.from("USD"), List.of() ,null),
                        new EnumConstDecl(List.of(), Name.from("CNY"), List.of() ,null),
                        new EnumConstDecl(List.of(), Name.from("EURO"), List.of() ,null)
                ))
                .build();

        assertEquals(
                expected,
                enumDecl("""
                       enum Currency {
                            USD,
                            CNY,
                            EURO
                        }
                        """)
        );

        assertEquals(
                expected,
                enumDecl("""
                       enum Currency {
                            USD,
                            CNY,
                            EURO,
                        }
                        """)
        );

        assertEquals(
                expected,
                enumDecl("""
                       enum Currency {
                            USD,
                            CNY,
                            EURO
                            ;
                        }
                        """)
        );

        assertEquals(
                expected,
                enumDecl("""
                       enum Currency {
                            USD,
                            CNY,
                            EURO,
                            ;
                        }
                        """)
        );
    }

    public void testEnumConstantAnonClass() {
        assertEquals(
                ClassDeclBuilder.builder(Name.from("Option"))
                        .tag(ClassTag.ENUM)
                        .enumConsts(List.of(
                                new EnumConstDecl(
                                        List.of(),
                                        Name.from("op1"),
                                        List.of(),
                                        ClassDeclBuilder.builder(NameTable.instance.empty)
                                                .addExtend(new Extend(new Call(Ident.from("Option"), List.of())))
                                                .build()
                                )
                        ))
                        .build(),
                enumDecl("""
                        enum Option {
                        
                            op1() {}
                            
                        }
                        """)
        );
    }

    public void testInheritance() {
        assertEquals(
                new ClassDecl(
                        ClassTag.CLASS, List.of(), List.of(), Name.from("Derived"), null,
                        List.of(
                                new Extend(new Call(Ident.from("Base"), List.of(new Literal(1)))),
                                new Extend(Ident.from("Speak"))
                        ),
                        List.of(), List.of(), List.of(), List.of()
                ),
                classDecl("""
                        class Derived: Base(1), Speak {}
                        """)
        );
    }

    public void testClassParam() {
        assertEquals(
                ClassDeclBuilder.builder(Name.from("Foo"))
                        .params(List.of(
                                new ClassParamDecl(
                                        List.of(new Modifier(ModifierTag.PRIV)),
                                        List.of(),
                                        true,
                                        true,
                                        new PrimitiveTypeNode(TypeTag.STRING),
                                        Name.from("name")
                                ),
                                new ClassParamDecl(
                                        List.of(),
                                        List.of(),
                                        false,
                                        false,
                                        new PrimitiveTypeNode(TypeTag.INT),
                                        Name.from("value")
                                )
                        ))
                        .build(),
                classDecl("class Foo(priv val name: string, value: int)")
        );
    }

    private ClassDecl enumDecl(String text) {
        var parser = parser(text);
        var classDecl = parser.enumDecl(List.of(), List.of());
        assertSame(TokenKind.EOF, parser.token().getKind());
        return classDecl;
    }

    private ClassDecl classDecl(String text) {
        var parser = parser(text);
        var classDecl = parser.classDecl(List.of(), List.of());
        assertSame(TokenKind.EOF, parser.token().getKind());
        return classDecl;
    }

    public void testExplicitLambda() {
        var file = expr("(a: any) -> a");
        var expected = new LambdaExpr(
                List.of(
                        new ParamDecl(
                                List.of(),
                                new PrimitiveTypeNode(TypeTag.ANY),
                                Name.from("a")
                        )
                ),
                null,
                new Ident(Name.from("a"))
        );
        assertEquals(expected, file);
    }

    public void testImplicitLambda() {
        var file = expr("(a) -> a");
        var expected = new LambdaExpr(
                List.of(
                        new ParamDecl(
                                List.of(),
                                null,
                                Name.from("a")
                        )
                ),
                null,
                new Ident(Name.from("a"))
        );
        assertEquals(expected, file);
    }

    public void testSimplestLambda() {
        var file = expr("a -> a");
        var expected = new LambdaExpr(
                List.of(
                        new ParamDecl(
                                List.of(),
                                null,
                                Name.from("a")
                        )
                ),
                null,
                new Ident(Name.from("a"))
        );
        assertEquals(expected, file);
    }

    public void testOptionalType() {
        var type = type("any?");
        var expected = new UnionTypeNode(
                List.of(
                        new PrimitiveTypeNode(TypeTag.ANY),
                        new PrimitiveTypeNode(TypeTag.NULL)
                )
        );
        assertEquals(expected, type);
    }

    public void testArrayType() {
        var type = type("any[]");
        var expected = new ArrayTypeNode(new PrimitiveTypeNode(TypeTag.ANY));
        assertEquals(expected, type);
    }

    public void testFuncType() {
        assertEquals(
                new FunctionTypeNode(
                        List.of(),
                        new PrimitiveTypeNode(TypeTag.VOID)
                ),
                type("() -> void")
        );

        assertEquals(
                new FunctionTypeNode(
                        List.of(new PrimitiveTypeNode(TypeTag.ANY)),
                        new PrimitiveTypeNode(TypeTag.VOID)
                ),
                type("(any) -> void")
        );

        assertEquals(
                new FunctionTypeNode(
                        List.of(new PrimitiveTypeNode(TypeTag.ANY), new PrimitiveTypeNode(TypeTag.ANY)),
                        new PrimitiveTypeNode(TypeTag.VOID)
                ),
                type("(any, any) -> void")
        );
    }

    public void testUncertainType() {
        assertEquals(
                new UncertainTypeNode(
                        new PrimitiveTypeNode(TypeTag.NEVER),
                        new PrimitiveTypeNode(TypeTag.ANY)
                ),
                type("[never, any]")
        );
    }

    public void testParType() {
        assertEquals(
                new PrimitiveTypeNode(TypeTag.ANY),
                type("(any)")
        );
    }

    public void testOptType() {
        assertEquals(
                new UnionTypeNode(
                        List.of(
                                new PrimitiveTypeNode(TypeTag.ANY),
                                new PrimitiveTypeNode(TypeTag.NULL)
                        )
                ),
                type("any?")
        );
    }

    public void testQualClassType() {
        assertEquals(
                new ClassTypeNode(new SelectorExpr(
                        new SelectorExpr(
                                Ident.from("java"),
                                Name.from("io")
                        ),
                        Name.from("Serializable")
                )),
                type("java.io.Serializable")
        );
    }

    public void testParameterizedType() {
        assertEquals(
                new ClassTypeNode(
                        new TypeApply(
                                Ident.from("List"),
                                List.of(new PrimitiveTypeNode(TypeTag.STRING))
                        )
                ),
                type("List<string>")
        );
    }

    public void testUnionType() {
        assertEquals(
                new UnionTypeNode(
                        List.of(
                                new PrimitiveTypeNode(TypeTag.INT),
                                new PrimitiveTypeNode(TypeTag.LONG),
                                new PrimitiveTypeNode(TypeTag.STRING)
                        )
                ),
                type("int|long|string")
        );
    }

    public void testIntersectionType() {
        assertEquals(
                new IntersectionTypeNode(
                        List.of(
                                new ClassTypeNode(Ident.from("Displayable")),
                                new ClassTypeNode(Ident.from("Purchasable")),
                                new ClassTypeNode(Ident.from("Sellable"))
                        )
                ),
                type("Displayable&Purchasable&Sellable")
        );
    }

    private TypeNode type(String text) {
        var parser = parser(text);
        var type = parser.type();
        assertSame(TokenKind.EOF, parser.token().getKind());
        return type;
    }


    public void testPackageAndImport() {
        var file = parse("""
                package shopping
            
                import util.Util
                """);
        var expected = new File(
                new PackageDecl(new Ident(Name.from("shopping"))),
                List.of(
                        new Import(new SelectorExpr(new Ident(Name.from("util")), Name.from("Util")))
                ),
                List.of()
        );
        assertEquals(expected, file);
    }

    public void testAnnotation() {
        assertEquals(
                new Annotation(Name.from("Bean"), List.of()),
                parser("@Bean").annotation()
        );

        assertEquals(
                new Annotation(
                        Name.from("Tag"),
                        List.of(
                                new Annotation.Attribute(
                                        Name.from("value"),
                                        new Literal(1)
                                )
                        )
                ),
                parser("@Tag(1)").annotation()
        );
    }

    public void testModifiers() {
        var mods = parser("pub value").mods();
        assertEquals(
                List.of(new Modifier(ModifierTag.PUB), new Modifier(ModifierTag.VALUE)),
                mods
        );
    }

    public void testInitBlock() {
        assertEquals(
                ClassDeclBuilder.builder(Name.from("Foo"))
                        .addMember(new Init(new Block(List.of())))
                        .build(),
                classDecl("""
                        class Foo {
                            {}
                        }
                        """)
        );
    }


    public void testTypeParams() {
        var classDecl = classDecl("class Entry<K, V> {}");
        var expected = new ClassDecl(
                ClassTag.CLASS,
                List.nil(),
                List.nil(),
                Name.from("Entry"),
                null,
                List.of(),
                List.of(
                        new TypeVariableDecl(Name.from("K"), null),
                        new TypeVariableDecl(Name.from("V"), null)
                ),
                List.of(),
                List.of(),
                List.of()
        );
        assertEquals(expected, classDecl);
    }

    public void testNestedTypeArg() {
        var expr = expr("List<List<string>>()");
        var expected = new Call(
                new TypeApply(
                        new Ident(Name.from("List")),
                        List.of(
                                new ClassTypeNode(
                                    new TypeApply(
                                            new Ident(Name.from("List")),
                                            List.of(
                                                    new PrimitiveTypeNode(TypeTag.STRING)
                                            )
                                    )
                                )
                        )
                ),
                List.nil()
        );
        assertEquals(expected, expr);
    }

    public void testTypeArgs() {
        var expr = expr("HashMap<string, any>()");
        var expected = new Call(
                new TypeApply(
                        new Ident(Name.from("HashMap")),
                        List.of(
                                new PrimitiveTypeNode(TypeTag.STRING),
                                new PrimitiveTypeNode(TypeTag.ANY)

                        )
                ),
                List.nil()
        );
        assertEquals(expected, expr);
    }

    public void testLocalVar() {
        var method = parser("""
                fn test() {
                    var name = "Foo"
                }
                """).method(List.nil(), List.nil());
        var expected = new MethodDecl(
                List.nil(),
                List.nil(),
                List.nil(),
                Name.from("test"),
                List.of(),
                null,
                new Block(
                        List.of(
                                new DeclStmt(
                                        new LocalVarDecl(
                                                null,
                                                Name.from("name"),
                                                new Literal("Foo")
                                        )
                                )
                        )
                )
        );
        assertEquals(expected, method);
    }

    public void testBlockStmt() {
        var actual = stmt("{}");
        var expected = new BlockStmt(new Block(List.nil()));
        assertEquals(expected, actual);
    }

    public void testStmtStartingWithValue() {
        assertEquals(
                new ExprStmt(
                        new AssignExpr(
                                null,
                                Ident.from("value"),
                                new Literal(1)
                        )
                ),
                stmt("value = 1")
        );
    }

    public void testLocalClass() {
        assertEquals(
                new DeclStmt(
                    ClassDeclBuilder.builder(Name.from("Money"))
                            .tag(ClassTag.VALUE)
                            .addModifier(new Modifier(ModifierTag.VALUE))
                            .build()
                ),
                stmt("value class Money()")
        );
    }

    public void testValueClass() {
        assertEquals(
                new File(
                        null,
                        List.of(),
                        List.of(
                        ClassDeclBuilder.builder(Name.from("Money"))
                                .tag(ClassTag.VALUE)
                                .addModifier(new Modifier(ModifierTag.VALUE))
                                .build()
                        )
                ),
                file("value class Money()")
        );

    }

    private File file(String text) {
        var parser = parser(text);
        var file = parser.file();
        assertSame(TokenKind.EOF, parser.token().getKind());
        return file;
    }

    public void testThrowStmt() {
        assertEquals(
                new ThrowStmt(new Call(new Ident(Name.from("Exception")), List.of())),
                stmt("throw Exception()")
        );
    }

    public void testDeleteStmt() {
        assertEquals(
                new DelStmt(Ident.from("a")),
                stmt("delete a")
        );
    }

    public void testLabeledStmt() {
        assertEquals(
                new LabeledStmt(
                        Name.from("out"),
                        new BlockStmt(new Block(List.of()))
                ),
                stmt("out: {}")
        );
        assertEquals(
                new LabeledStmt(
                        Name.from("value"),
                        new BlockStmt(new Block(List.of()))
                ),
                stmt("value: {}")
        );
    }

    public void testTryStmt() {
        assertEquals(
                new TryStmt(
                        new Block(List.of()),
                        List.of(
                                new Catcher(
                                        new LocalVarDecl(
                                                new ClassTypeNode(Ident.from("Exception")),
                                                Name.from("e"),
                                                null
                                        ),
                                        new Block(List.of())
                                )
                        )
                ),
                stmt("""
                        try {
                        } catch (e: Exception) {}
                        """)
        );
    }

    private Stmt stmt(String text) {
        var parser = parser(text);
        var stmt = parser.stmt();
        assertSame(TokenKind.EOF, parser.token().getKind());
        return stmt;
    }


    public void testNonnullExpr() {
        assertEquals(
                new PostfixExpr(PostfixOp.NONNULL, new Ident(Name.from("name"))),
                expr("name!!")
        );
    }


    public void testSelectorExpr() {
        assertEquals(
                new SelectorExpr(
                        new Ident(Name.from("product")),
                        Name.from("name")
                ),
                expr("product.name")
        );
    }

    public void testQualThis() {
        assertEquals(
                new SelectorExpr(
                        new Ident(Name.from("Parent")),
                        Name.from("this")
                ),
                expr("Parent.this")
        );
    }

    public void testLtExpr() {
        assertEquals(
                new BinaryExpr(
                        BinOp.LT,
                        new Ident(Name.from("stock")),
                        new Ident(Name.from("quantity"))
                ),
                expr("stock < quantity")
        );
    }

    public void testNumberLit() {
        assertEquals(
                new Literal(1),
                expr("1")
        );
        assertEquals(
                new Literal(1L),
                expr("1l")
        );
        assertEquals(
                new Literal(1.1),
                expr("1.1")
        );
        assertEquals(
                new Literal(1.1f),
                expr("1.1f")
        );
    }

    public void testStringLit() {
        assertEquals(
                new Literal("Kiwi"),
                expr("\"Kiwi\"")
        );
    }

    public void testBoolLit() {
        assertEquals(
                new Literal(false),
                expr("false")
        );
        assertEquals(
                new Literal(true),
                expr("true")
        );
    }

    public void testAssignExpr() {
        assertEquals(
                new AssignExpr(null, Ident.from("a"), new Literal(1)),
                expr("a = 1")
        );
        assertEquals(
                new AssignExpr(null,
                        Ident.from("a"),
                        new BinaryExpr(BinOp.MUL, Ident.from("b"), new Literal(2))
                ),
                expr("a = b * 2")
        );
    }

    public void testCompositeAssignExpr() {
        assertEquals(
                new AssignExpr(BinOp.ADD, Ident.from("a"), Ident.from("e")),
                expr("a += e")
        );
    }

    public void testNestedAssign() {
        assertEquals(
                new AssignExpr(
                        null,
                        Ident.from("a"),
                        new AssignExpr(
                                null,
                                Ident.from("b"),
                                new Literal(1)
                        )
                ),
                expr("a = b = 1")
        );
    }

    public void testAddExpr() {
        assertEquals(
                new BinaryExpr(BinOp.ADD, Ident.from("a"), Ident.from("b")),
                expr("a + b")
        );
    }

    public void testMulExpr() {
        assertEquals(
                new BinaryExpr(BinOp.MUL, Ident.from("a"), Ident.from("b")),
                expr("a * b")
        );
    }

    public void testDivExpr() {
        assertEquals(
                new BinaryExpr(BinOp.DIV, Ident.from("a"), Ident.from("b")),
                expr("a / b")
        );
    }

    public void testCallExpr() {
        assertEquals(
                new Call(Ident.from("speak"), List.of()),
                expr("""
                        speak()
                        """)
        );
        assertEquals(
                new Call(
                        Ident.from("require"),
                        List.of(
                                Ident.from("cond"),
                                new Literal("Value must be true")
                        )
                ),
                expr("""
                        require(cond, "Value must be true")
                        """)
        );
    }

    public void testRangeExpr() {
        assertEquals(
                new RangeExpr(new Literal(0), new Literal(10)),
                expr("0...10")
        );
    }

    public void testTernaryExpr() {
        assertEquals(
                new CondExpr(
                        Ident.from("value"),
                        new Literal(1),
                        new Literal(2)
                ),
                expr("value ? 1 : 2")
        );
    }

    private Expr expr(String text) {
        var parser = parser(text);
        var expr = parser.expr();
        assertSame(TokenKind.EOF, parser.token().getKind());
        return expr;
    }

    public void testRetStmt() {
        assertEquals(
                new RetStmt(Ident.from("a")),
                stmt("return a")
        );
    }

    public void testForeachStmt() {
        assertEquals(
                new ForeachStmt(
                        new LocalVarDecl(null, Name.from("e"), null),
                        Ident.from("list"),
                        new ExprStmt(
                                new Call(
                                        Ident.from("print"),
                                        List.of(Ident.from("e"))
                                )
                        )
                ),
                stmt("""
                        for (e in list) print(e)
                        """)
        );
    }

    public void testWhileStmt() {
        assertEquals(
                new WhileStmt(
                        Ident.from("cond"),
                        new BlockStmt(new Block(List.of()))
                ),
                stmt("while (cond) {}")
        );
    }

    public void testDoWhileStmt() {
        assertEquals(
                new DoWhileStmt(
                        Ident.from("cond"),
                        new BlockStmt(new Block(List.of()))
                ),
                stmt("do {} while (cond)")
        );
    }

    public void testContinueStmt() {
        assertEquals(
                new ContinueStmt(null),
                stmt("continue")
        );
        assertEquals(
                new ContinueStmt(Name.from("out")),
                stmt("continue out")
        );
        assertEquals(
                new ContinueStmt(null),
                parser("""
                        continue
                        out
                        """).stmt()
        );
    }

    public void testBreakStmt() {
        assertEquals(
                new BreakStmt(null),
                stmt("break")
        );
        assertEquals(
                new BreakStmt(Name.from("out")),
                stmt("break out")
        );
        assertEquals(
                new BreakStmt(null),
                parser("""
                        break
                        out
                        """).stmt()
        );
    }

    public void testValueIdent() {
        assertEquals(
                new Ident(NameTable.instance.value),
                expr("value")
        );
    }

    public void testAnalyzeParens() {
        assertSame(PARENS, parser("(a + b)").analyzeParen());
        assertSame(LAMBDA, parser("() -> true").analyzeParen());
        assertSame(LAMBDA, parser("(s) -> true").analyzeParen());
        assertSame(LAMBDA, parser("(String s) -> true").analyzeParen());
        assertSame(PARENS, parser("(a as int)").analyzeParen());
    }

    public void testAnalyzeLt() {
        assertSame(LtResult.LT, parser("<b").analyzeLt());
        assertSame(LtResult.LT, parser("<b && b>d").analyzeLt());
        assertSame(LtResult.LT, parser("<b & 1>d").analyzeLt());
        assertSame(LtResult.TYPE_ARGS, parser("<b & b>d").analyzeLt());
        assertSame(LtResult.TYPE_ARGS, parser("<b>").analyzeLt());
        assertSame(LtResult.TYPE_ARGS, parser("<List<string>>").analyzeLt());
        assertSame(LtResult.TYPE_ARGS, parser("<string>>()").analyzeLt());
    }

    public void testParExpr() {
        assertEquals(
                new BinaryExpr(
                        BinOp.ADD,
                        Ident.from("a"),
                        Ident.from("b")
                ),
                expr("(a + b)"));
    }

    public void testNewArrayExpr() {
        assertEquals(
                new NewArrayExpr(
                        new PrimitiveTypeNode(TypeTag.ANY),
                        false,
                        List.of()
                ),
                expr("new any[]")
        );

        assertEquals(
                new NewArrayExpr(
                        new PrimitiveTypeNode(TypeTag.INT),
                        false,
                        List.of(new Literal(1), new Literal(2), new Literal(3))
                ),
                expr("new int[] {1, 2, 3}")
        );
    }

    public void testNewArrayFollowedByBlock() {
        assertEquals(
                new NewArrayExpr(
                        new PrimitiveTypeNode(TypeTag.INT),
                        false,
                        List.of()
                ),
                parser("""
                        new int[]
                        {1, 2, 3}
                        """).expr()
        );
    }

    public void testAnonClassExpr() {
        assertEquals(
                new AnonClassExpr(
                        ClassDeclBuilder.builder(NameTable.instance.empty)
                                .addExtend(new Extend(new Call(Ident.from("Speak"), List.of())))
                                .build()
                ),
                expr("new Speak() {}")
        );
    }

    public void testIntegrated() throws URISyntaxException, IOException {
        var path = Paths.get(requireNonNull(ParserTest.class.getResource("/kiwi/shopping.kiwi")).toURI());
        var sourceFile = new PathSourceFile(
                path,
                new FileManager()
        );
        var l = new DefaultLog(sourceFile, DiagFactory.instance, new PrintWriter(System.out), new PrintWriter(System.err));
        var buf = Files.readString(path).toCharArray();
        var parser = new Parser(
                l, new Lexer(l, buf, buf.length)
        );
        parser.file();
        l.flush();
    }

    public void testError1() {
        log.setDelayed(true);
        parse("""
                @Label("商品")
                final class Product {}
                """);
        assertEquals(1, log.getDiags().size());
        var diag = log.getDiags().head();
        assertEquals(
                """
                        dummy.kiwi:2: Unexpected token: final
                            final class Product {}
                            ^""",
                diag.toString()
        );
    }

    private Parser parser(String text) {
        var log = log(text);
        return new Parser(log, new Lexer(log, text.toCharArray(), text.length()));
    }

    private Log log(String text) {
        log.setSource(new DiagSource(new DummySourceFile(text), log));
        return log;
    }

    private File parse(String text) {
        return parser(text).file();
    }

}
