package org.metavm.compiler.syntax;

import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.Token;
import org.metavm.compiler.element.Name;
import org.metavm.compiler.element.NameTable;
import org.metavm.compiler.util.List;
import org.metavm.compiler.util.Traces;
import org.metavm.expression.Expressions;
import org.metavm.util.Utils;

import javax.annotation.Nullable;

import static org.metavm.compiler.antlr.KiwiParser.*;

@Slf4j
public class AstBuilder {

    public static File build(CompilationUnitContext ctx) {
        var pkgDecl = ctx.packageDeclaration() != null ? buildPackageDecl(ctx.packageDeclaration()) : null;
        var imports = List.from(ctx.importDeclaration(), AstBuilder::buildImport);
        var decls = List.from(ctx.topLevTypeDecl(), decl -> buildClassDecl(decl.typeDeclaration(),
                List.from(decl.classOrInterfaceModifier(), AstBuilder::buildModifier)));
        return new File(pkgDecl, imports, decls);
    }

    private static Import buildImport(ImportDeclarationContext ctx) {
        return new Import((SelectorExpr) buildQualifiedName(ctx.qualifiedName()));
    }

    private static PackageDecl buildPackageDecl(PackageDeclarationContext ctx) {
        return new PackageDecl(buildQualifiedName(ctx.qualifiedName()));
    }

    private static ClassDecl buildClassDecl(TypeDeclarationContext ctx, List<Modifier> mods) {
        if (ctx.classDeclaration() != null)
            return buildClassDecl(ctx.classDeclaration(), mods);
        if (ctx.enumDeclaration() != null)
            return buildEnumDecl(ctx.enumDeclaration(), mods);
        if (ctx.interfaceDeclaration() != null)
            return buildInterfaceDecl(ctx.interfaceDeclaration(), mods);
        throw new ParsingException("Invalid type declaration '" + ctx.getText() + "'");
    }

    private static ClassDecl buildClassDecl(ClassDeclarationContext ctx, List<Modifier> mods) {
        var impls = List.<Extend>builder();
        var typeIt = ctx.type().iterator();
        if (ctx.arguments() != null) {
            impls.append(new Extend(buildType(typeIt.next()), buildExprList(ctx.arguments().expressionList())));
        }
        while (typeIt.hasNext())
            impls.append(new Extend(buildType(typeIt.next())));
        var decl = new ClassDecl(
                mods.anyMatch(mod -> mod.tag() == ModifierTag.VALUE) ? ClassTag.VALUE : ClassTag.CLASS,
                List.from(ctx.annotation(), AstBuilder::buildAnnotation),
                mods,
                ctx.BEAN() != null,
                buildName(ctx.identifier()),
                null,
                impls.build(),
                buildTypeVariableDeclList(ctx.typeParameters()),
                List.from(ctx.initParameter(), AstBuilder::buildClassParam),
                List.nil(),
                ctx.classBody() != null ?
                    List.from(ctx.classBody().classBodyDeclaration(), AstBuilder::buildClassMember) : List.nil()
        );
        if (Traces.traceParsing)
            log.trace("{}", decl.getText());
        return decl;
    }

    private static ClassParamDecl buildClassParam(InitParameterContext ctx) {
        var fieldDeclCtx = ctx.fieldDeclaration();
        if (fieldDeclCtx != null) {
            return new ClassParamDecl(
                    List.from(ctx.modifier(), AstBuilder::buildModifier),
                    true,
                    fieldDeclCtx.VAL() != null,
                    buildType(fieldDeclCtx.type()),
                    buildName(fieldDeclCtx.identifier())
            );
        }
        else {
            var paramDeclCtx = ctx.formalParameter();
            return new ClassParamDecl(
                    List.nil(),
                    false,
                    false,
                    buildType(paramDeclCtx.type()),
                    buildName(paramDeclCtx.identifier())
            );
        }
    }

    private static Annotation buildAnnotation(AnnotationContext ctx) {
        List<Annotation.Attribute> attrs;
        if (ctx.expression() != null)
            attrs = List.of(new Annotation.Attribute(Name.from("value"), buildExpr(ctx.expression())));
        else if (ctx.elementValuePairs() != null) {
            attrs = List.from(
                    ctx.elementValuePairs().elementValuePair(),
                    p -> new Annotation.Attribute(
                            Name.from(p.identifier().getText()),
                            buildExpr(p.expression())
                    )
            );
        }
        else
            attrs = List.nil();
        return new Annotation(Name.from(ctx.identifier().getText()), attrs);
    }

