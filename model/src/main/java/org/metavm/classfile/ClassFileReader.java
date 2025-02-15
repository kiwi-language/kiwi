package org.metavm.classfile;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Attribute;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRepository;
import org.metavm.entity.GenericDeclaration;
import org.metavm.flow.*;
import org.metavm.object.instance.core.PhysicalId;
import org.metavm.object.instance.core.TmpId;
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
                    .build();
            klass.setScope(parent);
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
        klass.setIndices(input.readList(() -> readIndex(klassF)));
        klass.setKlasses(input.readList(() -> readKlass(klassF)));
        klass.setAttributes(input.readList(() -> Attribute.read(input)));
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

    private Index readIndex(Klass klass) {
        var name = input.readUTF();
        var typeIndex = input.readInt();
        var unique = input.readBoolean();
        var existing = Utils.find(klass.getIndices(), i -> i.getName().equals(name));
        Index index;
        if (existing != null) {
            existing.setTypeIndex(typeIndex);
            existing.setUnique(unique);
            index = existing;
        }
        else
            index = new Index(klass.getRoot().nextChildId(), klass, name, null, unique, typeIndex);
        index.setMethod(input.readNullable(input::readReference));
        if (listener != null) listener.onIndexRead(index);
        return index;
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
        var ct = new CapturedTypeVariable(
                root.nextChildId(),
                input.readInt(),
                input.readReference(),
                scope
        );
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

    private Lambda readLambda(Flow flow) {
        var lambda = new Lambda(flow.getRoot().nextChildId(), List.of(), -1, flow);
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
