package org.metavm.asm;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.metavm.asm.antlr.AssemblyLexer;
import org.metavm.asm.antlr.AssemblyParser;
import org.metavm.asm.antlr.AssemblyParserBaseVisitor;
import org.metavm.entity.Entity;
import org.metavm.entity.GenericDeclaration;
import org.metavm.entity.SerializeContext;
import org.metavm.entity.StdKlass;
import org.metavm.expression.*;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.object.type.rest.dto.TypeDefDTO;
import org.metavm.object.type.rest.dto.TypeVariableDTO;
import org.metavm.util.DebugEnv;
import org.metavm.util.InternalException;
import org.metavm.util.LinkedList;
import org.metavm.util.NncUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

public class Assembler {

    public static final Logger logger = LoggerFactory.getLogger(Assembler.class);

    private final char[] buf = new char[1024 * 1024];

    private final Map<String, List<ClassType>> superTypes = new HashMap<>();
    private final List<KlassDTO> types = new ArrayList<>();
    private final Map<String, Klass> code2klass = new HashMap<>();
    private final List<TypeVariableDTO> typeVariables = new ArrayList<>();
    private final Map<ParserRuleContext, Map<AsmAttributeKey<?>, Object>> attributes = new HashMap<>();
    private final Function<String, Klass> klassProvider;
    private final Consumer<Entity> binder;

    public Assembler(Function<String, Klass> klassProvider, Consumer<Entity> binder) {
        for (StdKlass stdKlass : StdKlass.values()) {
            var klass = stdKlass.get();
            code2klass.put(klass.getCodeNotNull(), klass);
        }
        this.klassProvider = klassProvider;
        this.binder = binder;
    }

    @SuppressWarnings("UnusedReturnValue")
    public List<TypeDefDTO> assemble(List<String> sourcePaths) {
        var units = NncUtils.map(sourcePaths, path -> parse(getSource(path)));
        visit(units, new ImportParser());
        visit(units, new AsmInit());
        visit(units, new AsmDeclarator());
        visit(units, new IndexDefiner());
        visit(units, new Preprocessor());
        visit(units, new AsmGenerator());
        visit(units, new KlassInitializer());
        return getAllTypeDefs();
    }

    private void visit(List<AssemblyParser.CompilationUnitContext> units, AssemblyParserBaseVisitor<Void> visitor) {
        units.forEach(unit -> unit.accept(visitor));
    }

    private List<Parameter> parseParameterList(@Nullable AssemblyParser.FormalParameterListContext parameterList,
                                               @Nullable Callable callable, AsmScope scope, CompilationUit compilationUnit) {
        if (parameterList == null)
            return List.of();
        return NncUtils.map(parameterList.formalParameter(), p -> parseParameter(p, callable, scope, compilationUnit));
    }

    private Parameter parseParameter(AssemblyParser.FormalParameterContext parameter, @Nullable Callable callable, AsmScope scope, CompilationUit compilationUnit) {
        var name = parameter.IDENTIFIER().getText();
        var type = parseType(parameter.typeType(), scope, compilationUnit);
        if (callable != null) {
            var existing = callable.findParameter(p -> p.getName().equals(name));
            if (existing != null) {
                existing.setType(type);
                return existing;
            }
        }
        return new Parameter(
                NncUtils.randomNonNegative(),
                name,
                name,
                type
        );
    }

    private Expression parseExpression(String expression, ParsingContext parsingContext) {
        var replaced = expression.replace("==", "=")
                .replace("&&", "and")
                .replace("||", "or");
        return ExpressionParser.parse(replaced, parsingContext);
    }

    private <T> T getAttribute(ParserRuleContext ctx, AsmAttributeKey<T> key) {
        return key.cast(Objects.requireNonNull(attributes.get(ctx).get(key), "Can not find attribute " + key.name + " in " + ctx.getText()));
    }

    private <T> void setAttribute(ParserRuleContext ctx, AsmAttributeKey<T> key, T value) {
        attributes.computeIfAbsent(ctx, k -> new HashMap<>()).put(key, value);
    }

    private String getInternalName(AssemblyParser.TypeTypeContext typeType, Set<String> typeParameters, AsmScope scope) {
        if (typeType.primitiveType() != null)
            return parsePrimitiveType(typeType.primitiveType()).getInternalName(null);
        else if (typeType.ANY() != null)
            return Types.getAnyType().getInternalName(null);
        else if (typeType.NEVER() != null)
            return Types.getNeverType().getInternalName(null);
        else if (typeType.classOrInterfaceType() != null) {
            var classType = typeType.classOrInterfaceType();
            var className = classType.qualifiedName().getText();
            var typeArgs = classType.typeArguments();
            if (typeArgs != null)
                return className + "<" + NncUtils.join(typeArgs.typeType(), t -> getInternalName(t, typeParameters, scope)) + ">";
            else {
                if (typeParameters.contains(className))
                    return "this." + className;
                var s = scope;
                while (s != null) {
                    if (NncUtils.anyMatch(s.getTypeParameters(), tv -> tv.getCodeNotNull().equals(className)))
                        return s.getGenericDeclaration().getCodeNotNull() + "." + className;
                    s = scope.parent();
                }
                return className;
            }
        } else if (typeType.arrayKind() != null) {
            var arrayKind = parseArrayKind(typeType.arrayKind());
            return arrayKind.getInternalName(getInternalName(typeType.typeType(0), typeParameters, scope));
        } else if (!typeType.BITOR().isEmpty()) {
            return typeType.typeType().stream()
                    .map(t -> getInternalName(t, typeParameters, scope))
                    .sorted(String::compareTo)
                    .collect(Collectors.joining("|"));
        } else if (!typeType.BITAND().isEmpty()) {
            return typeType.typeType().stream()
                    .map(t -> getInternalName(t, typeParameters, scope))
                    .sorted(String::compareTo)
                    .collect(Collectors.joining("&"));
        } else if (typeType.ARROW() != null) {
            return "(" + NncUtils.join(typeType.typeType(), t -> getInternalName(t, typeParameters, scope)) + ")"
                    + "->" + getInternalName(typeType.typeType(typeType.typeType().size() - 1), typeParameters, scope);
        } else if (typeType.LBRACK() != null) {
            return "[" + getInternalName(typeType.typeType(0), typeParameters, scope) + ","
                    + getInternalName(typeType.typeType(1), typeParameters, scope) + "]";
        } else
            throw new IllegalArgumentException("Unrecognized type: " + typeType.getText());
    }

    public List<KlassDTO> getTypes() {
        return Collections.unmodifiableList(types);
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

        List<TypeVariable> getTypeParameters();

        GenericDeclaration getGenericDeclaration();

        default @Nullable TypeVariable findTypeParameter(String name) {
            return NncUtils.find(getTypeParameters(), tv -> tv.getName().equals(name));
        }

    }

