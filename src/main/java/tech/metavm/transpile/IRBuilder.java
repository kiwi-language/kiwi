package tech.metavm.transpile;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.TerminalNode;
import tech.metavm.transpile.ir.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ReflectUtils;
import tech.metavm.util.ValuePlaceholder;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.function.Consumer;

import static tech.metavm.transpile.IRTypeUtil.classForName;
import static tech.metavm.transpile.IRTypeUtil.fromClass;
import static tech.metavm.transpile.JavaParser.*;
import static tech.metavm.transpile.TsLexicon.*;
import static tech.metavm.util.NncUtils.requireNonNull;

public class IRBuilder {

    public static final Map<Integer, TsLexicon> MODIFIER_TO_LEXICON = Map.of(
            JavaLexer.PUBLIC, TsLexicon.PUBLIC,
            JavaLexer.PRIVATE, TsLexicon.PRIVATE,
            JavaLexer.STATIC, TsLexicon.STATIC,
            JavaLexer.ABSTRACT, TsLexicon.ABSTRACT,
            JavaLexer.FINAL, READONLY
    );

    public static final Map<Integer, Modifier> MODIFIER_MAP = Map.of(
            JavaLexer.PUBLIC, Modifier.PUBLIC,
            JavaLexer.PRIVATE, Modifier.PRIVATE,
            JavaLexer.STATIC, Modifier.STATIC,
            JavaLexer.ABSTRACT, Modifier.ABSTRACT,
            JavaLexer.FINAL, Modifier.FINAL
    );

    private static final Map<String, String> IDENTIFIER_MAP = Map.of(
//            "Object", "any",
//            "String", "string",
            "List", "Array",
            "ArrayList", "Array",
            "null", "undefined",
            "==", "===",
            "!=", "!==",
            "InternalException", "Error"
    );

    public static final Set<String> IMPORT_PKG_BLACKLIST = Set.of("java", "javax");


    public static IRBuilder createFromFileName(String fileName) {
        try {
            return new IRBuilder(CharStreams.fromFileName(fileName));
        }
        catch (IOException e) {
            throw new InternalException("Fail to create IRBuilder from file '" + fileName + "'", e);
        }
    }

    public static IRBuilder createFromString(String content) {
        return new IRBuilder(CharStreams.fromString(content));
    }

    private final JavaParser parser;
    private final PackageScanner packageScanner = new PackageScanner();
    private final LexiconOutputStream out = new LexiconOutputStream();
    private final SymbolTableV2 symbolTable = new SymbolTableV2();
    private String[] packageName;
    private IRPackage pkg;
    private final ExpressionTypeResolverV2 expressionTypeResolver = new ExpressionTypeResolverV2(symbolTable);

    private final LinkedList<BlockStatementContext> statementStack = new LinkedList<>();

    IRBuilder(CharStream stream) {
        JavaLexer lexer = new JavaLexer(stream);
        parser = new JavaParser(new CommonTokenStream(lexer));
        addBuiltinTypes();
    }

    public SourceUnit parse() {
        var compilationUnit = parser.compilationUnit();
        pkg = compilationUnit.packageDeclaration() != null ?
                parsePackage(compilationUnit.packageDeclaration()) : IRPackage.ROOT_PKG;
        symbolTable.addPackage(pkg);
        packageName = pkg.name().split("\\.");
        compilationUnit.importDeclaration().forEach(this::parseImportDeclaration);

        var initializers = new ArrayList<Runnable>();
        List<IRClass> irClasses = NncUtils.map(
                compilationUnit.typeDeclaration(),
                t -> parseType(t, initializers::add)
        );
        irClasses.forEach(symbolTable::addClass);
        initializers.forEach(Runnable::run);
        return new SourceUnit(irClasses);
    }

    private IRPackage parsePackage(PackageDeclarationContext packageDecl) {
        return new IRPackage(getQualifiedName(packageDecl.qualifiedName()));
    }

    private void addBuiltinTypes() {
        symbolTable.addPackage(new IRPackage("java.lang"));
    }

    private IRClass parseType(TypeDeclarationContext typeDeclaration, Consumer<Runnable> addInitializer) {
        List<IRAnnotation> annotations = new ArrayList<>();
        List<Modifier> modifiers = new ArrayList<>();
        parseClassOrInterfaceModifiers(typeDeclaration.classOrInterfaceModifier(), annotations, modifiers);

        if (typeDeclaration.classDeclaration() != null) {
            return parseClass(modifiers, annotations, typeDeclaration.classDeclaration(), null, null, addInitializer);
        } else if (typeDeclaration.interfaceDeclaration() != null) {
            return parseInterface(modifiers, annotations, typeDeclaration.interfaceDeclaration(), null, null, addInitializer);
        }
        else if(typeDeclaration.recordDeclaration() != null) {
            return parseRecord(typeDeclaration.recordDeclaration(), null);
        }
        throw new RuntimeException("Unsupported type kind (Only class and interface are supported are right)");
    }

    private void parseImportDeclaration(ImportDeclarationContext importDeclaration) {
        var importText = parseQualifiedName(importDeclaration.qualifiedName());
        if(importDeclaration.STATIC() == null) {
            if (importDeclaration.DOT() == null) {
                importClass(importText);
            } else {
                var allImports = packageScanner.getClassNames(importText);
                allImports.forEach(this::importClass);
            }
        }
        else {
            if(importDeclaration.DOT() == null) {
                var lastDotIdx = importText.lastIndexOf('.');
                NncUtils.requirePositive(lastDotIdx);
                IRClass klass = IRTypeUtil.classForName(importText.substring(0, lastDotIdx));
                String importName = importText.substring(lastDotIdx + 1);
                symbolTable.importStaticMember(klass, importName);
            }
            else {
                IRClass klass = IRTypeUtil.classForName(importText);
                symbolTable.importAllStaticMembers(klass);
            }
        }
    }

    private boolean shouldIgnoreImport(String importText) {
        for (String s : IMPORT_PKG_BLACKLIST) {
            if(importText.startsWith(s)) {
                return true;
            }
        }
        return false;
    }

    private void importClass(String name) {
        symbolTable.importClass(IRTypeUtil.classForName(name));
    }

    private String transformImport(String javaImport) {
        var javaImportSplits = javaImport.split("\\.");
        var className = javaImportSplits[javaImportSplits.length - 1];
        var builder = new StringBuilder(className);
        builder.append(" from '");
        int i;
        for (i = 0; i < Math.min(javaImportSplits.length - 1, packageName.length); i++) {
            if(!javaImportSplits[i].equals(packageName[i])) {
                break;
            }
        }
        if(i == packageName.length) {
            builder.append("./");
        }
        else {
            builder.append("../".repeat(Math.max(0, packageName.length - i)));
        }
        for (int j = i; j < javaImportSplits.length - 1; j++) {
            builder.append(javaImportSplits[j]).append('/');
        }
        builder.append(className).append("'");
        return builder.toString();
    }

    private String getQualifiedName(QualifiedNameContext qualifiedNameContext) {
        return symbolTable.getQualifiedName(parseQualifiedName(qualifiedNameContext));
    }

    private String parseQualifiedName(QualifiedNameContext qualifiedNameContext) {
        return NncUtils.join(qualifiedNameContext.identifier(), RuleContext::getText, ".");
    }

    private Set<Modifier> getModifierTypes0(List<ClassOrInterfaceModifierContext> modifiers) {
        List<TerminalNode> terminalNodes = NncUtils.mapAndFilterByType(
                modifiers, m -> m.getChild(0), TerminalNode.class
        );
        return NncUtils.mapUnique(terminalNodes, m -> MODIFIER_MAP.get(m.getSymbol().getType()));
    }

    private void writeTypeType(TypeTypeContext typeType) {
        writeTypeType(typeType, 0);
    }

    private IRType parseTypeType(TypeTypeContext typeType) {
        return parseTypeType(typeType, 0);
    }

    private IRType parseTypeType(TypeTypeContext typeType, int extraDimensions) {
        var type = typeType.primitiveType() != null ?
                parsePrimitiveType(typeType.primitiveType())
                : parseClassOrInterface(typeType.classOrInterfaceType());
        ChildrenStream stream = new ChildrenStream(typeType);
        int dimensions = stream.countLiteral("[") + extraDimensions;
        for (int i = 0; i < dimensions; i++) {
            type = type.getArrayType();
        }
        return type;
    }

    private IRType parseClassType(ClassTypeContext classTypeCtx) {
        if(classTypeCtx.classOrInterfaceType() != null) {
            var ownerType = parseClassOrInterface(classTypeCtx.classOrInterfaceType());
            List<IRType> typeArgs = NncUtils.get(classTypeCtx.typeArguments(), this::parseTypeArguments, () -> List.of());
            var ownerClass = IRUtil.getRawClass(ownerType);
            var rawClass = ownerClass.getClass(classTypeCtx.identifier().getText());
            if(ownerType instanceof IRClass && NncUtils.isEmpty(typeArgs)) {
                return rawClass;
            }
            else {
                return new PType(ownerType, rawClass, typeArgs);
            }
        }
        else {
            var klass = IRTypeUtil.classForName(
                    symbolTable.getQualifiedName(classTypeCtx.identifier().getText())
            );
            if(classTypeCtx.typeArguments() != null) {
                return new PType(null, klass, parseTypeArguments(classTypeCtx.typeArguments()));
            }
            else {
                return klass;
            }
        }
    }

    private IRClass parsePrimitiveType(PrimitiveTypeContext primitiveType) {
        return expressionTypeResolver.parsePrimitiveType(primitiveType);
    }

    private IRType parseClassOrInterface(ClassOrInterfaceTypeContext classOrInterface) {
        var namePrefix = NncUtils.join(
                classOrInterface.identifier(),
                RuleContext::getText,
                "."
        );
        var name = namePrefix.length() > 0 ?
                namePrefix + "." + classOrInterface.typeIdentifier().getText()
                : classOrInterface.typeIdentifier().getText();
        var klass = classForName(symbolTable.getQualifiedName(name));
        int nTypeArgs = classOrInterface.typeArguments().size();
        if(nTypeArgs == 0) {
            return klass;
        }
        else {
            var typeArgsContext = classOrInterface.typeArguments(nTypeArgs-1);
            List<IRType> typeArgs = NncUtils.map(
                    typeArgsContext.typeArgument(),
                    t -> parseTypeType(t.typeType())
            );
            return new PType(null, klass, typeArgs);
        }
    }

    private void writeTypeType(TypeTypeContext typeType, int dimension) {
        typeType.annotation().forEach(this::writeAnnotation);
        if(typeType.classOrInterfaceType() != null) {
            writeClassOrInterfaceType(typeType.classOrInterfaceType());
        }
        if(typeType.primitiveType() != null) {
            writePrimitiveType(typeType.primitiveType());
        }
        for (int i = 0; i < dimension; i++) {
            out.writeKeyword(COMPACT_OPEN_SQUARE_BRACKET);
            out.writeKeyword(COMPACT_CLOSING_SQUARE_BRACKET);
        }
    }

    private CodeBlock parseBlock(BlockContext block) {
        return parseBlock(block, b -> {});
    }

    private CodeBlock parseBlock(BlockContext blockContext, Consumer<CodeBlock> defineVariables) {
        var block = newCodeBlock();
        enterBlock(block);
        defineVariables.accept(block);
        blockContext.blockStatement().forEach(this::parseBlockStatement);
        exitBlock();
        return block;
    }

