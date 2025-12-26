package org.metavm.context;

import org.metavm.context.http.*;
import org.metavm.context.sql.Transactional;

import javax.annotation.Nullable;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.lang.model.util.Elements;

public class ProxyGenerator extends AbstractGenerator {

    private final Elements elements;
    private final MyTypes types;
    private final TypeElement clazz;
    private final String name;
    private final boolean scheduled;
    private final boolean transactional;
    private final @Nullable Controller controller;

    public ProxyGenerator(Elements elements, MyTypes types, TypeElement clazz) {
        this.elements = elements;
        this.types = types;
        this.clazz = clazz;
        name = clazz.getSimpleName() + ProxyManager.PROXY_SUFFIX;
        scheduled = isScheduled(clazz);
        transactional = isTransactional(clazz);
        controller = clazz.getAnnotation(Controller.class);
    }

    static boolean isScheduled(TypeElement clazz) {
        return clazz.getEnclosedElements().stream().anyMatch(e -> e.getAnnotation(Scheduled.class) != null);
    }

    static boolean isTransactional(TypeElement clazz) {
        return clazz.getEnclosedElements().stream().anyMatch(e -> e.getAnnotation(Transactional.class) != null);
    }

    static boolean isController(TypeElement clazz) {
        return clazz.getAnnotation(Controller.class) != null;
    }

    public String generate() {
        writePackageDecl();
        writeImports();
        write("public class ").write(clazz.getSimpleName()).write(ProxyManager.PROXY_SUFFIX).write(" extends ")
                        .write(clazz.getQualifiedName());
        if (controller != null)
            write(" implements org.metavm.server.Controller");
        writeln(" {");
        indent();
        writeln();
        generateFields();
        generateContr();
        for (Element mem : clazz.getEnclosedElements()) {
            if (mem.getAnnotation(Transactional.class) != null) {
                if (mem.getModifiers().contains(Modifier.STATIC))
                    throw new ContextConfigException("@Transactional method cannot be static", mem);
                generateTransactional((ExecutableElement) mem);
            }
        }
        if (controller != null) {
            generateGetRoutes();
            generateGetPath();
            generateThrowSneaky();
        }
        deIndent();
        writeln("}");
        return toString();
    }

    private void generateThrowSneaky() {
        write("""
                @SuppressWarnings("unchecked")
                private static <T extends Throwable> void throwSneaky(Throwable t) throws T {
                    throw (T) t;
                }
                
                """);
    }

    private void writePackageDecl() {
        var pkg = elements.getPackageOf(clazz);
        if (!pkg.getQualifiedName().isEmpty()) {
            write("package ").write(pkg.getQualifiedName()).writeln(";");
            writeln();
        }
    }

    private void generateFields() {
        if (isTransactional(clazz)) {
            writeln("private final org.metavm.jdbc.TransactionOperations __transactionOps__;");
            writeln();
        }
    }

