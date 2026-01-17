package org.manul.context;

import com.sun.source.util.Trees;
import lombok.SneakyThrows;
import org.manul.context.http.Controller;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes({"org.manul.context.Component", "org.manul.context.Configuration", "org.manul.context.http.Controller"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class ContextProcessor extends AbstractProcessor {

    public static final String MAPPING_PATH = "META-INF/services/org.manul.context.BeanDefinition";

    private Elements elements;
    private Messager messager;
    private final Set<TypeElement> classes = new HashSet<>();
    private AppConfig appConfig;
    private final ProxyManager proxyManager = new ProxyManager();
    private StaticBeanReg beanReg;
    private MyTypes myTypes;
    private Trees trees;
    private static final String BEAN_DEF_SUFFIX = "__BeanDef__";

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processingEnv = jbUnwrap(ProcessingEnvironment.class, processingEnv);
        elements = processingEnv.getElementUtils();
        appConfig = AppConfig.load(processingEnv);
        var types = processingEnv.getTypeUtils();
        myTypes = new MyTypes(elements, types);
        beanReg = StaticBeanReg.load(elements, myTypes, processingEnv);
        trees = Trees.instance(processingEnv);
        messager = processingEnv.getMessager();
    }

    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        T unwrapped = null;
        try {
            final Class<?> apiWrappers = wrapper.getClass().getClassLoader().loadClass("org.jetbrains.jps.javac.APIWrappers");
            final Method unwrapMethod = apiWrappers.getDeclaredMethod("unwrap", Class.class, Object.class);
            unwrapped = iface.cast(unwrapMethod.invoke(null, iface, wrapper));
        }
        catch (Throwable ignored) {}
        return unwrapped != null? unwrapped : wrapper;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var elements = roundEnv.getElementsAnnotatedWithAny(Set.of(Component.class, Configuration.class, Controller.class));
        boolean checkPassed = true;
        for (Element element : elements) {
            var cls = (TypeElement) element;
            if (!checkBeanType(cls))
                checkPassed = false;
        }
        if (!checkPassed)
            return false;
        for (Element element : elements) {
            beanReg.addBean((TypeElement) element);
        }
        for (Element element : elements) {
            var clazz = (TypeElement) element;
            if (ProxyGenerator.isTransactional(clazz) || ProxyGenerator.isScheduled(clazz) || ProxyGenerator.isController(clazz))
                generateProxy(clazz);
            generateBeanFactory(clazz);
        }
        if (roundEnv.processingOver())
            generateMapping();
        return false;
    }

    private boolean checkBeanType(TypeElement type) {
        if (type.getKind() == ElementKind.ENUM) {
            printError("bean type cannot be an enum", type, null);
            return false;
        }
        if (type.getModifiers().contains(Modifier.ABSTRACT)) {
            printError("bean type cannot be abstract", type, null);
            return false;
        }
        if (type.getKind() == ElementKind.INTERFACE) {
            printError("bean type cannot be an interface", type, null);
            return false;
        }
        if (type.getEnclosingElement().getKind() != ElementKind.PACKAGE) {
            printError("bean type cannot be an inner/local class", type, null);
            return false;
        }
        return true;
    }

    @SneakyThrows
    private void generateProxy(TypeElement clazz) {
        if (!proxyManager.addProxied(clazz))
            return;
        var gen = new ProxyGenerator(elements, myTypes, clazz);
        var file = processingEnv.getFiler().createSourceFile(clazz.getQualifiedName() + ProxyManager.PROXY_SUFFIX);
        try (var w = file.openWriter()) {
            var text = gen.generate();
            w.write(text);
        } catch (ContextConfigException e) {
            printError(e);
        }
    }

    @SneakyThrows
    private void generateBeanFactory(TypeElement clazz) {
        if (!classes.add(clazz))
            return;
        try {
            var gen = new BeanDefGenerator(clazz, elements, appConfig, proxyManager, beanReg, myTypes);
            var text = gen.generate();
            var file = processingEnv.getFiler().createSourceFile(clazz.getQualifiedName() + "__BeanDef__");
            try (var w = file.openWriter()) {
                w.write(text);
            }
        } catch (ContextConfigException e) {
            printError(e);
        }
    }

    private void printError(ContextConfigException e) {
        printError(e.getMessage(), e.getElement(), e.getAnnotation());
    }

    private void printError(String msg, Element element, AnnotationMirror at) {
        var path = at == null ? trees.getPath(element) : trees.getPath(element, at);
        trees.printMessage(Diagnostic.Kind.ERROR, msg, path.getLeaf(), path.getCompilationUnit());
    }

    @SneakyThrows
    private void generateMapping() {
        var existingFile = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", MAPPING_PATH);
        var existingContent = existingFile.getLastModified() > 0 ? existingFile.getCharContent(true).toString() : "";
        var lines = existingContent.split("\n");
        var existingClasses = new HashSet<String>();
        var buf = new StringBuilder();
        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                var cls = elements.getTypeElement(line);
                if (cls != null) {
                    if (line.endsWith(BEAN_DEF_SUFFIX)) {
                        var beanName = line.substring(0, line.length() - BEAN_DEF_SUFFIX.length());
                        var beanCls = elements.getTypeElement(beanName);
                        if (beanCls == null)
                            continue;
                    }
                    existingClasses.add(line);
                    buf.append(line).append('\n');
                }
            }
        }
        for (var clazz : classes) {
            var factoryName = clazz.getQualifiedName() + BEAN_DEF_SUFFIX;
            if (existingClasses.add(factoryName)) {
                buf.append(factoryName).append('\n');
            }
        }
        var file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", MAPPING_PATH);
        try (var w = file.openWriter()) {
            w.write(buf.toString());
        }
    }

}