    private Statement parseBlockStatement(BlockStatementContext blockStatement) {
        if(blockStatement.localVariableDeclaration() != null) {
            return parseLocalVariableDeclaration(blockStatement.localVariableDeclaration());
        }
        else if(blockStatement.localTypeDeclaration() != null) {
            return parseLocalTypeDeclaration(blockStatement.localTypeDeclaration());
        }
        else {
            return parseStatement(blockStatement.statement());
        }
    }

    private LocalVariableDeclaration parseLocalVariableDeclaration(LocalVariableDeclarationContext localVarDeclCtx) {
        var mod = parseVariableModifiers(localVarDeclCtx.variableModifier());
        if(localVarDeclCtx.typeType() != null) {
            var type = parseTypeType(localVarDeclCtx.typeType());
            List<LocalVariableDeclarator> declarators = new ArrayList<>();
            for (VariableDeclaratorContext varDeclCtx :
                    localVarDeclCtx.variableDeclarators().variableDeclarator()) {
                declarators.add(
                        parseVariableDeclarator(varDeclCtx, mod.annotations, mod.immutable, type)
                );
            }
            return codeBlock().addStatement(
                    new LocalVariableDeclaration(
                            mod.annotations,
                            mod.immutable,
                            type,
                            declarators
                    )
            );
        }
        else {
            writeVarDeclaration(
                    localVarDeclCtx.variableModifier(),
                    localVarDeclCtx.identifier()
            );
            var value = parseExpression(localVarDeclCtx.expression());
            var localVar = new LocalVariable(
                    codeBlock(),
                    mod.immutable, mod.annotations,
                    localVarDeclCtx.identifier().getText(),
                    value.type()
            );
            return codeBlock().addStatement(new LocalVariableDeclaration(
                    mod.annotations, mod.immutable, value.type(),
                    List.of(
                            new LocalVariableDeclarator(localVar, value)
                    )
            ));
        }
    }

    private void writeIdentifierAndType(Runnable writeIdentifier, Runnable writeType, int dimension) {
        writeIdentifier.run();
        if(writeType != null) {
            out.writeKeyword(COMPACT_COLON);
            writeType.run();
            writeDimensions(dimension);
        }
    }

    private LocalTypeDeclaration parseLocalTypeDeclaration(LocalTypeDeclarationContext localTypeDeclaration) {
        List<IRAnnotation> annotations = new ArrayList<>();
        List<Modifier> modifiers = new ArrayList<>();
        parseClassOrInterfaceModifiers(localTypeDeclaration.classOrInterfaceModifier(), annotations, modifiers);
        IRType type;
        if(localTypeDeclaration.classDeclaration() != null) {
            type = parseClassAndInit(modifiers, annotations, localTypeDeclaration.classDeclaration(), null, currentMethod());
        }
        else if(localTypeDeclaration.interfaceDeclaration() != null) {
            type = parseInterfaceAndInit(modifiers, annotations, localTypeDeclaration.interfaceDeclaration(), null, currentMethod());
        }
        else {
            type = parseRecord(localTypeDeclaration.recordDeclaration(), null);
        }
        return new LocalTypeDeclaration(type);
    }

    private Modifier getModifier(int symbolType) {
        return requireNonNull(
                MODIFIER_MAP.get(symbolType),
                "Can not find modifier for symbol type " + symbolType
        );
    }

    private void parseClassOrInterfaceModifiers(
            List<ClassOrInterfaceModifierContext> modifierContexts,
            List<IRAnnotation> annotations,
            List<Modifier> modifiers
    ) {
        for (ClassOrInterfaceModifierContext modCtx : modifierContexts) {
            if(modCtx.annotation() != null) {
                annotations.add(parseAnnotation(modCtx.annotation()));
            }
            else {
                modifiers.add(
                        getModifier(modCtx.getChild(TerminalNode.class, 0).getSymbol().getType())
                );
            }
        }
    }

    private Statement parseStatement(StatementContext statement) {
        var stream = new ChildrenStream(statement);
        if(stream.isNextInstanceOf(BlockContext.class)) {
            return parseBlock(stream.next(BlockContext.class));
        }
        else if(stream.isNextTerminalOf(JavaLexer.ASSERT)) {
            return parseAssert(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.IF)) {
            return parseIf(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.FOR)) {
            return parseFor(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.WHILE)) {
            return parseWhile(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.DO)) {
            return parseDo(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.TRY)) {
            return parseTry(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.SWITCH)) {
            return parseSwitch(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.SYNCHRONIZED)) {
            return parseSynchronized(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.RETURN)) {
            return parseReturn(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.THROW)) {
            return parseThrow(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.CONTINUE)) {
            return parseContinue(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.BREAK)) {
            return parseBreak(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.YIELD)) {
            return parseYield(stream);
        }
        else if(stream.isNextTerminalOf(JavaLexer.SEMI)) {
            return parseSemi();
        }
        else if(stream.isNextExpression()) {
            return parseExpression(stream.nextExpression());
        }
        else if(stream.isNextInstanceOf(SwitchExpressionContext.class)) {
            return parseSwitchExpression(stream.next(SwitchExpressionContext.class));
        }
        else if(stream.isNextIdentifier()) {
            Label label = parseLabel(stream);
            return label.statement();
        }
        throw new InternalException("Unrecognized statement " + statement);
    }

    private Label parseLabel(ChildrenStream stream) {
        return new Label(
                codeBlock(),
                stream.nextIdentifier().getText(),
                parseStatement(stream.nextStatement())
        );
    }

    private Statement parseAssert(@SuppressWarnings("unused") ChildrenStream stream) {
        return null;
    }

    private IRExpression parseSwitchExpression(SwitchExpressionContext switchExpression) {
        return new SwitchExpression(
                parseExpression(switchExpression.parExpression().expression()),
                NncUtils.map(
                        switchExpression.switchLabeledRule(),
                        this::parseSwitchLabeledRule
                )
        );
    }

    private SwitchExpressionCase parseSwitchLabeledRule(SwitchLabeledRuleContext switchLabeledRule) {
//        CodeBlock block = new CodeBlock(currentBlock());
//        enterBlock(block);
        SwitchCaseCondition condition;
        if (switchLabeledRule.CASE() != null) {
            out.writeKeyword(TsLexicon.CASE);
            if (switchLabeledRule.expressionList() != null) {
                condition = new SwitchMatchList(
                        parseExpressionList(switchLabeledRule.expressionList())
                );
            } else if (switchLabeledRule.NULL_LITERAL() != null) {
                condition = new SwitchMatchList(
                        List.of(new NullIRExpression())
                ) ;
            } else {
                condition = parseGuardedPattern(switchLabeledRule.guardedPattern());

            }
        } else {
            condition = new SwitchMatchList(List.of());
        }
        var isArrow = switchLabeledRule.ARROW() != null;
        var outcome = parseSwitchRuleOutcome(switchLabeledRule.switchRuleOutcome(), isArrow);
        exitBlock();
        return new SwitchExpressionCase(
//                block,
                condition,
                outcome
        );
    }

    private GuardedPattern parseGuardedPattern(GuardedPatternContext guardedPatternCtx) {
        ChildrenStream stream = new ChildrenStream(guardedPatternCtx);
        if(stream.isNextLiteral("(")) {
            return parseGuardedPattern(stream.next(GuardedPatternContext.class));
        }
        else if (stream.isNextInstanceOf(GuardedPatternContext.class)){
            var p = parseGuardedPattern(stream.next(GuardedPatternContext.class));
            var expr = parseExpression(stream.nextExpression());
            return new GuardedPattern(
                    p.variable(),
                    NncUtils.merge(p.expressions(), expr)
            );
        }
        else {
            var variable = parseLocalVariable(
                    guardedPatternCtx.variableModifier(),
                    guardedPatternCtx.annotation(),
                    guardedPatternCtx.identifier(),
                    guardedPatternCtx.typeType()
            );
            var expressions = NncUtils.map(
                    guardedPatternCtx.expression(),
                    this::parseExpression
            );
            return new GuardedPattern(variable, expressions);
        }
    }

    private LocalVariable parseLocalVariable(
            List<VariableModifierContext> modifierContexts,
            List<AnnotationContext> annotationContexts,
            IdentifierContext identifierContext,
            TypeTypeContext typeTypeContext
    ) {
        var mod = parseVariableModifiers(modifierContexts, annotationContexts);
        return new LocalVariable(
                codeBlock(),  mod.immutable, mod.annotations, identifierContext.getText(), parseTypeType(typeTypeContext)
        );
    }

    private SwitchRuleOutcome parseSwitchRuleOutcome(SwitchRuleOutcomeContext switchRuleOutcome, boolean isArrow) {
        if(switchRuleOutcome.block() != null) {
            return new BlockSwitchRuleOutcome(parseBlock(switchRuleOutcome.block()));
        }
        else if(isArrow) {
            return new ExpressionSwitchRuleOutcome(
                    parseExpression(switchRuleOutcome.blockStatement(0).statement().expression(0))
            );
        }
        else {
            return new StatementsSwitchRuleOutcome(
                NncUtils.map(
                        switchRuleOutcome.blockStatement(),
                        this::parseBlockStatement
                )
            );
        }
    }

    private Statement parseYield(ChildrenStream stream) {
        out.writeKeyword(TsLexicon.YIELD);
        return new Yield(parseExpression(stream.findExpression()));
    }

    private Statement parseThrow(ChildrenStream stream) {
        stream.nextTerminal(JavaLexer.THROW);
        return new Throw(parseExpression(stream.nextExpression()));
    }

    private Statement parseSemi() {
        return new Noop();
    }

    private Synchronized parseSynchronized(ChildrenStream stream) {
        return new Synchronized(
                parseParExpression(stream.nextParExpression()),
                parseBlock(stream.nextBlock())
        );
    }

    private Continue parseContinue(ChildrenStream stream) {
        stream.nextTerminal(JavaLexer.CONTINUE);
        return new Continue(
                stream.mapIfNextInstanceOf(IdentifierContext.class, this::resolveLabel)
        );
    }

    private Break parseBreak(ChildrenStream stream) {
        stream.nextTerminal(JavaLexer.BREAK);
        return new Break(
                stream.mapIfNextInstanceOf(IdentifierContext.class, this::resolveLabel)
        );
    }

    private Return parseReturn(ChildrenStream stream) {
        stream.nextTerminal(JavaLexer.RETURN);
        return new Return(
                stream.mapIfNextInstanceOf(
                        ExpressionContext.class,
                        this::parseExpression,
                        () -> null
                )
        );
    }

    private Try parseTry(ChildrenStream stream) {
        stream.nextTerminal(JavaLexer.TRY);
        try {
            CodeBlock block = newCodeBlock();
            enterBlock(block);
            return new Try(
                    block,
                    stream.mapIfNextInstanceOf(
                            ResourceSpecificationContext.class,
                            this::parseResourceSpecification,
                            List::of
                    ),
                    parseBlock(stream.nextBlock()),
                    NncUtils.map(stream.nextList(CatchClauseContext.class), this::parseCatch),
                    stream.mapIfNextInstanceOf(FinallyBlockContext.class, this::parseFinally, () -> null)
            );
        }
        finally {
            exitBlock();
        }
    }