    private void generateContr() {
        var constr = clazz.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR && !e.getModifiers().contains(Modifier.PRIVATE))
                .map(e -> (ExecutableElement) e).findFirst().orElseThrow();
        write("public ").write(name).write("(");
        writeList(constr.getParameters(), p -> {
            write(p.asType().toString());
            write(" ");
            write(p.getSimpleName());
        }, ",");
        if (transactional) {
            if (!constr.getParameters().isEmpty())
                write(", ");
            write("org.metavm.jdbc.TransactionOperations __transactionOps__");
        }
        if (scheduled) {
            if (!constr.getParameters().isEmpty() || transactional)
                write(", ");
            write("org.metavm.schedule.Scheduler __scheduler__");
        }
        writeln(") {");
        indent();
        write("super(");
        writeList(constr.getParameters(), p -> write(p.getSimpleName()), ", ");
        writeln(");");
        if (transactional)
            writeln("this.__transactionOps__ = __transactionOps__;");
        if (scheduled)
            generateSchedules();
        deIndent();
        writeln("}");
        writeln();
    }

    private void generateSchedules() {
        for (Element mem : clazz.getEnclosedElements()) {
            if (mem.getKind() == ElementKind.METHOD && mem.getAnnotation(Scheduled.class) != null) {
                var meth = (ExecutableElement) mem;
                if (!meth.getParameters().isEmpty())
                    throw new ContextConfigException("@Scheduled method cannot have parameters", meth);
                generateSchedule(meth);
            }
        }
    }

    private void generateSchedule(ExecutableElement method) {
        var scheduled = method.getAnnotation(Scheduled.class);
        write("__scheduler__").write(".schedule(this::").write(method.getSimpleName())
                .write(", ").write(scheduled.fixedDelay() + "").writeln(");");
    }

    private void generateGetPath() {
        var mapping = clazz.getAnnotation(Mapping.class);
        if (mapping == null)
            throw new ContextConfigException("Controller class must have @Mapping annotation", clazz);
        writeln("@Override");
        writeln("public String getPath() {");
        indent();
        write("return ").writeStringLit(mapping.value()).writeln(";");
        deIndent();
        writeln("}");
        writeln();
    }

    private void generateGetRoutes() {
        writeln("@Override");
        writeln("public List<RouteConfig> getRoutes() {");
        indent();
        write("return List.of(");
        indent();
        var first = true;
        for (Element member : clazz.getEnclosedElements()) {
            if (member instanceof ExecutableElement meth && isMapped(meth)) {
                if (first)
                    first = false;
                else
                    writeln(",");
                writeln("Route.create(");
                indent();
                String path;
                if (member.getAnnotation(Get.class) != null) {
                    write("HttpMethod.GET");
                    path = member.getAnnotation(Get.class).value();
                } else if (member.getAnnotation(Post.class) != null) {
                    write("HttpMethod.POST");
                    path = member.getAnnotation(Post.class).value();
                } else if (member.getAnnotation(Delete.class) != null) {
                    write("HttpMethod.DELETE");
                    path = member.getAnnotation(Delete.class).value();
                } else
                    throw new ContextConfigException("Unsupported HTTP method", meth);
                if (path.isEmpty())
                    path = "/";
                write(", ").writeStringLit(path).writeln(",");
                writeln("r -> {");
                indent();
                writeln("try {");
                indent();
                generateHandleResult(meth);
                deIndent();
                writeln("} catch(Exception __e__) {");
                indent();
                write("throwSneaky(__e__);");
                deIndent();
                writeln("}");
                deIndent();
                writeln("}");
                deIndent();
                writeln(")");
            }
        }
        writeln();
        deIndent();
        writeln(");");
        deIndent();
        writeln("}");
        writeln();
    }

    private void generateHandleResult(ExecutableElement meth) {
        var hasResult = meth.getReturnType().getKind() != TypeKind.VOID;
        if (hasResult)
            write("var rs = ");
        write(meth.getSimpleName()).write("(");
        writeList(meth.getParameters(), this::writeParam, ", ");
        writeln(");");
        if (hasResult) {
            if (types.isAssignable(meth.getReturnType(), types.responseEntity)) {
                writeln("if (rs != null) {");
                indent();
                generateWriteResponse("rs");
                deIndent();
                writeln("}");
            } else if (types.isAssignable(types.responseEntity, meth.getReturnType())) {
                writeln("if (rs instanceof ResponseEntity<?> resp) {");
                indent();
                generateWriteResponse("resp");
                deIndent();
                writeln("} else if (rs != null) {");
                indent();
                writeln("r.writeBody(rs);");
                deIndent();
                writeln("}");
            } else if (meth.getReturnType() instanceof PrimitiveType) {
                writeln("r.writeBody(rs);");
            } else {
                writeln("if (rs != null) {");
                indent();
                writeln("r.writeBody(rs);");
                deIndent();
                writeln("}");
            }
        }
    }

    private void generateWriteResponse(String name) {
        write("if (").write(name).writeln(".data() != null) {");
        indent();
        write("r.writeBody(").write(name).writeln(".data());");
        deIndent();
        writeln("}");
        write("for (var h : ").write(name).writeln(".headers().entrySet()) {");
        indent();
        writeln("r.addHeader(h.getKey(), h.getValue());");
        deIndent();
        writeln("}");
    }

    private boolean isMapped(ExecutableElement meth) {
        return meth.getAnnotation(Get.class) != null || meth.getAnnotation(Post.class) != null ||
                meth.getAnnotation(Delete.class) != null;
    }

    private void writeParam(VariableElement param) {
        RequestParam requestParam;
        PathVariable pathVar;
        if ((requestParam = param.getAnnotation(RequestParam.class)) != null)
            writeParse(param, () -> writeGetQueryParam(requestParam));
        else if ((pathVar = param.getAnnotation(PathVariable.class)) != null)
            writeParse(param, () -> writeGetPathVar(pathVar));
        else if (param.getAnnotation(RequestBody.class) != null)
            writeGetRequestBody(param.asType());
        else if (param.getAnnotation(ClientIP.class) != null) {
            if (!types.isSame(param.asType(), types.string))
                throw new ContextConfigException("@ClientIP parameter must be of type String", param);
            write("r.getClientIP()");
        } else if (param.getAnnotation(Header.class) != null) {
            if (!types.isSame(param.asType(), types.string))
                throw new ContextConfigException("@Header parameter must be of type String", param);
            var header = param.getAnnotation(Header.class);
            write("r.getHeader(");
            writeStringLit(header.value());
            write(")");
        } else if (param.getAnnotation(Headers.class) != null) {
            if (!types.isSame(param.asType(), types.map_string_list_string))
                throw new ContextConfigException("@Headers parameter must be of type Map<String, List<String>>", param);
            write("r.getHeaders()");
        } else if(param.getAnnotation(RequestURI.class) != null) {
            if (!types.isSame(param.asType(), types.string))
                throw new ContextConfigException("@RequestURI parameter must be of type String", param);
            write("r.getRequestURI()");
        } else if (types.isSame(param.asType(), types.inputStream))
            write("r.getBody()");
        else
            throw new ContextConfigException("Unsupported parameter annotation for parameter", param);
    }

    private void writeParse(VariableElement param, Runnable writeValue) {
        var type = param.asType();
        if (types.isString(type)) {
            writeValue.run();
            return;
        }
        write("RequestUtil.");
        switch (type.getKind()) {
            case BYTE -> write("parseByte");
            case SHORT -> write("parseShort");
            case INT -> write("parseInt");
            case LONG -> write("parseLong");
            case FLOAT -> write("parseFloat");
            case DOUBLE -> write("parseDouble");
            case BOOLEAN -> write("parseBoolean");
            case CHAR -> write("parseChar");
            case DECLARED -> {
                var dt = (DeclaredType) type;
                var cl = (TypeElement) dt.asElement();
                switch (cl.getQualifiedName().toString()) {
                    case "java.lang.Byte" -> write("byteValueOf");
                    case "java.lang.Short" -> write("shortValueOf");
                    case "java.lang.Integer" -> write("integerValueOf");
                    case "java.lang.Long" -> write("longValueOf");
                    case "java.lang.Float" -> write("floatValueOf");
                    case "java.lang.Double" -> write("doubleValueOf");
                    case "java.lang.Boolean" -> write("booleanValueOf");
                    case "java.lang.Character" -> write("characterValueOf");
                    default -> throw new ContextConfigException("Cannot parse parameter of type: " + type, param);
                }
            }
        }
        write("(");
        writeValue.run();
        write(")");
    }

    private void writeGetQueryParam(RequestParam requestParam) {
        if (!requestParam.required() && requestParam.defaultValue().isEmpty())
            write("r.getOptionalQueryParameter(");
        else
            write("r.getQueryParameter(");
        writeStringLit(requestParam.value());
        if (!requestParam.defaultValue().isEmpty()) {
            write(", ");
            writeStringLit(requestParam.defaultValue());
        }
        write(")");
    }

    private void writeGetPathVar(PathVariable param) {
        write("r.getPathVariable(");
        writeStringLit(param.value());
        write(")");
    }

    private void writeGetRequestBody(TypeMirror type) {
        write("(");
        writeType(type);
        write(") ");
        write("r.readRequestBody(");
        writeJsonkType(type);
        write(")");
    }

    private void writeType(TypeMirror type) {
        writeType(type, false);
    }

    private void writeType(TypeMirror type, boolean erase) {
        switch (type) {
            case PrimitiveType primitiveType -> write(primitiveType.toString());
            case DeclaredType dt -> {
                var cl = (TypeElement) dt.asElement();
                write(cl.getQualifiedName());
                if (!erase && !dt.getTypeArguments().isEmpty()) {
                    write("<");
                    writeList(dt.getTypeArguments(), this::writeType, ", ");
                    write(">");
                }
            }
            case ArrayType arrayType -> {
                writeType(arrayType.getComponentType(), erase);
                write("[]");
            }
            case TypeVariable tv -> {
                if (erase)
                    writeType(tv.getUpperBound(), true);
                else {
                    var typeParam = (TypeParameterElement) tv.asElement();
                    write(typeParam.getSimpleName());
                }
            }
            default -> throw new IllegalStateException("Cannot write raw type for: " + type);
        }
    }

    private void writeJsonkType(TypeMirror type) {
        switch (type) {
            case PrimitiveType primitiveType -> {
                write("Type.from(");
                write(primitiveType.toString());
                write(".class)");
            }
            case DeclaredType dt -> {
                var cl = (TypeElement) dt.asElement();
                write("Type.from(");
                write(cl.getQualifiedName());
                write(".class");
                for (TypeMirror typeArg : dt.getTypeArguments()) {
                    write(", ");
                    writeJsonkType(typeArg);
                }
                write(")");
            }
            case ArrayType arrayType -> {
                writeType(arrayType.getComponentType(), true);
                write("[].class");
            }
            case TypeVariable tv -> writeJsonkType(tv.getUpperBound());
            case IntersectionType itType -> writeJsonkType(itType.getBounds().getFirst());
            default -> throw new IllegalStateException("Cannot write Jsonk type for: " + type);
        }
    }

    private void writeImports() {
        writeImport("org.metavm.server.Route");
        writeImport("org.metavm.server.RouteConfig");
        writeImport("org.metavm.server.HttpMethod");
        writeImport("org.metavm.context.http.ResponseEntity");
        writeImport("org.jsonk.Type");
        writeImport("java.util.List");
        writeImport("org.metavm.server.RequestUtil");
        writeln();
    }

    private void writeImport(String imp) {
        write("import ").write(imp).writeln(";");
    }

    private void generateTransactional(ExecutableElement method) {
        if (method.getModifiers().contains(Modifier.PUBLIC))
            write("public ");
        else if (method.getModifiers().contains(Modifier.PROTECTED))
            write("protected ");
        else if (method.getModifiers().contains(Modifier.PRIVATE))
            throw new ContextConfigException("@Transactional method cannot be private", method);
        write(method.getReturnType().toString()).write(" ").write(method.getSimpleName()).write("(");
        writeList(method.getParameters(), p -> {
            write(p.asType().toString());
            write(" ");
            write(p.getSimpleName());
        }, ", ");
        writeln(") {");
        indent();
        var tx = method.getAnnotation(Transactional.class);
        var hasReturn = method.getReturnType().getKind() != TypeKind.VOID;
        if (hasReturn)
            write("return ");
        write("__transactionOps__.execute(");
        write("() -> super.").write(method.getSimpleName()).write("(");
        writeList(method.getParameters(), p -> write(p.getSimpleName()), ", ");
        write("), ")
                .write(tx.readonly()).write(", ")
                .write("org.metavm.context.sql.TransactionPropagation.").write(tx.propagation().name())
                .writeln(");");
        deIndent();
        writeln("}");
        writeln();
    }

}
