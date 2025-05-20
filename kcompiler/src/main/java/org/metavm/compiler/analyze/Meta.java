package org.metavm.compiler.analyze;

import lombok.extern.slf4j.Slf4j;
import org.metavm.compiler.element.Attribute;
import org.metavm.compiler.element.NameTable;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.util.CompilationException;
import org.metavm.compiler.util.List;
import org.metavm.entity.AttributeNames;
import org.metavm.entity.BeanKinds;
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
            attrs = attrs.prepend(new Attribute(AttributeNames.BEAN_NAME,
                    NamingUtils.firstCharToLowerCase(clazz.getName().toString())));
        }
        clazz.setAttributes(attrs);
        if (r.tag != null)
            clazz.setSourceTag(r.tag);
        return super.visitClassDecl(classDecl);
    }

    @Override
    public Void visitMethodDecl(MethodDecl methodDecl) {
        methodDecl.getElement().setAttributes(parseAnnotations(methodDecl.getAnnotations()).attributes);
        return super.visitMethodDecl(methodDecl);
    }

    @Override
    public Void visitParamDecl(ParamDecl paramDecl) {
        paramDecl.getElement().setAttributes(parseAnnotations(paramDecl.getAnnotations()).attributes);
        return super.visitParamDecl(paramDecl);
    }

    @Override
    public Void visitFieldDecl(FieldDecl fieldDecl) {
        var r = parseAnnotations(fieldDecl.getAnnotations());
        var field = fieldDecl.getElement();
        field.setAttributes(r.attributes);
        if (r.summary)
            field.setAsSummary();
        return super.visitFieldDecl(fieldDecl);
    }

    @Override
    public Void visitEnumConstDecl(EnumConstDecl enumConstDecl) {
        enumConstDecl.getElement().setAttributes(parseAnnotations(enumConstDecl.getAnnotations()).attributes);
        return super.visitEnumConstDecl(enumConstDecl);
    }

    @Override
    public Void visitClassParamDecl(ClassParamDecl classParamDecl) {
        var r = parseAnnotations(classParamDecl.getAnnotations());
        var field = classParamDecl.getField();
        if (field != null) {
            field.setAttributes(r.attributes);
            if (r.summary)
                field.setAsSummary();
        }
        classParamDecl.getElement().setAttributes(r.attributes);
        return super.visitClassParamDecl(classParamDecl);
    }

    private ParseResult parseAnnotations(List<Annotation> annotations) {
        var attrs = List.<Attribute>builder();
        var summary = false;
        var bean = false;
        Integer tag = null;
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
        }
        return new ParseResult(attrs.build(), summary, bean, tag);
    }

    private record ParseResult(
            List<Attribute> attributes,
            boolean summary,
            boolean bean,
            Integer tag
    ) {}

}