    private CatchClause parseCatch(CatchClauseContext catchClause) {
        var mod = parseVariableModifiers(catchClause.variableModifier());
        var variableHolder = new ValuePlaceholder<LocalVariable>();
        var body = parseBlock(catchClause.block(), b ->
                variableHolder.set(new LocalVariable(
                        b, mod.immutable, mod.annotations, catchClause.identifier().getText(),
                        parseCatchType(catchClause.catchType())
                ))
        );
        return new CatchClause(variableHolder.get(), body);
    }

    private IRType parseCatchType(CatchTypeContext catchTypeContext) {
        List<IRType> types = NncUtils.map(
                catchTypeContext.qualifiedName(),
                this::parseTypeFromQualifiedName
        );
        NncUtils.requireMinimumSize(types, 1);
        if(types.size() == 1) {
            return types.get(0);
        }
        return new TypeUnion(types);
    }

    private <T> void writeList(List<T> list, Consumer<T> operation, TsLexicon delimiter) {
        writeList(list, operation, () -> out.writeKeyword(delimiter));
    }

    private <T> void writeList(List<T> list, Consumer<T> operation, Runnable delimitingOperation) {
        for (int i = 0; i < list.size(); i++) {
            if(i > 0) {
                delimitingOperation.run();
            }
            operation.accept(list.get(i));
        }
    }

    private Label resolveLabel(IdentifierContext identifier) {
        if(currentBlock() instanceof CodeBlock b) {
            return b.getLabel(identifier.getText());
        }
        else {
            throw new InternalException("Currently not in code block");
        }
    }

    private CodeBlock parseFinally(FinallyBlockContext finallyBlock) {
        return parseBlock(finallyBlock.block());
    }

    private List<Resource> parseResourceSpecification(ResourceSpecificationContext resourceSpecification) {
        return parseResources(resourceSpecification.resources());
    }

    private List<Resource> parseResources(ResourcesContext resources) {
        return NncUtils.map(resources.resource(), this::parseResource);
    }

    private Resource parseResource(ResourceContext resource) {
        if(resource.expression() != null) {
            var expression = parseExpression(resource.expression());
            var type = resource.classOrInterfaceType() != null ?
                    parseClassOrInterface(resource.classOrInterfaceType()) : expression.type();
            var mod = parseVariableModifiers(resource.variableModifier());
            var name = resource.variableDeclaratorId() != null ?
                    resource.variableDeclaratorId().identifier().getText() : resource.identifier().getText();
            return new VariableDeclarationResource(
                    new LocalVariable(codeBlock(), mod.immutable, mod.annotations, name, type),
                    expression
            );
        }
        else {
            return new ValueSymbolResource(symbolTable.resolveValue(resource.identifier().getText()));
        }
    }

    private void writeVariableDeclaration(List<VariableModifierContext> modifiers,
                                          Runnable writeTypeOperation,
                                          IdentifierContext identifier,
                                          int dimensions
    ) {
        var mod = parseVariableModifiers(modifiers);
        if(mod.immutable) {
            out.writeKeyword(TsLexicon.CONST);
        }
        else {
            out.writeKeyword(LET);
        }
        writeIdentifier(identifier);
        out.writeKeyword(COMPACT_COLON);
        writeTypeOperation.run();
        writeDimensions(dimensions);
    }

    private void writeVarDeclaration(List<VariableModifierContext> modifiers, IdentifierContext identifier) {
        var mod = parseVariableModifiers(modifiers);
        if (mod.immutable) {
            out.writeKeyword(TsLexicon.CONST);
        }
        else {
            out.writeKeyword(LET);
        }
        writeIdentifier(identifier);
    }

    private int getDimension(VariableDeclaratorIdContext variableId) {
        return (variableId.getChildCount() - 1) / 2;
    }

    private void writeDimensions(int dimensions) {
        for (int i = 0; i < dimensions; i++) {
            out.writeKeyword(COMPACT_OPEN_SQUARE_BRACKET);
            out.writeKeyword(COMPACT_CLOSING_SQUARE_BRACKET);
        }
    }

    private Statement parseSwitch(ChildrenStream stream) {
        stream.nextTerminal(JavaLexer.SWITCH);
        var expression = parseParExpression(stream.next(ParExpressionContext.class));
        stream.next();
        enterBlock(newCodeBlock());
        List<SwitchCase> cases = new ArrayList<>(NncUtils.flatMap(
                stream.nextList(SwitchBlockStatementGroupContext.class),
                this::writeSwitchBlockStatementGroup
        ));
        cases.addAll(NncUtils.map(
                stream.nextList(SwitchLabelContext.class),
                l -> new SwitchCase(parseSwitchLabel(l), null)
        ));
        exitBlock();
        return new SwitchStatement(expression, cases);
    }

    private List<SwitchCase> writeSwitchBlockStatementGroup(SwitchBlockStatementGroupContext switchBlockStatementGroup) {
        List<SwitchCase> cases = new ArrayList<>();
        var labels = switchBlockStatementGroup.switchLabel();
        for (int i = 0; i < labels.size()-1; i++) {
            cases.add(
                    new SwitchCase(
                            parseSwitchLabel(labels.get(0)),
                            null
                    )
            );
        }
        enterBlock(newCodeBlock());
        cases.add(
                new SwitchCase(
                        parseSwitchLabel(labels.get(labels.size() - 1)),
                        NncUtils.map(
                                switchBlockStatementGroup.blockStatement(),
                                this::parseBlockStatement
                        )
                )
        );
        exitBlock();
        return cases;
    }

    private SwitchLabel parseSwitchLabel(SwitchLabelContext switchLabel) {
        ChildrenStream stream = new ChildrenStream(switchLabel);
        if (stream.isNextTerminalOf(JavaLexer.CASE)) {
            if (stream.isNextInstanceOf(ExpressionContext.class)) {
                return new ExpressionSwitchLabel(parseExpression(stream.nextExpression()));
            } else if (stream.isNextTerminalOf(JavaLexer.IDENTIFIER)) {
                return new ValueSwitchLabel(
                        symbolTable.resolveValue(stream.nextTerminal().getText())
                );
            } else {
                var type = parseTypeType(stream.nextTypeType());
                var identifier = stream.nextIdentifier();
                var variable = new LocalVariable(
                        codeBlock(), false, List.of(), identifier.getText(), type
                );
                return new VariableSwitchLabel(variable);
            }
        } else {
            return new DefaultSwitchLabel();
        }
    }

    private Statement parseDo(ChildrenStream stream) {
        stream.nextTerminal(JavaLexer.DO);
        return new DoWhileStatement(
                parseStatement(stream.nextStatement()),
                parseParExpression(stream.nextParExpression())
        );
    }

    private Statement parseWhile(ChildrenStream stream) {
        stream.nextTerminal(JavaLexer.WHILE);
        return new WhileStatement(
                parseParExpression(stream.nextParExpression()),
                parseStatement(stream.nextStatement())
        );
    }

    private Statement parseIf(ChildrenStream stream) {
        CodeBlock block = newCodeBlock();
        symbolTable.enterBlock(block);
        stream.nextTerminal(JavaLexer.IF);
        var expression = parseParExpression(stream.next(ParExpressionContext.class));
        var body = parseStatement(stream.nextStatement());
        Statement elseBody;
        if(stream.isNextTerminalOf(JavaLexer.ELSE)) {
            stream.nextTerminal(JavaLexer.ELSE);
            elseBody = parseStatement(stream.nextStatement());
        }
        else {
            elseBody = null;
        }
        symbolTable.exitBlock();
        return new IfStatement(expression, block, body, elseBody);
    }

    private Statement parseFor(ChildrenStream stream) {
        CodeBlock block = newCodeBlock();
        symbolTable.enterBlock(block);
        var control = parseForControl(stream.find(ForControlContext.class));
        var body = parseStatement(stream.nextStatement());
        symbolTable.exitBlock();
        return new ForStatement(block, control, body);
    }

    private ForControl parseForControl(ForControlContext forControl) {
        if(forControl.enhancedForControl() != null) {
            return parseEnhancedForControl(forControl.enhancedForControl());
        }
        else {
            return new ClassicForControl(
                    NncUtils.get(forControl.forInit(), this::parseForInit),
                    NncUtils.get(forControl.expression(), this::parseExpression),
                    NncUtils.get(forControl.expressionList(), this::parseExpressionList)
            );
        }
    }

    private ForInit parseForInit(ForInitContext forInit) {
        if(forInit.localVariableDeclaration() != null) {
            return new VariableDeclarationForInit(
                    parseLocalVariableDeclaration(forInit.localVariableDeclaration())
            );
        }
        else if(forInit.expressionList() != null) {
            return new ExpressionForInit(parseExpressionList(forInit.expressionList()));
        }
        else {
            return null;
        }
    }

    private EnhancedForControl parseEnhancedForControl(EnhancedForControlContext enhancedForControl) {
        var mod = parseVariableModifiers(enhancedForControl.variableModifier());
        var variableIdentifier = enhancedForControl.variableDeclaratorId().identifier();
        var expression = parseExpression(enhancedForControl.expression());
        var variableType = enhancedForControl.typeType() != null ?
                parseTypeType(enhancedForControl.typeType(), countDimensions(variableIdentifier))
                : IRUtil.getIterableElementType(expression.type());
        var variable = new LocalVariable(
                codeBlock(),
                mod.immutable,
                mod.annotations,
                variableIdentifier.getText(),
                variableType
        );
        return new EnhancedForControl(variable, expression);
    }

    private IRExpression parseParExpression(ParExpressionContext parExpression) {
        return parseExpression(parExpression.expression());
    }

    private List<IRExpression> parseExpressionList(ExpressionListContext expressionList) {
        return NncUtils.map(expressionList.expression(), this::parseExpression);
    }

    private LocalVariableDeclarator parseVariableDeclarator(VariableDeclaratorContext variableDeclarator,
                                                            List<IRAnnotation> annotations,
                                                            boolean immutable,
                                                            IRType type) {
        return new LocalVariableDeclarator(
                new LocalVariable(
                        codeBlock(),
                        immutable,
                        annotations,
                        variableDeclarator.variableDeclaratorId().getText(),
                        type
                ),
                NncUtils.get(
                        variableDeclarator.variableInitializer(),
                        e -> parseVariableInitializer(e, type)
                )
        );
    }

    private IRExpression parseVariableInitializer(VariableInitializerContext variableInitializer, IRType type) {
        if(variableInitializer.arrayInitializer() != null) {
            return parseArrayInitializer(variableInitializer.arrayInitializer(), (IRArrayType) type);
        }
        else {
            return resolveDirectly(
                    type,
                    parseExpression(variableInitializer.expression())
            );
        }
    }

    private ArrayExpression parseArrayInitializer(ArrayInitializerContext arrayInitializer, IRArrayType type) {
        return new ArrayExpression(
                type,
                NncUtils.map(
                        arrayInitializer.variableInitializer(),
                        e -> parseVariableInitializer(e, type.getElementType())
                )
        );
    }

