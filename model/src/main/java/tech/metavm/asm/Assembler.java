package tech.metavm.asm;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.asm.antlr.AssemblyLexer;
import tech.metavm.asm.antlr.AssemblyParser;
import tech.metavm.asm.antlr.AssemblyParserBaseVisitor;
import tech.metavm.flow.*;
import tech.metavm.flow.rest.*;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.type.Access;
import tech.metavm.object.type.ArrayKind;
import tech.metavm.object.type.TypeCategory;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.InternalException;
import tech.metavm.util.LinkedList;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

import static java.util.Objects.requireNonNull;

public class Assembler {

    public static final Logger logger = LoggerFactory.getLogger(Assembler.class);
    public static final Pattern CLASS_NAME_PTN = Pattern.compile("[A-Z][a-z0-9_]*");

    private final char[] buf = new char[1024 * 1024];

    private final Map<AsmType, String> typeIds = new HashMap<>();
    private final Map<FieldKey, String> fieldIds = new HashMap<>();
    private final Map<MethodKey, String> methodIds = new HashMap<>();
    private final Map<String, List<ClassAsmType>> superTypes = new HashMap<>();
    private final List<TypeDTO> types = new ArrayList<>();
    private final Map<AsmType, TypeDTO> compositeTypes = new HashMap<>();
    private final Map<ParserRuleContext, Map<AsmAttributeKey<?>, Object>> attributes = new HashMap<>();


    public Assembler(Map<AsmType, String> stdTypeIds) {
        typeIds.putAll(stdTypeIds);
    }

    public List<TypeDTO> assemble(List<String> sourcePaths) {
        var units = NncUtils.map(sourcePaths, path -> parse(getSource(path)));
        buildAST(units);
        assignIds(units);
        logIds();
        emit(units);
        emitCompositeTypes(units);
        return getAllTypes();
    }

    private void buildAST(List<AssemblyParser.CompilationUnitContext> units) {
        units.forEach(unit -> unit.accept(new AstBuilder()));
    }

    private void assignIds(List<AssemblyParser.CompilationUnitContext> units) {
        units.forEach(unit -> unit.accept(new Preprocessor()));
    }

    private void emit(List<AssemblyParser.CompilationUnitContext> units) {
        units.forEach(unit -> unit.accept(new Emitter()));
    }

    private void emitCompositeTypes(List<AssemblyParser.CompilationUnitContext> units) {
        units.forEach(unit -> unit.accept(new CompositeTypeEmitter()));
    }

    private String getTypeId(AsmType type) {
        return requireNonNull(typeIds.get(type), () -> "Can not find id for type: " + type.name());
    }

    private String getMethodId(MethodKey methodKey) {
        return requireNonNull(methodIds.get(methodKey), () -> "method '" + methodKey.name() + "' is not defined");
    }

    private static List<String> parseTypeParameters(@Nullable AssemblyParser.TypeParametersContext typeParameters) {
        return typeParameters != null ? NncUtils.map(typeParameters.typeParameter(), tv -> tv.IDENTIFIER().getText()) : List.of();
    }

    private List<ParameterDTO> parseParameterList(@Nullable AssemblyParser.FormalParameterListContext parameterList, AsmScope scope) {
        if(parameterList == null)
            return List.of();
        return NncUtils.map(parameterList.formalParameter(), p -> parseParameter(p, scope));
    }

    private ParameterDTO parseParameter(AssemblyParser.FormalParameterContext parameter, AsmScope scope) {
        var name = parameter.IDENTIFIER().getText();
        return ParameterDTO.create(TmpId.randomString(), name, name, getTypeId(parseType(parameter.typeType(), scope)));
    }

    private String getGenericDeclarationId(AsmGenericDeclaration genericDeclaration) {
        if (genericDeclaration instanceof ClassInfo k)
            return getTypeId(k.type);
        else if (genericDeclaration instanceof MethodInfo m)
            return getMethodId(m.method);
        else
            throw new InternalException("Invalid GenericDeclaration: " + genericDeclaration);
    }

    private <T> T getAttribute(ParserRuleContext ctx, AsmAttributeKey<T> key) {
        return key.cast(attributes.get(ctx).get(key));
    }