    private static final class ClassInfo implements AsmScope, AsmGenericDeclaration {
        @Nullable
        private final AsmScope parent;
        private final Klass klass;
        private final List<String> typeParameterNames;
        private final boolean isEnum;
        private @Nullable ClassType superType;
        int enumConstantOrdinal;
        private final Set<Method> visitedMethods = new HashSet<>();
        private final Set<Field> visitedFields = new HashSet<>();
        private final Set<EnumConstantDef> visitEnumConstantDefs = new HashSet<>();

        private ClassInfo(
                @Nullable AsmScope parent,
                Klass klass,
                List<String> typeParameterNames,
                boolean isEnum
        ) {
            this.parent = parent;
            this.klass = klass;
            this.typeParameterNames = typeParameterNames;
            this.isEnum = isEnum;
        }

        public int nextEnumConstantOrdinal() {
            return enumConstantOrdinal++;
        }

        public String name() {
            return klass.getName();
        }

        public String rawName() {
            return klass.getName();
        }

        @Nullable
        @Override
        public AsmScope parent() {
            return parent;
        }

        @Override
        public List<TypeVariable> getTypeParameters() {
            return klass.getTypeParameters();
        }

        @Override
        public Klass getGenericDeclaration() {
            return klass;
        }

        @Override
        public String toString() {
            return "ClassInfo[" +
                    "parent=" + parent + ", " +
                    "klass=" + klass + ", " +
                    "typeParameters=" + typeParameterNames + ']';
        }

    }

    private static final class MethodInfo implements AsmScope, AsmGenericDeclaration {
        private final ClassInfo parent;
        private final Method method;
        private final String name;

        private MethodInfo(ClassInfo parent, String name, Method method) {
            this.parent = parent;
            this.name = name;
            this.method = method;
        }

        @Override
        @Nullable
        public ClassInfo parent() {
            return parent;
        }

        @Override
        public List<TypeVariable> getTypeParameters() {
            return method.getTypeParameters();
        }

        @Override
        public GenericDeclaration getGenericDeclaration() {
            return method;
        }

        @Override
        public String toString() {
            return "MethodInfo[" +
                    "class=" + parent.rawName() + ", " +
                    "name=" + name;
        }


        @Override
        public String name() {
            return parent.rawName() + "." + name;
        }
    }

    private class VisitorBase extends AssemblyParserBaseVisitor<Void> {

        private CompilationUit compilationUnit;
        protected AsmScope scope;

        @Override
        public Void visitCompilationUnit(AssemblyParser.CompilationUnitContext ctx) {
            compilationUnit = getAttribute(ctx, AsmAttributeKey.compilationUnit);
            return super.visitCompilationUnit(ctx);
        }

        public CompilationUit getCompilationUnit() {
            return compilationUnit;
        }

        @Override
        public Void visitClassDeclaration(AssemblyParser.ClassDeclarationContext ctx) {
            visitTypeDef(ctx.IDENTIFIER().getText(),
                    ctx.RECORD() != null ? TypeCategory.VALUE : TypeCategory.CLASS,
                    ctx.STRUCT() != null,
                    ctx.typeType(),
                    ctx.typeList(),
                    ctx.typeParameters(),
                    ctx,
                    () -> super.visitClassDeclaration(ctx));
            return null;
        }

        @Override
        public Void visitEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx) {
            visitTypeDef(ctx.IDENTIFIER().getText(), TypeCategory.ENUM, false, null, ctx.typeList(),
                    null, ctx, () -> super.visitEnumDeclaration(ctx));
            return null;
        }

        @Override
        public Void visitInterfaceDeclaration(AssemblyParser.InterfaceDeclarationContext ctx) {
            visitTypeDef(ctx.IDENTIFIER().getText(), TypeCategory.INTERFACE, false, null,
                    ctx.typeList(), ctx.typeParameters(), ctx, () -> super.visitInterfaceDeclaration(ctx));
            return null;
        }