    private IRExpression parseExpression(ExpressionContext expression) {
        if(expression.primary() != null) {
            return parsePrimary(expression.primary());
        }
        else if(expression.getChildCount() == 1 && expression.methodCall() != null) {
            return parseMethodCall(expression.methodCall(), new ThisIRExpression(currentType()));
        }
        else if(expression.switchExpression() != null) {
            return parseSwitchExpression(expression.switchExpression());
        }
        else if(expression.lambdaExpression() != null) {
            return parseHalfLambdaExpression(expression.lambdaExpression());
        }
        else if(expression.prefix != null) {
            return new UnaryExpression(
                    parseExpression(expression.expression(0)),
                    IROperator.getByCode(expression.prefix.getText())
            );
        }
        else if(expression.postfix != null) {
            return new UnaryExpression(
                    parseExpression(expression.expression(0)),
                    IROperator.getByCode(expression.postfix.getText())
            );
        }
        else if(expression.getChildCount() >= 2 && expression.getChild(1).getText().equals("::")) {
            return parseMethodReference(new ChildrenStream(expression));
        }
        else {
            var stream = new ChildrenStream(expression);
            if(stream.isNextExpression()) {
                var expr = stream.nextExpression();
                if(stream.isNextLiteral("[")) {
                    return parseArrayElementExpression(expr, stream);
                }
                else if(expression.bop != null) {
                    final var bop = expression.bop;
                    if(bop.getType() == JavaLexer.INSTANCEOF) {
                        return writeInstanceOfExpression(expr, stream);
                    }
                    else if(bop.getText().equals(".")) {
                        return parseDotExpression(expr, stream);
                    }
                    else if(bop.getText().equals("?")) {
                        return parseConditionalExpression(expr, stream);
                    }
                    else {
                        return parseBinary(expr, stream);
                    }
                }
                else {
                    return parseShiftExpression(expr, stream);
                }
            }
            else if(stream.isNextTerminalOf(JavaLexer.NEW)) {
                return parseNewExpression(stream);
            }
            else if(stream.isNextLiteral("(")) {
                return parseCastExpression(stream);
            }
        }
        throw new InternalException("Unrecognized expression '" + expression.getText() + "'");
    }

    private IRExpression parseMethodReference(ChildrenStream stream) {
        if(stream.isNextExpression()) {
            var expr = parseExpression(stream.nextExpression());
            stream.next();
            List<IRType> typeArguments = stream.isNextInstanceOf(TypeArgumentsContext.class) ?
                    parseTypeArguments(stream.next(TypeArgumentsContext.class)) : List.of();
            var methodName = stream.nextIdentifier().getText();
            return new UnresolvedMethodReference(expr, methodName, typeArguments);
        }
        else if(stream.isNextTypeType()) {
            var type = parseTypeType(stream.nextTypeType());
            stream.next();
            List<IRType> typeArguments = stream.isNextInstanceOf(TypeArgumentsContext.class) ?
                    parseTypeArguments(stream.next(TypeArgumentsContext.class)) : List.of();
            if (stream.isNextIdentifier()) {
                var methodName = stream.nextIdentifier().getText();
                return new UnresolvedStaticMethodReference(type, methodName, typeArguments);
            }
            else {
                return new UnresolvedConstructorReference(type, typeArguments);
            }
        }
        else {
            var type = parseClassType(stream.next(ClassTypeContext.class));
            List<IRType> typeArguments = stream.isNextInstanceOf(TypeArgumentsContext.class) ?
                    parseTypeArguments(stream.next(TypeArgumentsContext.class)) : List.of();
            return new UnresolvedConstructorReference(type, typeArguments);
        }
    }

    private IRExpression writeInstanceOfExpression(ExpressionContext expression, ChildrenStream stream) {
        var expr = parseExpression(expression);
        if(stream.isNextTypeType()) {
            return new InstanceOfExpression(expr, parseTypeType(stream.nextTypeType()), null);
        }
        else {
            var variable = parsePattern(stream.next(PatternContext.class));
            return new InstanceOfExpression(expr, variable.type(), variable);
        }
    }

    private IRExpression parseConditionalExpression(ExpressionContext expression, ChildrenStream stream) {
        return new ConditionalExpression(
            parseExpression(expression),
            parseExpression(stream.findExpression()),
            parseExpression(stream.findExpression())
        );
    }

    private IRExpression parseDotExpression(ExpressionContext expression, ChildrenStream stream) {
        var prefix = parseExpression(expression);
        var type = prefix.type();
        var klass = IRUtil.getRawClass(type);
        stream.next();
        if(stream.isNextIdentifier()) {
            var identifier = stream.nextIdentifier().getText();
            if(prefix instanceof PseudoExpression) {
                if(prefix instanceof ClassExpression classExpression) {
                    var prefixClass = classExpression.klass();
                    if(prefixClass.isFieldDeclared(identifier)) {
                        return new StaticFieldExpression(classExpression.klass().getField(identifier));
                    }
                    else if(prefixClass.isClassDeclared(identifier)) {
                        return new ClassExpression(prefixClass.getClass(identifier));
                    }
                    else {
                        throw new InternalException("Can not find a member field or member with name of '" +
                                identifier + "' class in " + prefixClass);
                    }
                }
                else if(prefix instanceof PackageExpression packageExpression) {
                    return packageExpression.getMember(identifier);
                }
                else {
                    throw new InternalException("Unexpected prefix expression: " + prefix);
                }
            }
            else {
                return new FieldIRExpression(prefix, klass.getField(identifier));
            }
        }
        else if(stream.isNextInstanceOf(MethodCallContext.class)) {
            return parseMethodCall(stream.next(MethodCallContext.class), prefix);
        }
        else if(stream.isNextTerminalOf(JavaLexer.NEW)) {
            List<IRType> typeArgs = stream.mapIfNextInstanceOf(
                    NonWildcardTypeArgumentsContext.class,
                    this::parseNonWildcardTypeArguments,
                    List::of
            );
            return parseInnerCreator(stream.next(InnerCreatorContext.class), prefix, typeArgs);
        }
        else if(stream.isNextTerminalOf(JavaLexer.THIS)) {
            return new ThisIRExpression(currentType());
        }
        else if(stream.isNextTerminalOf(JavaLexer.SUPER)) {
            return parseSuperSuffix(stream.next(SuperSuffixContext.class), null, prefix);
        }
        else {
            return parseExplicitGenericInvocation(stream.next(ExplicitGenericInvocationContext.class), prefix);
        }
    }

    private IRClass parseClassFromExpression(ExpressionContext expressionContext) {
        var identifier = requireNonNull(expressionContext.identifier());
        return IRTypeUtil.classForName(symbolTable.getQualifiedName(identifier.getText()));
    }

    private IRExpression parseExplicitGenericInvocation(ExplicitGenericInvocationContext explicitGenericInvocation, IRExpression expression) {
        return parseExplicitGenericInvocationSuffix(
                explicitGenericInvocation.explicitGenericInvocationSuffix(),
                parseNonWildcardTypeArguments(explicitGenericInvocation.nonWildcardTypeArguments()),
                expression
        );
    }

    private IRExpression parseInnerCreator(InnerCreatorContext innerCreator, IRExpression owner, List<IRType> methodTypeArguments) {
        var klass = parseClassFromIdentifier(innerCreator.identifier());
        TypeLike typeLike;
        var typeArgsOrDiamondCtx = innerCreator.nonWildcardTypeArgumentsOrDiamond();
        if(typeArgsOrDiamondCtx != null) {
            if(typeArgsOrDiamondCtx.nonWildcardTypeArguments() != null) {
                var typeArgs = parseNonWildcardTypeArguments(typeArgsOrDiamondCtx.nonWildcardTypeArguments());
                typeLike = createParameterizedType(klass, typeArgs);
            }
            else {
                typeLike = new DiamondType(owner.type(), klass);
            }
        }
        else {
            typeLike = klass;
        }
        return parseClassCreatorRest(
                innerCreator.classCreatorRest(),
                typeLike,
                methodTypeArguments,
                owner
        );
    }

    private PType createParameterizedType(IRClass rawClass, List<IRType> typeArguments) {
        return new PType(null, rawClass, typeArguments);
    }

    private IRClass parseClassFromIdentifier(IdentifierContext identifier) {
        var qualifiedName = symbolTable.getQualifiedName(identifier.getText());
        return IRTypeUtil.classForName(qualifiedName);
    }

    private IRExpression parseBinary(ExpressionContext expression, ChildrenStream stream) {
        var first = parseExpression(expression);
        var operator = IROperator.getByCode(stream.nextTerminal().getText());
        var second = parseExpression(stream.nextExpression());
        if(operator == IROperator.ASSIGN) {
            second = resolveDirectly(first.type(), second);
        }
        return new BinaryIRExpression(
                first,
                operator,
                second
        );
    }

    private LocalVariable parsePattern(PatternContext pattern) {
        var mod = parseVariableModifiers(pattern.variableModifier());
        return new LocalVariable(
                codeBlock(),
                mod.immutable,
                mod.annotations,
                pattern.identifier().getText(),
                parseTypeType(pattern.typeType())
        );
    }

    private IRExpression parseArrayElementExpression(ExpressionContext expression, ChildrenStream stream) {
        return new ArrayElementExpression(
                parseExpression(expression),
                parseExpression(stream.findExpression())
        );
    }

    private IRExpression parseCastExpression(ChildrenStream stream) {
        stream.next();
        stream.nextList(AnnotationContext.class);
        List<IRType> types = new ArrayList<>();
        while(stream.isNextTypeType()) {
            types.add(parseTypeType(stream.nextTypeType()));
            if(stream.isNextLiteral("&")) {
                stream.next();
            }
        }
        NncUtils.requireMinimumSize(types, 1);
        stream.next();
        return new CastExpression(
                parseExpression(stream.nextExpression()),
                types.size() == 1 ? types.get(0) : new TypeIntersection(types)
        );
    }

    private IRExpression parseNewExpression(ChildrenStream stream) {
        stream.nextTerminal(JavaLexer.NEW);
        return parseCreator(stream.next(CreatorContext.class));
    }

    private IRExpression parseCreator(CreatorContext creator) {
        if(creator.nonWildcardTypeArguments() != null) {
            return parseClassCreatorRest(
                    creator.classCreatorRest(),
                    parseCreatedName(creator.createdName()),
                    parseNonWildcardTypeArguments(creator.nonWildcardTypeArguments()),
                    null
            );
        }
        else if(creator.arrayCreatorRest() != null) {
            return parseArrayCreatorRest(
                    creator.arrayCreatorRest(),
                    (IRType) parseCreatedName(creator.createdName())
            );
        }
        else {
            return parseClassCreatorRest(
                    creator.classCreatorRest(),
                    parseCreatedName(creator.createdName()),
                    List.of(),
                    null
            );
        }
    }

    private TypeLike parseCreatedName(CreatedNameContext createdNameContext) {
        if(createdNameContext.primitiveType() != null) {
            return parsePrimitiveType(createdNameContext.primitiveType());
        }
        else {
            var stream = new ChildrenStream(createdNameContext);
            TypeLike typeLike = null;
            while(stream.hasNext()) {
                IRType type = (IRType) typeLike;
                final var identifier = stream.nextIdentifier();
                var klass = parseClassFromIdentifier(identifier);
                if(stream.isNextInstanceOf(TypeArgumentsOrDiamondContext.class)) {
                    var typeArgsOrDiamond = stream.next(TypeArgumentsOrDiamondContext.class);
                    if(typeArgsOrDiamond.typeArguments() != null) {
                        typeLike = new PType(
                                type, klass, parseTypeArguments(typeArgsOrDiamond.typeArguments())
                        );
                    }
                    else {
                        typeLike = new DiamondType(type, klass);
                    }
                }
                else {

                    if(typeLike == null || typeLike instanceof IRClass) {
                        typeLike = klass;
                    }
                    else {
                        typeLike = new PType(type, klass, List.of());
                    }
                }
            }
            return NncUtils.requireNonNull(typeLike);
        }

    }

