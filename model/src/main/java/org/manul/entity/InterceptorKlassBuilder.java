package org.manul.entity;

import org.manul.flow.MethodBuilder;
import org.manul.flow.Parameter;
import org.manul.object.instance.core.Id;
import org.manul.object.type.*;

public class InterceptorKlassBuilder implements StdKlassBuilder {

    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01fae90100"), "Interceptor", "org.manul.api.Interceptor")
                .kind(ClassKind.INTERFACE)
                .source(ClassSource.BUILTIN)
                .tag(445)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.manul.api.Interceptor.class, klass);
        {
            var method = MethodBuilder.newBuilder(klass, "before")
                    .id(Id.parse("01fae90102"))
                    .returnType(Types.getVoidType())
                    .isNative(true)
                    .build();
            method.addParameter(new Parameter(Id.parse("01fae90104"), "request", registry.getType(org.manul.api.entity.HttpRequest.class), method));
            method.addParameter(new Parameter(Id.parse("01fae90106"), "response", registry.getType(org.manul.api.entity.HttpResponse.class), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "after")
                    .id(Id.parse("01fae90108"))
                    .returnType(Types.getNullableType(registry.getType(java.lang.Object.class)))
                    .isNative(true)
                    .build();
            method.addParameter(new Parameter(Id.parse("01fae9010a"), "request", registry.getType(org.manul.api.entity.HttpRequest.class), method));
            method.addParameter(new Parameter(Id.parse("01fae9010c"), "response", registry.getType(org.manul.api.entity.HttpResponse.class), method));
            method.addParameter(new Parameter(Id.parse("01fae9010e"), "result", Types.getNullableType(registry.getType(java.lang.Object.class)), method));
        }
        klass.setStage(ResolutionStage.DECLARATION);
        klass.emitCode();
        return klass;
    }

    @Override
    public Class<?> getJavaClass() {
        return org.manul.api.Interceptor.class;
    }

}
