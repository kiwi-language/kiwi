package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.metavm.compiler.element.SymName;
import org.metavm.compiler.element.SymNameTable;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;
import org.metavm.expression.Expressions;

import javax.annotation.Nullable;

import static org.metavm.compiler.antlr.KiwiParser.*;

@Slf4j
public class AstBuilder {

    public static File build(CompilationUnitContext ctx) {
        var pkgDecl = ctx.packageDeclaration() != null ? buildPackageDecl(ctx.packageDeclaration()) : null;
        var imports = List.from(ctx.importDeclaration(), AstBuilder::buildImport);
        var decls = List.from(ctx.typeDeclaration(), AstBuilder::buildClassDecl);
        return new File(pkgDecl, imports, decls);
    }

    private static Import buildImport(ImportDeclarationContext ctx) {
        return new Import((QualifiedName) buildQualifiedName(ctx.qualifiedName()));
    }

    private static PackageDecl buildPackageDecl(PackageDeclarationContext ctx) {
        return new PackageDecl(buildQualifiedName(ctx.qualifiedName()));
    }

    private static ClassDecl buildClassDecl(TypeDeclarationContext ctx) {
        var mods = List.from(ctx.classOrInterfaceModifier(), AstBuilder::buildModifier);
        if (ctx.classDeclaration() != null)
            return buildClassDecl(ctx.classDeclaration(), mods);
        if (ctx.enumDeclaration() != null)
            return buildEnumDecl(ctx.enumDeclaration(), mods);
        if (ctx.interfaceDeclaration() != null)
            return buildInterfaceDecl(ctx.interfaceDeclaration(), mods);
        throw new ParsingException("Invalid type declaration '" + ctx.getText() + "'");
    }

    private static ClassDecl buildClassDecl(ClassDeclarationContext ctx, List<Modifier> mods) {
        List<TypeNode> impls = ctx.typeList() != null ? List.from(ctx.typeList().type(), AstBuilder::buildType) : List.nil();
        var decl = new ClassDecl(
                mods.anyMatch(mod -> mod.tag() == ModifierTag.VALUE) ? ClassTag.VALUE : ClassTag.CLASS,
                List.from(ctx.annotation(), AstBuilder::buildAnnotation),
                mods,
                buildIdent(ctx.identifier()),
                null,
                impls,
                buildTypeVariableDeclList(ctx.typeParameters()),
                List.nil(),
                List.from(ctx.classBody().classBodyDeclaration(), AstBuilder::buildClassMember)
        );
        if (Traces.traceParsing)
            log.trace("{}", decl.getText());
        return decl;
    }

    private static Annotation buildAnnotation(AnnotationContext ctx) {
        List<Annotation.Attribute> attrs;
        if (ctx.expression() != null)
            attrs = List.of(new Annotation.Attribute(SymName.from("value"), buildExpr(ctx.expression())));
        else if (ctx.elementValuePairs() != null) {
            attrs = List.from(
                    ctx.elementValuePairs().elementValuePair(),
                    p -> new Annotation.Attribute(
                            SymName.from(p.identifier().getText()),
                            buildExpr(p.expression())
                    )
            );
        }
        else
            attrs = List.nil();
        return new Annotation(SymName.from(ctx.identifier().getText()), attrs);
    }

    private static ClassDecl buildEnumDecl(EnumDeclarationContext ctx, List<Modifier> mods) {
        return new ClassDecl(
                ClassTag.ENUM,
                List.from(ctx.annotation(), AstBuilder::buildAnnotation),
                mods,
                buildIdent(ctx.identifier()),
                null,
                buildTypeList(ctx.typeList()),
                List.nil(),
                ctx.enumConstants() != null ?
                        List.from(ctx.enumConstants().enumConstant(), AstBuilder::buildEnumConstant) : List.nil(),
                List.from(ctx.enumBodyDeclarations().classBodyDeclaration(), AstBuilder::buildClassMember)
        );
    }

