package org.metavm.meta.processor;

import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.Trees;

import javax.annotation.Nullable;
import javax.lang.model.AnnotatedConstruct;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;

class StdKlassBuilderGenerator extends AbstractGenerator {

    private final TypeElement clazz;
    private final Elements elements;
    private final Trees trees;
    private final MyTypes types;
    private final IdStores idStores;

    StdKlassBuilderGenerator(TypeElement clazz, Elements elements, Trees trees, MyTypes types, IdStores idStores) {
        this.elements = elements;
        this.clazz = clazz;
        this.trees = trees;
        this.types = types;
        this.idStores = idStores;
    }

    String generate() {
        writePackageDecl();
        writeImports();
        write("public class ").write(clazz.getSimpleName()).write("__KlassBuilder__")
                .writeln(" implements StdKlassBuilder {");
        indent();
        writeln();
        generateBuild();
        generateGetJavaClass();
        deIndent();
        writeln("}");
        return toString();
    }

    private void generateBuild() {
        writeln("@Override");
        writeln("public Klass build(StdKlassRegistry registry) {");
        indent();
        generateKlassBuild();
        write("registry.addKlass(").write(clazz.getQualifiedName()).write(".class, ").writeln("klass);");
        var superType = clazz.getSuperclass();
        if (superType.getKind() != TypeKind.NONE && isEntity(superType)) {
            write("klass.setSuperType((ClassType) ");
            generateType(superType);
            writeln(");");
        }
        var validInterfaces = clazz.getInterfaces().stream().filter(this::isEntity)
                .map(t -> (DeclaredType) t).toList();
        if (!validInterfaces.isEmpty()) {
            write("klass.setInterfaces(List.of(");
            writeList(validInterfaces, t -> {
                write("(ClassType) ");
                generateType(t);
                },
                    ","
            );
            writeln("));");
        }
        var isApi = clazz.getQualifiedName().toString().startsWith("org.metavm.api.");
        for (Element member : clazz.getEnclosedElements()) {
            if (member.getModifiers().contains(Modifier.TRANSIENT))
                    continue;
            switch (member) {
                case VariableElement field -> {
                    if (member.getModifiers().contains(Modifier.STATIC)) {
                      if (field.asType() instanceof DeclaredType dt && isClassType(dt, "org.metavm.entity.IndexDef"))
                          generateIndexBuild(field);
                    } else {
                        if (isApi)
                            generateFieldBuild(field);
                    }
                }
                case ExecutableElement method -> {
                    if ((isApi || isAnnotatedWith(method, "org.metavm.api.EntityFlow"))
                            && !member.getModifiers().contains(Modifier.STATIC))
                        generateMethodBuild(method);
                }
                default -> {}
            }
        }
        writeln("klass.setStage(ResolutionStage.DECLARATION);");
        generateSetTypeVariableBounds(clazz, "klass");
        writeln("klass.emitCode();");
        writeln("return klass;");
        deIndent();
        writeln("}");
        writeln();
    }

