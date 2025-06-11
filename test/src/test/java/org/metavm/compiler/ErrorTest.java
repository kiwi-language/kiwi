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

public class ErrorTest extends TestCase {

    public static final Logger logger = LoggerFactory.getLogger(ErrorTest.class);

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

    public void testCantResolveExpr() {
        var diags = compile("""
                class Foo {
                
                    fn test() {
                        bar()
                    }
                    
                }
                """);
        assertEquals(1, diags.size());
        logger.debug("{}", diags.head());
        assertEquals("""
                           dummy.kiwi:4: Cannot resolve function
                                       bar()
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
        logger.debug("{}", diags.head().toString());
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
        file.accept(new TypeResolver(project));
        file.accept(new IdentAttr(project, log));
        file.accept(new Attr(project, log));
        return log.getDiags();
    }

}
