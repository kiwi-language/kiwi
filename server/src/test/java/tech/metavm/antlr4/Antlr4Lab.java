package tech.metavm.antlr4;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

public class Antlr4Lab {

    public static void main(String[] args) {
        var lexer = new TestLexer(CharStreams.fromString("1 ? 2 : 3"));
        var parser = new TestParser(new CommonTokenStream(lexer));
        var expr = parser.expr();
        System.out.println(expr.getChildCount());
    }

}
