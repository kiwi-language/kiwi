package tech.metavm.entity;

import tech.metavm.flow.Flow;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.TmpId;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.util.ContextUtil;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.io.Closeable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class SerializeContext implements Closeable {

    public static final ThreadLocal<SerializeContext> THREAD_LOCAL = new ThreadLocal<>();

    public static List<TypeDTO> forceWriteTypes(List<Type> types) {
        try (var serContext = SerializeContext.enter()) {
            for (Type type : types) {
                serContext.forceWriteType(type);
            }
            return serContext.getTypes();
        }
    }

    private final Map<Object, Long> tmpIdMap = new HashMap<>();
    private final Set<Object> visited = new IdentitySet<>();
    private int level;
    private boolean includeValueType = false;
    private boolean includeNodeOutputType;
    private boolean includeExpressionType;
    private boolean includeCode;
    private boolean includeBuiltin;
    private boolean writeParameterizedTypeAsPTypeDTO;
    private final Set<Type> writtenTypes = new IdentitySet<>();
    private final Map<Type, TypeDTO> types = new HashMap<>();
    private final Map<Id, TypeDTO> typeMap = new HashMap<>();
    private final Set<ClassType> writingCodeTypes = new IdentitySet<>();

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

    public void addWritingCodeType(ClassType type) {
        this.writingCodeTypes.add(type);
    }

    public boolean shouldWriteCode(ClassType type) {
        return this.writingCodeTypes.contains(type);
    }

    public SerializeContext writeParameterizedTypeAsPTypeDTO(boolean writeParameterizedTypeAsPTypeDTO) {
        this.writeParameterizedTypeAsPTypeDTO = writeParameterizedTypeAsPTypeDTO;
        return this;
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

    public String getId(Object model) {
        if (model instanceof Entity entity && entity.getStringId() != null) {
            return entity.getStringId();
        } else {
            return TmpId.of(getTmpId(model)).toString();
        }
    }

    private boolean isBuiltinType(Type type) {
        return type instanceof PrimitiveType || type instanceof AnyType || type instanceof NeverType;
    }

    public boolean isIncludeExpressionType() {
        return includeExpressionType;
    }

    public void writeType(Type type) {
        writeType(type, false);
    }

    public void forceWriteType(Type type) {
        writeType(type, true);
    }

    private void writeType(Type type, boolean forceWrite) {
        if (!forceWrite && isBuiltinType(type) && !includeBuiltin)
            return;
        if (writtenTypes.contains(type))
            return;
        writtenTypes.add(type);
        TypeDTO typeDTO;
        if(writeParameterizedTypeAsPTypeDTO && type instanceof ClassType classType && classType.isParameterized())
            typeDTO = classType.toPTypeDTO(this);
        else
            typeDTO = type.toDTO();
        types.put(type, typeDTO);
        if (type.tryGetId() != null)
            typeMap.put(type.tryGetId(), typeDTO);
    }

    public Set<Type> getWrittenTypes() {
        return Collections.unmodifiableSet(writtenTypes);
    }

    public void setIncludeExpressionType(boolean includeExpressionType) {
        this.includeExpressionType = includeExpressionType;
    }

    private void writeChildTypes(IEntityContext context) {
        Queue<ClassType> classTypes = new LinkedList<>(NncUtils.filterByType(writtenTypes, ClassType.class));
        Set<ClassType> added = new IdentitySet<>(classTypes);
        while (!classTypes.isEmpty()) {
            var type = classTypes.poll();
            for (Field field : type.getReadyFields()) {
                if (field.isChild()) {
                    writeType(field.getType());
                    var concreteFieldType = field.getType().getConcreteType();
                    if (concreteFieldType instanceof ClassType classType && added.add(classType)) {
                        classTypes.offer(classType);
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
        Queue<ClassType> queue = new LinkedList<>();
        Set<ClassType> added = new IdentitySet<>();
        for (Type writtenType : new IdentitySet<>(writtenTypes)) {
            if (writtenType instanceof ClassType classType && !classType.getTypeArguments().isEmpty()) {
                queue.offer(classType);
                added.add(classType);
            }
        }
        while (!queue.isEmpty()) {
            var type = queue.poll();
            for (Type typeArgument : type.getTypeArguments()) {
                writeType(typeArgument);
                if (typeArgument instanceof ClassType classType
                        && !classType.getTypeArguments().isEmpty() && added.add(classType)) {
                    queue.offer(classType);
                }
            }
        }
    }

    private void writePropertyTypes(IEntityContext entityContext) {
        for (Type writtenType : new IdentitySet<>(writtenTypes)) {
            if (writtenType instanceof ClassType classType) {
                getPropertyTypes(classType).forEach(type -> writeType(type));
            }
        }
    }

    private Set<Type> getPropertyTypes(ClassType classType) {
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
        this.includeBuiltin = includeBuiltin;
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

    public List<TypeDTO> getTypes(Predicate<Type> filter) {
        return NncUtils.filterAndMap(types.entrySet(), e -> filter.test(e.getKey()), Map.Entry::getValue);
    }

    public void forEachType(Predicate<Type> filter, Consumer<Type> action) {
        types.keySet().stream().filter(filter).forEach(action);
    }

    public List<TypeDTO> getParameterizedTypes() {
        List<TypeDTO> pTypes = new ArrayList<>();
        for (Type writtenType : writtenTypes) {
            if(writtenType instanceof ClassType classType) {
                pTypes.add(classType.toPTypeDTO(this));
            }
        }
        return pTypes;
    }

    public List<TypeDTO> getTypesExclude(Type type) {
        return NncUtils.filter(types.values(), t -> !Objects.equals(t.id(), getId(type)));
    }

    public TypeDTO getType(Id id) {
        return NncUtils.requireNonNull(typeMap.get(id));
    }

    @Override
    public void close() {
        if (--level <= 0) THREAD_LOCAL.remove();
    }
}