        public void visitTypeDef(
                String name,
                TypeCategory typeCategory,
                boolean isStruct,
                @Nullable AssemblyParser.TypeTypeContext superType,
                @Nullable AssemblyParser.TypeListContext interfaces,
                @Nullable AssemblyParser.TypeParametersContext typeParameters,
                ParserRuleContext ctx,
                Runnable processBody
        ) {
            scope = getAttribute(ctx, AsmAttributeKey.classInfo);
            processBody.run();
            scope = scope.parent();
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

    private record CompilationUit(@Nullable String packageName, Map<String, String> names) {

        public String getReferenceName(String name) {
            return names.getOrDefault(name, getDefinitionName(name));
        }

        public String getDefinitionName(String name) {
            return packageName != null ? packageName + "." + name : name;
        }

    }

    private class ImportParser extends AssemblyParserBaseVisitor<Void> {

        @Override
        public Void visitCompilationUnit(AssemblyParser.CompilationUnitContext ctx) {
            var map = new HashMap<String, String>();
            for (AssemblyParser.ImportDeclarationContext importDecl : ctx.importDeclaration()) {
                var qualifiedName = importDecl.qualifiedName().getText();
                var idx = qualifiedName.lastIndexOf('.');
                if (idx != -1) {
                    var name = qualifiedName.substring(idx + 1);
                    map.put(name, qualifiedName);
                }
            }
            var pkgName = ctx.packageDeclaration() != null ? ctx.packageDeclaration().qualifiedName().getText() : null;
            setAttribute(ctx, AsmAttributeKey.compilationUnit, new CompilationUit(pkgName, map));
            return super.visitCompilationUnit(ctx);
        }
    }

    private class AsmInit extends VisitorBase {

        @Override
        public void visitTypeDef(String name,
                                 TypeCategory typeCategory,
                                 boolean isStruct, @Nullable AssemblyParser.TypeTypeContext superType,
                                 @Nullable AssemblyParser.TypeListContext interfaces,
                                 @Nullable AssemblyParser.TypeParametersContext typeParameters,
                                 ParserRuleContext ctx,
                                 Runnable processBody) {
            var code = getCompilationUnit().getDefinitionName(name);
            var klass = tryGetKlass(code);
            var kind = ClassKind.fromTypeCategory(typeCategory);
            if (klass == null)
                klass = createKlass(name, code, kind);
            else
                klass.setKind(kind);
            var classInfo = new ClassInfo(
                    scope,
                    klass,
                    typeParameters != null ?
                            NncUtils.map(typeParameters.typeParameter(), tv -> tv.IDENTIFIER().getText())
                            : List.of(),
                    typeCategory == TypeCategory.ENUM
            );
            setAttribute(ctx, AsmAttributeKey.classInfo, classInfo);
            super.visitTypeDef(name, typeCategory, isStruct, superType, interfaces, typeParameters, ctx, processBody);
        }

        @Override
        protected void visitFunction(String name, @Nullable AssemblyParser.TypeParametersContext typeParameters, @Nullable AssemblyParser.FormalParameterListContext formalParameterList, @Nullable AssemblyParser.TypeTypeOrVoidContext returnType, @Nullable AssemblyParser.BlockContext block, ParserRuleContext ctx, boolean isConstructor, Runnable processBody) {
            processBody.run();
        }
    }

    private class AsmDeclarator extends VisitorBase {

        private final LinkedList<Set<String>> modsStack = new LinkedList<>();

        @Override
        public Void visitTypeDeclaration(AssemblyParser.TypeDeclarationContext ctx) {
            modsStack.push(NncUtils.mapUnique(ctx.classOrInterfaceModifier(), RuleContext::getText));
            super.visitTypeDeclaration(ctx);
            modsStack.pop();
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
        public void visitTypeDef(String name, TypeCategory typeCategory, boolean isStruct, @Nullable AssemblyParser.TypeTypeContext superType, @Nullable AssemblyParser.TypeListContext interfaces, @Nullable AssemblyParser.TypeParametersContext typeParameters, ParserRuleContext ctx, Runnable processBody) {
            var classInfo = getAttribute(ctx, AsmAttributeKey.classInfo);
            scope = classInfo;
            var klass = classInfo.klass;
            processBody.run();
            if(klass.isEnum())
                classInfo.visitedMethods.add(Flows.saveValuesMethod(klass));
            var removedMethods = NncUtils.exclude(klass.getMethods(), classInfo.visitedMethods::contains);
            removedMethods.forEach(klass::removeMethod);
            var removedFields = NncUtils.exclude(klass.getFields(), classInfo.visitedFields::contains);
            removedFields.forEach(klass::removeField);
            scope = classInfo.parent;
        }

        @Override
        public Void visitFieldDeclaration(AssemblyParser.FieldDeclarationContext ctx) {
            var type = parseType(ctx.typeType(), scope, getCompilationUnit());
            var name = ctx.IDENTIFIER().getText();
            var mods = currentMods();
            var classInfo = (ClassInfo) scope;
            var klass = classInfo.klass;
            var field = klass.findField(f -> f.getCodeNotNull().equals(name));
            if (field == null) {
                field = FieldBuilder.newBuilder(name, name, klass, type)
                        .tmpId(NncUtils.randomNonNegative())
                        .isChild(mods.contains(Modifiers.CHILD))
                        .build();
            } else {
                field.setType(type);
                field.setChild(mods.contains(Modifiers.CHILD));
            }
            field.setAccess(getAccess(mods));
            field.setReadonly(mods.contains(Modifiers.READONLY));
            field.setStatic(mods.contains(Modifiers.STATIC));
            if (mods.contains(Modifiers.TITLE))
                klass.setTitleField(field);
            classInfo.visitedFields.add(field);
            return null;
        }

        @Override
        public Void visitEnumConstant(AssemblyParser.EnumConstantContext ctx) {
            var classInfo = (ClassInfo) scope;
            var klass = classInfo.klass;
            var name = ctx.IDENTIFIER().getText();
            var field = klass.findStaticField(f -> f.getName().equals(name));
            if (field == null) {
                field = FieldBuilder.newBuilder(name, name, klass, klass.getType())
                        .tmpId(NncUtils.randomNonNegative())
                        .isStatic(true)
                        .build();
            }
            var enumConstantDef = klass.findEnumConstantDef(ec -> ec.getName().equals(name));
            List<AssemblyParser.ExpressionContext> argCtx =
                    ctx.arguments() != null ? ctx.arguments().expressionList().expression() : List.of();
            var args = NncUtils.map(argCtx, arg -> Values.expression(parseExpression(arg.getText(), new EmptyParsingContext())));
            if(enumConstantDef == null) {
                enumConstantDef = new EnumConstantDef(klass, name, classInfo.nextEnumConstantOrdinal(), args);
            }
            else {
                enumConstantDef.setOrdinal(classInfo.nextEnumConstantOrdinal());
                enumConstantDef.setArguments(args);
            }
            setAttribute(ctx, AsmAttributeKey.field, field);
            setAttribute(ctx, AsmAttributeKey.enumConstantDef, enumConstantDef);
            return super.visitEnumConstant(ctx);
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
            var klass = classInfo.klass;
            List<AssemblyParser.FormalParameterContext> params = formalParameterList != null ? formalParameterList.formalParameter() : List.of();
            Set<String> typeParamNames = typeParameters != null ?
                    NncUtils.mapUnique(typeParameters.typeParameter(), tv -> tv.IDENTIFIER().getText()) : Set.of();
            var paramTypeNames = new ArrayList<String>();
            if(klass.isEnum() && isConstructor) {
                paramTypeNames.add("String");
                paramTypeNames.add("Long");
            }
            params.forEach(p -> paramTypeNames.add(getInternalName(p.typeType(), typeParamNames, scope)));
            var internalName = klass.getCodeNotNull() + "." + name + "(" + String.join(",", paramTypeNames) + ")";
            var method = klass.findMethod(m -> m.getInternalName(null).equals(internalName));
            if (method != null) {
                method.clearNodes();
            }
            else {
                method = MethodBuilder.newBuilder(klass, name, name)
                        .tmpId(NncUtils.randomNonNegative())
                        .isConstructor(isConstructor)
                        .build();
            }
            classInfo.visitedMethods.add(method);
            var methodInfo = new MethodInfo(classInfo, name, method);
            setAttribute(ctx, AsmAttributeKey.methodInfo, methodInfo);
            super.visitFunction(name, typeParameters, formalParameterList, returnType, block, ctx, isConstructor, processBody);
            var parameters = new ArrayList<Parameter>();
            if (isConstructor && classInfo.klass.isEnum()) {
                var nameParam = method.findParameter(p -> p.getName().equals("_name"));
                if (nameParam == null) {
                    nameParam = new Parameter(
                            NncUtils.randomNonNegative(),
                            "_name",
                            "_name",
                            PrimitiveType.stringType
                    );
                }
                parameters.add(nameParam);
                var ordinalParam = method.findParameter(p -> p.getName().equals("_ordinal"));
                if (ordinalParam == null) {
                    ordinalParam = new Parameter(
                            NncUtils.randomNonNegative(),
                            "_ordinal",
                            "_ordinal",
                            PrimitiveType.longType
                    );
                }
                parameters.add(ordinalParam);
            }
            parameters.addAll(parseParameterList(formalParameterList, method, methodInfo, getCompilationUnit()));
            method.setParameters(parameters);
            if (isConstructor) {
                method.setConstructor(true);
                method.setReturnType(klass.getType());
            } else
                method.setReturnType(parseType(requireNonNull(returnType), methodInfo, getCompilationUnit()));
            method.setStatic(currentMods().contains(Modifiers.STATIC));
        }

        @Override
        public Void visitTypeParameter(AssemblyParser.TypeParameterContext ctx) {
            var genericDecl = scope.getGenericDeclaration();
            var name = ctx.IDENTIFIER().getText();
            var type = new TypeVariable(NncUtils.randomNonNegative(), name, name, genericDecl);
            setAttribute(ctx, AsmAttributeKey.typeVariable, type);
            return super.visitTypeParameter(ctx);
        }

        @Override
        public Void visitIndexDeclaration(AssemblyParser.IndexDeclarationContext ctx) {
            var klass = ((ClassInfo) scope).klass;
            var name = ctx.IDENTIFIER().getText();
            var index = klass.findIndex(idx -> idx.getName().equals(name));
            var mods = currentMods();
            if (index == null)
                index = new Index(klass, name, name, null, mods.contains("unique"));
            setAttribute(ctx, AsmAttributeKey.index, index);
            return null;
        }

        private Set<String> currentMods() {
            return requireNonNull(modsStack.peek());
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

    }

    private class IndexDefiner extends VisitorBase {

        @Override
        public Void visitIndexDeclaration(AssemblyParser.IndexDeclarationContext ctx) {
            var index = getAttribute(ctx, AsmAttributeKey.index);
            index.setFields(NncUtils.map(ctx.indexField(), f -> parseIndexField(f, index)));
            return null;
        }

        private IndexField parseIndexField(AssemblyParser.IndexFieldContext ctx, Index index) {
            var name = ctx.IDENTIFIER().getText();
            var field = index.findField(f -> f.getName().equals(name));
            var expression = parseExpression(ctx.expression().getText(), new TypeParsingContext(
                    id -> {
                        throw new UnsupportedOperationException();
                    },
                    new IndexedTypeDefProvider() {
                        @Nullable
                        @Override
                        public Klass findKlassByName(String name) {
                            return klassProvider.apply(name);
                        }

                        @Override
                        public TypeDef getTypeDef(Id id) {
                            throw new UnsupportedOperationException();
                        }
                    },
                    index.getDeclaringType())
            );
            if (field == null)
                field = new IndexField(index, name, name, Values.expression(expression));
            else
                field.setValue(Values.expression(expression));
            return field;
        }

    }

    private class Preprocessor extends VisitorBase {

        @Override
        public Void visitClassDeclaration(AssemblyParser.ClassDeclarationContext ctx) {
            var classInfo = getAttribute(ctx, AsmAttributeKey.classInfo);
            var supers = new ArrayList<ClassType>();
            superTypes.put(classInfo.rawName(), supers);
            if (ctx.EXTENDS() != null)
                supers.add((ClassType) parseType(ctx.typeType(), scope, getCompilationUnit()));
            if (ctx.IMPLEMENTS() != null)
                ctx.typeList().typeType().forEach(t -> supers.add((ClassType) parseType(t, scope, getCompilationUnit())));
            super.visitClassDeclaration(ctx);
            return null;
        }

        @Override
        public Void visitEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx) {
            var classInfo = getAttribute(ctx, AsmAttributeKey.classInfo);
            var supers = addSupers(classInfo.rawName());
            var pEnumType = new ClassType(getKlass(Enum.class.getName()), List.of(classInfo.klass.getType()));
            supers.add(pEnumType);
            if (ctx.IMPLEMENTS() != null)
                forEachClass(ctx.typeList(), supers::add);
            super.visitEnumDeclaration(ctx);
            return null;
        }

        @Override
        public Void visitInterfaceDeclaration(AssemblyParser.InterfaceDeclarationContext ctx) {
            var classInfo = getAttribute(ctx, AsmAttributeKey.classInfo);
            var supers = addSupers(classInfo.rawName());
            if (ctx.EXTENDS() != null)
                forEachClass(ctx.typeList(), supers::add);
            super.visitInterfaceDeclaration(ctx);
            return null;
        }

        private List<ClassType> addSupers(String name) {
            var supers = new ArrayList<ClassType>();
            superTypes.put(name, supers);
            return supers;
        }

        private void forEachClass(AssemblyParser.TypeListContext typeList, Consumer<ClassType> action) {
            typeList.typeType().forEach(t -> action.accept((ClassType) parseType(t, scope, getCompilationUnit())));
        }

    }

    private class AsmGenerator extends VisitorBase {

        private final LinkedList<Method> cinits = new LinkedList<>();
        private int nextNodeNum = 0;

        @Override
        public void visitTypeDef(String name,
                                 TypeCategory typeCategory,
                                 boolean isStruct,
                                 @Nullable AssemblyParser.TypeTypeContext superType,
                                 @Nullable AssemblyParser.TypeListContext interfaces,
                                 @Nullable AssemblyParser.TypeParametersContext typeParameters,
                                 ParserRuleContext ctx,
                                 Runnable processBody) {
            var currentClass = getAttribute(ctx, AsmAttributeKey.classInfo);
            var klass = currentClass.klass;
            scope = currentClass;
            klass.setStruct(isStruct);
            if (typeCategory.isEnum())
                currentClass.superType = new ClassType(getKlass(Enum.class.getName()), List.of(currentClass.klass.getType()));
            else if (superType != null)
                currentClass.superType = (ClassType) parseType(superType, currentClass, getCompilationUnit());
            if (currentClass.superType != null)
                klass.setSuperType(currentClass.superType);
            if (interfaces != null)
                klass.setInterfaces(NncUtils.map(interfaces.typeType(), t -> (ClassType) parseType(t, scope, getCompilationUnit())));
            var cinit = klass.findMethodByCodeAndParamTypes("<cinit>", List.of());
            if (cinit != null)
                cinit.clearNodes();
            else {
                cinit = MethodBuilder.newBuilder(currentClass.klass, "<cinit>", "<cinit>")
                        .isStatic(true)
                        .tmpId(NncUtils.randomNonNegative())
                        .access(Access.PRIVATE)
                        .returnType(PrimitiveType.voidType)
                        .build();
            }
            if(klass.isEnum())
                Flows.generateValuesMethodBody(klass);
            cinits.push(cinit);
            processBody.run();
            Nodes.ret(nextNodeName(), cinit.getRootScope(), null);
            scope = currentClass.parent;
            cinits.pop();
        }

        private Method cinit() {
            return requireNonNull(cinits.peek());
        }

        private ClassInfo currentClass() {
            var s = scope;
            while (s != null && !(s instanceof ClassInfo)) {
                s = s.parent();
            }
            return Objects.requireNonNull((ClassInfo) s, "Not in any class scope");
        }

        @Override
        public Void visitTypeParameter(AssemblyParser.TypeParameterContext ctx) {
            var typeVariable = getAttribute(ctx, AsmAttributeKey.typeVariable);
            if (ctx.typeType() != null)
                typeVariable.setBounds(List.of(parseType(ctx.typeType(), scope, getCompilationUnit())));
            try (var serContext = SerializeContext.enter()) {
                typeVariables.add(typeVariable.toDTO(serContext));
            }
            return null;
        }

        @Override
        protected void visitFunction(String name, @Nullable AssemblyParser.TypeParametersContext typeParameters, @Nullable AssemblyParser.FormalParameterListContext parameterList, @Nullable AssemblyParser.TypeTypeOrVoidContext returnType, @Nullable AssemblyParser.BlockContext block, ParserRuleContext ctx, boolean isConstructor, Runnable processBody) {
            var methodInfo = getAttribute(ctx, AsmAttributeKey.methodInfo);
            scope = methodInfo;
            try {
                var klass = currentClass().klass;
                var method = methodInfo.method;
                if (!isConstructor) {
                    var supers = new LinkedList<>(superTypes.get(currentClass().klass.getName()));
                    while (!supers.isEmpty()) {
                        var s = supers.poll();
                        var overridden = s.resolve().findMethod(m -> {
                            if (m.getName().equals(method.getName()) && Objects.equals(m.getCode(), method.getCode())
                                    && m.getParameters().size() == method.getParameters().size()
                                    && m.getTypeParameters().size() == method.getTypeParameters().size()
                            ) {
                                if (method.getTypeParameters().isEmpty())
                                    return m.getParameterTypes().equals(method.getParameterTypes());
                                else {
                                    var subst = new TypeSubstitutor(method.getTypeArguments(), m.getTypeArguments());
                                    var paramTypesSubst = NncUtils.map(method.getParameterTypes(), t -> t.accept(subst));
                                    return paramTypesSubst.equals(m.getParameterTypes());
                                }
                            } else
                                return false;
                        });
                        if (overridden != null)
                            method.addOverridden(overridden);
                        else
                            supers.addAll(superTypes.getOrDefault(s.getKlass().getName(), List.of()));
                    }
                }
                var currentClass = currentClass();
                if (block != null) {
                    var rootScope = method.getRootScope();
                    SelfNode selfNode = null;
                    if (!method.isStatic()) {
                        selfNode = new SelfNode(
                                NncUtils.randomNonNegative(),
                                "this",
                                null,
                                klass.getType(),
                                null,
                                rootScope
                        );
                    }
                    var inputNode = Nodes.input(method, nextNodeName("input"), null);
                    for (var parameter : method.getParameters()) {
                        new ValueNode(
                                NncUtils.randomNonNegative(),
                                parameter.getName(),
                                null,
                                parameter.getType(),
                                rootScope.getLastNode(),
                                rootScope,
                                Values.nodeProperty(inputNode, inputNode.getKlass().getFieldByCode(parameter.getCode()))
                        );
                    }
                    if (isConstructor && currentClass.isEnum) {
                        new UpdateObjectNode(
                                NncUtils.randomNonNegative(),
                                nextNodeName(),
                                null,
                                rootScope.getLastNode(),
                                rootScope,
                                Values.node(selfNode),
                                List.of(
                                        new UpdateField(
                                                klass.getFieldByCode("name").getRef(),
                                                UpdateOp.SET,
                                                Values.nodeProperty(inputNode, inputNode.getKlass().getFieldByCode("_name"))
                                        ),
                                        new UpdateField(
                                                klass.getFieldByCode("ordinal").getRef(),
                                                UpdateOp.SET,
                                                Values.nodeProperty(inputNode, inputNode.getKlass().getFieldByCode("_ordinal"))
                                        )
                                )
                        );
                    }
                    processMethodBlock(block, method);
                    if (isConstructor) {
                        new ReturnNode(
                                NncUtils.randomNonNegative(),
                                nextNodeName(),
                                null,
                                rootScope.getLastNode(),
                                rootScope,
                                Values.node(Objects.requireNonNull(selfNode))
                        );
                    } else if (Objects.requireNonNull(returnType).VOID() != null) {
                        new ReturnNode(
                                NncUtils.randomNonNegative(),
                                nextNodeName(),
                                null,
                                rootScope.getLastNode(),
                                rootScope,
                                null
                        );
                    }
                }
                if (typeParameters != null)
                    typeParameters.accept(this);
            } finally {
                scope = scope.parent();
            }
        }

        private void processMethodBlock(AssemblyParser.BlockContext block, Method method) {
            for (var stmt : block.labeledStatement()) {
                processLabeledStatement(stmt, method.getRootScope());
            }
        }

        private String nextNodeName() {
            return nextNodeName("__node__");
        }

        private String nextNodeName(String prefix) {
            return prefix + nextNodeNum++;
        }

        @SuppressWarnings("UnusedReturnValue")
        private List<NodeRT> parseBlockNodes(AssemblyParser.BlockContext block, ScopeRT scope) {
            return NncUtils.map(block.labeledStatement(), s -> processLabeledStatement(s, scope));
        }

        private NodeRT processLabeledStatement(AssemblyParser.LabeledStatementContext labeledStatement, ScopeRT scope) {
            var name = labeledStatement.IDENTIFIER() != null ? labeledStatement.IDENTIFIER().getText() :
                    nextNodeName();
            return processStatement(name, labeledStatement.statement(), scope);
        }

        private NodeRT processStatement(String name, AssemblyParser.StatementContext statement, ScopeRT scope) {
            try {
                var currentClass = currentClass();
                var prevNode = scope.getLastNode();
                var parsingContext = new FlowParsingContext(
                        id -> {
                            throw new UnsupportedOperationException();
                        },
                        new AsmTypeDefProvider(),
                        scope,
                        scope.getLastNode()
                );
                if (statement.bop != null) {
                    var object = statement.THIS() != null ? parseValue("this", parsingContext)
                            : parseValue(statement.IDENTIFIER(0).getText(), parsingContext);
                    var objectType = (ClassType) object.getType();
                    var field = objectType.resolve().getFieldByCode(statement.IDENTIFIER(statement.IDENTIFIER().size() - 1).getText());
                    return new UpdateObjectNode(
                            NncUtils.randomNonNegative(),
                            name,
                            null,
                            scope.getLastNode(),
                            scope,
                            object,
                            List.of(
                                    new UpdateField(
                                            field.getRef(),
                                            parseUpdateOp(statement.bop.getText()),
                                            parseValue(statement.expression(), parsingContext)
                                    )
                            )
                    );
                }
                if (statement.RETURN() != null) {
                    return new ReturnNode(
                            NncUtils.randomNonNegative(),
                            name,
                            null,
                            scope.getLastNode(),
                            scope,
                            statement.expression() != null ?
                                    parseValue(statement.expression(), parsingContext) : null
                    );
                }
                if (statement.NEW() != null || statement.UNEW() != null || statement.ENEW() != null) {
                    var creator = statement.creator();
                    var type = (ClassType) parseClassType(creator.classOrInterfaceType(), this.scope, getCompilationUnit());
                    var targetKlass = type.resolve();
                    List<AssemblyParser.ExpressionContext> arguments =
                            NncUtils.getOrElse(
                                    creator.arguments().expressionList(),
                                    AssemblyParser.ExpressionListContext::expression,
                                    List.of()
                            );
                    List<Type> typeArgs = creator.typeArguments() != null ?
                            NncUtils.map(creator.typeArguments().typeType(), t -> parseType(t, this.scope, getCompilationUnit())) : List.of();
                    var args = NncUtils.map(arguments, arg -> parseValue(arg, parsingContext));
                    var constructor = targetKlass.resolveMethod(
                            targetKlass.getEffectiveTemplate().getName(), NncUtils.map(args, Value::getType), typeArgs, false
                    );
                    return new NewObjectNode(
                            NncUtils.randomNonNegative(),
                            name,
                            null,
                            constructor.getRef(),
                            NncUtils.biMap(constructor.getParameters(), args, (p, a) -> new Argument(NncUtils.randomNonNegative(), p.getRef(), a)),
                            scope.getLastNode(),
                            scope,
                            null,
                            statement.UNEW() != null,
                            statement.ENEW() != null
                    );
                }
                if (statement.methodCall() != null) {
                    var methodCall = statement.methodCall();
                    List<Value> arguments = methodCall.expressionList() != null ?
                            NncUtils.map(methodCall.expressionList().expression(), e -> parseValue(e, parsingContext)) : List.of();
                    Value self;
                    String methodName;
                    ClassType type;
                    if (methodCall.IDENTIFIER() != null) {
                        methodName = methodCall.IDENTIFIER().getText();
                        var targetKlass = tryGetKlass(methodCall.expression().getText());
                        if (targetKlass != null) {
                            type = new ClassType(targetKlass, List.of());
                            self = null;
                        } else {
                            self = parseValue(methodCall.expression(), parsingContext);
                            type = (ClassType) self.getType();
                        }
                    } else if (methodCall.SUPER() != null) {
                        methodName = requireNonNull(currentClass.superType).getKlass().getName();
                        self = parseValue("this", parsingContext);
                        type = (ClassType) self.getType();
                    } else if (methodCall.THIS() != null) {
                        methodName = currentClass.klass.getName();
                        self = parseValue("this", parsingContext);
                        type = (ClassType) self.getType();
                    } else
                        throw new InternalException("methodCall syntax error: " + methodCall.getText());
                    List<Type> typeArgs = methodCall.typeArguments() != null ?
                            NncUtils.map(methodCall.typeArguments().typeType(), t -> parseType(t, this.scope, getCompilationUnit())) : List.of();
                    Method method = type.resolve().resolveMethod(methodName, NncUtils.map(arguments, Value::getType), typeArgs, false);
                    return new MethodCallNode(
                            NncUtils.randomNonNegative(),
                            name,
                            null,
                            prevNode,
                            scope,
                            self,
                            method.getRef(),
                            NncUtils.biMap(method.getParameters(), arguments, (p, v) -> new Argument(NncUtils.randomNonNegative(), p.getRef(), v))
                    );
                }
                if (statement.functionCall() != null) {
                    var funcCall = statement.functionCall();
                    return new FunctionNode(
                            NncUtils.randomNonNegative(),
                            name,
                            null,
                            prevNode,
                            scope,
                            parseValue(funcCall.expression(), parsingContext),
                            parseValueList(funcCall.expressionList(), parsingContext)
                    );
                }
                if (statement.THROW() != null) {
                    return new RaiseNode(
                            NncUtils.randomNonNegative(),
                            name,
                            null,
                            prevNode,
                            scope,
                            RaiseParameterKind.THROWABLE,
                            parseValue(statement.expression(), parsingContext),
                            null
                    );
                }
                if (statement.IF() != null) {
                    var branchNode = new BranchNode(
                            NncUtils.randomNonNegative(),
                            name,
                            null,
                            false,
                            prevNode,
                            scope
                    );
                    var thenBranch = branchNode.addBranch(parseValue(statement.parExpression().expression(), parsingContext));
                    parseBlockNodes(statement.block(0), thenBranch.getScope());
                    var elseBranch = branchNode.addDefaultBranch();
                    if (statement.ELSE() != null)
                        parseBlockNodes(statement.block(1), elseBranch.getScope());
                    return branchNode;
                }
                if (statement.FOR() != null) {
                    var fieldTypes = new HashMap<String, Type>();
                    var initialValues = new HashMap<String, Value>();
                    var updatedValues = new HashMap<String, Value>();
                    var forCtl = statement.forControl();
                    var loopVarDecls = forCtl.loopVariableDeclarators();
                    if (loopVarDecls != null) {
                        for (var decl : loopVarDecls.loopVariableDeclarator()) {
                            var fieldName = decl.IDENTIFIER().getText();
                            fieldTypes.put(fieldName, parseType(decl.typeType(), currentClass, getCompilationUnit()));
                            initialValues.put(fieldName, parseValue(decl.expression(), parsingContext));
                        }
                    }
                    var loopKlass = KlassBuilder.newBuilder("loop" + NncUtils.randomNonNegative(), null)
                            .tmpId(NncUtils.randomNonNegative())
                            .temporary()
                            .build();
                    fieldTypes.forEach((fieldName, fieldType) ->
                            FieldBuilder.newBuilder(fieldName, fieldName, loopKlass, fieldType).build()
                    );
                    var whileNode = new WhileNode(
                            NncUtils.randomNonNegative(),
                            name,
                            null,
                            loopKlass,
                            prevNode,
                            scope,
                            Values.constantBoolean(true)
                    );
                    parseBlockNodes(statement.block(0), whileNode.getBodyScope());
                    var loopParsingContext = new FlowParsingContext(
                            id -> {
                                throw new UnsupportedOperationException();
                            },
                            new AsmTypeDefProvider(),
                            whileNode.getBodyScope(),
                            whileNode.getBodyScope().getLastNode()
                    );
                    whileNode.setCondition(parseValue(forCtl.expression(), loopParsingContext));
                    if (loopVarDecls != null) {
                        for (var update : forCtl.loopVariableUpdates().loopVariableUpdate()) {
                            updatedValues.put(update.IDENTIFIER().getText(), parseValue(update.expression(), loopParsingContext));
                        }
                    }
                    fieldTypes.keySet().forEach(fieldName -> whileNode.setField(
                            loopKlass.getFieldByCode(fieldName),
                            requireNonNull(initialValues.get(fieldName)),
                            requireNonNull(updatedValues.get(fieldName))
                    ));
                    if (DebugEnv.debugging) {
                        DebugEnv.logger.info("loopFields: {}", NncUtils.toJSONString(whileNode.getFields()));
                        DebugEnv.logger.info("loopCond: {}", NncUtils.toJSONString(parseValue(forCtl.expression(), parsingContext)));
                    }

                    return whileNode;
                }
                if (statement.lambda() != null) {
                    var lambda = statement.lambda();
                    var params = parseParameterList(lambda.lambdaParameters().formalParameterList(), null, this.scope, getCompilationUnit());
                    var lambdaNode = new LambdaNode(
                            NncUtils.randomNonNegative(),
                            name,
                            null,
                            prevNode,
                            scope,
                            params,
                            parseType(lambda.typeTypeOrVoid(), this.scope, getCompilationUnit()),
                            null
                    );
                    var bodyScope = lambdaNode.getBodyScope();
                    var inputKlass = KlassBuilder.newBuilder("Input" + NncUtils.randomNonNegative(), null)
                            .tmpId(NncUtils.randomNonNegative())
                            .temporary()
                            .build();
                    var inputName = nextNodeName("__input__");
                    var inputNode = new InputNode(
                            NncUtils.randomNonNegative(),
                            inputName,
                            null,
                            inputKlass,
                            null,
                            bodyScope
                    );
                    params.forEach(p -> {
                        var inputField = FieldBuilder.newBuilder(p.getName(), p.getCode(), inputKlass, p.getType()).build();
                        new ValueNode(NncUtils.randomNonNegative(),
                                p.getName(),
                                p.getCode(),
                                p.getType(),
                                bodyScope.getLastNode(),
                                bodyScope,
                                Values.nodeProperty(inputNode, inputField)
                        );
                    });
                    parseBlockNodes(lambda.lambdaBody().block(), lambdaNode.getBodyScope());
                    if (lambda.typeTypeOrVoid().VOID() != null) {
                        new ReturnNode(NncUtils.randomNonNegative(),
                                nextNodeName(),
                                null,
                                bodyScope.getLastNode(),
                                bodyScope,
                                null);
                    }
                    return lambdaNode;
                }
                if (statement.select() != null) {
                    var select = statement.select();
                    var klass = getKlass(select.qualifiedName().getText());
                    var indexName = select.IDENTIFIER().getText();
                    var index = Objects.requireNonNull(klass.findIndex(i -> i.getName().equals(indexName)),
                            () -> "Cannot find index with name " + indexName + " class " + klass.getTypeDesc());
                    var fieldValues = NncUtils.map(
                            select.expression(),
                            e -> Values.expression(parseExpression(e.getText(), parsingContext))
                    );
                    var key = new IndexQueryKey(
                            index,
                            NncUtils.biMap(
                                    index.getFields(), fieldValues,
                                    IndexQueryKeyItem::new
                            )
                    );
                    if (select.SELECT() != null) {
                        return new IndexSelectNode(
                                null,
                                name,
                                null,
                                index.getDeclaringType().getType(),
                                prevNode, scope,
                                index,
                                key
                        );
                    } else {
                        return new IndexSelectFirstNode(
                                null,
                                name,
                                null,
                                prevNode,
                                scope,
                                index,
                                key
                        );
                    }
                }
                if(statement.castType != null) {
                    return new CastNode(
                            null,
                            name,
                            null,
                            parseType(statement.castType, this.scope, getCompilationUnit()),
                            prevNode,
                            scope,
                            parseValue(statement.expression().getText(), parsingContext)
                    );
                }
                throw new InternalException("Unknown statement: " + statement.getText());
            } catch (Exception e) {
                throw new InternalException("Fail to process statement: " + statement.getText(), e);
            }
        }

        private UpdateOp parseUpdateOp(String bop) {
            if (bop.equals("="))
                return UpdateOp.SET;
            if (bop.equals("+="))
                return UpdateOp.INC;
            if (bop.equals("-="))
                return UpdateOp.DEC;
            throw new InternalException("Unknown binary operator: " + bop);
        }

        private Value parseValue(AssemblyParser.ExpressionContext expression, ParsingContext parsingContext) {
            return parseValue(expression.getText(), parsingContext);
        }

        private List<Value> parseValueList(@Nullable AssemblyParser.ExpressionListContext expressionList, ParsingContext parsingContext) {
            if (expressionList == null)
                return List.of();
            return NncUtils.map(expressionList.expression(), e -> parseValue(e, parsingContext));
        }


        private Value parseValue(String expression, ParsingContext parsingContext) {
            return Values.expression(parseExpression(expression, parsingContext));
        }

    }

    private class KlassInitializer extends VisitorBase {

        @Override
        public void visitTypeDef(String name, TypeCategory typeCategory, boolean isStruct, @Nullable AssemblyParser.TypeTypeContext superType, @Nullable AssemblyParser.TypeListContext interfaces, @Nullable AssemblyParser.TypeParametersContext typeParameters, ParserRuleContext ctx, Runnable processBody) {
            var klass = getAttribute(ctx, AsmAttributeKey.classInfo).klass;
            try (var serContext = SerializeContext.enter()) {
                serContext.addWritingCodeType(klass);
                types.add(klass.toDTO(serContext));
            }
            super.visitTypeDef(name, typeCategory, isStruct, superType, interfaces, typeParameters, ctx, processBody);
        }
    }

    private AssemblyParser.CompilationUnitContext parse(String source) {
        var input = CharStreams.fromString(source);
        var parser = new AssemblyParser(new CommonTokenStream(new AssemblyLexer(input)));
        return parser.compilationUnit();
    }

    private Type parseType(AssemblyParser.TypeTypeOrVoidContext typeTypeOrVoid, AsmScope scope, CompilationUit compilationUnit) {
        if (typeTypeOrVoid.VOID() != null)
            return PrimitiveType.voidType;
        return parseType(typeTypeOrVoid.typeType(), scope, compilationUnit);
    }

    private Type parseType(AssemblyParser.TypeTypeContext typeType, AsmScope scope, CompilationUit compilationUnit) {
        if (typeType.ANY() != null)
            return AnyType.instance;
        if (typeType.NEVER() != null)
            return NeverType.instance;
        if (typeType.primitiveType() != null)
            return parsePrimitiveType(typeType.primitiveType());
        if (typeType.classOrInterfaceType() != null)
            return parseClassType(typeType.classOrInterfaceType(), scope, compilationUnit);
        if (typeType.arrayKind() != null) {
            var arrayKind = typeType.arrayKind();
            var elementType = parseType(typeType.typeType(0), scope, compilationUnit);
            return new ArrayType(elementType, parseArrayKind(arrayKind));
        }
        if (!typeType.BITOR().isEmpty()) {
            var members = NncUtils.map(
                    typeType.typeType(),
                    typeType1 -> parseType(typeType1, scope, compilationUnit)
            );
            return new UnionType(new HashSet<>(members));
        }
        if (!typeType.BITAND().isEmpty()) {
            var types = NncUtils.map(
                    typeType.typeType(),
                    typeType1 -> parseType(typeType1, scope, compilationUnit)
            );
            return new IntersectionType(new HashSet<>(types));
        }
        if (typeType.ARROW() != null) {
            int numParams = typeType.typeType().size() - 1;
            var parameterTypes = NncUtils.map(
                    typeType.typeType().subList(0, numParams),
                    typeType1 -> parseType(typeType1, scope, compilationUnit)
            );
            var returnType = parseType(typeType.typeType(numParams), scope, compilationUnit);
            return new FunctionType(parameterTypes, returnType);
        }
        if (typeType.LBRACK() != null) {
            var lowerBound = parseType(typeType.typeType(0), scope, compilationUnit);
            var upperBound = parseType(typeType.typeType(1), scope, compilationUnit);
            return new UncertainType(lowerBound, upperBound);
        }
        throw new InternalException("Unknown type: " + typeType.getText());
    }

    private ArrayKind parseArrayKind(AssemblyParser.ArrayKindContext ctx) {
        if (ctx.R() != null)
            return ArrayKind.READ_ONLY;
        else if (ctx.RW() != null)
            return ArrayKind.READ_WRITE;
        else if (ctx.C() != null)
            return ArrayKind.CHILD;
        else if (ctx.V() != null)
            return ArrayKind.VALUE;
        throw new InternalException("Unknown array kind: " + ctx.getText());
    }

    private static PrimitiveType parsePrimitiveType(AssemblyParser.PrimitiveTypeContext primitiveType) {
        if (primitiveType.INT() != null)
            return PrimitiveType.longType;
        else if (primitiveType.DOUBLE() != null)
            return PrimitiveType.doubleType;
        else if (primitiveType.BOOLEAN() != null)
            return PrimitiveType.booleanType;
        else if (primitiveType.STRING() != null)
            return PrimitiveType.stringType;
        else if (primitiveType.PASSWORD() != null)
            return PrimitiveType.passwordType;
        else if (primitiveType.TIME() != null)
            return PrimitiveType.timeType;
        else if (primitiveType.NULL() != null)
            return PrimitiveType.nullType;
        else if (primitiveType.VOID() != null)
            return PrimitiveType.voidType;
        else
            throw new InternalException("Unknown primitive type");
    }

    private Type parseClassType(AssemblyParser.ClassOrInterfaceTypeContext classOrInterfaceType, AsmScope scope, CompilationUit compilationUnit) {
        var name = compilationUnit.getReferenceName(classOrInterfaceType.qualifiedName().getText());
        if (!name.contains(".")) {
            var k = scope;
            while (k != null) {
                var found = k.findTypeParameter(name);
                if (found != null)
                    return new VariableType(found);
                k = k.parent();
            }
        }
        List<Type> typeArguments = classOrInterfaceType.typeArguments() != null ? NncUtils.map(
                classOrInterfaceType.typeArguments().typeType(),
                typeType1 -> parseType(typeType1, scope, compilationUnit)
        ) : List.of();
        return new ClassType(getKlass(name), typeArguments);
    }

    public Klass getKlass(String name) {
        return requireNonNull(tryGetKlass(name), () -> "Can not find class with name '" + name + "'");
    }

    private @Nullable Klass tryGetKlass(String code) {
        return code2klass.computeIfAbsent(code, klassProvider);
    }

    private Klass createKlass(String name, String code, ClassKind kind) {
        var klass = KlassBuilder.newBuilder(name, code).kind(kind).tmpId(NncUtils.randomNonNegative()).build();
        code2klass.put(name, klass);
        binder.accept(klass);
        return klass;
    }

    private String getSource(String path) {
        try (var reader = new BufferedReader(new FileReader(path))) {
            int n = reader.read(buf);
            return new String(buf, 0, n);
        } catch (IOException e) {
            throw new InternalException("Can not read source '" + path + "'", e);
        }
    }

    private record AsmAttributeKey<T>(String name, Class<T> klass) {

        public static final AsmAttributeKey<TypeVariable> typeVariable = new AsmAttributeKey<>("typeVariable", TypeVariable.class);

        public static final AsmAttributeKey<ClassInfo> classInfo = new AsmAttributeKey<>("classInfo", ClassInfo.class);

        public static final AsmAttributeKey<MethodInfo> methodInfo = new AsmAttributeKey<>("methodInfo", MethodInfo.class);

        public static final AsmAttributeKey<Field> field = new AsmAttributeKey<>("field", Field.class);

        public static final AsmAttributeKey<Index> index = new AsmAttributeKey<>("index", Index.class);

        public static final AsmAttributeKey<CompilationUit> compilationUnit = new AsmAttributeKey<>("compilationUnit", CompilationUit.class);

        public static final AsmAttributeKey<EnumConstantDef> enumConstantDef = new AsmAttributeKey<>("enumConstantDef", EnumConstantDef.class);

        T cast(Object value) {
            return klass.cast(value);
        }
    }

    public interface AsmGenericDeclaration {

        String name();

    }

    public List<TypeDefDTO> getAllTypeDefs() {
        List<TypeDefDTO> typeDefs = new ArrayList<>(types);
        typeDefs.addAll(typeVariables);
        return typeDefs;
    }

    private class AsmTypeDefProvider implements IndexedTypeDefProvider {

        @Nullable
        @Override
        public Klass findKlassByName(String name) {
            return Assembler.this.getKlass(name);
        }

        @Override
        public TypeDef getTypeDef(Id id) {
            throw new UnsupportedOperationException();
        }
    }
}
