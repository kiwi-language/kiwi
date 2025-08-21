package org.metavm.compiler.syntax;

import org.metavm.compiler.diag.Error;
import org.metavm.compiler.diag.*;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.NameTable;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import static org.metavm.compiler.syntax.TokenKind.*;

public class Parser {

    public static final Logger logger = LoggerFactory.getLogger(Parser.class);
    private static final Predicate<TokenKind> LAX_IDENT = tk -> tk == IDENT || tk == VALUE;
    private final Log log;
    private final Lexer lexer;
    private int lastErrorPos = -1;

    public Parser(Log log, Lexer lexer) {
        this.log = log;
        this.lexer = lexer;
    }

    public Parser(Log log, String text) {
        this(log, new Lexer(log, text.toCharArray(), text.length()));
    }

    public File file() {
        var pkgDecl = is(TokenKind.PACKAGE) ? packageDecl() : null;
        var imports = List.<Import>builder();
        while (is(TokenKind.IMPORT)) {
            imports.append(import_());
        }
        var classDecls = List.<ClassDecl>builder();
        while (!isEof()) {
            var annotations = annotations();
            var mods = mods();
            ClassDecl decl;
            switch (tokenKind()) {
                case CLASS -> decl = classDecl(annotations, mods);
                case INTERFACE -> decl = interfaceDecl(annotations, mods);
                case ENUM -> decl = enumDecl(annotations, mods);
                default -> {
                    unexpected();
                    nextToken();
                    continue;
                }
            }
            classDecls.append(decl);
        }
        return new File(
                pkgDecl,
                imports.build(),
                classDecls.build()
        ).setSourceFile(log.getSource().file());
    }

    private TokenKind tokenKind() {
        return lexer.token().getKind();
    }

    public  PackageDecl packageDecl() {
        accept(PACKAGE);
        return new PackageDecl(expr());
    }

    List<Modifier> mods() {
        var mods = List.<Modifier>builder();
        for (;;) {
            // Handle the case of `value: <type>` where value is the name of a class parameter
            if (isEof() || peekToken(token(), COLON))
                return mods.build();
            Modifier mod;
            switch (tokenKind()) {
                case PUB -> mod = new Modifier(ModifierTag.PUB);
                case PRIV -> mod = new Modifier(ModifierTag.PRIV);
                case PROT -> mod = new Modifier(ModifierTag.PROT);
                case STATIC -> mod = new Modifier(ModifierTag.STATIC);
                case ABSTRACT -> mod = new Modifier(ModifierTag.ABSTRACT);
                case DELETED -> mod = new Modifier(ModifierTag.DELETED);
                case VALUE -> mod = new Modifier(ModifierTag.VALUE);
                case TEMP -> mod = new Modifier(ModifierTag.TEMP);
                default -> {
                    return mods.build();
                }
            }
            mods.append(mod.setPos(pos()));
            nextToken();
        }
    }

    private Import import_() {
        var pos = pos();
        accept(TokenKind.IMPORT);
        var imp = new Import((SelectorExpr) expr());
        imp.setPos(pos);
        return imp;
    }

    Expr expr() {
        return assignExpr();
    }

