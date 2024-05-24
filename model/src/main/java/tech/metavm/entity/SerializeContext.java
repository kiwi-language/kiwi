package tech.metavm.entity;

import tech.metavm.flow.Flow;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeDefDTO;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SerializeContext implements Closeable {

    public static final ThreadLocal<SerializeContext> THREAD_LOCAL = new ThreadLocal<>();

    public static List<TypeDefDTO> forceWriteTypeDefs(List<TypeDef> typeDefs) {
        try (var serContext = SerializeContext.enter()) {
            for (var typeDef : typeDefs) {
                serContext.writeTypeDef(typeDef);
            }
            return serContext.getTypeDefs();
        }
    }

    private final Map<Object, Long> tmpIdMap = new HashMap<>();
    private final Set<Object> visited = new IdentitySet<>();
    private int level;
    private boolean includeValueType = false;
    private boolean includeNodeOutputType;
    private boolean includeExpressionType;
    private boolean includeCode;
    private final Set<TypeDef> writtenTypes = new IdentitySet<>();
    private final Map<TypeDef, TypeDefDTO> types = new HashMap<>();
    private final Map<Id, TypeDefDTO> typeMap = new HashMap<>();
    private final Set<Klass> writingCodeTypes = new IdentitySet<>();

    private SerializeContext() {
    }

    public static SerializeContext enter() {
        SerializeContext context = THREAD_LOCAL.get();
        if (context == null) {
            context = new SerializeContext();
            THREAD_LOCAL.set(context);
        }
        context.level++;
        return context;
    }

    public void addWritingCodeType(Klass type) {
        this.writingCodeTypes.add(type);
    }

    public boolean shouldWriteCode(Klass type) {
        return this.writingCodeTypes.contains(type);
    }

    public Long getTmpId(Object model) {
        NncUtils.requireNonNull(model);
        if (model instanceof Entity entity) {
            if (entity.tryGetId() != null)
                return null;
            else if (entity.getTmpId() != null)
                return entity.getTmpId();
        }
        return tmpIdMap.computeIfAbsent(model, k -> ContextUtil.nextTmpId());
    }

    public String getStringId(Object model) {
        if (model instanceof Entity entity && entity.getStringId() != null) {
            return entity.getStringId();
        } else {
            return TmpId.of(getTmpId(model)).toString();
        }
    }

    public Id getId(Object obj) {
        Id id;
        if (obj instanceof Entity entity && (id = entity.tryGetId()) != null)
            return id;
        else
            return TmpId.of(getTmpId(obj));
    }

    public boolean isIncludeExpressionType() {
        return includeExpressionType;
    }

    public void forceWriteKlass(Klass klass) {
        writeTypeDef(klass);
    }

    public void writeTypeDef(TypeDef typeDef) {
        if (writtenTypes.contains(typeDef))
            return;
        if (typeDef instanceof Klass klass && klass.isParameterized())
            return;
        writtenTypes.add(typeDef);
        var typeDefDTO = typeDef.toDTO(this);
        types.put(typeDef, typeDefDTO);
        if (typeDef.tryGetId() != null)
            typeMap.put(typeDef.tryGetId(), typeDefDTO);
    }

    public Set<TypeDef> getWrittenTypes() {
        return Collections.unmodifiableSet(writtenTypes);
    }

    public void setIncludeExpressionType(boolean includeExpressionType) {
        this.includeExpressionType = includeExpressionType;
    }

    private void writeChildTypes(IEntityContext context) {
        Queue<Klass> classTypes = new LinkedList<>(NncUtils.filterByType(writtenTypes, Klass.class));
        Set<Klass> added = new IdentitySet<>(classTypes);
        while (!classTypes.isEmpty()) {
            var type = classTypes.poll();
            for (var field : type.getReadyFields()) {
                if (field.isChild()) {
                    var fieldType = field.getType();
                    if (fieldType instanceof ClassType classType)
                        writeTypeDef(classType.resolve());
                    var concreteFieldType = field.getType().getConcreteType();
                    if (concreteFieldType instanceof ClassType classType) {
                        var klass = classType.resolve();
                        if (added.add(klass))
                            classTypes.offer(klass);
                    }
                }
            }
        }
    }

    public void writeDependencies(IEntityContext entityContext) {
        writeChildTypes(entityContext);
        writePropertyTypes(entityContext);
        writeTypeArguments(entityContext);
    }

    private void writeTypeArguments(IEntityContext context) {
        Queue<Klass> queue = new LinkedList<>();
        Set<Klass> added = new IdentitySet<>();
        for (var writtenType : new IdentitySet<>(writtenTypes)) {
            if (writtenType instanceof Klass classType && !classType.getTypeArguments().isEmpty()) {
                queue.offer(classType);
                added.add(classType);
            }
        }
        while (!queue.isEmpty()) {
            var type = queue.poll();
            for (Type typeArgument : type.getTypeArguments()) {
                if (typeArgument instanceof ClassType classType
                        && !classType.getTypeArguments().isEmpty()) {
                    var klass = classType.resolve();
                    writeTypeDef(klass);
                    if (added.add(klass))
                        queue.offer(klass);
                }
            }
        }
    }

    private void writePropertyTypes(IEntityContext entityContext) {
        for (var writtenType : new IdentitySet<>(writtenTypes)) {
            if (writtenType instanceof Klass classType) {
                getPropertyTypes(classType).forEach(type -> {
                    if (type instanceof ClassType classType1)
                        writeTypeDef(classType1.resolve());
                });
            }
        }
    }

    private Set<Type> getPropertyTypes(Klass classType) {
        Set<Type> propTypes = new IdentitySet<>();
        for (Flow flow : classType.getMethods()) {
            propTypes.addAll(flow.getParameterTypes());
            propTypes.add(flow.getReturnType());
            propTypes.add(flow.getType());
        }
        for (Field field : classType.getFields()) {
            propTypes.add(field.getType());
        }
        return propTypes;
    }

    public boolean includeValueType() {
        return includeValueType;
    }

    public SerializeContext includingValueType(boolean includingValueType) {
        this.includeValueType = includingValueType;
        return this;
    }

    public boolean includeNodeOutputType() {
        return includeNodeOutputType;
    }

    public SerializeContext includeNodeOutputType(boolean includingNodeOutputType) {
        this.includeNodeOutputType = includingNodeOutputType;
        return this;
    }

    public SerializeContext includeBuiltin(boolean includeBuiltin) {
        return this;
    }

    public boolean isIncludeCode() {
        return includeCode;
    }

    public SerializeContext includingCode(boolean includingCode) {
        this.includeCode = includingCode;
        return this;
    }

    public List<TypeDTO> getTypes() {
        return getTypes(t -> true);
    }

    public List<TypeDTO> getTypes(Predicate<Klass> filter) {
        return NncUtils.filterAndMap(types.entrySet(), e -> e.getKey() instanceof Klass klass && filter.test(klass), e -> (TypeDTO) e.getValue());
    }

    public List<TypeDefDTO> getTypeDefs() {
        return new ArrayList<>(types.values());
    }

    public List<TypeDefDTO> getTypeDefs(Predicate<TypeDef> predicate) {
        return NncUtils.filterAndMap(types.entrySet(), e -> predicate.test(e.getKey()), Map.Entry::getValue);
    }

    public void forEachType(Predicate<TypeDef> filter, Consumer<TypeDef> action) {
        types.keySet().stream().filter(filter).forEach(action);
    }

    public List<TypeDTO> getTypesExclude(Klass klass) {
        var result = new ArrayList<TypeDTO>();
        types.values().forEach(t -> {
            if (t instanceof TypeDTO typeDTO && !Objects.equals(typeDTO.id(), getStringId(klass)))
                result.add(typeDTO);
        });
        return result;
    }

    public List<TypeDTO> getTypesExclude(Type type) {
        var result = new ArrayList<TypeDTO>();
        types.values().forEach(t -> {
            if (t instanceof TypeDTO typeDTO && !Objects.equals(typeDTO.id(), getStringId(type)))
                result.add(typeDTO);
        });
        return result;
    }

    public TypeDTO getType(Id id) {
        return (TypeDTO) NncUtils.requireNonNull(typeMap.get(id));
    }

    @Override
    public void close() {
        if (--level <= 0) THREAD_LOCAL.remove();
    }
}