    private static ClassDecl buildEnumDecl(EnumDeclarationContext ctx, List<Modifier> mods) {
        var name = buildName(ctx.identifier());
        return new ClassDecl(
                ClassTag.ENUM,
                List.from(ctx.annotation(), AstBuilder::buildAnnotation),
                mods,
                false,
                name,
                null,
                buildExtends(ctx.typeList()),
                List.nil(),
                List.from(ctx.initParameter(), AstBuilder::buildClassParam),
                ctx.enumConstants() != null ?
                        List.from(ctx.enumConstants().enumConstant(), ec -> buildEnumConstant(ec, name)) : List.nil(),
                List.from(ctx.enumBodyDeclarations().classBodyDeclaration(), AstBuilder::buildClassMember)
        );
    }

    private static List<TypeVariableDecl> buildTypeVariableDeclList(TypeParametersContext ctx) {
        return ctx != null ?
                List.from(ctx.typeParameter(), AstBuilder::buildTypeVariableDecl) : List.nil();
    }

    private static TypeVariableDecl buildTypeVariableDecl(TypeParameterContext ctx) {
        return new TypeVariableDecl(
                buildName(ctx.identifier()),
                ctx.type() != null ? buildType(ctx.type()) : null
        );
    }

    private static ClassDecl buildInterfaceDecl(InterfaceDeclarationContext ctx, List<Modifier> mods) {
        return new ClassDecl(
                ClassTag.INTERFACE,
                List.from(ctx.annotation(), AstBuilder::buildAnnotation),
                mods,
                false,
                buildName(ctx.identifier()),
                null,
                buildExtends(ctx.typeList()),
                buildTypeVariableDeclList(ctx.typeParameters()),
                List.nil(),
                List.nil(),
                List.from(ctx.interfaceBody().interfaceBodyDeclaration(), AstBuilder::buildInterfaceMember)
        );
    }

    private static EnumConstDecl buildEnumConstant(EnumConstantContext ctx, Name className) {
        var name = buildName(ctx.identifier());
        List<Expr> args = ctx.arguments() != null ? buildExprList(ctx.arguments().expressionList()) : List.nil();
        if (ctx.classBody() == null) {
            return new EnumConstDecl(name, args, null);
        }
        else {
            return new EnumConstDecl(name,
                    List.nil(),
                    buildAnonymousClass(ctx.classBody(), args, new ClassTypeNode(new Ident(className)))
            );
        }
    }