    private Expr assignExpr() {
        var pos = pos();
        var expr = ternaryExpr();
        switch (tokenKind()) {
            case ASSIGN -> {
                nextToken();
                expr = new AssignExpr(null, expr, assignExpr());
            }
            case PLUS_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.ADD, expr, assignExpr());
            }
            case MINUS_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.SUB, expr, assignExpr());
            }
            case MUL_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.MUL, expr, assignExpr());
            }
            case DIV_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.DIV, expr, assignExpr());
            }
            case MOD_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.MOD, expr, assignExpr());
            }
            case AND_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.AND, expr, assignExpr());
            }
            case OR_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.OR, expr, assignExpr());
            }
            case BITOR_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.BIT_OR, expr, assignExpr());
            }
            case BITAND_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.BIT_AND, expr, assignExpr());
            }
            case BITXOR_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.BIT_XOR, expr, assignExpr());
            }
            case SHL_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.SHL, expr, assignExpr());
            }
            case SHR_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.SHR, expr, assignExpr());
            }
            case USHR_ASSIGN -> {
                nextToken();
                expr = new AssignExpr(BinOp.USHR, expr, assignExpr());
            }
            default -> {
                return expr;
            }
        }
        expr.setPos(pos);
        return expr;
    }

    private Expr ternaryExpr() {
        var pos = pos();
        var expr = orExpr();
        if (is(QUES)) {
            nextToken();
            var trueExpr = orExpr();
            accept(COLON);
            var falseExpr = ternaryExpr();
            var condExpr = new CondExpr(expr, trueExpr, falseExpr);
            condExpr.setPos(pos);
            return condExpr;
        }
        else
            return expr;
    }

    private Expr orExpr() {
        var pos = pos();
        var expr = andExpr();
        while (is(OR)) {
            nextToken();
            expr = new BinaryExpr(BinOp.OR, expr, andExpr());
            expr.setPos(pos);
        }
        return expr;
    }

    private Expr andExpr() {
        var pos = pos();
        var expr = rangeExpr();
        while (is(AND)) {
            nextToken();
            expr = new BinaryExpr(BinOp.AND, expr, rangeExpr()).setPos(pos);
        }
        return expr;
    }

    private Expr rangeExpr() {
        var pos = pos();
        var expr = bitorExpr();
        if (is(ELLIPSIS)) {
            nextToken();
            return new RangeExpr(expr, bitorExpr()).setPos(pos);
        }
        else
            return expr;
    }

    private Expr bitorExpr() {
        var pos = pos();
        var expr = bitxorExpr();
        while (is(BITOR)) {
            nextToken();
            expr = new BinaryExpr(BinOp.BIT_OR, expr, bitxorExpr()).setPos(pos);
        }
        return expr;
    }

    private Expr bitxorExpr() {
        var pos = pos();
        var expr = bitandExpr();
        while (is(BITXOR)) {
            nextToken();
            expr = new BinaryExpr(BinOp.BIT_XOR, expr, bitandExpr()).setPos(pos);
        }
        return expr;
    }

    private Expr bitandExpr() {
        var pos = pos();
        var expr = equalityExpr();
        while (is(BITAND)) {
            nextToken();
            expr = new BinaryExpr(BinOp.BIT_AND, expr, equalityExpr()).setPos(pos);
        }
        return expr;
    }

    private Expr equalityExpr() {
        var pos = pos();
        var expr = relationalExpr();
        for(;;) {
            BinOp op;
            switch (tokenKind()) {
                case EQ -> op = BinOp.EQ;
                case NE -> op = BinOp.NE;
                default -> {
                    return expr;
                }
            }
            nextToken();
            expr = new BinaryExpr(op, expr, relationalExpr()).setPos(pos);
        }
    }

    private Expr relationalExpr() {
        var pos = pos();
        var expr = isExpr();
        for (;;) {
            BinOp op;
            switch (tokenKind()) {
                case LT -> op = BinOp.LT;
                case GT -> op = BinOp.GT;
                case LE -> op = BinOp.LE;
                case GE -> op = BinOp.GE;
                default -> {
                    return expr;
                }
            }
            nextToken();
            expr = new BinaryExpr(op, expr, isExpr()).setPos(pos);
        }
    }

    private Expr isExpr() {
        var pos = pos();
        var expr = shiftExpr();
        while (is(TokenKind.IS)) {
            nextToken();
            var type = type();
            LocalVarDecl var = null;
            if (is(TokenKind.IDENT)) {
                var varPos = pos();
                var = new LocalVarDecl(type, ident(), null, false).setPos(varPos);
            }
            expr = new IsExpr(expr, type, var).setPos(pos);
        }
        return expr;
    }

    private Expr shiftExpr() {
        var pos = pos();
        var expr = additiveExpr();
        for (;;) {
            BinOp op;
            switch (tokenKind()) {
                case SHL -> op = BinOp.SHL;
                case SHR -> op = BinOp.SHR;
                case USHR -> op = BinOp.USHR;
                default -> {
                    return expr;
                }
            }
            nextToken();
            expr = new BinaryExpr(op, expr, additiveExpr()).setPos(pos);
        }
    }

    private Expr additiveExpr() {
        var pos = pos();
        var expr = multiplicativeExpr();
        for (;;) {
            BinOp op;
            switch (tokenKind()) {
                case PLUS -> op = BinOp.ADD;
                case MINUS -> op = BinOp.SUB;
                default -> {
                    return expr;
                }
            }
            nextToken();
            expr = new BinaryExpr(op, expr, multiplicativeExpr()).setPos(pos);
        }
    }

    private Expr multiplicativeExpr() {
        var pos = pos();
        var expr = asExpr();
        for (;;) {
            BinOp op;
            switch (tokenKind()) {
                case MUL -> op = BinOp.MUL;
                case DIV -> op = BinOp.DIV;
                case MOD -> op = BinOp.MOD;
                default -> {
                    return expr;
                }
            }
            nextToken();
            expr = new BinaryExpr(op, expr, asExpr()).setPos(pos);
        }
    }

    private Expr asExpr() {
        var expr = prefixExpr();
        while (is(AS)) {
            var pos = pos();
            nextToken();
            var type = type();
            expr = new CastExpr(type, expr).setPos(pos);
        }
        return expr;
    }

    private Expr prefixExpr() {
        var pos = pos();
        Expr expr;
        switch (tokenKind()) {
            case PLUS -> {
                nextToken();
                expr = new PrefixExpr(PrefixOp.POS, prefixExpr());
            }
            case MINUS -> {
                nextToken();
                expr = new PrefixExpr(PrefixOp.NEGATE, prefixExpr());
            }
            case INC -> {
                nextToken();
                expr = new PrefixExpr(PrefixOp.INC, prefixExpr());
            }
            case DEC -> {
                nextToken();
                expr = new PrefixExpr(PrefixOp.DEC, prefixExpr());
            }
            case NOT -> {
                nextToken();
                expr = new PrefixExpr(PrefixOp.NOT, prefixExpr());
            }
            case BITNOT -> {
                nextToken();
                expr = new PrefixExpr(PrefixOp.BIT_NOT, prefixExpr());
            }
            default -> {
                return postfixExpr();
            }
        }
        expr.setPos(pos);
        return expr;
    }

    private Expr postfixExpr() {
        var expr = atomExpr();
        for (;;) {
            switch (tokenKind()) {
                case INC -> {
                    nextToken();
                    expr = new PostfixExpr(PostfixOp.INC, expr).setPos(expr.getIntPos());
                }
                case DEC -> {
                    nextToken();
                    expr = new PostfixExpr(PostfixOp.DEC, expr).setPos(expr.getIntPos());
                }
                case LBRACKET -> {
                    nextToken();
                    var index = expr();
                    accept(TokenKind.RBRACKET);
                    expr = new IndexExpr(expr, index).setPos(expr.getIntPos());
                }
                case DOT -> {
                    nextToken();
                    var pos1 = pos();
                    var name = switch (tokenKind()) {
                        case THIS -> {
                            nextToken();
                            yield NameTable.instance.this_;
                        }
                        case SUPER -> {
                            nextToken();
                            yield NameTable.instance.super_;
                        }
                        default -> ident();
                    };
                    expr = new SelectorExpr(expr, name).setPos(pos1);
                }
                case NONNULL -> {
                    nextToken();
                    expr = new PostfixExpr(PostfixOp.NONNULL, expr).setPos(expr.getIntPos());
                }
                case LPAREN -> {
                    var args = arguments();
                    expr = new Call(expr, args).setPos(expr.getIntPos());
                }
                case LT -> {
                    if (analyzeLt() == LtResult.TYPE_ARGS)
                        expr = new TypeApply(expr, typeArgs()).setPos(expr.getIntPos());
                    else
                        return expr;
                }
                default -> {
                    return expr;
                }
            }
        }
    }

    private Expr atomExpr() {
        var pos = pos();
        var atomExpr = switch (tokenKind()) {
            case IDENT, VALUE, TIME, PASSWORD -> {
                if (peekToken().is(ARROW))
                    yield lambdaExpr();
                else
                    yield new Ident(ident());
            }
            case INTEGER_LIT -> {
                var numToken = (NumberToken) token();
                nextToken();
                yield new Literal(Integer.parseInt(numToken.getValue(), numToken.getRadix()));
            }
            case LONG_LIT -> {
                var numToken = (NumberToken) token();
                nextToken();
                yield new Literal(Long.parseLong(numToken.getValue(), numToken.getRadix()));
            }
            case FLOAT_LIT -> {
                var numToken = (NumberToken) token();
                nextToken();
                yield new Literal(Float.parseFloat(numToken.getValue()));
            }
            case DOUBLE_LIT -> {
                var numToken = (NumberToken) token();
                nextToken();
                yield new Literal(Double.parseDouble(numToken.getValue()));
            }
            case STRING_LIT -> {
                var strToken = (StringToken) token();
                nextToken();
                yield new Literal(strToken.getValue());
            }
            case CHAR_LIT ->  {
                var numberToken = (NumberToken) token();
                nextToken();
                yield new Literal((char) Integer.parseInt(numberToken.getValue(), 10));
            }
            case TRUE -> {
                nextToken();
                yield new Literal(true);
            }
            case FALSE -> {
                nextToken();
                yield new Literal(false);
            }
            case NULL -> {
                nextToken();
                yield new Literal(null);
            }
            case THIS -> {
                nextToken();
                yield new Ident(NameTable.instance.this_);
            }
            case SUPER -> {
                nextToken();
                yield new Ident(NameTable.instance.super_);
            }
            case LPAREN -> {
                var r = analyzeParen();
                yield switch (r) {
                    case PARENS -> {
                        nextToken();
                        var expr = expr();
                        accept(RPAREN);
                        yield expr;
                    }
                    case LAMBDA -> lambdaExpr();
                };
            }
            case NEW -> {
                nextToken();
                var type = type();
                if (type instanceof ArrayTypeNode arrayType)
                    yield newArrayExpr(arrayType);
                else if (type instanceof ClassTypeNode classType)
                    yield anonClassExpr(classType);
                else {
                    error(Errors.illegalNewExpression);
                    yield new ErrorExpr();
                }
            }
            default -> {
                log.error(token().getStart(), Errors.illegalStartOfExpr);
                nextToken();
                yield new ErrorExpr();
            }
        };
        atomExpr.setPos(pos);
        return atomExpr;
    }

    private List<TypeNode> typeArgs() {
        accept(LT);
        if (isGt()) {
            nextToken();
            return List.of();
        }
        var types = List.<TypeNode>builder();
        for (;;) {
            var t = type();
            types.append(t);
            if (is(COMMA))
                nextToken();
            else
                break;
        }
        acceptGt();
        return types.build();
    }

    private Lexer.LaIt la(int n) {
        return lexer.la(n);
    }

    private AnonClassExpr anonClassExpr(ClassTypeNode classType) {
        var args = arguments();
        var members = classMembers();
        return new AnonClassExpr(
                ClassDeclBuilder.builder(NameTable.instance.empty)
                        .addExtend(new Extend(new Call(classType.getExpr(), args)))
                        .members(members)
                        .build()
        );
    }


    private NewArrayExpr newArrayExpr(ArrayTypeNode arrayType) {
        var elements = List.<Expr>builder();
        if (is(LBRACE) && line() == line(prevEnd())) {
            nextToken();
            if (!is(RBRACE)) {
                elements.append(expr());
                while (is(COMMA)) {
                    nextToken();
                    elements.append(expr());
                }
            }
            accept(RBRACE);
        }
        return new NewArrayExpr(
                arrayType.element(),
                false,
                elements.build()
        );
    }

    enum LtResult {
        LT,
        TYPE_ARGS
    }

    LtResult analyzeLt() {
        var it = la(0);
        var depth = 0;
        var nesting = 0;
        for (;; it.next()) {
            switch (it.get().getKind()) {
                case BYTE:
                case SHORT:
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                case VOID:
                case BOOL:
                case STRING:
                case ANY:
                case NEVER:
                    if (depth == 1) {
                        if (it.peek().isOneOf(COMMA, GT))
                            return LtResult.TYPE_ARGS;
                    }
                    break;
                case LPAREN:
                    nesting++;
                    break;
                case RPAREN:
                    if (--nesting < 0)
                        return LtResult.LT;
                    break;
                case LBRACE:
                case RBRACE:
                case ARROW:
                case BITAND:
                case BITOR:
                case COMMA:
                case DOT:
                case NULL:
                case RBRACKET:
                    break;
                case QUES:
                    if (depth == 1) {
                        if (peekToken(it.get(), COMMA) || peekToken(it.get(), GT))
                            return LtResult.TYPE_ARGS;
                    }
                    break;
                case LBRACKET:
                    if (depth == 1) {
                        if (peekToken(it.get(), RBRACKET, COMMA) || peekToken(it.get(), RBRACKET, GT))
                            return LtResult.TYPE_ARGS;
                    }
                    break;
                case VALUE:
                case IDENT:
                    if (peekToken(it.get(), LPAREN))
                        return LtResult.LT;
                    break;
                case LT:
                    depth++;
                    break;
                case USHR:
                    depth--;
                case SHR:
                    depth--;
                case GT:
                    depth--;
//                    if (peekToken(t, LPAREN))
//                        return LtResult.LT;
                    if (depth <= 0)
                        return LtResult.TYPE_ARGS;
                    break;
                default:
                    return LtResult.LT;
            }
        }
    }

    ParensResult analyzeParen() {
        var nesting = 0;
        var depth = 0;
        var it = la(0);
        ParensResult defaultResult = ParensResult.PARENS;
        for (; ; it.next()) {
            switch (it.get().getKind()) {
                case COMMA:
                case DOT:
                case BITAND:
                case BITOR:
                case QUES:
                case NULL:
                case ARROW:
                case RBRACKET:
                    break;
                case BYTE:
                case SHORT:
                case INT:
                case LONG:
                case FLOAT:
                case DOUBLE:
                case BOOL:
                case CHAR:
                case VOID:
                case ANY:
                case NEVER:
                    if (nesting == 1)
                        return ParensResult.LAMBDA;
                    break;
                case LPAREN:
                    if (nesting == 0 && it.peek().is(RPAREN)) {
                        // '(', ')' -> explicit lambda
                        return ParensResult.LAMBDA;
                    }
                    nesting++;
                    break;
                case RPAREN:
                    if (--nesting == 0)
                        return defaultResult;
                    break;
                case IDENT:
                case VALUE:
                    if (nesting == 1) {
                        if (it.prev().isOneOf(LPAREN, COMMA) && it.peek().is(COLON)) {
                            return ParensResult.LAMBDA;
                        } else if (peekToken(it.get(), RPAREN, ARROW)) {
                            return ParensResult.LAMBDA;
                        } else if (depth == 0 && peekToken(it.get(), COMMA)) {
                            defaultResult = ParensResult.LAMBDA;
                        }
                    }
                    break;
//                case FINAL:
//                case ELLIPSIS:
//                    those can only appear in explicit lambdas
//                    return ParensResult.EXPLICIT_LAMBDA;
                case AT:
                    skipAnnotation(it);
                    break;
                case LBRACKET:
                    if (nesting == 1 && peekToken(it.get(), RBRACKET))
                        return ParensResult.LAMBDA;
                    break;
                case LT:
                    depth++;
                    break;
                case USHR:
                    depth--;
                case SHR:
                    depth--;
                case GT:
                    depth--;
                    if (depth == 0) {
                        if (peekToken(it.get(), COMMA) || peekToken(it.get(), RPAREN, ARROW)) {
                            // '>', ',' -> explicit lambda
                            // '>', ')', '->' -> explicit lambda
                            return ParensResult.LAMBDA;
                        }
                        break;
                    } else if (depth < 0) {
                        //unbalanced '<', '>' - not a generic type
                        return ParensResult.PARENS;
                    }
                    break;
                default:
                    //this includes EOF
                    return defaultResult;
            }
        }
    }

    private void skipAnnotation(Lexer.LaIt it) {
        it.next();
        while (!it.get().isEof() && it.peek().is(DOT)) {
            it.next();
            it.next();
        }
        if (it.peek().is(LPAREN)) {
            it.next();
            //skip annotation values
            int nesting = 0;
            for (; ; it.next()) {
                switch (it.get().getKind()) {
                    case LPAREN:
                        nesting++;
                        break;
                    case RPAREN:
                        if (--nesting == 0)
                            return;
                        break;
                    default:
                        if (it.peek().isEof())
                            return;
                }
            }
        }
    }

    enum ParensResult {
        PARENS,
        LAMBDA,
    }

    LambdaExpr lambdaExpr() {
        List<ParamDecl> params;
        if (is(LPAREN))
            params = lambdaParams();
        else {
            params = List.of(new ParamDecl(
                    List.of(),
                    null,
                    ident()
            ));
        }
        accept(ARROW);
        var body = is(LBRACE) ? block() : expr();
        return new LambdaExpr(params, null, body);
    }

    public ClassDecl classDecl(List<Annotation> annotations, List<Modifier> mods) {
        var pos = pos();
        accept(TokenKind.CLASS);
        var name = ident();
        var typeParams = typeParamsOpt();
        var params = classParamsOpt();
        var ext = extendsOpt();
        var members = is(LBRACE) ? classMembers() : List.<Node>of();
        var tag = mods.anyMatch(m -> m.tag() == ModifierTag.VALUE) ? ClassTag.VALUE : ClassTag.CLASS;
        var classDecl = new ClassDecl(
                tag,
                annotations,
                mods,
                name,
                null,
                ext,
                typeParams,
                params,
                List.of(),
                members
        );
        classDecl.setPos(pos);
        if (Traces.traceParsing)
            logger.trace("Parsed class declaration: {}", classDecl);
        return classDecl;
    }

    private List<Extend> extendsOpt() {
        if (!is(COLON))
            return List.of();
        nextToken();
        var ext = List.<Extend>builder();
        var superType = classType();
        if (is(TokenKind.LPAREN))
            ext.append(new Extend(new Call(superType.getExpr(), arguments())));
        else
            ext.append(new Extend(superType.getExpr()));
        while (is(COMMA)) {
            nextToken();
            ext.append(new Extend(classType().getExpr()));
        }
        return ext.build();
    }

    private List<TypeVariableDecl> typeParamsOpt() {
        if (!is(LT))
            return List.of();
        nextToken();
        var typeParams = List.<TypeVariableDecl>builder();
        for (;;) {
            typeParams.append(typeVarDecl());
            if (is(COMMA))
                nextToken();
            else
                break;
        }
        acceptGt();
        return typeParams.build();
    }

    private boolean isGt() {
        return isOneOf(GT, GE, SHR, USHR, SHR_ASSIGN, USHR_ASSIGN);
    }

    private void acceptGt() {
        switch (tokenKind()) {
            case GT -> nextToken();
            case GE, SHR, USHR, SHR_ASSIGN, USHR_ASSIGN -> split();
            default -> error(Errors.expected(GT));
        }
    }

    private void split() {
        lexer.split();
    }

    private void unexpected() {
        log.error(token().getStart(), Errors.unexpectedToken(token()));
    }

    private void error(Error error) {
        log.error(prevEnd(), error);
    }

    private void warn(Warning warning) {
        log.warn(prevEnd(), warning);
    }

    private void note(Note note) {
        log.note(prevEnd(), note);
    }

    private int prevEnd() {
        return lexer.prevToken().getEnd();
    }

    private int pos() {
        return token().getStart();
    }

    private int start() {
        return token().getStart();
    }

    private int end() {
        return token().getEnd();
    }

    private TypeVariableDecl typeVarDecl() {
        var pos = pos();
        var name = ident();
        TypeNode bound;
        if (is(TokenKind.COLON)) {
            nextToken();
            bound = type();
        }
        else
            bound = null;
        return new TypeVariableDecl(name, bound).setPos(pos);
    }

    private @Nullable Node classMember() {
        var annotations = annotations();
        var mods = mods();
        return switch (tokenKind()) {
            case VAL, VAR -> field(annotations, mods);
            case FN -> method(annotations, mods);
            case CLASS -> classDecl(annotations, mods);
            case INTERFACE -> interfaceDecl(annotations, mods);
            case ENUM -> enumDecl(annotations, mods);
            case LBRACE -> new Init(block());
            default -> {
                skipStopAtMember();
                yield null;
            }
        };
    }

    private void skipStopAtMember() {
        for (;;) {
            switch (tokenKind()) {
                case VAR, VAL, FN, CLASS, INTERFACE, ENUM, PUB, PRIV, PROT, STATIC, VALUE, TEMP, AT, LBRACE, RBRACE, EOF -> {
                    return;
                }
                default -> nextToken();
            }
        }
    }

    private ClassDecl interfaceDecl(List<Annotation> annotations, List<Modifier> mods) {
        var pos = pos();
        accept(INTERFACE);
        var name = ident();
        var typeParams = typeParamsOpt();
        var ext = extendsOpt();
        var members = classMembers();
        return new ClassDecl(
                ClassTag.INTERFACE,
                annotations,
                mods,
                name,
                null,
                ext,
                typeParams,
                List.of(),
                List.of(),
                members
        ).setPos(pos);
    }

    ClassDecl enumDecl(List<Annotation> annotations, List<Modifier> mods) {
        var pos = pos();
        accept(ENUM);
        var name = ident();
        var params = classParamsOpt();
        accept(LBRACE);
        var enumConsts = List.<EnumConstDecl>builder();
        while (!isOneOf(RBRACE, SEMICOLON, EOF)) {
            enumConsts.append(enumConst(name));
            if (is(COMMA))
                nextToken();
            else
                break;
        }
        var members = List.<Node>builder();
        if (is(SEMICOLON)) {
            nextToken();
            while (!isOneOf(EOF, RBRACE)) {
                var member = classMember();
                if (member != null)
                    members.append(member);
            }
        }
        accept(RBRACE);
        return new ClassDecl(
                ClassTag.ENUM,
                annotations,
                mods,
                name,
                null,
                List.nil(),
                List.nil(),
                params,
                enumConsts.build(),
                members.build()
        ).setPos(pos);
    }

    private List<ClassParamDecl> classParamsOpt() {
        if (!is(LPAREN))
            return List.of();
        nextToken();
        var params = List.<ClassParamDecl>builder();
        if (!isOneOf(EOF, RPAREN)) {
            for (;;) {
                params.append(classParam());
                if (is(COMMA))
                    nextToken();
                else
                    break;
            }
        }
        accept(RPAREN);
        return params.build();
    }

    private EnumConstDecl enumConst(Name className) {
        var annotations = annotations();
        var pos = pos();
        var name = ident();
        List<Expr> args = List.of();
        if (is(LPAREN))
            args = arguments();
        List<Node> classMembers = is(LBRACE) ? classMembers() : List.of();
        var classDecl = new ClassDecl(
                ClassTag.CLASS, List.nil(), List.nil(),
                Name.from("$" + name),
                null,
                List.of(new Extend(new Call(new Ident(className).setPos(pos), args).setPos(pos)).setPos(pos)),
                List.nil(),
                List.nil(),
                List.nil(),
                classMembers
        ).setPos(pos);
        return new EnumConstDecl(annotations, name, List.of(), classDecl).setPos(pos);
    }

    private List<Node> classMembers() {
        accept(LBRACE);
        var members = List.<Node>builder();
        while (!isOneOf(EOF, RBRACE)) {
            var mem = classMember();
            if (mem != null)
                members.append(mem);
        }
        accept(RBRACE);
        return members.build();
    }

    MethodDecl method(List<Annotation> annotations, List<Modifier> mods) {
        accept(FN);
        var pos = pos();
        var name = ident();
        if (Traces.traceParsing)
            logger.trace("Parsing method {}", name);
        var typeParams = typeParamsOpt();
        var params = params();
        TypeNode retType = null;
        if (is(ARROW)) {
            nextToken();
            retType = type();
        }
        Block block = null;
        if (is(LBRACE))
            block = block();
        return new MethodDecl(
                mods,
                annotations,
                typeParams,
                name,
                params,
                retType,
                block
        ).setPos(pos);
    }

    private Block block() {
        accept(LBRACE);
        var stmts = List.<Stmt>builder();
        while (!is(RBRACE)) {
            stmts.append(stmt());
        }
        accept(RBRACE);
        return new Block(stmts.build());
    }

    Stmt stmt() {
        var stmt = switch (tokenKind()) {
            case RETURN -> retStmt();
            case IF -> ifStmt();
            case WHILE -> whileStmt();
            case DO -> doWhileStmt();
            case FOR -> foreachStmt();
            case THROW -> throwStmt();
            case CONTINUE -> continueStmt();
            case BREAK -> breakStmt();
            case TRY -> tryStmt();
            case DELETE -> deleteStmt();
            case PUB, PRIV, STATIC, INTERFACE, ENUM, CLASS, VAR, VAL, FN -> declStmt();
            case VALUE -> {
                var r = analyzeValue();
                yield switch (r) {
                    case DECL -> declStmt();
                    case IDENT -> exprStmt();
                    case LABEL -> labeledStmt();
                };
            }
            case LBRACE -> new BlockStmt(block());
            case IDENT -> {
                if (peekToken().is(COLON))
                    yield labeledStmt();
                else
                    yield exprStmt();
            }
            default -> exprStmt();
        };
        if (Traces.traceParsing)
            logger.trace("Parsed statement: {}", stmt);
        return stmt;
    }

    private TryStmt tryStmt() {
        accept(TRY);
        var tryBlock = block();
        var catchers = List.<Catcher>builder();
        while (is(CATCH)) {
            nextToken();
            accept(LPAREN);
            var name = ident();
            accept(COLON);
            var type = type();
            accept(RPAREN);
            var v = new LocalVarDecl(type, name, null, false);
            var catchBlock = block();
            catchers.append(new Catcher(v, catchBlock));
        }
        return new TryStmt(tryBlock, catchers.build());
    }

    private DelStmt deleteStmt() {
        accept(DELETE);
        return new DelStmt(expr());
    }

    private ExprStmt exprStmt() {
        return new ExprStmt(expr());
    }

    private LabeledStmt labeledStmt() {
        var label = ident();
        accept(COLON);
        var stmt = stmt();
        return new LabeledStmt(label, stmt);
    }

    enum ValueResult {
        DECL,
        IDENT,
        LABEL
    }

    ValueResult analyzeValue() {
        var it = la(0);
        skipMods(it);
        return it.get().isOneOf(CLASS, ENUM, CLASS, FN, VAR, VAL) ? ValueResult.DECL :
                it.get().is(COLON) ? ValueResult.LABEL : ValueResult.IDENT;
    }

    private void skipMods(Lexer.LaIt it) {
        for (;;) {
            switch (it.get().getKind()) {
                case PUB, PRIV, PROT, VALUE, STATIC, ABSTRACT, DELETED, TEMP -> it.next();
                case AT -> skipAnnotation(it);
                default -> {
                    return;
                }
            }
        }
    }

    private ContinueStmt continueStmt() {
        var ln = line();
        accept(CONTINUE);
        Name label = null;
        if (is(LAX_IDENT) && ln == line())
            label = ident();
        return new ContinueStmt(label);
    }

    private BreakStmt breakStmt() {
        var ln = line();
        accept(BREAK);
        Name label = null;
        if (is(LAX_IDENT) && ln == line())
            label = ident();
        return new BreakStmt(label);
    }

    private DeclStmt declStmt() {
        var annotations = annotations();
        var mods = mods();
        return new DeclStmt(decl(annotations, mods));
    }

    private Decl<?> decl(List<Annotation> annotations, List<Modifier> mods) {
        return switch (tokenKind()) {
            case CLASS -> classDecl(annotations, mods);
            case INTERFACE -> interfaceDecl(annotations, mods);
            case ENUM -> enumDecl(annotations, mods);
            case VAR -> localVar(true);
            case VAL -> localVar(false);
            default -> throw new IllegalStateException("Invalid start of declaration: " + tokenKind());
        };
    }

    private LocalVarDecl localVar(boolean mutable) {
        nextToken();
        var pos = pos();
        var name = ident();
        TypeNode type = null;
        if(is(COLON)) {
            nextToken();
            type = type();
        }
        Expr initial= null;
        if (is(ASSIGN)) {
            nextToken();
            initial = expr();
        }
        return new LocalVarDecl(type, name, initial, mutable).setPos(pos);
    }


    private ThrowStmt throwStmt() {
        accept(THROW);
        return new ThrowStmt(expr());
    }

    private IfStmt ifStmt() {
        accept(IF);
        var cond = parExpr();
        var thenBody = stmt();
        Stmt elseBody = null;
        if (is(ELSE)) {
            nextToken();
            elseBody = stmt();
        }
        return new IfStmt(cond, thenBody, elseBody);
    }

    private WhileStmt whileStmt() {
        accept(WHILE);
        var cond = parExpr();
        var body = stmt();
        return new WhileStmt(cond, body);
    }

    private DoWhileStmt doWhileStmt() {
        accept(DO);
        var body = stmt();
        accept(WHILE);
        var cond = parExpr();
        return new DoWhileStmt(cond, body);
    }

    private ForeachStmt foreachStmt() {
        accept(FOR);
        accept(LPAREN);
        var name = ident();
        TypeNode type = null;
        if (is(COLON)) {
            nextToken();
            type = type();
        }
        accept(IN);
        var iterated = expr();
        accept(RPAREN);
        return new ForeachStmt(
                new LocalVarDecl(type, name, null, false),
                iterated,
                stmt()
        );
    }

    private Expr parExpr() {
        accept(LPAREN);
        var expr = expr();
        accept(RPAREN);
        return expr;
    }

    private RetStmt retStmt() {
        var ln = line();
        var pos = pos();
        accept(RETURN);
        if (isOneOf(EOF, SEMICOLON) || line() != ln)
            return new RetStmt(null).setPos(pos);
        else
            return new RetStmt(expr()).setPos(pos);
    }

    private int line() {
        return line(start());
    }

    private int line(int pos) {
        return lexer.getLine(pos);
    }

    private List<ParamDecl> params() {
        accept(LPAREN);
        var params = List.<ParamDecl>builder();
        if (!is(RPAREN)) {
            for (;;) {
                params.append(param());
                if (is(COMMA))
                    nextToken();
                else
                    break;
            }
        }
        accept(RPAREN);
        return params.build();
    }

    private List<ParamDecl> lambdaParams() {
        accept(LPAREN);
        var params = List.<ParamDecl>builder();
        if (!is(RPAREN)) {
            for (;;) {
                params.append(lambdaParam());
                if (is(COMMA))
                    nextToken();
                else
                    break;
            }
        }
        accept(RPAREN);
        return params.build();
    }

    private ParamDecl param() {
        var annotations = annotations();
        var pos = pos();
        var name = ident();
        accept(COLON);
        var type = type();
        return new ParamDecl(annotations, type, name).setPos(pos);
    }

    private ParamDecl lambdaParam() {
        var annotations = annotations();
        var pos = pos();
        var name = ident();
        TypeNode type = null;
        if (is(COLON)) {
            nextToken();
            type = type();
        }
        return new ParamDecl(annotations, type, name).setPos(pos);
    }

    private FieldDecl field(List<Annotation> annotations, List<Modifier> mods) {
        var mutable = switch (tokenKind()) {
            case VAR -> true;
            case VAL ->  false;
            default -> throw new IllegalStateException("Not a field");
        };
        nextToken();
        var pos = pos();
        var name = ident();
        TypeNode type = null;
        if (is(TokenKind.COLON)) {
            nextToken();
            type = type();
        }
        Expr initial = null;
        if (is(TokenKind.ASSIGN)) {
            nextToken();
            initial = expr();
        }
        return new FieldDecl(
                mods,
                annotations,
                type,
                name,
                initial,
                mutable
        ).setPos(pos);
    }

    private List<Annotation> annotations() {
        var annotations = List.<Annotation>builder();
        while (is(AT)) {
            annotations.append(annotation());
        }
        return annotations.build();
    }

    Annotation annotation() {
        accept(AT);
        var name = ident();
        var attrs = List.<Annotation.Attribute>builder();
        if (is(LPAREN)) {
            nextToken();
            if (!is(RPAREN)) {
                attrs.append(annotationAttr());
                while (is(COMMA)) {
                    nextToken();
                    attrs.append(annotationAttr());
                }
            }
            accept(RPAREN);
        }
        return new Annotation(name, attrs.build());
    }

    private Annotation.Attribute annotationAttr() {
        var first = expr();
        if (is(ASSIGN)) {
            if (first instanceof Ident ident) {
                var name = ident.getName();
                var value = expr();
                return new Annotation.Attribute(name, value);
            }
            else
                throw new CompilationException("Invalid annotation attribute name: " + first.getText());
        }
        else
            return new Annotation.Attribute(NameTable.instance.value, first);
    }

    private List<Expr> arguments() {
        accept(TokenKind.LPAREN);
        if (is(TokenKind.RPAREN)) {
            nextToken();
            return List.of();
        }
        var args = List.<Expr>builder();
        args.append(expr());
        while (is(COMMA)) {
            nextToken();
            args.append(expr());
        }
        accept(RPAREN);
        return args.build();
    }

    TypeNode type() {
        return unionType();
    }

    private TypeNode unionType() {
        var pos = pos();
        var type = intersectionType();
        if (is(BITOR)) {
            var alternatives = List.<TypeNode>builder();
            alternatives.append(type);
            while (is(BITOR)) {
                nextToken();
                alternatives.append(intersectionType());
            }
            var unionType = new UnionTypeNode(alternatives.build());
            unionType.setPos(pos);
            return unionType;
        }
        else
            return type;
    }

    private TypeNode intersectionType() {
        var pos = pos();
        var type = postfixType();
        if (is(BITAND)) {
            var bounds = List.<TypeNode>builder();
            bounds.append(type);
            while (is(BITAND)) {
                nextToken();
                bounds.append(postfixType());
            }
            var intersectType = new IntersectionTypeNode(bounds.build());
            intersectType.setPos(pos);
            return intersectType;
        }
        else
            return type;
    }

    private TypeNode postfixType() {
        var pos = pos();
        var type = atomType();
        for (;;) {
            switch (tokenKind()) {
                case LBRACKET -> {
                    nextToken();
                    accept(RBRACKET);
                    type = new ArrayTypeNode(type);
                    type.setPos(pos);
                }
                case QUES -> {
                    nextToken();
                    type = new UnionTypeNode(List.of(type, new PrimitiveTypeNode(TypeTag.NULL)));
                    type.setPos(pos);
                }
                default -> {
                    return type;
                }
            }
        }
    }

    private TypeNode atomType() {
        var pos = pos();
        var atomType = switch (tokenKind()) {
            case VOID -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.VOID);
            }
            case BOOL -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.BOOL);
            }
            case BYTE -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.BYTE);
            }
            case SHORT -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.SHORT);
            }
            case INT -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.INT);
            }
            case LONG -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.LONG);
            }
            case FLOAT -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.FLOAT);
            }
            case DOUBLE -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.DOUBLE);
            }
            case STRING -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.STRING);
            }
            case NULL -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.NULL);
            }
            case ANY -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.ANY);
            }
            case NEVER -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.NEVER);
            }
            case TIME -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.TIME);
            }
            case PASSWORD -> {
                nextToken();
                yield new PrimitiveTypeNode(TypeTag.PASSWORD);
            }
            case LPAREN -> {
                nextToken();
                if (is(RPAREN)) {
                    nextToken();
                    accept(ARROW);
                    yield new FunctionTypeNode(List.of(), type());
                }
                var type = type();
                if (is(COMMA)) {
                    var paramTypes = List.<TypeNode>builder();
                    paramTypes.append(type);
                    nextToken();
                    paramTypes.append(type());
                    while (is(COMMA)) {
                        nextToken();
                        paramTypes.append(type());
                    }
                    accept(RPAREN);
                    accept(ARROW);
                    var retType = type();
                    yield new FunctionTypeNode(paramTypes.build(), retType);
                }
                else {
                    accept(RPAREN);
                    if (is(ARROW)) {
                        nextToken();
                        var retType = type();
                        yield new FunctionTypeNode(List.of(type), retType);
                    }
                    else
                        yield type;
                }
            }
            case LBRACKET -> {
                nextToken();
                var lb = type();
                accept(COMMA);
                var up = type();
                accept(RBRACKET);
                yield new UncertainTypeNode(lb, up);
            }
            case IDENT -> classType();
            default -> {
                error(Errors.illegalStartOfExpr);
                yield new ErrorTypeNode();
            }
        };
        atomType.setPos(pos);
        return atomType;
    }

    private ClassTypeNode classType() {
        var pos = pos();
        Expr expr = new Ident(ident()).setPos(pos);
        for (; ; ) {
            switch (tokenKind()) {
                case LT -> {
                    var pos1 = pos();
                    expr = new TypeApply(expr, typeArgs()).setPos(pos1);
                }
                case DOT -> {
                    nextToken();
                    var pos1 = pos();
                    var sel = ident();
                    expr = new SelectorExpr(expr, sel).setPos(pos1);
                }
                default -> {
                    return new ClassTypeNode(expr);
                }
            }
        }
    }

    private boolean isError() {
        return token().getEnd() <= lastErrorPos;
    }

    Token token() {
        return lexer.token();
    }

    private ClassParamDecl classParam() {
        var annotations = annotations();
        var mods = mods();
        boolean withField = false;
        boolean mutable = true;
        switch (tokenKind()) {
            case VAL -> {
                nextToken();
                withField = true;
                mutable = false;
            }
            case VAR -> {
                nextToken();
                withField = true;
            }
        }
        var pos = pos();
        var name = ident();
        TypeNode type = null;
        if (is(COLON)) {
            nextToken();
            type = type();
        }
        return new ClassParamDecl(
                mods,
                annotations,
                withField,
                mutable,
                type,
                name
        ).setPos(pos);
    }

    private Name ident() {
        switch (tokenKind()) {
            case IDENT ->  {
                var name = ((NamedToken) token()).getName();
                nextToken();
                return name;
            }
            case VALUE -> {
                nextToken();
                return NameTable.instance.value;
            }
            case PASSWORD -> {
                nextToken();
                return NameTable.instance.password;
            }
            case TIME -> {
                nextToken();
                return NameTable.instance.time;
            }
            default -> {
                accept(TokenKind.IDENT);
                return NameTable.instance.error;
            }
        }
    }

    private void nextToken() {
        lexer.nextToken();
    }

    private Token peekToken() {
        return lexer.peekToken();
    }

    private Token peekToken(Token token) {
        return lexer.peekToken(token);
    }

    private boolean peekToken(Token token, Predicate<TokenKind> filter) {
        var t = peekToken(token);
        return filter.test(t.getKind());
    }

    private boolean peekToken(Token token, Predicate<TokenKind> tk1, Predicate<TokenKind> tk2) {
        var tk = peekToken(token).getKind();
        return tk1.test(tk) || tk2.test(tk);
    }


    private boolean peekToken(Token token, Predicate<TokenKind> tk1, Predicate<TokenKind> tk2, Predicate<TokenKind> tk3) {
        var tk = peekToken(token).getKind();
        return tk1.test(tk) || tk2.test(tk) || tk3.test(tk);
    }

    private void accept(TokenKind tk) {
        if (tokenKind() == tk)
            nextToken();
        else {
            log.error(token().getStart(), Errors.expected(tk));
            lastErrorPos = prevEnd();
        }
    }

    public boolean is(Predicate<TokenKind> tk) {
        return tk.test(tokenKind());
    }

    private boolean isOneOf(TokenKind tk1, TokenKind tk2) {
        return is(tk1) || is(tk2);
    }

    private boolean isOneOf(TokenKind tk1, TokenKind tk2, TokenKind tk3) {
        return is(tk1) || is(tk2) || is(tk3);
    }

    private boolean isOneOf(TokenKind tk1, TokenKind tk2, TokenKind tk3, TokenKind tk4) {
        return is(tk1) || is(tk2) || is(tk3) || is(tk4);
    }

    private boolean isOneOf(TokenKind tk1, TokenKind tk2, TokenKind tk3, TokenKind tk4, TokenKind tk5) {
        return is(tk1) || is(tk2) || is(tk3) || is(tk4) || is(tk5);
    }


    private boolean isOneOf(TokenKind tk1, TokenKind tk2, TokenKind tk3, TokenKind tk4, TokenKind tk5, TokenKind tk6) {
        return is(tk1) || is(tk2) || is(tk3) || is(tk4) || is(tk5) || is(tk6);
    }
    private boolean isOneOf(TokenKind tk1, TokenKind tk2, TokenKind tk3, TokenKind tk4, TokenKind tk5, TokenKind tk6, TokenKind tk7) {
        return is(tk1) || is(tk2) || is(tk3) || is(tk4) || is(tk5) || is(tk6) || is(tk7);
    }
    boolean isEof() {
        return tokenKind() == TokenKind.EOF;
    }

}
