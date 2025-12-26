package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.*;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.List;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
import org.metavm.entity.NumberFormats;
import org.metavm.util.NamingUtils;

@Slf4j
public class Meta extends StructuralNodeVisitor {

    @Override
    public Void visitClassDecl(ClassDecl classDecl) {
        var clazz = classDecl.getElement();
        clazz.setSearchable(true);
        var r = parseAnnotations(classDecl.getAnnotations());
        var attrs = r.attributes;
        if (r.bean) {
            attrs = attrs.prepend(new Attribute(AttributeNames.BEAN_KIND, BeanKinds.COMPONENT));
            attrs = attrs.prepend(new Attribute(AttributeNames.BEAN_NAME, getBeanName(clazz.getName())));
        }
        else if (r.config) {
            attrs = attrs.prepend(new Attribute(AttributeNames.BEAN_KIND, BeanKinds.CONFIGURATION));
            attrs = attrs.prepend(new Attribute(AttributeNames.BEAN_NAME, getBeanName(clazz.getName())));
        }
        clazz.setAttributes(attrs);
        if (r.tag != null)
            clazz.setSourceTag(r.tag);
        return super.visitClassDecl(classDecl);
    }

    private String getBeanName(Name className) {
        return NamingUtils.firstCharsToLowerCase(className.toString());
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        var r = parseAnnotations(methodDecl.getAnnotations());
        var method = methodDecl.getElement();
        var attrs = r.attributes;
        if (r.bean)
            attrs = attrs.prepend(new Attribute(AttributeNames.BEAN_NAME, getBeanName(method.getName())));
        method.setAttributes(attrs);
        return super.visitMethodDecl(methodDecl);
    }

    @Override
    public Void visitParamDecl(ParamDecl paramDecl) {
        var r = parseAnnotations(paramDecl.getAnnotations());
        paramDecl.getElement().setAttributes(r.attributes);
        return super.visitParamDecl(paramDecl);
    }

    @Override
    public Void visitClassParamDecl(ClassParamDecl classParamDecl) {
        var field = classParamDecl.getField();
        if (field != null) {
            processField(field, classParamDecl.getAnnotations());
            classParamDecl.getElement().setAttributes(field.getAttributes());
        } else {
            var r = parseAnnotations(classParamDecl.getAnnotations());
            classParamDecl.getElement().setAttributes(r.attributes);
        }
        return super.visitClassParamDecl(classParamDecl);
    }

    @Override
    public Void visitFieldDecl(FieldDecl fieldDecl) {
        processField(fieldDecl.getElement(), fieldDecl.getAnnotations());
        return super.visitFieldDecl(fieldDecl);
    }

    private void processField(Field field, List<Annotation> annotations) {
        var r = parseAnnotations(annotations);
        field.setAttributes(r.attributes);
        if (r.summary)
            field.setAsSummary();
        if (r.tag != null)
            field.setSourceTag(r.tag);
    }

    @Override
    public Void visitEnumConstDecl(EnumConstDecl enumConstDecl) {
        enumConstDecl.getElement().setAttributes(parseAnnotations(enumConstDecl.getAnnotations()).attributes);
        return super.visitEnumConstDecl(enumConstDecl);
    }

    private ParseResult parseAnnotations(List<Annotation> annotations) {
        var attrs = List.<Attribute>builder();
        var summary = false;
        var bean = false;
        var config = false;
        Integer tag = null;
        String builtinParam = null;
        for (Annotation annotation : annotations) {
            var aName = annotation.getName();
            if (aName == NameTable.instance.Summary)
                summary = true;
            if (aName == NameTable.instance.Label) {
                if (annotation.extractValue() instanceof String str)
                    attrs.append(new Attribute(AttributeNames.LABEL, str));
                else
                    throw new CompilationException("Invalid label: " + annotation);
            } else if (aName == NameTable.instance.Tag) {
                if (annotation.extractValue() instanceof Integer i)
                    tag = i;
                else
                    throw new CompilationException("Invalid tag: " + annotation);
            } else if (aName == NameTable.instance.Bean)
                bean = true;
            else if (aName == NameTable.instance.Configuration)
                config = true;
            else if (aName == NameTable.instance.Date)
                attrs.append(new Attribute(AttributeNames.NUMBER_FORMAT, NumberFormats.DATE));
            else if (aName == NameTable.instance.CurrentUser)
                attrs.append(new Attribute(AttributeNames.BUILTIN_PARAM, "CurrentUser"));
            else if (aName == NameTable.instance.AuthToken)
                attrs.append(new Attribute(AttributeNames.BUILTIN_PARAM, "AuthToken"));
        }
        return new ParseResult(attrs.build(), summary, bean, config, builtinParam, tag);
    }

    private record ParseResult(
            List<Attribute> attributes,
            boolean summary,
            boolean bean,
            boolean config,
            String builtinParam,
            Integer tag
    ) {}

}