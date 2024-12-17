package org.metavm.asm;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RuleContext;
import org.metavm.asm.antlr.AssemblyLexer;
import org.metavm.asm.antlr.AssemblyParser;
import org.metavm.asm.antlr.AssemblyParserBaseVisitor;
import org.metavm.entity.*;
import org.metavm.expression.Expression;
import org.metavm.flow.*;
import org.metavm.object.type.EnumConstantDef;
import org.metavm.object.type.*;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.util.LinkedList;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static java.util.Objects.requireNonNull;

public class Assembler {

    public static final Logger logger = LoggerFactory.getLogger(Assembler.class);

    private final char[] buf = new char[1024 * 1024];

    private final Map<String, List<ClassType>> superTypes = new HashMap<>();
    private final List<Klass> generatedKlasses = new ArrayList<>();
    private final Map<String, Klass> name2klass = new HashMap<>();
    private final Map<ParserRuleContext, Map<AsmAttributeKey<?>, Object>> attributes = new HashMap<>();
    private final Function<String, Klass> klassProvider;

    public Assembler(Function<String, Klass> klassProvider) {
        for (StdKlass stdKlass : StdKlass.values()) {
            var klass = stdKlass.get();
            name2klass.put(klass.getQualifiedName(), klass);
        }
        this.klassProvider = klassProvider;
    }

    @SuppressWarnings("UnusedReturnValue")
    public void assemble(List<String> sourcePaths) {
        var units = NncUtils.map(sourcePaths, path -> parse(getSource(path)));
        assemble0(units);
    }

    public void generateClasses(String targetDir) {
        try (var serContext = SerializeContext.enter();
             var ignored = ContextUtil.getProfiler().enter("deploy")) {
            NncUtils.clearDirectory(targetDir);
            serContext.includingCode(true)
                    .includeNodeOutputType(false)
                    .includingValueType(false);
            for (var klass : generatedKlasses) {
                if(!klass.isInner() && !klass.isLocal())
                    writeClassFile(klass, targetDir, serContext);
            }
            logger.info("Compile successful");
            createArchive(targetDir);
        }
    }

    private void writeClassFile(Klass klass, String targetDir, SerializeContext serializeContext) {
        var path = targetDir + '/' + requireNonNull(klass.getQualifiedName()).replace('.', '/') + ".mvclass";
        var bout = new ByteArrayOutputStream();
        var output = new KlassOutput(bout, serializeContext);
        klass.write(output);
        NncUtils.writeFile(path, bout.toByteArray());
    }