    private void generateKlassBuild() {
        write("var klass = KlassBuilder.newBuilder(");
        generateGetModelId("org.metavm.object.type.Klass", getModelName(clazz));
        write(", \"").write(clazz.getSimpleName()).write("\", \"")
                .write(clazz.getQualifiedName()).writeln("\")");
        indent();
        if (clazz.getKind() == ElementKind.INTERFACE)
            writeln(".kind(ClassKind.INTERFACE)");
        else if (types.isAssignable(clazz.asType(), types.valueObject) || types.isAssignable(clazz.asType(), types.value))
            writeln(".kind(ClassKind.VALUE)");
        writeln(".source(ClassSource.BUILTIN)");
        if (!clazz.getTypeParameters().isEmpty()) {
            write(".typeParameters(List.of(");
            writeList(clazz.getTypeParameters(), this::generateTypeVariableBuild, ", ");
            writeln("))");
        }
        write(".tag(").write(idStores.getTypeTag(clazz.getQualifiedName().toString()) + "").writeln(")");
        var entityAt = findAnnotation(clazz, "org.metavm.api.Entity");
        if (entityAt != null) {
            var sinceAttr = getAnnotationAttribute(entityAt, "since");
            if (sinceAttr != null)
                write(".since(").write(sinceAttr.toString()).writeln(")");
            var ephemeralAttr = getAnnotationAttribute(entityAt, "ephemeral");
            if (ephemeralAttr != null)
                write(".ephemeral(").write(ephemeralAttr.toString()).writeln(")");
            var searchableAttr = getAnnotationAttribute(entityAt, "searchable");
            if (searchableAttr != null)
                write(".searchable(").write(searchableAttr.toString()).writeln(")");
        }
        writeln(".maintenanceDisabled()");
        writeln(".build();");
        deIndent();
    }

    private @Nullable AnnotationMirror findAnnotation(AnnotatedConstruct construct, String fqn) {
        for (AnnotationMirror at : construct.getAnnotationMirrors()) {
            if (isClassType(at.getAnnotationType(), fqn))
                return at;
        }
        return null;
    }

    private Object getAnnotationAttribute(AnnotationMirror annotation, String attribute) {
        for (var e : annotation.getElementValues().entrySet()) {
            if (e.getKey().getSimpleName().contentEquals(attribute)) {
                return e.getValue().getValue();
            }
        }
        return null;
    }

    private boolean isEntity(TypeMirror type) {
        return type instanceof DeclaredType dt && isAnnotatedWith(dt.asElement(), "org.metavm.api.Entity");
    }

    private void generateIndexBuild(VariableElement field) {
        writeln("{");
        indent();
        var tree = (VariableTree) trees.getTree(field);
        if (tree == null)
            throw new IllegalStateException("Failed to get tree for field: " + field.getEnclosingElement().getSimpleName() + "." + field.getSimpleName());
        var init = tree.getInitializer();
        if (init == null)
            throw new EntityConfigException("IndexDef field must be initialized", field);
        write("var index = new Index(");
        generateGetModelId("org.metavm.object.type.Index", getModelName(field));
        write(", klass, \"").write(field.getSimpleName()).write("\", null, ");
        write(isUniqueIndex(init) ? " true" : "false");
        writeln(", Types.getAnyType(), null);");
        write("var indexDef = ").write(clazz.getQualifiedName()).write(".").write(field.getSimpleName()).writeln(";");
        writeln("index.setIndexDef(indexDef);");
        writeln("indexDef.setIndex(index);");
        deIndent();
        writeln("}");
    }

    private boolean isUniqueIndex(ExpressionTree init) {
        if (init instanceof MethodInvocationTree invoke && invoke.getMethodSelect() instanceof MemberSelectTree fn)
            return fn.getIdentifier().contentEquals("createUnique");
        else
            throw new EntityConfigException("Cannot resolve IndexDef initialization", clazz);
    }

    private boolean isClassType(DeclaredType declaredType, String fqn) {
        var clazz = (TypeElement) declaredType.asElement();
        return clazz.getQualifiedName().contentEquals(fqn);
    }

    private void generateFieldBuild(VariableElement field) {
        write("FieldBuilder.newBuilder(\"").write(field.getSimpleName()).write("\", klass, ");
        generateType(field.asType(), isNullable(field));
        writeln(")");
        indent();
        write(".id(");
        generateGetModelId("org.metavm.object.type.Field", getModelName(field));
        writeln(")");
        writeln(".build();");
        deIndent();
    }

