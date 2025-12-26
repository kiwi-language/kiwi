package org.metavm.context;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import lombok.SneakyThrows;
import org.metavm.context.http.Controller;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@SupportedAnnotationTypes({"org.metavm.context.Component", "org.metavm.context.Configuration", "org.metavm.context.http.Controller"})
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@AutoService(Processor.class)
public class ContextProcessor extends AbstractProcessor {

    public static final String MAPPING_PATH = "META-INF/services/org.metavm.context.BeanDefinition";

    private Elements elements;
    private final Set<TypeElement> classes = new HashSet<>();
    private AppConfig appConfig;
    private final ProxyManager proxyManager = new ProxyManager();
    private StaticBeanReg beanReg;
    private MyTypes myTypes;
    private Trees trees;

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
            var path = trees.getPath(e.getElement());
            trees.printMessage(Diagnostic.Kind.ERROR, e.getMessage(), path.getLeaf(), path.getCompilationUnit());
        }
    }

    @SneakyThrows
    private void generateBeanFactory(TypeElement clazz) {
        if (!classes.add(clazz))
            return;
        try {
            var gen = new BeanDefGenerator(elements, clazz, appConfig, proxyManager, beanReg, myTypes);
            var text = gen.generate();
            var file = processingEnv.getFiler().createSourceFile(clazz.getQualifiedName() + "__BeanDef__");
            try (var w = file.openWriter()) {
                w.write(text);
            }
        } catch (ContextConfigException e) {
            var path = trees.getPath(e.getElement());
            trees.printMessage(Diagnostic.Kind.ERROR,
                    e.getMessage(),
                    path.getLeaf(),
                    path.getCompilationUnit()
            );
        }
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
                existingClasses.add(line);
                buf.append(line).append('\n');
            }
        }
        for (var clazz : classes) {
            var factoryName = clazz.getQualifiedName() + "__BeanDef__";
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
