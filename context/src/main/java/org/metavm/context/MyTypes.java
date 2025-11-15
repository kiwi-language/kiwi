package org.metavm.context;

import org.metavm.context.http.ResponseEntity;

import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.io.InputStream;

public class MyTypes {

    final Types types;
    final DeclaredType initializingBean;
    final DeclaredType controller;
    final DeclaredType string;
    final DeclaredType inputStream;
    final DeclaredType responseEntity;
    final DeclaredType map_string_list_string;

    public MyTypes(Elements elements, Types types) {
        this.types = types;
        var r = new Resolver(elements, types);
        initializingBean = r.resolve(InitializingBean.class);
        controller = r.resolve("org.metavm.server.Controller");
        string = r.resolve(String.class);
        inputStream = r.resolve(InputStream.class);
        responseEntity = r.resolve(ResponseEntity.class);
        var mapCls = elements.getTypeElement("java.util.Map");
        var listCls = elements.getTypeElement("java.util.List");
        var list_string = types.getDeclaredType(listCls, string);
        map_string_list_string = types.getDeclaredType(mapCls, string, list_string);
    }

    boolean isAssignable(TypeMirror from, TypeMirror to) {
        return types.isAssignable(from, to);
    }

    boolean isSame(TypeMirror t1, TypeMirror t2) {
        return types.isSameType(t1, t2);
    }

    private record Resolver(Elements elements, Types types) {

        DeclaredType resolve(Class<?> cls) {
            return resolve(cls.getName());
        }

        DeclaredType resolve(String fqn) {
            return types.getDeclaredType(elements.getTypeElement(fqn));
        }

    }
}