    private static List<Extend> buildExtends(TypeListContext ctx) {
        if (ctx != null)
            return List.from(ctx.type(), t -> new Extend(buildType(t)));
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
            else if (memberDecl.typeDeclaration() != null)
                return buildClassDecl(memberDecl.typeDeclaration(), mods);
            else if (memberDecl.block() != null)
                return buildInit(memberDecl.block(), mods);
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
                buildName(ctx.identifier()),
                buildParams(ctx.formalParameters().formalParameterList()),
                ctx.typeOrVoid() != null ? buildType(ctx.typeOrVoid()) : null,
                ctx.methodBody() != null ? buildBlock(ctx.methodBody().block()) : null
        );
    }

    private static MethodDecl buildInterfaceMethodDecl(InterfaceMethodDeclarationContext ctx, List<Modifier> mods) {
        return new MethodDecl(
                mods,
                buildTypeVariableDeclList(ctx.typeParameters()),
                buildName(ctx.identifier()),
                buildParams(ctx.formalParameters().formalParameterList()),
                ctx.typeOrVoid() != null ? buildType(ctx.typeOrVoid()) : null,
                null
        );
    }

    private static Init buildInit(BlockContext ctx, List<Modifier> mods) {
        return new Init(buildBlock(ctx));
    }

    private static List<ParamDecl> buildParams(FormalParameterListContext ctx) {
        return ctx != null ?
                List.from(ctx.formalParameter(), AstBuilder::buildParam) :
                List.nil();
    }

    private static List<ParamDecl> buildLambdaParams(LambdaParametersContext ctx) {
        if (ctx.identifier() != null)
            return List.of(new ParamDecl(null, buildName(ctx.identifier())));
        else if (ctx.lambdaParameterList() != null)
            return List.from(ctx.lambdaParameterList().lambdaParameter(), AstBuilder::buildLambdaParam);
        else
            return List.of();
    }

    private static FieldDecl buildFieldDecl(FieldDeclarationContext ctx, List<Modifier> mods) {
        return new FieldDecl(
                mods,
                Utils.safeCall(ctx.type(), AstBuilder::buildType),
                buildName(ctx.identifier()),
                Utils.safeCall(ctx.expression(), AstBuilder::buildExpr)
        );
    }

    private static ParamDecl buildParam(FormalParameterContext ctx) {
        return new ParamDecl(buildType(ctx.type()), buildName(ctx.identifier()));
    }

    private static ParamDecl buildLambdaParam(LambdaParameterContext ctx) {
        return new ParamDecl(Utils.safeCall(ctx.type(), AstBuilder::buildType), buildName(ctx.identifier()));
    }

    private static TypeNode buildType(TypeOrVoidContext ctx) {
        if (ctx.VOID() != null)
            return new PrimitiveTypeNode(TypeTag.VOID);
        else
            return buildType(ctx.type());
    }

    private static TypeNode buildType(TypeContext ctx) {
        return buildUnionType(ctx.unionType());
    }

    private static TypeNode buildUnionType(UnionTypeContext ctx) {
        var it = ctx.intersectionType().iterator();
        var type = buildIntersectionType(it.next());
        if (it.hasNext()) {
            var alternatives = List.<TypeNode>builder();
            alternatives.append(type);
            while (it.hasNext()) {
                alternatives.append(buildIntersectionType(it.next()));
            }
            return new UnionTypeNode(alternatives.build());
        }
        else
            return type;
    }

    private static TypeNode buildIntersectionType(IntersectionTypeContext ctx) {
        var it = ctx.postfixType().iterator();
        var type = buildPostfixType(it.next());
        if (it.hasNext()) {
            var alternatives = List.<TypeNode>builder();
            alternatives.append(type);
            while (it.hasNext()) {
                alternatives.append(buildPostfixType(it.next()));
            }
            return new IntersectionTypeNode(alternatives.build());
        }
        else
            return type;
    }

    private static TypeNode buildPostfixType(PostfixTypeContext ctx) {
        var type = buildAtomicType(ctx.atomicType());
        for (var suffix : ctx.typeSuffix()) {
            if (suffix.QUESTION() != null)
                type = new UnionTypeNode(List.of(type, new PrimitiveTypeNode(TypeTag.NULL)));
            else if (suffix.arrayKind() != null)
                type = new ArrayTypeNode(type);
            else
                throw new ParsingException("Invalid type suffix: " + suffix.getText());
        }
        return type;
    }

    private static TypeNode buildAtomicType(AtomicTypeContext ctx) {
        if (ctx.primitiveType() != null)
            return buildPrimitiveType(ctx.primitiveType());
        if (ctx.classType() != null)
            return buildClassType(ctx.classType());
        if (ctx.functionType() != null)
            return buildFunctionType(ctx.functionType());
        if (ctx.uncertainType() != null)
            return buildUncertainType(ctx.uncertainType());
        throw new ParsingException("Invalid type: " + ctx.getText());
    }

    private static TypeNode buildUncertainType(UncertainTypeContext ctx) {
        return new UncertainTypeNode(buildType(ctx.type(0)), buildType(ctx.type(1)));
    }

    private static TypeNode buildFunctionType(FunctionTypeContext ctx) {
        var it = ctx.type().iterator();
        var paramTypes = List.<TypeNode>builder();
        if (!it.hasNext())
            throw new ParsingException("Invalid function type: " + ctx.getText());
        for(;;) {
            var type = buildType(it.next());
            if (it.hasNext())
                paramTypes.append(type);
            else
                return new FunctionTypeNode(paramTypes.build(), type);
        }
    }

    private static TypeNode buildPrimitiveType(PrimitiveTypeContext ctx) {
        return new PrimitiveTypeNode(TypeTag.valueOf(ctx.getText().toUpperCase()));
    }

    private static TypeNode buildClassType(ClassTypeContext ctx) {
        var it = ctx.classTypePart().iterator();
        var firstPart = it.next();
        Expr expr = new Ident(buildName(firstPart.identifier()));
        if (firstPart.typeArguments() != null)
            expr = new TypeApply(expr, buildTypeArguments(firstPart.typeArguments()));
        while (it.hasNext()) {
            var part = it.next();
            expr = new SelectorExpr(expr, buildName(part.identifier()));
            if (part.typeArguments() != null) {
                expr = new TypeApply(expr, buildTypeArguments(part.typeArguments()));
            }
        }
        return new ClassTypeNode(expr);
    }

    private static Name buildName(IdentifierContext node) {
        return getSymName(node.getText());
    }

    private static Modifier buildModifier(ClassOrInterfaceModifierContext ctx) {
        return Modifier.fromText(ctx.getText());
    }

    private static Block buildBlock(BlockContext ctx) {
        return new Block(List.from(ctx.statement(), AstBuilder::buildStmt));
    }

    private static Stmt buildStmt(StatementContext ctx) {
        if (ctx.DO() != null)
            return new DoWhileStmt(buildExpr(ctx.parExpression().expression()), buildStmt(ctx.statement(0)));
        if (ctx.WHILE() != null)
            return new WhileStmt(buildExpr(ctx.parExpression().expression()), buildStmt(ctx.statement(0)));
        if (ctx.IF() != null) {
            return new IfStmt(
                    buildExpr(ctx.parExpression().expression()), buildStmt(ctx.statement(0)),
                    ctx.ELSE() != null ? buildStmt(ctx.statement(1)) : null
            );
        }
        if (ctx.TRY() != null) {
            return new TryStmt(
                    buildBlock(ctx.block()),
                    List.from(ctx.catchClause(), AstBuilder::buildCatcher)
            );
        }
        if (ctx.FOR() != null) {
            var forCtl = ctx.forControl();
            var loopVar = forCtl.loopVariable();
            return new ForeachStmt(
                    new LocalVarDecl(
                            Utils.safeCall(loopVar.type(), AstBuilder::buildType),
                            buildName(loopVar.identifier()),
                            null
                    ),
                    buildExpr(forCtl.expression()),
                    buildStmt(ctx.statement(0))
            );
        }
        if (ctx.RETURN() != null)
            return new RetStmt(ctx.expression() == null ? null : buildExpr(ctx.expression()));
        if (ctx.statementExpression != null)
            return new ExprStmt(buildExpr(ctx.statementExpression));
        if (ctx.localVariableDeclaration() != null) {
            var declCtx = ctx.localVariableDeclaration();
            var name = buildName(declCtx.identifier());
            var decl = new LocalVarDecl(declCtx.type() != null ? buildType(declCtx.type()) : null,  name,
                    declCtx.expression() != null ? buildExpr(declCtx.expression()) : null);
            return new DeclStmt(decl);
        }
        if (ctx.THROW() != null)
            return new ThrowStmt(buildExpr(ctx.expression()));
        if (ctx.getText().equals(";"))
            return new EmptyStmt();
        if (ctx.block() != null)
            return new BlockStmt(buildBlock(ctx.block()));
        if (ctx.BREAK() != null)
            return new BreakStmt(Utils.safeCall(ctx.identifier(), AstBuilder::buildName));
        if (ctx.CONTINUE() != null)
            return new ContinueStmt(Utils.safeCall(ctx.identifier(), AstBuilder::buildName));
        if (ctx.COLON() != null)
            return new LabeledStmt(buildName(ctx.identifier()), buildStmt(ctx.statement(0)));
        throw new ParsingException("Invalid statement: " + ctx.getText());
    }

    private static Catcher buildCatcher(CatchClauseContext ctx) {
        return new Catcher(
                new LocalVarDecl(buildType(ctx.type()), buildName(ctx.identifier()), null),
                buildBlock(ctx.block())
        );
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
            expr = new BinaryExpr(BinOp.OR, expr, buildConjunction(it.next()));
        }
        return expr;
    }

    private static Expr buildConjunction(ConjunctionContext ctx) {
        var it = ctx.range().iterator();
        var expr = buildRange(it.next());
        while (it.hasNext()) {
            expr = new BinaryExpr(BinOp.AND, expr, buildRange(it.next()));
        }
        return expr;
    }

    private static Expr buildRange(RangeContext ctx) {
        var expr = buildBitor(ctx.bitor(0));
        if (ctx.ELLIPSIS() != null)
            expr = new RangeExpr(expr, buildBitor(ctx.bitor(1)));
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
        for (var sfx : ctx.isSuffix()) {
            if (sfx.type() != null)
                expr = new IsExpr(expr, buildType(sfx.type()), null);
            else {
                var type = buildType(sfx.typePtn().type());
                var var = new LocalVarDecl(type, buildName(sfx.typePtn().identifier()), null);
                expr = new IsExpr(expr, type, var);
            }
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
                expr = new Call(
                        expr,
                        buildExprList(suffix.callSuffix().arguments().expressionList())
                );
            }
            else if (suffix.indexingSuffix() != null)
                expr = new IndexExpr(expr, buildExpr(suffix.indexingSuffix().expression()));
            else if (suffix.selectorSuffix() != null) {
                var ident = suffix.selectorSuffix().THIS() != null ?
                        Name.this_() :
                        buildName(suffix.selectorSuffix().identifier());
                expr = new SelectorExpr(expr, ident);
            } else if (suffix.typeArguments() != null)
              expr = new TypeApply(expr, buildTypeArguments(suffix.typeArguments()));
            else
                throw new ParsingException("Invalid postfix expression: " + ctx.getText());
        }
        return expr;
    }

    private static Expr buildPrimaryExpr(PrimaryExprContext ctx) {
        if (ctx.LPAREN() != null)
            return buildExpr(ctx.expression());
        else if (ctx.THIS() != null)
            return new Ident(Name.this_());
        else if (ctx.SUPER() != null)
            return new Ident(Name.super_());
        else if (ctx.anonClassExpr() != null)
            return buildAnonymousClassExpr(ctx.anonClassExpr());
        else if (ctx.newArray() != null)
            return buildNewArray(ctx.newArray());
        else if (ctx.literal() != null)
            return buildLiteral(ctx.literal());
        else if (ctx.identifier() != null)
            return new Ident(buildName(ctx.identifier()));
        else if (ctx.lambdaExpression() != null)
            return buildLambdaExpr(ctx.lambdaExpression());
        else
            throw new ParsingException("Invalid primary expression: " + ctx.getText());
    }

    private static LambdaExpr buildLambdaExpr(LambdaExpressionContext ctx) {
        return new LambdaExpr(
                buildLambdaParams(ctx.lambdaParameters()),
                Utils.safeCall(ctx.typeOrVoid(), AstBuilder::buildType),
                ctx.lambdaBody() != null ? buildBlock(ctx.lambdaBody().block()) : buildExpr(ctx.expression())
        );
    }

    private static List<TypeNode> buildTypeArguments(@Nullable TypeArgumentsContext ctx) {
        return ctx != null ? List.from(ctx.type(), AstBuilder::buildType) : List.nil();
    }

    private static List<Expr> buildExprList(@Nullable ExpressionListContext ctx) {
        return ctx != null ? List.from(ctx.expression(), AstBuilder::buildExpr) : List.nil();
    }

    private static Expr buildAnonymousClassExpr(AnonClassExprContext ctx) {
        return new AnonClassExpr(
                buildAnonymousClass(ctx.classBody(), buildExprList(ctx.arguments().expressionList()), buildClassType(ctx.classType()))
        );
    }

    private static ClassDecl buildAnonymousClass(ClassBodyContext bodyCtx, List<Expr> args, TypeNode baseType) {
        return new ClassDecl(
                ClassTag.CLASS,
                List.nil(),
                List.nil(),
                false,
                NameTable.instance.empty,
                null,
                List.of(new Extend(baseType, args)),
                List.nil(),
                List.nil(),
                List.nil(),
                List.from(bodyCtx.classBodyDeclaration(), AstBuilder::buildClassMember)
        );
    }

    private static Expr buildNewArray(NewArrayContext ctx) {
        return new NewArrayExpr(
                buildType(ctx.type()),
                ctx.arrayKind().R() != null,
                ctx.arrayInitializer() != null ?
                        List.from(
                                ctx.arrayInitializer().variableInitializer(),
                                v -> buildExpr(v.expression())
                        ) : List.nil()
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

    private static Expr buildQualifiedName(QualifiedNameContext ctx) {
        var it = ctx.identifier().iterator();
        Expr expr = new Ident(buildName(it.next()));
        while (it.hasNext()) {
            expr = new SelectorExpr(expr, buildName(it.next()));
        }
        return expr;
    }

    private static Name getSymName(String text) {
        return NameTable.instance.get(text);
    }

}
