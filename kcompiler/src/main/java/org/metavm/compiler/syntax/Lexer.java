package org.metavm.compiler.syntax;

import org.jetbrains.annotations.Nullable;
import org.metavm.compiler.diag.Diag;
import org.metavm.compiler.diag.Log;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.NoSuchElementException;
import java.util.Objects;

import static org.metavm.compiler.syntax.TokenKind.EOF;

public class Lexer {

    public static final Logger logger = LoggerFactory.getLogger(Lexer.class);

    public static final Token DUMMY = new Token(TokenKind.ERROR, 0, 0) {
        @Override
        public void setNext(@Nullable Token next) {
        }
    };

    private final Log log;
    private final Tokenizer tokenizer;
    private Token prevToken = DUMMY;
    private Token token;
    private Token tip;
    private int line;

    public Lexer(Log log, char[] buf, int len) {
        tokenizer = new Tokenizer(log, buf, len);
        this.log = log;
        tip = token = tokenizer.nextToken();
    }

    public Token nextToken() {
        prevToken = token;
        return token = getNext(token);
    }

    public Token token() {
        return token;
    }

    public void split() {
        var splits = token.split();
        prevToken = splits[0];
        if (tip == token)
            tip = splits[1];
        splits[1].setNext(token.getNext());
        token = splits[1];
        prevToken.setNext(token);
    }

    public Token peekToken() {
        return peekToken(token);
    }

    public Token peekToken(Token token) {
        return getNext(token);
    }

    public LaIt la(int lookahead) {
        if (token.getKind() == EOF)
            return emptyLaIt;
        else {
            var t = token;
            for (int i = 0; i < lookahead; i++)
                t = getNext(t);
            return new LaIterator(t);
        }
    }

    private Token getNext(Token token) {
        if (token.getKind() == EOF)
            throw new IllegalStateException("EOF");
        if (token.getNext() == null) {
            assert token == tip;
            advance();
        }
        return Objects.requireNonNull(token.getNext());
    }

    public Token prevToken() {
        return prevToken;
    }

    private void advance() {
        var newTip = tokenizer.nextToken();
        tip.setNext(newTip);
        tip = newTip;
    }

    public int getLine(int pos) {
        return tokenizer.getLine(pos);
    }

    public Marker mark() {
        return new Marker();
    }

    public interface LaIt {

        Token get();

        Token peek();

        Token prev();

        boolean hasNext();

        void next();
    }

    class LaIterator implements LaIt {

        private Token prev = DUMMY;
        private Token token;

        public LaIterator(Token token) {
            this.token = token;
        }

        public boolean hasNext() {
            return !token.isEof();
        }

        public void next() {
            if (token.isEof())
                throw new NoSuchElementException();
            prev = token;
            if (token.getNext() == null) {
                assert tip == token;
                advance();
            }
            token = Objects.requireNonNull(token.getNext());
        }

        @Override
        public Token get() {
            if (token != null)
                return token;
            else
                throw new NoSuchElementException();
        }

        @Override
        public Token peek() {
            return getNext(token);
        }

        @Override
        public Token prev() {
            return prev;
        }
    }

    final LaIt emptyLaIt = new LaIt() {

        @Override
        public Token get() {
            throw new NoSuchElementException();
        }

        @Override
        public Token peek() {
            throw new NoSuchElementException();
        }

        @Override
        public Token prev() {
            return DUMMY;
        }

        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public void next() {
            throw new NoSuchElementException();
        }
    };

    public class Marker {

        private final Token prevToken = Lexer.this.prevToken;
        private final int line = Lexer.this.line;
        private final org.metavm.compiler.util.List<Diag> prevDiags = log.getDiags();

        public void rollback() {
            Lexer.this.prevToken = prevToken;
            token = Objects.requireNonNull(prevToken.getNext());
            Lexer.this.line = line;
            log.setDiags(prevDiags);
        }

    }

}