    private static List<TypeVariableDecl> buildTypeVariableDeclList(TypeParametersContext ctx) {
        return ctx != null ?
                List.from(ctx.typeParameter(), AstBuilder::buildTypeVariableDecl) : List.nil();
    }

    private static TypeVariableDecl buildTypeVariableDecl(TypeParameterContext ctx) {
        return new TypeVariableDecl(
                buildIdent(ctx.identifier()),
                ctx.type() != null ? buildType(ctx.type()) : null
        );
    }

    private static ClassDecl buildInterfaceDecl(InterfaceDeclarationContext ctx, List<Modifier> mods) {
        return new ClassDecl(
                ClassTag.INTERFACE,
                List.from(ctx.annotation(), AstBuilder::buildAnnotation),
                mods,
                buildIdent(ctx.identifier()),
                null,
                buildTypeList(ctx.typeList()),
                buildTypeVariableDeclList(ctx.typeParameters()),
                List.nil(),
                List.from(ctx.interfaceBody().interfaceBodyDeclaration(), AstBuilder::buildInterfaceMember)
        );
    }

    private static EnumConstantDecl buildEnumConstant(EnumConstantContext ctx) {
        return new EnumConstantDecl(buildIdent(ctx.identifier()),
                    ctx.arguments() != null && ctx.arguments().expressionList() != null ?
                            List.from(ctx.arguments().expressionList().expression(), AstBuilder::buildExpr) :
                            List.nil()
        );
    }

    private static List<TypeNode> buildTypeList(TypeListContext ctx) {
        if (ctx != null)
            return List.from(ctx.type(), AstBuilder::buildType);
        else
            return List.nil();
    }

    private static Node buildClassMember(ClassBodyDeclarationContext ctx) {
        var mods = List.from(ctx.modifier(), AstBuilder::buildModifier);
        var memberDecl = ctx.memberDeclaration();
        if (memberDecl != null) {
            if (memberDecl.fieldDeclaration() != null)
                return buildFieldDecl(memberDecl.fieldDeclaration(), mods);
            else if (memberDecl.methodDeclaration() != null)
                return buildMethodDecl(memberDecl.methodDeclaration(), mods);
            else if (memberDecl.classDeclaration() != null)
                return buildClassDecl(memberDecl.classDeclaration(), mods);
            else if (memberDecl.constructorDeclaration() != null)
                return buildConstructorDecl(memberDecl.constructorDeclaration(), mods);
        }
        else if (ctx.staticBlock() != null)
            return new ClassInit(buildBlock(ctx.staticBlock().block()));
        throw new ParsingException("Invalid class member declaration: " + ctx.getText());
    }

    private static Node buildInterfaceMember(InterfaceBodyDeclarationContext ctx) {
        var mods = List.from(ctx.modifier(), AstBuilder::buildModifier);
        var memberDecl = ctx.interfaceMemberDeclaration();
        if (memberDecl != null) {
            if (memberDecl.interfaceMethodDeclaration() != null)
                return buildInterfaceMethodDecl(memberDecl.interfaceMethodDeclaration(), mods);
        }
        throw new ParsingException("Invalid class member declaration: " + ctx.getText());
    }



    private static MethodDecl buildMethodDecl(MethodDeclarationContext ctx, List<Modifier> mods) {
        return new MethodDecl(
                mods,
                buildTypeVariableDeclList(ctx.typeParameters()),
                buildIdent(ctx.identifier()),
                buildParams(ctx.formalParameters().formalParameterList()),
                ctx.typeOrVoid() != null ? buildType(ctx.typeOrVoid()) : null,
                ctx.methodBody() != null ? buildBlock(ctx.methodBody().block()) : null
        );
    }

    private static MethodDecl buildInterfaceMethodDecl(InterfaceMethodDeclarationContext ctx, List<Modifier> mods) {
        return new MethodDecl(
                mods,
                buildTypeVariableDeclList(ctx.typeParameters()),
                buildIdent(ctx.identifier()),
                buildParams(ctx.formalParameters().formalParameterList()),
                ctx.typeOrVoid() != null ? buildType(ctx.typeOrVoid()) : null,
                null
        );
    }

