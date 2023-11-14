package tech.metavm.entity;

import tech.metavm.dto.RefDTO;
import tech.metavm.flow.Flow;
import tech.metavm.infra.RegionManager;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.ParameterizedTypeDTO;
import tech.metavm.object.meta.rest.dto.TypeDTO;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.NncUtils;

import java.io.Closeable;
import java.util.*;
import java.util.function.Predicate;

public class SerializeContext implements Closeable {

    public static final ThreadLocal<SerializeContext> THREAD_LOCAL = new ThreadLocal<>();

    private final Map<Object, Long> tmpIdMap = new HashMap<>();
    private final Set<Object> visited = new IdentitySet<>();
    private long nextTmpId = 1;
    private int level;
    private boolean includingValueType = false;
    private boolean includingNodeOutputType;
    private boolean includeExpressionType;
    private boolean includingCode;
    private boolean includeBuiltinTypes;
    private final Set<Type> writtenTypes = new IdentitySet<>();
    private final Map<Type, TypeDTO> types = new HashMap<>();
    private final Map<Long, TypeDTO> typeMap = new HashMap<>();
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

    public Long getTmpId(Object model) {
        NncUtils.requireNonNull(model);
        if(model instanceof Entity entity) {
            if(entity.getId() != null) {
                return null;
            }
            else if(entity.getTmpId() != null) {
                return entity.getTmpId();
            }
        }
        return tmpIdMap.computeIfAbsent(model, k -> nextTmpId++);
    }

    public RefDTO getRef(Object model) {
        if (model instanceof Identifiable identifiable && identifiable.getId() != null) {
            return new RefDTO(identifiable.getId(), null);
        } else {
            return new RefDTO(null, getTmpId(model));
        }
    }

    private boolean isBuiltinType(Type type) {
        return type instanceof PrimitiveType || type instanceof ObjectType || type instanceof  NothingType;
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
        if(!forceWrite && isBuiltinType(type) && !includeBuiltinTypes) {
            return;
        }
        if(writtenTypes.contains(type)) {
            return;
        }
        writtenTypes.add(type);
        var typeDTO = type.toDTO();
        types.put(type, typeDTO);
        if(type.getId() != null) {
            typeMap.put(type.getIdRequired(), typeDTO);
        }
    }

    public Set<Type> getWrittenTypes() {
        return Collections.unmodifiableSet(writtenTypes);
    }

    public void setIncludeExpressionType(boolean includeExpressionType) {
        this.includeExpressionType = includeExpressionType;
    }

    private void writeChildTypes() {
        Queue<ClassType> classTypes = new LinkedList<>(NncUtils.filterByType(writtenTypes, ClassType.class));
        Set<ClassType> added = new IdentitySet<>(classTypes);
        while (!classTypes.isEmpty()) {
            var type = classTypes.poll();
            for (Field field : type.getFields()) {
                if(field.isChildField()) {
                    writeType(field.getType());
                    var concreteFieldType = field.getType().getConcreteType();
                    if(concreteFieldType instanceof ClassType classType && added.add(classType)) {
                        classTypes.offer(classType);
                    }
                }
            }
        }
    }

    public void writeDependencies() {
        writeChildTypes();
        writePropertyTypes();
        writeTypeArguments();
    }

    private void writeTypeArguments() {
        Queue<ClassType> queue = new LinkedList<>();
        Set<ClassType> added = new IdentitySet<>();
        for (Type writtenType : new IdentitySet<>(writtenTypes)) {
            if(writtenType instanceof ClassType classType && !classType.getTypeArguments().isEmpty()) {
                queue.offer(classType);
                added.add(classType);
            }
        }
        while (!queue.isEmpty()) {
            var type = queue.poll();
            for (Type typeArgument : type.getTypeArguments()) {
                writeType(typeArgument);
                if(typeArgument instanceof ClassType classType
                        && !classType.getTypeArguments().isEmpty() && added.add(classType)) {
                    queue.offer(classType);
                }
            }
        }
    }

    private void writePropertyTypes() {
        for (Type writtenType : new IdentitySet<>(writtenTypes)) {
            if(writtenType instanceof ClassType classType) {
                getPropertyTypes(classType).forEach(this::writeType);
            }
        }
    }

    private Set<Type> getPropertyTypes(ClassType classType) {
        Set<Type> propTypes = new IdentitySet<>();
        for (Flow flow : classType.getFlows()) {
            propTypes.addAll(flow.getParameterTypes());
            propTypes.add(flow.getReturnType());
            propTypes.add(flow.getType());
        }
        for (Field field : classType.getFields()) {
            propTypes.add(field.getType());
        }
        return propTypes;
    }

    public boolean isIncludingValueType() {
        return includingValueType;
    }

    public void setIncludingValueType(boolean includingValueType) {
        this.includingValueType = includingValueType;
    }

    public boolean isIncludingNodeOutputType() {
        return includingNodeOutputType;
    }

    public void setIncludingNodeOutputType(boolean includingNodeOutputType) {
        this.includingNodeOutputType = includingNodeOutputType;
    }

    public void setIncludeBuiltinTypes(boolean includeBuiltinTypes) {
        this.includeBuiltinTypes = includeBuiltinTypes;
    }

    public boolean isIncludingCode() {
        return includingCode;
    }

    public void setIncludingCode(boolean includingCode) {
        this.includingCode = includingCode;
    }

    public List<TypeDTO> getTypes() {
        return getTypes(t -> true);
    }

    public List<TypeDTO> getTypes(Predicate<Type> filter) {
        return NncUtils.filterAndMap(types.entrySet(), e -> filter.test(e.getKey()), Map.Entry::getValue);
    }

    public List<TypeDTO> getNonSystemTypes() {
        return getTypes(t -> t.isIdNull() || !RegionManager.isSystemId(t.getIdRequired()));
    }

    public List<TypeDTO> getTypesExclude(Type type) {
        return NncUtils.filter(types.values(), t -> !Objects.equals(t.getRef(), getRef(type)));
    }

    public TypeDTO getType(long id) {
        return NncUtils.requireNonNull(typeMap.get(id));
    }

    @Override
    public void close() {
        if (--level <= 0) THREAD_LOCAL.remove();
    }
}
