//package tech.metavm.transpile;
//
//import javassist.util.proxy.ProxyObject;
//import org.antlr.v4.runtime.CharStream;
//import org.antlr.v4.runtime.CharStreams;
//import org.antlr.v4.runtime.CommonTokenStream;
//import org.antlr.v4.runtime.RuleContext;
//import org.antlr.v4.runtime.tree.ParseTree;
//import org.antlr.v4.runtime.tree.TerminalNode;
//import org.elasticsearch.common.TriFunction;
//import tech.metavm.util.InternalException;
//import tech.metavm.util.NncUtils;
//import tech.metavm.util.ReflectUtils;
//
//import javax.annotation.Nullable;
//import java.io.IOException;
//import java.util.*;
//import java.util.function.*;
//
//import static tech.metavm.transpile.JavaParser.*;
//import static tech.metavm.transpile.TsLexicon.*;
//
//public class Transpiler {
//
//    public static final Map<Integer, TsLexicon> MODIFIER_MAP = Map.of(
//            JavaLexer.PUBLIC, TsLexicon.PUBLIC,
//            JavaLexer.PRIVATE, TsLexicon.PRIVATE,
//            JavaLexer.STATIC, TsLexicon.STATIC,
//            JavaLexer.ABSTRACT, TsLexicon.ABSTRACT,
//            JavaLexer.FINAL, READONLY
//    );
//
//    private static final Map<String, String> IDENTIFIER_MAP = Map.of(
////            "Object", "any",
////            "String", "string",
//            "List", "Array",
//            "ArrayList", "Array",
//            "null", "undefined",
//            "==", "===",
//            "!=", "!==",
//            "InternalException", "Error"
//    );
//
//    public static final Set<String> IMPORT_PKG_BLACKLIST = Set.of("java", "javax");
//
//    public static Transpiler createFromFileName(String fileName) throws IOException {
//        return new Transpiler(CharStreams.fromFileName(fileName));
//    }
//
//    public static Transpiler createFromString(String content) {
//        return new Transpiler(CharStreams.fromString(content));
//    }
//
//    private final JavaParser parser;
//    private final PackageScanner packageScanner = new PackageScanner();
//    private final LexiconOutputStream out = new LexiconOutputStream();
//    private final SymbolTable symbolTable = new SymbolTable();
//    private String[] packageName;
//    private final ExpressionTypeResolver expressionTypeResolver = new ExpressionTypeResolver(symbolTable);
//
//    private Transpiler(CharStream stream) {
//        JavaLexer lexer = new JavaLexer(stream);
//        parser = new JavaParser(new CommonTokenStream(lexer));
//    }
//
//    String transpile() {
//        addBuiltinTypes();
//        write();
//        return out.toString();
//    }
//
//    private void write() {
//        var compilationUnit = parser.compilationUnit();
//        boolean defaultExported = false;
//        writeIfPresent(compilationUnit.packageDeclaration(), this::writePackageDeclaration);
//        writeList(compilationUnit.importDeclaration(), this::writeImportDeclaration, () -> {});
//        for (int i = 0; i < compilationUnit.typeDeclaration().size(); i++) {
//            if(writeTypeDeclaration(compilationUnit.typeDeclaration(i), defaultExported)) {
//                defaultExported = true;
//            }
//        }
//    }
//
//    private void addBuiltinTypes() {
//        symbolTable.addImport(String.class);
//        symbolTable.addImport(Integer.class);
//        symbolTable.addImport(Long.class);
//        symbolTable.addImport(Float.class);
//        symbolTable.addImport(Double.class);
//        symbolTable.addImport(Boolean.class);
//        symbolTable.addImport(Character.class);
//        symbolTable.addImport(Byte.class);
//        symbolTable.addImport(Short.class);
//        symbolTable.addImport(Float.class);
//        symbolTable.addImport(Date.class);
//
//        symbolTable.addImport(Set.class);
//        symbolTable.addImport(List.class);
//        symbolTable.addImport(Map.class);
//        symbolTable.addImport(HashSet.class);
//        symbolTable.addImport(TreeSet.class);
//        symbolTable.addImport(ArrayList.class);
//        symbolTable.addImport(java.util.LinkedList.class);
//        symbolTable.addImport(HashMap.class);
//        symbolTable.addImport(LinkedHashMap.class);
//        symbolTable.addImport(TreeMap.class);
//        symbolTable.addImport(IdentityHashMap.class);
//        symbolTable.addImport(Collection.class);
//        symbolTable.addImport(Collections.class);
//        symbolTable.addImport(Object.class);
//        symbolTable.addImport(Objects.class);
//        symbolTable.addImport(Function.class);
//        symbolTable.addImport(Predicate.class);
//        symbolTable.addImport(Consumer.class);
//        symbolTable.addImport(BiFunction.class);
//        symbolTable.addImport(TriFunction.class);
//        symbolTable.addImport(Runnable.class);
//
//        symbolTable.addImport(IntFunction.class);
//        symbolTable.addImport(IntConsumer.class);
//        symbolTable.addImport(IntPredicate.class);
//
//        symbolTable.addImport(ProxyObject.class);
//        symbolTable.addImport(Class.class);
//    }
//
//    private boolean writeTypeDeclaration(TypeDeclarationContext typeDeclaration, boolean defaultExported) {
//        Set<Integer> modifiers = getModifierTypes0(typeDeclaration.classOrInterfaceModifier());
//        if(modifiers.contains(JavaLexer.PUBLIC)) {
//            out.writeKeyword(EXPORT);
//            if(!defaultExported) {
//                out.writeKeyword(TsLexicon.DEFAULT);
//                defaultExported = true;
//            }
//        }
//        var classDeclaration = typeDeclaration.classDeclaration();
//        if(classDeclaration != null) {
//            writeClassDeclaration(classDeclaration, modifiers);
//        }
//        var interfaceDeclaration = typeDeclaration.interfaceDeclaration();
//        if(interfaceDeclaration != null) {
//            writeInterfaceDeclaration(interfaceDeclaration);
//        }
//        return defaultExported;
//    }
//
//    private void writeImportDeclaration(ImportDeclarationContext importDeclaration) {
//        var importText = parseQualifiedName(importDeclaration.qualifiedName());
//        if(shouldIgnoreImport(importText)) {
//            return;
//        }
//        if(importDeclaration.STATIC() == null) {
//            if(importDeclaration.DOT() == null) {
//                writeImport(importText);
//            }
//            else {
//                var packageRoot = importText.split("\\.")[0];
//                if(!IMPORT_PKG_BLACKLIST.contains(packageRoot)) {
////                    var reflections = new Reflections(importText, new SubTypesScanner(false));
////                    var allImports = NncUtils.filterAndMap(
////                            reflections.getSubTypesOf(Object.class),
////                            c -> !(Annotation.class.isAssignableFrom(c)),
////                            Class::getName
////                    );
//                    var allImports = packageScanner.getClassNames(importText);
//                    allImports.forEach(this::writeImport);
//                }
//            }
//        }
//        else {
//            if(importDeclaration.DOT() == null) {
//                var lastDotIdx = importText.lastIndexOf('.');
//                NncUtils.requirePositive(lastDotIdx);
//                Class<?> klass = ReflectUtils.classForName(importText.substring(0, lastDotIdx));
//                String importName = importText.substring(lastDotIdx + 1);
//                symbolTable.addStaticImport(klass, importName);
//                writeImport(importName);
//            }
//            else {
//                Class<?> klass = ReflectUtils.classForName(importText);
//                symbolTable.addStaticImportAll(klass);
//                writeImport(importText);
//            }
//        }
//    }
//
//    private boolean shouldIgnoreImport(String importText) {
//        for (String s : IMPORT_PKG_BLACKLIST) {
//            if(importText.startsWith(s)) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private void writeImport(String importText) {
//        symbolTable.addImport(ReflectUtils.classForName(importText));
//        var tsImport = transformImport(importText);
//        out.writeKeyword(TsLexicon.IMPORT);
//        out.writeIdentifier(tsImport);
//        out.writeNewLine();
//    }
//
//    private String transformImport(String javaImport) {
//        var javaImportSplits = javaImport.split("\\.");
//        var className = javaImportSplits[javaImportSplits.length - 1];
//        var builder = new StringBuilder(className);
//        builder.append(" from '");
//        int i;
//        for (i = 0; i < Math.min(javaImportSplits.length - 1, packageName.length); i++) {
//            if(!javaImportSplits[i].equals(packageName[i])) {
//                break;
//            }
//        }
//        if(i == packageName.length) {
//            builder.append("./");
//        }
//        else {
//            builder.append("../".repeat(Math.max(0, packageName.length - i)));
//        }
//        for (int j = i; j < javaImportSplits.length - 1; j++) {
//            builder.append(javaImportSplits[j]).append('/');
//        }
//        builder.append(className).append("'");
//        return builder.toString();
//    }
//
//    private String parseQualifiedName(QualifiedNameContext qualifiedName) {
//        return NncUtils.join(qualifiedName.identifier(), RuleContext::getText, ".");
//    }
//
//    @SuppressWarnings("CommentedOutCode")
//    private void writePackageDeclaration(PackageDeclarationContext packageDeclaration) {
//        var pkgName = parseQualifiedName(packageDeclaration.qualifiedName());
//        symbolTable.setPackageName(pkgName);
//        this.packageName = pkgName.split("\\.");
////        var reflections = new Reflections(new ConfigurationBuilder().forPackages(pkgName));
////        List<String> classNames = NncUtils.filter(reflections.getAllTypes(), t -> t.startsWith(pkgName + "."));
//        var classNames = packageScanner.getClassNames(pkgName);
//        classNames.forEach(this::writeImport);
////        for (Class<?> klass : reflections.getSubTypesOf(Object.class)) {
////            writeImport(klass.getName());
////        }
////        for (Class<?> klass : reflections.getSubTypesOf(Enum.class)) {
////            writeImport(klass.getName());
////        }
//    }
//
//    private Set<Integer> getModifierTypes0(List<ClassOrInterfaceModifierContext> modifiers) {
//        List<TerminalNode> terminalNodes = NncUtils.mapAndFilterByType(
//                modifiers, m -> m.getChild(0), TerminalNode.class
//        );
//        return NncUtils.mapUnique(terminalNodes, m -> m.getSymbol().getType());
//    }
//
//    private void writeTypeType(TypeTypeContext typeType) {
//        writeTypeType(typeType, 0);
//    }
//
//    private Class<?> parseRawClass(TypeTypeContext typeType) {
//        return expressionTypeResolver.parseRawClass(typeType);
//    }
//
//    private Class<?> parseRawClass(TypeTypeOrVoidContext typeTypeOrVoid) {
//        if(typeTypeOrVoid.typeType() != null) {
//            return expressionTypeResolver.parseRawClass(typeTypeOrVoid.typeType());
//        }
//        else {
//            return void.class;
//        }
//    }
//
//    private void writeTypeType(TypeTypeContext typeType, int dimension) {
//        typeType.annotation().forEach(this::writeAnnotation);
//        if(typeType.classOrInterfaceType() != null) {
//            writeClassOrInterfaceType(typeType.classOrInterfaceType());
//        }
//        if(typeType.primitiveType() != null) {
//            writePrimitiveType(typeType.primitiveType());
//        }
//        for (int i = 0; i < dimension; i++) {
//            out.writeKeyword(COMPACT_OPEN_SQUARE_BRACKET);
//            out.writeKeyword(COMPACT_CLOSING_SQUARE_BRACKET);
//        }
//    }
//
//    private void writeTypeList(TypeListContext typeList) {
//        for (int i = 0; i < typeList.typeType().size(); i++) {
//            if(i > 0) {
//                out.writeComma();
//            }
//            writeTypeType(typeList.typeType(i));
//        }
//    }
//
//    private void writeBlock(BlockContext block) {
//        writeBlock(block, true);
//    }
//
//    private void writeBlock(BlockContext block, boolean pushPopBlocks) {
//        enterBlock(pushPopBlocks);
//        writeList(block.blockStatement(), this::writeBlockStatement, out::writeNewLine);
//        exitBlock(pushPopBlocks);
//    }
//
//    private void writeBlockStatement(BlockStatementContext blockStatement) {
//        if(blockStatement.localVariableDeclaration() != null) {
//            writeLocalVariableDeclaration(blockStatement.localVariableDeclaration());
//        }
//        if(blockStatement.localTypeDeclaration() != null) {
//            writeLocalTypeDeclaration(blockStatement.localTypeDeclaration());
//        }
//        if(blockStatement.statement() != null) {
//            writeStatement(blockStatement.statement());
//        }
//    }
//
//    private void writeLocalVariableDeclaration(LocalVariableDeclarationContext localVariableDeclaration) {
//        if(localVariableDeclaration.typeType() != null) {
//            List<AnnotationContext> annotations = getAnnotations(localVariableDeclaration.variableModifier());
//            if(isImmutable(localVariableDeclaration.variableModifier())) {
//                out.writeKeyword(TsLexicon.CONST);
//            }
//            else {
//                out.writeKeyword(TsLexicon.LET);
//            }
//            for (VariableDeclaratorContext variableDeclarator :
//                    localVariableDeclaration.variableDeclarators().variableDeclarator()) {
//                symbolTable.defineLocalVariable(
//                        variableDeclarator.variableDeclaratorId().identifier().getText(),
//                        parseRawClass(localVariableDeclaration.typeType())
//                );
//            }
//            writeVariableDeclarators(
//                    localVariableDeclaration.variableDeclarators(),
//                    localVariableDeclaration.typeType(),
//                    annotations
//            );
//        }
//        else {
//            writeVarDeclaration(
//                    localVariableDeclaration.variableModifier(),
//                    localVariableDeclaration.identifier()
//            );
//            out.writeKeyword(TsLexicon.EQUAL);
//            writeExpression(localVariableDeclaration.expression());
//            symbolTable.defineLocalVariable(localVariableDeclaration.identifier().getText(), null);
//        }
//    }
//
//    private void writeVariableDeclarators(VariableDeclaratorsContext variableDeclarators, TypeTypeContext typeType, List<AnnotationContext> annotations) {
//        writeList(
//                variableDeclarators.variableDeclarator(),
//                v -> writeVariableDeclarator(v, typeType, annotations),
//                TsLexicon.COMMA
//        );
//    }
//
//    private void writeIdentifierAndType(VariableDeclaratorIdContext variableId, TypeTypeContext typeType) {
//        writeIdentifierAndType(
//                () -> writeIdentifier(variableId.identifier()),
//                () -> writeTypeType(typeType),
//                getDimension(variableId)
//        );
//    }
//
//    private void writeIdentifierAndType(IdentifierContext identifier, boolean nullable, Runnable writeType, int dimension) {
//        writeIdentifierAndType(() -> writeIdentifier(identifier, nullable), writeType, dimension);
//    }
//
//    private void writeIdentifierAndType(IdentifierContext identifier, Runnable writeType, int dimension) {
//        writeIdentifierAndType(() -> writeIdentifier(identifier), writeType, dimension);
//    }
//
//    private void writeIdentifierAndType(Runnable writeIdentifier, Runnable writeType, int dimension) {
//        writeIdentifier.run();
//        if(writeType != null) {
//            out.writeKeyword(COMPACT_COLON);
//            writeType.run();
//            writeDimensions(dimension);
//        }
//    }
//
//    private void writeLocalTypeDeclaration(LocalTypeDeclarationContext localTypeDeclaration) {
//        writeClassOrInterfaceModifiers(localTypeDeclaration.classOrInterfaceModifier());
//        if(localTypeDeclaration.classDeclaration() != null) {
//            writeClassDeclaration(localTypeDeclaration.classDeclaration(), Set.of());
//        }
//        else if(localTypeDeclaration.interfaceDeclaration() != null) {
//            writeInterfaceDeclaration(localTypeDeclaration.interfaceDeclaration());
//        }
//        else {
//            writeRecordDeclaration(localTypeDeclaration.recordDeclaration());
//        }
//    }
//
//    private void writeClassOrInterfaceModifiers(List<ClassOrInterfaceModifierContext> modifiers) {
//        List<ParseTree> tokens = NncUtils.map(modifiers, m -> m.getChild(0));
//        for (ParseTree token : tokens) {
//            if(token instanceof TerminalNode terminalNode) {
//                int tokenIndex = terminalNode.getSymbol().getTokenIndex();
//                final var lexicon = switch (tokenIndex) {
//                    case JavaLexer.ABSTRACT -> TsLexicon.ABSTRACT;
//                    case JavaLexer.PUBLIC -> TsLexicon.PUBLIC;
//                    case JavaLexer.PROTECTED -> TsLexicon.PROTECTED;
//                    case JavaLexer.PRIVATE -> TsLexicon.PRIVATE;
//                    case JavaLexer.STATIC -> TsLexicon.STATIC;
//                    default -> throw new InternalException("Unsupported type modifier '" + tokenIndex + "'");
//                };
//                out.writeKeyword(lexicon);
//            }
//        }
//    }
//
//    private void writeStatement(StatementContext statement) {
//        var stream = new ChildrenStream(statement);
//        if(stream.isNextInstanceOf(BlockContext.class)) {
//            writeBlock(stream.next(BlockContext.class));
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.ASSERT)) {
//            writeAssert(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.IF)) {
//            writeIf(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.FOR)) {
//            writeFor(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.WHILE)) {
//            writeWhile(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.DO)) {
//            writeDo(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.TRY)) {
//            writeTry(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.SWITCH)) {
//            writeSwitch(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.SYNCHRONIZED)) {
//            writeSynchronized(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.RETURN)) {
//            writeReturn(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.THROW)) {
//            writeThrow(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.CONTINUE)) {
//            writeContinue(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.BREAK)) {
//            writeBreak(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.YIELD)) {
//            writeYield(stream);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.SEMI)) {
//            writeSemi();
//        }
//        else if(stream.isNextExpression()) {
//            writeExpression(stream.nextExpression());
//        }
//        else if(stream.isNextInstanceOf(SwitchExpressionContext.class)) {
//            writeSwitchExpression(stream.next(SwitchExpressionContext.class));
//        }
//        else if(stream.isNextIdentifier()) {
//            writeIdentifier(stream.nextIdentifier());
//            out.writeKeyword(COMPACT_COLON);
//            writeStatement(stream.nextStatement());
//        }
//    }
//
//    private void writeAssert(@SuppressWarnings("unused") ChildrenStream stream) {
//    }
//
//    private void writeSwitchExpression(SwitchExpressionContext switchExpression) {
//        out.writeKeyword(TsLexicon.SWITCH);
//        writeParExpression(switchExpression.parExpression());
//        out.writeKeyword(OPEN_CURLY_BRACE);
//        writeList(switchExpression.switchLabeledRule(), this::writeSwitchLabeledRule, out::writeNewLine);
//        out.writeKeyword(CLOSING_CURLY_BRACE);
//    }
//
//    private void writeSwitchLabeledRule(SwitchLabeledRuleContext switchLabeledRule) {
//        if(switchLabeledRule.CASE() != null) {
//            out.writeKeyword(TsLexicon.CASE);
//            if(switchLabeledRule.expressionList() != null) {
//                writeExpressionList(switchLabeledRule.expressionList());
//            }
//            else if(switchLabeledRule.NULL_LITERAL() != null) {
//                out.writeKeyword(UNDEFINED);
//            }
//            else {
//                writeGuardedPattern(switchLabeledRule.guardedPattern());
//            }
//        }
//        else {
//            out.writeKeyword(TsLexicon.DEFAULT);
//        }
//        if(switchLabeledRule.COLON() != null) {
//            out.writeKeyword(CASE_COLON);
//        }
//        else {
//            out.writeKeyword(TsLexicon.ARROW);
//        }
//        writeSwitchRuleOutcome(switchLabeledRule.switchRuleOutcome());
//    }
//
//    private void writeGuardedPattern(GuardedPatternContext guardedPattern) {
//        ChildrenStream stream = new ChildrenStream(guardedPattern);
//        if(stream.isNextLiteral("(")) {
//            out.writeKeyword(OPEN_PARENTHESIS);
//            writeGuardedPattern(stream.next(GuardedPatternContext.class));
//            out.writeKeyword(CLOSING_PARENTHESIS);
//        }
//        else if (stream.isNextInstanceOf(GuardedPatternContext.class)){
//            writeGuardedPattern(stream.next(GuardedPatternContext.class));
//            stream.nextTerminal();
//            out.writeKeyword(TsLexicon.AND);
//            writeExpression(stream.nextExpression());
//        }
//        else {
//            writeVariableDeclaration(
//                    guardedPattern.variableModifier(),
//                    () -> writeTypeType(guardedPattern.typeType()),
//                    guardedPattern.identifier(),
//                    0
//            );
//            out.writeKeyword(TsLexicon.AND);
//            writeList(guardedPattern.expression(), this::writeExpression, TsLexicon.AND);
//        }
//    }
//
//    private void writeSwitchRuleOutcome(SwitchRuleOutcomeContext switchRuleOutcome) {
//
//    }
//
//    private void writeYield(ChildrenStream stream) {
//        out.writeKeyword(TsLexicon.YIELD);
//        writeExpression(stream.findExpression());
//    }
//
//    private void writeThrow(ChildrenStream stream) {
//        stream.nextTerminal(JavaLexer.THROW);
//        out.writeKeyword(TsLexicon.THROW);
//        writeExpression(stream.nextExpression());
//    }
//
//    private void writeSemi() {
//        out.writeKeyword(TsLexicon.SEMI);
//    }
//
//    private void writeSynchronized(ChildrenStream stream) {
//        stream.nextTerminal(SYNCHRONIZED);
//        stream.nextParExpression();
//        writeBlock(stream.nextBlock());
//    }
//
//    private void writeContinue(ChildrenStream stream) {
//        stream.nextTerminal(JavaLexer.CONTINUE);
//        out.writeKeyword(TsLexicon.CONTINUE);
//        if(stream.isNextIdentifier()) {
//            writeIdentifier(stream.nextIdentifier());
//        }
//    }
//
//    private void writeBreak(ChildrenStream stream) {
//        stream.nextTerminal(JavaLexer.BREAK);
//        out.writeKeyword(TsLexicon.BREAK);
//        if(stream.isNextIdentifier()) {
//            writeIdentifier(stream.nextIdentifier());
//        }
//    }
//
//    private void writeReturn(ChildrenStream stream) {
//        stream.nextTerminal(JavaLexer.RETURN);
//        out.writeKeyword(TsLexicon.RETURN);
//        if(stream.isNextExpression()) {
//            writeExpression(stream.nextExpression());
//        }
//    }
//
//    private void writeTry(ChildrenStream stream) {
//        stream.nextTerminal(JavaLexer.TRY);
//        if(stream.isNextInstanceOf(ResourceSpecificationContext.class)) {
//            writeResourceSpecification(stream.next(ResourceSpecificationContext.class));
//        }
//        writeBlock(stream.nextBlock());
//        stream.nextList(CatchClauseContext.class).forEach(this::writeCatch);
//        if(stream.isNextInstanceOf(FinallyBlockContext.class)) {
//            writeFinally(stream.next(FinallyBlockContext.class));
//        }
//    }
//
//    private void writeCatch(CatchClauseContext catchClause) {
//        out.writeKeyword(TsLexicon.CATCH);
//        out.writeKeyword(OPEN_PARENTHESIS);
//        writeIdentifier(catchClause.identifier());
//        out.writeKeyword(COMPACT_COLON);
//        writeCatchType(catchClause.catchType());
//        out.writeKeyword(CLOSING_PARENTHESIS);
//    }
//
//    private void writeCatchType(CatchTypeContext catchType) {
//        writeList(catchType.qualifiedName(), this::writeQualifiedName, VERTICAL_BAR);
//    }
//
//    private void writeQualifiedName(QualifiedNameContext qualifiedName) {
//        writeList(qualifiedName.identifier(), this::writeIdentifier, TsLexicon.DOT);
//    }
//
//    private <T> void writeList(List<T> list, Consumer<T> operation, TsLexicon delimiter) {
//        writeList(list, operation, () -> out.writeKeyword(delimiter));
//    }
//
//    private  <T extends ParseTree> void writeIfPresent(T parseTree, Consumer<T> operation) {
//        if(parseTree != null) {
//            operation.accept(parseTree);
//        }
//    }
//
//    private <T> void writeList(List<T> list, Consumer<T> operation, Runnable delimitingOperation) {
//        for (int i = 0; i < list.size(); i++) {
//            if(i > 0) {
//                delimitingOperation.run();
//            }
//            operation.accept(list.get(i));
//        }
//    }
//
//    private void writeFinally(FinallyBlockContext finallyBlock) {
//        out.writeKeyword(TsLexicon.FINALLY);
//        writeBlock(finallyBlock.block());
//    }
//
//    private void writeResourceSpecification(ResourceSpecificationContext resourceSpecification) {
//        out.writeKeyword(OPEN_PARENTHESIS);
//        writeResources(resourceSpecification.resources());
//        out.writeKeyword(CLOSING_PARENTHESIS);
//    }
//
//    private void writeResources(ResourcesContext resources) {
//        writeList(resources.resource(), this::writeResource, TsLexicon.SEMI);
//    }
//
//    private void writeResource(ResourceContext resource) {
//        if(resource.expression() != null) {
//            if (resource.classOrInterfaceType() != null) {
//                writeVariableDeclaration(
//                        resource.variableModifier(),
//                        resource.classOrInterfaceType(),
//                        resource.variableDeclaratorId()
//                );
//            } else {
//                writeVarDeclaration(resource.variableModifier(), resource.identifier());
//            }
//            out.writeKeyword(TsLexicon.EQUAL);
//            writeExpression(resource.expression());
//        }
//        else {
//            writeIdentifier(resource.identifier());
//        }
//    }
//
//    private void writeVariableDeclaration(List<VariableModifierContext> modifiers,
//                                          ClassOrInterfaceTypeContext type,
//                                          VariableDeclaratorIdContext variableId) {
//        writeVariableDeclaration(
//                modifiers,
//                () -> writeClassOrInterfaceType(type),
//                variableId
//        );
//    }
//
//    private void writeVariableDeclaration(List<VariableModifierContext> modifiers,
//                                          TypeTypeContext type,
//                                          VariableDeclaratorIdContext variableId) {
//        writeVariableDeclaration(
//                modifiers,
//                () -> writeTypeType(type),
//                variableId
//        );
//    }
//
//    private void writeVariableDeclaration(List<VariableModifierContext> modifiers,
//                                          Runnable writeTypeOperation,
//                                          VariableDeclaratorIdContext variableId) {
//        writeVariableDeclaration(
//                modifiers,
//                writeTypeOperation,
//                variableId.identifier(),
//                getDimension(variableId)
//        );
//    }
//
//    private void writeVariableDeclaration(List<VariableModifierContext> modifiers,
//                                          Runnable writeTypeOperation,
//                                          IdentifierContext identifier,
//                                          int dimensions
//    ) {
//        if(isImmutable(modifiers)) {
//            out.writeKeyword(TsLexicon.CONST);
//        }
//        else {
//            out.writeKeyword(LET);
//        }
//        writeIdentifier(identifier);
//        out.writeKeyword(COMPACT_COLON);
//        writeTypeOperation.run();
//        writeDimensions(dimensions);
//    }
//
//    private void writeVarDeclaration(List<VariableModifierContext> modifiers, IdentifierContext identifier) {
//        if (isImmutable(modifiers)) {
//            out.writeKeyword(TsLexicon.CONST);
//        }
//        else {
//            out.writeKeyword(LET);
//        }
//        writeIdentifier(identifier);
//    }
//
//    private int getDimension(VariableDeclaratorIdContext variableId) {
//        return (variableId.getChildCount() - 1) / 2;
//    }
//
//    private void writeDimensions(int dimensions) {
//        for (int i = 0; i < dimensions; i++) {
//            out.writeKeyword(COMPACT_OPEN_SQUARE_BRACKET);
//            out.writeKeyword(COMPACT_CLOSING_SQUARE_BRACKET);
//        }
//    }
//
//    private boolean isImmutable(List<VariableModifierContext> variableModifiers) {
//        return variableModifiers.stream().anyMatch(m -> m.FINAL() != null);
//    }
//
//    private void writeSwitch(ChildrenStream stream) {
//        stream.nextTerminal(JavaLexer.SWITCH);
//        out.writeKeyword(TsLexicon.SWITCH);
//        writeParExpression(stream.next(ParExpressionContext.class));
//        stream.next();
//        stream.nextList(SwitchBlockStatementGroupContext.class).forEach(this::writeSwitchBlockStatementGroup);
//        stream.nextList(SwitchLabelContext.class).forEach(this::writeSwitchLabel);
//    }
//
//    private void writeSwitchBlockStatementGroup(SwitchBlockStatementGroupContext switchBlockStatementGroup) {
//        switchBlockStatementGroup.switchLabel().forEach(this::writeSwitchLabel);
//        switchBlockStatementGroup.blockStatement().forEach(this::writeBlockStatement);
//    }
//
//    private void writeSwitchLabel(SwitchLabelContext switchLabel) {
//        ChildrenStream stream = new ChildrenStream(switchLabel);
//        if(stream.isNextTerminalOf(JavaLexer.CASE)) {
//            out.writeKeyword(TsLexicon.CASE);
//            if(stream.isNextInstanceOf(ExpressionContext.class)) {
//                writeExpression(stream.nextExpression());
//            }
//            else if(stream.isNextTerminalOf(JavaLexer.IDENTIFIER)) {
//                out.writeIdentifier(stream.nextTerminal().getText());
//            }
//            else {
//                throw new InternalException("Unsupported java 17 switch syntax");
//            }
//        }
//        else {
//            out.writeKeyword(TsLexicon.DEFAULT);
//        }
//        out.writeKeyword(CASE_COLON);
//        out.writeNewLine();
//    }
//
//    private void writeDo(ChildrenStream stream) {
//        out.writeKeyword(TsLexicon.DO);
//        writeStatement(stream.nextStatement());
//        out.writeKeyword(TsLexicon.WHILE);
//        writeParExpression(stream.nextParExpression());
//        out.writeNewLine();
//    }
//
//    private void writeWhile(ChildrenStream stream) {
//        stream.nextTerminal(JavaLexer.WHILE);
//        writeParExpression(stream.nextParExpression());
//        writeStatement(stream.nextStatement());
//    }
//
//    private void writeIf(ChildrenStream stream) {
//        symbolTable.enterBlock();
//        stream.nextTerminal(JavaLexer.IF);
//        out.writeKeyword(TsLexicon.IF);
//        writeParExpression(stream.next(ParExpressionContext.class));
//        writeStatement(stream.nextStatement());
//        if(stream.isNextTerminalOf(JavaLexer.ELSE)) {
//            stream.nextTerminal(JavaLexer.ELSE);
//            out.writeKeyword(TsLexicon.ELSE);
//            writeStatement(stream.nextStatement());
//        }
//        symbolTable.exitBlock();
//    }
//
//    private void writeFor(ChildrenStream stream) {
//        symbolTable.enterBlock();
//        out.writeKeyword(TsLexicon.FOR);
//        out.writeKeyword(OPEN_PARENTHESIS);
//        writeForControl(stream.find(ForControlContext.class));
//        out.writeKeyword(CLOSING_PARENTHESIS);
//        writeStatement(stream.nextStatement());
//        symbolTable.exitBlock();
//    }
//
//    private void writeForControl(ForControlContext forControl) {
//        if(forControl.enhancedForControl() != null) {
//            writeEnhancedForControl(forControl.enhancedForControl());
//        }
//        else {
//            if(forControl.forInit() != null) {
//                writeForInit(forControl.forInit());
//            }
//            out.writeKeyword(TsLexicon.COMMA);
//            writeExpression(forControl.expression());
//            out.writeKeyword(TsLexicon.COMMA);
//            if(forControl.expressionList() != null) {
//                writeExpressionList(forControl.expressionList());
//            }
//        }
//    }
//
//    private void writeForInit(ForInitContext forInit) {
//        if(forInit.localVariableDeclaration() != null) {
//            writeLocalVariableDeclaration(forInit.localVariableDeclaration());
//        }
//        else if(forInit.expressionList() != null) {
//            writeExpressionList(forInit.expressionList());
//        }
//    }
//
//    private void writeEnhancedForControl(EnhancedForControlContext enhancedForControl) {
//        boolean immutable = enhancedForControl.variableModifier().stream().anyMatch(
//                m -> isTerminalOf(m.getChild(0), JavaLexer.FINAL)
//        );
//        if(immutable) {
//            out.writeKeyword(TsLexicon.CONST);
//        }
//        else {
//            out.writeKeyword(LET);
//        }
//        var variableIdentifier = enhancedForControl.variableDeclaratorId().identifier();
//        writeIdentifier(variableIdentifier);
//        var variableType = enhancedForControl.typeType() != null ?
//                parseRawClass(enhancedForControl.typeType()) : null;
//        symbolTable.defineLocalVariable(variableIdentifier.getText(), variableType);
//        if(enhancedForControl.typeType() != null) {
//            out.writeKeyword(COMPACT_COLON);
//            var dimension = (enhancedForControl.variableDeclaratorId().getChildCount() - 1) / 2;
//            writeTypeType(enhancedForControl.typeType(), dimension);
//        }
//        out.writeKeyword(IN);
//        writeExpression(enhancedForControl.expression());
//    }
//
//    private boolean isTerminalOf(ParseTree parseTree, int tokenIndex) {
//        if(parseTree instanceof TerminalNode terminalNode) {
//            return terminalNode.getSymbol().getTokenIndex() == tokenIndex;
//        }
//        return false;
//    }
//
//    private void writeParExpression(ParExpressionContext parExpression) {
//        out.writeKeyword(OPEN_PARENTHESIS);
//        writeExpression(parExpression.expression());
//        out.writeKeyword(CLOSING_PARENTHESIS);
//    }
//
//    private void writeExpressionList(ExpressionListContext expressionList) {
//        writeList(expressionList.expression(), this::writeExpression, TsLexicon.COMMA);
//    }
//
//    private void writeVariableDeclarator(VariableDeclaratorContext variableDeclarator, TypeTypeContext type, List<AnnotationContext> annotations) {
//        var variableId = variableDeclarator.variableDeclaratorId();
//        writeIdentifierAndType(
//                variableId.identifier(),
//                isNullable(annotations),
//                () -> writeTypeType(type),
//                getDimension(variableId)
//        );
//        if(variableDeclarator.variableInitializer() != null) {
//            out.writeKeyword(TsLexicon.EQUAL);
//            writeVariableInitializer(variableDeclarator.variableInitializer());
//        }
//    }
//
//    private boolean isNullable(List<AnnotationContext> annotations) {
//        for (AnnotationContext annotation : annotations) {
//            if(annotation.qualifiedName() != null) {
//                String qualifiedName = parseQualifiedName(annotation.qualifiedName());
//                if(qualifiedName.equals("Nullable") || qualifiedName.equals("javax.annotation.Nullable")
//                        || qualifiedName.equals("org.jetbrains.annotations.Nullable")) {
//                    return true;
//                }
//            }
//        }
//        return false;
//    }
//
//
//    private void writeVariableInitializer(VariableInitializerContext variableInitializer) {
//        if(variableInitializer.arrayInitializer() != null) {
//            writeArrayInitializer(variableInitializer.arrayInitializer());
//        }
//        else {
//            writeExpression(variableInitializer.expression());
//        }
//    }
//
//    private void writeArrayInitializer(ArrayInitializerContext arrayInitializer) {
//        out.writeKeyword(OPEN_SQUARE_BRACKET);
//        for (int i = 0; i < arrayInitializer.variableInitializer().size(); i++) {
//            if(i > 0) {
//                out.writeComma();
//            }
//            writeVariableInitializer(arrayInitializer.variableInitializer(i));
//        }
//        out.writeKeyword(CLOSING_SQUARE_BRACKET);
//    }
//
//    private void writeExpression(ExpressionContext expression) {
//        if(expression.primary() != null) {
//            writePrimary(expression.primary());
//        }
//        else if(expression.getChildCount() == 1 && expression.methodCall() != null) {
//            var methodVariable = resolveMethodFromCall(expression.methodCall());
//            if(methodVariable.prefix() != null) {
//                out.writeIdentifier(methodVariable.prefix());
//                out.writeKeyword(TsLexicon.DOT);
//            }
//            writeMethodCall(expression.methodCall(), null);
//        }
//        else if(expression.switchExpression() != null) {
//            writeSwitchExpression(expression.switchExpression());
//        }
//        else if(expression.lambdaExpression() != null) {
//            writeLambdaExpression(expression.lambdaExpression());
//        }
//        else if(expression.prefix != null) {
//            out.writeOperatorBefore(expression.prefix.getText());
//            writeExpression(expression.expression(0));
//        }
//        else if(expression.postfix != null) {
//            writeExpression(expression.expression(0));
//            out.writeOperatorAfter(expression.prefix.getText());
//        }
//        else if(expression.getChildCount() >= 2 && expression.getChild(1).getText().equals("::")) {
//            writeMethodReference(new ChildrenStream(expression));
//        }
//        else {
//            ChildrenStream stream = new ChildrenStream(expression);
//            if(stream.isNextExpression()) {
//                var expr = stream.nextExpression();
//                if(stream.isNextLiteral("[")) {
//                    writeArrayElementExpression(expr, stream);
//                }
//                else if(expression.bop != null) {
//                    final var bop = expression.bop;
//                    if(bop.getType() == JavaLexer.INSTANCEOF) {
//                        writeInstanceOfExpression(expr, stream);
//                    }
//                    else if(bop.getText().equals(".")) {
//                        writeDotExpression(expr, stream);
//                    }
//                    else if(bop.getText().equals("?")) {
//                        writeConditionalExpression(expr, stream);
//                    }
//                    else {
//                        writeBinary(expr, stream);
//                    }
//                }
//                else {
//                    writeShiftExpression(expr, stream);
//                }
//            }
//            else if(stream.isNextTerminalOf(JavaLexer.NEW)) {
//                writeNewExpression(stream);
//            }
//            else if(stream.isNextLiteral("(")) {
//                writeCastExpression(stream);
//            }
//        }
//    }
//
//    private Symbol resolveMethodFromCall(MethodCallContext methodCall) {
//        return expressionTypeResolver.resolveMethodFromCall(methodCall);
//    }
//
//    private void writeMethodReference(ChildrenStream stream) {
//        if(stream.isNextExpression()) {
//            var expr = stream.nextExpression();
//            writeExpression(expr);
//            out.writeKeyword(TsLexicon.DOT);
//            stream.next();
//            stream.skipIfNextInstanceOf(TypeArgumentsContext.class);
//            writeIdentifier(stream.nextIdentifier());
//            out.writeKeyword(TsLexicon.DOT);
//            out.writeIdentifier("bind");
//            writeExpression(expr);
//        }
//        else if(stream.isNextTypeType()) {
//            writeTypeType(stream.nextTypeType());
//            stream.next();
//            stream.skipIfNextInstanceOf(TypeArgumentsContext.class);
//            if (stream.isNextIdentifier()) {
//                out.writeKeyword(TsLexicon.DOT);
//                writeIdentifier(stream.nextIdentifier());
//            }
//        }
//        else {
//            writeClassTypeAsIdentifier(stream.next(ClassTypeContext.class));
//        }
//    }
//
//    private void writeClassTypeAsIdentifier(ClassTypeContext classType) {
//        if(classType.classOrInterfaceType() != null) {
//            writeClassOrInterfaceTypeAsIdentifier(classType.classOrInterfaceType());
//            out.writeKeyword(TsLexicon.DOT);
//        }
//        writeIdentifier(classType.identifier());
//    }
//
//    private void writeClassOrInterfaceTypeAsIdentifier(ClassOrInterfaceTypeContext classOrInterfaceType) {
//        if(classOrInterfaceType.identifier().size() > 0) {
//            writeList(classOrInterfaceType.identifier(), this::writeIdentifier, TsLexicon.DOT);
//            out.writeKeyword(TsLexicon.DOT);
//        }
//        out.writeIdentifier(classOrInterfaceType.typeIdentifier().getText());
//    }
//
//    private void writeInstanceOfExpression(ExpressionContext expression, ChildrenStream stream) {
//        writeExpression(expression);
//        stream.next();
//        out.writeKeyword(TsLexicon.INSTANCEOF);
//        if(stream.isNextTypeType()) {
//            writeTypeType(stream.nextTypeType());
//        }
//        else {
//            writePattern(stream.next(PatternContext.class), expression);
//        }
//    }
//
//    private void writeConditionalExpression(ExpressionContext expression, ChildrenStream stream) {
//        writeExpression(expression);
//        out.writeKeyword(TsLexicon.QUESTION);
//        writeExpression(stream.findExpression());
//        out.writeKeyword(TsLexicon.COLON);
//        writeExpression(stream.findExpression());
//    }
//
//    private void writeDotExpression(ExpressionContext expression, ChildrenStream stream) {
//        writeExpression(expression);
//        stream.next();
//        out.writeKeyword(TsLexicon.DOT);
//        if(stream.isNextIdentifier()) {
//            writeIdentifier(stream.nextIdentifier());
//        }
//        else if(stream.isNextInstanceOf(MethodCallContext.class)) {
//            writeMethodCall(stream.next(MethodCallContext.class), expression);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.NEW)) {
//            out.writeKeyword(TsLexicon.NEW);
//            var typeArgs = stream.nextIf(NonWildcardTypeArgumentsContext.class);
//            writeInnerCreator(stream.next(InnerCreatorContext.class), () -> {
//                if(typeArgs != null) {
//                    writeNonWildcardTypeArguments(typeArgs);
//                }
//            });
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.THIS)) {
//            out.writeKeyword(TsLexicon.THIS);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.SUPER)) {
//            out.writeKeyword(TsLexicon.SUPER);
//            writeSuperSuffix(stream.next(SuperSuffixContext.class));
//        }
//        else {
//            writeExplicitGenericInvocation(stream.next(ExplicitGenericInvocationContext.class));
//        }
//    }
//
//    private void writeExplicitGenericInvocation(ExplicitGenericInvocationContext explicitGenericInvocation) {
//        writeExplicitGenericInvocationSuffix(
//                explicitGenericInvocation.explicitGenericInvocationSuffix(),
//                explicitGenericInvocation.nonWildcardTypeArguments()
//        );
//    }
//
//    private void writeInnerCreator(InnerCreatorContext innerCreator, Runnable writePrefixTypeArgs) {
//        writeClassCreatorRest(
//                innerCreator.classCreatorRest(),
//                () -> {
//                    writeIdentifier(innerCreator.identifier());
//                    writePrefixTypeArgs.run();
//                    if(innerCreator.nonWildcardTypeArgumentsOrDiamond() != null) {
//                        writeNonWildcardTypeArgumentsOrDiamond(innerCreator.nonWildcardTypeArgumentsOrDiamond());
//                    }
//                },
//                innerCreator.identifier().getText()
//        );
//    }
//
//    private void writeNonWildcardTypeArgumentsOrDiamond(
//            NonWildcardTypeArgumentsOrDiamondContext nonWildcardTypeArgumentsOrDiamond
//    ) {
//        if(nonWildcardTypeArgumentsOrDiamond.nonWildcardTypeArguments() != null) {
//            writeNonWildcardTypeArguments(nonWildcardTypeArgumentsOrDiamond.nonWildcardTypeArguments());
//        }
//        else {
//            writeDiamond();
//        }
//    }
//
//    private void writeBinary(ExpressionContext expression, ChildrenStream stream) {
//        writeExpression(expression);
//        writeText(stream.nextTerminal().getText());
//        writeExpression(stream.nextExpression());
//    }
//
//    private void writePattern(PatternContext pattern, ExpressionContext expression) {
//        writeTypeType(pattern.typeType());
//        var symbol = tryConvertToSymbol(expression);
//        if(symbol == null) {
//            writeIdentifier(pattern.identifier());
//            symbolTable.defineLocalVariable(pattern.identifier().getText(), parseRawClass(pattern.typeType()));
//        }
//        else {
//            symbolTable.currentBlock().defineManually(
//                    SymbolIdentifier.variable(pattern.identifier().getText()),
//                    symbol
//            );
//        }
//    }
//
//    private Symbol tryConvertToSymbol(ExpressionContext expression) {
//        if(expression.primary() != null && expression.primary().identifier() != null) {
//            return symbolTable.resolveVariableOrType(
//                    expression.primary().identifier().getText()
//            );
//        }
//        return null;
//    }
//
//    private void writeArrayElementExpression(ExpressionContext expression, ChildrenStream stream) {
//        writeExpression(expression);
//        out.writeKeyword(OPEN_SQUARE_BRACKET);
//        writeExpression(stream.findExpression());
//        out.writeKeyword(CLOSING_SQUARE_BRACKET);
//    }
//
//    private void writeCastExpression(ChildrenStream stream) {
//        stream.next();
//        stream.nextList(AnnotationContext.class);
//        List<TypeTypeContext> types = new ArrayList<>();
//        while(stream.isNextTypeType()) {
//            types.add(stream.nextTypeType());
//            if(stream.isNextLiteral("&")) {
//                stream.next();
//            }
//        }
//        stream.next();
//        writeExpression(stream.nextExpression());
//        out.writeKeyword(TsLexicon.AS);
//        writeList(types, this::writeTypeType, BIT_AND);
//    }
//
//    private void writeNewExpression(ChildrenStream stream) {
//        stream.nextTerminal(JavaLexer.NEW);
//        out.writeKeyword(TsLexicon.NEW);
//        writeCreator(stream.next(CreatorContext.class));
//    }
//
//    private void writeCreator(CreatorContext creator) {
//        var createdName = creator.createdName();
//        String typeName = createdName.identifier() != null ?
//                createdName.identifier(0).getText() : createdName.primitiveType().getText();
//        if(creator.nonWildcardTypeArguments() != null) {
//            writeClassCreatorRest(
//                    creator.classCreatorRest(),
//                    () -> {
//                        writeCreatedName(creator.createdName());
//                        writeNonWildcardTypeArguments(creator.nonWildcardTypeArguments());
//                    },
//                    typeName
//            );
//        }
//        else if(creator.arrayCreatorRest() != null) {
//            writeArrayCreatorRest(creator.arrayCreatorRest());
//        }
//        else {
//            writeClassCreatorRest(
//                    creator.classCreatorRest(),
//                    () -> writeCreatedName(creator.createdName()),
//                    typeName
//            );
//        }
//    }
//
//    private void writeArrayCreatorRest(ArrayCreatorRestContext arrayCreatorRest) {
//        if(arrayCreatorRest.arrayInitializer() != null) {
//            writeArrayInitializer(arrayCreatorRest.arrayInitializer());
//        }
//        else {
//            out.writeKeyword(OPEN_SQUARE_BRACKET);
//            out.writeKeyword(CLOSING_SQUARE_BRACKET);
//        }
//    }
//
//    private void writeClassCreatorRest(ClassCreatorRestContext classCreatorRest, Runnable writeClassNameOperation, String typeName) {
//        if(classCreatorRest.classBody() == null) {
//            writeClassNameOperation.run();
//            writeArguments(classCreatorRest.arguments());
//        }
//        else {
//            out.writeKeyword(TsLexicon.CLASS);
//            out.writeKeyword(TsLexicon.EXTENDS);
//            writeClassNameOperation.run();
//            writeClassBody(classCreatorRest.classBody(), null, typeName, false);
//            writeArguments(classCreatorRest.arguments());
//        }
//    }
//
//    private void writeCreatedName(CreatedNameContext createdName) {
//        if(createdName.primitiveType() != null) {
//            writePrimitiveType(createdName.primitiveType());
//        }
//        else {
//            ChildrenStream stream = new ChildrenStream(createdName);
//            while(stream.hasNext()) {
//                writeIdentifier(stream.nextIdentifier());
//                if(stream.isNextInstanceOf(TypeArgumentsOrDiamondContext.class)) {
//                    writeTypeArgumentsOrDiamond(stream.next(TypeArgumentsOrDiamondContext.class));
//                }
//                if(stream.isNextLiteral(".")) {
//                    out.writeKeyword(TsLexicon.DOT);
//                }
//            }
//        }
//    }
//
//    private void writeTypeArgumentsOrDiamond(TypeArgumentsOrDiamondContext typeArgumentsOrDiamond) {
//        if(typeArgumentsOrDiamond.typeArguments() != null) {
//            writeTypeArguments(typeArgumentsOrDiamond.typeArguments());
//        }
//        else {
//            writeDiamond();
//        }
//    }
//
//    private void writeDiamond() {
////        out.writeKeyword(OPEN_ANGULAR_BRACKET);
////        out.writeKeyword(CLOSING_ANGULAR_BRACKET);
//    }
//
//    private void writeShiftExpression(ExpressionContext expression, ChildrenStream stream) {
//        writeExpression(expression);
//        if(stream.isNextLiteral("<")) {
//            stream.next();
//            stream.next();
//            out.writeKeyword(TsLexicon.LEFT_SHIFT);
//            writeExpression(stream.nextExpression());
//        }
//        else {
//            stream.next();
//            stream.next();
//            if(stream.isNextLiteral(">")) {
//                out.writeKeyword(RIGHT_SHIFT_UNSIGNED);
//            }
//            else {
//                out.writeKeyword(RIGHT_SHIFT);
//            }
//            writeExpression(stream.nextExpression());
//        }
//    }
//
//    private void writeMethodCall(MethodCallContext methodCall, @Nullable ExpressionContext instance) {
//        if(isRecordComponentCall(methodCall, instance)) {
//            writeIdentifier(methodCall.identifier());
//            return;
//        }
//        if(methodCall.identifier() != null) {
//            writeIdentifier(methodCall.identifier());
//        }
//        else if(methodCall.THIS() != null) {
//            out.writeKeyword(TsLexicon.THIS);
//        }
//        else {
//            out.writeKeyword(TsLexicon.SUPER);
//        }
//        out.writeKeyword(COMPACT_OPEN_PARENTHESIS);
//        if(methodCall.expressionList() != null) {
//            writeExpressionList(methodCall.expressionList());
//        }
//        out.writeKeyword(CLOSING_PARENTHESIS);
//    }
//
//    private boolean isRecordComponentCall(MethodCallContext methodCall, ExpressionContext instance) {
//        if(instance == null || methodCall.identifier() == null || methodCall.expressionList() != null) {
//            return false;
//        }
//        Class<?> expressionType = evaluateExpressionType(instance);
//        if(expressionType != null && Record.class.isAssignableFrom(expressionType)) {
//            return NncUtils.anyMatch(
//                    Arrays.asList(expressionType.getRecordComponents()),
//                    c -> c.getName().equals(methodCall.identifier().getText())
//            );
//        }
//        return false;
//    }
//
//    private void writeLambdaExpression(LambdaExpressionContext lambdaExpression) {
//        symbolTable.enterBlock();
//        writeLambdaParameters(lambdaExpression.lambdaParameters());
//        out.writeKeyword(TS_ARRAY);
//        writeLambdaBody(lambdaExpression.lambdaBody());
//        symbolTable.exitBlock();
//    }
//
//    private void writeLambdaParameters(LambdaParametersContext lambdaParameters) {
//        if(lambdaParameters.getChild(0) instanceof IdentifierContext identifier) {
//            writeIdentifier(identifier);
//            symbolTable.defineLocalVariable(identifier.getText(), null);
//        }
//        else {
//            out.writeKeyword(OPEN_PARENTHESIS);
//            if(lambdaParameters.formalParameterList() != null) {
//                writeFormalParameterList(lambdaParameters.formalParameterList());
//            }
//            else if(lambdaParameters.identifier().size() > 0) {
//                writeList(lambdaParameters.identifier(), this::writeIdentifier, TsLexicon.COMMA);
//                for (IdentifierContext identifier : lambdaParameters.identifier()) {
//                    symbolTable.defineLocalVariable(identifier.getText(), null);
//                }
//            }
//            else if(lambdaParameters.lambdaLVTIList() != null) {
//                writeLambdaLVTIList(lambdaParameters.lambdaLVTIList());
//            }
//            out.writeKeyword(CLOSING_PARENTHESIS);
//        }
//    }
//
//    private void writeLambdaLVTIList(LambdaLVTIListContext lambdaLVTIList) {
//        writeList(lambdaLVTIList.lambdaLVTIParameter(), this::writeLambdaLVTIParameter, TsLexicon.COMMA);
//    }
//
//    private void writeLambdaLVTIParameter(LambdaLVTIParameterContext lambdaLVTIParameter) {
//        writeIdentifier(lambdaLVTIParameter.identifier());
//        symbolTable.defineLocalVariable(lambdaLVTIParameter.identifier().getText(), null);
//    }
//
//    private void writeLambdaBody(LambdaBodyContext lambdaBody) {
//        if(lambdaBody.expression() != null) {
//            writeExpression(lambdaBody.expression());
//        }
//        else {
//            writeBlock(lambdaBody.block());
//        }
//    }
//
//    private void writePrimary(PrimaryContext primary) {
//        ChildrenStream stream = new ChildrenStream(primary);
//        if(stream.isNextLiteral("(")) {
//            stream.next();
//            out.writeKeyword(OPEN_PARENTHESIS);
//            writeExpression(stream.nextExpression());
//            out.writeKeyword(CLOSING_PARENTHESIS);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.THIS)) {
//            out.writeKeyword(TsLexicon.THIS);
//        }
//        else if(stream.isNextTerminalOf(JavaLexer.SUPER)) {
//            out.writeKeyword(TsLexicon.SUPER);
//        }
//        else if(stream.isNextInstanceOf(LiteralContext.class)) {
//            writeLiteral(stream.next(LiteralContext.class));
//        }
//        else if(stream.isNextIdentifier()) {
//            writeExpressionIdentifier(stream.nextIdentifier());
//        }
//        else if(stream.isNextInstanceOf(TypeTypeOrVoidContext.class)) {
//            writeClassLiteral(stream.next(TypeTypeOrVoidContext.class));
//        }
//        else if(stream.isNextInstanceOf(NonWildcardTypeArgumentsContext.class)) {
//            writeGenericMethodCall(stream);
//        }
//    }
//
//    private void writeGenericMethodCall(ChildrenStream stream) {
//        var typeArguments = stream.next(NonWildcardTypeArgumentsContext.class);
//        if(stream.isNextInstanceOf(ExplicitGenericInvocationSuffixContext.class)) {
//            writeExplicitGenericInvocationSuffix(
//                    stream.next(ExplicitGenericInvocationSuffixContext.class), typeArguments
//            );
//        }
//        else {
//            stream.nextTerminal(JavaLexer.THIS);
//            out.writeKeyword(TsLexicon.THIS);
//            writeNonWildcardTypeArguments(typeArguments);
//            writeArguments(stream.next(ArgumentsContext.class));
//        }
//    }
//
//    private void writeArguments(ArgumentsContext arguments) {
//        out.writeKeyword(COMPACT_OPEN_PARENTHESIS);
//        if(arguments.expressionList() != null) {
//            writeExpressionList(arguments.expressionList());
//        }
//        out.writeKeyword(CLOSING_PARENTHESIS);
//    }
//
//    private void writeNonWildcardTypeArguments(NonWildcardTypeArgumentsContext nonWildcardTypeArguments) {
//
//    }
//
//    private void writeExplicitGenericInvocationSuffix(ExplicitGenericInvocationSuffixContext explicitGenericInvocation,
//                                                      NonWildcardTypeArgumentsContext nonWildcardTypeArgs) {
//        if(explicitGenericInvocation.SUPER() != null) {
//            out.writeKeyword(TsLexicon.SUPER);
//            writeNonWildcardTypeArguments(nonWildcardTypeArgs);
//            writeSuperSuffix(explicitGenericInvocation.superSuffix());
//        }
//        else {
//            writeIdentifier(explicitGenericInvocation.identifier());
//            writeNonWildcardTypeArguments(nonWildcardTypeArgs);
//            writeArguments(explicitGenericInvocation.arguments());
//        }
//    }
//
//    private void writeSuperSuffix(SuperSuffixContext superSuffix) {
//        if(superSuffix.arguments() != null) {
//            writeArguments(superSuffix.arguments());
//        }
//        else {
//            out.writeKeyword(TsLexicon.DOT);
//            writeIdentifier(superSuffix.identifier());
//            if(superSuffix.typeArguments() != null) {
//                writeTypeArguments(superSuffix.typeArguments());
//            }
//            if(superSuffix.arguments() != null) {
//                writeArguments(superSuffix.arguments());
//            }
//        }
//    }
//
//    private void writeClassLiteral(TypeTypeOrVoidContext typeTypeOrVoid) {
//        out.writeIdentifier("ReflectUtils.getClass");
//        out.writeKeyword(COMPACT_OPEN_PARENTHESIS);
//        writeTypeTypeOrVoid(typeTypeOrVoid, 0);
//        out.writeKeyword(CLOSING_PARENTHESIS);
//    }
//
//    private void writeLiteral(LiteralContext literal) {
//        writeText(literal.getText());
//    }
//
//    private void writeExpressionIdentifier(IdentifierContext identifier) {
//        var text = identifier.getText();
//        var splits = text.split("\\.");
//        var firstSplit = splits[0];
//        splits[0] = symbolTable.resolveVariableOrType(firstSplit).build();
//        writeText(NncUtils.join(Arrays.asList(splits), "."));
//    }
//
//    private void writeIdentifier(IdentifierContext identifier) {
//        writeIdentifier(identifier, false);
//    }
//
//    private void writeIdentifier(IdentifierContext identifier, boolean nullable) {
//        if(nullable) {
//            writeText(identifier.getText() + "?");
//        }
//        else {
//            writeText(identifier.getText());
//        }
//    }
//
//    private Class<?> evaluateExpressionType(ExpressionContext expression) {
//        if(expression.primary() != null && expression.primary().identifier() != null) {
//            String variableName = expression.primary().identifier().getText();
//            SourceType variableType = symbolTable.resolveVariableOrType(variableName).type();
//            if(variableType != null) {
//                return variableType.getReflectClass();
//            }
//        }
//        return null;
//    }
//
//    private void writeText(String text) {
//        String mappedText = IDENTIFIER_MAP.get(text);
//        out.writeIdentifier(mappedText != null ? mappedText : text);
//    }
//
//    public void writeClassDeclaration(ClassDeclarationContext classDeclaration, Set<Integer> modifiers) {
//        boolean isStatic = modifiers.contains(JavaLexer.STATIC);
//        ChildrenStream stream = new ChildrenStream(classDeclaration);
//        stream.next(TerminalNode.class);
//        writeKeyword(TsLexicon.CLASS);
//        var nameIdentifier = stream.next(IdentifierContext.class);
//        symbolTable.defineType(nameIdentifier.getText(), isStatic);
//        writeIdentifier(nameIdentifier);
//        if(stream.isNextInstanceOf(TypeParametersContext.class)) {
//            writeTypeParameters(stream.next(TypeParametersContext.class));
//        }
//        String superName;
//        if(stream.skipIfNextSymbolOf(JavaLexer.EXTENDS)) {
//            writeKeyword(TsLexicon.EXTENDS);
//            var superType = stream.next(TypeTypeContext.class);
//            var superClass = parseRawClass(superType);
//            superName = superClass.getName();
//            writeTypeType(superType);
//        }
//        else {
//            superName = null;
//        }
//        if(stream.skipIfNextSymbolOf(JavaLexer.IMPLEMENTS)) {
//            writeTypeList(stream.next(TypeListContext.class));
//        }
//        if(stream.skipIfNextSymbolOf(JavaLexer.PERMITS)) {
//            stream.next();
//        }
//        if(stream.isNextInstanceOf(ClassBodyContext.class)) {
//            writeClassBody(
//                    stream.next(ClassBodyContext.class),
//                    nameIdentifier.getText(),
//                    superName,
//                    isStatic
//            );
//        }
//    }
//
//    private void writeClassBody(ClassBodyContext classBody, String className, String superName, boolean isStatic) {
//        symbolTable.enterClass(className, superName, isStatic);
//        enterBlock(false);
//        classBody.classBodyDeclaration().forEach(this::defineClassMember);
//        writeList(classBody.classBodyDeclaration(), this::writeClassBodyDeclaration, out::writeNewLine);
//        exitBlock(false);
//        symbolTable.exitClass();
//    }
//
//    private void defineClassMember(ClassBodyDeclarationContext classBodyDeclaration) {
//        Set<Integer> modifiers = getModifierTypes(classBodyDeclaration.modifier());
//        boolean isStatic = modifiers.contains(JavaLexer.STATIC);
//        if(classBodyDeclaration.memberDeclaration() != null) {
//            var member = classBodyDeclaration.memberDeclaration();
//            if (member.fieldDeclaration() != null) {
//                for (VariableDeclaratorContext variableDeclarator :
//                        member.fieldDeclaration().variableDeclarators().variableDeclarator()) {
//                    var identifier = variableDeclarator.variableDeclaratorId().identifier();
//                    symbolTable.defineField(
//                            identifier.getText(),
//                            parseRawClass(member.fieldDeclaration().typeType()),
//                            isStatic
//                    );
//                }
//            } else if (member.methodDeclaration() != null) {
//                var methodDecl = member.methodDeclaration();
//                var methodName = methodDecl.identifier().getText();
//                var param = methodDecl.formalParameters();
//                List<SourceType> paramTypes;
//                if(param.formalParameterList() != null) {
//                    paramTypes = NncUtils.map(
//                            param.formalParameterList().formalParameter(),
//                            p -> SourceTypeUtil.fromClass(parseRawClass(p.typeType()))
//                    );
//                }
//                else {
//                    paramTypes = List.of();
//                }
//                Class<?> returnType = parseRawClass(methodDecl.typeTypeOrVoid());
//                symbolTable.defineMethod(methodName, paramTypes, new ReflectSourceType(returnType), isStatic);
//            } else if (member.classDeclaration() != null) {
//                // TODO to implement
//            } else if (member.recordDeclaration() != null) {
//                // TODO to implement
//            } else if (member.interfaceDeclaration() != null) {
//                // TODO to implement
//            }
//        }
//    }
//
//    private void writeClassBodyDeclaration(ClassBodyDeclarationContext classBodyDeclaration) {
//        ChildrenStream stream = new ChildrenStream(classBodyDeclaration);
//        if(stream.skipIfNextSymbolOf(JavaLexer.STATIC)) {
//            writeKeyword(TsLexicon.STATIC);
//            writeBlock(stream.next(BlockContext.class));
//        }
//        else {
//            writeMemberDeclaration(
//                    stream.nextList(ModifierContext.class),
//                    stream.next(MemberDeclarationContext.class)
//            );
//        }
//    }
//
//    private void writeMemberDeclaration(List<ModifierContext> modifiers, MemberDeclarationContext member) {
//        if(member.classDeclaration() != null) {
//            writeClassDeclaration(member.classDeclaration(), getModifierTypes(modifiers));
//        }
//        else if(member.interfaceDeclaration() != null) {
//            writeInterfaceDeclaration(member.interfaceDeclaration());
//        }
//        else if(member.fieldDeclaration() != null) {
//            writeFieldDeclaration(member.fieldDeclaration(), modifiers);
//        }
//        else if(member.methodDeclaration() != null) {
//            writeMethodDeclaration(member.methodDeclaration(), modifiers);
//        }
//        else if(member.recordDeclaration() != null) {
//            writeRecordDeclaration(member.recordDeclaration());
//        }
//        else if(member.enumDeclaration() != null) {
//            writeEnumDeclaration(member.enumDeclaration());
//        }
//        else if(member.constructorDeclaration() != null) {
//            writeConstructorDeclaration(member.constructorDeclaration());
//        }
//    }
//
//    private void writeFieldDeclaration(FieldDeclarationContext fieldDeclaration, List<ModifierContext> modifiers) {
//        Set<Integer> modifierTypes = getModifierTypes(modifiers);
//        var isStatic = modifierTypes.contains(JavaLexer.STATIC);
//        List<AnnotationContext> annotations = getAnnotationsFromModifiers(modifiers);
//        for (VariableDeclaratorContext variable : fieldDeclaration.variableDeclarators().variableDeclarator()) {
//            writeModifiers(modifiers);
//            writeVariableDeclarator(variable, fieldDeclaration.typeType(), annotations);
//            symbolTable.defineField(
//                    variable.variableDeclaratorId().identifier().getText(),
//                    parseRawClass(fieldDeclaration.typeType()),
//                    isStatic
//            );
//        }
//    }
//
//    private List<AnnotationContext> getAnnotationsFromModifiers(List<ModifierContext> modifiers) {
//        List<AnnotationContext> result = new ArrayList<>();
//        for (ModifierContext modifier : modifiers) {
//            if(modifier.classOrInterfaceModifier() != null && modifier.classOrInterfaceModifier().annotation() != null) {
//                result.add(modifier.classOrInterfaceModifier().annotation());
//            }
//        }
//        return result;
//    }
//
//    private Set<Integer> getModifierTypes(List<ModifierContext> modifiers) {
//        return getModifierTypes0(
//                NncUtils.mapAndFilter(modifiers, ModifierContext::classOrInterfaceModifier, Objects::nonNull)
//        );
//    }
//
//    private void writeMethodDeclaration(MethodDeclarationContext methodDeclaration, List<ModifierContext> modifiers) {
//        writeNewLine();
//        Set<Integer> mods = getModifierTypes(modifiers);
//        if(mods.contains(JavaLexer.PUBLIC)) {
//            out.writeKeyword(TsLexicon.PUBLIC);
//        }
//        if(mods.contains(JavaLexer.PROTECTED)) {
//            out.writeKeyword(TsLexicon.PROTECTED);
//        }
//        if(mods.contains(JavaLexer.PRIVATE)) {
//            out.writeKeyword(TsLexicon.PRIVATE);
//        }
//        if(mods.contains(JavaLexer.STATIC)) {
//            out.writeKeyword(TsLexicon.STATIC);
//        }
//        writeIdentifier(methodDeclaration.identifier());
//        symbolTable.enterBlock();
//        writeFormalParameters(methodDeclaration.formalParameters());
//        int dimensionsX2 = methodDeclaration.getChildCount() - 4;
//        if(methodDeclaration.THROWS() != null) {
//            dimensionsX2 -= 2;
//        }
//        int dimensions = dimensionsX2 / 2;
//        writeKeyword(COMPACT_COLON);
//
//        writeTypeTypeOrVoid(methodDeclaration.typeTypeOrVoid(), dimensions);
//        List<AnnotationContext> annotations = getAnnotationsFromModifiers(modifiers);
//        if(isNullable(annotations)) {
//            writeNullable();
//        }
//        writeMethodBody(methodDeclaration.methodBody());
//        symbolTable.exitBlock();
//    }
//
//    private void writeNullable() {
//        out.writeKeyword(COMPACT_VERTICAL_BAR);
//        out.writeKeyword(UNDEFINED);
//    }
//
//    private void writeMethodBody(MethodBodyContext methodBody) {
//        if(methodBody.block() != null) {
//            writeBlock(methodBody.block(), false);
//        }
//    }
//
//    private void writeConstructorDeclaration(ConstructorDeclarationContext constructor) {
//        writeNewLine();
//        writeKeyword(TsLexicon.CONSTRUCTOR);
//        symbolTable.enterBlock();
//        writeFormalParameters(constructor.formalParameters());
//        writeBlock(constructor.block(), false);
//        symbolTable.exitBlock();
//    }
//
//    private void writeTypeTypeOrVoid(TypeTypeOrVoidContext typeTypeOrVoid, int dimension) {
//        if(typeTypeOrVoid.typeType() != null) {
//            writeTypeType(typeTypeOrVoid.typeType(), dimension);
//        }
//        else {
//            out.writeKeyword(TsLexicon.VOID);
//        }
//    }
//
//    private void writeFormalParameters(FormalParametersContext formalParameters) {
//        out.writeKeyword(COMPACT_OPEN_PARENTHESIS);
//        if(formalParameters.receiverParameter() != null) {
//            writeReceiverParameter(formalParameters.receiverParameter());
//        }
//        if(formalParameters.formalParameterList() != null) {
//            if(formalParameters.receiverParameter() != null) {
//                out.writeKeyword(TsLexicon.COMMA);
//            }
//            writeFormalParameterList(formalParameters.formalParameterList());
//        }
//        out.writeKeyword(CLOSING_PARENTHESIS);
//    }
//
//    private List<AnnotationContext> getAnnotations(List<VariableModifierContext> varModifiers) {
//        return NncUtils.mapAndFilter(varModifiers, VariableModifierContext::annotation, Objects::nonNull);
//    }
//
//    private void writeReceiverParameter(ReceiverParameterContext receiverParameter) {
//        writeIdentifierAndType(
//                () -> {
//                    writeList(receiverParameter.identifier(), this::writeIdentifier, TsLexicon.DOT);
//                    if(receiverParameter.identifier().size() > 0) {
//                        out.writeKeyword(TsLexicon.DOT);
//                    }
//                    out.writeKeyword(TsLexicon.THIS);
//                },
//                () -> writeTypeType(receiverParameter.typeType()),
//                0
//        );
//    }
//
//    private void writeFormalParameterList(FormalParameterListContext formalParameterList) {
//        writeList(formalParameterList.formalParameter(), this::writeFormalParameter, TsLexicon.COMMA);
//        if(formalParameterList.lastFormalParameter() != null) {
//            if(formalParameterList.formalParameter().size() > 0) {
//                out.writeKeyword(TsLexicon.COMMA);
//            }
//            writeLastFormalParameter(formalParameterList.lastFormalParameter());
//        }
//    }
//
//    private void writeFormalParameter(FormalParameterContext formalParameter) {
//        symbolTable.defineLocalVariable(
//                formalParameter.variableDeclaratorId().identifier().getText(),
//                parseRawClass(formalParameter.typeType())
//        );
//        boolean nullable = isNullable(getAnnotations(formalParameter.variableModifier()));
//        writeIdentifierAndType(
//                formalParameter.variableDeclaratorId().identifier(),
//                () -> {
//                    writeTypeType(formalParameter.typeType());
//                    if(nullable) {
//                        writeNullable();
//                    }
//                },
//                0
//        );
//    }
//
//    private void writeLastFormalParameter(LastFormalParameterContext lastFormalParameter) {
//        symbolTable.defineLocalVariable(
//                lastFormalParameter.variableDeclaratorId().identifier().getText(),
//                parseRawClass(lastFormalParameter.typeType())
//        );
//        writeIdentifierAndType(
//                () -> {
//                    out.writeKeyword(TRIPLE_DOTS);
//                    writeIdentifier(lastFormalParameter.variableDeclaratorId().identifier());
//                },
//                () -> writeTypeType(lastFormalParameter.typeType()),
//                getDimension(lastFormalParameter.variableDeclaratorId())
//        );
//    }
//
//    private void writeEnumDeclaration(EnumDeclarationContext enumDeclaration) {
//
//    }
//
//    private void writeRecordDeclaration(RecordDeclarationContext recordDeclaration) {
//
//    }
//
//    private void writeModifiers(List<ModifierContext> modifiers) {
//        for (ModifierContext modifier : modifiers) {
//            if(modifier.classOrInterfaceModifier() != null) {
//                writeClassOrInterfaceModifier(modifier.classOrInterfaceModifier());
//            }
//        }
//    }
//
//    private void writeClassOrInterfaceModifier(ClassOrInterfaceModifierContext classOrInterfaceModifier) {
//        var child = classOrInterfaceModifier.getChild(0);
//        if(child instanceof TerminalNode terminalNode) {
//            TsLexicon tsModifier = MODIFIER_MAP.get(terminalNode.getSymbol().getType());
//            if(tsModifier != null) {
//                out.writeKeyword(tsModifier);
//            }
//        }
//    }
//
//    private void writePrimitiveType(PrimitiveTypeContext primitiveType) {
//        if(primitiveType.BOOLEAN() != null) {
//            out.writeIdentifier("boolean");
//        }
//        else if(primitiveType.BYTE() != null || primitiveType.SHORT() != null || primitiveType.INT() != null
//                || primitiveType.LONG() != null || primitiveType.FLOAT() != null || primitiveType.DOUBLE() != null) {
//            out.writeIdentifier("number");
//        }
//        else if(primitiveType.CHAR() != null) {
//            out.writeIdentifier("string");
//        }
//        else {
//            throw new InternalException("Unrecognized primitive type " + primitiveType.getText());
//        }
//    }
//
//    private void writeClassOrInterfaceType(ClassOrInterfaceTypeContext classOrInterfaceType) {
//        ChildrenStream stream = new ChildrenStream(classOrInterfaceType);
//        while (stream.isNextIdentifier()) {
//            writeIdentifier(stream.nextIdentifier());
//            if(stream.isNextInstanceOf(TypeArgumentsContext.class)) {
//                writeTypeArguments(stream.next(TypeArgumentsContext.class));
//            }
//            out.writeKeyword(TsLexicon.DOT);
//        }
//        writeTypeIdentifier(stream.next(TypeIdentifierContext.class));
//        if(stream.isNextInstanceOf(TypeArgumentsContext.class)) {
//            writeTypeArguments(stream.next(TypeArgumentsContext.class));
//        }
//    }
//
//    private void writeAnnotation(AnnotationContext annotation) {
//    }
//
//    private void writeTypeArguments(TypeArgumentsContext typeArguments) {
//        out.writeKeyword(OPEN_ANGULAR_BRACKET);
//        writeList(typeArguments.typeArgument(), this::writeTypeArgument, TsLexicon.COMMA);
//        out.writeKeyword(CLOSING_ANGULAR_BRACKET);
//    }
//
//    private void writeTypeArgument(TypeArgumentContext typeArgument) {
//        if(typeArgument.typeType() == null) {
//            out.writeIdentifier("?");
//        }
//        else {
//            if (typeArgument.SUPER() != null) {
//                out.writeIdentifier("? super");
//            } else if (typeArgument.EXTENDS() != null) {
//                out.writeIdentifier("? extends");
//            }
//            writeTypeType(typeArgument.typeType());
//        }
//    }
//
//    private void writeTypeIdentifier(TypeIdentifierContext typeIdentifier) {
//        writeText(typeIdentifier.getText());
//    }
//
//    private void writeTypeParameters(TypeParametersContext typeParameters) {
//        out.writeKeyword(OPEN_ANGULAR_BRACKET);
//        for (int i = 0; i < typeParameters.getChildCount(); i++) {
//            if(i > 0) {
//                out.writeComma();
//            }
//            out.writeIdentifier(typeParameters.typeParameter(i).identifier().getText());
//        }
//        out.writeKeyword(CLOSING_ANGULAR_BRACKET);
//    }
//
//    private void writeInterfaceDeclaration(InterfaceDeclarationContext interfaceDeclaration) {
//
//    }
//
//    private void writeNewLine() {
//        out.writeNewLine();
//    }
//
//    private void writeKeyword(TsLexicon tsLexicon) {
//        out.writeKeyword(tsLexicon);
//    }
//
//    private void enterBlock(boolean pushBlock) {
//        out.writeKeyword(OPEN_CURLY_BRACE);
//        if(pushBlock) {
//            symbolTable.enterBlock();
//        }
//    }
//
//    private void exitBlock(boolean popBlock) {
//        if(popBlock) {
//            symbolTable.exitBlock();
//        }
//        out.writeKeyword(CLOSING_CURLY_BRACE);
//    }
//
//    private String getResult() {
//        return out.toString();
//    }
//
//}
