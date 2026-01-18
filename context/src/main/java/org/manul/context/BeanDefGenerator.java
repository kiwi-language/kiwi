package org.manul.context;

import org.manul.context.http.Controller;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.util.Elements;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;

class BeanDefGenerator extends AbstractGenerator {

    private final Elements elements;
    private final TypeElement clazz;
    private final AppConfig appConfig;
    private final ProxyManager proxyManager;
    private final StaticBeanReg beanReg;
    private final MyTypes myTypes;
    private final String module;

    BeanDefGenerator(TypeElement clazz, Elements elements, AppConfig appConfig, ProxyManager proxyManager, StaticBeanReg beanReg, MyTypes myTypes) {
        this.elements = elements;
        this.clazz = clazz;
        this.appConfig = appConfig;
        this.proxyManager = proxyManager;
        this.beanReg = beanReg;
        this.myTypes = myTypes;
        module = getModule(clazz);
    }

    String generate() {
        generatePackageDecl();
        generateImports();
        write("public class ").write(clazz.getSimpleName()).write("__BeanDef__ extends ");
        var config = clazz.getAnnotation(Configuration.class);
        if (config != null)
            write("ConfigBeanDefinition");
        else
            write("BeanDefinition");
        write("<").write(clazz.getQualifiedName()).writeln("> {");
        indent();
        writeln();
        generateCreateBean();
        generateInitBean();
        generateGetName();
        generateGetSupportedType(clazz);
        generateGetInitializers();
        generateIsPrimary();
        if (config != null)
            generateCreateInnerDefs();
        if (!module.isEmpty())
            generateGetModule();
        deIndent();
        writeln("}");
        return toString();
    }

    private void generateGetName() {
        String name;
        var comp = clazz.getAnnotation(Component.class);
        if (comp != null && comp.value() != null && !comp.value().isEmpty())
            name = comp.value();
        else {
            var config = clazz.getAnnotation(Configuration.class);
            if (config != null && config.value() != null && !config.value().isEmpty())
                name = config.value();
            else
                name = decapitalize(clazz.getSimpleName().toString());
        }
        generateGetName(name);
    }

    private void generateGetName(String name) {
        writeln("@Override");
        writeln("public String getName() {");
        indent();
        write("return ");
        writeStringLit(name);
        writeln(";");
        deIndent();
        writeln("}");
        writeln();
    }

    private void generateIsPrimary() {
        generateIsPrimary(clazz.getAnnotation(Primary.class) != null);
    }

    private void generateIsPrimary(boolean b) {
        writeln("@Override");
        writeln("public boolean isPrimary() {");
        indent();
        write("return ").write(b).writeln(";");
        deIndent();
        writeln("}");
        writeln();
    }


    static String decapitalize(String s) {
        if (s.length() <= 1)
            return s.toLowerCase();
        else
            return Character.toLowerCase(s.charAt(0)) + s.substring(1);
    }

    private void generateCreateInnerDefs() {
        writeln("@Override");
        writeln("public List<BeanDefinition<?>> createInnerDefinitions() {");
        indent();
        writeln("return List.of(");
        indent();
        boolean first = true;
        for (Element mem : clazz.getEnclosedElements()) {
            if (mem.getKind() == ElementKind.METHOD && mem.getAnnotation(Bean.class) != null) {
                if (first)
                    first = false;
                else
                    writeln(",");
                generateInnerBeanDef((ExecutableElement) mem);
            }
        }
        writeln();
        deIndent();
        writeln(");");
        deIndent();
        writeln("}");
        writeln();
    }

    private void generateInnerBeanDef(ExecutableElement method) {
        if (method.getReturnType() instanceof DeclaredType dt) {
            var cl = (TypeElement) dt.asElement();
            writeln("new BeanDefinition() {");
            indent();
            writeln();
            writeln("@Override");
            write("protected ").write(cl.getQualifiedName()).writeln(" createBean() {");
            indent();
            write("return ").write(clazz.getSimpleName()).write("__BeanDef__.this.getBean().")
                    .write(method.getSimpleName()).write("(");
            writeList(method.getParameters(), this::generateInject, ", ");
            writeln(");");
            deIndent();
            writeln("}");
            writeln();
            generateGetSupportedType(cl);
            generateGetName(method.getSimpleName().toString());
            generateIsPrimary(method.getAnnotation(Primary.class) != null);
            deIndent();
            write("}");
        } else
            throw new ContextConfigException("Inner @Bean method must have class return type", method);
    }

    private void generatePackageDecl() {
        var pkg = elements.getPackageOf(clazz);
        if (!pkg.getQualifiedName().isEmpty()) {
            write("package ").write(pkg.getQualifiedName()).writeln(";");
            writeln();
        }
    }