    private static MethodDecl buildConstructorDecl(ConstructorDeclarationContext ctx, List<Modifier> mods) {
        return new MethodDecl(
                mods,
                List.nil(),
                new Ident(SymNameTable.instance.init),
                buildParams(ctx.formalParameters().formalParameterList()),
                null,
                ctx.constructorBody != null ? buildBlock(ctx.constructorBody) : null
        );
    }

    private static List<ParamDecl> buildParams(FormalParameterListContext ctx) {
        return ctx != null ?
                List.from(ctx.formalParameter(), AstBuilder::buildParam) :
                List.nil();
    }

    private static FieldDecl buildFieldDecl(FieldDeclarationContext ctx, List<Modifier> mods) {
        return new FieldDecl(
                mods,
                buildType(ctx.type()),
                buildIdent(ctx.identifier())
        );
    }

    private static ParamDecl buildParam(FormalParameterContext ctx) {
        return new ParamDecl(buildType(ctx.type()), buildIdent(ctx.identifier()));
    }

    private static TypeNode buildType(TypeOrVoidContext ctx) {
        if (ctx.VOID() != null)
            return new PrimitiveTypeNode(TypeTag.VOID);
        else
            return buildType(ctx.type());
    }

    private static TypeNode buildType(TypeContext ctx) {
        if (ctx.primitiveType() != null)
            return buildPrimitiveType(ctx.primitiveType());
        if (ctx.ANY() != null)
            return new PrimitiveTypeNode(TypeTag.ANY);
        if (ctx.NEVER() != null)
            return new PrimitiveTypeNode(TypeTag.NEVER);
        if (ctx.classOrInterfaceType() != null)
            return buildClassType(ctx.classOrInterfaceType());
        if (ctx.arrayKind() != null)
            return new ArrayTypeNode(buildType(ctx.type(0)));
        if (!ctx.BITOR().isEmpty())
            return new UnionTypeNode(List.from(ctx.type(), AstBuilder::buildType));
        if (!ctx.BITAND().isEmpty())
            return new IntersectionTypeNode(List.from(ctx.type(), AstBuilder::buildType));
        if (ctx.ARROW() != null) {
            List<TypeNode> paramTypes = ctx.type().size() > 1 ?
                    List.from(ctx.type().subList(0, ctx.type().size() - 1), AstBuilder::buildType) : List.nil();
            var returnType = buildType(ctx.type().getLast());
            return new FunctionTypeNode(paramTypes, returnType);
        }
        if (ctx.LBRACK() != null)
            return new UncertainTypeNode(buildType(ctx.type(0)), buildType(ctx.type(1)));
        throw new ParsingException("Invalid type expression: " + ctx.getText());
    }

    private static TypeNode buildPrimitiveType(PrimitiveTypeContext ctx) {
        if (ctx.BYTE() != null)
            return new PrimitiveTypeNode(TypeTag.BYTE);
        if (ctx.SHORT() != null)
            return new PrimitiveTypeNode(TypeTag.SHORT);
        if (ctx.INT() != null)
            return new PrimitiveTypeNode(TypeTag.INT);
        if (ctx.LONG() != null)
            return new PrimitiveTypeNode(TypeTag.LONG);
        if (ctx.DOUBLE() != null)
            return new PrimitiveTypeNode(TypeTag.DOUBLE);
        if (ctx.CHAR() != null)
            return new PrimitiveTypeNode(TypeTag.CHAR);
        if (ctx.BOOLEAN() != null)
            return new PrimitiveTypeNode(TypeTag.BOOLEAN);
        if (ctx.NULL() != null)
            return new PrimitiveTypeNode(TypeTag.NULL);
        if (ctx.STRING() != null)
            return new PrimitiveTypeNode(TypeTag.STRING);
        if (ctx.TIME() != null)
            return new PrimitiveTypeNode(TypeTag.TIME);
        if (ctx.PASSWORD() != null)
            return new PrimitiveTypeNode(TypeTag.PASSWORD);
        throw new ParsingException("Unrecognized primitive type: " + ctx.getText());
    }

