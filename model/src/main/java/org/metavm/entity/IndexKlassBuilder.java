package org.metavm.entity;

import org.metavm.api.Index;
import org.metavm.flow.MethodBuilder;
import org.metavm.flow.Parameter;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.Instances;

import java.util.List;
import java.util.Objects;

public class IndexKlassBuilder implements StdKlassBuilder {

    @Override
    public Klass build(StdKlassRegistry registry) {
        var klass = KlassBuilder.newBuilder(Id.parse("01f8e90100"), "Index", "org.metavm.api.Index")
                .source(ClassSource.BUILTIN)
                .typeParameters(List.of(new TypeVariable(Id.parse("01f8e90102"), "K", DummyGenericDeclaration.INSTANCE), new TypeVariable(Id.parse("01f8e90104"), "V", DummyGenericDeclaration.INSTANCE)))
                .tag(444)
                .maintenanceDisabled()
                .build();
        registry.addKlass(org.metavm.api.Index.class, klass);
        FieldBuilder.newBuilder("name", klass, registry.getType(java.lang.String.class))
                .id(Id.parse("01f8e9012a"))
                .build();
        {
            var method = MethodBuilder.newBuilder(klass, "Index")
                    .id(Id.parse("01f8e90106"))
                    .returnType(Types.getVoidType())
                    .isConstructor(true)
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        self.initField(StdField.indexName.get(), args.getFirst());
                        return self.getReference();
                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("01f8e90108"), "unique", Types.getBooleanType(), method));
            method.addParameter(new Parameter(Id.parse("01f8e9010a"), "keyComputer", registry.getParameterizedType(null, java.util.function.Function.class, List.of(registry.getTypeVariable(org.metavm.api.Index.class, "V"), registry.getTypeVariable(org.metavm.api.Index.class, "K"))), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "Index")
                    .id(Id.parse("01f8e9010c"))
                    .returnType(Types.getVoidType())
                    .isConstructor(true)
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        self.initField(StdField.indexName.get(), args.getFirst());
                        return self.getReference();
                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("01f8e9010e"), "name", registry.getType(java.lang.String.class), method));
            method.addParameter(new Parameter(Id.parse("01f8e90110"), "unique", Types.getBooleanType(), method));
            method.addParameter(new Parameter(Id.parse("01f8e90112"), "keyComputer", registry.getParameterizedType(null, java.util.function.Function.class, List.of(registry.getTypeVariable(org.metavm.api.Index.class, "V"), registry.getTypeVariable(org.metavm.api.Index.class, "K"))), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "query")
                    .id(Id.parse("01f8e9011e"))
                    .returnType(Types.getArrayType(registry.getTypeVariable(org.metavm.api.Index.class, "V")))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var index = getIndex(self);
                        var min = args.get(0);
                        var max = args.get(1);
                        var minKey = index.getRawIndex().createIndexKey(Indexes.getIndexValues(index, min));
                        var maxKey = index.getRawIndex().createIndexKey(Indexes.getIndexValues(index, max));
                        var result = callContext.instanceRepository().indexScan(minKey, maxKey);
                        return convertToArray(self, result);

                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("01f8e90120"), "min", registry.getTypeVariable(org.metavm.api.Index.class, "K"), method));
            method.addParameter(new Parameter(Id.parse("01f8e90122"), "max", registry.getTypeVariable(org.metavm.api.Index.class, "K"), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "count")
                    .id(Id.parse("01f8e90118"))
                    .returnType(Types.getLongType())
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var index = getIndex(self);
                        var min = args.get(0);
                        var max = args.get(1);
                        var minKey = index.getRawIndex().createIndexKey(Indexes.getIndexValues(index, min));
                        var maxKey = index.getRawIndex().createIndexKey(Indexes.getIndexValues(index, max));
                        return Instances.longInstance(callContext.instanceRepository().indexCount(minKey, maxKey));

                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("01f8e9011a"), "min", registry.getTypeVariable(org.metavm.api.Index.class, "K"), method));
            method.addParameter(new Parameter(Id.parse("01f8e9011c"), "max", registry.getTypeVariable(org.metavm.api.Index.class, "K"), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "getAll")
                    .id(Id.parse("01f8e90114"))
                    .returnType(Types.getArrayType(registry.getTypeVariable(org.metavm.api.Index.class, "V")))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var index = getIndex(self);
                        var key = args.getFirst();
                        var indexKey = index.getRawIndex().createIndexKey(Indexes.getIndexValues(index, key));
                        var result = callContext.instanceRepository().indexSelect(indexKey);
                        return convertToArray(self, result);

                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("01f8e90116"), "key", registry.getTypeVariable(org.metavm.api.Index.class, "K"), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "getFirst")
                    .id(Id.parse("01f8e90124"))
                    .returnType(Types.getNullableType(registry.getTypeVariable(org.metavm.api.Index.class, "V")))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var index = getIndex(self);
                        var key = args.getFirst();
                        var indexKey = index.getRawIndex().createIndexKey(Indexes.getIndexValues(index, key));
                        return Objects.requireNonNullElseGet(
                                callContext.instanceRepository().selectFirstByKey(indexKey),
                                Instances::nullInstance
                        );

                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("01f8e90126"), "key", registry.getTypeVariable(org.metavm.api.Index.class, "K"), method));
        }
        {
            var method = MethodBuilder.newBuilder(klass, "getLast")
                    .id(Id.parse("01f8e9012c"))
                    .returnType(Types.getNullableType(registry.getTypeVariable(org.metavm.api.Index.class, "V")))
                    .isNative(true)
                    .nativeFunction((self, args, callContext) -> {
                        var index = getIndex(self);
                        var key = args.getFirst();
                        var indexKey = index.getRawIndex().createIndexKey(Indexes.getIndexValues(index, key));
                        return Objects.requireNonNullElseGet(
                                callContext.instanceRepository().selectLastByKey(indexKey),
                                Instances::nullInstance
                        );

                    })
                    .build();
            method.addParameter(new Parameter(Id.parse("01f8e9012e"), "key", registry.getTypeVariable(org.metavm.api.Index.class, "K"), method));
        }
        {
            MethodBuilder.newBuilder(klass, "isUnique")
                    .id(Id.parse("01f8e90128"))
                    .returnType(Types.getBooleanType())
                    .isNative(true)
                    .build();
        }
        klass.setStage(ResolutionStage.DECLARATION);
        klass.getTypeParameterByName("K").setBounds(List.of(registry.getType(java.lang.Object.class)));
        klass.getTypeParameterByName("V").setBounds(List.of(registry.getType(java.lang.Object.class)));
        klass.emitCode();
        return klass;

    }

    @Override
    public Class<?> getJavaClass() {
        return Index.class;
    }

    private static IndexRef getIndex(ClassInstance instance) {
        var indexName = Instances.toJavaString(instance.getField(StdField.indexName.get()));
        var valueType = (ClassType) instance.getInstanceType().getTypeArguments().get(1);
        return Objects.requireNonNull(
                valueType.findSelfIndex(idx -> idx.getName().equals(indexName)),
                () -> "Cannot find index with name '" + indexName + "' in class " + valueType
        );
    }

    private static Value convertToArray(ClassInstance instance, List<Reference> result) {
        var type = (ClassType) instance.getInstanceType().getTypeArguments().get(1);
        var arrayType = new ArrayType(type, ArrayKind.DEFAULT);
        return new ArrayInstance(arrayType, result).getReference();
    }
}