    private TypePlaceholder parseTypePlaceholder(
            @Nullable TypePlaceholder ownerType,
            IdentifierContext identifierContext,
            TypeArgumentsOrDiamondContext typeArgumentsOrDiamondContext) {
        var klass = parseClassFromIdentifier(identifierContext);
        if(typeArgumentsOrDiamondContext.typeArguments() != null) {
            return TypePlaceholder.createResolved(
                    createParameterizedType(
                            klass,
                            parseTypeArguments(typeArgumentsOrDiamondContext.typeArguments())
                    )
            );
        }
        else {
            return new TypePlaceholder(klass, ownerType);
        }
    }

    private IRExpression parseArrayCreatorRest(ArrayCreatorRestContext arrayCreatorRest, IRType elementType) {
        if(arrayCreatorRest.arrayInitializer() != null) {
            int dimensions = countLiterals(arrayCreatorRest, "[");
            var type = elementType.getArrayType();
            for (int i = 1; i < dimensions; i++) {
                type = type.getArrayType();
            }
            return parseArrayInitializer(arrayCreatorRest.arrayInitializer(), type);
        }
        else {
            var stream = new ChildrenStream(arrayCreatorRest);
            List<IRExpression> dimensions = new ArrayList<>();
            int rank = 0;
            while (stream.hasNext()) {
                rank++;
                stream.next();
                if(stream.isNextExpression()) {
                    dimensions.add(parseExpression(stream.nextExpression()));
                }
                stream.next();
            }
            return new TensorExpression(elementType, rank, dimensions);
        }
    }

    private IRExpression parseClassCreatorRest(ClassCreatorRestContext classCreatorRest,
                                               TypeLike typeLike,
                                               List<IRType> methodTypeArguments,
                                               @Nullable IRExpression owner) {
        var args = parseArguments(classCreatorRest.arguments());
        var rawClass = IRUtil.getRawClass(typeLike);
        var resolver = createConstructorResolver(rawClass, args);
        if (classCreatorRest.classBody() == null) {
            if(typeLike instanceof IRType type) {
                return new CreatorExpression(type, resolver.matched, methodTypeArguments, resolver.resolvedArguments, owner);
            }
            else if(typeLike instanceof DiamondType diamondType){
                return new UnresolvedCreatorExpression(
                        diamondType.rawClass(), resolver.matched, resolver.resolvedArguments, methodTypeArguments, owner, t -> t
                );
            }
            else {
                throw new InternalException("Unrecognized TypeLike: " + typeLike.getClass().getName());
            }
        } else {
            var klass = new IRClass(
                    null,
                    IRClassKind.CLASS,
                    pkg,
                    List.of(),
                    List.of(),
                    null,
                    currentMethod()
            );
            if(typeLike instanceof IRType type) {
                klass.setSuperType(type);
                parseClassBody(classCreatorRest.classBody(), klass);
                return new CreatorExpression(
                        klass, resolver.matched, methodTypeArguments, resolver.resolvedArguments, owner
                );
            }
            else if(typeLike instanceof DiamondType diamondType){
                return new UnresolvedCreatorExpression(
                        diamondType.rawClass(), resolver.matched, resolver.resolvedArguments,methodTypeArguments,
                        owner,
                        t -> {
                            klass.setSuperType(t);
                            parseClassBody(classCreatorRest.classBody(), klass);
                            return klass;
                        }
                );
            }
            else {
                throw new InternalException("Unrecognized TypeLike: " + typeLike.getClass().getName());
            }
        }

    }

    private IRExpression parseShiftExpression(ExpressionContext expression, ChildrenStream stream) {
        var first = parseExpression(expression);
        if(stream.isNextLiteral("<")) {
            return new BinaryIRExpression(first, IROperator.LEFT_SHIFT, parseExpression(stream.nextExpression()));
        }
        else {
            stream.next();
            stream.next();
            if(stream.isNextLiteral(">")) {
                return new BinaryIRExpression(
                        first,
                        IROperator.UNSIGNED_RIGHT_SHIFT,
                        parseExpression(stream.nextExpression())
                );
            }
            else {
                return new BinaryIRExpression(
                        first,
                        IROperator.RIGHT_SHIFT,
                        parseExpression(stream.nextExpression())
                );
            }
        }
    }

    private IRExpression parseMethodCall(MethodCallContext methodCall, @Nullable IRExpression prefix) {
        var methodName = methodCall.identifier().getText();
        IRClass klass;
        IRExpression instance;
        if(prefix == null) {
            klass = currentClass();
            instance = new ThisIRExpression(currentType());
        }
        else if(prefix instanceof ClassExpression typePrefix) {
            klass = typePrefix.klass;
            instance = null;
        }
        else if(prefix instanceof OwnerExpression owner) {
            requireNonNull(methodCall.identifier());
            instance = owner;
            klass = IRUtil.getRawClass(owner.type());
        }
        else {
            klass = prefix.klass();
            instance = prefix;
        }

        List<IRExpression> args = NncUtils.get(
                methodCall.expressionList(), this::parseExpressionList, () -> List.of()
        );

        if(methodCall.THIS() != null) {
            var resolver = new CallableResolver<>(
                    NncUtils.map(
                            klass.getConstructors(),
                            m -> new CallableMatcher<>(m.parameterTypes(), m)
                    ),
                    args
            );
            return new ConstructorCallIRExpression(
                    currentType(),
                    List.of(),
                    resolver.matched,
                    resolver.resolvedArguments
            );
        }
        else if(methodCall.SUPER() != null){
            var resolver = new CallableResolver<>(
                    NncUtils.map(
                            klass.getRawSuperClass().getConstructors(),
                            m -> new CallableMatcher<>(m.parameterTypes(), m)
                    ),
                    args
            );
            return new ConstructorCallIRExpression(
                    currentType(),
                    List.of(),
                    resolver.matched,
                    resolver.resolvedArguments
            );
        }
        else {
            var resolver = new CallableResolver<>(
                    NncUtils.map(
                            klass.getMethods(methodName),
                            m -> new CallableMatcher<>(m.parameterTypes(), m)
                    ),
                    args
            );
//            return new MethodCall(instance,
//                    List.of(), resolver.matched,
//                    resolver.resolved, graph);
            return new UnresolvedMethodCall(instance, klass, methodName, args);
        }
    }

    private UnresolvedLambdaExpression parseHalfLambdaExpression(LambdaExpressionContext lambdaExprCtx) {
        return new UnresolvedLambdaExpression(
                getLambdaParameterTypes(lambdaExprCtx.lambdaParameters()),
                LambdaTypeMatchUtil.hasReturnType(lambdaExprCtx),
                lambdaExprCtx,
                this
        );
    }

    public IRLambda parseLambdaExpression(LambdaExpressionContext lambdaExpression, IRType functionType) {
        var block = newCodeBlock();
        var parameters = parseLambdaParameters(
                lambdaExpression.lambdaParameters(),
                IRUtil.getFunctionParameters(functionType)
        );
        symbolTable.enterBlock(block);
        var body = parseLambdaBody(
                lambdaExpression.lambdaBody(),
                b -> parameters.forEach(p -> p.defineVariable(b))
        );
        symbolTable.exitBlock();
        return new IRLambda(functionType, parameters, body);
    }

    public List<IRType> getLambdaParameterTypes(LambdaParametersContext lambdaParamsCtx) {
        if(lambdaParamsCtx.identifier().size() > 0) {
            return NncUtils.fillWith(null, lambdaParamsCtx.identifier().size());
        }
        if(lambdaParamsCtx.formalParameterList() != null) {
            return NncUtils.map(
                    parseFormalParameterList(lambdaParamsCtx.formalParameterList()),
                    IRParameter::type
            );
        }
        if(lambdaParamsCtx.lambdaLVTIList() != null) {
            return NncUtils.fillWith(null, lambdaParamsCtx.lambdaLVTIList().lambdaLVTIParameter().size());
        }
        return List.of();
    }

    private List<IRParameter> parseLambdaParameters(LambdaParametersContext lambdaParameters,
                                                    List<IRParameter> receivingParameters) {
        if(lambdaParameters.identifier().size() > 0) {
            List<IRParameter> result = new ArrayList<>();
            for (int i = 0; i < lambdaParameters.identifier().size(); i++) {
                var receivingParam = receivingParameters.get(i);
                result.add(
                        new IRParameter(
                                receivingParam.immutable(),
                                receivingParam.annotations(),
                                lambdaParameters.identifier(i).getText(),
                                receivingParam.type(),
                                receivingParam.varArgs()
                        )
                );
            }
            return result;
        }
        else if(lambdaParameters.formalParameterList() != null) {
            return parseFormalParameterList(lambdaParameters.formalParameterList());
        }
        else {
            var lvtiParams = lambdaParameters.lambdaLVTIList().lambdaLVTIParameter();
            List<IRParameter> result = new ArrayList<>();
            for (int i = 0; i < lvtiParams.size(); i++) {
                var receivingParam = receivingParameters.get(i);
                var lvtiParam = lvtiParams.get(i);
                var mod = parseVariableModifiers(lvtiParam.variableModifier());
                result.add(
                        new IRParameter(
                                mod.immutable(),
                                mod.annotations(),
                                lvtiParam.identifier().getText(),
                                receivingParam.type(),
                                false
                        )
                );
            }
            return result;
        }
    }

    private LambdaBody parseLambdaBody(LambdaBodyContext lambdaBody, Consumer<CodeBlock> defineVariables) {
        if(lambdaBody.expression() != null) {
            var block = newCodeBlock();
            enterBlock(block);
            defineVariables.accept(block);
            var result = new ExpressionLambdaBody(parseExpression(lambdaBody.expression()), block);
            exitBlock();
            return result;
        }
        else {
            return new BlockLambdaBody(parseBlock(lambdaBody.block(), defineVariables));
        }
    }

    private IRExpression parsePrimary(PrimaryContext primary) {
        ChildrenStream stream = new ChildrenStream(primary);
        if(stream.isNextLiteral("(")) {
            return parseExpression(stream.nextExpression());
        }
        else if(stream.isNextTerminalOf(JavaLexer.THIS)) {
            return new ThisIRExpression(currentType());
        }
        else if(stream.isNextTerminalOf(JavaLexer.SUPER)) {
            var currentClass = (IRClass) currentType();
            return new SuperIRExpression(currentClass.getSuperType());
        }
        else if(stream.isNextInstanceOf(LiteralContext.class)) {
            return parseLiteral(stream.next(LiteralContext.class));
        }
        else if(stream.isNextIdentifier()) {
            return parseExpressionIdentifier(stream.nextIdentifier());
        }
        else if(stream.isNextInstanceOf(TypeTypeOrVoidContext.class)) {
            return parseClassLiteral(stream.next(TypeTypeOrVoidContext.class));
        }
        else {
            return parseGenericMethodCall(stream);
        }
    }

    private IRClass currentType() {
        return symbolTable.currentClass();
    }

    private IBlock currentBlock() {
        return symbolTable.currentBlock();
    }

    private IBlock tryGetCurrentBlock() {
        return symbolTable.isInBlock() ? currentBlock() : null;
    }

    private CodeBlock codeBlock() {
        return symbolTable.currentCodeBlock();
    }

