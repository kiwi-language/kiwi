package org.metavm.entity;

import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.http.HttpResponseImpl;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.Instances;
import org.metavm.util.Utils;

import java.util.List;

public class HttpResponseImplKlassBuilder implements StdKlassBuilder {
    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("019ced0100"), "HttpResponseImpl", "org.metavm.http.HttpResponseImpl")
                .source(ClassSource.BUILTIN)
                .tag(766)
                .ephemeral(true)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.metavm.http.HttpResponseImpl.class, klass);
        klass.setInterfaces(List.of((ClassType) registry.getType(org.metavm.api.entity.HttpResponse.class)));
        {
            var method = MethodBuilder.newBuilder(klass, "addCookie")
                    .id(Id.parse("019ced010a"))
                    .returnType(Types.getVoidType())
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var r = (HttpResponseImpl) self;
                        var name = args.getFirst();
                        var value = args.get(1);
                        r.addCookie(name.stringValue(), value.stringValue());
                        return Instances.nullInstance();
                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("019ced010c"), "name", registry.getType(java.lang.String.class), method));
            method.addParameter(new Parameter(Id.parse("019ced010e"), "value", registry.getType(java.lang.String.class), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "addHeader")
                    .id(Id.parse("019ced0104"))
                    .returnType(Types.getVoidType())
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var r = (HttpResponseImpl) self;
                        var name = args.getFirst();
                        var value = args.get(1);
                        r.addHeader(name.stringValue(), value.stringValue());
                        return Instances.nullInstance();
                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("019ced0106"), "name", registry.getType(java.lang.String.class), method));
            method.addParameter(new Parameter(Id.parse("019ced0108"), "value", registry.getType(java.lang.String.class), method));
        }
        {
            MethodBuilder.newBuilder(klass, "getCookies")
                    .id(Id.parse("019ced0110"))
                    .returnType(Types.getArrayType(registry.getType(org.metavm.api.entity.HttpCookie.class)))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var r = (HttpResponseImpl) self;
                        return Instances.createArray(Types.getArrayType(StdKlass.httpCookie.type()),
                                Utils.map(r.getCookies(), c -> (Value) c)).getReference();
                    })
                    .build();
        }
        {
            MethodBuilder.newBuilder(klass, "getHeaders")
                    .id(Id.parse("019ced0102"))
                    .returnType(Types.getArrayType(registry.getType(org.metavm.api.entity.HttpHeader.class)))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var r = (HttpResponseImpl) self;
                        return Instances.createArray(Types.getArrayType(StdKlass.httpHeader.type()),
                                Utils.map(r.getHeaders(), h -> (Value) h)).getReference();
                    })
                    .build();
        }
        klass.setStage(ResolutionStage.DECLARATION);
        klass.emitCode();
        return klass;

    }

    @Override
    public Class<?> getJavaClass() {
        return HttpResponseImpl.class;
    }
}
