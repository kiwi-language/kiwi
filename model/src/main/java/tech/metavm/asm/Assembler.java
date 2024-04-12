package tech.metavm.asm;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RuleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.asm.antlr.AssemblyLexer;
import tech.metavm.asm.antlr.AssemblyParser;
import tech.metavm.asm.antlr.AssemblyParserBaseVisitor;
import tech.metavm.flow.MethodDTOBuilder;
import tech.metavm.flow.NodeDTOFactory;
import tech.metavm.flow.UpdateOp;
import tech.metavm.flow.ValueDTOFactory;
import tech.metavm.flow.rest.*;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.type.Access;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.TypeCategory;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.InternalException;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class Assembler {

    public static final Logger LOGGER = LoggerFactory.getLogger(Assembler.class);

    private final char[] buf = new char[1024 * 1024];

    private final Map<AsmType, String> typeIds = new HashMap<>();
    private final Map<FieldKey, String> fieldIds = new HashMap<>();
    private final Map<MethodKey, String> methodIds = new HashMap<>();
    private final List<TypeDTO> types = new ArrayList<>();
    private final Map<AsmType, TypeDTO> compositeTypes = new HashMap<>();

    public Assembler(Map<AsmType, String> stdTypeIds) {
        typeIds.putAll(stdTypeIds);
    }

    public List<TypeDTO> assemble(List<String> sourcePaths) {
        var units = NncUtils.map(sourcePaths, path -> parse(getSource(path)));
        assignIds(units);
        logIds();
        emit(units);
        emitCompositeTypes(units);
        return getAllTypes();
    }

    private void assignIds(List<AssemblyParser.CompilationUnitContext> units) {
        units.forEach(unit -> unit.accept(new IdAssigner()));
    }

    private void emit(List<AssemblyParser.CompilationUnitContext> units) {
        units.forEach(unit -> unit.accept(new Emitter()));
    }

    private void emitCompositeTypes(List<AssemblyParser.CompilationUnitContext> units) {
        units.forEach(unit -> unit.accept(new CompositeTypeEmitter()));
    }

    private String getTypeId(AsmType type) {
        return Objects.requireNonNull(typeIds.get(type), () -> "Can not find id for type: " + type.name());
    }

    public List<TypeDTO> getTypes() {
        return types;
    }

    public static class Modifiers {

        public static final String PRIVATE = "private";
        public static final String PUBLIC = "public";
        public static final String PROTECTED = "protected";
        public static final String STATIC = "static";
        public static final String READONLY = "readonly";
        public static final String ABSTRACT = "abstract";
        public static final String CHILD = "child";
        public static final String TITLE = "title";
    }

    private record ClassInfo(
            @Nullable ClassInfo parent,
            ClassAsmType type,
            List<String> typeParameters
    ) {

        public static ClassInfo fromContext(AssemblyParser.ClassDeclarationContext classDeclaration, @Nullable ClassInfo parent) {
            return new ClassInfo(
                    parent,
                    new ClassAsmType(classDeclaration.IDENTIFIER().getText(), List.of()),
                    classDeclaration.typeParameters() != null ?
                            NncUtils.map(classDeclaration.typeParameters().typeParameter(), tv -> tv.IDENTIFIER().getText())
                            : List.of()
            );
        }

        public String name() {
            return type.name();
        }

    }

    private class IdAssigner extends AssemblyParserBaseVisitor<Void> {

        private ClassInfo currentClass;

        @Override
        public Void visitClassDeclaration(AssemblyParser.ClassDeclarationContext ctx) {
            var className = ctx.IDENTIFIER().getText();
            LOGGER.info("visiting class: {}", className);
            enterClass(ctx);
            typeIds.put(new ClassAsmType(className, List.of()), TmpId.randomString());
            try {
                return super.visitClassDeclaration(ctx);
            } finally {
                exitClass();
            }
        }

        @Override
        public Void visitFieldDeclaration(AssemblyParser.FieldDeclarationContext ctx) {
            var name = ctx.IDENTIFIER().getText();
            LOGGER.info("visiting field: {}", name);
            fieldIds.put(new FieldKey(currentClass().name(), name), TmpId.randomString());
            return super.visitFieldDeclaration(ctx);
        }

        @Override
        public Void visitConstructorDeclaration(AssemblyParser.ConstructorDeclarationContext ctx) {
            processFunction(ctx.IDENTIFIER().getText(), ctx.formalParameters());
            return super.visitConstructorDeclaration(ctx);
        }

        @Override
        public Void visitMethodDeclaration(AssemblyParser.MethodDeclarationContext ctx) {
            var name = ctx.IDENTIFIER().getText();
            LOGGER.info("visiting method: {}", name);
            processFunction(name, ctx.formalParameters());
            return super.visitMethodDeclaration(ctx);
        }

        @Override
        public Void visitTypeParameter(AssemblyParser.TypeParameterContext ctx) {
            var typeVariable = new AsmTypeVariable(currentClass().name(), ctx.IDENTIFIER().getText());
            typeIds.put(typeVariable, TmpId.randomString());
            return super.visitTypeParameter(ctx);
        }

        @Override
        public Void visitTypeType(AssemblyParser.TypeTypeContext ctx) {
            if (ctx.primitiveType() == null && ctx.classOrInterfaceType() == null) {
                var type = parseType(ctx, currentClass);
                if (!typeIds.containsKey(type))
                    typeIds.put(type, TmpId.randomString());
            }
            return super.visitTypeType(ctx);
        }

        @Override
        public Void visitClassOrInterfaceType(AssemblyParser.ClassOrInterfaceTypeContext ctx) {
            var type = parseClassType(ctx, currentClass);
            if (type instanceof ClassAsmType classAsmType && classAsmType.isParameterized() && !typeIds.containsKey(type))
                typeIds.put(type, TmpId.randomString());
            return super.visitClassOrInterfaceType(ctx);
        }

        private void processFunction(String name, AssemblyParser.FormalParametersContext parameters) {
            List<AsmType> types = parameters.formalParameterList() != null ? NncUtils.map(
                    parameters.formalParameterList().formalParameter(),
                    p -> parseType(p.typeType(), currentClass)
            ) : List.of();
            methodIds.put(new MethodKey(currentClass().name(), name, types), TmpId.randomString());
        }

        private void enterClass(AssemblyParser.ClassDeclarationContext classDecl) {
            currentClass = ClassInfo.fromContext(classDecl, currentClass);
        }

        private void exitClass() {
            currentClass = currentClass.parent;
        }

        private ClassInfo currentClass() {
            return Objects.requireNonNull(currentClass);
        }

    }

    private class Emitter extends AssemblyParserBaseVisitor<Void> {

        private final LinkedList<ClassTypeDTOBuilder> builders = new LinkedList<>();
        private final LinkedList<Set<String>> modsStack = new LinkedList<>();
        private ClassInfo currentClass;

//        private final LinkedList<MethodDTOBuilder> methodBuilders = new LinkedList<>();
//        private final LinkedList<ScopeDTO> scopes = new LinkedList<>();

        private int nextNodeNum = 0;

        @Override
        public Void visitTypeDeclaration(AssemblyParser.TypeDeclarationContext ctx) {
            modsStack.push(NncUtils.mapUnique(ctx.classOrInterfaceModifier(), RuleContext::getText));
            super.visitTypeDeclaration(ctx);
            modsStack.pop();
            return null;
        }

        @Override
        public Void visitClassDeclaration(AssemblyParser.ClassDeclarationContext ctx) {
            var name = ctx.IDENTIFIER().getText();
            var builder = ClassTypeDTOBuilder.newBuilder(name)
                    .code(name)
                    .struct(ctx.STRUCT() != null)
                    .id(getTypeId(new ClassAsmType(name, List.of())))
                    .typeParameterIds(
                            ctx.typeParameters() != null ?
                                    NncUtils.map(ctx.typeParameters().typeParameter(),
                                            tp -> getTypeId(new AsmTypeVariable(name, tp.IDENTIFIER().getText()))) :
                                    List.of()
                    );
            builders.push(builder);
            currentClass = ClassInfo.fromContext(ctx, currentClass);
            super.visitClassDeclaration(ctx);
            currentClass = currentClass.parent;
            types.add(builder.build());
            builders.pop();
            return null;
        }

        @Override
        public Void visitTypeParameter(AssemblyParser.TypeParameterContext ctx) {
            var name = ctx.IDENTIFIER().getText();
            var typeVariable = new AsmTypeVariable(currentClass.name(), name);
            var boundId = ctx.typeType() != null ? getTypeId(parseType(ctx.typeType(), currentClass)) : null;
            types.add(new TypeDTO(
                    getTypeId(typeVariable),
                    name,
                    name,
                    TypeCategory.VARIABLE.code(),
                    false,
                    false,
                    new TypeVariableParam(
                            getTypeId(currentClass.type),
                            currentClass.typeParameters.indexOf(name),
                            boundId != null ? List.of(boundId) : List.of()
                    )
            ));
            return null;
        }

        @Override
        public Void visitFieldDeclaration(AssemblyParser.FieldDeclarationContext ctx) {
            var classBuilder = builder();
            var typeId = getTypeId(parseType(ctx.typeType(), currentClass));
            var name = ctx.IDENTIFIER().getText();
            var mods = currentMods();
            var fieldBuilder = FieldDTOBuilder.newBuilder(name, typeId)
                    .id(fieldIds.get(new FieldKey(classBuilder.getName(), name)))
                    .code(name)
                    .access(getAccess(mods).code());
            if (mods.contains(Modifiers.CHILD))
                fieldBuilder.isChild(true);
            if (mods.contains(Modifiers.STATIC))
                fieldBuilder.isStatic(true);
            if (mods.contains(Modifiers.READONLY))
                fieldBuilder.readonly(true);
            if (mods.contains(Modifiers.TITLE))
                fieldBuilder.asTitle(true);
            classBuilder.addField(fieldBuilder.build());
            return null;
        }

        @Override
        public Void visitConstructorDeclaration(AssemblyParser.ConstructorDeclarationContext ctx) {
            processFunction(ctx.IDENTIFIER().getText(), ctx.formalParameters().formalParameterList(),
                    null, ctx.block(), currentMods(), true);
            return null;
        }

        @Override
        public Void visitMethodDeclaration(AssemblyParser.MethodDeclarationContext ctx) {
            processFunction(ctx.IDENTIFIER().getText(), ctx.formalParameters().formalParameterList(),
                    ctx.typeTypeOrVoid(), ctx.methodBody().block(), currentMods(), false);
            return null;
        }

        @Override
        public Void visitClassBodyDeclaration(AssemblyParser.ClassBodyDeclarationContext ctx) {
            modsStack.push(NncUtils.mapUnique(ctx.modifier(), RuleContext::getText));
            super.visitClassBodyDeclaration(ctx);
            modsStack.pop();
            return null;
        }

        private void processFunction(String name,
                                     @Nullable AssemblyParser.FormalParameterListContext formalParameterList,
                                     @Nullable AssemblyParser.TypeTypeOrVoidContext returnType,
                                     @Nullable AssemblyParser.BlockContext block,
                                     Set<String> mods, boolean isConstructor) {
            var classBuilder = builder();
            List<AssemblyParser.FormalParameterContext> params = NncUtils.getOrElse(
                    formalParameterList, AssemblyParser.FormalParameterListContext::formalParameter, List.of()
            );
            var paramTypes = NncUtils.map(params, p -> parseType(p.typeType(), currentClass));
            var methodBuilder = MethodDTOBuilder.newBuilder(classBuilder.getId(), name)
                    .code(name)
                    .id(methodIds.get(new MethodKey(classBuilder.getName(), name, paramTypes)))
                    .access(getAccess(mods).code());
            for (var param : params) {
                methodBuilder.addParameter(
                        new ParameterDTO(
                                TmpId.randomString(),
                                param.IDENTIFIER().getText(),
                                param.IDENTIFIER().getText(),
                                getTypeId(parseType(param.typeType(), currentClass)),
                                null,
                                null,
                                null
                        )
                );
            }
            if (isConstructor) {
                methodBuilder.isConstructor(true);
                methodBuilder.returnTypeId(classBuilder.getId());
            } else
                methodBuilder.returnTypeId(getTypeId(parseType(Objects.requireNonNull(returnType), currentClass)));
            if (mods.contains(Modifiers.STATIC))
                methodBuilder.isStatic(true);
            else
                methodBuilder.addNode(NodeDTOFactory.createSelfNode(NncUtils.randomNonNegative(), "this", classBuilder.getId()));
            methodBuilder.addNode(NodeDTOFactory.createInputNode(
                    NncUtils.randomNonNegative(),
                    "_input",
                    NncUtils.map(
                            params,
                            p -> InputFieldDTO.create(
                                    p.IDENTIFIER().getText(),
                                    getTypeId(parseType(p.typeType(), currentClass))
                            )
                    )
            ));
            if (block != null)
                processMethodBlock(block, methodBuilder);
            classBuilder.addMethod(methodBuilder.build());
        }

        private Set<String> currentMods() {
            return Objects.requireNonNull(modsStack.peek());
        }

        private void processMethodBlock(AssemblyParser.BlockContext block, MethodDTOBuilder methodBuilder) {
            for (var stmt : block.labeledStatement()) {
                methodBuilder.addNode(processLabeledStatement(stmt));
            }
        }

        private String nextNodeName() {
            return "__node__" + nextNodeNum++;
        }

        private NodeDTO processLabeledStatement(AssemblyParser.LabeledStatementContext labeledStatement) {
            var name = labeledStatement.IDENTIFIER() != null ? labeledStatement.IDENTIFIER().getText() :
                    nextNodeName();
            return processStatement(name, labeledStatement.statement());
        }

        private NodeDTO processStatement(String name, AssemblyParser.StatementContext statement) {
            try {
                if (statement.statementExpression != null) {
                    return NodeDTOFactory.createValueNode(
                            NncUtils.randomNonNegative(),
                            name,
                            ValueDTOFactory.createExpression(parseExpression(statement.statementExpression))
                    );
                }
                if (statement.bop != null) {
                    var object = statement.THIS() != null ? ValueDTOFactory.createReference("this")
                            : ValueDTOFactory.createExpression(statement.IDENTIFIER(0).getText());
                    return NodeDTOFactory.createUpdateObjectNode(
                            NncUtils.randomNonNegative(),
                            name,
                            object,
                            List.of(
                                    new UpdateFieldDTO(
                                            null,
                                            statement.IDENTIFIER(statement.IDENTIFIER().size() - 1).getText(),
                                            parseUpdateOp(statement.bop.getText()),
                                            ValueDTOFactory.createExpression(parseExpression(statement.expression()))
                                    )
                            )
                    );
                }
                if (statement.RETURN() != null) {
                    return NodeDTOFactory.createReturnNode(
                            NncUtils.randomNonNegative(),
                            name,
                            statement.expression() != null ?
                                    ValueDTOFactory.createExpression(parseExpression(statement.expression())) : null
                    );
                }
                if (statement.NEW() != null || statement.NEW_UNBOUND() != null || statement.NEW_EPHEMERAL() != null) {
                    var type = (ClassAsmType) parseClassType(statement.creator().classOrInterfaceType(), currentClass);
                    var methodName = type.name;
                    List<AssemblyParser.ExpressionContext> arguments =
                            NncUtils.getOrElse(
                                    statement.creator().arguments().expressionList(),
                                    AssemblyParser.ExpressionListContext::expression,
                                    List.of()
                            );
                    return NodeDTOFactory.createUnresolvedNewObjectNode(
                            NncUtils.randomNonNegative(),
                            name,
                            getTypeId(type),
                            methodName,
                            NncUtils.map(arguments, arg -> ValueDTOFactory.createExpression(parseExpression(arg))),
                            statement.NEW_UNBOUND() != null,
                            statement.NEW_EPHEMERAL() != null
                    );
                }
                if (statement.methodCall() != null) {
                    var methodCall = statement.methodCall();
                    List<ValueDTO> arguments = methodCall.expressionList() != null ?
                            NncUtils.map(methodCall.expressionList().expression(), this::parseValue) : List.of();
                    return NodeDTOFactory.createUnresolvedMethodCallNode(
                            NncUtils.randomNonNegative(),
                            name,
                            methodCall.IDENTIFIER().getText(),
                            null,
                            parseValue(methodCall.expression()),
                            arguments
                    );
                }
                throw new InternalException("Unknown statement: " + statement.getText());
            } catch (Exception e) {
                throw new InternalException("Fail to process statement: " + statement.getText(), e);
            }
        }

        private int parseUpdateOp(String bop) {
            if (bop.equals("="))
                return UpdateOp.SET.code();
            if (bop.equals("+="))
                return UpdateOp.INC.code();
            if (bop.equals("-="))
                return UpdateOp.DEC.code();
            throw new InternalException("Unknown binary operator: " + bop);
        }

        private ValueDTO parseValue(AssemblyParser.ExpressionContext expression) {
            return ValueDTOFactory.createExpression(expression.getText());
        }

        private String parseExpression(AssemblyParser.ExpressionContext expression) {
            return expression.getText();
        }

        private Access getAccess(Set<String> mods) {
            if (mods.contains(Modifiers.PUBLIC))
                return Access.PUBLIC;
            if (mods.contains(Modifiers.PRIVATE))
                return Access.PRIVATE;
            if (mods.contains(Modifiers.PROTECTED))
                return Access.PROTECTED;
            return Access.PACKAGE;
        }

        private ClassTypeDTOBuilder builder() {
            return Objects.requireNonNull(builders.peek());
        }

    }

    private class CompositeTypeEmitter extends AssemblyParserBaseVisitor<Void> {

        private ClassInfo currentClass;

        @Override
        public Void visitClassDeclaration(AssemblyParser.ClassDeclarationContext ctx) {
            currentClass = ClassInfo.fromContext(ctx, currentClass);
            super.visitClassDeclaration(ctx);
            currentClass = currentClass.parent;
            return null;
        }

        @Override
        public Void visitTypeType(AssemblyParser.TypeTypeContext ctx) {
            if (ctx.classOrInterfaceType() == null) {
                var type = parseType(ctx, currentClass);
                if (!compositeTypes.containsKey(type)) {
                    var id = getTypeId(type);
                    var typeDTO = switch (type) {
                        case UnionAsmType unionAsmType -> new TypeDTO(
                                id,
                                type.name(),
                                null,
                                TypeCategory.UNION.code(),
                                false,
                                false,
                                new UnionTypeParam(NncUtils.map(unionAsmType.members, Assembler.this::getTypeId))
                        );
                        case UncertainAsmType uncertainAsmType -> new TypeDTO(
                                id,
                                type.name(),
                                null,
                                TypeCategory.UNCERTAIN.code(),
                                false,
                                false,
                                new UncertainTypeParam(
                                        getTypeId(uncertainAsmType.lowerBound),
                                        getTypeId(uncertainAsmType.upperBound)
                                )
                        );
                        case ArrayAsmType arrayAsmType -> new TypeDTO(
                                id,
                                type.name(),
                                null,
                                switch (arrayAsmType.kind) {
                                    case CHILD -> TypeCategory.CHILD_ARRAY.code();
                                    case READ_WRITE -> TypeCategory.READ_WRITE_ARRAY.code();
                                    case READ_ONLY -> TypeCategory.READ_ONLY_ARRAY.code();
                                },
                                false,
                                false,
                                new ArrayTypeParam(
                                        getTypeId(arrayAsmType.elementType),
                                        switch (arrayAsmType.kind) {
                                            case CHILD -> ArrayKind.CHILD.code();
                                            case READ_WRITE -> ArrayKind.READ_WRITE.code();
                                            case READ_ONLY -> ArrayKind.READ_ONLY.code();
                                        }
                                )
                        );
                        default -> null;
                    };
                    if (typeDTO != null)
                        compositeTypes.put(type, typeDTO);
                }
            }
            return super.visitTypeType(ctx);
        }

        @Override
        public Void visitClassOrInterfaceType(AssemblyParser.ClassOrInterfaceTypeContext ctx) {
            var type = parseClassType(ctx, currentClass);
            if (type instanceof ClassAsmType classAsmType && classAsmType.isParameterized() && !compositeTypes.containsKey(type)) {
                var id = getTypeId(classAsmType);
                compositeTypes.put(type, new TypeDTO(
                                id,
                                type.name(),
                                null,
                                TypeCategory.CLASS.code(),
                                false,
                                false,
                                new PTypeDTO(
                                        id,
                                        getTypeId(new ClassAsmType(classAsmType.name, List.of())),
                                        NncUtils.map(
                                                classAsmType.typeArguments, Assembler.this::getTypeId
                                        ),
                                        List.of(),
                                        List.of(),
                                        List.of(),
                                        List.of()
                                )
                        )
                );
            }
            return super.visitClassOrInterfaceType(ctx);
        }

    }

    private AssemblyParser.CompilationUnitContext parse(String source) {
        var input = CharStreams.fromString(source);
        var parser = new AssemblyParser(new CommonTokenStream(new AssemblyLexer(input)));
        return parser.compilationUnit();
    }

    private AsmType parseType(AssemblyParser.TypeTypeOrVoidContext typeTypeOrVoid, ClassInfo currentClass) {
        if (typeTypeOrVoid.VOID() != null)
            return new PrimitiveAsmType(AsmPrimitiveKind.VOID);
        return parseType(typeTypeOrVoid.typeType(), currentClass);
    }

    private AsmType parseType(AssemblyParser.TypeTypeContext typeType, ClassInfo currentClass) {
        if (typeType.primitiveType() != null)
            return parsePrimitiveType(typeType.primitiveType());
        if (typeType.classOrInterfaceType() != null) {
            return parseClassType(typeType.classOrInterfaceType(), currentClass);
        }
        if (typeType.arrayKind() != null) {
            var arrayKind = typeType.arrayKind();
            var elementType = parseType(typeType.typeType(0), currentClass);
            var kind = arrayKind.R() != null ? AsmArrayKind.READ_ONLY :
                    arrayKind.RW() != null ? AsmArrayKind.READ_WRITE :
                            arrayKind.C() != null ? AsmArrayKind.CHILD : null;
            if (kind == null)
                throw new InternalException("Unknown array kind");

            return new ArrayAsmType(elementType, kind);
        }
        if (!typeType.BITOR().isEmpty()) {
            var members = NncUtils.map(
                    typeType.typeType(),
                    typeType1 -> parseType(typeType1, currentClass)
            );
            return new UnionAsmType(new HashSet<>(members));
        }
        if (!typeType.BITAND().isEmpty()) {
            var types = NncUtils.map(
                    typeType.typeType(),
                    typeType1 -> parseType(typeType1, currentClass)
            );
            return new IntersectionAsmType(new HashSet<>(types));
        }
        if (typeType.ARROW() != null) {
            int numParams = typeType.typeType().size() - 1;
            var parameterTypes = NncUtils.map(
                    typeType.typeType().subList(0, numParams),
                    typeType1 -> parseType(typeType1, currentClass)
            );
            var returnType = parseType(typeType.typeType(numParams), currentClass);
            return new FunctionAsmType(parameterTypes, returnType);
        }
        if (typeType.LBRACK() != null) {
            var lowerBound = parseType(typeType.typeType(0), currentClass);
            var upperBound = parseType(typeType.typeType(1), currentClass);
            return new UncertainAsmType(lowerBound, upperBound);
        }
        throw new InternalException("Unknown type: " + typeType.getText());
    }

    private PrimitiveAsmType parsePrimitiveType(AssemblyParser.PrimitiveTypeContext primitiveType) {
        AsmPrimitiveKind kind;
        if (primitiveType.INT() != null)
            kind = AsmPrimitiveKind.LONG;
        else if (primitiveType.DOUBLE() != null)
            kind = AsmPrimitiveKind.DOUBLE;
        else if (primitiveType.BOOLEAN() != null)
            kind = AsmPrimitiveKind.BOOLEAN;
        else if (primitiveType.STRING() != null)
            kind = AsmPrimitiveKind.STRING;
        else if (primitiveType.PASSWORD() != null)
            kind = AsmPrimitiveKind.PASSWORD;
        else if (primitiveType.TIME() != null)
            kind = AsmPrimitiveKind.TIME;
        else if (primitiveType.NULL() != null)
            kind = AsmPrimitiveKind.NULL;
        else if (primitiveType.VOID() != null)
            kind = AsmPrimitiveKind.VOID;
        else
            throw new InternalException("Unknown primitive type");
        return new PrimitiveAsmType(kind);
    }

    private AsmType parseClassType(AssemblyParser.ClassOrInterfaceTypeContext classOrInterfaceType, @Nullable ClassInfo currentClass) {
        var name = classOrInterfaceType.qualifiedName().getText();
        if (!name.contains(".")) {
            var k = currentClass;
            while (k != null) {
                if (k.typeParameters.contains(name))
                    return new AsmTypeVariable(k.name(), name);
                k = k.parent;
            }
        }
        List<AsmType> typeArguments = classOrInterfaceType.typeArguments() != null ? NncUtils.map(
                classOrInterfaceType.typeArguments().typeType(),
                typeType1 -> parseType(typeType1, currentClass)
        ) : List.of();
        return new ClassAsmType(name, typeArguments);
    }

    public interface AsmType {

        String name();

    }

    public record ClassAsmType(String name, List<AsmType> typeArguments) implements AsmType {

        public static ClassAsmType create(String name) {
            return new ClassAsmType(name, List.of());
        }

        @Override
        public String name() {
            return typeArguments().isEmpty() ? name : name + "<" + NncUtils.join(typeArguments, AsmType::name, ",") + ">";
        }

        boolean isParameterized() {
            return !typeArguments.isEmpty();
        }
    }

    public record UnionAsmType(Set<AsmType> members) implements AsmType {
        @Override
        public String name() {
            return NncUtils.join(members, AsmType::name, "|");
        }
    }

    public record IntersectionAsmType(Set<AsmType> types) implements AsmType {
        @Override
        public String name() {
            return NncUtils.join(types, AsmType::name, "&");
        }
    }

    public record FunctionAsmType(List<AsmType> parameterTypes, AsmType returnType) implements AsmType {

        @Override
        public String name() {
            return NncUtils.join(parameterTypes, AsmType::name, ",") + "->" + returnType.name();
        }

    }

    public record UncertainAsmType(AsmType lowerBound, AsmType upperBound) implements AsmType {

        @Override
        public String name() {
            return "[" + lowerBound.name() + ", " + upperBound.name() + "]";
        }
    }

    public record ArrayAsmType(AsmType elementType, AsmArrayKind kind) implements AsmType {

        @Override
        public String name() {
            return elementType.name() + "[" + kind.label + "]";
        }
    }

    public record PrimitiveAsmType(AsmPrimitiveKind kind) implements AsmType {
        @Override
        public String name() {
            return kind.name();
        }
    }

    public record AsmTypeVariable(String ownerName, String name) implements AsmType {
        @Override
        public String name() {
            return ownerName + "." + name;
        }
    }

    public enum AsmPrimitiveKind {
        LONG,
        STRING,
        PASSWORD,
        NULL,
        TIME,
        DOUBLE,
        BOOLEAN,
        VOID
    }

    private enum AsmArrayKind {
        READ_ONLY("R"),
        READ_WRITE("RW"),
        CHILD("C"),

        ;

        final String label;

        AsmArrayKind(String label) {
            this.label = label;
        }
    }

    private String getSource(String path) {
        try (var reader = new BufferedReader(new FileReader(path))) {
            int n = reader.read(buf);
            return new String(buf, 0, n);
        } catch (IOException e) {
            throw new InternalException("Can not read source '" + path + "'", e);
        }
    }

    private record FieldKey(String typeName, String fieldName) {
    }

    private record MethodKey(String typeName, String methodName, List<AsmType> parameterTypes) {
    }

    public List<TypeDTO> getAllTypes() {
        var allTypes = new ArrayList<>(types);
        allTypes.addAll(compositeTypes.values());
        return allTypes;
    }

    public void logIds() {
        LOGGER.info("Type IDs: {}", typeIds);
        LOGGER.info("Field IDs: {}", fieldIds);
        LOGGER.info("Method IDs: {}", methodIds);
    }

    public void logTypes(String file) {
        try (var writer = new FileWriter(file)) {
            var json = NncUtils.toJSONString(getAllTypes());
            writer.write(json);
        } catch (IOException e) {
            throw new InternalException("fail to write to file: '" + file + "'");
        }
    }

    public void writeTypes(String path) {

    }

}
