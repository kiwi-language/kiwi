package tech.metavm.object.instance.query;

import tech.metavm.util.NncUtils;

import java.util.*;

public class ExpressionTokenizer {

    private final String expression;
    private Token next;
    private int position;

    private static final Set<Character> SPACES = Set.of(
        ' ', '\t', '\r'
    );

    private static final Set<Character> OPERATORS = Set.of(
        '.', '=', '!', '>', '<', ')', '(', '-', '+', '*', '/', '`', '%', ','
    );

    private static final Set<Character> DIGITS = Set.of(
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
    );

    private static final Set<Character> COMBINABLE_OPERATORS = Set.of(
            '>', '<', '-', '+', '!'
    );

    public static final Set<String> OPERATOR_COMBOS = Set.of(
            ">=", "<=", "!="
    );

    public static final Set<String> KEYWORDS = new HashSet<>();

    private static final Set<String> KEYWORD_SEQUENCES = Set.of(
            "IS NULL", "IS NOT NULL", "STARTS WITH", "NOT EXISTS"
    );

    public static final String NULL = "NULL";

    private static class Tree {
        final Map<String, Tree> childMap = new HashMap<>();
        final String name;

        private Tree(String name) {
            this.name = name;
        }

        void addChild(String childName) {
            childMap.put(childName, new Tree(childName));
        }

        Tree getOrCreateChild(String childName) {
            return childMap.computeIfAbsent(childName.toUpperCase(), k -> new Tree(childName));
        }

        Tree getChild(String childName) {
            return childMap.get(childName.toUpperCase());
        }

        boolean containsChild(String childName) {
            return childMap.containsKey(childName.toUpperCase());
        }

        boolean isLeaf() {
            return childMap.isEmpty();
        }
    }

    private static void addPath(String path) {
        String[] splits = path.split(" ");

        Tree tree = ROOT;
        for (String split : splits) {
            split = split.trim();
            if(split.length() == 0) {
                continue;
            }
            KEYWORDS.add(split);
            tree = tree.getOrCreateChild(split);
        }
    }

    private static final Tree ROOT = new Tree("root");

    static {
        KEYWORD_SEQUENCES.forEach(ExpressionTokenizer::addPath);
    }

    public ExpressionTokenizer(String queryString) {
        this.expression = queryString;
        next = forward();
    }

    public Token next() {
        Token current = next;
        next = forward();
        return current;
    }

    public boolean hasNext() {
        return next != null;
    }

    private Token forward() {
        Token token = forwardOne();
        if(token == null) {
            return null;
        }

        if(token.isVariable()) {
            skipSpaces();
            if(isDot()) {
                List<Token> tokens = new ArrayList<>();
                tokens.add(token);
                while (isDot()) {
                    position++;
                    token = forwardOne();
                    if (token == null || !token.isVariable()) {
                        throw new QueryStringException("Variable expected, but got " + token);
                    }
                    tokens.add(token);
                    skipSpaces();
                }
                String fieldPath = NncUtils.join(tokens, Token::getName, ".");
                return new Token(TokenType.VARIABLE, fieldPath, fieldPath);
            }
            else if(isLeftParenthesis()) {
                return new Token(TokenType.FUNCTION, token.rawValue(), Function.getByNameRequired(token.getName()));
            }
            else {
                return token;
            }
        }
        else if(ROOT.containsChild(token.rawValue())) {
            List<Token> tokens = new ArrayList<>();
            tokens.add(token);
            Tree tree = ROOT.getChild(token.rawValue());
            while (!tree.isLeaf()) {
                token = forwardOne();
                if (token == null || !tree.containsChild(token.rawValue())) {
                    throw new QueryStringException("Incomplete token sequence: " + tokenSequenceValue(tokens));
                }
                tree = tree.getChild(token.rawValue());
                tokens.add(token);
            }
            String tokenSeq = tokenSequenceValue(tokens);
            return new Token(
                    TokenType.OPERATOR,
                    tokenSeq,
                    Operator.getByOpRequired(tokenSeq)
            );
        }
        else {
            return token;
        }
    }

    private String tokenSequenceValue(List<Token> tokens) {
        return NncUtils.join(tokens, Token::rawValue, " ");
    }

    private Token forwardOne() {
        skipSpaces();

        int start = position;
        if(isEof()) {
            return null;
        }

        TokenType tokenType;
        if(isOperator()) {
//            if(isNegation()) {
//                position++;
//                skipSpaces();
//                if(isDigit()) {
//                    tokenType = forwardNumber();
//                }
//                else {
//                    tokenType = TokenType.OPERATOR;
//                }
//            }
            if(isLeftParenthesis()) {
                position++;
                tokenType = TokenType.LEFT_PARENTHESIS;
            }
            else if(isRightParenthesis()) {
                position++;
                tokenType = TokenType.RIGHT_PARENTHESIS;
            }
            else if(isCombinableOperator()) {
                position++;
                if(isOperatorCombo()) {
                    position ++;
                }
                tokenType = TokenType.OPERATOR;
            }
            else {
                position++;
                tokenType = TokenType.OPERATOR;
            }
        }
        else if(isSingleQuotation()) {
            forwardSingleQuoted();
            tokenType = TokenType.SINGLE_QUOTED_STRING;
        }
        else if(isDoubleQuotation()) {
            forwardDoubleQuoted();
            tokenType = TokenType.DOUBLE_QUOTED_STRING;
        }
        else if(isDigit()) {
            tokenType = forwardNumber();
        }
        else {
            while (!isEof() && !isSpace() && !isOperator()) {
                position++;
            }
            if(isNull(start)) {
                tokenType = TokenType.NULL;
            }
            else if(isBoolean(start)) {
                tokenType = TokenType.BOOLEAN;
            }
            else if(isOperator(start)) {
                tokenType = TokenType.OPERATOR;
            }
            else if(isKeyword(start)) {
                tokenType = TokenType.KEYWORD;
            }
            else {
                tokenType = TokenType.VARIABLE;
            }
        }
        return createToken(tokenType, expression.substring(start, position));
    }