    private void generateImports() {
        writeImport("java.util.List");
        writeImport("org.manul.context.BeanRegistry");
        writeImport("org.manul.context.BeanDefinition");
        writeImport("javax.annotation.Nullable");
        writeImport("org.manul.context.Initializer");
        if (clazz.getAnnotation(Configuration.class) != null)
            writeImport("org.manul.context.ConfigBeanDefinition");
        writeln();
    }

    private void writeImport(String fqn) {
        write("import ").write(fqn).writeln(";");
    }

    private void generateCreateBean() {
        writeln("@Override");
        write("protected ").write(clazz.getQualifiedName()).writeln(" createBean() {");
        indent();
        write("return new ").write(proxyManager.getProxyName(clazz)).write("(");
        var contr = getContr();
        writeList(contr.getParameters(), this::generateInject, ", ");
        var transactional = ProxyGenerator.getTransactionalElement(clazz);
        if (transactional != null) {
            if (!contr.getParameters().isEmpty())
                write(", ");
            var at = Annotations.getAnnotation(transactional, "org.manul.context.sql.Transactional");
            var txOps = beanReg.getBeanName(myTypes.transactionOperations, null, module, transactional, at);
            write("registry.getBean(org.manul.jdbc.TransactionOperations.class");
            for (String name : txOps) {
                write(", \"").write(name).write("\"");
            }
            write((")"));
        }
        var scheduled = ProxyGenerator.getScheduledElement(clazz);
        if (scheduled != null) {
            if (!contr.getParameters().isEmpty() || transactional != null)
                write(", ");
            var at = Annotations.getAnnotation(scheduled, "org.manul.context.Scheduled");
            write("registry.getBean(org.manul.schedule.Scheduler.class");
            var schedulers = beanReg.getBeanName(myTypes.scheduler, null, module, scheduled, at);
            for (String name : schedulers) {
                write(", \"").write(name).write("\"");
            }
            write((")"));
        }
        writeln(");");
        deIndent();
        writeln("}");
        writeln();
    }

    private void generateGetInitializers() {
        writeln("@Override");
        writeln("public List<Initializer> getInitializers() {");
        indent();
        write("return List.of(");
        var first = true;
        for (Element member : clazz.getEnclosedElements()) {
            var initAt = Annotations.findAnnotation(member, "org.manul.context.Init");
            var scheduledAt = Annotations.findAnnotation(member, "org.manul.context.Scheduled");
            if (initAt != null || scheduledAt != null) {
                var meth = (ExecutableElement) member;
                var atName = initAt != null ? "Init" : "Scheduled";
                if (!meth.getParameters().isEmpty()) {
                    throw new ContextConfigException("@" + atName + " method must not have parameters", meth);
                }
                if (meth.getModifiers().contains(Modifier.PRIVATE)) {
                    throw new ContextConfigException("@" + atName + " method must not be private", meth);
                }

                if (first) {
                    writeln();
                    indent();
                    first = false;
                } else
                    writeln(", ");
                writeln("new Initializer() {");
                indent();
                
                // ----- start run method -----
                writeln("@Override");
                writeln("public void run() {");
                indent();
                if (initAt != null) {
                    write("getBean().").write(member.getSimpleName()).writeln("();");
                } else {
                    var schedulers = beanReg.getBeanName(myTypes.scheduler, null, module, meth, scheduledAt);
                    write("var scheduler = registry.getBean(org.manul.schedule.Scheduler.class, ");
                    writeList(schedulers, this::writeStringLit, ", ");
                    writeln(");");
                    write("scheduler.schedule(getBean()::").write(meth.getSimpleName()).write(", ");
                    var delay = (long) Annotations.getAttribute(scheduledAt, "fixedDelay", -1L);
                    write(Long.toString(delay)).writeln(");");
                }
                deIndent();
                writeln("}");
                writeln();
                // ----- end of run method -----
                
                // ----- start of getOrder method -----
                writeln("@Override");
                writeln("public int getOrder() {");
                indent();
                var order = initAt != null ?
                    (int) Annotations.getAttribute(initAt, "order", Integer.MAX_VALUE) : Integer.MAX_VALUE;
                write("return ").write(Integer.toString(order)).writeln(";");
                deIndent();
                writeln("}");
                writeln();
                deIndent();
                write("}");
                // ----- end of getOrder method -----

            }
        }
        if (!first) {
            writeln();
            deIndent();
        }
        writeln(");");
        deIndent();
        writeln("}");
        writeln();
    }

    private void generateInitBean() {
        writeln("@Override");
        write("protected void initBean(").write(clazz.getQualifiedName()).writeln(" o) {");
        indent();
        for (ExecutableElement setter : getSetters()) {
            write("o.").write(setter.getSimpleName()).write("(");
            generateInject(setter.getParameters().getFirst());
            writeln(");");
        }
        deIndent();
        writeln("}");
        writeln();
    }

    private void generateInject(VariableElement param) {
        if (param.getAnnotation(Value.class) != null)
            generateValueInject(param);
        else
            generateAutowireInject(param);
    }