    private static TypeNode buildClassType(ClassOrInterfaceTypeContext ctx) {
        Node expr = buildQualifiedName(ctx.qualifiedName());
        if (ctx.typeArguments() != null) {
            expr = new TypeApply(
                    (Name) expr,
                    List.from(ctx.typeArguments().type(), AstBuilder::buildType)
            );
        }
        return new ClassTypeNode(expr);
    }

    private static Ident buildIdent(IdentifierContext node) {
        return new Ident(getSymName(node.getText()));
    }

    private static Modifier buildModifier(ClassOrInterfaceModifierContext ctx) {
        return Modifier.fromText(ctx.getText());
    }

    private static BlockStmt buildBlockStmt(BlockContext ctx) {
        return new BlockStmt(buildBlock(ctx));
    }

    private static Block buildBlock(BlockContext ctx) {
        return new Block(List.from(ctx.statement(), AstBuilder::buildStmt));
    }

    private static Stmt buildStmt(StatementContext ctx) {
        if (ctx.WHILE() != null)
            return new WhileStmt(buildExpr(ctx.parExpression().expression()), buildBlockStmt(ctx.block(0)));
        if (ctx.IF() != null) {
            return new IfStmt(
                    buildExpr(ctx.parExpression().expression()), buildBlockStmt(ctx.block(0)),
                    ctx.ELSE() != null ? buildBlockStmt(ctx.block(1)) : null
            );
        }
        if (ctx.RETURN() != null)
            return new ReturnStmt(ctx.expression() == null ? null : buildExpr(ctx.expression()));
        if (ctx.statementExpression != null)
            return new ExprStmt(buildExpr(ctx.statementExpression));
        if (ctx.localVariableDeclaration() != null) {
            var declCtx = ctx.localVariableDeclaration();
            var name = buildIdent(declCtx.identifier());
            var decl = new LocalVarDecl(declCtx.type() != null ? buildType(declCtx.type()) : null,  name,
                    declCtx.expression() != null ? buildExpr(declCtx.expression()) : null);
            return new DeclStmt(decl);
        }
        if (ctx.THROW() != null)
            return new ThrowStmt(buildExpr(ctx.expression()));
        if (ctx.getText().equals(";"))
            return new EmptyStmt();
        throw new ParsingException("Invalid statement: " + ctx.getText());
    }

    private static Modifier buildModifier(ModifierContext ctx) {
        return Modifier.fromText(ctx.getText());
    }

    private static Expr buildExpr(ExpressionContext ctx) {
        return buildAssignment(ctx.assignment());
    }

    private static Expr buildAssignment(AssignmentContext ctx) {
        var expr = buildTernary(ctx.ternary());
        for (var suffix : ctx.assignmentSuffix()) {
            var op = parseAssignOp(suffix.op);
            var rhs = buildAssignment(suffix.assignment());
            expr = new AssignExpr(op, expr, rhs);
        }
        return expr;
    }

    private static Expr buildTernary(TernaryContext ctx) {
        var expr = buildDisjunction(ctx.disjunction(0));
        if (ctx.QUESTION() != null) {
            expr = new CondExpr(
                    expr,
                    buildDisjunction(ctx.disjunction(1)),
                    buildTernary(ctx.ternary())
            );
        }
        return expr;
    }

    private static Expr buildDisjunction(DisjunctionContext ctx) {
        var it = ctx.conjunction().iterator();
        var expr = buildConjunction(it.next());
        while (it.hasNext()) {
            expr = new BinaryExpr(BinOp.AND, expr, buildConjunction(it.next()));
        }
        return expr;
    }