    private void generateMethodBuild(ExecutableElement method) {
        writeln("{");
        indent();
        if (!method.getTypeParameters().isEmpty() || !method.getParameters().isEmpty())
            write("var method = ");
        write("MethodBuilder.newBuilder(klass, \"").write(getMethodName(method)).writeln("\")");
        indent();
        write(".id(");
        generateGetModelId("org.metavm.flow.Method", getModelName(method));
        writeln(")");
        if (!method.getTypeParameters().isEmpty()) {
            writeln(".typeParameters(List.of(");
            indent();
            writeList(method.getTypeParameters(), this::generateTypeVariableBuild, ",\n");
            writeln();
            deIndent();
            writeln("))");
        }
        write(".returnType(");
        generateType(method.getReturnType(), isNullable(method));
        writeln(")");
        if (method.getKind() == ElementKind.CONSTRUCTOR)
            writeln(".isConstructor(true)");
        writeln(".isNative(true)");
        writeln(".build();");
        deIndent();
        for (VariableElement parameter : method.getParameters()) {
            write("method.addParameter(");
            generateParameterBuild(parameter);
            writeln(");");
        }
        generateSetTypeVariableBounds(method, "method");
        deIndent();
        writeln("}");
    }

    private void generateSetTypeVariableBounds(Parameterizable parameterizable, String parentName) {
        for (TypeParameterElement typeParam : parameterizable.getTypeParameters()) {
            write(parentName).write(".getTypeParameterByName(\"").write(typeParam.getSimpleName()).write("\").setBounds(List.of(");
            writeList(typeParam.getBounds(), b -> generateType(b, false), ", ");
            writeln("));");
        }
    }

    private static Name getMethodName(ExecutableElement method) {
        if (method.getKind() == ElementKind.CONSTRUCTOR) {
            var cl = (TypeElement) method.getEnclosingElement();
            return cl.getSimpleName();
        } else
            return method.getSimpleName();
    }

    private void generateParameterBuild(VariableElement parameter) {
        write("new Parameter(");
        generateGetModelId("org.metavm.flow.Parameter", getModelName(parameter));
        write(", \"").write(parameter.getSimpleName()).write("\", ");
        generateType(parameter.asType(), isNullable(parameter));
        write(", method)");
    }

    private boolean isNullable(Element element) {
        return isAnnotatedWith(element, "javax.annotation.Nullable");
    }

    private void generateTypeVariableBuild(TypeParameterElement typeParam) {
        write("new TypeVariable(");
        generateGetModelId("org.metavm.object.type.TypeVariable", getModelName(typeParam));
        write(", \"").write(typeParam.getSimpleName()).write("\"");
        write(", DummyGenericDeclaration.INSTANCE)");
    }

    private void generateGetModelId(String className, String name) {
        var id = idStores.getId(className, name);
        write("Id.parse(\"").write(id).write("\")");
    }

    private static String getNullableTypeExpr(TypeMirror type, @Nullable Object current, boolean parenthesized) {
        var expr = getTypeExpr(type, current);
        if (!(type instanceof PrimitiveType)) {
            if (parenthesized) {
                if (expr.compareTo("null") < 0) return "(" + expr + "|null)";
                else return "(null|" + expr + ")";
            } else {
                if (expr.compareTo("null") < 0) return expr + "|null";
                else return "null|" + expr ;
            }
        }
        else return expr;
    }

