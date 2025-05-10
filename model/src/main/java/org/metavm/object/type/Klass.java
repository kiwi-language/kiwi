package org.metavm.object.type;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.annotation.NativeEntity;
import org.metavm.api.EntityField;
import org.metavm.api.Generated;
import org.metavm.api.JsonIgnore;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.EntityRegistry;
import org.metavm.entity.natives.NativeBase;
import org.metavm.expression.Var;
import org.metavm.flow.Error;
import org.metavm.flow.*;
import org.metavm.object.instance.core.Instance;
import org.metavm.object.instance.core.Reference;
import org.metavm.object.instance.core.Value;
import org.metavm.object.instance.core.*;
import org.metavm.object.type.ClassType;
import org.metavm.object.type.Klass;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.util.*;
import org.metavm.util.MvInput;
import org.metavm.util.MvOutput;
import org.metavm.util.StreamVisitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.metavm.util.Utils.*;

@NativeEntity(26)
@org.metavm.api.Entity
@Slf4j
public class Klass extends TypeDef implements GenericDeclaration, ChangeAware, StagedEntity, GlobalKey, LoadAware, LocalKey, Message, ConstantScope, KlassDeclaration {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static final Logger logger = LoggerFactory.getLogger(Klass.class);

    public static final IndexDef<Klass> IDX_ALL_FLAG = IndexDef.create(Klass.class, 1,
            e -> List.of(Instances.booleanInstance(e.allFlag)));
    public static final IndexDef<Klass> IDX_NAME = IndexDef.create(Klass.class,
            1, klass -> List.of(Instances.stringInstance(klass.name)));

    public static final IndexDef<Klass> UNIQUE_QUALIFIED_NAME = IndexDef.createUnique(Klass.class,
            1, klass -> List.of(klass.qualifiedName != null ? Instances.stringInstance(klass.qualifiedName) : Instances.nullInstance())
            );

    public static final IndexDef<Klass> UNIQUE_SOURCE_TAG = IndexDef.createUnique(Klass.class,
            1, klass -> List.of(klass.sourceTag != null ? Instances.intInstance(klass.sourceTag) : Instances.nullInstance())
            );
    @SuppressWarnings("unused")
    private static Klass __klass__;

    private @Nullable Integer sourceTag;
    @EntityField(asTitle = true)
    private String name;
    @Nullable
    private String qualifiedName;
    private ClassKind kind;
    private boolean anonymous;
    private boolean ephemeral;
    private boolean searchable;

    @Nullable
    private ClassType superType;
    private List<ClassType> interfaces = new ArrayList<>();

    @Nullable
    private Integer superTypeIndex;
    private List<Integer> interfaceIndexes = new ArrayList<>();
//    private transient List<Klass> interfaces = new ArrayList<>();
//    private transient @Nullable Klass superKlass;
    private ClassSource source;
    @Nullable
    private String desc;

    private int nextFieldTag;

    private int nextFieldSourceCodeTag = 1000000;

    private List<Field> fields = new ArrayList<>();
    @Nullable
    private Reference titleField;
    private List<Method> methods = new ArrayList<>();
    private @Nullable KlassDeclaration scope;
    private List<Klass> klasses = new ArrayList<>();

    private List<Field> staticFields = new ArrayList<>();
    private List<Index> indices = new ArrayList<>();
    // Don't remove, for search
    @SuppressWarnings("unused")
    private boolean isAbstract;
    private boolean isTemplate;
    // Don't remove, used for search
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private boolean isParameterized;
    private List<TypeVariable> typeParameters = new ArrayList<>();
    private List<Error> errors = new ArrayList<>();
    private boolean error;

    // For unit test. Do not remove
    @SuppressWarnings("unused")
    @Nullable
    private KlassFlags flags;

    private ClassTypeState state = ClassTypeState.INIT;
    private long tag;

    private int since;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private boolean templateFlag = false;

    private boolean struct;

    @SuppressWarnings({"FieldMayBeFinal", "unused"}) // for unit test
    private boolean dummyFlag = false;

    private boolean methodTableBuildDisabled;

    private ConstantPool constantPool = new ConstantPool(this);

    private boolean allFlag = true;

    private transient ResolutionStage stage = ResolutionStage.INIT;

    @CopyIgnore
    private transient volatile MethodTable methodTable;

    // length of the longest path from the current type upwards to a root in the type hierarchy
    private transient int rank;

    private transient int level;

    private transient Map<Long,Integer> tag2level = new HashMap<>();

    private transient List<Klass> extensions = new ArrayList<>();

    private transient List<Klass> implementations = new ArrayList<>();

    private transient List<Klass> sortedKlasses = new ArrayList<>();

    private transient List<Field> sortedFields = new ArrayList<>();

    private transient int fieldCount;

    @CopyIgnore
    private transient volatile Closure closure;

    private transient ClassType type;

    private transient Class<? extends NativeBase> nativeClass;

    private transient boolean frozen;

    private transient Klass arrayKlass;

    private transient Klass componentKlass;

    public Klass(
            @NotNull Id id,
            String name,
            @Nullable String qualifiedName,
            @Nullable ClassType superType,
            List<ClassType> interfaces,
            @NotNull ClassKind kind,
            ClassSource source,
            boolean anonymous,
            boolean ephemeral,
            boolean struct,
            boolean searchable,
            @Nullable String desc,
            boolean isAbstract,
            boolean isTemplate,
            @Nullable KlassDeclaration scope,
            List<TypeVariable> typeParameters,
            long tag,
            @Nullable Integer sourceTag,
            int since,
            boolean methodTableBuildDisabled) {
        super(id);
        this.name = name;
        this.qualifiedName = qualifiedName;
        this.kind = kind;
        this.isAbstract = isAbstract;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.struct = struct;
        this.searchable = searchable;
        this.source = source;
        this.desc = desc;
        this.tag = tag;
        this.sourceTag = sourceTag;
        this.since = since;
        this.scope = scope;
        this.fieldCount = superType != null ? superType.getKlass().getFieldCount() : 0;
        this.methodTableBuildDisabled = methodTableBuildDisabled;
        setSuperType(superType, true);
        setInterfaces(interfaces, true);
        resetRank();
        if (scope != null)
            scope.addKlass(this);
        closure = new Closure(this);
        resetRank();
        if (superType != null)
            superType.getKlass().addExtension(this);
        interfaces.forEach(it -> it.getKlass().addImplementation(this));
        setTypeParameters(typeParameters);
//        getMethodTable().rebuild();
        setTemplateFlag(isTemplate);
        resetSortedClasses();
        Utils.require(getAncestorClasses().size() <= Constants.MAX_INHERITANCE_DEPTH,
                "Inheritance depth of class " + name + "  exceeds limit: " + Constants.MAX_INHERITANCE_DEPTH);
    }

