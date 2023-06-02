package tech.metavm.transpile;

import junit.framework.TestCase;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class JavaParserTest extends TestCase {

    public static final Logger LOGGER = LoggerFactory.getLogger(JavaParserTest.class);

    public void test() throws IOException {
        String fileName = "/Users/leen/workspace/object/src/test/java/tech/metavm/transpile/JavaParserTest.java";
        JavaLexer lexer = new JavaLexer(CharStreams.fromFileName(fileName));
        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
        JavaParser parser = new JavaParser(tokenStream);
        ParseTree parseTree = parser.compilationUnit();
        ParseTreeWalker walker = new ParseTreeWalker();
        walker.walk(new ParserListener(), parseTree);
    }


    private static class ParserListener extends JavaParserBaseListener {

        @Override
        public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
            JavaParser.IdentifierContext node = ctx.identifier();
            String methodName = node.getText();
            LOGGER.info("method name: " + methodName);
        }
    }

}