    private void generateValueInject(VariableElement param) {
        var key = param.getAnnotation(Value.class).value();
        Object value;
        if (key.startsWith("${") && key.endsWith("}")) {
            key = key.substring(2, key.length() - 1);
            value = switch (param.asType().getKind()) {
                case INT -> appConfig.getInt(key, param);
                case LONG -> appConfig.getLong(key, param);
                case BOOLEAN -> appConfig.getBoolean(key, param);
                case DECLARED -> {
                    var dt = (DeclaredType) param.asType();
                    var cl = (TypeElement) dt.asElement();
                    if (cl.getQualifiedName().contentEquals("java.lang.String"))
                        yield appConfig.getString(key, param);
                    else
                        yield appConfig.get(key, param);
                }
                default -> appConfig.get(key, param);
            };
        } else {
            value = switch (param.asType().getKind()) {
                case INT -> Integer.parseInt(key);
                case LONG -> Long.parseLong(key);
                case FLOAT -> Float.parseFloat(key);
                case DOUBLE -> Double.parseDouble(key);
                case BOOLEAN -> Boolean.parseBoolean(key);
                default -> key;
            };
        }
        switch (value) {
            case String s -> writeStringLit(s);
            case Long l -> write(l.toString()).write("l");
            case Float f -> write(f.toString()).write("f");
            default -> write(Objects.toString(value));
        }
    }

    private void generateAutowireInject(VariableElement param) {
        var type = param.asType();
        if (type instanceof DeclaredType declaredType) {
            var cl = (TypeElement) declaredType.asElement();
            if (cl.getQualifiedName().contentEquals("java.util.List")) {
                var elemType = declaredType.getTypeArguments().getFirst();
                if (elemType instanceof DeclaredType elemCt) {
                    var elemCl = (TypeElement) elemCt.asElement();
                    var names = beanReg.getBeanNames(elemCt, module);
                    write("registry.getBeans(").write(elemCl.getQualifiedName()).write(".class, List.of(");
                    writeList(names, this::writeStringLit, ", ");
                    write("))");
                } else
                    throw new ContextConfigException("Cannot autowire List with non-class element type", param);
            } else {
                write("registry.getBean(").write(cl.getQualifiedName()).write(".class, ");
                var qual = param.getAnnotation(Qualifier.class);
                var names = beanReg.getBeanName(declaredType, qual != null ? qual.value() : null, module, param, null);
                writeList(names, this::writeStringLit, ", ");
                write(")");
            }
        } else
            throw new ContextConfigException("Cannot autowire non-class type", param);
    }

    private ExecutableElement getContr() {
        return clazz.getEnclosedElements().stream()
                .filter(e -> e.getKind() == ElementKind.CONSTRUCTOR && !e.getModifiers().contains(Modifier.PRIVATE))
                .map(e -> (ExecutableElement) e)
                .findFirst().orElseThrow();
    }

    private List<ExecutableElement> getSetters() {
        var setters = new ArrayList<ExecutableElement>();
        getSetters(clazz, setters::add);
        return setters;
    }

    private void getSetters(TypeElement cl, Consumer<ExecutableElement> addSetter) {
        if (cl.getSuperclass().getKind() != TypeKind.NONE) {
            var superType = (DeclaredType) cl.getSuperclass();
            var superCl = (TypeElement) superType.asElement();
            if (!superCl.getQualifiedName().contentEquals("java.lang.Object")
                    && !superCl.getQualifiedName().contentEquals("java.lang.Record"))
                getSetters(superCl, addSetter);
        }
        for (Element mem : cl.getEnclosedElements()) {
            if (mem.getKind() == ElementKind.METHOD
                    && !mem.getModifiers().contains(Modifier.PRIVATE)
                    && mem.getAnnotation(Autowired.class) != null) {
                var meth = (ExecutableElement) mem;
                if (meth.getParameters().size() != 1)
                    throw new ContextConfigException("@Autowired method must have exactly one parameter", meth);
                addSetter.accept(meth);
            }
        }
    }

    private void generateGetSupportedType(TypeElement clazz) {
        writeln("@Override");
        write("public Class<").write(clazz.getQualifiedName()).writeln("> getSupportedType() {");
        indent();
        write("return ").write(clazz.getQualifiedName()).writeln(".class;");
        deIndent();
        writeln("}");
        writeln();
    }

    private void generateGetModule() {
        writeln("@Override");
        writeln("public @Nullable String getModule() {");
        indent();
        write("return ");
        writeStringLit(module);
        writeln(";");
        deIndent();
        writeln("}");
        writeln();
    }

    static String getModule(TypeElement clazz) {
        var component = clazz.getAnnotation(Component.class);
        if (component != null)
            return component.module();
        var config = clazz.getAnnotation(Configuration.class);
        if (config != null)
            return config.module();
        var controller = clazz.getAnnotation(Controller.class);
        return controller != null ? controller.module() : "";
    }

}