    private CodeBlock newCodeBlock() {
        return new CodeBlock(symbolTable.isInBlock() ? currentBlock() : null);
    }

    private IRMethod currentMethod() {
        return symbolTable.currentMethod();
    }

    private IRClass currentClass() {
        return symbolTable.currentClass();
    }

    private IRClass superClass() {
        return currentClass().getRawSuperClass();
    }

    private IRExpression parseGenericMethodCall(ChildrenStream stream) {
        var typeArgs = parseNonWildcardTypeArguments(
                stream.next(NonWildcardTypeArgumentsContext.class)
        );
        if(stream.isNextInstanceOf(ExplicitGenericInvocationSuffixContext.class)) {
            return parseExplicitGenericInvocationSuffix(
                    stream.next(ExplicitGenericInvocationSuffixContext.class), typeArgs, null
            );
        }
        else {
            var args = parseArguments(stream.next(ArgumentsContext.class));
            var resolver = createConstructorResolver(currentClass(), args);
            return new ConstructorCallIRExpression(currentType(), typeArgs, resolver.matched, resolver.resolvedArguments);
        }
    }

    private List<IRExpression> parseArguments(ArgumentsContext arguments) {
        if(arguments.expressionList() != null) {
            return parseExpressionList(arguments.expressionList());
        }
        else {
            return List.of();
        }
    }

    private List<IRType> parseNonWildcardTypeArguments(NonWildcardTypeArgumentsContext nonWildcardTypeArguments) {
        return NncUtils.map(
                nonWildcardTypeArguments.typeList().typeType(),
                this::parseTypeType
        );
    }

    private IRExpression parseExplicitGenericInvocationSuffix(
            ExplicitGenericInvocationSuffixContext explicitGenericInvocation,
            List<IRType> typeArguments,
            @Nullable IRExpression prefix
    ) {
        if(explicitGenericInvocation.SUPER() != null) {
            return parseSuperSuffix(explicitGenericInvocation.superSuffix(), typeArguments, prefix);
        }
        else {
            IRClass declaringClass;
            IRExpression instance;
            switch (prefix) {
                case null -> {
                    declaringClass = currentClass();
                    instance = new ThisIRExpression(currentType());
                }
                case ClassExpression classExpr -> {
                    declaringClass = classExpr.klass;
                    instance = null;
                }
                case OwnerExpression owner -> {
                    declaringClass = owner.klass();
                    instance = owner;
                }
                default -> throw new InternalException("Invalid prefix for generic invocation: " + prefix);
            }
            var args = parseArguments(explicitGenericInvocation.arguments());
            var resolver = createMethodResolver(
                    declaringClass,
                    explicitGenericInvocation.identifier().getText(),
                    args
            );
            return new MethodCall(
                    instance,
                    typeArguments,
                    resolver.matched,
                    resolver.resolvedArguments,
                    null
            );
        }
    }

    private IRExpression parseSuperSuffix(SuperSuffixContext superSuffix, List<IRType> typeArguments, IRExpression prefix) {
        if(superSuffix.arguments() != null) {
            NncUtils.requireNull(prefix, "Super constructor call can not have prefix");
            var args = parseArguments(superSuffix.arguments());
            var resolver = createConstructorResolver(currentClass().getRawSuperClass(), args);
            return new ConstructorCallIRExpression(currentType(), typeArguments, resolver.matched, resolver.resolvedArguments);
        }
        else {
            List<IRType> methodTypeArgs = NncUtils.get(
                    superSuffix.typeArguments(), this::parseTypeArguments, () -> List.of()
            );
            IRClass declaringClass;
            IRExpression instance;
            switch (prefix) {
                case null -> {
                    declaringClass = currentClass().getRawSuperClass();
                    instance = new ThisIRExpression(currentType());
                }
                case ClassExpression classExpr -> {
                    declaringClass = classExpr.klass.getRawSuperClass();
                    instance = null;
                }
                case OwnerExpression owner -> {
                    declaringClass = owner.klass().getRawSuperClass();
                    instance = owner;
                }
                default -> throw new InternalException("Unexpected prefix for superSuffix: " + prefix);
            }

            List<IRExpression> args = NncUtils.get(
                    superSuffix.arguments(), this::parseArguments, () -> List.of()
            );
            var resolver = createMethodResolver(
                    declaringClass, superSuffix.identifier().getText(), args
            );
            return new MethodCall(
                    instance, methodTypeArgs, resolver.matched, resolver.resolvedArguments,
                    null);
        }
    }

    private IRExpression parseClassLiteral(TypeTypeOrVoidContext typeTypeOrVoid) {
        var klass = (IRClass) parseTypeTypeOrVoid(typeTypeOrVoid);
        return new ConstantExpression(
                ReflectUtils.classForName(klass.getName()),
                IRTypeUtil.fromClass(Class.class)
        );
    }

    private IRExpression parseLiteral(LiteralContext literal) {
        Object value;
        final var text = literal.getText();
        if(literal.integerLiteral() != null) {
            var intLiteral = literal.integerLiteral();
            int radix;
            if(intLiteral.DECIMAL_LITERAL() != null) {
                radix = 10;
            }
            else if(intLiteral.BINARY_LITERAL() != null) {
                radix = 2;
            }
            else if(intLiteral.OCT_LITERAL() != null) {
                radix = 8;
            }
            else {
                radix = 16;
            }
            if(text.endsWith("l") || text.endsWith("L")) {
                value = Long.parseLong(text.substring(0, text.length() - 1), radix);
            }
            else {
                value = Integer.parseInt(text, radix);
            }
        }
        else if(literal.floatLiteral() != null) {
            if(text.endsWith("f") || text.endsWith("F")) {
                value = Float.parseFloat(text.substring(0, text.length() - 1));
            }
            else {
                if(text.endsWith("d") || text.endsWith("D")) {
                    value = Double.parseDouble(text.substring(0, text.length() - 1));
                }
                else {
                    value = Double.parseDouble(text);
                }
            }
        }
        else if(literal.CHAR_LITERAL() != null) {
            value = TranspileUtil.unescapePerlString(
                    literal.getText().substring(1, literal.getText().length() - 1)
            );
        }
        else if(literal.TEXT_BLOCK() != null) {
            value = TranspileUtil.unescapeStringBlock(
                    text.substring(3, text.length() - 3)
            );
        }
        else if(literal.BOOL_LITERAL() != null) {
            value = Boolean.parseBoolean(literal.getText());
        }
        else {
            value = null;
        }
        return ConstantExpression.create(value);
    }


    private IRExpression parseExpressionIdentifier(IdentifierContext identifier) {
        var value = symbolTable.resolveValue(identifier.getText());
        if(value != null) {
            if (value instanceof IRField field) {
                return new FieldIRExpression(new ThisIRExpression(currentType()), field);
            }
            if (value instanceof LocalVariable var) {
                return new LocalVariableIRExpression(var);
            } else {
                throw new InternalException("Unrecognized value type: " + value.getClass().getName());
            }
        }
        var klass = symbolTable.tryResolveClass(identifier.getText());
        if(klass != null) {
            return new ClassExpression(klass);
        }
        else {
            return new PackageExpression(new IRPackage(identifier.getText()));
        }
    }

    private void writeIdentifier(IdentifierContext identifier) {
        writeIdentifier(identifier, false);
    }

    private void writeIdentifier(IdentifierContext identifier, boolean nullable) {
        if(nullable) {
            writeText(identifier.getText() + "?");
        }
        else {
            writeText(identifier.getText());
        }
    }

    private void writeText(String text) {
        String mappedText = IDENTIFIER_MAP.get(text);
        out.writeIdentifier(mappedText != null ? mappedText : text);
    }

    private void parseClassBody(ClassBodyContext classBody, IRClass klass) {
        symbolTable.enterBlock(new ClassBlock(tryGetCurrentBlock(), klass));
        var bodyDeclarations = new ArrayList<>(classBody.classBodyDeclaration());
        bodyDeclarations.sort(Comparator.comparing(d ->
                d.memberDeclaration().methodDeclaration() != null ||
                        d.memberDeclaration().fieldDeclaration() != null ? 1 : 0
        ));
        List<Runnable> postInitializers = new ArrayList<>();
        for (ClassBodyDeclarationContext bodyDecl : bodyDeclarations) {
            parseClassBodyDeclaration(bodyDecl, klass, postInitializers::add);
        }
        postInitializers.forEach(Runnable::run);
        symbolTable.exitBlock();
    }

    private void parseInterfaceBody(InterfaceBodyContext interfaceBodyCtx, IRClass klass) {
        var bodyDeclarations = new ArrayList<>(interfaceBodyCtx.interfaceBodyDeclaration());
        bodyDeclarations.sort(Comparator.comparing(d ->
                d.interfaceMemberDeclaration().interfaceMethodDeclaration() != null ? 1 : 0
        ));
        List<Runnable> postInitializers = new ArrayList<>();
        for (InterfaceBodyDeclarationContext bodyDecl : bodyDeclarations) {
            parseInterfaceBodyDeclaration(bodyDecl, klass, postInitializers::add);
        }
        postInitializers.forEach(Runnable::run);
    }

    private void parseInterfaceBodyDeclaration(
            InterfaceBodyDeclarationContext interfaceBodyDeclCtx,
            IRClass declaringClass,
            Consumer<Runnable> addPostInitializer) {
        List<IRAnnotation> annotations = new ArrayList<>();
        List<Modifier> modifiers = new ArrayList<>();
        parseModifiers(interfaceBodyDeclCtx.modifier(), annotations, modifiers);

        var memberDecl = interfaceBodyDeclCtx.interfaceMemberDeclaration();
        if(memberDecl.interfaceMethodDeclaration() != null) {
            parseInterfaceMethod(
                    modifiers,
                    annotations,
                    memberDecl.interfaceMethodDeclaration().interfaceMethodModifier(),
                    null,
                    memberDecl.interfaceMethodDeclaration().interfaceCommonBodyDeclaration(),
                    declaringClass,
                    addPostInitializer);
        }
        else if(memberDecl.genericInterfaceMethodDeclaration() != null) {
            parseInterfaceMethod(
                    modifiers,
                    annotations,
                    memberDecl.genericInterfaceMethodDeclaration().interfaceMethodModifier(),
                    memberDecl.genericInterfaceMethodDeclaration().typeParameters(),
                    memberDecl.genericInterfaceMethodDeclaration().interfaceCommonBodyDeclaration(),
                    declaringClass,
                    addPostInitializer
            );
        }
        else if(memberDecl.classDeclaration() != null) {
            parseClass(modifiers, annotations, memberDecl.classDeclaration(), declaringClass, null, addPostInitializer);
        }
        else if(memberDecl.interfaceDeclaration() != null) {
            parseInterface(modifiers, annotations, memberDecl.interfaceDeclaration(), declaringClass, null, addPostInitializer);
        }
        else if(memberDecl.enumDeclaration() != null) {
            // TODO to implement
        }
        else if(memberDecl.recordDeclaration() != null) {
            // TODO to implement
        }
    }