    private void createArchive(String target) {
        var targetDir = Paths.get(target);
        var zipFilePath = targetDir + "/target.mva";
        try(var zipOut = new ZipOutputStream(new FileOutputStream(zipFilePath));
            var files = Files.walk(targetDir)) {
            files.filter(f -> f.toString().endsWith(".mvclass"))
                    .forEach(f -> {
                        var zipEntry = new ZipEntry(targetDir.relativize(f).toString());
                        try {
                            zipOut.putNextEntry(zipEntry);
                            Files.copy(f, zipOut);
                            zipOut.closeEntry();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }



    public void assemble(InputStream input) {
        var unit = parse(input);
        assemble0(List.of(unit));
    }

    private void assemble0(List<AssemblyParser.CompilationUnitContext> units) {
        visit(units, new ImportParser());
        visit(units, new AsmInit());
        visit(units, new AsmDeclarator());
        visit(units, new IndexDefiner());
        visit(units, new Preprocessor());
        visit(units, new AsmGenerator());
        try (var ignored = SerializeContext.enter()) {
            visit(units, new KlassInitializer());
        }
    }

    private void visit(List<AssemblyParser.CompilationUnitContext> units, AssemblyParserBaseVisitor<Void> visitor) {
        units.forEach(unit -> unit.accept(visitor));
    }

    private List<Parameter> parseParameterList(@Nullable AssemblyParser.FormalParameterListContext parameterList,
                                               Callable callable, AsmScope scope, AsmCompilationUnit compilationUnit) {
        if (parameterList == null)
            return List.of();
        return NncUtils.map(parameterList.formalParameter(), p -> parseParameter(p, callable, scope, compilationUnit));
    }

    private Parameter parseParameter(AssemblyParser.FormalParameterContext parameter, Callable callable, AsmScope scope, AsmCompilationUnit compilationUnit) {
        var name = parameter.IDENTIFIER().getText();
        var type = parseType(parameter.typeType(), scope, compilationUnit);
        var existing = callable.findParameter(p -> p.getName().equals(name));
        if (existing != null) {
            existing.setType(type);
            return existing;
        }
        return new Parameter(
                NncUtils.randomNonNegative(),
                name,
                type,
                callable
        );
    }

    private Type parseExpression(AssemblyParser.ExpressionContext ctx,
                                  AsmCodeGenerator codeGenerator) {
        var callable = (AsmCallable) codeGenerator.scopeNotNull();
        var compilationUnit = callable.getCompilationUnit();
        return new AsmExpressionResolver(
                t -> parseType(t, callable, compilationUnit),
                t -> parseClassType(t, callable, compilationUnit),
                codeGenerator,
                name -> findKlass(compilationUnit.getReferenceName(name))
        ).resolve(ctx);
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
            var className = scope.getCompilationUnit().getReferenceName(classType.qualifiedName().getText());
            var typeArgs = classType.typeArguments();
            if (typeArgs != null)
                return className + "<" + NncUtils.join(typeArgs.typeType(), t -> getInternalName(t, typeParameters, scope)) + ">";
            else {
                if (typeParameters.contains(className))
                    return "this." + className;
                var s = scope;
                while (s != null) {
                    if (NncUtils.anyMatch(s.getTypeParameters(), tv -> tv.getName().equals(className)))
                        return s.getGenericDeclaration().getQualifiedName() + "." + className;
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

    public List<Klass> getGeneratedKlasses() {
        return Collections.unmodifiableList(generatedKlasses);
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
        public static final String DELETED = "deleted";
    }

    private class VisitorBase extends AssemblyParserBaseVisitor<Void> {

        private AsmCompilationUnit compilationUnit;
        protected AsmScope scope;

        @Override
        public Void visitCompilationUnit(AssemblyParser.CompilationUnitContext ctx) {
            compilationUnit = getAttribute(ctx, AsmAttributeKey.compilationUnit);
            return super.visitCompilationUnit(ctx);
        }

        public AsmCompilationUnit getCompilationUnit() {
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
                    ctx.annotation(), ctx,
                    () -> super.visitClassDeclaration(ctx));
            return null;
        }

        @Override
        public Void visitEnumDeclaration(AssemblyParser.EnumDeclarationContext ctx) {
            visitTypeDef(ctx.IDENTIFIER().getText(), TypeCategory.ENUM, false, null, ctx.typeList(),
                    null, ctx.annotation(), ctx, () -> super.visitEnumDeclaration(ctx));
            return null;
        }

        @Override
        public Void visitInterfaceDeclaration(AssemblyParser.InterfaceDeclarationContext ctx) {
            visitTypeDef(ctx.IDENTIFIER().getText(), TypeCategory.INTERFACE, false, null,
                    ctx.typeList(), ctx.typeParameters(), ctx.annotation(), ctx, () -> super.visitInterfaceDeclaration(ctx));
            return null;
        }

        public void visitTypeDef(
                String name,
                TypeCategory typeCategory,
                boolean isStruct,
                @Nullable AssemblyParser.TypeTypeContext superType,
                @Nullable AssemblyParser.TypeListContext interfaces,
                @Nullable AssemblyParser.TypeParametersContext typeParameters,
                List<AssemblyParser.AnnotationContext> annotations, ParserRuleContext ctx,
                Runnable processBody) {
            enterScope(getAttribute(ctx, AsmAttributeKey.classInfo));
            processBody.run();
            exitScope();
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
            enterScope(getAttribute(ctx, AsmAttributeKey.methodInfo));
            processBody.run();
            exitScope();
        }

        public void enterScope(AsmScope scope) {
            this.scope = scope;
        }

        public void exitScope() {
            scope = scope.parent();
        }

        public AsmScope scope() {
            return scope;
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
            setAttribute(ctx, AsmAttributeKey.compilationUnit, new AsmCompilationUnit(pkgName, map));
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
                                 List<AssemblyParser.AnnotationContext> annotations, ParserRuleContext ctx,
                                 Runnable processBody) {
            var code = getCompilationUnit().getDefinitionName(name);
            var klass = findKlass(code);
            var kind = ClassKind.fromTypeCategory(typeCategory);
            boolean searchable = false;
            boolean isBean = false;
            for (AssemblyParser.AnnotationContext annotation : annotations) {
                if(annotation.IDENTIFIER().getText().equals("Component"))
                    isBean = true;
                else if(annotation.IDENTIFIER().getText().equals("Searchable"))
                    searchable = true;
            }
            if (klass == null) {
                klass = createKlass(name, code, kind);
                klass.disableMethodTableBuild();
            } else {
                klass.getConstantPool().clear();
                klass.disableMethodTableBuild();
                if (klass.getKind() == ClassKind.ENUM && kind != ClassKind.ENUM)
                    klass.clearEnumConstantDefs();
                klass.setKind(kind);
                if(!klass.isEnum())
                    klass.setSuperType(null);
            }
            if(isBean) {
                klass.setAttribute(AttributeNames.BEAN_KIND, BeanKinds.COMPONENT);
                klass.setAttribute(AttributeNames.BEAN_NAME, NamingUtils.firstCharToLowerCase(klass.getName()));
            }
            else {
                klass.removeAttribute(AttributeNames.BEAN_KIND);
                klass.removeAttribute(AttributeNames.BEAN_NAME);
            }
            klass.setSearchable(searchable);
            var classInfo = new AsmKlass(
                    scope,
                    getCompilationUnit(), klass,
                    typeParameters != null ?
                            NncUtils.map(typeParameters.typeParameter(), tv -> tv.IDENTIFIER().getText())
                            : List.of(),
                    typeCategory == TypeCategory.ENUM
            );
            setAttribute(ctx, AsmAttributeKey.classInfo, classInfo);
            super.visitTypeDef(name, typeCategory, isStruct, superType, interfaces, typeParameters, annotations, ctx, processBody);
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
        public void visitTypeDef(String name, TypeCategory typeCategory, boolean isStruct, @Nullable AssemblyParser.TypeTypeContext superType, @Nullable AssemblyParser.TypeListContext interfaces, @Nullable AssemblyParser.TypeParametersContext typeParameters, List<AssemblyParser.AnnotationContext> annotations, ParserRuleContext ctx, Runnable processBody) {
            var classInfo = getAttribute(ctx, AsmAttributeKey.classInfo);
            enterScope(classInfo);
            var klass = classInfo.getKlass();
            klass.setStruct(isStruct);
            if (typeCategory.isEnum())
                classInfo.superType = new KlassType(null, getKlass(Enum.class.getName()), List.of(classInfo.getKlass().getType()));
            else if (superType != null)
                classInfo.superType = (ClassType) parseType(superType, classInfo, getCompilationUnit());
            if (classInfo.superType != null)
                klass.setSuperType(classInfo.superType);
            else if(!klass.isEnum())
                klass.setSuperType(null);
            if (interfaces != null)
                klass.setInterfaces(NncUtils.map(interfaces.typeType(), t -> (ClassType) parseType(t, scope, getCompilationUnit())));
            processBody.run();
            if(klass.isEnum())
                classInfo.visitedMethods.add(Flows.saveValuesMethod(klass));
            var removedMethods = NncUtils.exclude(klass.getMethods(), classInfo.visitedMethods::contains);
            removedMethods.forEach(klass::removeMethod);
            var removedFields = NncUtils.exclude(klass.getFields(), classInfo.visitedFields::contains);
            removedFields.forEach(f -> {
                f.setMetadataRemoved();
                f.resetTypeIndex();
            });
            NncUtils.exclude(klass.getStaticFields(), classInfo.visitedFields::contains).forEach(klass::removeField);
            klass.rebuildMethodTable();
            exitScope();
        }

        @Override
        public Void visitFieldDeclaration(AssemblyParser.FieldDeclarationContext ctx) {
            var type = parseType(ctx.typeType(), scope, getCompilationUnit());
            var name = ctx.IDENTIFIER().getText();
            var mods = currentMods();
            var classInfo = (AsmKlass) scope;
            var klass = classInfo.getKlass();
            var isStatic = mods.contains(Modifiers.STATIC);
            var field = isStatic ? klass.findSelfStaticFieldByName(name) : klass.findSelfFieldByName(name);
            if (field == null) {
                field = FieldBuilder.newBuilder(name, klass, type)
                        .tmpId(NncUtils.randomNonNegative())
                        .isChild(mods.contains(Modifiers.CHILD))
                        .isStatic(isStatic)
                        .build();
            } else {
                field.setType(type);
                field.setChild(mods.contains(Modifiers.CHILD));
            }
            field.setAccess(getAccess(mods));
            field.setReadonly(mods.contains(Modifiers.READONLY));
            field.setStatic(isStatic);
            if (mods.contains(Modifiers.TITLE))
                klass.setTitleField(field);
            else if(klass.getSelfTitleField() == field)
                klass.setTitleField(null);
            if(mods.contains(Modifiers.DELETED))
                field.setState(MetadataState.REMOVED);
            else
                field.setState(MetadataState.READY);
            classInfo.visitedFields.add(field);
            return null;
        }

        @Override
        public Void visitEnumConstant(AssemblyParser.EnumConstantContext ctx) {
            var classInfo = (AsmKlass) scope;
            var klass = classInfo.getKlass();
            var name = ctx.IDENTIFIER().getText();
            var field = klass.findStaticField(f -> f.getName().equals(name));
            if (field == null) {
                field = FieldBuilder.newBuilder(name, klass, klass.getType())
                        .tmpId(NncUtils.randomNonNegative())
                        .isStatic(true)
                        .build();
            }
            else
                field.resetTypeIndex();
            classInfo.visitedFields.add(field);
            var enumConstantDef = klass.findEnumConstantDef(ec -> ec.getName().equals(name));
            if(enumConstantDef == null) {
                var initializer = MethodBuilder.newBuilder(klass, "$" + name)
                        .isStatic(true)
                        .returnType(klass.getType())
                        .build();
                enumConstantDef = new EnumConstantDef(klass, name, classInfo.nextEnumConstantOrdinal(), initializer);
            } else
                enumConstantDef.setOrdinal(classInfo.nextEnumConstantOrdinal());
            classInfo.visitedMethods.add(enumConstantDef.getInitializer());
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
            var classInfo = (AsmKlass) scope;
            var klass = classInfo.getKlass();
            List<AssemblyParser.FormalParameterContext> params = formalParameterList != null ? formalParameterList.formalParameter() : List.of();
            Set<String> typeParamNames = typeParameters != null ?
                    NncUtils.mapUnique(typeParameters.typeParameter(), tv -> tv.IDENTIFIER().getText()) : Set.of();
            var paramTypeNames = new ArrayList<String>();
            if(klass.isEnum() && isConstructor) {
                paramTypeNames.add("String");
                paramTypeNames.add("Long");
            }
            params.forEach(p -> paramTypeNames.add(getInternalName(p.typeType(), typeParamNames, scope)));
            var internalName = klass.getQualifiedName() + "." + name + "(" + String.join(",", paramTypeNames) + ")";
            var method = klass.findMethod(m -> m.getInternalName(null).equals(internalName));
            if (method != null)
                method.clearContent();
            else {
                method = MethodBuilder.newBuilder(klass, name)
                        .tmpId(NncUtils.randomNonNegative())
                        .isConstructor(isConstructor)
                        .build();
            }
            classInfo.visitedMethods.add(method);
            method.setStatic(currentMods().contains(Modifiers.STATIC));
            var methodInfo = new AsmMethod(classInfo, method);
            setAttribute(ctx, AsmAttributeKey.methodInfo, methodInfo);
            super.visitFunction(name, typeParameters, formalParameterList, returnType, block, ctx, isConstructor, processBody);
            var parameters = new ArrayList<Parameter>();
            if (isConstructor && classInfo.getKlass().isEnum()) {
                var nameParam = method.findParameter(p -> p.getName().equals("_name"));
                if (nameParam == null) {
                    nameParam = new Parameter(
                            NncUtils.randomNonNegative(),
                            "_name",
                            PrimitiveType.stringType,
                            method
                    );
                }
                else
                    nameParam.setType(PrimitiveType.stringType);
                parameters.add(nameParam);
                var ordinalParam = method.findParameter(p -> p.getName().equals("_ordinal"));
                if (ordinalParam == null) {
                    ordinalParam = new Parameter(
                            NncUtils.randomNonNegative(),
                            "_ordinal",
                            PrimitiveType.intType,
                            method
                    );
                }
                else
                    ordinalParam.setType(PrimitiveType.intType);
                parameters.add(ordinalParam);
            }
            parameters.addAll(parseParameterList(formalParameterList, method, methodInfo, getCompilationUnit()));
            Type retType;
            if (isConstructor) {
                method.setConstructor(true);
                retType = klass.getType();
            } else
                retType = parseType(requireNonNull(returnType), methodInfo, getCompilationUnit());
            method.update(parameters, retType);
        }

        @Override
        public Void visitTypeParameter(AssemblyParser.TypeParameterContext ctx) {
            var genericDecl = scope.getGenericDeclaration();
            var name = ctx.IDENTIFIER().getText();
            var type = NncUtils.find(genericDecl.getTypeParameters(), tv -> tv.getName().equals(name));
            if(type == null)
                type = new TypeVariable(NncUtils.randomNonNegative(), name, genericDecl);
            setAttribute(ctx, AsmAttributeKey.typeVariable, type);
            return super.visitTypeParameter(ctx);
        }

        @Override
        public Void visitIndexDeclaration(AssemblyParser.IndexDeclarationContext ctx) {
            var klass = ((AsmKlass) scope).getKlass();
            var name = ctx.IDENTIFIER().getText();
            var index = klass.findSelfIndex(idx -> idx.getName().equals(name));
            var mods = currentMods();
            if (index == null)
                index = new Index(null, klass, name, null, mods.contains("unique"));
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
        public void visitTypeDef(String name, TypeCategory typeCategory, boolean isStruct, @Nullable AssemblyParser.TypeTypeContext superType, @Nullable AssemblyParser.TypeListContext interfaces, @Nullable AssemblyParser.TypeParametersContext typeParameters, List<AssemblyParser.AnnotationContext> annotations, ParserRuleContext ctx, Runnable processBody) {
            super.visitTypeDef(name, typeCategory, isStruct, superType, interfaces, typeParameters, annotations, ctx, processBody);
            var classInfo = getAttribute(ctx, AsmAttributeKey.classInfo);
            var klass = classInfo.getKlass();
            var removedIndices = NncUtils.exclude(klass.getAllIndices(), classInfo.visitedIndices::contains);
            removedIndices.forEach(klass::removeConstraint);
        }

        @Override
        protected void visitFunction(String name, @Nullable AssemblyParser.TypeParametersContext typeParameters, @Nullable AssemblyParser.FormalParameterListContext formalParameterList, @Nullable AssemblyParser.TypeTypeOrVoidContext returnType, @Nullable AssemblyParser.BlockContext block, ParserRuleContext ctx, boolean isConstructor, Runnable processBody) {
            super.visitFunction(name, typeParameters, formalParameterList, returnType, block, ctx, isConstructor, processBody);
            var methodInfo = getAttribute(ctx, AsmAttributeKey.methodInfo);
            var method = methodInfo.getCallable();
            var classInfo = Objects.requireNonNull(methodInfo.parent());
            var klass = method.getDeclaringType();
            if(!isConstructor
                    && (name.startsWith("idx") || name.startsWith("uniqueIdx"))
                    && method.getParameters().isEmpty()
                    && !method.getReturnType().isVoid()
                    && !method.isStatic()) {
                Index index = NncUtils.find(klass.getAllIndices(), idx -> Objects.equals(idx.getName(), name));
                if (index == null) {
                    index = new Index(
                            klass,
                            name,
                            "",
                            name.startsWith("uniqueIdx"),
                            List.of(),
                            method
                    );
                } else {
                    index.setName(name);
                }
                classInfo.visitedIndices.add(index);
                var indexF = index;
                var keyType = method.getReturnType();
                if(keyType instanceof KlassType ct && ct.isValue()) {
                    ct.forEachField(field -> {
                        if (!field.isStatic() && !field.isTransient()) {
                            var indexField = NncUtils.find(indexF.getFields(), f -> Objects.equals(f.getName(), field.getName()));
                            if (indexField == null)
                                new IndexField(indexF, field.getName(), field.getType(), Values.nullValue());
                            else
                                indexField.setType(field.getType());
                        }
                    });
                } else {
                    var indexField = NncUtils.find(index.getFields(), f -> Objects.equals(f.getName(), "value"));
                    if (indexField == null)
                        new IndexField(index, "value", keyType, Values.nullValue());
                    else
                        indexField.setType(keyType);
                }
            }
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
            var pEnumType = new KlassType(null, getKlass(Enum.class.getName()), List.of(classInfo.getKlass().getType()));
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

    private class AsmGenerator extends VisitorBase implements AsmCodeGenerator {

        private final LinkedList<Method> cinits = new LinkedList<>();
        private int nextNodeNum = 0;

        @Override
        public void visitTypeDef(String name,
                                 TypeCategory typeCategory,
                                 boolean isStruct,
                                 @Nullable AssemblyParser.TypeTypeContext superType,
                                 @Nullable AssemblyParser.TypeListContext interfaces,
                                 @Nullable AssemblyParser.TypeParametersContext typeParameters,
                                 List<AssemblyParser.AnnotationContext> annotations, ParserRuleContext ctx,
                                 Runnable processBody) {
            var currentClass = getAttribute(ctx, AsmAttributeKey.classInfo);
            var klass = currentClass.getKlass();
            enterScope(currentClass);
            var cinit = klass.findSelfMethod(m -> m.getName().equals("__cinit__")
                    && m.isStatic() && m.getParameters().isEmpty());
            if (cinit != null)
                cinit.clearContent();
            else {
                cinit = MethodBuilder.newBuilder(klass, "__cinit__")
                        .isStatic(true)
                        .tmpId(NncUtils.randomNonNegative())
                        .access(Access.PRIVATE)
                        .returnType(PrimitiveType.voidType)
                        .build();
            }
            currentClass.setClassInitializer(new AsmMethod(currentClass, cinit));
            if(klass.isEnum())
                Flows.generateValuesMethodBody(klass);
            cinits.push(cinit);
            processBody.run();
            Nodes.voidRet(cinit.getCode());
            exitScope();
            cinits.pop();
            klass.emitCode();
        }

        private Method cinit() {
            return requireNonNull(cinits.peek());
        }

        private AsmKlass currentClass() {
            var s = scope;
            while (s != null && !(s instanceof AsmKlass)) {
                s = s.parent();
            }
            return Objects.requireNonNull((AsmKlass) s, "Not in any class scope");
        }

        @Override
        public Void visitTypeParameter(AssemblyParser.TypeParameterContext ctx) {
            var typeVariable = getAttribute(ctx, AsmAttributeKey.typeVariable);
            if (ctx.typeType() != null)
                typeVariable.setBounds(List.of(parseType(ctx.typeType(), scope, getCompilationUnit())));
            return null;
        }

        @Override
        protected void visitFunction(String name, @Nullable AssemblyParser.TypeParametersContext typeParameters, @Nullable AssemblyParser.FormalParameterListContext parameterList, @Nullable AssemblyParser.TypeTypeOrVoidContext returnType, @Nullable AssemblyParser.BlockContext block, ParserRuleContext ctx, boolean isConstructor, Runnable processBody) {
            var methodInfo = getAttribute(ctx, AsmAttributeKey.methodInfo);
            enterScope(methodInfo);
            try {
                var klass = currentClass().getKlass();
                var method = methodInfo.getCallable();
                for (Parameter parameter : method.getParameters()) {
                    methodInfo.declareVariable(parameter.getName(), parameter.getType());
                }
                if (!isConstructor) {
                    var supers = new LinkedList<>(superTypes.get(currentClass().getKlass().getName()));
                    while (!supers.isEmpty()) {
                        var s = supers.poll();
                        var overridden = s.findMethod(m -> {
                            if (m.getName().equals(method.getName())
                                    && m.getParameters().size() == method.getParameters().size()
                                    && m.getTypeParameters().size() == method.getTypeParameters().size()
                            ) {
                                if (method.getTypeParameters().isEmpty())
                                    return m.getParameterTypes().equals(method.getParameterTypes());
                                else {
                                    var subst = new TypeSubstitutor(method.getDefaultTypeArguments(), m.getTypeArguments());
                                    var paramTypesSubst = NncUtils.map(method.getParameterTypes(), t -> t.accept(subst));
                                    return paramTypesSubst.equals(m.getParameterTypes());
                                }
                            } else
                                return false;
                        });
                        if (overridden == null)
                            supers.addAll(superTypes.getOrDefault(s.getKlass().getName(), List.of()));
                    }
                }
                var currentClass = currentClass();
                if (block != null) {
                    var code = method.getCode();
                    if (isConstructor && currentClass.isEnum) {
                        Values.node(Nodes.this_(code));
                        Values.node(Nodes.load(1, Types.getStringType(), code));
                        Nodes.setField(StdField.enumName.get().getRef(), code);
                        Values.node(Nodes.this_(code));
                        Values.node(Nodes.load(2, Types.getIntType(), code));
                        Nodes.setField(StdField.enumOrdinal.get().getRef(), code);
                    }
                    processBlock(block, method.getCode());
                    if (isConstructor) {
                        Values.node(Nodes.this_(code));
                        Nodes.ret(code);
                    } else if (Objects.requireNonNull(returnType).VOID() != null)
                        Nodes.voidRet(code);
                }
                if (typeParameters != null)
                    typeParameters.accept(this);
//                if(method.getName().equals("decQuantity")) {
//                    logger.debug("{}", method.getText());
//                }
            } finally {
                exitScope();
            }
        }

        @Override
        public Void visitStaticBlock(AssemblyParser.StaticBlockContext ctx) {
            var asmKlass = currentClass();
            var cinit = Objects.requireNonNull(cinits.peek());
            enterScope(asmKlass.getClassInitializer());
            processBlock(ctx.block(), cinit.getCode());
            exitScope();
            return null;
        }

        @Override
        public Void visitEnumConstant(AssemblyParser.EnumConstantContext ctx) {
            var classInfo = (AsmKlass) scope;
            var klass = classInfo.getKlass();
            var name = ctx.IDENTIFIER().getText();
            var enumConstantDef = Objects.requireNonNull(klass.findEnumConstantDef(ec -> ec.getName().equals(name)));
            List<AssemblyParser.ExpressionContext> argCtx =
                    ctx.arguments() != null ? ctx.arguments().expressionList().expression() : List.of();
            var initializer = enumConstantDef.getInitializer();
            initializer.clearContent();
            enterScope(new AsmMethod(classInfo, initializer));
            var code = initializer.getCode();
            Nodes.loadConstant(Instances.stringInstance(name), code);
            Nodes.loadConstant(Instances.intInstance(enumConstantDef.getOrdinal()), code);
            var types = NncUtils.merge(
                    List.of(Types.getStringType(), Types.getIntType()),
                    NncUtils.map(argCtx, this::parseExpression)
            );
            var constructor = klass.getType().resolveMethod(klass.getName(), types, List.of(), false);
            Nodes.newObject(code, constructor, false, false);
            Nodes.ret(code);
            exitScope();
            return super.visitEnumConstant(ctx);
        }

        public void processBlock(AssemblyParser.BlockContext block, Code code) {
            for (var stmt : block.statement()) {
                processStatement(stmt, code);
            }
        }

        private String nextNodeName() {
            return nextNodeName("__node__");
        }

        private String nextNodeName(String prefix) {
            return prefix + nextNodeNum++;
        }

        private void parseBlockNodes(AssemblyParser.BlockContext block, Code code) {
            block.statement().forEach(s -> processStatement(s, code));
        }

        private void processStatement(AssemblyParser.StatementContext statement, Code code) {
            try {
                if (statement.RETURN() != null) {
                    if(statement.expression() != null) {
                        parseExpression(statement.expression());
                        Nodes.ret(code);
                    }
                    else
                        Nodes.voidRet(code);
                }
                else if(statement.statementExpression != null) {
                    var type = parseExpression(statement.statementExpression);
                    if (type != null && !type.isVoid())
                        Nodes.pop(code);
                } else if (statement.THROW() != null) {
                    parseExpression(statement.expression());
                    Nodes.raise(code);
                } else if (statement.IF() != null) {
                    parseExpression(statement.parExpression().expression());
                    var ifNode = Nodes.ifEq(null, code);
                    parseBlockNodes(statement.block(0), code);
                    var g = Nodes.goto_(code);
                    ifNode.setTarget(Nodes.noop(code));
                    if (statement.ELSE() != null)
                        parseBlockNodes(statement.block(1), code);
                    g.setTarget(Nodes.noop(code));
                }
                else if (statement.WHILE() != null) {
                    var entry = Nodes.noop(code);
                    parseExpression(statement.parExpression().expression());
                    var ifNode = Nodes.ifEq(null, code);
                    parseBlockNodes(statement.block(0), code);
                    var g = Nodes.goto_(code);
                    g.setTarget(entry);
                    var exit = Nodes.noop(code);
                    ifNode.setTarget(exit);
                }
                else if(statement.localVariableDeclaration() != null) {
                    var callable = (AsmCallable) this.scope;
                    var decl = statement.localVariableDeclaration();
                    var name = decl.IDENTIFIER().getText();
                    if(decl.VAR() != null) {
                        var type = parseExpression(decl.expression());
                        var v = callable.declareVariable(name, type);
                        Nodes.store(v.index(), code);
                    } else {
                        var type = parseType(decl.typeType(), callable, callable.getCompilationUnit());
                        var v = callable.declareVariable(name, type);
                        if (decl.expression() != null) {
                            var type1 = parseExpression(decl.expression());
                            if(!type.isAssignableFrom(type1))
                                throw new IllegalStateException("Invalid initializer for variable: " + name);
                            Nodes.store(v.index(), code);
                        }
                    }
                }
                else
                    throw new InternalException("Unknown statement: " + statement.getText());
            } catch (Exception e) {
                throw new InternalException("Fail to process statement: " + statement.getText(), e);
            }
        }

        private Type getExpressionType(Expression expression, Code code) {
            var lastNode = code.getLastNode();
            return lastNode != null ? lastNode.getNextExpressionTypes().getType(expression) : expression.getType();
        }

        private Type parseExpression(AssemblyParser.ExpressionContext expression) {
            return Assembler.this.parseExpression(expression, this);
        }

        private List<Type> parseExpressionList(@Nullable AssemblyParser.ExpressionListContext expressionList) {
            if (expressionList == null)
                return List.of();
            return NncUtils.map(expressionList.expression(), this::parseExpression);
        }

    }

    private class KlassInitializer extends VisitorBase {

        @Override
        public void visitTypeDef(String name, TypeCategory typeCategory, boolean isStruct, @Nullable AssemblyParser.TypeTypeContext superType, @Nullable AssemblyParser.TypeListContext interfaces, @Nullable AssemblyParser.TypeParametersContext typeParameters, List<AssemblyParser.AnnotationContext> annotations, ParserRuleContext ctx, Runnable processBody) {
            var klass = getAttribute(ctx, AsmAttributeKey.classInfo).getKlass();
            try (var serContext = SerializeContext.enter()) {
                serContext.addWritingCodeType(klass);
                generatedKlasses.add(klass);
            }
            super.visitTypeDef(name, typeCategory, isStruct, superType, interfaces, typeParameters, annotations, ctx, processBody);
        }
    }

    private AssemblyParser.CompilationUnitContext parse(String source) {
        var input = CharStreams.fromString(source);
        var parser = new AssemblyParser(new CommonTokenStream(new AssemblyLexer(input)));
        return parser.compilationUnit();
    }

    private AssemblyParser.CompilationUnitContext parse(InputStream in) {
        try {
            var input = CharStreams.fromReader(new InputStreamReader(in));
            var parser = new AssemblyParser(new CommonTokenStream(new AssemblyLexer(input)));
            return parser.compilationUnit();
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to read source input", e);
        }
    }

    private Type parseType(AssemblyParser.TypeTypeOrVoidContext typeTypeOrVoid, AsmScope scope, AsmCompilationUnit compilationUnit) {
        if (typeTypeOrVoid.VOID() != null)
            return PrimitiveType.voidType;
        return parseType(typeTypeOrVoid.typeType(), scope, compilationUnit);
    }

    private Type parseType(AssemblyParser.TypeTypeContext typeType, AsmScope scope, AsmCompilationUnit compilationUnit) {
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

    public static ArrayKind parseArrayKind(AssemblyParser.ArrayKindContext ctx) {
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

    private static Type parsePrimitiveType(AssemblyParser.PrimitiveTypeContext primitiveType) {
        if (primitiveType.INT() != null)
            return PrimitiveType.intType;
        else if (primitiveType.LONG() != null)
            return PrimitiveType.longType;
        else if (primitiveType.SHORT() != null)
            return PrimitiveType.shortType;
        else if (primitiveType.BYTE() != null)
            return PrimitiveType.byteType;
        else if(primitiveType.CHAR() != null)
            return PrimitiveType.charType;
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
            return NullType.instance;
        else if (primitiveType.VOID() != null)
            return PrimitiveType.voidType;
        else
            throw new InternalException("Unknown primitive type");
    }

    private Type parseClassType(AssemblyParser.ClassOrInterfaceTypeContext classOrInterfaceType, AsmScope scope, AsmCompilationUnit compilationUnit) {
        var name = compilationUnit.getReferenceName(classOrInterfaceType.qualifiedName().getText());
        if (!name.contains(".")) {
            var k = scope;
            while (k != null) {
                var found = k.findTypeParameter(name);
                if (found != null)
                    return found.getType();
                k = k.parent();
            }
        }
        List<Type> typeArguments = classOrInterfaceType.typeArguments() != null ? NncUtils.map(
                classOrInterfaceType.typeArguments().typeType(),
                typeType1 -> parseType(typeType1, scope, compilationUnit)
        ) : List.of();
        return new KlassType(null, getKlass(name), typeArguments);
    }

    public Klass getKlass(String name) {
        return requireNonNull(findKlass(name), () -> "Can not find class with name '" + name + "'");
    }

    private @Nullable Klass findKlass(String name) {
        return name2klass.computeIfAbsent(name, klassProvider);
    }

    private Klass createKlass(String name, String qualifiedName, ClassKind kind) {
        var klass = KlassBuilder.newBuilder(name, qualifiedName).kind(kind).tmpId(NncUtils.randomNonNegative()).build();
        name2klass.put(qualifiedName, klass);
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

        public static final AsmAttributeKey<AsmKlass> classInfo = new AsmAttributeKey<>("classInfo", AsmKlass.class);

        public static final AsmAttributeKey<AsmMethod> methodInfo = new AsmAttributeKey<>("methodInfo", AsmMethod.class);

        public static final AsmAttributeKey<Field> field = new AsmAttributeKey<>("field", Field.class);

        public static final AsmAttributeKey<Index> index = new AsmAttributeKey<>("index", Index.class);

        public static final AsmAttributeKey<AsmCompilationUnit> compilationUnit = new AsmAttributeKey<>("compilationUnit", AsmCompilationUnit.class);

        public static final AsmAttributeKey<EnumConstantDef> enumConstantDef = new AsmAttributeKey<>("enumConstantDef", EnumConstantDef.class);

        T cast(Object value) {
            return klass.cast(value);
        }
    }

}
