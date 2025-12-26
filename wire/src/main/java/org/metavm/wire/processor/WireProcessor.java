package org.metavm.wire.processor;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Names;
import lombok.SneakyThrows;
import org.metavm.wire.Wire;

import javax.annotation.Nullable;
import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.StandardLocation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

@AutoService(Processor.class)
@SupportedAnnotationTypes("org.metavm.wire.Wire")
@SupportedSourceVersion(SourceVersion.RELEASE_21)
public class WireProcessor extends AbstractProcessor {

    static {
        ModuleBreaker.open();
    }

    private JavacTrees trees;
    private TreeMaker treeMaker;
    private MyNames myNames;
    private MyTypes myTypes;
    private MyClasses myClasses;
    private MyMethods myMethods;
    private Introspects introspects;
    private MyMessenger myMessenger;
    private final Set<TypeElement> classes = new HashSet<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        processingEnv = jbUnwrap(ProcessingEnvironment.class, processingEnv);
        Types types = processingEnv.getTypeUtils();
        Elements elements = processingEnv.getElementUtils();
        var javacEnv = (JavacProcessingEnvironment) processingEnv;
        var ctx = javacEnv.getContext();
        Symtab syms = Symtab.instance(ctx);
        treeMaker = TreeMaker.instance(ctx);
        trees = JavacTrees.instance(ctx);
        Names names = Names.instance(ctx);
        myNames = new MyNames(names);
        myTypes = new MyTypes(elements, types, syms);
        myClasses = new MyClasses(elements);
        myMethods = new MyMethods(myClasses, elements, myTypes, names);
        myMessenger = new MyMessenger(trees);
        introspects = new Introspects(myTypes, trees, myNames, myClasses, myTypes);
    }

    /** @noinspection SameParameterValue*/
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

    @SneakyThrows
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            var elements = roundEnv.getElementsAnnotatedWith(Wire.class);
            for (Element element : elements) {
                var clazz = (Symbol.ClassSymbol) element;
                var tree = trees.getTree(clazz);
                classes.add(clazz);
                var cl = introspects.introspectEntity(clazz);
                if (!cl.symbol().isInterface()) {
                    var constructorGen = new WireTransformer(cl, treeMaker, trees, myNames, myTypes, myClasses, myMethods, introspects);
                    constructorGen.transform();
                }
                if (getCustomAdapter(clazz) == null) {
                    var gen = cl.subTypes().nonEmpty() ?
                            new PolymorphAdapterGenerator(cl, treeMaker, myNames, myTypes, myClasses, myMethods) :
                            new DefaultAdapterGenerator(cl, treeMaker, myNames, myTypes, myClasses);
                    var adapterClassDef = gen.generate();
                    tree.defs = tree.defs.append(adapterClassDef);
                }
//                var text = trees.getPath(clazz).getCompilationUnit().toString();
//                Files.writeString(
//                        Path.of("/Users/leen/workspace/kiwi/entity/src/test/resources/generated/" + clazz.getSimpleName() + ".java"),
//                        text
//                );
            }
            if (roundEnv.processingOver()) {
                generateServiceMapping();
                for (Element e : roundEnv.getRootElements()) {
                    var cu = trees.getPath(e).getCompilationUnit();
                    System.out.println("Transformed:\n" + cu.toString());
                }
            }
            return false;
        } catch (WireConfigException e) {
            myMessenger.error(e.getMessage(), e.getElement());
            return true;
        }
    }

    private @Nullable Symbol.ClassSymbol getCustomAdapter(TypeElement element) {
        var adapter = (Type.ClassType) Annotations.getAttribute(element, myClasses.wire, myNames.adapter);
        if (adapter != null && adapter.tsym != myClasses.wireAdapter)
            return (Symbol.ClassSymbol) adapter.tsym;
        else
            return null;
    }

    @lombok.SneakyThrows
    private void generateServiceMapping() {
        var path = "META-INF/services/org.metavm.wire.WireAdapter";
        var existing = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", path);
        var content = existing.getLastModified() != 0 ? existing.getCharContent(true).toString() : "";
        var lines = content.split("\n");
        var sb = new StringBuilder();
        var blacklist = new HashSet<String>();
        for (TypeElement cl : classes) {
            if (getCustomAdapter(cl) != null) {
                blacklist.add(getGeneratedAdapterName(cl));
            }
        }
        var adapterNames = new HashSet<String>();
        for (String line : lines) {
            var adapterName = line.trim();
            if (!adapterName.isEmpty() && !blacklist.contains(adapterName)) {
                sb.append(adapterName).append('\n');
                adapterNames.add(adapterName);
            }
        }
        for (TypeElement cl : classes) {
            var customAdapter = getCustomAdapter(cl);
            var adapterName = customAdapter != null ?
                    customAdapter.getQualifiedName().toString() : getGeneratedAdapterName(cl);
            if (adapterNames.add(adapterName))
                sb.append(adapterName).append('\n');
        }
        var file = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, "", path);
        try (var w = file.openWriter()) {
            w.write(sb.toString());
        }
    }

    private String getGeneratedAdapterName(TypeElement cl) {
        return cl.getQualifiedName() + "$__WireAdapter__";
    }

}