    @Generated
    public static void visitBody(StreamVisitor visitor) {
        TypeDef.visitBody(visitor);
        visitor.visitNullable(visitor::visitInt);
        visitor.visitUTF();
        visitor.visitNullable(visitor::visitUTF);
        visitor.visitByte();
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitNullable(visitor::visitValue);
        visitor.visitList(visitor::visitValue);
        visitor.visitNullable(visitor::visitInt);
        visitor.visitList(visitor::visitInt);
        visitor.visitByte();
        visitor.visitNullable(visitor::visitUTF);
        visitor.visitInt();
        visitor.visitInt();
        visitor.visitList(visitor::visitEntity);
        visitor.visitNullable(visitor::visitValue);
        visitor.visitList(visitor::visitEntity);
        visitor.visitList(visitor::visitEntity);
        visitor.visitList(visitor::visitEntity);
        visitor.visitList(visitor::visitEntity);
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitList(visitor::visitEntity);
        visitor.visitList(() -> Error.visit(visitor));
        visitor.visitBoolean();
        visitor.visitNullable(visitor::visitEntity);
        visitor.visitByte();
        visitor.visitLong();
        visitor.visitInt();
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitBoolean();
        visitor.visitBoolean();
        ConstantPool.visit(visitor);
        visitor.visitBoolean();
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    void resetSortedClasses() {
        sortedKlasses = new ArrayList<>();
        forEachSuperClass(sortedKlasses::add);
        level = sortedKlasses.size() - 1;
        sortedKlasses.sort(Comparator.comparingInt(Klass::getLevel));
        tag2level = new HashMap<>();
        for (Klass k : sortedKlasses) {
            tag2level.put(k.getTag(), k.getLevel());
        }
    }

    public Map<Long, Integer> getTag2level() {
        return Collections.unmodifiableMap(tag2level);
    }

    private void removeImplementation(Klass klass) {
        implementations.remove(klass);
    }

    private void removeExtension(Klass klass) {
        extensions.remove(klass);
    }

    protected void setTemplateFlag(boolean templateFlag) {
        this.templateFlag = templateFlag;
    }

    @Nullable
    @SuppressWarnings("unused")
    public String getDesc() {
        return desc;
    }

    @JsonIgnore
    public List<Field> getReadyFields() {
        return Collections.unmodifiableList(fields);
    }

    public List<Field> getFields() {
        return Collections.unmodifiableList(fields);
    }

    @JsonIgnore
    public List<Field> getAllFields() {
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return Utils.union(superKlass.getAllFields(), fields);
        else
            return Collections.unmodifiableList(fields);
    }

    @JsonIgnore
    @Override
    public boolean isValidGlobalKey() {
        return isBuiltin();
    }

    @JsonIgnore
    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        return getQualifiedName();
    }

    public void forEachField(Consumer<Field> action) {
        var superKlass = getSuperKlass();
        if (superKlass != null)
            superKlass.forEachField(action);
        for (Field field : fields) {
            if(!field.isMetadataRemoved())
                action.accept(field);
        }
    }

    public long getTag() {
        return tag;
    }

    @Nullable
    public Integer getSourceTag() {
        return sourceTag;
    }

    public void setTitleField(@Nullable Field titleField) {
        if (titleField != null && !titleField.getType().getUnderlyingType().isString())
            throw new BusinessException(ErrorCode.TITLE_FIELD_MUST_BE_STRING);
        this.titleField = Utils.safeCall(titleField, Instance::getReference);
    }

    @JsonIgnore
    public boolean isTemplate() {
        return !typeParameters.isEmpty();
    }

    //<editor-fold desc="hierarchy">

    @JsonIgnore
    public List<Klass> getAncestorClasses() {
        List<Klass> result = new ArrayList<>();
        accept(new VoidElementVisitor() {
            @Override
            public Void visitKlass(Klass klass) {
                var s = klass.getSuperKlass();
                if (s != null)
                    s.accept(this);
                result.add(klass);
                return super.visitKlass(klass);
            }
        });
        return result;
    }

    @org.metavm.api.JsonIgnore
    public Closure getClosure() {
        if(closure == null) {
            synchronized (this) {
                if(closure == null)
                    closure = new Closure(this);
            }
        }
        return closure;
    }

    @JsonIgnore
    public List<Klass> getSubKlasses() {
        return Utils.merge(extensions, implementations);
    }

    @JsonIgnore
    public List<Klass> getDescendantTypes() {
        List<Klass> types = new ArrayList<>();
        visitDescendantTypes(types::add);
        return types;
    }

    public void visitDescendantTypes(Consumer<Klass> action) {
        action.accept(this);
        for (Klass subType : extensions) {
            subType.visitDescendantTypes(action);
        }
        for (Klass subType : implementations) {
            subType.visitDescendantTypes(action);
        }
    }

    private void resetRank() {
        var superKlass = getSuperKlass();
        int r = superKlass != null ? superKlass.getRank() : 0;
        for (var it : getInterfaceKlasses()) {
            var itRank = it.getRank();
            if (itRank > r)
                r = itRank;
        }
        rank = r + 1;
    }

    @JsonIgnore
    public int getRank() {
        return rank;
    }

    public List<Error> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public void clearElementErrors(Element element) {
        errors.removeIf(e -> e.getElement() == element);
    }

    public void addError(Element element, ErrorLevel level, String message) {
        errors.add(new Error(element, level, message));
    }

    //</editor-fold>

    //<editor-fold desc="method">

    @JsonIgnore
    public Method getDefaultConstructor() {
        return getMethodByNameAndParamTypes(Types.getConstructorName(this), List.of());
    }

    public void sortFields(Comparator<Field> comparator) {
        this.fields.sort(comparator);
        this.staticFields.sort(comparator);
    }

    public ClassTypeState getState() {
        return state;
    }

    public void setState(ClassTypeState state) {
        this.state = state;
    }

    @JsonIgnore
    public List<Klass> getSortedKlasses() {
        return sortedKlasses;
    }

    @JsonIgnore
    public List<Field> getSortedFields() {
        return Objects.requireNonNull(sortedFields, () -> "Klass '" + getTypeDesc() + "' has not been initialized");
    }

    void resetFieldOffsets() {
        forEachExtension(Klass::resetSelfFieldOffset);
    }

    @JsonIgnore
    public int getFieldCount() {
        return fieldCount;
    }

    private void resetSelfFieldOffset() {
        var superKlass = getSuperKlass();
        var offset = superKlass != null ? superKlass.getFieldCount() : 0;
        for (Field f : getSortedFields()) {
            f.setOffset(offset++);
        }
        fieldCount = offset;
    }

    void resetFieldTransients() {
        resetSortedFields();
        resetFieldOffsets();
    }

    private void resetSortedFields() {
        sortedFields = new ArrayList<>();
        sortedFields.addAll(this.fields);
        sortedFields.sort(Comparator.comparingInt(Field::getTag));
        assert fields.size() <= 1 || Utils.allMatch(fields, EntityUtils::isModelInitialized);
    }

    public MethodTable getMethodTable() {
        if (methodTable == null) {
            synchronized (this) {
                if (methodTable == null)
                    methodTable = new MethodTable(this);
            }
        }
        return methodTable;
    }

    public List<Method> getMethods() {
        return Collections.unmodifiableList(methods);
    }

    @JsonIgnore
    public @Nullable Method getWriteObjectMethod() {
        return getMethodTable().getWriteObjectMethod();
    }

    @JsonIgnore
    public @Nullable Method getReadObjectMethod() {
        return getMethodTable().getReadObjectMethod();
    }

    public Method getMethodByInternalName(String internalName) {
        return Utils.findRequired(methods, m -> m.getInternalName(null).equals(internalName),
                () -> "Failed to find method with internal name '" + internalName + "' in class " + getTypeDesc());
    }

    @JsonIgnore
    public List<Method> getDeclaredMethods() {
        return methods;
    }

    public Method getMethod(long id) {
        return Utils.findRequired(methods, m -> Objects.equals(m.tryGetTreeId(), id));
    }