    private static String getTypeExpr(TypeMirror type, @Nullable Object current) {
        return switch (type.getKind()) {
            case BYTE -> "byte";
            case SHORT -> "short";
            case INT -> "int";
            case LONG -> "long";
            case FLOAT -> "float";
            case DOUBLE -> "double";
            case BOOLEAN -> "boolean";
            case CHAR -> "char";
            case VOID -> "void";
            case DECLARED -> {
                var dt = (DeclaredType) type;
                var cl = (TypeElement) dt.asElement();
                var qualName = cl.getQualifiedName();
                if (qualName.contentEquals("java.lang.Object"))
                    yield "any";
                else if (qualName.contentEquals("java.util.Date"))
                    yield "time";
                else if (qualName.contentEquals("org.metavm.object.type.Klass"))
                    yield "org.metavm.object.type.Klass";
                var name = dt.getEnclosingType().getKind() != TypeKind.NONE ?
                        getModelName(dt.getEnclosingType(), current) + "." + cl.getSimpleName()
                        : cl.getQualifiedName().toString();
                if (dt.getTypeArguments().isEmpty())
                    yield name;
                else {
                    yield name + "<" +
                            Utils.join(dt.getTypeArguments(), t -> getTypeExpr(t, current))
                            + ">";
                }
            }
            case ARRAY -> {
                var arrayType = (ArrayType) type;
                yield getNullableTypeExpr(arrayType.getComponentType(), current, true) + "[]";
            }
            case TYPEVAR -> {
                var tv = (TypeVariable) type;
                yield "@" + getModelName(tv.asElement());
            }
            case WILDCARD -> {
                var wildcardType = (WildcardType) type;
                var lbName = wildcardType.getSuperBound() != null ?
                        getModelName(wildcardType.getSuperBound(), current) : "never";
                var ubName = wildcardType.getExtendsBound() != null ?
                        getModelName(wildcardType.getExtendsBound(), current) : "any";
                yield "[" + lbName + "," + ubName + "]";
            }
            default -> throw new IllegalStateException("Unexpected type kind: " + type.getKind());
        };
    }

    private static String getModelName(Object element) {
        return getModelName(element, null);
    }

    private static String getModelName(Object element, @Nullable Object current) {
        if (current != null && current.equals(element)) return "this";
        return switch (element) {
            case TypeElement cl -> cl.getQualifiedName().toString();
            case ExecutableElement m -> {
                var cl = (TypeElement) m.getEnclosingElement();
                var className = getModelName(cl);
                var typeExpressions = Utils.join(
                        m.getParameters(),
                        p -> getNullableTypeExpr(p.asType(), m, false)
                );
                yield  className + "." + getMethodName(m) + "(" + typeExpressions + ")";
            }
            case VariableElement var -> getModelName(var.getEnclosingElement()) + "." + var.getSimpleName();
            case TypeParameterElement tv -> getModelName(tv.getEnclosingElement(), current) + "." + tv.getSimpleName();
            default -> throw new IllegalArgumentException("Cannot get model identity for " + element.getClass().getName());
        };
    }

