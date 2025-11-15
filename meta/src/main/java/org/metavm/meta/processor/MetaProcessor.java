package org.metavm.meta.processor;

import com.google.auto.service.AutoService;
import com.sun.source.util.Trees;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import lombok.Setter;
import lombok.SneakyThrows;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Function;

@AutoService(Processor.class)
@SupportedSourceVersion(SourceVersion.RELEASE_21)
@SupportedAnnotationTypes("org.metavm.api.Entity")
public class MetaProcessor extends AbstractProcessor {

    static {
        ModuleBreaker.open();
    }

    private Elements elements;
    private Trees trees;
    private MyTypes types;
    private TypeElement apiEntityClass;
    private final Set<TypeElement> classes = new LinkedHashSet<>();
    private IdStores idStores;
    private TreeMaker maker;
    private Names names;
    @Setter
    private Function<ProcessingEnvironment, IdStores> idStoresCreator = DefaultIdStores::new;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processingEnv = jbUnwrap(ProcessingEnvironment.class, processingEnv);
        elements = processingEnv.getElementUtils();
        trees = Trees.instance(processingEnv);
        apiEntityClass = elements.getTypeElement("org.metavm.api.Entity");
        types = new MyTypes(elements, processingEnv.getTypeUtils());
        idStores = idStoresCreator.apply(processingEnv);
        var context = ((JavacProcessingEnvironment) processingEnv).getContext();
        maker = TreeMaker.instance(context);
        names = Names.instance(context);
    }

    private static <T> T jbUnwrap(Class<? extends T> iface, T wrapper) {
        //noinspection DuplicatedCode
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
        var entities = roundEnv.getElementsAnnotatedWith(apiEntityClass);
        for (Element entity : entities) {
            var clazz = (TypeElement) entity;
            if (clazz.getKind() == ElementKind.CLASS)
                transformEntity(clazz);
            generateKlassBuilder(clazz);
        }
        if (roundEnv.processingOver()) {
            generateMapping();
        }
        return false;
    }

    private void transformEntity(TypeElement clazz) {
        processingEnv.getMessager().printNote("Transforming class: " + clazz.getQualifiedName());
        var tree = (JCTree.JCClassDecl) trees.getTree(clazz);
        var trans = new EntityTransformer(tree, maker, names, types);
        trans.transform();
        processingEnv.getMessager().printNote(tree.toString());
    }

    @SneakyThrows
    private void generateKlassBuilder(TypeElement clazz) {
        try {
            if (!classes.add(clazz))
                return;
            var builderFqn = clazz.getQualifiedName() + "__KlassBuilder__";
            var gen = new StdKlassBuilderGenerator(clazz, elements, trees, types, idStores);
            var text = gen.generate();
            var file = processingEnv.getFiler().createSourceFile(builderFqn);
            try (var w = file.openWriter()) {
                w.write(text);
            }
        } catch (EntityConfigException e) {
            var path = trees.getPath(e.getElement());
            trees.printMessage(
                    Diagnostic.Kind.ERROR,
                    e.getMessage(),
                    path.getLeaf(),
                    path.getCompilationUnit()
            );
        }
    }

    @SneakyThrows
    private void generateMapping() {
        var path = "META-INF/services/org.metavm.entity.StdKlassBuilder";
        var existingFile = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", path);
        var existingContent = existingFile.getLastModified() == 0 ? "" : existingFile.getCharContent(false).toString();
        var text = new ServiceMappingGenerator().generate(existingContent, classes);
        try (var w = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", path).openWriter()) {
            w.write(text);
        }
    }

}