    private <T> void setAttribute(ParserRuleContext ctx, AsmAttributeKey<T> key, T value) {
        attributes.computeIfAbsent(ctx, k -> new HashMap<>()).put(key, value);
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

    private interface AsmScope {

        @Nullable
        AsmScope parent();

        List<String> getTypeParameterNames();

        List<AsmTypeVariable> getTypeParameters();

        default @Nullable AsmTypeVariable findTypeParameter(String name) {
            return NncUtils.find(getTypeParameters(), tv -> tv.name.equals(name));
        }

        default AsmTypeVariable getTypeParameter(String name) {
            return Objects.requireNonNull(findTypeParameter(name), () -> "Can not find type parameter '" + name + "' in " + this);
        }

        void addTypeParameter(AsmTypeVariable typeParameter);

        default <T extends AsmScope> T getAncestor(Class<T> klass) {
            var s = this;
            while (s != null && !klass.isInstance(s)) {
                s = s.parent();
            }
            return klass.cast(Objects.requireNonNull(s));
        }

    }

    private static final class ClassInfo implements AsmScope, AsmGenericDeclaration {
        @Nullable
        private final AsmScope parent;
        private final ClassAsmType type;
        private final List<String> typeParameterNames;
        private final List<AsmTypeVariable> typeParameters = new ArrayList<>();
        private final boolean isEnum;
        private @Nullable ClassAsmType superType;
        int enumConstantOrdinal;

        private ClassInfo(
                @Nullable AsmScope parent,
                ClassAsmType type,
                List<String> typeParameterNames,
                boolean isEnum
        ) {
            this.parent = parent;
            this.type = type;
            this.typeParameterNames = typeParameterNames;
            this.isEnum = isEnum;
        }

        public static ClassInfo fromContext(AssemblyParser.ClassDeclarationContext classDeclaration, @Nullable AsmScope parent) {
            var type = new ClassAsmType(classDeclaration.IDENTIFIER().getText(), List.of());
            var classInfo = new ClassInfo(
                    parent,
                    type,
                    classDeclaration.typeParameters() != null ?
                            NncUtils.map(classDeclaration.typeParameters().typeParameter(), tv -> tv.IDENTIFIER().getText())
                            : List.of(),
                    false
            );
            if (classDeclaration.EXTENDS() != null) {
                classInfo.superType = (ClassAsmType) parseType(classDeclaration.typeType(), classInfo);
            }
            return classInfo;
        }

        public static ClassInfo fromContext(AssemblyParser.InterfaceDeclarationContext interfaceDecl, @Nullable AsmScope parent) {
            var type = new ClassAsmType(interfaceDecl.IDENTIFIER().getText(), List.of());
            return new ClassInfo(
                    parent,
                    type,
                    interfaceDecl.typeParameters() != null ?
                            NncUtils.map(interfaceDecl.typeParameters().typeParameter(), tv -> tv.IDENTIFIER().getText())
                            : List.of(),
                    false
            );
        }

        public static ClassInfo fromContext(AssemblyParser.EnumDeclarationContext classDeclaration, @Nullable AsmScope parent) {
            var type = new ClassAsmType(classDeclaration.IDENTIFIER().getText(), List.of());
            return new ClassInfo(
                    parent,
                    type,
                    List.of(),
                    true
            );
        }

        public int nextEnumConstantOrdinal() {
            return enumConstantOrdinal++;
        }

        public String name() {
            return type.name();
        }

        public String rawName() {
            return type.rawName;
        }

        @Nullable
        @Override
        public AsmScope parent() {
            return parent;
        }

        public List<String> getTypeParameterNames() {
            return typeParameterNames;
        }

        @Override
        public List<AsmTypeVariable> getTypeParameters() {
            return typeParameters;
        }

        public void addTypeParameter(AsmTypeVariable typeParameter) {
            this.typeParameters.add(typeParameter);
        }

        @Override
        public String toString() {
            return "ClassInfo[" +
                    "parent=" + parent + ", " +
                    "type=" + type + ", " +
                    "typeParameters=" + typeParameterNames + ']';
        }

    }

    private static final class MethodInfo implements AsmScope, AsmGenericDeclaration {
        private final ClassInfo parent;
        private MethodKey method;
        private final String name;
        private final List<String> typeParameterNames;
        private final List<AsmTypeVariable> typeParameters = new ArrayList<>();

        private MethodInfo(ClassInfo parent, String name, List<String> typeParameterNames) {
            this.parent = parent;
            this.name = name;
            this.typeParameterNames = new ArrayList<>(typeParameterNames);
        }

        public void setMethod(MethodKey method) {
            this.method = method;
        }

        @Override
        @Nullable
        public ClassInfo parent() {
            return parent;
        }

        public MethodKey method() {
            return method;
        }

        @Override
        public List<String> getTypeParameterNames() {
            return typeParameterNames;
        }

        @Override
        public List<AsmTypeVariable> getTypeParameters() {
            return typeParameters;
        }

        @Override
        public void addTypeParameter(AsmTypeVariable typeParameter) {
            typeParameters.add(typeParameter);
        }

        @Override
        public String toString() {
            return "MethodInfo[" +
                    "class=" + parent.rawName() + ", " +
                    "name=" + name + ", " +
                    "typeParameters=" + NncUtils.join(typeParameters, tv -> tv.name) + ']';
        }


        @Override
        public String name() {
            return parent.rawName() + "." + name;
        }
    }

    private class VisitorBase extends AssemblyParserBaseVisitor<Void> {

        protected AsmScope scope;

        @Override
        public Void visitClassDeclaration(AssemblyParser.ClassDeclarationContext ctx) {
            scope = getAttribute(ctx, AsmAttributeKey.classInfo);
            super.visitClassDeclaration(ctx);
            scope = scope.parent();
            return null;
        }

        @Override
        public Void visitEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx) {
            scope = getAttribute(ctx, AsmAttributeKey.classInfo);
            super.visitEnumDeclaration(ctx);
            scope = scope.parent();
            return null;
        }

        @Override
        public Void visitInterfaceDeclaration(AssemblyParser.InterfaceDeclarationContext ctx) {
            scope = getAttribute(ctx, AsmAttributeKey.classInfo);
            super.visitInterfaceDeclaration(ctx);
            scope = scope.parent();
            return null;
        }

        @Override
        public Void visitMethodDeclaration(AssemblyParser.MethodDeclarationContext ctx) {
            visitFunction(ctx.IDENTIFIER().getText(),
                    ctx.typeParameters(),
                    ctx.formalParameters().formalParameterList(),
                    ctx.typeTypeOrVoid(),
                    ctx.methodBody().block(),
                    ctx,
                    false, () -> super.visitMethodDeclaration(ctx));
            return null;
        }

        @Override
        public Void visitConstructorDeclaration(AssemblyParser.ConstructorDeclarationContext ctx) {
            visitFunction(ctx.IDENTIFIER().getText(),
                    ctx.typeParameters(),
                    ctx.formalParameters().formalParameterList(),
                    null,
                    ctx.block(),
                    ctx,
                    true, () -> super.visitConstructorDeclaration(ctx));
            return null;
        }

        @Override
        public Void visitInterfaceMethodDeclaration(AssemblyParser.InterfaceMethodDeclarationContext ctx) {
            var commonDecl = ctx.interfaceCommonBodyDeclaration();
            visitFunction(commonDecl.IDENTIFIER().getText(),
                    ctx.typeParameters(),
                    commonDecl.formalParameters().formalParameterList(),
                    commonDecl.typeTypeOrVoid(),
                    null,
                    ctx,
                    false, () -> super.visitInterfaceMethodDeclaration(ctx));
            return null;
        }

        protected void visitFunction(String name,
                                     @Nullable AssemblyParser.TypeParametersContext typeParameters,
                                     @Nullable AssemblyParser.FormalParameterListContext formalParameterList,
                                     @Nullable AssemblyParser.TypeTypeOrVoidContext returnType,
                                     @Nullable AssemblyParser.BlockContext block,
                                     ParserRuleContext ctx, boolean isConstructor, Runnable processBody) {
            scope = getAttribute(ctx, AsmAttributeKey.methodInfo);
            processBody.run();
            scope = scope.parent();
        }

    }