    private boolean isAnnotatedWith(Element element, String annotation) {
        var annotations = element.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotations) {
            var cl = (TypeElement) annotationMirror.getAnnotationType().asElement();
            if (cl.getQualifiedName().contentEquals(annotation))
                return true;
        }
        return false;
    }

    private void generateType(TypeMirror type) {
        generateType(type, false);
    }

    private void generateType(TypeMirror type, boolean nullable) {
        if (nullable && !(type instanceof PrimitiveType)) {
            write("Types.getNullableType(");
            generateType(type, false);
            write(")");
            return;
        }
        switch (type.getKind()) {
            case BYTE -> write("Types.getByteType()");
            case SHORT -> write("Types.getShortType()");
            case INT -> write("Types.getIntType()");
            case LONG -> write("Types.getLongType()");
            case FLOAT -> write("Types.getFloatType()");
            case DOUBLE -> write("Types.getDoubleType()");
            case BOOLEAN -> write("Types.getBooleanType()");
            case CHAR -> write("Types.getCharType()");
            case VOID -> write("Types.getVoidType()");
            case DECLARED -> {
                var dt = (DeclaredType) type;
                var cl = (TypeElement) dt.asElement();
                write("registry.");
                if (dt.getTypeArguments().isEmpty())
                    write("getType(").write(cl.getQualifiedName()).write(".class)");
                else {
                    write("getParameterizedType(");
                    if (dt.getEnclosingType().getKind() != TypeKind.NONE)
                        generateType(dt.getEnclosingType());
                    else
                        write("null");
                    write(", ");
                    write(cl.getQualifiedName()).write(".class");
                    write(", List.of(");
                    writeList(dt.getTypeArguments(), this::generateType, ", ");
                    write("))");
                }
            }
            case ARRAY -> {
                var arrayType = (ArrayType) type;
                write("Types.getArrayType(");
                generateType(arrayType.getComponentType());
                write(")");
            }
            case WILDCARD -> {
                var wt = (WildcardType) type;
                write("Types.getUncertainType(");
                if (wt.getSuperBound() != null)
                    generateType(wt.getSuperBound());
                else
                    write("Types.getNeverType()");
                write(", ");
                if (wt.getExtendsBound() != null)
                    generateType(wt.getExtendsBound());
                else
                    write("Types.getNullableAnyType()");
            }
            case INTERSECTION -> {
                var itype = (IntersectionType) type;
                write("Types.getIntersectionType(");
                var boundsIt = itype.getBounds().iterator();
                generateType(boundsIt.next());
                while (boundsIt.hasNext()) {
                    write(", ");
                    generateType(boundsIt.next());
                }
                write(")");
            }
            case UNION -> {
                var utype = (UnionType) type;
                write("Types.getUnionType(");
                var altsIt = utype.getAlternatives().iterator();
                generateType(altsIt.next());
                while (altsIt.hasNext()) {
                    write(", ");
                    generateType(altsIt.next());
                }
                write(")");
            }
            case TYPEVAR -> {
                var tv = (TypeVariable) type;
                var tp = (TypeParameterElement) tv.asElement();
                if (tp.getGenericElement() instanceof TypeElement cl) {
                    write("registry.getTypeVariable(")
                            .write(cl.getQualifiedName()).write(".class, \"")
                            .write(tp.getSimpleName()).write("\")");
                }
                else
                    throw new EntityConfigException("Cannot resolve type variable '" + tp + "'", tp);
            }
            default -> throw new IllegalStateException("Unexpected value: " + type.getKind());
        }
    }

    private void generateGetJavaClass() {
        writeln("@Override");
        writeln("public Class<?> getJavaClass() {");
        indent();
        write("return ").write(clazz.getQualifiedName()).writeln(".class;");
        deIndent();
        writeln("}");
        writeln();
    }

    private void writePackageDecl() {
        var pkg = elements.getPackageOf(clazz);
        if (pkg != null && !pkg.getQualifiedName().contentEquals("")) {
            write("package ").write(pkg.getQualifiedName()).writeln(";");
            writeln();
        }
    }

    private void writeImports() {
        writeImport("org.metavm.entity.StdKlassBuilder");
        writeImport("org.metavm.object.type.KlassBuilder");
        writeImport("org.metavm.object.type.Klass");
        writeImport("org.metavm.object.type.FieldBuilder");
        writeImport("org.metavm.flow.MethodBuilder");
        writeImport("org.metavm.entity.StdKlassRegistry");
        writeImport("org.metavm.entity.ModelIdentity");
        writeImport("org.metavm.object.type.Types");
        writeImport("org.metavm.util.ParameterizedTypeImpl");
        writeImport("org.metavm.flow.NameAndType");
        writeImport("org.metavm.entity.DummyGenericDeclaration");
        writeImport("org.metavm.object.type.TypeVariable");
        writeImport("org.metavm.util.Utils");
        writeImport("org.metavm.flow.Parameter");
        writeImport("org.metavm.object.type.Index");
        writeImport("org.metavm.object.type.ClassType");
        writeImport("org.metavm.object.type.ResolutionStage");
        writeImport("org.metavm.object.type.ClassKind");
        writeImport("org.metavm.object.type.ClassSource");
        writeImport("org.metavm.object.instance.core.Id");
        writeImport("java.util.List");
        writeln();
    }

    private void writeImport(String fqn) {
        write("import ").write(fqn).writeln(";");
    }
    
}