    private void parseClassBodyDeclaration(
            ClassBodyDeclarationContext classBodyDecl,
            IRClass declaringClass,
            Consumer<Runnable> addPostInitializer) {
        if(classBodyDecl.block() != null) {
            return;
        }
        List<IRAnnotation> annotations = new ArrayList<>();
        List<Modifier> modifiers = new ArrayList<>();
        parseModifiers(classBodyDecl.modifier(), annotations, modifiers);

        var memberDecl = classBodyDecl.memberDeclaration();
        if(memberDecl.fieldDeclaration() != null) {
            parseField(modifiers, annotations, memberDecl.fieldDeclaration(), declaringClass);
        }
        else if(memberDecl.methodDeclaration() != null ) {
            parseMethod(modifiers, annotations, null, memberDecl.methodDeclaration(), declaringClass, addPostInitializer);
        }
        else if(memberDecl.genericMethodDeclaration() != null) {
            parseMethod(modifiers, annotations, memberDecl.genericMethodDeclaration().typeParameters(),
                    memberDecl.genericMethodDeclaration().methodDeclaration(), declaringClass, addPostInitializer);
        }
        else if(memberDecl.classDeclaration() != null) {
            parseClass(modifiers, annotations, memberDecl.classDeclaration(), declaringClass, null, addPostInitializer);
        }
        else if(memberDecl.interfaceDeclaration() != null) {
            parseInterface(modifiers, annotations, memberDecl.interfaceDeclaration(), declaringClass, null, addPostInitializer);
        }
        else if(memberDecl.enumDeclaration() != null) {
            // TODO to implement
        }
        else if(memberDecl.recordDeclaration() != null) {
            // TODO to implement
        }
    }

    private void parseField(List<Modifier> modifiers,
                            List<IRAnnotation> annotations,
                            FieldDeclarationContext fieldDeclCtx,
                            IRClass declaringClass) {
        for (VariableDeclaratorContext variable : fieldDeclCtx.variableDeclarators().variableDeclarator()) {
            var field = new IRField(
                    modifiers,
                    annotations,
                    variable.variableDeclaratorId().identifier().getText(),
                    parseTypeType(fieldDeclCtx.typeType()),
                    declaringClass
            );
            declaringClass.addField(field);
        }
    }

    private IRMethod parseInterfaceMethod(
            List<Modifier> modifiers,
            List<IRAnnotation> annotations,
            List<InterfaceMethodModifierContext> interfaceMethodModifierContexts,
            TypeParametersContext typeParametersContext,
            InterfaceCommonBodyDeclarationContext itCommonBodyDeclCtx,
            IRClass declaringClass,
            Consumer<Runnable> addPostInitializer
    ) {
        parseInterfaceMethodModifiers(interfaceMethodModifierContexts, modifiers, annotations);
        annotations.addAll(NncUtils.map(itCommonBodyDeclCtx.annotation(), this::parseAnnotation));
        List<IRType> throwsTypes = new ArrayList<>();
        if(itCommonBodyDeclCtx.THROWS() != null) {
            throwsTypes.addAll(
                    NncUtils.map(
                            parseQualifiedNameList(itCommonBodyDeclCtx.qualifiedNameList()),
                            IRTypeUtil::classForName
                    )
            );
        }
        var stream = new ChildrenStream(itCommonBodyDeclCtx);
        var method = new IRMethod(
                itCommonBodyDeclCtx.identifier().getText(),
                modifiers,
                annotations,
                declaringClass
        );
        parseTypeParameters(typeParametersContext, method);
        method.initialize(
                parseTypeTypeOrVoid(itCommonBodyDeclCtx.typeTypeOrVoid(), stream.countLiteral("[")),
                parseFormalParameters(itCommonBodyDeclCtx.formalParameters()),
                throwsTypes
        );
        if(itCommonBodyDeclCtx.methodBody().block() != null) {
            addPostInitializer.accept(
                    () -> method.setBody(parseBlock(itCommonBodyDeclCtx.methodBody().block()))
            );
        }
        return method;
    }

    private List<String> parseQualifiedNameList(QualifiedNameListContext qualifiedNameListContext) {
        return NncUtils.map(
                qualifiedNameListContext.qualifiedName(),
                this::getQualifiedName
        );
    }

    private void parseInterfaceMethodModifiers(
            List<InterfaceMethodModifierContext> modifierContexts,
            List<Modifier> modifiers,
            List<IRAnnotation> annotations
    ) {
        for (InterfaceMethodModifierContext modCtx : modifierContexts) {
            if(modCtx.annotation() != null) {
                annotations.add(parseAnnotation(modCtx.annotation()));
            }
            else {
                modifiers.add(getModifier(
                        modCtx.getChild(TerminalNode.class, 0).getSymbol().getType()
                ));
            }
        }
    }

    private void parseMethod(
            List<Modifier> modifiers,
            List<IRAnnotation> annotations,
            @Nullable TypeParametersContext typeParametersContext,
            MethodDeclarationContext methodDeclCtx,
            IRClass declaringClass,
            Consumer<Runnable> addPostInitializer) {
        List<IRType> throwsTypes = methodDeclCtx.THROWS() != null ?
                parseTypesFromQualifiedNameList(methodDeclCtx.qualifiedNameList()) : List.of();
        var method = new IRMethod(
                methodDeclCtx.identifier().getText(),
                modifiers,
                annotations,
                declaringClass
        );
        if(typeParametersContext != null) {
            parseTypeParameters(typeParametersContext, method);
        }
        method.initialize(
                parseTypeTypeOrVoid(methodDeclCtx.typeTypeOrVoid(), countLiterals(methodDeclCtx, "[")),
                parseFormalParameters(methodDeclCtx.formalParameters()),
                throwsTypes
        );
        if(methodDeclCtx.methodBody().block() != null) {
            addPostInitializer.accept(() -> method.setBody(
                    parseBlock(
                            methodDeclCtx.methodBody().block(),
                            b -> method.parameters().forEach(p -> p.defineVariable(b))
                    )
            ));
        }
    }

    @SuppressWarnings("SameParameterValue")
    private int countLiterals(ParserRuleContext context, String literal) {
        return new ChildrenStream(context).countLiteral(literal);
    }

    private List<IRType> parseTypesFromQualifiedNameList(QualifiedNameListContext qualifiedNameListContext) {
        return NncUtils.map(
                parseQualifiedNameList(qualifiedNameListContext),
                IRTypeUtil::classForName
        );
    }

    private IRType parseTypeFromQualifiedName(QualifiedNameContext qualifiedNameContext) {
        return IRTypeUtil.classForName(symbolTable.getQualifiedName(qualifiedNameContext.getText()));
    }

    private IRClass parseInterface(
            List<Modifier> modifiers,
            List<IRAnnotation> annotations,
            InterfaceDeclarationContext interfaceDeclCtx,
            IRClass declaringClass,
            IRMethod declaringMethod,
            Consumer<Runnable> addPostInitializer
    ) {
        ChildrenStream stream = new ChildrenStream(interfaceDeclCtx);
        stream.next(TerminalNode.class);
        final String name = stream.next(IdentifierContext.class).getText();

        var klass = new IRClass(
                name,
                IRClassKind.INTERFACE,
                pkg,
                modifiers,
                annotations,
                declaringClass,
                declaringMethod
        );

        addPostInitializer.accept(() -> {
            if (stream.isNextInstanceOf(TypeParametersContext.class)) {
                parseTypeParameters(stream.next(TypeParametersContext.class), klass);
            }
            if (stream.skipIfNextSymbolOf(JavaLexer.EXTENDS)) {
                klass.setSuperType(parseTypeType(stream.next(TypeTypeContext.class)));
            }
            if (stream.skipIfNextSymbolOf(JavaLexer.PERMITS)) {
                stream.next();
            }
            if (interfaceDeclCtx.interfaceBody() != null) {
                parseInterfaceBody(interfaceDeclCtx.interfaceBody(), klass);
            }
        });

        return klass;
    }

    private IRClass parseClassAndInit(
            List<Modifier> modifiers,
            List<IRAnnotation> annotations,
            ClassDeclarationContext classDeclCtx,
            IRClass declaringClass,
            IRMethod declaringMethod
    ) {
        var initializers = new ArrayList<Runnable>();
        var klass = parseClass(modifiers, annotations, classDeclCtx, declaringClass, declaringMethod, initializers::add);
        initializers.forEach(Runnable::run);
        return  klass;
    }

    private IRClass parseInterfaceAndInit(
            List<Modifier> modifiers,
            List<IRAnnotation> annotations,
            InterfaceDeclarationContext interfaceDeclCtx,
            IRClass declaringClass,
            IRMethod declaringMethod
    ) {
        var initializers = new ArrayList<Runnable>();
        var klass = parseInterface(modifiers, annotations, interfaceDeclCtx, declaringClass, declaringMethod, initializers::add);
        initializers.forEach(Runnable::run);
        return  klass;
    }

    private IRClass parseClass(
            List<Modifier> modifiers,
            List<IRAnnotation> annotations,
            ClassDeclarationContext classDeclCtx,
            IRClass declaringClass,
            IRMethod declaringMethod,
            Consumer<Runnable> addPostInitializer
    ) {
        ChildrenStream stream = new ChildrenStream(classDeclCtx);
        stream.next(TerminalNode.class);
        final String name = stream.next(IdentifierContext.class).getText();

        var klass = IRTypeUtil.internClass(
                new IRClass(
                    name,
                    IRClassKind.CLASS,
                    pkg,
                    modifiers,
                    annotations,
                    declaringClass,
                    declaringMethod
                )
        );

        addPostInitializer.accept(() -> {
            if (stream.isNextInstanceOf(TypeParametersContext.class)) {
                parseTypeParameters(stream.next(TypeParametersContext.class), klass);
            }
            if (stream.skipIfNextSymbolOf(JavaLexer.EXTENDS)) {
                klass.setSuperType(parseTypeType(stream.next(TypeTypeContext.class)));
            }
            if (stream.skipIfNextSymbolOf(JavaLexer.IMPLEMENTS)) {
                klass.setInterfaces(NncUtils.map(stream.next(TypeListContext.class).typeType(), this::parseTypeType));
            }
            if (stream.skipIfNextSymbolOf(JavaLexer.PERMITS)) {
                stream.next();
            }
            if (classDeclCtx.classBody() != null) {
                parseClassBody(classDeclCtx.classBody(), klass);
            }
        });
        return klass;
    }

    private void parseModifiers(List<ModifierContext> modifierContexts,
                                List<IRAnnotation> annotations,
                                List<Modifier> modifiers) {

        List<ClassOrInterfaceModifierContext> classOrInterfaceMods = NncUtils.mapAndFilter(
                modifierContexts, ModifierContext::classOrInterfaceModifier, Objects::nonNull
        );
        parseClassOrInterfaceModifiers(classOrInterfaceMods, annotations, modifiers);

        for (ModifierContext modCtx : modifierContexts) {
            if(modCtx.classOrInterfaceModifier() == null) {
                modifiers.add(getModifier(
                        modCtx.getChild(TerminalNode.class, 0).getSymbol().getType()
                ));
            }
        }
    }

    private IRAnnotation parseAnnotation(AnnotationContext annotationContext) {
        if(annotationContext.qualifiedName() != null) {
            return new IRAnnotation(
                    ReflectUtils.classForName(
                            symbolTable.getQualifiedName(getQualifiedName(annotationContext.qualifiedName()))
                    ).asSubclass(Annotation.class)
            );
        }
        else {
            return null;
        }
    }

    private IRType parseTypeTypeOrVoid(TypeTypeOrVoidContext typeTypeOrVoid) {
        return parseTypeTypeOrVoid(typeTypeOrVoid, 0);
    }

