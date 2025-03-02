package org.metavm.classfile;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.*;
import org.metavm.flow.*;
import org.metavm.object.instance.core.IntValue;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.StringReference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.*;
import org.metavm.util.DebugEnv;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

@Slf4j
public class ClassFileReader {

    private final MvInput input;
    private final EntityRepository repository;
    private final @Nullable ClassFileListener listener;
    private final boolean tracing = DebugEnv.traceClassFileIO;


    public ClassFileReader(MvInput input, EntityRepository repository, @Nullable ClassFileListener listener) {
        this.input = input;
        this.repository = repository;
        this.listener = listener;
    }

    public Klass read() {
        return readKlass(null);
    }

    public Klass readKlass(@Nullable KlassDeclaration parent) {
        var tracing = this.tracing;
        var sourceTag = input.readNullable(input::readInt);
        var name = input.readUTF();
        var qualName = input.readNullable(input::readUTF);
        if (tracing) log.trace("Reading class {}", qualName != null ? qualName : name);
        Klass existing;
        if (sourceTag != null)
            existing = repository.selectFirstByKey(Klass.UNIQUE_SOURCE_TAG, Instances.intInstance(sourceTag));
        else if (parent == null)
            existing = repository.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(Objects.requireNonNull(qualName)));
        else
            existing = Utils.find(parent.getKlasses(), k -> k.getName().equals(name));
        if (tracing && existing != null)
            log.trace("Found existing class {} for '{}'", existing.getQualifiedName(), qualName);
        Klass klass;
        if (existing == null) {
            var id = parent != null ? ((Entity) parent).getRoot().nextChildId() :
                    PhysicalId.of(repository.allocateTreeId(), 0L);
            klass = KlassBuilder.newBuilder(id, name, qualName)
                    .sourceTag(sourceTag)
                    .scope(parent)
                    .build();
            repository.bind(klass);
            if (listener != null) listener.beforeKlassCreate();
        }
        else {
            klass = existing;
            klass.setQualifiedName(qualName);
            klass.setName(name);
            klass.incVersion();
            if (listener != null) listener.beforeKlassUpdate(klass);
        }
        klass.disableMethodTableBuild();
        var klassF = klass;
        readConstantPool(klass.getConstantPool());
        klass.setKind(ClassKind.fromCode(input.read()));
        klass.setClassFlags(input.readInt());
        klass.setSince(input.readInt());
        klass.setSuperTypeIndex(input.readNullable(input::readInt));
        klass.setInterfaceIndexes(input.readList(input::readInt));
        klass.setTypeParameters(input.readList(() -> readTypeVariable(klassF)));
        klass.setFields(input.readList(() -> readField(klassF)));
        klass.setStaticFields(input.readList(() -> readField(klassF)));
        klass.setMethods(input.readList(() -> readMethod(klassF)));
        klass.setKlasses(input.readList(() -> readKlass(klassF)));
        klass.setAttributes(input.readList(() -> Attribute.read(input)));
        klass.setIndices(Utils.mapAndFilter(klass.getStaticFields(), this::addIndex, Objects::nonNull));
        if (listener != null) {
            if (klass.isNew()) listener.onKlassCreate(klass);
            else listener.onKlassUpdate(klass);
        }
        return klass;
    }

    private TypeVariable readTypeVariable(GenericDeclaration genericDeclaration) {
        var name = input.readUTF();
        var typeVariable = Utils.find(genericDeclaration.getTypeParameters(), tv -> tv.getName().equals(name));
        if (typeVariable == null) {
            var root = ((Entity) genericDeclaration).getRoot();
            typeVariable = new TypeVariable(root.nextChildId(), name, genericDeclaration);
        }
        typeVariable.setBoundIndexes(input.readList(input::readInt));
        typeVariable.setAttributes(input.readList(() -> Attribute.read(input)));
        return typeVariable;
    }

    private void readConstantPool(ConstantPool constantPool) {
        constantPool.clear();
        var numEntries = input.readInt();
        for (int i = 0; i < numEntries; i++) {
            constantPool.addEntry(readConstant());
        }
    }

    private Value readConstant() {
        return input.readValue();
    }

    private Method readMethod(Klass klass) {
        var tracing = this.tracing;
        var internalName = input.readUTF();
        if (tracing) log.trace("Reading method {}", internalName);
        var name = input.readUTF();
        var returnTypeIndex = input.readInt();
        var existing = Utils.find(klass.getMethods(), m -> Objects.equals(m.tryGetInternalName(), internalName));
        Method method;
        if (existing != null) {
            existing.setName(name);
            existing.setReturnTypeIndex(returnTypeIndex);
            method = existing;
        }
        else
            method = MethodBuilder.newBuilder(klass, name).returnTypeIndex(returnTypeIndex).build();
        method.setInternalName(internalName);
        method.setFlags(input.readInt());
        readConstantPool(method.getConstantPool());
        method.setParameters(input.readList(() -> readParameter(method)));
        method.setTypeParameters(input.readList(() -> readTypeVariable(method)));
        method.setCapturedTypeVariables(input.readList(() -> readCapturedTypeVariable(method)));
        method.setLambdas(input.readList(() -> readLambda(method)));
        method.setKlasses(input.readList(() -> readKlass(method)));
        method.setAttributes(input.readList(() -> Attribute.read(input)));
        if (method.hasBody()) readCode(method.getCode());
        if (listener != null) listener.onMethodRead(method);
        return method;
    }

    private CapturedTypeVariable readCapturedTypeVariable(CapturedTypeScope scope) {
        var root = ((Entity) scope).getRoot();
        var name = input.readUTF();
        var ct = Utils.find(scope.getCapturedTypeVariables(), t -> t.getName().equals(name));
        if (ct == null) {
            ct = new CapturedTypeVariable(
                    root.nextChildId(),
                    name,
                    input.readInt(),
                    input.readReference(),
                    scope
            );
        }
        else {
            ct.setUncertainTypeIndex(input.readInt());
            ct.setTypeVariable(input.readReference());
        }
        ct.setAttributes(input.readList(() -> Attribute.read(input)));
        return ct;
    }

    private Field readField(Klass klass) {
        var tracing = this.tracing;
        var sourceTag = input.readNullable(input::readInt);
        var name = input.readUTF();
        if (tracing) log.trace("Reading field {}.{}", klass.getQualifiedName(), name);
        Field existing;
        if (sourceTag != null)
            existing = klass.findSelfField(f -> Objects.equals(f.getSourceTag(), sourceTag));
        else
            existing = klass.findSelfFieldByName(name);
        var typeIndex = input.readInt();
        var acc = Access.fromCode(input.read());
        var flags = input.readInt();
        Field field;
        if (existing != null) {
            if (listener != null) listener.beforeFieldUpdate(existing);
            existing.setName(name);
            existing.setTypeIndex(typeIndex);
            field = existing;
        }
        else {
            field = FieldBuilder.newBuilder(name, klass, typeIndex)
                    .sourceTag(sourceTag)
                    .isStatic((flags & Field.FLAG_STATIC) != 0)
                    .build();
        }
        field.setAccess(acc);
        field.setFlags(flags);
        field.setOrdinal(input.readInt());
        field.setSince(input.readInt());
        field.setInitializerReference(input.readNullable(input::readReference));
        var deleted = input.readBoolean();
        if (deleted) field.setState(MetadataState.REMOVED);
        else if (field.getState() == MetadataState.REMOVED) field.setState(MetadataState.READY);
        if (listener != null) {
            if (field.isNew()) listener.onFieldCreate(field);
            else listener.onFieldUpdate(field);
        }
        return field;
    }

    private Index addIndex(Field field) {
        var type = field.getType().getUnderlyingType();
        if (type instanceof ClassType classType && classType.getKlass() == StdKlass.index.get()) {
            var initializer = Objects.requireNonNull(field.getInitializer());
            var keyType = classType.getTypeArguments().getFirst();
            if(initializer.getCode().getNodes().isEmpty())
                initializer.getCode().rebuildNodes();
            var nodes = initializer.getCode().getNodes();
            var ldc = (LoadConstantNode) nodes.get(1);
            var ldc1 = (LoadConstantNode) nodes.get(2);
            var func = nodes.get(3);
            var name = ((StringReference) ldc.getValue()).getValue();
            var unique = ((IntValue) ldc1.getValue()).getValue() != 0;
            Method keyComputeMethod;
            switch (func) {
                case LambdaNode lambdaNode -> {
                    var lambda = lambdaNode.getLambda();
                    if(lambda.getCode().getNodes().isEmpty())
                        lambda.getCode().rebuildNodes();
                    var invokeNode1 = (InvokeNode) lambda.getCode().getNodes().get(1);
                    keyComputeMethod = (Method) invokeNode1.getFlowRef().getRawFlow();
                }
                case GetStaticMethodNode getStaticMethodNode ->
                        keyComputeMethod = getStaticMethodNode.getMethodRef().getRawFlow();
                case GetMethodNode getMethodNode -> keyComputeMethod = getMethodNode.getMethodRef().getRawFlow();
                default -> throw new IllegalStateException("Unrecognized function node: " + func);
            }
            var klass = field.getDeclaringType();
            var existing = klass.findSelfIndex(idx -> idx.getName().equals(name));

            if (existing == null) {
                return new Index(
                        klass.nextChildId(),
                        klass,
                        name,
                        null,
                        unique,
                        keyType,
                        keyComputeMethod
                );
            } else {
                existing.setUnique(unique);
                existing.setType(keyType);
                existing.setMethod(keyComputeMethod);
                return existing;
            }
        }
        else
            return null;
    }

    private Lambda readLambda(Flow flow) {
        var name = input.readUTF();
        var existing = Utils.find(flow.getLambdas(), l -> l.getName().equals(name));
        Lambda lambda;
        if (existing == null)
            lambda = new Lambda(flow.getRoot().nextChildId(), name, List.of(), -1, flow);
        else
            lambda = existing;
        lambda.setParameters(input.readList(() -> readParameter(lambda)));
        lambda.setReturnTypeIndex(input.readInt());
        readCode(lambda.getCode());
        return lambda;
    }

    private Parameter readParameter(Callable callable) {
        var name = input.readUTF();
        var typeIndex = input.readInt();
        var param = Utils.find(callable.getParameters(), p -> p.getName().equals(name));
        if (param == null) {
            var root = ((Entity) callable).getRoot();
            param = new Parameter(root.nextChildId(), name, typeIndex, callable);
        } else
            param.setTypeIndex(typeIndex);
        param.setAttributes(input.readList(() -> Attribute.read(input)));
        return param;
    }

    private void readCode(Code code) {
        code.setMaxLocals(input.readInt());
        code.setMaxStack(input.readInt());
        code.setCode(input.readBytes());
    }

}