    private Token createToken(TokenType type, String rawValue) {
        Object value =  switch (type) {
            case INTEGER -> Long.parseLong(rawValue);
            case FLOAT -> Double.parseDouble(rawValue);
            case BOOLEAN -> Boolean.parseBoolean(rawValue);
            case DOUBLE_QUOTED_STRING -> unquoteDoubleQuoted(rawValue);
            case SINGLE_QUOTED_STRING -> unquoteSingleQuoted(rawValue);
            case OPERATOR ->  Operator.getByOpRequired(rawValue);
            case FUNCTION -> Function.getByNameRequired(rawValue);
            case VARIABLE -> rawValue;
            case NULL, KEYWORD, LEFT_PARENTHESIS, RIGHT_PARENTHESIS ->  null;
        };
        return new Token(type, rawValue, value);
    }

    private String unquoteDoubleQuoted(String string) {
        return string.substring(1, string.length() - 1);
    }

    private String unquoteSingleQuoted(String str) {
        StringBuilder buf = new StringBuilder();
        for(int i = 1; i < str.length() - 1; i++) {
            char c = str.charAt(i);
            buf.append(c);
            if(c == '\'') {
                i++;
                if(i >= str.length()) {
                    throw new QueryStringException("Malformed single quoted string: " + str);
                }
            }
        }
        return buf.toString();
    }

    private void skipSpaces() {
        while (isSpace()) {
            position++;
        }
    }

    private TokenType forwardNumber() {
        forwardInteger();
        if(isDot()) {
            position++;
            forwardInteger();
            return TokenType.FLOAT;
        }
        else {
            return TokenType.INTEGER;
        }
    }

    private void forwardInteger() {
        while(isDigit()) {
            position++;
        }
    }

    private void forwardSingleQuoted() {
        boolean ended = false;
        do {
            position++;
            if (isEof()) {
                throw new QueryStringException("EOF while parsing string");
            }
            if(isSingleQuotation()) {
                position++;
                if(isSingleQuotation()) {
                    position++;
                }
                else {
                    ended = true;
                }
            }
        } while (!ended);
    }

    private void forwardDoubleQuoted() {
        position++;
        while (!isDoubleQuotation()) {
            position++;
        }
        if(isEof()) {
            throw new QueryStringException("EOF while parsing string");
        }
        position++;
    }

    public char current() {
        return expression.charAt(position);
    }

    public boolean isEof(int pos) {
        return pos >= expression.length();
    }

    public boolean isEof() {
        return isEof(position);
    }

    private boolean isNegation() {
        return !isEof() && expression.charAt(position) == '-';
    }

    private boolean isOperator() {
        return !isEof() && OPERATORS.contains(current());
    }

    private boolean isCombinableOperator() {
        return !isEof() && COMBINABLE_OPERATORS.contains(current());
    }

    private boolean isLeftParenthesis() {
        return !isEof() && current() == '(';
    }

    private boolean isRightParenthesis() {
        return !isEof() && current() == ')';
    }

    private boolean isDot() {
        return !isEof() && current() == '.';
    }

    private boolean isOperatorCombo() {
        if(position > 0 && isEof()) {
            return false;
        }
        return OPERATOR_COMBOS.contains(expression.substring(position - 1, position + 1));
    }

    private boolean isDigit() {
        return !isEof() && DIGITS.contains(current());
    }

    private  boolean isSpace() {
        return !isEof() && SPACES.contains(current());
    }

    private boolean isKeyword(int start) {
        String value = expression.substring(start, position);
        return KEYWORDS.contains(value.toUpperCase());
    }

    private boolean isOperator(int start) {
        return Operator.isOperator(expression.substring(start, position));
    }

    private boolean isNull(int start) {
        return NULL.equals(value(start));
    }

    private boolean isBoolean(int start) {
        String value = upperCaseValue(start);
        return "TRUE".equals(value) || "FALSE".equals(value);
    }

    public String value(int start) {
        return expression.substring(start, position);
    }

    public String upperCaseValue(int start) {
        return value(start).toUpperCase();
    }

    private boolean isSingleQuotation() {
        return !isEof() && current() == '\'';
    }

    private boolean isDoubleQuotation() {
        return !isEof() && current() == '\"';
    }

    public String getExpression() {
        return expression;
    }

    public static void main(String[] args) {
        String query = "material.creator.name starts with '钢' and (material.virtual = true OR number > 0)";
        ExpressionTokenizer tokenizer = new ExpressionTokenizer(query);

        while (tokenizer.hasNext()) {
            System.out.println(tokenizer.next());
        }
    }

}