    private IRType parseTypeTypeOrVoid(TypeTypeOrVoidContext typeTypeOrVoid, int extraDimensions) {
        if(typeTypeOrVoid.typeType() != null) {
            return parseTypeType(typeTypeOrVoid.typeType(), extraDimensions);
        }
        else {
            return IRTypeUtil.fromPrimitiveClass(void.class);
        }
    }

    private List<IRParameter> parseFormalParameters(FormalParametersContext formalParameters) {
        if(formalParameters.formalParameterList() != null) {
            return parseFormalParameterList(formalParameters.formalParameterList());
        }
        else {
            return List.of();
        }
    }

    private VariableModifier parseVariableModifiers(List<VariableModifierContext> varModContexts) {
        return parseVariableModifiers(varModContexts, List.of());
    }

    private VariableModifier parseVariableModifiers(List<VariableModifierContext> varModContexts,
                                                    List<AnnotationContext> extraAnnotations) {
        boolean immutable = false;
        List<IRAnnotation> annotations = new ArrayList<>(
                NncUtils.map(extraAnnotations, this::parseAnnotation)
        );
        for (VariableModifierContext mod : varModContexts) {
            if(mod.annotation() != null) {
                annotations.add(parseAnnotation(mod.annotation()));
            }
            else if(!immutable && mod.FINAL() != null){
                immutable = true;
            }
        }
        return new VariableModifier(annotations, immutable);
    }

    private List<IRParameter> parseFormalParameterList(FormalParameterListContext formalParameterList) {
        List<IRParameter> parameters = new ArrayList<>();
        formalParameterList.formalParameter().forEach(
                p -> parameters.add(parseFormalParameter(p))
        );
        if(formalParameterList.lastFormalParameter() != null) {
            parameters.add(parseLastFormalParameter(formalParameterList.lastFormalParameter()));
        }
        return parameters;
    }

    private IRParameter parseFormalParameter(FormalParameterContext formalParameter) {
        var variableId = formalParameter.variableDeclaratorId();
        var mod = parseVariableModifiers(formalParameter.variableModifier());
        return new IRParameter(
                mod.immutable,
                mod.annotations,
                variableId.identifier().getText(),
                parseTypeType(formalParameter.typeType(), countDimensions(variableId)),
                false
        );
    }

    private int countDimensions(ParserRuleContext context) {
        return new ChildrenStream(context).countLiteral("[");
    }

    private IRParameter parseLastFormalParameter(LastFormalParameterContext lastFormalParameter) {
        writeIdentifierAndType(
                () -> {
                    out.writeKeyword(TRIPLE_DOTS);
                    writeIdentifier(lastFormalParameter.variableDeclaratorId().identifier());
                },
                () -> writeTypeType(lastFormalParameter.typeType()),
                getDimension(lastFormalParameter.variableDeclaratorId())
        );
        var mod = parseVariableModifiers(lastFormalParameter.variableModifier());
        var variableIdd = lastFormalParameter.variableDeclaratorId();
        return new IRParameter(
                mod.immutable,
                mod.annotations,
                variableIdd.identifier().getText(),
                parseTypeType(lastFormalParameter.typeType(), countDimensions(variableIdd)),
                true
        );
    }

    private IRClass parseEnum(EnumDeclarationContext enumDeclaration, IRClass declaringClass) {
        return null;
    }

    private IRClass parseRecord(RecordDeclarationContext recordDeclaration, IRClass declaringClass) {
        return null;
    }

    private void writeClassOrInterfaceModifier(ClassOrInterfaceModifierContext classOrInterfaceModifier) {
        var child = classOrInterfaceModifier.getChild(0);
        if(child instanceof TerminalNode terminalNode) {
            TsLexicon tsModifier = MODIFIER_TO_LEXICON.get(terminalNode.getSymbol().getType());
            if(tsModifier != null) {
                out.writeKeyword(tsModifier);
            }
        }
    }

    private void writePrimitiveType(PrimitiveTypeContext primitiveType) {
        if(primitiveType.BOOLEAN() != null) {
            out.writeIdentifier("boolean");
        }
        else if(primitiveType.BYTE() != null || primitiveType.SHORT() != null || primitiveType.INT() != null
                || primitiveType.LONG() != null || primitiveType.FLOAT() != null || primitiveType.DOUBLE() != null) {
            out.writeIdentifier("number");
        }
        else if(primitiveType.CHAR() != null) {
            out.writeIdentifier("string");
        }
        else {
            throw new InternalException("Unrecognized primitive type " + primitiveType.getText());
        }
    }

    private void writeClassOrInterfaceType(ClassOrInterfaceTypeContext classOrInterfaceType) {
        ChildrenStream stream = new ChildrenStream(classOrInterfaceType);
        while (stream.isNextIdentifier()) {
            writeIdentifier(stream.nextIdentifier());
            if(stream.isNextInstanceOf(TypeArgumentsContext.class)) {
                parseTypeArguments(stream.next(TypeArgumentsContext.class));
            }
            out.writeKeyword(TsLexicon.DOT);
        }
        writeTypeIdentifier(stream.next(TypeIdentifierContext.class));
        if(stream.isNextInstanceOf(TypeArgumentsContext.class)) {
            parseTypeArguments(stream.next(TypeArgumentsContext.class));
        }
    }

    private void writeAnnotation(AnnotationContext annotation) {
    }

    private List<IRType> parseTypeArguments(TypeArgumentsContext typeArguments) {
        return NncUtils.map(
                typeArguments.typeArgument(),
                this::parseTypeArgument
        );
    }

    private IRType parseTypeArgument(TypeArgumentContext typeArgument) {
        if(typeArgument.typeType() == null) {
            return IRWildCardType.asterisk();
        }
        else {
            var type = parseTypeType(typeArgument.typeType());
            if (typeArgument.SUPER() != null) {
                return IRWildCardType.superOf(type);
            } else if (typeArgument.EXTENDS() != null) {
                return IRWildCardType.extensionOf(type);
            }
            else {
                return type;
            }
        }
    }

    private void writeTypeIdentifier(TypeIdentifierContext typeIdentifier) {
        writeText(typeIdentifier.getText());
    }

    private <D extends GenericDeclaration<D>> List<TypeVariable<D>> parseTypeParameters(
            TypeParametersContext typeParameters, D genericDeclaration) {
        return NncUtils.map(typeParameters.typeParameter(), p -> parseTypeParameter(p, genericDeclaration));
    }

    private <D extends GenericDeclaration<D>> TypeVariable<D> parseTypeParameter(
            TypeParameterContext typeParameter, D genericDeclaration) {
        List<IRType> upperBounds = new ArrayList<>();
        if(typeParameter.typeBound() != null) {
            for (TypeTypeContext typeType : typeParameter.typeBound().typeType()) {
                upperBounds.add(parseTypeType(typeType));
            }
        }
        else {
            upperBounds.add(fromClass(Object.class));
        }
        return new TypeVariable<>(genericDeclaration, typeParameter.getText(), upperBounds);
    }

    private void enterBlock(CodeBlock block) {
        enterBlock(true, block);
    }

    private void enterBlock(boolean pushBlock, IBlock block) {
        if(pushBlock) {
            symbolTable.enterBlock(block);
        }
    }

    private void exitBlock() {
        exitBlock(true);
    }

    private void exitBlock(boolean popBlock) {
        if(popBlock) {
            symbolTable.exitBlock();
        }
    }

    private interface PseudoExpression extends IRExpression {

        @Override
        default IRType type() {
            throw new UnsupportedOperationException();
        }
    }

    private record ClassExpression(
            IRClass klass
    ) implements PseudoExpression {
    }

    private record PackageExpression(
            IRPackage irPackage
    ) implements PseudoExpression {

        public IRExpression getMember(String name) {
            var klass = irPackage.tryGetClass(name);
            if(klass != null) {
                return new ClassExpression(klass);
            }
            else {
                return new PackageExpression(irPackage.subPackage(name));
            }
        }
    }

    private record VariableModifier(
            List<IRAnnotation> annotations,
            boolean immutable
    )  {

    }

    private record CallableMatcher<T>(
            List<IRType> parameterTypes,
            T target
    ) {
    }

    private IRExpression resolveDirectly(IRType type, IRExpression expression) {
        return new CallableResolver<>(
                List.of(
                        new CallableMatcher<>(List.of(type), type)
                ),
                List.of(expression)
        ).resolvedArguments.get(0);
    }

    private CallableResolver<IRMethod> createMethodResolver(
            IRClass klass, String methodName, List<IRExpression> arguments
    ) {
        return new CallableResolver<>(
                NncUtils.map(
                        klass.getMethods(methodName),
                        m -> new CallableMatcher<>(
                                m.parameterTypes(),
                                m
                        )
                ),
                arguments
        );
    }

    private CallableResolver<IRConstructor> createConstructorResolver(
            IRClass klass, List<IRExpression> arguments
    ) {
        return new CallableResolver<>(
                NncUtils.map(
                        klass.getConstructors(),
                        m -> new CallableMatcher<>(
                                m.parameterTypes(),
                                m
                        )
                ),
                arguments
        );
    }

    private class CallableResolver<T> {

        private final List<CallableMatcher<T>> matchers;
        private final List<IRExpression> arguments;
        private T matched;
        private List<IRExpression> resolvedArguments;

        private CallableResolver(List<CallableMatcher<T>> matchers, List<IRExpression> arguments) {
            this.matchers = matchers;
            this.arguments = arguments;
            resolve();
        }

        private void resolve() {
            for (CallableMatcher<T> matcher : matchers) {
                if(isMatched(matcher)) {
                    matched = matcher.target;
                    resolvedArguments =  resolveArguments(matcher);
                    return;
                }
            }
            throw new InternalException("Can't find a matcher for the expressions");
        }

        private boolean isMatched(CallableMatcher<T> matcher) {
            var paramTypes = matcher.parameterTypes;
            if(paramTypes.size() != arguments.size()) {
                return false;
            }
            for (int i = 0; i < paramTypes.size(); i++) {
                if(!matchArgument(paramTypes.get(i), arguments.get(i))) {
                    return false;
                }
            }
            return true;
        }

        private boolean matchArgument(IRType parameterType, IRExpression argument) {
            if(IRUtil.isConstantNull(argument)) {
                return true;
            }
            else if(argument instanceof UnresolvedExpression unresolvedExpression) {
                return unresolvedExpression.isTypeMatched(parameterType);
            }
            else {
                return parameterType.isAssignableFrom(argument.type());
            }
        }

        private List<IRExpression> resolveArguments(CallableMatcher<T> matcher) {
            List<IRExpression> result = new ArrayList<>();
            for (int i = 0; i < arguments.size(); i++) {
                var arg = arguments.get(i);
                if(arg instanceof UnresolvedExpression unresolvedExpression) {
                    result.add(unresolvedExpression.resolve(matcher.parameterTypes.get(i)));
                }
                else {
                    result.add(arg);
                }
            }
            return result;
        }

        private IRExpression resolveExpression(IRType type, UnresolvedExpression expression) {
            if(expression instanceof UnresolvedLambdaExpression unresolvedLambdaExpression) {
                return resolveLambdaExpression(type, unresolvedLambdaExpression);
            }
            throw new InternalException("Unrecognized expression " + expression);
        }

        private IRExpression resolveLambdaExpression(IRType type, UnresolvedLambdaExpression unresolvedLambdaExpression) {
            return parseLambdaExpression(unresolvedLambdaExpression.context(), type);
        }

    }


}
