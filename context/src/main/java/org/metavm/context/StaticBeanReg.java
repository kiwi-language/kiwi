package org.metavm.context;

import lombok.SneakyThrows;
import org.metavm.context.http.Controller;

import javax.annotation.Nullable;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.tools.StandardLocation;
import java.util.*;
import java.util.stream.Collectors;

class StaticBeanReg {

    @SneakyThrows
    static StaticBeanReg load(Elements elements, MyTypes types, ProcessingEnvironment processingEnv) {
        var reg = new StaticBeanReg(types);
        var file = processingEnv.getFiler().getResource(StandardLocation.CLASS_OUTPUT, "", ContextProcessor.MAPPING_PATH);
        if (file.getLastModified() > 0) {
            var content = file.getCharContent(true).toString();
            processingEnv.getMessager().printNote("File content:\n" + content);
            var lines = content.split("\n");
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty()) {
                    var cl = elements.getTypeElement(line);
                    if (cl != null) {
                        for (Element e : cl.getEnclosedElements()) {
                            if (e instanceof ExecutableElement m
                                    && m.getSimpleName().contentEquals("getSupportedType")
                                    && m.getParameters().isEmpty()) {
                                if (m.getReturnType() instanceof DeclaredType dt) {
                                    var beanType = (DeclaredType) dt.getTypeArguments().getFirst();
                                    reg.addBean((TypeElement) beanType.asElement());
                                }
                            }
                        }
                    }
                }
            }
        }
        return reg;
    }

    private final Set<TypeElement> beanClasses = new HashSet<>();
    private final Map<String, BeanDef> defs = new LinkedHashMap<>();
    private final MyTypes types;

    StaticBeanReg(MyTypes types) {
        this.types = types;
    }

    String getBeanName(DeclaredType type, @Nullable String qualifier, Element context) {
        BeanDef matched = null;
        for (BeanDef def : defs.values()) {
            if (isBeanApplicable(def.clazz, type)) {
                if (qualifier != null) {
                    if (def.name.equals(qualifier))
                        return def.name;
                } else if (def.primary)
                    return def.name;
                else if (matched == null)
                    matched = def;
                else
                    throw new ContextConfigException("Multiple bean definitions found but none is marked as @Primary", context);
            }
        }
        if (matched == null)
            throw new ContextConfigException("Cannot find bean definition", context);
        return matched.name;
    }

    List<String> getBeanNames(DeclaredType type) {
        return defs.values().stream()
                .filter(def -> isBeanApplicable(def.clazz, type))
                .map(BeanDef::name)
                .collect(Collectors.toList());
    }

    private boolean isBeanApplicable(TypeElement beanCls, DeclaredType type) {
        if (types.isSame(type, types.controller) && beanCls.getAnnotation(Controller.class) != null)
            return true;
        return types.isAssignable(beanCls.asType(), type);
    }

    void addBean(TypeElement cls) {
        if (!beanClasses.add(cls))
            return;
        addBeanDef(new BeanDef(
                BeanDefGenerator.decapitalize(cls.getSimpleName().toString()),
                cls,
                cls.getAnnotation(Primary.class) != null
        ));
        if (cls.getAnnotation(Configuration.class) != null) {
            for (Element mem : cls.getEnclosedElements()) {
                if (mem.getAnnotation(Bean.class) != null) {
                    var m = (ExecutableElement) mem;
                    var retType = m.getReturnType();
                    if (retType instanceof DeclaredType dt) {
                        var cl = (TypeElement) dt.asElement();
                        addBeanDef(new BeanDef(m.getSimpleName().toString(), cl, m.getAnnotation(Primary.class) != null));
                    } else
                        throw new ContextConfigException("@Bean method must return a class type", mem);
                }
            }
        }
    }

    private void addBeanDef(BeanDef beanDef) {
        if (defs.containsKey(beanDef.name))
            throw new ContextConfigException("Duplicate bean name", beanDef.clazz());
        defs.put(beanDef.name(), beanDef);
    }

    private record BeanDef(String name, TypeElement clazz, boolean primary) {
    }

}