    public Method tryGetMethod(String name, List<Type> parameterTypes) {
        var method = Utils.find(methods,
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.getMethod(name, parameterTypes);
        return null;
    }

    public Method getMethod(String name, List<Type> parameterTypes) {
        return Objects.requireNonNull(
                tryGetMethod(name, parameterTypes),
                () -> "Can not find method '" + name + "(" +
                        Utils.join(parameterTypes, Type::getName, ",")
                        + ")' in type '" + getName() + "'"
        );
    }

    public @Nullable Method findMethodByNameAndParamTypes(String name, List<Type> parameterTypes) {
        var method = Utils.find(methods,
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        var superKlass = getSuperKlass();
        if (superKlass != null) {
            var m = superKlass.findMethodByNameAndParamTypes(name, parameterTypes);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : getInterfaceKlasses()) {
                var m = it.findMethodByNameAndParamTypes(name, parameterTypes);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    public Method getMethodByNameAndParamTypes(String name, List<Type> parameterTypes) {
        var found = findMethodByNameAndParamTypes(name, parameterTypes);
        if(found == null) {
            throw new NullPointerException(String.format("Can not find method %s(%s) in klass %s",
                    name, Utils.join(parameterTypes, Type::getTypeDesc, ","), getTypeDesc()));
        }
        return found;
    }

    public Method getMethodByName(String name) {
        return Objects.requireNonNull(findMethodByName(name), () -> "Can not find method with name '" + name + "' in class '" + this.name + "'");
    }

    public @Nullable Method findMethodByName(String name) {
        return findMethod(Method::getName, name);
    }

    public Method findSelfMethod(Predicate<Method> predicate) {
        return Utils.find(methods, predicate);
    }

    public Method getSelfMethod(Predicate<Method> predicate) {
        return Objects.requireNonNull(findSelfMethod(predicate));
    }

    public Method getMethod(Predicate<Method> predicate) {
        return getMethod(predicate, () -> "Can not find method with predicate in klass " + this);
    }

    public Method getMethod(Predicate<Method> predicate, Supplier<String> messageSupplier) {
        var found = findMethod(predicate);
        if (found != null)
            return found;
        throw new NullPointerException(messageSupplier.get());
    }

    public @Nullable Method findMethod(Predicate<Method> predicate) {
        var found = Utils.find(methods, predicate);
        if (found != null)
            return found;
        var superKlass = getSuperKlass();
        if (superKlass != null && (found = superKlass.findMethod(predicate)) != null)
            return found;
        for (var it : getInterfaceKlasses()) {
            if ((found = it.findMethod(predicate)) != null)
                return found;
        }
        return null;
    }

    public <T> @Nullable Method findMethod(IndexMapper<Method, T> property, T value) {
        var method = Utils.find(methods, m -> Objects.equals(property.apply(m), value));
        if (method != null)
            return method;
        var superKlass = getSuperKlass();
        if (superKlass != null) {
            var m = superKlass.findMethod(property, value);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : getInterfaceKlasses()) {
                var m = it.findMethod(property, value);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    public void removeMethod(Method method) {
        methods.remove(method);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            getMethodTable().rebuild();
    }

    public void addMethod(Method method) {
        if (methods.contains(method))
            throw new InternalException("Method '" + method + "' is already added to the class type");
        methods.add(method);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            getMethodTable().rebuild();
        method.setDeclaringType(this);
    }
    //</editor-fold>

    public void addInnerKlass(Klass klass) {
        klasses.add(klass);
        klass.scope = this;
    }

    public List<Klass> getKlasses() {
        return Collections.unmodifiableList(klasses);
    }

    @Override
    public void addKlass(Klass klass) {
        addInnerKlass(klass);
    }

    public void setKlasses(List<Klass> klasses) {
        this.klasses.clear();
        this.klasses.addAll(klasses);
        klasses.forEach(k -> k.scope = this);
    }

    @JsonIgnore
    public List<Field> getDeclaredFields() {
        return fields;
    }

    @JsonIgnore
    public List<Index> getDeclaredConstraints() {
        return indices;
    }

    public void addField(Field field) {
        if (fields.contains(field))
            throw new RuntimeException("Field " + field.tryGetId() + " is already added");
        if (findSelfInstanceField(f -> f.getName().equals(field.getName())) != null
                || findSelfStaticField(f -> f.getName().equals(field.getName())) != null)
            throw BusinessException.invalidField(field, "Field name '" + field.getName() + "' is already used in class " + getName());
        if (field.isStatic())
            staticFields.add(field);
        else
            fields.add(field);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            resetFieldTransients();
        field.setDeclaringType(this);
    }

    @Override
    public void onLoadPrepare() {
        extensions = new ArrayList<>();
        implementations = new ArrayList<>();
    }

    @Override
    public void onLoad() {
        stage = ResolutionStage.INIT;
//        if(!Constants.maintenanceDisabled) {
//            closure = new Closure(this);
//            sortedFields = new ArrayList<>();
//            resetSortedFields();
//            sortedKlasses = new ArrayList<>();
//            tag2level = new HashMap<>();
//            resetSortedClasses();
//            if (superKlass != null)
//                superKlass.addExtension(this);
//            interfaces.forEach(it -> it.addImplementation(this));
//            resetSelfFieldOffset();
//            resetRank();
//        }
    }

    protected void addExtension(Klass klass) {
        if (!frozen)
            extensions.add(klass);
    }

    protected void addImplementation(Klass klass) {
        if (!frozen)
            implementations.add(klass);
    }

    private void forEachExtension(Consumer<Klass> action) {
        action.accept(this);
        extensions.forEach(k -> k.forEachExtension(action));
    }

    private void forEachSubclass(Consumer<Klass> action) {
        action.accept(this);
        extensions.forEach(k -> k.forEachSubclass(action));
        implementations.forEach(k -> k.forEachSubclass(action));
    }

    public void freeze() {
        if (frozen)
            throw new IllegalStateException("Already frozen");
        this.frozen = true;
    }

    public int getLevel() {
        return level;
    }

    public void addConstraint(Index constraint) {
        indices.add(constraint);
        constraint.setDeclaringType(this);
    }

    public void removeConstraint(Index constraint) {
        indices.remove(constraint);
    }

    @JsonIgnore
    public boolean isEnum() {
        return kind == ClassKind.ENUM;
    }

    @JsonIgnore
    public boolean isInterface() {
        return kind == ClassKind.INTERFACE;
    }

    @JsonIgnore
    public boolean isEffectiveAbstract() {
        return isInterface() || isAbstract;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @JsonIgnore
    public boolean isClass() {
        return kind == ClassKind.CLASS;
    }

    @JsonIgnore
    public boolean isValueKlass() {
        return this.kind == ClassKind.VALUE;
    }

    public Field getField(Id id) {
        var field = Utils.find(fields, f -> f.idEquals(id));
        if (field != null)
            return field;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.getField(id);
        throw new NullPointerException("Can not find field for " + id + " in type " + name);
    }

    public Method getMethod(Id id) {
        return Utils.findRequired(methods, m -> m.idEquals(id));
    }

    public @Nullable Field findSelfField(Predicate<Field> predicate) {
        var field = findSelfInstanceField(predicate);
        return field != null ? field : findSelfStaticField(predicate);
    }

    public @Nullable Field findSelfInstanceField(Predicate<Field> predicate) {
        return Utils.find(fields, predicate);
    }

    public Klass getInnerKlass(Predicate<Klass> filter) {
        return Objects.requireNonNull(findInnerKlass(filter));
    }

    public @Nullable Klass findInnerKlass(Predicate<Klass> filter) {
        return Utils.find(klasses, filter);
    }

    public Field getSelfField(Predicate<Field> predicate) {
        return Objects.requireNonNull(findSelfInstanceField(predicate));
    }

    public @Nullable Field findSelfStaticField(Predicate<Field> predicate) {
        return Utils.find(staticFields, predicate);
    }

    public @Nullable Field findField(Predicate<Field> predicate) {
        var field = findSelfInstanceField(predicate);
        if (field != null)
            return field;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.findField(predicate);
        return null;
    }

    public Field getField(Predicate<Field> predicate) {
        var found = findField(predicate);
        if (found != null)
            return found;
        if (DebugEnv.resolveVerbose)
            forEachField(f -> logger.info(f.getQualifiedName()));
        throw new NullPointerException("Fail to find field satisfying the specified criteria in klass: " + this);
    }

    public @Nullable Field findStaticField(Predicate<Field> predicate) {
        var field = Utils.find(staticFields, predicate);
        if (field != null)
            return field;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.findStaticField(predicate);
        return null;
    }

    public Field getFieldByName(String fieldName) {
        return Objects.requireNonNull(findFieldByName(fieldName), () -> "Cannot find field '" + fieldName + "' in class '" + getName() + "'");
    }

    public Field getStaticFieldByName(String fieldName) {
        return Objects.requireNonNull(findStaticFieldByName(fieldName),
                () -> "Static field " + fieldName + " not found in class " + getQualifiedName());
    }

    public Field getSelfStaticField(Predicate<Field> filter) {
        return Objects.requireNonNull(findSelfStaticField(filter));
    }

    /**
     * Get static field, instance method or static method by var.
     */
    public Property getStaticPropertyByVar(Var var) {
        return Objects.requireNonNull(
                findStaticPropertyByVar(var),
                () -> "Can not find property for " + var + " in type " + name
        );
    }

    /**
     * Find static field, instance method or static method by var.
     */
    public Property findStaticPropertyByVar(Var var) {
        if (var.isId()) {
            var p = findSelfStaticProperty(m -> m.idEquals(var.getId()));
            if (p != null)
                return p;
        } else {
            var p = findSelfStaticProperty(m -> m.getName().equals(var.getName()));
            if (p != null)
                return p;
        }
        var superKlass = getSuperKlass();
        if (superKlass != null) {
            var p = superKlass.findStaticPropertyByVar(var);
            if (p != null)
                return p;
        }
        for (var it : getInterfaceKlasses()) {
            var p = it.findStaticPropertyByVar(var);
            if (p != null)
                return p;
        }
        return null;
    }

    private Property findSelfStaticProperty(Predicate<Property> predicate) {
        var field = Utils.find(staticFields, predicate);
        if (field != null)
            return field;
        return Utils.find(methods, predicate);
    }

    public void setSource(ClassSource source) {
        this.source = source;
    }

    @Nullable
    public Field findFieldByName(String name) {
        var field = Utils.find(fields, f -> f.getName().equals(name));
        if (field != null)
            return field;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.findFieldByName(name);
        return null;
    }

    public Property getPropertyByVar(Var var) {
        return switch (var.getType()) {
            case NAME -> getPropertyByName(var.getName());
            case ID -> findProperty(p -> p.idEquals(var.getId()));
        };
    }

    public Property getProperty(Id id) {
        return Objects.requireNonNull(getProperty(Property::tryGetId, id),
                "Can not find attribute with id: " + id + " in type " + this);
    }

    public Property getPropertyByName(String name) {
        return getProperty(Property::getName, name);
    }

    public Property findProperty(Predicate<Property> filter) {
        var field = Utils.find(fields, filter);
        if(field != null)
            return field;
        var method = Utils.find(methods, m -> !m.isStatic() && filter.test(m));
        if(method != null)
            return method;
        var superKlass = getSuperKlass();
        if(superKlass != null)
            return superKlass.findProperty(filter);
        return null;
    }

    private <T> Property getProperty(IndexMapper<Property, T> property, T value) {
        var field = Utils.find(fields, f -> Objects.equals(property.apply(f), value));
        if (field != null)
            return field;
        var method = Utils.find(methods, m -> Objects.equals(property.apply(m), value));
        if (method != null)
            return method;
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.getProperty(property, value);
        return null;
    }

    @JsonIgnore
    public List<Property> getProperties() {
        return Utils.concatList(fields, methods);
    }

    public @Nullable Field findSelfFieldByName(String name) {
        var field = findSelfInstanceFieldByName(name);
        return field != null ? field : findSelfStaticFieldByName(name);
    }

    public Field getSelfFieldByName(String name) {
        return Objects.requireNonNull(findSelfFieldByName(name),
                () -> "Cannot find field '" + name + "' in klass '" + this.name + "'");
    }

    public Field getSelfInstanceFieldByName(String name) {
        return Objects.requireNonNull(findSelfInstanceFieldByName(name),
                () -> "Cannot find field \"" + name + "\" in klass " + getTypeDesc());
    }

    @Nullable
    public Field findSelfInstanceFieldByName(String name) {
        return Utils.find(fields, f -> f.getName().equals(name));
    }

    public Field getSelfStaticFieldByName(String name) {
        return Objects.requireNonNull(findSelfStaticFieldByName(name),
                () -> "Cannot find static field '" + name + "' in klass " + qualifiedName);
    }

    @Nullable
    public Field findSelfStaticFieldByName(String name) {
        return Utils.find(staticFields, f -> f.getName().equals(name));
    }

    @Nullable
    public Field findStaticFieldByName(String name) {
        var superKlass = getSuperKlass();
        if (superKlass != null) {
            Field superField = superKlass.findStaticFieldByName(name);
            if (superField != null)
                return superField;
        }
        return Utils.find(staticFields, f -> f.getName().equals(name));
    }

    @Nullable
    public Index findSelfIndex(Predicate<Index> predicate) {
        for (Constraint constraint : indices) {
            if (constraint instanceof Index index && predicate.test(index))
                return index;
        }
        return null;
    }

    public ClassSource getSource() {
        return source;
    }

    public boolean isBuiltin() {
        return source == ClassSource.BUILTIN;
    }

    public Field getFieldByJavaField(java.lang.reflect.Field javaField) {
        String fieldName = EntityUtils.getMetaFieldName(javaField);
        return Objects.requireNonNull(findFieldByName(fieldName),
                "Can not find field for java field " + javaField);
    }

    public boolean checkColumnAvailable(Column column) {
        return Utils.find(fields, f -> f.getColumn() == column) == null;
    }

    public boolean check() {
        return accept(new FlowChecker());
    }

    Column allocateColumn(Field field) {
        Type fieldType = field.getType();
        if (fieldType.isBinaryNullable())
            fieldType = fieldType.getUnderlyingType();
        if (fieldType.getSQLType() == null)
            return null;
        return allocateColumn(fieldType, field);
    }

    public Column allocateColumn(Type fieldType, Field field) {
        Set<Column> usedColumns = filterAndMapUnique(
                getFieldsInHierarchy(),
                f -> !f.equals(field),
                Field::getColumn
        );
        return Column.allocate(usedColumns, fieldType.getSQLType());
    }


    private List<Field> getFieldsInHierarchy() {
        return fields;
    }

    public Field getFieldNyName(String fieldName) {
        return Objects.requireNonNull(
                findFieldByName(fieldName), "Cannot find field '" + fieldName + "' in klass " + qualifiedName
        );
    }

    public void removeField(Field field) {
        if (field.isStatic())
            staticFields.remove(field);
        else
            fields.remove(field);
        if(!Constants.maintenanceDisabled)
            resetFieldTransients();
    }

    public boolean isAssignableFrom(Klass that) {
        if (this == that) {
            return true;
        }
        var thatSuperKlass = that.getSuperKlass();
        if (thatSuperKlass != null && isAssignableFrom(thatSuperKlass))
            return true;
        if (isInterface()) {
            for (var it : that.getInterfaceKlasses()) {
                if (isAssignableFrom(it)) {
                    return true;
                }
            }
        }
        return false;
    }

    public @NotNull ClassType getType() {
        if (type == null) {
            var owner = switch (scope) {
                case Klass k -> k.getType();
                case Flow flow -> flow.getRef();
                case null -> null;
                default -> throw new IllegalStateException("Unrecognized klass declaration: " + scope.getClass().getName());
            };
            type = new KlassType(owner, this, List.of());
        }
        return type;
    }

    @Override
    public String getTitle() {
        return name;
    }

    public void setType(ClassType type) {
        this.type = type;
    }

    @Nullable
    public ClassType getSuperType() {
        return superTypeIndex != null ? constantPool.getClassType(superTypeIndex) : null;
    }

    @Nullable
    public Integer getSuperTypeIndex() {
        return superTypeIndex;
    }

    @Nullable
    @JsonIgnore
    public Klass getSuperKlass() {
        var superType = getSuperType();
        return superType != null ? superType.getKlass() : null;
    }

    @JsonIgnore
    public List<Klass> getInterfaceKlasses() {
        return Utils.map(getInterfaces(), ClassType::getKlass);
    }

    public List<ClassType> getInterfaces() {
        return Utils.map(interfaceIndexes, constantPool::getClassType);
    }

    public List<Integer> getInterfaceIndexes() {
        return Collections.unmodifiableList(interfaceIndexes);
    }

    public void forEachSuper(Consumer<Klass> action) {
        var superKlass = getSuperKlass();
        if (superKlass != null)
            action.accept(superKlass);
        Objects.requireNonNull(getInterfaceKlasses(), () -> "Klass " + qualifiedName + " is yet initialized")
                .forEach(action);
    }

    public void forEachSuperType(Consumer<ClassType> action, TypeMetadata typeMetadata) {
        if(superTypeIndex != null)
            action.accept((ClassType) typeMetadata.getType(superTypeIndex));
        interfaceIndexes.forEach(i -> action.accept((ClassType) typeMetadata.getType(i)));
    }

    public void forEachSuperClass(Consumer<Klass> action) {
        action.accept(this);
        var superKlass = getSuperKlass();
        if (superKlass != null)
            superKlass.forEachSuperClass(action);
    }

    public void forEachSuperClassTopDown(Consumer<Klass> action) {
        var superKlass = getSuperKlass();
        if (superKlass != null)
            superKlass.forEachSuperClassTopDown(action);
        action.accept(this);
    }

    public <T extends Constraint> List<T> getAllConstraints(Class<T> constraintType) {
        List<T> result = filterAndMap(
                indices,
                constraintType::isInstance,
                constraintType::cast
        );
        var superKlass = getSuperKlass();
        if (superKlass != null) {
            result = Utils.union(
                    superKlass.getAllConstraints(constraintType),
                    result
            );
        }
        return result;
    }

    public <T extends Constraint> T getConstraint(Class<T> constraintType, Id id) {
        return find(getAllConstraints(constraintType), c -> c.getId().equals(id));
    }

    @SuppressWarnings("unused")
    public Constraint getConstraint(Id id) {
        return Utils.find(Objects.requireNonNull(indices), c -> c.idEquals(id));
    }

    public Index getUniqueConstraint(Id id) {
        return getConstraint(Index.class, id);
    }

    public boolean isType(Type type) {
        return getType().equals(type);
    }

    // BFS
    public void foreachAncestor(Consumer<Klass> action) {
        var queue = new LinkedList<Klass>();
        queue.offer(this);
        while (!queue.isEmpty()) {
            var k = queue.poll();
            action.accept(k);
            k.forEachSuper(queue::offer);
        }
    }

    public void forEachMethod(Consumer<Method> action) {
        methods.forEach(action);
        var superKlass = getSuperKlass();
        if (superKlass != null)
            superKlass.forEachMethod(action);
        getInterfaceKlasses().forEach(it -> it.forEachMethod(action));
    }

    @Nullable
    public MethodRef findOverride(Method method, TypeMetadata typeMetadata) {
        return getMethodTable().findOverride(method, typeMetadata);
    }

    public void updateParameterized() {
        ParameterizedStore.forEach(constantPool, (typeArgs, k) -> {
            var p = (ConstantPool) k;
            p.setStage(ResolutionStage.INIT);
            var subst = new SubstitutorV2(
                    this, typeParameters, typeArgs, k, stage);
            constantPool.accept(subst);
        });
    }

    public List<TypeVariable> getTypeParameters() {
        return Collections.unmodifiableList(typeParameters);
    }

    public TypeVariable getTypeParameterByName(String name) {
        return Utils.findRequired(typeParameters, tp -> tp.getName().equals(name),
                () -> "Cannot find type parameter with name '" + name + "' in class '" + this + "'");
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        isTemplate = true;
        typeParameters.add(typeParameter);
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Nullable
    @Override
    public String getQualifiedName() {
        return qualifiedName;
    }

    public String getClassFilePath() {
        return Objects.requireNonNull(qualifiedName).replace('.', '/') + ".mvclass";
    }

    public void setQualifiedName(@Nullable String qualifiedName) {
        this.qualifiedName = qualifiedName;
    }

    public ClassKind getKind() {
        return kind;
    }

    public void setKind(ClassKind kind) {
        this.kind = kind;
    }

    public boolean isAnonymous() {
        return anonymous;
    }

    public void setAnonymous(boolean anonymous) {
        this.anonymous = anonymous;
    }

    @Nullable
    @Override
    public org.metavm.entity.Entity getParentEntity() {
        return (org.metavm.entity.Entity) scope;
    }

    public boolean isEphemeralKlass() {
        return ephemeral;
    }

    public void setEphemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
    }

    public boolean isSearchable() {
        return searchable;
    }

    public void setSearchable(boolean searchable) {
        this.searchable = searchable;
    }

    @JsonIgnore
    public List<Type> getDefaultTypeArguments() {
        return Utils.map(typeParameters, TypeVariable::getType);
    }

    public List<Index> getIndices() {
        return Collections.unmodifiableList(indices);
    }

    @JsonIgnore
    public List<Index> getAllIndices() {
        return getAllConstraints(Index.class);
    }

    @Override
    public String toString() {
        return "Klass " + getTypeDesc();
    }

    protected final void onSuperTypesChanged() {
        onAncestorChanged();
        resetSortedClasses();
        resetFieldOffsets();
    }

    public void resetHierarchy() {
        closure = new Closure(this);
        getMethodTable().rebuild();
        resetRank();
        resetSortedClasses();
        resetSortedFields();
        resetSelfFieldOffset();
    }

    @JsonIgnore
    public boolean isInitialized() {
        return sortedFields != null;
    }

    private void onAncestorChanged() {
        forEachSubclass(Klass::onAncestorChangedSelf);
    }

    public void setSuperType(@Nullable ClassType superType) {
        setSuperType(superType, Constants.maintenanceDisabled);
    }

    public void setSuperTypeIndex(@Nullable Integer superTypeIndex) {
        this.superTypeIndex = superTypeIndex;
    }

    public void setSuperType(@Nullable ClassType superType, boolean skipMaintenance) {
        if(skipMaintenance) {
            if (superType != null) {
                this.superTypeIndex = constantPool.addValue(superType);
                this.superType = superType;
            }
            else {
                superTypeIndex = null;
                this.superType = null;
            }
        }
        else {
            var superKlass = getSuperKlass();
            if (superKlass != null) {
                superKlass.removeExtension(this);
            }
            if (superType != null) {
                this.superTypeIndex = constantPool.addValue(superType);
                superKlass = superType.getKlass();
                this.superType = superType;
                superKlass.addExtension(this);
            } else {
                this.superType = null;
                this.superTypeIndex = null;
            }
            onSuperTypesChanged();
        }
    }

    public void setInterfaces(List<ClassType> interfaces) {
        setInterfaces(interfaces, Constants.maintenanceDisabled);
    }

    public void setInterfaceIndexes(List<Integer> interfaceIndexes) {
        this.interfaceIndexes = new ArrayList<>(interfaceIndexes);
        this.interfaces = Utils.map(interfaceIndexes, constantPool::getClassType);
    }

    public void setInterfaces(List<ClassType> interfaces, boolean skipMaintenance) {
        if(skipMaintenance) {
            Objects.requireNonNull(this.getInterfaceKlasses(), () -> "Klass " + qualifiedName + " is not yet initialized").clear();
            this.interfaces.clear();
            this.interfaces.addAll(interfaces);
            this.interfaceIndexes.clear();
            this.interfaceIndexes.addAll(Utils.map(interfaces, constantPool::addValue));
        }
        else {
            for (var anInterface : this.getInterfaceKlasses()) {
                anInterface.removeImplementation(this);
            }
            this.interfaces.clear();
            this.interfaceIndexes.clear();
            for (var anInterface : interfaces) {
                this.interfaces.add(anInterface);
                this.interfaceIndexes.add(constantPool.addValue(anInterface));
                anInterface.getKlass().addImplementation(this);
            }
            onSuperTypesChanged();
        }
    }

    protected void onAncestorChangedSelf() {
        closure = new Closure(this);
        if(!methodTableBuildDisabled)
            getMethodTable().rebuild();
        resetRank();
    }

    public void setTypeParameters(List<TypeVariable> typeParameters) {
        this.isTemplate = !typeParameters.isEmpty();
        typeParameters.forEach(tp -> tp.setGenericDeclaration(this));
        this.typeParameters.clear();
        this.typeParameters.addAll(typeParameters);
    }

    public void setFields(List<Field> fields) {
        fields.forEach(f -> f.setDeclaringType(this));
        this.fields.clear();
        this.fields.addAll(fields);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            resetFieldTransients();
    }

    public boolean isLocal() {
        return scope instanceof Flow;
    }

    public void setStaticFields(List<Field> staticFields) {
        staticFields.forEach(f -> f.setDeclaringType(this));
        this.staticFields.clear();
        this.staticFields.addAll(staticFields);
    }

    public void setIndices(List<Index> indices) {
        indices.forEach(c -> c.setDeclaringType(this));
        this.indices.clear();
        this.indices.addAll(indices);
    }

    public void setMethods(List<Method> methods) {
        methods.forEach(m -> m.setDeclaringType(this));
        this.methods.clear();
        this.methods.addAll(methods);
        if(!Constants.maintenanceDisabled && !methodTableBuildDisabled)
            rebuildMethodTable();
    }

    @Override
    public String getTypeDesc() {
        if(scope != null)
            return scope.getTypeDesc() + "." + name;
        else
            return Objects.requireNonNullElse(qualifiedName, name);
    }

    @Override
    public List<Instance> beforeRemove(IInstanceContext context) {
        var superKlass = getSuperKlass();
        if (superKlass != null)
            superKlass.removeExtension(this);
        for (var anInterface : getInterfaceKlasses()) {
            anInterface.removeImplementation(this);
        }
        var cascade = new ArrayList<Instance>();
        var sft = context.selectFirstByKey(StaticFieldTable.IDX_KLASS, getReference());
        if (sft != null)
            cascade.add(sft);
        return cascade;
    }

    public ResolutionStage setStage(ResolutionStage stage) {
        var origStage = this.stage;
        this.stage = stage;
        return origStage;
    }

    public ResolutionStage getStage() {
        if (stage == null)
            stage = ResolutionStage.DEFINITION;
        return stage;
    }

    public void rebuildMethodTable() {
        getMethodTable().rebuild();
    }

    public void removeErrors(Element element) {
        errors.removeIf(e -> e.getElement() == element);
    }

    @Override
    public void onChange(IInstanceContext context) {
        if(!methodTableBuildDisabled)
            rebuildMethodTable();
        if (!isInterface()) {
            for (var it : getInterfaceKlasses()) {
                for(var method : it.getMethods()) {
                    if (!method.isStatic() && findOverride(method, constantPool) == null) {
                        throw new BusinessException(ErrorCode.INTERFACE_FLOW_NOT_IMPLEMENTED,
                                getName(), method.getName(), it.getName());
                    }
                }
            }
        }
    }

    @Override
    public boolean isChangeAware() {
        return !anonymous;
    }

    @JsonIgnore
    public @Nullable Field getSelfTitleField() {
        return Utils.safeCall(titleField, r -> (Field) r.get());
    }

    @Nullable
    public Field getTitleField() {
        if (titleField != null)
            return getSelfTitleField();
        var superKlass = getSuperKlass();
        if (superKlass != null)
            return superKlass.getTitleField();
        return null;
    }

    public List<Field> getStaticFields() {
        return Collections.unmodifiableList(staticFields);
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        return getQualifiedName();
    }

    @Override
    public GenericDeclarationRef getRef() {
        return getType();
    }

    @JsonIgnore
    public boolean isList() {
        return StdKlass.list.get().isAssignableFrom(this);
    }
    @JsonIgnore
    public boolean isSAMInterface() {
        return isInterface() && Utils.count(methods, m -> m.isAbstract() && !m.isStatic()) == 1;
    }

    @JsonIgnore
    public Method getSingleAbstractMethod() {
        if (!isSAMInterface())
            throw new InternalException("Type " + getName() + " is not a SAM interface");
        return getMethod(m -> m.isAbstract() && !m.isStatic());
    }

    public boolean isStruct() {
        return struct;
    }

    public void setStruct(boolean struct) {
        this.struct = struct;
    }

    public void setAbstract(boolean anAbstract) {
        isAbstract = anAbstract;
    }

    public void sortMethods(Comparator<Method> comparator) {
        methods.sort(comparator);
    }

    public int nextFieldTag() {
        return nextFieldTag++;
    }

    public int getNextFieldTag() {
        return nextFieldTag;
    }

    public int getNextFieldSourceCodeTag() {
        return nextFieldSourceCodeTag;
    }

    public void setNextFieldTag(int nextFieldTag) {
        this.nextFieldTag = nextFieldTag;
    }

    public void setNextFieldSourceCodeTag(int nextFieldSourceCodeTag) {
        this.nextFieldSourceCodeTag = nextFieldSourceCodeTag;
    }

    @JsonIgnore
    public Class<? extends NativeBase> getNativeClass() {
        return nativeClass;
    }

    public void setNativeClass(Class<? extends NativeBase> nativeClass) {
        this.nativeClass = nativeClass;
    }

    public List<Field> getEnumConstants() {
        return Utils.filter(staticFields, Field::isEnumConstant);
    }

    public void clearEnumConstantDefs() {
        staticFields.forEach(f -> {
            f.setEnumConstant(false);
            f.setOrdinal(-1);
        });
    }

    public int nextFieldSourceCodeTag() {
        return nextFieldSourceCodeTag++;
    }

    public int getSince() {
        return since;
    }

    @Nullable
    public KlassFlags getFlags() {
        return flags;
    }

    public boolean isFlag1() {
        return flags != null && flags.isFlag1();
    }

    @JsonIgnore
    public boolean isBeanClass() {
        return getAttribute(AttributeNames.BEAN_KIND) != null;
    }

    @JsonIgnore
    public @Nullable Method getHashCodeMethod() {
        return methodTable.getHashCodeMethod();
    }

    @JsonIgnore
    public @Nullable Method getToStringMethod() {
        return methodTable.getToStringMethod();
    }

    @JsonIgnore
    public @Nullable Method getEqualsMethod() {
        return methodTable.getEqualsMethod();
    }

    @JsonIgnore
    public Klass getArrayKlass() {
       if(arrayKlass == null) {
           arrayKlass = KlassBuilder.newBuilder(new NullId(), name + "[]", Utils.safeCall(qualifiedName, c -> c + "[]")).build();
           arrayKlass.setEphemeral();
           arrayKlass.componentKlass = this;
       }
       return arrayKlass;
    }

    @JsonIgnore
    public Klass getComponentKlass() {
        return componentKlass;
    }

    public boolean isOverrideOf(Method override, Method overridden) {
        return methodTable.findOverride(overridden, constantPool).getRawFlow() == override;
    }

    public void emitCode() {
        accept(new MaxesComputer());
        methods.forEach(Flow::emitCode);
        klasses.forEach(Klass::emitCode);
    }

    public void setTag(long tag) {
        this.tag = tag;
    }

    public void setSourceTag(@Nullable Integer sourceTag) {
        this.sourceTag = sourceTag;
    }

    public static final int FLAG_ABSTRACT = 1;
    public static final int FLAG_STRUCT = 2;
    public static final int FLAG_SEARCHABLE = 4;
    public static final int FLAG_EPHEMERAL = 8;
    public static final int FLAG_ANONYMOUS = 16;
    public static final int FLAG_TEMPLATE = 32;

    public int getClassFlags() {
        int flags = 0;
        if(isAbstract)
            flags |= FLAG_ABSTRACT;
        if(struct)
            flags |= FLAG_STRUCT;
        if(searchable)
            flags |= FLAG_SEARCHABLE;
        if(ephemeral)
            flags |= FLAG_EPHEMERAL;
        if(anonymous)
            flags |= FLAG_ANONYMOUS;
        if(templateFlag)
            flags |= FLAG_TEMPLATE;
        return flags;
    }

    public void setClassFlags(int flags) {
        isAbstract = (flags & FLAG_ABSTRACT) != 0;
        struct = (flags & FLAG_STRUCT) != 0;
        searchable = (flags & FLAG_SEARCHABLE) != 0;
        ephemeral = (flags & FLAG_EPHEMERAL) != 0;
        anonymous = (flags & FLAG_ANONYMOUS) != 0;
        templateFlag = (flags & FLAG_TEMPLATE) != 0;
    }

    public void setSince(int since) {
        this.since = since;
    }

    public boolean isInner() {
        return scope != null;
    }

    @Nullable
    public KlassDeclaration getScope() {
        return scope;
    }

    public void setScope(@Nullable KlassDeclaration scope) {
        this.scope = scope;
    }

    @JsonIgnore
    @Override
    public boolean isValidLocalKey() {
        return true;
    }

    @Override
    public String getLocalKey(@NotNull BuildKeyContext context) {
        return name;
    }

    public ConstantPool getConstantPool() {
        return constantPool;
    }

    public int addConstant(Value value) {
        return constantPool.addValue(value);
    }

    public ConstantPool getExistingTypeMetadata(List<? extends Type> typeArguments) {
        if (Utils.map(getAllTypeParameters(), TypeVariable::getType).equals(typeArguments))
            return constantPool;
        return (ConstantPool) ParameterizedStore.get(this, typeArguments);
    }

    private ConstantPool createTypeMetadata(List<? extends Type> typeArguments) {
        return new ConstantPool(this, typeArguments);
    }

    public void addTypeMetadata(ConstantPool parameterized) {
        var existing = ParameterizedStore.get(this, parameterized.typeArguments.secretlyGetTable());
        if(existing != null)
            throw new IllegalStateException("Parameterized klass " + parameterized + " already exists. "
                    + "existing: " + System.identityHashCode(existing) + ", new: "+ System.identityHashCode(parameterized)
            );
        Utils.require(ParameterizedStore.put(this, parameterized.typeArguments.secretlyGetTable(), parameterized) == null,
                () -> "Parameterized klass " + parameterized + " already exists");
    }

    public boolean isConstantPoolParameterized() {
        return isTemplate || (scope != null && scope.isConstantPoolParameterized());
    }

    public TypeMetadata getTypeMetadata(List<Type> typeArguments) {
        if (!isConstantPoolParameterized()) {
            if (typeArguments.isEmpty())
                return constantPool;
            else
                throw new InternalException(this + " is not a template class");
        }
//        typeArguments.forEach(Type::getTypeDesc);
        var typeMetadata = getExistingTypeMetadata(typeArguments);
        if (typeMetadata == constantPool)
            return constantPool;
        if(typeMetadata == null) {
            typeMetadata = createTypeMetadata(typeArguments);
            addTypeMetadata(typeMetadata);
        }
        else if (typeMetadata.getStage().isAfterOrAt(stage))
            return typeMetadata;
        var subst = new SubstitutorV2(
                constantPool, getAllTypeParameters(), typeArguments, typeMetadata, stage);
        typeMetadata = (ConstantPool) constantPool.accept(subst);
        return typeMetadata;
    }

    @JsonIgnore
    public List<TypeVariable> getAllTypeParameters() {
        var typeParams = new ArrayList<TypeVariable>();
        foreachGenericDeclaration(d -> typeParams.addAll(d.getTypeParameters()));
        return typeParams;
    }

    public void foreachGenericDeclaration(Consumer<GenericDeclaration> action) {
        if(scope != null)
            scope.foreachGenericDeclaration(action);
        action.accept(this);
    }

    public void disableMethodTableBuild() {
        methodTableBuildDisabled = true;
    }

    private void onRead() {
        stage = ResolutionStage.INIT;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitKlass(this);
    }

    @Override
    public void acceptChildren(ElementVisitor<?> visitor) {
        super.acceptChildren(visitor);
        fields.forEach(arg -> arg.accept(visitor));
        methods.forEach(arg -> arg.accept(visitor));
        klasses.forEach(arg -> arg.accept(visitor));
        staticFields.forEach(arg -> arg.accept(visitor));
        indices.forEach(arg -> arg.accept(visitor));
        typeParameters.forEach(arg -> arg.accept(visitor));
        constantPool.accept(visitor);
    }

    public String getText() {
        var writer = new CodeWriter();
        writeCode(writer);
        return writer.toString();
    }

    public void writeCode(CodeWriter writer) {
        writer.write(kind.name().toLowerCase() + " " + name);
        var supType = getSuperType();
        if (supType != null)
            writer.write(" extends " + supType.getTypeDesc());
        if (!interfaces.isEmpty())
            writer.write(" implements " + Utils.join(interfaces, Type::getTypeDesc));
        writer.writeln(" {");
        writer.indent();
        for (Field field : fields) {
            field.writeCode(writer);
        }
        for (Field staticField : staticFields) {
            staticField.writeCode(writer);
        }
        for (Index index : indices) {
            index.writeCode(writer);
        }
        for (Method method : methods) {
            method.writeCode(writer);
        }
        for (Klass klass : klasses) {
            klass.writeCode(writer);
        }
        writer.unindent();
        writer.writeln("}");
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        super.forEachReference(action);
        if (superType != null) superType.forEachReference(action);
        for (var interfaces_ : interfaces) interfaces_.forEachReference(action);
        for (var fields_ : fields) action.accept(fields_.getReference());
        if (titleField != null) action.accept(titleField);
        for (var methods_ : methods) action.accept(methods_.getReference());
        for (var klasses_ : klasses) action.accept(klasses_.getReference());
        for (var staticFields_ : staticFields) action.accept(staticFields_.getReference());
        for (var indices_ : indices) action.accept(indices_.getReference());
        for (var typeParameters_ : typeParameters) action.accept(typeParameters_.getReference());
        for (var errors_ : errors) errors_.forEachReference(action);
        if (flags != null) action.accept(flags.getReference());
        constantPool.forEachReference(action);
    }

    @Override
    public void buildJson(Map<String, Object> map) {
        map.put("tag2level", this.getTag2level());
        var desc = this.getDesc();
        if (desc != null) map.put("desc", desc);
        map.put("fields", this.getFields().stream().map(Entity::getStringId).toList());
        map.put("tag", this.getTag());
        var sourceTag = this.getSourceTag();
        if (sourceTag != null) map.put("sourceTag", sourceTag);
        map.put("errors", this.getErrors().stream().map(Error::toJson).toList());
        map.put("error", this.isError());
        map.put("state", this.getState().name());
        map.put("methods", this.getMethods().stream().map(Entity::getStringId).toList());
        map.put("klasses", this.getKlasses().stream().map(Entity::getStringId).toList());
        map.put("level", this.getLevel());
        map.put("abstract", this.isAbstract());
        map.put("source", this.getSource().name());
        map.put("builtin", this.isBuiltin());
        map.put("type", this.getType().toJson());
        var superType = this.getSuperType();
        if (superType != null) map.put("superType", superType.toJson());
        var superTypeIndex = this.getSuperTypeIndex();
        if (superTypeIndex != null) map.put("superTypeIndex", superTypeIndex);
        map.put("interfaces", this.getInterfaces().stream().map(ClassType::toJson).toList());
        map.put("interfaceIndexes", this.getInterfaceIndexes());
        map.put("typeParameters", this.getTypeParameters().stream().map(Entity::getStringId).toList());
        map.put("name", this.getName());
        var qualifiedName = this.getQualifiedName();
        if (qualifiedName != null) map.put("qualifiedName", qualifiedName);
        map.put("classFilePath", this.getClassFilePath());
        map.put("kind", this.getKind().name());
        map.put("anonymous", this.isAnonymous());
        map.put("ephemeralKlass", this.isEphemeralKlass());
        map.put("indices", this.getIndices().stream().map(Entity::getStringId).toList());
        map.put("local", this.isLocal());
        var titleField = this.getTitleField();
        if (titleField != null) map.put("titleField", titleField.getStringId());
        map.put("staticFields", this.getStaticFields().stream().map(Entity::getStringId).toList());
        map.put("struct", this.isStruct());
        map.put("nextFieldTag", this.getNextFieldTag());
        map.put("nextFieldSourceCodeTag", this.getNextFieldSourceCodeTag());
        map.put("enumConstants", this.getEnumConstants().stream().map(Entity::getStringId).toList());
        map.put("since", this.getSince());
        var flags = this.getFlags();
        if (flags != null) map.put("flags", flags.getStringId());
        map.put("flag1", this.isFlag1());
        map.put("classFlags", this.getClassFlags());
        map.put("inner", this.isInner());
        var scope = this.getScope();
        if (scope != null) map.put("scope", scope.getStringId());
        map.put("constantPool", this.getConstantPool().toJson());
        map.put("attributes", this.getAttributes().stream().map(Attribute::toJson).toList());
    }

    @Override
    public Klass getInstanceKlass() {
        return __klass__;
    }

    @Override
    public ClassType getInstanceType() {
        return __klass__.getType();
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
        super.forEachChild(action);
        for (var fields_ : fields) action.accept(fields_);
        for (var methods_ : methods) action.accept(methods_);
        for (var klasses_ : klasses) action.accept(klasses_);
        for (var staticFields_ : staticFields) action.accept(staticFields_);
        for (var indices_ : indices) action.accept(indices_);
        for (var typeParameters_ : typeParameters) action.accept(typeParameters_);
        if (flags != null) action.accept(flags);
    }

    @Override
    public int getEntityTag() {
        return EntityRegistry.TAG_Klass;
    }

    @Generated
    @Override
    public void readBody(MvInput input, Entity parent) {
        super.readBody(input, parent);
        this.scope = (KlassDeclaration) parent;
        this.sourceTag = input.readNullable(input::readInt);
        this.name = input.readUTF();
        this.qualifiedName = input.readNullable(input::readUTF);
        this.kind = ClassKind.fromCode(input.read());
        this.anonymous = input.readBoolean();
        this.ephemeral = input.readBoolean();
        this.searchable = input.readBoolean();
        this.superType = input.readNullable(() -> (ClassType) input.readType());
        this.interfaces = input.readList(() -> (ClassType) input.readType());
        this.superTypeIndex = input.readNullable(input::readInt);
        this.interfaceIndexes = input.readList(input::readInt);
        this.source = ClassSource.fromCode(input.read());
        this.desc = input.readNullable(input::readUTF);
        this.nextFieldTag = input.readInt();
        this.nextFieldSourceCodeTag = input.readInt();
        this.fields = input.readList(() -> input.readEntity(Field.class, this));
        this.titleField = input.readNullable(() -> (Reference) input.readValue());
        this.methods = input.readList(() -> input.readEntity(Method.class, this));
        this.klasses = input.readList(() -> input.readEntity(Klass.class, this));
        this.staticFields = input.readList(() -> input.readEntity(Field.class, this));
        this.indices = input.readList(() -> input.readEntity(Index.class, this));
        this.isAbstract = input.readBoolean();
        this.isTemplate = input.readBoolean();
        this.isParameterized = input.readBoolean();
        this.typeParameters = input.readList(() -> input.readEntity(TypeVariable.class, this));
        this.errors = input.readList(() -> Error.read(input));
        this.error = input.readBoolean();
        this.flags = input.readNullable(() -> input.readEntity(KlassFlags.class, this));
        this.state = ClassTypeState.fromCode(input.read());
        this.tag = input.readLong();
        this.since = input.readInt();
        this.templateFlag = input.readBoolean();
        this.struct = input.readBoolean();
        this.dummyFlag = input.readBoolean();
        this.methodTableBuildDisabled = input.readBoolean();
        this.constantPool = ConstantPool.read(input, this);
        this.allFlag = input.readBoolean();
        this.onRead();
    }

    @Generated
    @Override
    public void writeBody(MvOutput output) {
        super.writeBody(output);
        output.writeNullable(sourceTag, output::writeInt);
        output.writeUTF(name);
        output.writeNullable(qualifiedName, output::writeUTF);
        output.write(kind.code());
        output.writeBoolean(anonymous);
        output.writeBoolean(ephemeral);
        output.writeBoolean(searchable);
        output.writeNullable(superType, output::writeValue);
        output.writeList(interfaces, output::writeValue);
        output.writeNullable(superTypeIndex, output::writeInt);
        output.writeList(interfaceIndexes, output::writeInt);
        output.write(source.code());
        output.writeNullable(desc, output::writeUTF);
        output.writeInt(nextFieldTag);
        output.writeInt(nextFieldSourceCodeTag);
        output.writeList(fields, output::writeEntity);
        output.writeNullable(titleField, output::writeValue);
        output.writeList(methods, output::writeEntity);
        output.writeList(klasses, output::writeEntity);
        output.writeList(staticFields, output::writeEntity);
        output.writeList(indices, output::writeEntity);
        output.writeBoolean(isAbstract);
        output.writeBoolean(isTemplate);
        output.writeBoolean(isParameterized);
        output.writeList(typeParameters, output::writeEntity);
        output.writeList(errors, arg0 -> arg0.write(output));
        output.writeBoolean(error);
        output.writeNullable(flags, output::writeEntity);
        output.write(state.code());
        output.writeLong(tag);
        output.writeInt(since);
        output.writeBoolean(templateFlag);
        output.writeBoolean(struct);
        output.writeBoolean(dummyFlag);
        output.writeBoolean(methodTableBuildDisabled);
        constantPool.write(output);
        output.writeBoolean(allFlag);
    }

    @Override
    protected void buildSource(Map<String, Value> source) {
        super.buildSource(source);
    }
}