    private class AstBuilder extends VisitorBase {

        @Override
        public Void visitClassDeclaration(AssemblyParser.ClassDeclarationContext ctx) {
            var classInfo = ClassInfo.fromContext(ctx, scope);
            setAttribute(ctx, AsmAttributeKey.classInfo, classInfo);
            super.visitClassDeclaration(ctx);
            if (ctx.typeType() != null)
                classInfo.superType = (ClassAsmType) parseType(ctx.typeType(), classInfo);
            return null;
        }

        @Override
        public Void visitEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx) {
            var classInfo = ClassInfo.fromContext(ctx, scope);
            classInfo.superType = new ClassAsmType("Enum", List.of(classInfo.type));
            setAttribute(ctx, AsmAttributeKey.classInfo, classInfo);
            super.visitEnumDeclaration(ctx);
            return null;
        }

        @Override
        public Void visitInterfaceDeclaration(AssemblyParser.InterfaceDeclarationContext ctx) {
            var classInfo = ClassInfo.fromContext(ctx, scope);
            setAttribute(ctx, AsmAttributeKey.classInfo, classInfo);
            return super.visitInterfaceDeclaration(ctx);
        }

        @Override
        protected void visitFunction(String name,
                                     @Nullable AssemblyParser.TypeParametersContext typeParameters,
                                     @Nullable AssemblyParser.FormalParameterListContext formalParameterList,
                                     @Nullable AssemblyParser.TypeTypeOrVoidContext returnType,
                                     @Nullable AssemblyParser.BlockContext block,
                                     ParserRuleContext ctx,
                                     boolean isConstructor,
                                     Runnable processBody) {
            var classInfo = (ClassInfo) scope;
            var methodInfo = new MethodInfo(classInfo, name, parseTypeParameters(typeParameters));
            setAttribute(ctx, AsmAttributeKey.methodInfo, methodInfo);
            super.visitFunction(name, typeParameters, formalParameterList, returnType, block, ctx, isConstructor, processBody);
            List<AsmType> paramTypes = formalParameterList != null ?
                    NncUtils.map(formalParameterList.formalParameter(), p -> parseType(p.typeType(), methodInfo, true)) : List.of();
            var methodKey = new MethodKey(classInfo.rawName(), name, paramTypes, methodInfo.getTypeParameters().size());
            logger.info("method key: {}", methodKey);
            methodInfo.setMethod(methodKey);
        }

