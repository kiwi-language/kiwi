package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.*;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.*;
import org.metavm.util.DebugEnv;
import org.metavm.util.Instances;
import org.metavm.util.MvInput;
import org.metavm.util.Utils;

import javax.annotation.Nullable;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class KlassInput extends MvInput {

    private static final int STATE_NORMAL = 0;

    private static final int STATE_PEEKING = 1;

    private static final int STATE_REPLAYING = 2;

    private int state = STATE_NORMAL;

    private byte[] peekedBytes = new byte[16];
    private int peekedBytesOffset = 0;
    private int peekedBytesLimit = 0;

    private @Nullable SymbolMap symbolMap;

    private final Map<Id, Instance> cached = new HashMap<>();
    private final EntityRepository repository;

    public KlassInput(InputStream in, EntityRepository repository) {
        super(in);
        this.repository = repository;
    }

    @Override
    public Message readTree() {
        return readEntityMessage();
    }

    @Override
    public Value readRemovingInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readValueInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readRelocatingInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readRedirectingInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readRedirectingReference() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reference readReference() {
        var tracing = DebugEnv.traceClassFileIO;
        var refType = read();
        return switch (refType) {
            case SymbolRefs.KLASS -> {
                var qualName = readUTF();
                yield new Reference(
                        TmpId.random(),
                        () -> {
                            var klass = Objects.requireNonNull(repository.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(qualName)),
                                    () -> "Cannot find klass '" + qualName + "'");
                            if (tracing) log.trace("Resolved class klass {} for name {}", klass.getQualifiedName(), qualName);
                            return klass;
                        }
                );
            }
            case SymbolRefs.ENCLOSED_KLASS -> {
                var scopeRef = (Reference) readReference();
                var name = readUTF();
                yield new Reference(TmpId.random(),
                        () -> ((KlassDeclaration) scopeRef.resolveDurable()).getKlassByByName(name));
            }
            case SymbolRefs.METHOD -> {
                var klassRef = readReference();
//                var internalName = readUTF();
                var index = readInt();
                yield  new Reference(
                        TmpId.random(),
                        () -> ((Klass) klassRef.get()).getMethods().get(index)
//                        () -> {
//                            var found = ((Klass) klassRef.get()).findSelfMethod(m -> m.getInternalName().equals(internalName));
//                            if (found == null)
//                                throw new NullPointerException("Cannot find method " + internalName + " in klas " + ((Klass)klassRef.get()).getName());
//                            return found;
//                        }
                );
            }
            case SymbolRefs.FIELD -> {
                var klassRef = readReference();
                var name = readUTF();
                yield new Reference(
                        TmpId.random(),
                        () -> ((Klass) klassRef.get()).getSelfFieldByName(name)
                );
            }
            case SymbolRefs.INDEX -> {
                var klassRef = readReference();
                var name = readUTF();
                yield new Reference(TmpId.random(),
                        () -> Utils.findRequired(((Klass) klassRef.resolveDurable()).getIndices(), i -> i.getName().equals(name)));
            }
            case SymbolRefs.FUNCTION -> {
                var name = readUTF();
                yield new Reference(
                        TmpId.random(),
                        () -> Objects.requireNonNull(repository.selectFirstByKey(Function.UNIQUE_NAME, Instances.stringInstance(name)),
                                () -> "Cannot find function '" + name + "'")
                );
            }
            case SymbolRefs.TYPE_VARIABLE -> {
                var declarationRef = readReference();
                var name = readUTF();
                yield new Reference(TmpId.random(),
                        () -> ((GenericDeclaration) declarationRef.resolveDurable()).getTypeParameterByName(name));
            }
            case SymbolRefs.CAPTURED_TYPE_VARIABLE -> {
                var scopeRef = readReference();
                var index = readInt();
                yield new Reference(TmpId.random(),
                        () -> ((CapturedTypeScope) scopeRef.resolveDurable()).getCapturedTypeVariables().get(index));
            }
            case SymbolRefs.LAMBADA -> {
                var flowRef = readReference();
                var index = readInt();
                yield new Reference(TmpId.random(),
                        () -> ((Flow) flowRef.resolveDurable()).getLambdas().get(index));
            }
            case SymbolRefs.PARAMETER -> {
                var callableRef = readReference();
                var name = readUTF();
                yield new Reference(TmpId.random(),
                        () -> ((Callable) callableRef.resolveDurable()).getParameterByName(name));
            }
            default -> throw new IllegalStateException("Invalid symbol reference type: " + refType);
        };
    }

    @Override
    public Value readFlaggedReference() {
        throw new UnsupportedOperationException();
    }

    protected  <T extends Entity> T getOrCreateEntity(Class<T> klass) {
        Entity existing = null;
        if (klass == Klass.class) {
            startPeeking();
            readId();
            readList(() -> Attribute.read(this));
            var sourceTag = readNullable(this::readInt);
            if (sourceTag != null)
                existing = repository.selectFirstByKey(Klass.UNIQUE_SOURCE_TAG, Instances.intInstance(sourceTag));
            else {
                var name = readUTF();
                String qualifiedName;
                if (symbolMap != null)
                    existing = symbolMap.get(Klass.class, name);
                else if ((qualifiedName = readNullable(this::readUTF)) != null)
                    existing = repository.selectFirstByKey(Klass.UNIQUE_QUALIFIED_NAME, Instances.stringInstance(qualifiedName));
            }
            stopPeeking();
        }
        else if (klass == Field.class) {
            startPeeking();
            readId();
            var sourceTag = readNullable(this::readInt);
            if (sourceTag != null)
                existing = Objects.requireNonNull(symbolMap).get(Field.class, "tag:" + sourceTag);
            else {
                var name = readUTF();
                existing = Objects.requireNonNull(symbolMap).get(Field.class, name);
            }
            stopPeeking();
        }
        else if (klass == Method.class || klass == Function.class) {
            startPeeking();
            readId();
            readList(() -> Attribute.read(this));
            var internalName = readUTF();
            stopPeeking();
            if (klass == Function.class) existing = repository.selectFirstByKey(Function.UNIQUE_NAME, Instances.stringInstance(internalName));
            else existing = Objects.requireNonNull(symbolMap).get(Method.class, internalName);
        }
        else if (klass == Parameter.class) {
            startPeeking();
            readId();
            readList(() -> Attribute.read(this));
            var name = readUTF();
            stopPeeking();
            existing = Objects.requireNonNull(symbolMap).get(Parameter.class, name);
        }
        else if (klass == Index.class) {
            startPeeking();
            readId();
            var name = readUTF();
            stopPeeking();
            existing = Objects.requireNonNull(symbolMap).get(Index.class, name);
        }
        else if (klass == IndexField.class) {
            startPeeking();
            readId();
            var name = readUTF();
            stopPeeking();
            existing = Objects.requireNonNull(symbolMap).get(IndexField.class, name);
        }
        else if (klass == TypeVariable.class) {
            startPeeking();
            readId();
            readList(() -> Attribute.read(this));
            var name = readUTF();
            stopPeeking();
            existing = Objects.requireNonNull(symbolMap).get(TypeVariable.class, name);
        }
        var id = readId();
        //noinspection unchecked
        return existing != null ? (T) existing : getEntity(klass, id);
    }

    @Override
    protected <T extends Entity> T getEntity(Class<T> klass, Id id) {
        var existing = klass.cast(cached.get(id));
        if (existing != null)
            return existing;
        existing = repository.getEntity(klass, id);
        if (existing != null)
            return existing;
        var newEntity = super.getEntity(klass, id);
        cached.put(id, newEntity);
        return newEntity;
    }

    @Override
    public Klass readEntityMessage() {
        return readEntity(Klass.class, null);
    }

    @Override
    public int read(byte[] buf) {
        var state = this.state;
        var len = buf.length;
        switch (state) {
            case STATE_PEEKING -> {
                super.read(buf);
                var pkLimit = peekedBytesLimit;
                var pkLen = peekedBytes.length;
                var minPkLen = pkLimit + len;
                if (pkLen < minPkLen) {
                    do pkLen <<= 1; while (pkLen < minPkLen);
                    peekedBytes = Arrays.copyOf(peekedBytes, pkLen);
                }
                System.arraycopy(buf, 0, peekedBytes, pkLimit, len);
                peekedBytesLimit = pkLimit + len;
            }
            case STATE_REPLAYING -> {
                var pkBytes = peekedBytes;
                var pkOffset = peekedBytesOffset;
                var pkLimit = peekedBytesLimit;
                if (pkLimit - pkOffset < len) throw new IllegalStateException("Insufficient peeked bytes");
                System.arraycopy(pkBytes, pkOffset, buf, 0, len);
                pkOffset += len;
                if (pkOffset < pkLimit) peekedBytesOffset = pkOffset;
                else if (pkOffset == pkLimit) {
                    this.state = STATE_NORMAL;
                    peekedBytesOffset = 0;
                    peekedBytesLimit = 0;
                }
                else throw new IllegalStateException("Corrupt data");
            }
            case STATE_NORMAL -> super.read(buf);
            default -> throw new IllegalStateException("Invalid state: " + state);
        }
        return len;
    }

    @Override
    public int read() {
        var state = this.state;
        return switch (state) {
            case STATE_NORMAL -> super.read();
            case STATE_PEEKING ->  {
                var pkLimit = peekedBytesLimit;
                assert pkLimit <= peekedBytes.length;
                if (pkLimit == peekedBytes.length)
                    peekedBytes = Arrays.copyOf(peekedBytes, peekedBytes.length << 1);
                var b = super.read();
                peekedBytes[pkLimit] = (byte) b;
                peekedBytesLimit = pkLimit + 1;
                yield b;
            }
            case STATE_REPLAYING -> {
                assert peekedBytesOffset < peekedBytes.length;
                var b = peekedBytes[peekedBytesOffset++];
                if (peekedBytesOffset == peekedBytesLimit) {
                    this.state = STATE_NORMAL;
                    peekedBytesOffset = 0;
                    peekedBytesLimit = 0;
                }
                yield b;
            }
            default -> throw new IllegalStateException("Invalid state: " + state);
        };
    }

    public void startPeeking() {
        if (state != STATE_NORMAL) throw new IllegalStateException();
        assert peekedBytesOffset == 0 && peekedBytesLimit == 0 : "peekedBytesOffset and peekedBytesLimit should be zero";
        state = STATE_PEEKING;
    }

    public void stopPeeking() {
        if (state != STATE_PEEKING) throw new IllegalStateException();
        if (peekedBytesLimit > 0) state = STATE_REPLAYING;
        else state = STATE_NORMAL;
    }

    public <T extends Entity> T readEntity(Class<T> klass, Entity parent) {
        var entity = getOrCreateEntity(klass);
        enterSymbolMap(entity);
        entity.readHeadAndBody(this, parent);
        exitSymbolMap();
        if (entity.tryGetId() instanceof TmpId tmpId && repository.getEntity(klass, tmpId) == null)
            repository.bind(entity);
        return entity;
    }

    public void logCurrentSymbols() {
        log.trace("{}", symbolMap != null ? symbolMap.symbols : "null");
    }

    protected void enterSymbolMap(Entity entity) {
        symbolMap = entity.tryGetId() instanceof PhysicalId ? SymbolMap.fromEntity(entity, symbolMap) : new SymbolMap(symbolMap);
    }

    protected void exitSymbolMap() {
        symbolMap = Objects.requireNonNull(symbolMap).parent;
    }

    private static class SymbolMap {
        private final @Nullable SymbolMap parent;
        private final Map<Symbol, Entity> symbols = new HashMap<>();

        public SymbolMap(@Nullable SymbolMap parent) {
            this.parent = parent;
        }


        public static SymbolMap fromEntity(Entity entity, @Nullable SymbolMap parent) {
            return switch (entity) {
                case Klass klass -> fromKlass(klass, parent);
                case Flow flow -> fromFlow(flow, parent);
                case Index index -> fromIndex(index, parent);
                default -> new SymbolMap(parent);
            } ;
        }

        public static SymbolMap fromKlass(Klass klass, @Nullable SymbolMap parent) {
            var map = new SymbolMap(parent);
            for (Field field : klass.getFields()) {
                map.put(Field.class, field.getName(), field);
                if (field.getSourceTag() != null)
                    map.put(Field.class, "tag:" + field.getSourceTag(), field);
            }
            for (Field field : klass.getStaticFields()) {
                map.put(Field.class, field.getName(), field);
                if (field.getSourceTag() != null)
                    map.put(Field.class, "tag:" + field.getSourceTag(), field);
            }
            for (Method method : klass.getMethods()) {
                map.put(Method.class, method.getInternalName(null), method);
            }
            for (Klass innerKlass : klass.getKlasses()) {
                map.put(Klass.class, innerKlass.getName(), innerKlass);
            }
            for (Index index : klass.getIndices()) {
                map.put(Index.class, index.getName(), index);
            }
            for (TypeVariable typeParameter : klass.getTypeParameters()) {
                map.put(TypeVariable.class, typeParameter.getName(), typeParameter);
            }
            return map;
        }

        public static SymbolMap fromFlow(Flow flow, @Nullable SymbolMap parent) {
            var map = new SymbolMap(parent);
            for (Parameter parameter : flow.getParameters()) {
                map.put(Parameter.class, parameter.getName(), parameter);
            }
            for (Klass klass : flow.getKlasses()) {
                map.put(Klass.class, klass.getName(), klass);
            }
            for (TypeVariable typeParameter : flow.getTypeParameters()) {
                map.put(TypeVariable.class, typeParameter.getName(), typeParameter);
            }
            return map;
        }

        public static SymbolMap fromIndex(Index index, @Nullable SymbolMap parent) {
            var map = new SymbolMap(parent);
            for (IndexField field : index.getFields()) {
                map.put(IndexField.class, field.getName(), field);
            }
            return map;
        }

        void put(Class<?> clazz, String name, Entity entity) {
            symbols.put(new Symbol(clazz, name), entity);
        }

        <T extends Entity> T get(Class<T> clazz, String name) {
            return clazz.cast(symbols.get(new Symbol(clazz, name)));
        }

    }

    private record Symbol(Class<?> clazz, String name) {}

}