    private static Expr buildConjunction(ConjunctionContext ctx) {
        var it = ctx.bitor().iterator();
        var expr = buildBitor(it.next());
        while (it.hasNext()) {
            expr = new BinaryExpr(BinOp.AND, expr, buildBitor(it.next()));
        }
        return expr;
    }

    private static Expr buildBitor(BitorContext ctx) {
        var it = ctx.bitand().iterator();
        var expr = buildBitand(it.next());
        while (it.hasNext()) {
            expr = new BinaryExpr(BinOp.BIT_OR, expr, buildBitand(it.next()));
        }
        return expr;
    }

    private static Expr buildBitand(BitandContext ctx) {
        var it = ctx.bitxor().iterator();
        var expr = buildBitxor(it.next());
        while (it.hasNext()) {
            expr = new BinaryExpr(BinOp.BIT_AND, expr, buildBitxor(it.next()));
        }
        return expr;
    }

    private static Expr buildBitxor(BitxorContext ctx) {
        var it = ctx.equality().iterator();
        var expr = buildEquality(it.next());
        while (it.hasNext()) {
            expr = new BinaryExpr(BinOp.BIT_XOR, expr, buildEquality(it.next()));
        }
        return expr;
    }

    private static Expr buildEquality(EqualityContext ctx) {
        var expr = buildRelational(ctx.relational());
        for (var suffix : ctx.equalitySuffix()) {
            expr = new BinaryExpr(parseBinOp(suffix.op), expr, buildRelational(suffix.relational()));
        }
        return expr;
    }

    private static Expr buildRelational(RelationalContext ctx) {
        var expr = buildIsExpr(ctx.isExpr());
        for (var suffix : ctx.relationalSuffix()) {
            expr = new BinaryExpr(parseBinOp(suffix.op), expr, buildIsExpr(suffix.isExpr()));
        }
        return expr;
    }

    private static Expr buildIsExpr(IsExprContext ctx) {
        var expr = buildShift(ctx.shift());
        for (var type : ctx.type()) {
            expr = new IsExpr(expr, buildType(type));
        }
        return expr;
    }

    private static Expr buildShift(ShiftContext ctx) {
        var expr = buildAdditive(ctx.additive());
        for (var suffix : ctx.shiftSuffix()) {
            BinOp op;
            if (suffix.LT().size() == 2)
                op = BinOp.SHL;
            else if (suffix.GT().size() == 2)
                op = BinOp.SHR;
            else if (suffix.GT().size() == 3)
                op = BinOp.USHR;
            else
                throw new ParsingException("Invalid shift expression: " + ctx.getText());
            expr = new BinaryExpr(op, expr, buildAdditive(suffix.additive()));
        }
        return expr;
    }

    private static Expr buildAdditive(AdditiveContext ctx) {
        var expr = buildMultiplicative(ctx.multiplicative());
        for (var suffix : ctx.additiveSuffix()) {
            expr = new BinaryExpr(parseBinOp(suffix.op), expr, buildMultiplicative(suffix.multiplicative()));
        }
        return expr;
    }

    private static Expr buildMultiplicative(MultiplicativeContext ctx) {
        var expr = buildAsExpr(ctx.asExpr());
        for (var suffix : ctx.multiplicativeSuffix()) {
            expr = new BinaryExpr(parseBinOp(suffix.op), expr, buildAsExpr(suffix.asExpr()));
        }
        return expr;
    }

    private static Expr buildAsExpr(AsExprContext ctx) {
        var expr = buildPrefixExpr(ctx.prefixExpr());
        for (var type : ctx.type()) {
            expr = new CastExpr(buildType(type), expr);
        }
        return expr;
    }

    private static Expr buildPrefixExpr(PrefixExprContext ctx) {
        var expr = buildPostfixExpr(ctx.postfixExpr());
        var it = ctx.prefixOp().listIterator(ctx.prefixOp().size());
        while (it.hasPrevious()) {
            expr = new PrefixExpr(parsePrefixOp(it.previous().op), expr);
        }
        return expr;
    }