        @Override
        public Void visitTypeParameter(AssemblyParser.TypeParameterContext ctx) {
            var genericDecl = (AsmGenericDeclaration) scope;
            var type = new AsmTypeVariable(genericDecl, ctx.IDENTIFIER().getText(), scope.getTypeParameters().size());
            setAttribute(ctx, AsmAttributeKey.typeVariable, type);
            scope.addTypeParameter(type);
            return super.visitTypeParameter(ctx);
        }
    }

    private class Preprocessor extends VisitorBase {

        @Override
        public Void visitClassDeclaration(AssemblyParser.ClassDeclarationContext ctx) {
            var classInfo = getAttribute(ctx, AsmAttributeKey.classInfo);
            typeIds.put(classInfo.type, TmpId.randomString());
            var supers = new ArrayList<ClassAsmType>();
            superTypes.put(classInfo.rawName(), supers);
            if (ctx.EXTENDS() != null)
                supers.add((ClassAsmType) parseType(ctx.typeType(), scope));
            if (ctx.IMPLEMENTS() != null)
                ctx.typeList().typeType().forEach(t -> supers.add((ClassAsmType) parseType(t, scope)));
            super.visitClassDeclaration(ctx);
            return null;
        }

        @Override
        public Void visitEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx) {
            var classInfo = getAttribute(ctx, AsmAttributeKey.classInfo);
            var supers = addSupers(classInfo.rawName());
            var pEnumType = new ClassAsmType("Enum", List.of(classInfo.type));
            supers.add(pEnumType);
            if (ctx.IMPLEMENTS() != null)
                forEachClass(ctx.typeList(), supers::add);
            typeIds.put(classInfo.type, TmpId.randomString());
            typeIds.put(pEnumType, TmpId.randomString());
            super.visitEnumDeclaration(ctx);
            return null;
        }

        @Override
        public Void visitInterfaceDeclaration(AssemblyParser.InterfaceDeclarationContext ctx) {
            var classInfo = getAttribute(ctx, AsmAttributeKey.classInfo);
            var supers = addSupers(classInfo.rawName());
            if (ctx.EXTENDS() != null)
                forEachClass(ctx.typeList(), supers::add);
            typeIds.put(classInfo.type, TmpId.randomString());
            super.visitInterfaceDeclaration(ctx);
            return null;
        }

        private List<ClassAsmType> addSupers(String name) {
            var supers = new ArrayList<ClassAsmType>();
            superTypes.put(name, supers);
            return supers;
        }

        private void forEachClass(AssemblyParser.TypeListContext typeList, Consumer<ClassAsmType> action) {
            typeList.typeType().forEach(t -> action.accept((ClassAsmType) parseType(t, scope)));
        }

        @Override
        public Void visitFieldDeclaration(AssemblyParser.FieldDeclarationContext ctx) {
            var name = ctx.IDENTIFIER().getText();
            fieldIds.put(new FieldKey(currentClass().name(), name), TmpId.randomString());
            return super.visitFieldDeclaration(ctx);
        }

        @Override
        public Void visitTypeParameter(AssemblyParser.TypeParameterContext ctx) {
            var typeVariable = scope.getTypeParameter(ctx.IDENTIFIER().getText());
            typeIds.put(typeVariable, TmpId.randomString());
            return super.visitTypeParameter(ctx);
        }

        @Override
        public Void visitTypeType(AssemblyParser.TypeTypeContext ctx) {
            if (ctx.primitiveType() == null && ctx.classOrInterfaceType() == null) {
                var type = parseType(ctx, scope);
                if (!typeIds.containsKey(type))
                    typeIds.put(type, TmpId.randomString());
            }
            return super.visitTypeType(ctx);
        }

        @Override
        public Void visitClassOrInterfaceType(AssemblyParser.ClassOrInterfaceTypeContext ctx) {
            var type = parseClassType(ctx, scope);
            if (type instanceof ClassAsmType classAsmType && classAsmType.isParameterized() && !typeIds.containsKey(type)) {
                logger.info("generating id for parameterized type: {}", ctx.getText());
                typeIds.put(type, TmpId.randomString());
            }
            return super.visitClassOrInterfaceType(ctx);
        }

        @Override
        protected void visitFunction(String name, @Nullable AssemblyParser.TypeParametersContext typeParameters, AssemblyParser.FormalParameterListContext formalParameterList, AssemblyParser.TypeTypeOrVoidContext returnType, AssemblyParser.BlockContext block, ParserRuleContext ctx, boolean isConstructor, Runnable processBody) {
            methodIds.put(getAttribute(ctx, AsmAttributeKey.methodInfo).method, TmpId.randomString());
            super.visitFunction(name, typeParameters, formalParameterList, returnType, block, ctx, isConstructor, processBody);
        }

        private ClassInfo currentClass() {
            return scope.getAncestor(ClassInfo.class);
        }

    }

    private class Emitter extends VisitorBase {

        private final LinkedList<ClassTypeDTOBuilder> builders = new LinkedList<>();
        private final LinkedList<Set<String>> modsStack = new LinkedList<>();
        //        private ClassInfo currentClass;
        private final LinkedList<MethodDTOBuilder> staticBuilders = new LinkedList<>();
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
            processClass(name, TypeCategory.CLASS, ctx.STRUCT() != null, ctx.typeParameters(),
                    ctx.EXTENDS() != null ? ctx.typeType() : null,
                    ctx.typeList(),
                    ctx,
                    () -> super.visitClassDeclaration(ctx));
            return null;
        }

        @Override
        public Void visitEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx) {
            var name = ctx.IDENTIFIER().getText();
            processClass(name, TypeCategory.ENUM, false, null, null, ctx.typeList(), ctx, () -> super.visitEnumDeclaration(ctx));
            return null;
        }

        @Override
        public Void visitInterfaceDeclaration(AssemblyParser.InterfaceDeclarationContext ctx) {
            var name = ctx.IDENTIFIER().getText();
            processClass(name, TypeCategory.INTERFACE, false, ctx.typeParameters(), null, ctx.typeList(), ctx
                    , () -> super.visitInterfaceDeclaration(ctx));
            return null;
        }

        private void processClass(String name,
                                  TypeCategory typeCategory,
                                  boolean isStruct,
                                  @Nullable AssemblyParser.TypeParametersContext typeParameters,
                                  @Nullable AssemblyParser.TypeTypeContext superType,
                                  @Nullable AssemblyParser.TypeListContext interfaces,
                                  ParserRuleContext ctx, Runnable processBody
        ) {
            var currentClass = getAttribute(ctx, AsmAttributeKey.classInfo);
            var builder = ClassTypeDTOBuilder.newBuilder(name)
                    .code(name)
                    .struct(isStruct)
                    .id(getTypeId(new ClassAsmType(name, List.of())))
                    .typeParameterIds(
                            typeParameters != null ?
                                    NncUtils.map(typeParameters.typeParameter(),
                                            tp -> getTypeId(currentClass.getTypeParameter(tp.IDENTIFIER().getText()))) :
                                    List.of()
                    );
            builders.push(builder);
            scope = currentClass;
            builder.typeCategory(typeCategory.code());
            if (typeCategory.isEnum())
                currentClass.superType = new ClassAsmType("Enum", List.of(currentClass.type));
            else if (superType != null)
                currentClass.superType = (ClassAsmType) parseType(superType, currentClass);
            if (currentClass.superType != null)
                builder.superClassId(getTypeId(currentClass.superType));
            if (interfaces != null)
                builder.interfaceIds(NncUtils.map(interfaces.typeType(), t -> getTypeId(parseType(t, scope))));
            var staticBuilder = MethodDTOBuilder.newBuilder(getTypeId(currentClass.type), "类型初始化")
                    .isStatic(true)
                    .id(TmpId.randomString())
                    .code("<cinit>")
                    .access(Access.PRIVATE.code())
                    .returnTypeId(getTypeId(new PrimitiveAsmType(AsmPrimitiveKind.VOID)));
            staticBuilders.push(staticBuilder);
            processBody.run();
            staticBuilder.addNode(NodeDTOFactory.createReturnNode(NncUtils.randomNonNegative(), "return", null));
            builder.addMethod(staticBuilder.build());
            scope = currentClass.parent;
            types.add(builder.build());
            staticBuilders.pop();
            builders.pop();
        }

        private MethodDTOBuilder staticBuilder() {
            return requireNonNull(staticBuilders.peek());
        }

        private ClassInfo currentClass() {
            var s = scope;
            while (s != null && !(s instanceof ClassInfo)) {
                s = s.parent();
            }
            return Objects.requireNonNull((ClassInfo) s, "Not in any class scope");
        }

        @Override
        public Void visitEnumConstant(AssemblyParser.EnumConstantContext ctx) {
            var classBuilder = builder();
            var staticBuilder = staticBuilder();
            var name = ctx.IDENTIFIER().getText();
            var typeId = getTypeId(currentClass().type);
            classBuilder.addStaticField(
                    FieldDTOBuilder.newBuilder(name, typeId)
                            .id(TmpId.randomString())
                            .code(name)
                            .isStatic(true)
                            .build()
            );
            var args = new ArrayList<ValueDTO>();
            args.add(ValueDTOFactory.createConstant(name));
            args.add(ValueDTOFactory.createConstant(currentClass().nextEnumConstantOrdinal()));
            if (ctx.arguments() != null && ctx.arguments().expressionList() != null)
                NncUtils.forEach(ctx.arguments().expressionList().expression(), e -> args.add(parseValue(e)));
            staticBuilder.addNode(NodeDTOFactory.createUnresolvedNewObjectNode(
                    NncUtils.randomNonNegative(),
                    "value" + name,
                    typeId,
                    currentClass().name(),
                    List.of(),
                    args,
                    false,
                    false
            ));
            staticBuilder.addNode(NodeDTOFactory.createUpdateStaticNode(
                    NncUtils.randomNonNegative(),
                    "update" + name,
                    typeId,
                    List.of(new UpdateFieldDTO(null, name, UpdateOp.SET.code(), ValueDTOFactory.createReference("value" + name)))
            ));
            return super.visitEnumConstant(ctx);
        }

        @Override
        public Void visitTypeParameter(AssemblyParser.TypeParameterContext ctx) {
            var name = ctx.IDENTIFIER().getText();
            var typeVariable = scope.getTypeParameter(name);
            var boundId = ctx.typeType() != null ? getTypeId(parseType(ctx.typeType(), scope)) : null;
            types.add(new TypeDTO(
                    getTypeId(typeVariable),
                    name,
                    name,
                    TypeCategory.VARIABLE.code(),
                    false,
                    false,
                    new TypeVariableParam(
                            getGenericDeclarationId(typeVariable.owner),
                            scope.getTypeParameterNames().indexOf(name),
                            boundId != null ? List.of(boundId) : List.of()
                    )
            ));
            return null;
        }

        @Override
        public Void visitFieldDeclaration(AssemblyParser.FieldDeclarationContext ctx) {
            var classBuilder = builder();
            var typeId = getTypeId(parseType(ctx.typeType(), scope));
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
        public Void visitInterfaceMethodDeclaration(AssemblyParser.InterfaceMethodDeclarationContext ctx) {
            modsStack.push(NncUtils.mapUnique(ctx.interfaceMethodModifier(), RuleContext::getText));
            super.visitInterfaceMethodDeclaration(ctx);
            modsStack.pop();
            return null;
        }

        @Override
        public Void visitClassBodyDeclaration(AssemblyParser.ClassBodyDeclarationContext ctx) {
            modsStack.push(NncUtils.mapUnique(ctx.modifier(), RuleContext::getText));
            super.visitClassBodyDeclaration(ctx);
            modsStack.pop();
            return null;
        }

        @Override
        protected void visitFunction(String name, @Nullable AssemblyParser.TypeParametersContext typeParameters, @Nullable AssemblyParser.FormalParameterListContext parameterList, @Nullable AssemblyParser.TypeTypeOrVoidContext returnType, @Nullable AssemblyParser.BlockContext block, ParserRuleContext ctx, boolean isConstructor, Runnable processBody) {
            var classBuilder = builder();
            var methodKey = getAttribute(ctx, AsmAttributeKey.methodInfo).method;
            scope = getAttribute(ctx, AsmAttributeKey.methodInfo);
            try {
                var mods = currentMods();
                var methodBuilder = MethodDTOBuilder.newBuilder(classBuilder.getId(), name)
                        .code(name)
                        .isAbstract(block == null)
                        .id(getMethodId(methodKey))
                        .access(getAccess(mods).code());
                if (typeParameters != null) {
                    methodBuilder.typeParameterIds(
                            NncUtils.map(typeParameters.typeParameter(), tv -> getTypeId(scope.getTypeParameter(tv.IDENTIFIER().getText())))
                    );
                }
                if (!isConstructor) {
                    var overriddenIds = new ArrayList<String>();
                    var supers = new LinkedList<>(superTypes.get(currentClass().type.rawName));
                    while (!supers.isEmpty()) {
                        var s = supers.poll();
                        var overriddenId = methodIds.get(new MethodKey(s.rawName, name, methodKey.parameterTypes, methodKey.numTypeParameters));
                        if (overriddenId != null)
                            overriddenIds.add(overriddenId);
                        else
                            supers.addAll(superTypes.getOrDefault(s.rawName, List.of()));
                    }
                    methodBuilder.overriddenIds(overriddenIds);
                }
                var currentClass = currentClass();
                if (isConstructor && currentClass.isEnum) {
                    methodBuilder.addParameter(ParameterDTO.create(
                            TmpId.randomString(),
                            "_name",
                            "_name",
                            getTypeId(new PrimitiveAsmType(AsmPrimitiveKind.STRING))
                    ));
                    methodBuilder.addParameter(ParameterDTO.create(
                            TmpId.randomString(),
                            "_ordinal",
                            "_ordinal",
                            getTypeId(new PrimitiveAsmType(AsmPrimitiveKind.LONG))
                    ));
                }
                parseParameterList(parameterList, scope).forEach(methodBuilder::addParameter);
                if (isConstructor) {
                    methodBuilder.isConstructor(true);
                    methodBuilder.returnTypeId(classBuilder.getId());
                } else
                    methodBuilder.returnTypeId(getTypeId(parseType(requireNonNull(returnType), scope)));
                var isStatic = mods.contains(Modifiers.STATIC);
                if (isStatic)
                    methodBuilder.isStatic(true);
                if (block != null) {
                    if (!isStatic)
                        methodBuilder.addNode(NodeDTOFactory.createSelfNode(NncUtils.randomNonNegative(), "this", classBuilder.getId()));
                    methodBuilder.autoCreateInputNode(NncUtils.randomNonNegative(), "_input");
                    for (var parameter : methodBuilder.getParameters()) {
                        methodBuilder.addNode(NodeDTOFactory.createValueNode(
                                NncUtils.randomNonNegative(),
                                parameter.name(),
                                ValueDTOFactory.createReference("_input." + parameter.name())
                        ));
                    }
                    if (isConstructor && currentClass.isEnum) {
                        methodBuilder.addNode(NodeDTOFactory.createUpdateObjectNode(
                                NncUtils.randomNonNegative(),
                                nextNodeName(),
                                ValueDTOFactory.createReference("this"),
                                List.of(
                                        new UpdateFieldDTO(null, "名称", UpdateOp.SET.code(), ValueDTOFactory.createReference("_input._name")),
                                        new UpdateFieldDTO(null, "序号", UpdateOp.SET.code(), ValueDTOFactory.createReference("_input._ordinal"))
                                )
                        ));
                    }
                    processMethodBlock(block, methodBuilder);
                    if (isConstructor) {
                        methodBuilder.addNode(NodeDTOFactory.createReturnNode(
                                NncUtils.randomNonNegative(),
                                nextNodeName(),
                                ValueDTOFactory.createReference("this")
                        ));
                    } else if (returnType.VOID() != null) {
                        methodBuilder.addNode(NodeDTOFactory.createReturnNode(
                                NncUtils.randomNonNegative(),
                                nextNodeName(),
                                null
                        ));
                    }
                }
                classBuilder.addMethod(methodBuilder.build());
                if (typeParameters != null)
                    typeParameters.accept(this);
            } finally {
                scope = scope.parent();
            }
        }

        private Set<String> currentMods() {
            return requireNonNull(modsStack.peek());
        }

        private void processMethodBlock(AssemblyParser.BlockContext block, MethodDTOBuilder methodBuilder) {
            for (var stmt : block.labeledStatement()) {
                methodBuilder.addNode(processLabeledStatement(stmt));
            }
        }

        private String nextNodeName() {
            return nextNodeName("__node__");
        }

        private String nextNodeName(String prefix) {
            return prefix + nextNodeNum++;
        }

        private List<NodeDTO> parseBlockNodes(AssemblyParser.BlockContext block) {
            return NncUtils.map(block.labeledStatement(), this::processLabeledStatement);
        }

        private NodeDTO processLabeledStatement(AssemblyParser.LabeledStatementContext labeledStatement) {
            var name = labeledStatement.IDENTIFIER() != null ? labeledStatement.IDENTIFIER().getText() :
                    nextNodeName();
            return processStatement(name, labeledStatement.statement());
        }

        private NodeDTO processStatement(String name, AssemblyParser.StatementContext statement) {
            try {
                var currentClass = currentClass();
                /*if (statement.statementExpression != null) {
                    return NodeDTOFactory.createValueNode(
                            NncUtils.randomNonNegative(),
                            name,
                            ValueDTOFactory.createExpression(parseExpression(statement.statementExpression))
                    );
                }*/
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
                if (statement.NEW() != null || statement.UNEW() != null || statement.ENEW() != null) {
                    var creator = statement.creator();
                    var type = (ClassAsmType) parseClassType(creator.classOrInterfaceType(), scope);
                    var methodName = type.rawName;
                    List<AssemblyParser.ExpressionContext> arguments =
                            NncUtils.getOrElse(
                                    creator.arguments().expressionList(),
                                    AssemblyParser.ExpressionListContext::expression,
                                    List.of()
                            );
                    return NodeDTOFactory.createUnresolvedNewObjectNode(
                            NncUtils.randomNonNegative(),
                            name,
                            getTypeId(type),
                            methodName,
                            creator.typeArguments() != null ?
                                    NncUtils.map(creator.typeArguments().typeType(), t -> getTypeId(parseType(t, scope))) : List.of(),
                            NncUtils.map(arguments, arg -> ValueDTOFactory.createExpression(parseExpression(arg))),
                            statement.UNEW() != null,
                            statement.ENEW() != null
                    );
                }
                if (statement.methodCall() != null) {
                    var methodCall = statement.methodCall();
                    List<ValueDTO> arguments = methodCall.expressionList() != null ?
                            NncUtils.map(methodCall.expressionList().expression(), this::parseValue) : List.of();
                    ValueDTO self;
                    String methodName;
                    String typeId = null;
                    if (methodCall.IDENTIFIER() != null) {
                        methodName = methodCall.IDENTIFIER().getText();
                        var expr = parseExpression(methodCall.expression());
                        if (CLASS_NAME_PTN.matcher(expr).matches()) {
                            typeId = getTypeId(ClassAsmType.create(expr));
                            self = null;
                            logger.info("Detecting static method call: " + methodCall.getText());
                        } else
                            self = ValueDTOFactory.createExpression(expr);
                    } else if (methodCall.SUPER() != null) {
                        methodName = requireNonNull(currentClass.superType).rawName;
                        self = ValueDTOFactory.createReference("this");
                    } else if (methodCall.THIS() != null) {
                        methodName = currentClass.type.rawName;
                        self = ValueDTOFactory.createReference("this");
                    } else
                        throw new InternalException("methodCall syntax error: " + methodCall.getText());
                    return NodeDTOFactory.createUnresolvedMethodCallNode(
                            NncUtils.randomNonNegative(),
                            name,
                            methodName,
                            methodCall.typeArguments() != null ?
                                    NncUtils.map(methodCall.typeArguments().typeType(), t -> getTypeId(parseType(t, scope))) : List.of(),
                            typeId,
                            self,
                            arguments
                    );
                }
                if(statement.functionCall() != null) {
                    var funcCall = statement.functionCall();
                    return NodeDTOFactory.createFunction(
                            NncUtils.randomNonNegative(),
                            name,
                            parseValue(funcCall.expression()),
                            parseValueList(funcCall.expressionList())
                    );
                }
                if (statement.THROW() != null) {
                    return NodeDTOFactory.createRaiseNodeWithException(
                            NncUtils.randomNonNegative(),
                            name,
                            parseValue(statement.expression())
                    );
                }
                if (statement.IF() != null) {
                    return NodeDTOFactory.createBranchNode(
                            NncUtils.randomNonNegative(),
                            name,
                            List.of(
                                    NodeDTOFactory.createBranch(
                                            NncUtils.randomNonNegative(),
                                            0,
                                            parseValue(statement.parExpression().expression()),
                                            false,
                                            parseBlockNodes(statement.block(0))
                                    ),
                                    NodeDTOFactory.createBranch(
                                            NncUtils.randomNonNegative(),
                                            1,
                                            ValueDTOFactory.createConstant(true),
                                            true,
                                            statement.ELSE() != null ?
                                                    parseBlockNodes(statement.block(1)) : List.of()
                                    )
                            )
                    );
                }
                if (statement.FOR() != null) {
                    var fieldTypes = new HashMap<String, AsmType>();
                    var initialValues = new HashMap<String, ValueDTO>();
                    var updatedValues = new HashMap<String, ValueDTO>();
                    var forCtl = statement.forControl();
                    var loopVarDecls = forCtl.loopVariableDeclarators();
                    if (loopVarDecls != null) {
                        for (var decl : loopVarDecls.loopVariableDeclarator()) {
                            var fieldName = decl.IDENTIFIER().getText();
                            fieldTypes.put(fieldName, parseType(decl.typeType(), currentClass));
                            initialValues.put(fieldName, parseValue(decl.expression()));
                        }
                        for (var update : forCtl.loopVariableUpdates().loopVariableUpdate()) {
                            updatedValues.put(update.IDENTIFIER().getText(), parseValue(update.expression()));
                        }
                    }
                    var fields = NncUtils.map(fieldTypes.keySet(), fieldName -> new LoopFieldDTO(
                            TmpId.randomString(),
                            fieldName,
                            getTypeId(fieldTypes.get(fieldName)),
                            requireNonNull(initialValues.get(fieldName)),
                            requireNonNull(updatedValues.get(fieldName))
                    ));
                    if (DebugEnv.debugging) {
                        DebugEnv.logger.info("loopFields: {}", NncUtils.toJSONString(fields));
                        DebugEnv.logger.info("loopCond: {}", NncUtils.toJSONString(parseValue(forCtl.expression())));
                    }
                    return NodeDTOFactory.createWhileNode(
                            NncUtils.randomNonNegative(),
                            name,
                            parseValue(forCtl.expression()),
                            parseBlockNodes(statement.block(0)),
                            fields
                    );
                }
                if (statement.lambda() != null) {
                    var lambda = statement.lambda();
                    var params = parseParameterList(lambda.lambdaParameters().formalParameterList(), scope);
                    var nodes = new ArrayList<NodeDTO>();
                    var inputName = nextNodeName("__input__");
                    nodes.add(NodeDTOFactory.createInputNode(
                            NncUtils.randomNonNegative(),
                            inputName,
                            NncUtils.map(params, p -> InputFieldDTO.create(p.name(), p.typeId()))
                    ));
                    params.forEach(p -> nodes.add(NodeDTOFactory.createValueNode(
                            NncUtils.randomNonNegative(),
                            p.name(),
                            ValueDTOFactory.createReference(inputName + "." + p.name())
                    )));
                    nodes.addAll(parseBlockNodes(lambda.lambdaBody().block()));
                    if(lambda.typeTypeOrVoid().VOID() != null)
                        nodes.add(NodeDTOFactory.createReturnNode(NncUtils.randomNonNegative(), nextNodeName(), null));
                    return NodeDTOFactory.createLambda(
                            NncUtils.randomNonNegative(),
                            name,
                            params,
                            getTypeId(parseType(lambda.typeTypeOrVoid(), scope)),
                            nodes
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
            return ValueDTOFactory.createExpression(parseExpression(expression));
        }

        private List<ValueDTO> parseValueList(@Nullable AssemblyParser.ExpressionListContext expressionList) {
            if(expressionList == null)
                return List.of();
            return NncUtils.map(expressionList.expression(), this::parseValue);
        }

        private String parseExpression(AssemblyParser.ExpressionContext expression) {
            return expression.getText().replace("==", "=")
                    .replace("&&", "and")
                    .replace("||", "or");
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
            return requireNonNull(builders.peek());
        }

    }

    private class CompositeTypeEmitter extends VisitorBase {

        @Override
        public Void visitEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx) {
            var type = ClassAsmType.create(ctx.IDENTIFIER().getText());
            var pEnumType = new ClassAsmType("Enum", List.of(type));
            var pEnumTypeId = getTypeId(pEnumType);
            types.add(new TypeDTO(
                    pEnumTypeId,
                    pEnumType.name(),
                    null,
                    TypeCategory.CLASS.code(),
                    false,
                    false,
                    new PTypeDTO(
                            pEnumTypeId,
                            getTypeId(ClassAsmType.create("Enum")),
                            List.of(getTypeId(type)),
                            List.of(),
                            List.of(),
                            List.of(),
                            List.of()
                    )
            ));
            return super.visitEnumDeclaration(ctx);
        }

        @Override
        public Void visitTypeType(AssemblyParser.TypeTypeContext ctx) {
            if (ctx.classOrInterfaceType() == null) {
                var type = parseType(ctx, scope);
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
                        case FunctionAsmType functionAsmType -> new TypeDTO(
                                id,
                                type.name(),
                                null,
                                TypeCategoryCodes.FUNCTION,
                                false,
                                false,
                                new FunctionTypeParam(
                                        NncUtils.map(functionAsmType.parameterTypes, Assembler.this::getTypeId),
                                        getTypeId(functionAsmType.returnType)
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
            var type = parseClassType(ctx, scope);
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
                                        getTypeId(new ClassAsmType(classAsmType.rawName, List.of())),
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

    private AsmType parseType(AssemblyParser.TypeTypeOrVoidContext typeTypeOrVoid, AsmScope scope) {
        if (typeTypeOrVoid.VOID() != null)
            return new PrimitiveAsmType(AsmPrimitiveKind.VOID);
        return parseType(typeTypeOrVoid.typeType(), scope);
    }

    private static AsmType parseType(AssemblyParser.TypeTypeContext typeType, AsmScope scope) {
        return parseType(typeType, scope, false);
    }

    private static AsmType parseType(AssemblyParser.TypeTypeContext typeType, AsmScope scope, boolean forMethodSignature) {
        if (typeType.ANY() != null)
            return AnyAsmType.INSTANCE;
        if (typeType.NEVER() != null)
            return NeverAsmType.INSTANCE;
        if (typeType.primitiveType() != null)
            return parsePrimitiveType(typeType.primitiveType());
        if (typeType.classOrInterfaceType() != null) {
            return parseClassType(typeType.classOrInterfaceType(), scope, forMethodSignature);
        }
        if (typeType.arrayKind() != null) {
            var arrayKind = typeType.arrayKind();
            var elementType = parseType(typeType.typeType(0), scope);
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
                    typeType1 -> parseType(typeType1, scope)
            );
            return new UnionAsmType(new HashSet<>(members));
        }
        if (!typeType.BITAND().isEmpty()) {
            var types = NncUtils.map(
                    typeType.typeType(),
                    typeType1 -> parseType(typeType1, scope)
            );
            return new IntersectionAsmType(new HashSet<>(types));
        }
        if (typeType.ARROW() != null) {
            int numParams = typeType.typeType().size() - 1;
            var parameterTypes = NncUtils.map(
                    typeType.typeType().subList(0, numParams),
                    typeType1 -> parseType(typeType1, scope)
            );
            var returnType = parseType(typeType.typeType(numParams), scope);
            return new FunctionAsmType(parameterTypes, returnType);
        }
        if (typeType.LBRACK() != null) {
            var lowerBound = parseType(typeType.typeType(0), scope);
            var upperBound = parseType(typeType.typeType(1), scope);
            return new UncertainAsmType(lowerBound, upperBound);
        }
        throw new InternalException("Unknown type: " + typeType.getText());
    }

    private static PrimitiveAsmType parsePrimitiveType(AssemblyParser.PrimitiveTypeContext primitiveType) {
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

    private static AsmType parseClassType(AssemblyParser.ClassOrInterfaceTypeContext classOrInterfaceType, @Nullable AsmScope scope) {
        return parseClassType(classOrInterfaceType, scope, false);
    }

    private static AsmType parseClassType(AssemblyParser.ClassOrInterfaceTypeContext classOrInterfaceType, @Nullable AsmScope scope, boolean forMethodSignature) {
        var name = classOrInterfaceType.qualifiedName().getText();
        if (!name.contains(".")) {
            var k = scope;
            while (k != null) {
                var found = k.findTypeParameter(name);
                if (found != null) {
                    if (forMethodSignature && k == scope)
                        return new SelfTypeParameter(found.index());
                    else
                        return found;
                }
                k = k.parent();
            }
        }
        List<AsmType> typeArguments = classOrInterfaceType.typeArguments() != null ? NncUtils.map(
                classOrInterfaceType.typeArguments().typeType(),
                typeType1 -> parseType(typeType1, scope)
        ) : List.of();
        return new ClassAsmType(name, typeArguments);
    }

    public interface AsmType {

        String name();

    }

    public record ClassAsmType(String rawName, List<AsmType> typeArguments) implements AsmType {

        public static ClassAsmType create(String name) {
            return new ClassAsmType(name, List.of());
        }

        @Override
        public String name() {
            return typeArguments().isEmpty() ? rawName : rawName + "<" + NncUtils.join(typeArguments, AsmType::name, ",") + ">";
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

    public record AsmTypeVariable(@NotNull AsmGenericDeclaration owner, @NotNull String name,
                                  int index) implements AsmType {

        @Override
        public String name() {
            return owner.name() + "." + name;
        }
    }

    public record SelfTypeParameter(int index) implements AsmType {

        @Override
        public String name() {
            return "T#" + index;
        }
    }

    public record AnyAsmType() implements AsmType {

        public static final AnyAsmType INSTANCE = new AnyAsmType();

        @Override
        public String name() {
            return "any";
        }
    }

    public record NeverAsmType() implements AsmType {

        public static final NeverAsmType INSTANCE = new NeverAsmType();

        @Override
        public String name() {
            return "never";
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

    private record MethodKey(String typeName, String methodName, List<AsmType> parameterTypes, int numTypeParameters) {

        public String name() {
            return typeName + "." + (numTypeParameters > 0 ? "<" + numTypeParameters + ">" : "")
                    + methodName + "(" + NncUtils.join(parameterTypes, AsmType::name) + ")";
        }

    }

    private static class AsmAttributeKey<T> {

        public static final AsmAttributeKey<AsmTypeVariable> typeVariable = new AsmAttributeKey<>(AsmTypeVariable.class);

        public static final AsmAttributeKey<ClassInfo> classInfo = new AsmAttributeKey<>(ClassInfo.class);

        public static final AsmAttributeKey<MethodInfo> methodInfo = new AsmAttributeKey<>(MethodInfo.class);

        private final Class<T> klass;

        private AsmAttributeKey(Class<T> klass) {
            this.klass = klass;
        }

        T cast(Object value) {
            return klass.cast(value);
        }
    }

    public interface AsmGenericDeclaration {

        String name();

    }

    public List<TypeDTO> getAllTypes() {
        var allTypes = new ArrayList<>(types);
        allTypes.addAll(compositeTypes.values());
        return allTypes;
    }

    public List<ParameterizedFlowDTO> getParameterizedFlows() {
        return List.of();
    }

    public void logIds() {
        logger.info("Type IDs: {}", typeIds);
        logger.info("Field IDs: {}", fieldIds);
        logger.info("Method IDs: {}", methodIds);
    }

}