    private static Expr buildPostfixExpr(PostfixExprContext ctx) {
        var expr = buildPrimaryExpr(ctx.primaryExpr());
        for (var suffix : ctx.postfixSuffix()) {
            if (suffix.op != null)
                expr = new PostfixExpr(parsePostfixOp(suffix.op), expr);
            else if (suffix.callSuffix() != null) {
                expr = new CallExpr(
                        expr,
                        buildTypeArguments(suffix.callSuffix().typeArguments()),
                        buildExprList(suffix.callSuffix().arguments().expressionList())
                );
            }
            else if (suffix.newExpr() != null)
                expr = buildNew(suffix.newExpr(), expr);
            else if (suffix.indexingSuffix() != null)
                expr = new IndexExpr(expr, buildExpr(suffix.indexingSuffix().expression()));
            else if (suffix.selectorSuffix() != null)
                expr = new SelectorExpr(expr, buildIdent(suffix.selectorSuffix().identifier()));
            else
                throw new ParsingException("Invalid postfix expression: " + ctx.getText());
        }
        return expr;
    }

    private static Expr buildPrimaryExpr(PrimaryExprContext ctx) {
        if (ctx.LPAREN() != null)
            return buildExpr(ctx.expression());
        else if (ctx.THIS() != null)
            return new RefExpr(new Ident(SymName.this_()));
        else if (ctx.SUPER() != null)
            return new RefExpr(new Ident(SymName.super_()));
        else if (ctx.newExpr() != null)
            return buildNew(ctx.newExpr(), null);
        else if (ctx.newArray() != null)
            return buildNewArray(ctx.newArray());
        else if (ctx.literal() != null)
            return buildLiteral(ctx.literal());
        else if (ctx.identifier() != null)
            return new RefExpr(buildIdent(ctx.identifier()));
        else if (ctx.lambdaExpression() != null)
            return buildLambdaExpr(ctx.lambdaExpression());
        else
            throw new ParsingException("Invalid primary expression: " + ctx.getText());
    }

    private static LambdaExpr buildLambdaExpr(LambdaExpressionContext ctx) {
        return new LambdaExpr(
                buildParams(ctx.lambdaParameters().formalParameterList()),
                buildType(ctx.typeOrVoid()),
                buildBlock(ctx.lambdaBody().block())
        );
    }

    private static List<TypeNode> buildTypeArguments(@Nullable TypeArgumentsContext ctx) {
        return ctx != null ? List.from(ctx.type(), AstBuilder::buildType) : List.nil();
    }

    private static List<Expr> buildExprList(@Nullable ExpressionListContext ctx) {
        return ctx != null ? List.from(ctx.expression(), AstBuilder::buildExpr) : List.nil();
    }

    private static Expr buildNew(NewExprContext ctx, @Nullable Expr owner) {
        return new NewExpr(
                owner,
                buildClassType(ctx.classOrInterfaceType()),
                ctx.arguments().expressionList() != null ?
                        List.from(ctx.arguments().expressionList().expression(), AstBuilder::buildExpr) : List.nil()
        );
    }

    private static Expr buildNewArray(NewArrayContext ctx) {
        return new NewArrayExpr(
                buildType(ctx.type()),
                ctx.arrayKind().R() != null,
                List.nil()
        );
    }

    private static PrefixOp parsePrefixOp(Token token) {
        return switch (token.getType()) {
            case ADD -> PrefixOp.POS;
            case SUB -> PrefixOp.NEGATE;
            case INC ->  PrefixOp.INC;
            case DEC -> PrefixOp.DEC;
            case BANG -> PrefixOp.NOT;
            case TILDE -> PrefixOp.BIT_NOT;
            default -> throw new ParsingException("Invalid operator: " + token.getText());
        };
    }

    private static PostfixOp parsePostfixOp(Token token) {
        return switch (token.getType()) {
            case INC ->  PostfixOp.INC;
            case DEC -> PostfixOp.DEC;
            case BANGBANG -> PostfixOp.NONNULL;
            default -> throw new ParsingException("Invalid operator: " + token.getText());
        };
    }

    private static BinOp parseBinOp(Token token) {
        return switch (token.getType()) {
            case ADD -> BinOp.ADD;
            case SUB -> BinOp.SUB;
            case MUL ->  BinOp.MUL;
            case DIV -> BinOp.DIV;
            case MOD -> BinOp.MOD;
            case BITAND -> BinOp.BIT_AND;
            case BITOR -> BinOp.BIT_OR;
            case CARET -> BinOp.BIT_XOR;
            case LT ->  BinOp.LT;
            case LE -> BinOp.LE;
            case GT -> BinOp.GT;
            case GE -> BinOp.GE;
            case EQUAL -> BinOp.EQ;
            case NOTEQUAL -> BinOp.NE;
            default -> throw new ParsingException("Invalid operator: " + token.getText());
        };
    }

    private static @Nullable BinOp parseAssignOp(Token token) {
        return switch (token.getType()) {
            case ASSIGN -> null;
            case ADD_ASSIGN -> BinOp.ADD;
            case SUB_ASSIGN -> BinOp.SUB;
            case MUL_ASSIGN -> BinOp.MUL;
            case DIV_ASSIGN -> BinOp.DIV;
            case AND_ASSIGN -> BinOp.BIT_AND;
            case OR_ASSIGN -> BinOp.BIT_OR;
            case XOR_ASSIGN -> BinOp.BIT_XOR;
            case LSHIFT_ASSIGN -> BinOp.SHL;
            case RSHIFT_ASSIGN -> BinOp.SHR;
            case URSHIFT_ASSIGN -> BinOp.USHR;
            default -> throw new ParsingException("Invalid operator: " + token.getText());
        };
    }

    private static Literal buildLiteral(LiteralContext ctx) {
        Object value;
        if (ctx.BOOL_LITERAL() != null)
            value = Boolean.valueOf(ctx.getText());
        else if (ctx.STRING_LITERAL() != null)
            value = Expressions.deEscapeDoubleQuoted(ctx.getText());
        else if (ctx.integerLiteral() != null)
            value = parseInt(ctx);
        else if (ctx.floatLiteral() != null)
            value = parseFloat(ctx.floatLiteral());
        else if (ctx.NULL() != null)
            value = null;
        else
            throw new ParsingException("Invalid literal: " + ctx.getText());
        return new Literal(value);
    }

    private static Object parseInt(LiteralContext ctx) {
        var text = ctx.getText();
        var intLit = ctx.integerLiteral();
        int radix;
        if (intLit.BINARY_LITERAL() != null) {
            radix = 2;
            text = text.substring(2);
        }
        else if (intLit.OCT_LITERAL() != null) {
            radix = 8;
            text = text.substring(2);
        }
        else if (intLit.DECIMAL_LITERAL() != null)
            radix = 10;
        else if (intLit.DECIMAL_LITERAL() != null) {
            radix = 16;
            text = text.substring(2);
        }
        else
            throw new ParsingException("Invalid integer literal: " + text);
        var l = text.endsWith("l") || text.endsWith("L");
        if (l) {
            text = text.substring(0, text.length() -1);
            return Long.parseLong(text, radix);
        }
        else
            return Integer.parseInt(text, radix);
    }

    private static Object parseFloat(FloatLiteralContext ctx) {
        var text = ctx.getText();
        if (text.endsWith("f") || text.endsWith("F"))
            return Float.parseFloat(text);
        else
            return Double.parseDouble(text);
    }

    private static Name buildQualifiedName(QualifiedNameContext ctx) {
        var it = ctx.identifier().iterator();
        Name name = buildIdent(it.next());
        while (it.hasNext()) {
            name = new QualifiedName(name, buildIdent(it.next()));
        }
        return name;
    }

    private static SymName getSymName(String text) {
        return SymNameTable.instance.get(text);
    }

}
