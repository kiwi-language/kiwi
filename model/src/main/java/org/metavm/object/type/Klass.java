package org.metavm.object.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.metavm.api.ChildEntity;
import org.metavm.api.EntityField;
import org.metavm.api.EntityType;
import org.metavm.common.ErrorCode;
import org.metavm.entity.*;
import org.metavm.entity.natives.ListNative;
import org.metavm.entity.natives.NativeBase;
import org.metavm.expression.Var;
import org.metavm.flow.Error;
import org.metavm.flow.*;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.Id;
import org.metavm.object.type.generic.SubstitutorV2;
import org.metavm.object.type.generic.TypeSubstitutor;
import org.metavm.object.type.rest.dto.KlassDTO;
import org.metavm.object.view.MappingSaver;
import org.metavm.object.view.ObjectMapping;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import static org.metavm.util.NncUtils.*;

@EntityType(searchable = true)
public class Klass extends TypeDef implements GenericDeclaration, ChangeAware, GenericElement, StagedEntity, GlobalKey, LoadAware {

    public static final Logger debugLogger = LoggerFactory.getLogger("Debug");

    public static final Logger logger = LoggerFactory.getLogger(Klass.class);

    public static final IndexDef<Klass> IDX_NAME = IndexDef.create(Klass.class, "name");

    public static final IndexDef<Klass> UNIQUE_CODE = IndexDef.createUnique(Klass.class, "code");

    public static final IndexDef<Klass> UNIQUE_SOURCE_CODE_TAG = IndexDef.createUnique(Klass.class, "sourceCodeTag");

    public static final IndexDef<Klass> TEMPLATE_IDX = IndexDef.create(Klass.class, "template");

    @EntityField(asTitle = true)
    private String name;
    @Nullable
    private String code;
    private ClassKind kind;
    private boolean anonymous;
    private boolean ephemeral;
    private boolean searchable;
    @Nullable
    private ClassType superType;
    @ChildEntity
    private final ReadWriteArray<ClassType> interfaces = addChild(new ReadWriteArray<>(ClassType.class), "interfaces");
    private ClassSource source;
    @Nullable
    private String desc;
    @ChildEntity
    private final ChildArray<Field> fields = addChild(new ChildArray<>(Field.class), "fields");
    @Nullable
    private Field titleField;
    @ChildEntity
    private final ChildArray<Method> methods = addChild(new ChildArray<>(Method.class), "methods");

    @ChildEntity
    private final ChildArray<Field> staticFields = addChild(new ChildArray<>(Field.class), "staticFields");
    @ChildEntity
    private final ChildArray<Constraint> constraints = addChild(new ChildArray<>(Constraint.class), "constraints");
    @ChildEntity
    private final ChildArray<EnumConstantDef> enumConstantDefs = addChild(new ChildArray<>(EnumConstantDef.class), "enumConstantDefs");
    @Nullable
    private Klass template;
    // Don't remove, for search
    @SuppressWarnings("unused")
    private boolean isAbstract;
    private boolean isTemplate;
    // Don't remove, used for search
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private boolean isParameterized;
    @ChildEntity
    private final ChildArray<TypeVariable> typeParameters = addChild(new ChildArray<>(TypeVariable.class), "typeParameters");
    @ChildEntity
    private final ReadWriteArray<Type> typeArguments = addChild(new ReadWriteArray<>(Type.class), "typeArguments");
    @ChildEntity
    private final ChildArray<Error> errors = addChild(new ChildArray<>(Error.class), "errors");

    private boolean error;

    // For unit test. Do not remove
    @ChildEntity(since = 1)
    @Nullable
    private KlassFlags flags;

    @ChildEntity
    private final ChildArray<ObjectMapping> mappings = addChild(new ChildArray<>(ObjectMapping.class), "mappings");

    @Nullable
    private ObjectMapping defaultMapping;

    @CopyIgnore
    private @Nullable Klass copySource;

    private int nextFieldTag;
    private int nextFieldSourceCodeTag = 1000000;

    private ClassTypeState state = ClassTypeState.INIT;

    private final @Nullable Integer sourceCodeTag;

    private final long tag;

    private final int since;

    @SuppressWarnings({"unused", "FieldCanBeLocal"})
    private boolean templateFlag = false;

    private boolean struct;

    @SuppressWarnings({"FieldMayBeFinal", "unused"}) // for unit test
    private boolean dummyFlag = false;

    private transient ResolutionStage stage = ResolutionStage.INIT;

    private transient volatile MethodTable methodTable;

    // length of the longest path from the current type upwards to a root in the type hierarchy
    private transient int rank;

    private transient int level;

    private transient List<Klass> extensions = new ArrayList<>();

    private transient List<Klass> implementations = new ArrayList<>();

    private transient List<Klass> sortedKlasses = new ArrayList<>();

    private transient List<Field> sortedFields = new ArrayList<>();

    private transient int numFields;

    private transient Closure closure;

    private transient ClassType type;

    private transient Class<? extends NativeBase> nativeClass;

    private transient boolean frozen;

    private transient Klass arrayKlass;

    private transient Klass componentKlass;

    public Klass(
            Long tmpId,
            String name,
            @Nullable String code,
            @Nullable ClassType superType,
            List<ClassType> interfaces,
            @NotNull ClassKind kind,
            ClassSource source,
            @Nullable Klass template,
            boolean anonymous,
            boolean ephemeral,
            boolean struct,
            boolean searchable,
            @Nullable String desc,
            boolean isAbstract,
            boolean isTemplate,
            List<TypeVariable> typeParameters,
            List<? extends Type> typeArguments,
            long tag,
            @Nullable Integer sourceCodeTag,
            int since) {
        this.name = name;
        this.code = code;
        this.kind = kind;
        setTmpId(tmpId);
        setSuperType(superType);
        setInterfaces(interfaces);
        this.isAbstract = isAbstract;
        this.anonymous = anonymous;
        this.ephemeral = ephemeral;
        this.struct = struct;
        this.searchable = searchable;
        this.template = copySource = template;
        this.source = source;
        this.desc = desc;
        this.tag = tag;
        this.sourceCodeTag = sourceCodeTag;
        this.since = since;
        this.numFields = superType != null ? superType.resolve().getNumFields() : 0;
        closure = new Closure(this);
        resetRank();
        if (superType != null)
            superType.resolve().addExtension(this);
        interfaces.forEach(it -> it.resolve().addImplementation(this));
        setTypeParameters(typeParameters);
        if(typeParameters.isEmpty())
            setTypeArguments(typeArguments);
        else
            setTypeArguments(NncUtils.map(typeParameters, TypeVariable::getType));
        getMethodTable().rebuild();
        setTemplateFlag(isTemplate);
        resetSortedClasses();
        NncUtils.requireTrue(getAncestorClasses().size() <= Constants.MAX_INHERITANCE_DEPTH,
                "Inheritance depth of class " + name + "  exceeds limit: " + Constants.MAX_INHERITANCE_DEPTH);
    }

    public void update(KlassDTO klassDTO) {
        this.name = klassDTO.name();
        setDesc(klassDTO.desc());
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    void resetSortedClasses() {
        sortedKlasses.clear();
        forEachSuperClass(sortedKlasses::add);
        sortedKlasses.sort(Comparator.comparingLong(Klass::getTag));
        level = sortedKlasses.size() - 1;
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

    public List<Field> getReadyFields() {
        return fields.toList();
    }

    public List<Field> getFields() {
        return fields.toList();
    }

    public List<Field> getAllFields() {
        if (superType != null)
            return NncUtils.union(superType.resolve().getAllFields(), fields.toList());
        else
            return fields.toList();
    }

    @Override
    public boolean isValidGlobalKey() {
        return isBuiltin() || template != null;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        if (template == null) {
            return getCodeNotNull();
        } else {
            throw new InternalException("Getting global key for parameterized type: " + getTypeDesc() + ", method: " + ((Method) DebugEnv.object).getQualifiedSignature());
        }
    }

    public String getCodeNotNull() {
        return Objects.requireNonNull(code);
    }

    public void forEachField(Consumer<Field> action) {
        if (superType != null)
            superType.resolve().forEachField(action);
        for (Field field : fields) {
            if(!field.isMetadataRemoved())
                action.accept(field);
        }
    }

    public long getTag() {
        return tag;
    }

    public void setTitleField(@Nullable Field titleField) {
        if (titleField != null && !titleField.getType().getUnderlyingType().isString())
            throw new BusinessException(ErrorCode.TITLE_FIELD_MUST_BE_STRING);
        this.titleField = titleField;
    }

    public boolean isTemplate() {
        return !typeParameters.isEmpty();
    }

    //<editor-fold desc="hierarchy">

    public List<Klass> getAncestorClasses() {
        List<Klass> result = new ArrayList<>();
        accept(new VoidElementVisitor() {
            @Override
            public Void visitKlass(Klass klass) {
                if (klass.superType != null)
                    klass.superType.accept(this);
                result.add(klass);
                return super.visitKlass(klass);
            }
        });
        return result;
    }

    public Closure getClosure() {
        return closure;
    }

    public List<Klass> getSubKlasses() {
        return NncUtils.merge(extensions, implementations);
    }

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

    public List<ObjectMapping> getMappings() {
        return mappings.toList();
    }

    public ObjectMapping getMapping(Predicate<ObjectMapping> predicate) {
        return Objects.requireNonNull(findMapping(predicate),
                () -> "Can not find mapping with predicate in klass " + getTypeDesc());
    }

    public @Nullable ObjectMapping findMapping(Predicate<ObjectMapping> predicate) {
        var found = NncUtils.find(mappings, predicate);
        if (found != null)
            return found;
        if (superType != null && (found = superType.resolve().findMapping(predicate)) != null)
            return found;
        return null;
    }

    public @Nullable ObjectMapping getBuiltinMapping() {
        return NncUtils.find(mappings, ObjectMapping::isBuiltin);
    }

    private void resetRank() {
        int r = superType != null ? superType.resolve().getRank() : 0;
        for (var it : interfaces) {
            var itRank = it.resolve().getRank();
            if (itRank > r)
                r = itRank;
        }
        rank = r + 1;
    }

    public int getRank() {
        return rank;
    }

    public List<Error> getErrors() {
        return errors.toList();
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
        errors.addChild(new Error(element, level, message));
    }

    //</editor-fold>

    //<editor-fold desc="method">

    public Method getDefaultConstructor() {
        return getMethodByCodeAndParamTypes(Types.getConstructorCode(this), List.of());
    }

    public void sortFields(Comparator<Field> comparator) {
        this.fields.sort(comparator);
    }

    public ClassTypeState getState() {
        return state;
    }

    public boolean isDeployed() {
        return state.ordinal() >= ClassTypeState.DEPLOYED.ordinal();
    }

    public void setState(ClassTypeState state) {
        this.state = state;
    }

    public List<Klass> getSortedKlasses() {
        return sortedKlasses;
    }

    public List<Field> getSortedFields() {
        return sortedFields;
    }

    void resetFieldOffsets() {
        forEachExtension(Klass::resetSelfFieldOffset);
    }

    public int getNumFields() {
        return numFields;
    }

    private void resetSelfFieldOffset() {
        var offset = superType != null ? superType.resolve().getNumFields() : 0;
        for (Field f : getSortedFields()) {
            f.setOffset(offset++);
        }
        numFields = offset;
    }

    void resetFieldTransients() {
        resetSortedFields();
        resetFieldOffsets();
    }

    private void resetSortedFields() {
        this.sortedFields.clear();
        sortedFields.addAll(this.fields.toList());
        sortedFields.sort(Comparator.comparingInt(Field::getTag));
        assert fields.size() <= 1 || NncUtils.allMatch(fields, EntityUtils::isModelInitialized);
    }

    public void moveMethod(Method method, int index) {
        moveProperty(methods, method, index);
    }

    private MethodTable getMethodTable() {
        if (methodTable == null) {
            synchronized (this) {
                if (methodTable == null)
                    methodTable = new MethodTable(this);
            }
        }
        return methodTable;
    }

    public List<Method> getMethods() {
        return methods.toList();
    }

    public List<Method> getAllMethods() {
        if (superType != null)
            return NncUtils.union(superType.resolve().getAllMethods(), NncUtils.listOf(methods));
        else
            return NncUtils.listOf(getMethods());
    }

    public Method getMethodByInternalName(String internalName) {
        return NncUtils.findRequired(methods, m -> m.getInternalName(null).equals(internalName),
                () -> "Failed to find method with internal name '" + internalName + "' in class " + getTypeDesc());
    }

    public ReadonlyArray<Method> getDeclaredMethods() {
        return methods;
    }

    public Method getMethod(long id) {
        return methods.get(Entity::tryGetId, id);
    }

    public Method tryGetMethod(String name, List<Type> parameterTypes) {
        var method = NncUtils.find(methods,
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        if (superType != null)
            return superType.resolve().getMethod(name, parameterTypes);
        return null;
    }

    public Method getMethod(String name, List<Type> parameterTypes) {
        return NncUtils.requireNonNull(
                tryGetMethod(name, parameterTypes),
                () -> "Can not find method '" + name + "(" +
                        NncUtils.join(parameterTypes, Type::getName, ",")
                        + ")' in type '" + getName() + "'"
        );
    }

    public @Nullable Method findMethodByCodeAndParamTypes(String code, List<Type> parameterTypes) {
        var method = NncUtils.find(methods,
                f -> Objects.equals(f.getCode(), code) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        if (superType != null) {
            var m = superType.resolve().findMethodByCodeAndParamTypes(code, parameterTypes);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : interfaces) {
                var m = it.resolve().findMethodByCodeAndParamTypes(code, parameterTypes);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    public Method getMethodByCodeAndParamTypes(String code, List<Type> parameterTypes) {
        var found = findMethodByCodeAndParamTypes(code, parameterTypes);
        if(found == null) {
            throw new NullPointerException(String.format("Can not find method %s(%s) in klass %s",
                    code, NncUtils.join(parameterTypes, Type::getTypeDesc, ","), getTypeDesc()));
        }
        return found;
    }

    public Method getMethodByCode(String code) {
        return Objects.requireNonNull(findMethodByCode(code), () -> "Can not find method with code '" + code + "' in type '" + name + "'");
    }

    public @Nullable Method findMethodByCode(String code) {
        return findMethod(Method::getCode, code);
    }

    public @Nullable Method findSelfMethodByCode(String code) {
        return methods.get(Flow::getCode, code);
    }

    public @Nullable Method findMethodByVerticalTemplate(Method template) {
        return findMethod(m -> m.getVerticalTemplate() == template);
    }

    public Method findSelfMethod(Predicate<Method> predicate) {
        return NncUtils.find(methods.toList(), predicate);
    }

    public Method getMethod(Predicate<Method> predicate) {
        return getMethod(predicate, () -> "Can not find method with predicate in klass " + this);
    }

    public Method getMethod(Predicate<Method> predicate, Supplier<String> messageSupplier) {
        var found = findMethod(predicate);
        if (found != null)
            return found;
        if (DebugEnv.resolveVerbose) {
            logger.info("Fail to resolve method with predicate in klass " + getTypeDesc());
            forEachMethod(m -> logger.info(m.getQualifiedName()));
        }
        throw new NullPointerException(messageSupplier.get());
    }

    public @Nullable Method findMethod(Predicate<Method> predicate) {
        var found = NncUtils.find(methods, predicate);
        if (found != null)
            return found;
        if (superType != null && (found = superType.resolve().findMethod(predicate)) != null)
            return found;
        for (ClassType it : interfaces) {
            if ((found = it.resolve().findMethod(predicate)) != null)
                return found;
        }
        return null;
    }

    public <T> @Nullable Method findMethod(IndexMapper<Method, T> property, T value) {
        var method = methods.get(property, value);
        if (method != null)
            return method;
        if (superType != null) {
            var m = superType.resolve().findMethod(property, value);
            if (m != null)
                return m;
        }
        if (isEffectiveAbstract()) {
            for (var it : interfaces) {
                var m = it.resolve().findMethod(property, value);
                if (m != null)
                    return m;
            }
        }
        return null;
    }

    public void removeMethod(Method method) {
        methods.remove(method);
        getMethodTable().rebuild();
    }

    public void addMethod(Method method) {
        if (methods.contains(method))
            throw new InternalException("Method '" + method + "' is already added to the class type");
        methods.addChild(method);
        getMethodTable().rebuild();
    }
    //</editor-fold>

    public ReadonlyArray<Field> getDeclaredFields() {
        return fields;
    }

    public ReadonlyArray<Constraint> getDeclaredConstraints() {
        return constraints;
    }

    public void addField(Field field) {
        if (fields.contains(field))
            throw new RuntimeException("Field " + field.tryGetId() + " is already added");
        if (findSelfField(f -> f.getName().equals(field.getName())) != null
                || findSelfStaticField(f -> f.getName().equals(field.getName())) != null)
            throw BusinessException.invalidField(field, "Field name '" + field.getName() + "' is already used in class " + getName());
        if (field.getCode() != null &&
                (findSelfFieldByCode(field.getCode()) != null || findSelfStaticFieldByCode(field.getCode()) != null))
            throw BusinessException.invalidField(field, "Field code " + field.getCode() + " is already used in class " + getName());
        if (field.isStatic())
            staticFields.addChild(field);
        else
            fields.addChild(field);
        resetFieldTransients();
    }

    @Override
    public void onLoadPrepare() {
        extensions = new ArrayList<>();
        implementations = new ArrayList<>();
    }

    @Override
    public void onLoad() {
        stage = ResolutionStage.INIT;
        sortedFields = new ArrayList<>();
        resetSortedFields();
        sortedKlasses = new ArrayList<>();
        resetSortedClasses();
        if (superType != null)
            superType.resolve().addExtension(this);
        interfaces.forEach(it -> it.resolve().addImplementation(this));
        resetSelfFieldOffset();
        closure = new Closure(this);
        resetRank();
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
        if (isTemplate()) {
            ParameterizedStore.forEach(this, (k,v) -> ((Klass) v).freeze());
        }
    }

    public int getLevel() {
        return level;
    }

    public List<Index> getFieldIndices(Field field) {
        return NncUtils.filter(
                getConstraints(Index.class),
                index -> index.isFieldIndex(field)
        );
    }

    @Override
    @Nullable
    public Klass getCopySource() {
        return copySource;
    }

    public void setCopySource(@Nullable Object copySource) {
        this.copySource = (Klass) copySource;
    }

    public void addConstraint(Constraint constraint) {
        constraints.addChild(constraint);
    }

    public void removeConstraint(Constraint constraint) {
        constraints.remove(constraint);
    }

    @JsonIgnore
    public boolean isEnum() {
        return kind == ClassKind.ENUM;
    }

    public boolean isInterface() {
        return kind == ClassKind.INTERFACE;
    }

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
    public boolean isValue() {
        return this.kind == ClassKind.VALUE;
    }

    @JsonIgnore
    public boolean isReference() {
        return isEnum() || isClass() || isInterface();
    }

    public void addMapping(ObjectMapping mapping) {
        this.mappings.addChild(mapping);
        if (defaultMapping == null)
            defaultMapping = mapping;
    }

    public Field getField(Id id) {
        var field = fields.get(Entity::tryGetId, id);
        if (field != null)
            return field;
        if (superType != null)
            return superType.resolve().getField(id);
        throw new NullPointerException("Can not find field for " + id + " in type " + name);
    }

    public Method getMethod(Id id) {
        return Objects.requireNonNull(methods.get(Entity::tryGetId, id));
    }

    public @Nullable Field findSelfField(Predicate<Field> predicate) {
        return NncUtils.find(fields, predicate);
    }

    public Field getSelfField(Predicate<Field> predicate) {
        return Objects.requireNonNull(findSelfField(predicate));
    }

    public @Nullable Field findSelfStaticField(Predicate<Field> predicate) {
        return NncUtils.find(staticFields, predicate);
    }

    public @Nullable Field findField(Predicate<Field> predicate) {
        var field = findSelfField(predicate);
        if (field != null)
            return field;
        if (superType != null)
            return superType.resolve().findField(predicate);
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

    public Field getFieldByTemplate(Field template) {
        return getField(f -> f.getEffectiveTemplate() == template);
    }

    public @Nullable Field findStaticField(Predicate<Field> predicate) {
        var field = NncUtils.find(staticFields, predicate);
        if (field != null)
            return field;
        if (superType != null)
            return superType.resolve().findStaticField(predicate);
        return null;
    }

    public Field tryGetFieldByName(String fieldName) {
        if (superType != null) {
            Field superField = superType.resolve().tryGetFieldByName(fieldName);
            if (superField != null)
                return superField;
        }
        return fields.get(Field::getName, fieldName);
    }

    public Field getFieldByName(String fieldName) {
        return NncUtils.requireNonNull(tryGetFieldByName(fieldName));
    }

    public Field tryGetStaticFieldByName(String fieldName) {
        if (superType != null) {
            Field superField = superType.resolve().tryGetStaticFieldByName(fieldName);
            if (superField != null)
                return superField;
        }
        return staticFields.get(Field::getName, fieldName);
    }

    public Field getStaticFieldByName(String fieldName) {
        return NncUtils.requireNonNull(tryGetStaticFieldByName(fieldName),
                () -> "Static field " + fieldName + " not found in class " + getCodeNotNull());
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
        if (superType != null) {
            var p = superType.resolve().findStaticPropertyByVar(var);
            if (p != null)
                return p;
        }
        for (var it : interfaces) {
            var p = it.resolve().findStaticPropertyByVar(var);
            if (p != null)
                return p;
        }
        return null;
    }

    private Property findSelfStaticProperty(Predicate<Property> predicate) {
        var field = NncUtils.find(staticFields, predicate);
        if (field != null)
            return field;
        return NncUtils.find(methods, predicate);
    }

    public void setSource(ClassSource source) {
        this.source = source;
    }

    @Nullable
    public Field findFieldByCode(String code) {
        var field = fields.get(Field::getCode, code);
        if (field != null)
            return field;
        if (superType != null)
            return superType.resolve().findFieldByCode(code);
        return null;
    }

    public Property getPropertyByVar(Var var) {
        return switch (var.getType()) {
            case NAME -> getPropertyByName(var.getName());
            case ID -> findProperty(p -> p.getUltimateTemplate().idEquals(var.getId()));
        };
    }

    public Property getProperty(Id id) {
        return NncUtils.requireNonNull(getProperty(Property::tryGetId, id),
                "Can not find attribute with id: " + id + " in type " + this);
    }

    public Property getPropertyByName(String name) {
        return getProperty(Property::getName, name);
    }

    public Property findProperty(Predicate<Property> filter) {
        var field = NncUtils.find(fields, filter);
        if(field != null)
            return field;
        var method = NncUtils.find(methods, m -> !m.isStatic() && filter.test(m));
        if(method != null)
            return method;
        if(superType != null)
            return superType.resolve().findProperty(filter);
        return null;
    }

    private <T> Property getProperty(IndexMapper<Property, T> property, T value) {
        var field = fields.get(property, value);
        if (field != null)
            return field;
        var method = methods.get(property, value);
        if (method != null)
            return method;
        if (superType != null)
            return superType.resolve().getProperty(property, value);
        return null;
    }

    public List<Property> getProperties() {
        return NncUtils.concatList(fields.toList(), methods.toList());
    }

    public Field getFieldByCode(String code) {
        return NncUtils.requireNonNull(findFieldByCode(code),
                String.format("Can not find field with code '%s' in type '%s'", code, name));
    }


    @Nullable
    public Field findSelfFieldByCode(String code) {
        return fields.get(Property::getCode, code);
    }

    @Nullable
    public Field findSelfStaticFieldByCode(String code) {
        return staticFields.get(Property::getCode, code);
    }

    @Nullable
    public Field findStaticFieldByCode(String code) {
        if (superType != null) {
            Field superField = superType.resolve().findStaticFieldByCode(code);
            if (superField != null)
                return superField;
        }
        return staticFields.get(Field::getCode, code);
    }

    @Nullable
    public Index findIndex(Predicate<Index> predicate) {
        for (Constraint constraint : constraints) {
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
        return requireNonNull(tryGetFieldByName(fieldName),
                "Can not find field for java field " + javaField);
    }

    public boolean checkColumnAvailable(Column column) {
        return NncUtils.find(fields, f -> f.getColumn() == column) == null;
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


    private ReadonlyArray<Field> getFieldsInHierarchy() {
        return fields;
    }

    public Field getFieldNyName(String fieldName) {
        return NncUtils.requireNonNull(
                tryGetFieldByName(fieldName), "field not found: " + fieldName
        );
    }

    public void removeField(Field field) {
        if (field.isStatic())
            staticFields.remove(field);
        else
            fields.remove(field);
        resetFieldTransients();
    }

    public boolean isAssignableFrom(Klass that) {
        if (equals(that)) {
            return true;
        }
        if (template != null) {
            var s = that.findAncestorByTemplate(template);
            if (s != null)
                return NncUtils.biAllMatch(typeArguments, s.typeArguments, Type::contains);
            else
                return false;
        } else {
            if (that.getSuperType() != null && getType().isAssignableFrom(that.getSuperType()))
                return true;
            if (isInterface()) {
                for (var it : that.interfaces) {
                    if (isAssignableFrom(it.resolve())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public @NotNull ClassType getType() {
        if (type == null)
            type = new ClassType(this.getEffectiveTemplate(), isParameterized() ? typeArguments.toList() : List.of());
        return type;
    }

    public void addParameterized(Klass parameterized) {
        NncUtils.requireTrue(parameterized.getTemplate() == this);
        var existing = ParameterizedStore.get(this, parameterized.typeArguments.secretlyGetTable());
        if(existing != null)
            throw new IllegalStateException("Parameterized klass " + parameterized.getTypeDesc() + " already exists. "
                    + "existing: " + System.identityHashCode(existing) + ", new: "+ System.identityHashCode(parameterized)
            );
        NncUtils.requireNull(ParameterizedStore.put(this, parameterized.typeArguments.secretlyGetTable(), parameterized),
                () -> "Parameterized klass " + parameterized.getTypeDesc() + " already exists");
    }

    public Klass getExistingParameterized(List<? extends Type> typeArguments) {
        if (NncUtils.map(typeParameters, TypeVariable::getType).equals(typeArguments))
            return this;
        return (Klass) ParameterizedStore.get(this, typeArguments);
    }

    public Klass getParameterized(List<? extends Type> typeArguments) {
        return getParameterized(typeArguments, ResolutionStage.DEFINITION);
    }

    public Klass getParameterized(List<? extends Type> typeArguments, ResolutionStage stage) {
        if (!isTemplate) {
            if (typeArguments.isEmpty())
                return this;
            else
                throw new InternalException(this + " is not a template class");
        }
//        typeArguments.forEach(Type::getTypeDesc);
        var pClass = getExistingParameterized(typeArguments);
        if (pClass == this)
            return this;
        if(pClass == null) {
            pClass = createParameterized(typeArguments);
            addParameterized(pClass);
        }
        else if (pClass.getStage().isAfterOrAt(stage))
            return pClass;
        var subst = new SubstitutorV2(
                this, typeParameters.toList(), typeArguments, pClass, stage);
        pClass = (Klass) subst.copy(this);
        return pClass;
    }

    private Klass createParameterized(List<? extends Type> typeArguments) {
        var copy = KlassBuilder.newBuilder(name, null)
                .kind(getKind())
                .typeArguments(typeArguments)
                .anonymous(true)
                .ephemeral(isEphemeral())
                .template(this)
//                .tmpId(getCopyTmpId(template))
                .tag(getTag())
                .build();
        copy.setStrictEphemeral(true);
        return copy;
    }

    @Nullable
    public ClassType getSuperType() {
        return superType;
    }

    @Nullable
    public Klass getSuperKlass() {
        return superType != null ? superType.resolve() : null;
    }

    public List<ClassType> getInterfaces() {
        return Collections.unmodifiableList(interfaces.toList());
    }

    public void forEachSuper(Consumer<Klass> action) {
        if (superType != null)
            action.accept(superType.resolve());
        interfaces.forEach(it -> action.accept(it.resolve()));
    }

    public void forEachSuperClass(Consumer<Klass> action) {
        action.accept(this);
        if (superType != null)
            superType.resolve().forEachSuperClass(action);
    }

    public Klass findAncestorByTemplate(Klass template) {
        return getClosure().find(t -> t.templateEquals(template));
    }

    public @NotNull Klass getAncestorByTemplate(Klass template) {
        return Objects.requireNonNull(findAncestorByTemplate(template),
                () -> "Cannot find ancestor with template " + template.getName() + " of class "  +getTypeDesc()
        );
    }

    public @Nullable Klass findAncestorKlassByTemplate(Klass template) {
        return findAncestorKlass(k -> k.getEffectiveTemplate() == template);
    }

    public @Nullable Klass findAncestorKlass(Predicate<Klass> predicate) {
        if (predicate.test(this))
            return this;
        if (superType != null)
            return superType.resolve().findAncestorKlass(predicate);
        return null;
    }

    public KlassDTO toDTO() {
        try (var serContext = SerializeContext.enter()) {
            return toDTO(serContext);
        }
    }

    public KlassDTO toDTO(SerializeContext serializeContext) {
        typeParameters.forEach(serializeContext::writeTypeDef);
        if (template != null)
            serializeContext.writeTypeDef(template);
        return new KlassDTO(
                serializeContext.getStringId(this),
                name,
                code,
                kind.code(),
                ephemeral,
                anonymous,
                getAttributesMap(),
                NncUtils.get(superType, t -> t.toExpression(serializeContext)),
                NncUtils.map(interfaces, t -> t.toExpression(serializeContext)),
                source.code(),
                NncUtils.map(fields, Field::toDTO),
                NncUtils.map(staticFields, Field::toDTO),
                NncUtils.get(titleField, serializeContext::getStringId),
                NncUtils.map(constraints, Constraint::toDTO),
                NncUtils.map(methods, f -> f.toDTO(serializeContext.shouldWriteCode(this), serializeContext)),
                NncUtils.map(mappings, m -> m.toDTO(serializeContext)),
                NncUtils.map(enumConstantDefs, ec -> ec.toDTO(serializeContext)),
                NncUtils.get(defaultMapping, serializeContext::getStringId),
                desc,
                getExtra(),
                isAbstract,
                isTemplate(),
                NncUtils.map(typeParameters, serializeContext::getStringId),
                NncUtils.map(typeParameters, tv -> tv.toDTO(serializeContext)),
                NncUtils.get(template, serializeContext::getStringId),
                NncUtils.map(typeArguments, t -> t.toExpression(serializeContext)),
                !extensions.isEmpty() || !implementations.isEmpty(),
                struct,
                searchable,
                tag,
                sourceCodeTag,
                NncUtils.map(errors, Error::toDTO)
        );
    }

    protected Object getExtra() {
        return null;
    }

    public <T extends Constraint> List<T> getConstraints(Class<T> constraintType) {
        List<T> result = filterAndMap(
                constraints,
                constraintType::isInstance,
                constraintType::cast
        );
        if (superType != null) {
            result = NncUtils.union(
                    superType.resolve().getConstraints(constraintType),
                    result
            );
        }
        return result;
    }

    public List<Constraint> getConstraints() {
        return constraints.toList();
    }

    public <T extends Constraint> T getConstraint(Class<T> constraintType, Id id) {
        return find(getConstraints(constraintType), c -> c.getId().equals(id));
    }

    public List<CheckConstraint> getFieldCheckConstraints(Field field) {
        var constraints = getConstraints(CheckConstraint.class);
        return NncUtils.filter(constraints, c -> c.isFieldConstraint(field));
    }

    @SuppressWarnings("unused")
    public Constraint getConstraint(Id id) {
        return NncUtils.find(requireNonNull(constraints), c -> c.idEquals(id));
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

    public Method resolveMethod(@NotNull Method methodRef) {
        return Objects.requireNonNull(
                tryResolveMethod(methodRef),
                () -> String.format("Fail to resolve method %s.%s in type %s",
                        methodRef.getDeclaringType().getTypeDesc(),
                        methodRef.getTypeDesc(), this)
        );
    }

    public @Nullable Method tryResolveMethod(@NotNull Method methodRef) {
        if (methodRef.getDeclaringType() == this)
            return methodRef;
        var hTemplate = methodRef.getHorizontalTemplate();
        if (hTemplate != null) {
            var resolvedTemplate = tryResolveNonParameterizedMethod(hTemplate);
            if (resolvedTemplate == null)
                return null;
            return resolvedTemplate.getParameterized(methodRef.getTypeArguments());
        } else
            return tryResolveNonParameterizedMethod(methodRef);
    }

    public void forEachMethod(Consumer<Method> action) {
        methods.forEach(action);
        if (superType != null)
            superType.resolve().forEachMethod(action);
        interfaces.forEach(it -> it.resolve().forEachMethod(action));
    }

    public Method resolveMethod(String code, List<Type> argumentTypes, List<Type> typeArguments, boolean staticOnly) {
        var found = tryResolveMethod(code, argumentTypes, typeArguments, staticOnly);
        if (found != null)
            return found;
        if (DebugEnv.resolveVerbose) {
            logger.info("method resolution failed");
            forEachMethod(m -> logger.info(m.getSignatureString()));
        }
        throw new NullPointerException(
                String.format("Can not find method %s%s(%s) in type %s",
                        NncUtils.isNotEmpty(typeArguments) ? "<" + NncUtils.join(typeArguments, Type::getName) + ">" : "",
                        code,
                        NncUtils.join(argumentTypes, Type::getName, ","), getName()));
    }

    public @Nullable Method tryResolveMethod(String code, List<Type> argumentTypes, List<Type> typeArguments, boolean staticOnly) {
        var candidates = new ArrayList<Method>();
        getCallCandidates(code, argumentTypes, typeArguments, staticOnly, candidates);
        out:
        for (Method m1 : candidates) {
            for (Method m2 : candidates) {
                if (m1 != m2 && m1.isHiddenBy(m2))
                    continue out;
            }
            return m1;
        }
        return null;
    }

    private void getCallCandidates(String code,
                                   List<Type> argumentTypes,
                                   List<Type> typeArguments,
                                   boolean staticOnly,
                                   List<Method> candidates) {
        methods.forEach(m -> {
            if ((m.isStatic() || !staticOnly) && code.equals(m.getCode()) && m.getParameters().size() == argumentTypes.size()) {
                if (NncUtils.isNotEmpty(typeArguments)) {
                    if (m.getTypeParameters().size() == typeArguments.size()) {
                        var pMethod = (Method) m.getParameterized(typeArguments);
                        if (pMethod.matches(code, argumentTypes))
                            candidates.add(pMethod);
                    }
                } else {
                    if (m.matches(code, argumentTypes))
                        candidates.add(m);
                }
            }
        });
        forEachSuper(k -> k.getCallCandidates(code, argumentTypes, typeArguments, staticOnly, candidates));
    }

    @Nullable
    public Method tryResolveNonParameterizedMethod(Method methodRef) {
        NncUtils.requireFalse(methodRef.getParameterizedFlows());
        var methodTable = getMethodTable();
        if (methodRef.getDeclaringType().isUncertain())
            methodRef = methodTable.findByVerticalTemplate(Objects.requireNonNull(methodRef.getVerticalTemplate()));
        return methodTable.findByOverridden(methodRef);
    }

    public boolean isUncertain() {
        return NncUtils.anyMatch(typeArguments, Type::isUncertain);
    }

    @Nullable
    public Klass getTemplate() {
        return template;
    }

    public void updateParameterized() {
        ParameterizedStore.forEach(this, (typeArgs, k) -> {
            var p = (Klass) k;
            p.setStage(ResolutionStage.INIT);
            var subst = new SubstitutorV2(
                    this, typeParameters.toList(), typeArgs, k, stage);
            subst.copy(this);
        });
    }

    public void setTemplate(Object template) {
        NncUtils.requireNull(this.template);
        isParameterized = template != null;
        this.template = copySource = (Klass) template;
    }

    public void forEachParameterized(Consumer<Klass> action) {
        ParameterizedStore.forEach(this, (k,v) -> action.accept((Klass) v));
    }

    public Klass getEffectiveTemplate() {
        return template != null ? template : this;
    }

    public boolean templateEquals(Klass that) {
        return this == that || this.getTemplate() == that;
    }

    public List<TypeVariable> getTypeParameters() {
        return typeParameters.toList();
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        isTemplate = true;
        typeParameters.addChild(typeParameter);
        typeArguments.add(typeParameter.getType());
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
    public String getCode() {
        return code;
    }

    public void setCode(@Nullable String code) {
        this.code = code;
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

    public boolean isEphemeral() {
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

    public List<Type> getTypeArguments() {
        return typeArguments.toList();
    }

    public List<Type> getEffectiveTypeArguments() {
        return isParameterized() ? typeArguments.toList() : NncUtils.map(typeParameters, TypeVariable::getType);
    }

    public Index getUniqueConstraint(List<Field> fields) {
        return find(getIndices(), c -> c.isUnique() && c.getTypeFields().equals(fields));
    }

    public List<Index> getIndices() {
        return getConstraints(Index.class);
    }

    @Override
    protected String toString0() {
        return "Klass " + name + " (id:" + id + ")";
    }

    public void setTypeArguments(List<? extends Type> typeArguments) {
        if (isTemplate() && !NncUtils.iterableEquals(NncUtils.map(typeParameters, TypeVariable::getType), typeArguments))
            throw new InternalException("Type arguments must equal to type parameters for a template type. Actual type arguments: " + typeArguments);
        this.typeArguments.reset(typeArguments);
    }

    protected final void onSuperTypesChanged() {
        onAncestorChanged();
        resetSortedClasses();
        resetFieldOffsets();
    }


    private void onAncestorChanged() {
        forEachSubclass(Klass::onAncestorChangedSelf);
    }

    public void setSuperType(@Nullable ClassType superType) {
        if (this.superType != null) {
            this.superType.resolve().removeExtension(this);
        }
        if (superType != null) {
            this.superType = superType;
            superType.resolve().addExtension(this);
        } else
            this.superType = null;
        onSuperTypesChanged();
    }

    public void setInterfaces(List<ClassType> interfaces) {
        for (var anInterface : this.interfaces) {
            anInterface.resolve().removeImplementation(this);
        }
        this.interfaces.clear();
        for (var anInterface : interfaces) {
            this.interfaces.add(anInterface);
            anInterface.resolve().addImplementation(this);
        }
        onSuperTypesChanged();
    }

    protected void onAncestorChangedSelf() {
        closure = new Closure(this);
        getMethodTable().rebuild();
        resetRank();
    }

    public void setTypeParameters(List<TypeVariable> typeParameters) {
        this.isTemplate = !typeParameters.isEmpty();
        typeParameters.forEach(tp -> tp.setGenericDeclaration(this));
        this.typeParameters.resetChildren(typeParameters);
        if (isTemplate())
            setTypeArguments(NncUtils.map(typeParameters, TypeVariable::getType));
    }

    public ObjectMapping getMappingInAncestors(Id id) {
        var mapping = findMapping(id);
        if (mapping != null)
            return mapping;
        if (superType != null) {
            if ((mapping = superType.resolve().getMappingInAncestors(id)) != null)
                return mapping;
        }
        for (var it : interfaces) {
            if ((mapping = it.resolve().getMappingInAncestors(id)) != null)
                return mapping;
        }
        throw new InternalException("Can not find mapping in the ancestors of type: " + getName());
    }

    public @Nullable ObjectMapping findMapping(Id id) {
        return mappings.get(Entity::getId, id);
    }

    public void setFields(List<Field> fields) {
        requireTrue(allMatch(fields, f -> f.getDeclaringType() == this));
        this.fields.resetChildren(fields);
        resetFieldTransients();
    }

    public void moveField(Field field, int index) {
        moveProperty(fields, field, index);
    }

    private <T extends Entity & Property> void moveProperty(ChildArray<T> properties, T property, int index) {
        if (index < 0 || index >= properties.size())
            throw new BusinessException(ErrorCode.INDEX_OUT_OF_BOUND);
        if (!properties.remove(property))
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND, property.getName());
        if (index >= properties.size())
            properties.addChild(property);
        else
            properties.addChild(index, property);
    }

    public void setStaticFields(List<Field> staticFields) {
        requireTrue(allMatch(staticFields, f -> f.getDeclaringType() == this));
        this.staticFields.resetChildren(staticFields);
    }

    public void setConstraints(List<Constraint> constraints) {
        requireTrue(allMatch(constraints, c -> c.getDeclaringType() == this));
        this.constraints.resetChildren(constraints);
    }

    public void setMethods(List<Method> methods) {
        requireTrue(allMatch(methods, f -> f.getDeclaringType() == this));
        this.methods.resetChildren(methods);
        rebuildMethodTable();
    }

    public boolean isParameterized() {
        return template != null && template != this;
    }

    @Override
    public String getTypeDesc() {
        if (isParameterized())
            return Objects.requireNonNull(template).getName() + "<" + NncUtils.join(typeArguments, Type::getTypeDesc, ",") + ">";
        else
            return getName();
    }

    @Override
    public List<Object> beforeRemove(IEntityContext context) {
        if (superType != null)
            superType.resolve().removeExtension(this);
        for (var anInterface : interfaces) {
            anInterface.resolve().removeImplementation(this);
        }
        var cascade = new ArrayList<>();
        var sft = context.selectFirstByKey(StaticFieldTable.IDX_KLASS, this);
        if (sft != null)
            cascade.add(sft);
        if (isTemplate())
            cascade.addAll(context.selectByKey(Klass.TEMPLATE_IDX, this));
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
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitKlass(this);
    }

    @Override
    public void onChange(ClassInstance instance, IEntityContext context) {
        rebuildMethodTable();
        if (!isInterface()) {
            for (var it : interfaces) {
                for (Method method : it.resolve().getMethods()) {
                    if (tryResolveNonParameterizedMethod(method) == null) {
                        throw new BusinessException(ErrorCode.INTERFACE_FLOW_NOT_IMPLEMENTED,
                                getName(), method.getName(), it.getName());
                    }
                }
            }
        }
        saveMapping(context);
    }

    @Override
    public boolean isChangeAware() {
        return !anonymous;
    }

    public void saveMapping(IEntityContext context) {
        if (!(context instanceof SystemDefContext)) {
            if(shouldGenerateBuiltinMapping() && !context.isFlagSet(ContextFlag.SKIP_SAVING_MAPPINGS))
                MappingSaver.create(context).saveBuiltinMapping(this, true);
            else {
                clearMapping();
            }
        }
    }

    private void clearMapping() {
        defaultMapping = null;
        mappings.clear();
        methods.removeIf(Klass::isMappingMethod);
    }

    public static final String[] MAPPING_METHOD_PREFIXES = new String[] {
            "getView", "saveView", "map", "unmap", "fromView"
    };

    private static boolean isMappingMethod(Method method) {
        if(method.isSynthetic()) {
            var methodName = method.getName();
            for (String prefix : MAPPING_METHOD_PREFIXES) {
                if(methodName.startsWith(prefix))
                    return true;
            }
            return false;
        }
        else
            return false;
    }

    public boolean shouldGenerateBuiltinMapping() {
        return (isClass() || isValue()) && !anonymous;
    }

    public void setDefaultMapping(@Nullable ObjectMapping mapping) {
        this.defaultMapping = mapping;
    }

    @Nullable
    public Field getTitleField() {
        if (titleField != null)
            return titleField;
        if (superType != null)
            return superType.resolve().getTitleField();
        return null;
    }

    public void setMappings(List<ObjectMapping> mappings) {
        this.mappings.resetChildren(mappings);
    }

    public @Nullable ObjectMapping getDefaultMapping() {
        return defaultMapping;
    }

    public List<Field> getStaticFields() {
        return staticFields.toList();
    }

    public void removeMapping(ObjectMapping mapping) {
        mappings.remove(mapping);
    }

    public boolean isViewType(Type type) {
        if (isType(type))
            return true;
        return NncUtils.anyMatch(mappings, m -> m.getTargetType().equals(type));
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        if (isParameterized())
            return requireNonNull(template).getCodeNotNull() + "<" + NncUtils.join(typeArguments, type -> type.getInternalName(current)) + ">";
        else
            return getCodeNotNull();
    }

    @Override
    public GenericDeclarationRef getRef() {
        return getType();
    }

    public boolean isList() {
        var t = getEffectiveTemplate();
        return t.getNativeClass() == ListNative.class;
    }

    public boolean isChildList() {
        return getEffectiveTemplate() == StdKlass.childList.get();
    }

    public Type getFirstTypeArgument() {
        return getTypeArguments().get(0);
    }

    public Type getIterableElementType() {
        var iterableType = Objects.requireNonNull(
                findAncestorByTemplate(StdKlass.iterable.get()),
                () -> getTypeDesc() + " is not an Iterable class");
        return iterableType.getTypeArguments().get(0);
    }

    public boolean isSAMInterface() {
        return isInterface() && getMethods().size() == 1;
    }

    public Method getSingleAbstractMethod() {
        if (!isSAMInterface())
            throw new InternalException("Type " + getName() + " is not a SAM interface");
        return getMethods().get(0);
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

    public Method findGetterByPropertyName(String propertyName) {
        for (Method method : getAllMethods()) {
            if (method.isGetter() && method.getPropertyName().equals(propertyName))
                return method;
        }
        return null;
    }

    public Method findSetterByPropertyName(String propertyName) {
        for (Method method : getAllMethods()) {
            if (method.isSetter() && method.getPropertyName().equals(propertyName))
                return method;
        }
        return null;
    }

    public int nextFieldTag() {
        return nextFieldTag++;
    }

    public Class<? extends NativeBase> getNativeClass() {
        return nativeClass;
    }

    public void setNativeClass(Class<? extends NativeBase> nativeClass) {
        this.nativeClass = nativeClass;
    }

    public void clearBuiltinMapping() {
        mappings.removeIf(ObjectMapping::isBuiltin);
        if(defaultMapping != null && defaultMapping.isBuiltin())
            defaultMapping = null;
    }

    public List<EnumConstantDef> getEnumConstantDefs() {
        return enumConstantDefs.toList();
    }

    public @Nullable EnumConstantDef findEnumConstantDef(Predicate<EnumConstantDef> predicate) {
        return NncUtils.find(enumConstantDefs, predicate);
    }

    public void addEnumConstantDef(EnumConstantDef enumConstantDef) {
        this.enumConstantDefs.addChild(enumConstantDef);
    }

    public void setEnumConstantDefs(List<EnumConstantDef> enumConstantDefs) {
        this.enumConstantDefs.resetChildren(enumConstantDefs);
    }

    public void clearEnumConstantDefs() {
        enumConstantDefs.forEach(ecd -> removeField(ecd.getField()));
        enumConstantDefs.clear();
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

    public boolean isBeanClass() {
        return getAttribute(AttributeNames.BEAN_KIND) != null;
    }

    public @Nullable Method getHashCodeMethod() {
        return methodTable.getHashCodeMethod();
    }

    public @Nullable Method getToStringMethod() {
        return methodTable.getToStringMethod();
    }

    public @Nullable Method getEqualsMethod() {
        return methodTable.getEqualsMethod();
    }

    public TypeSubstitutor getSubstitutor() {
        return new TypeSubstitutor(
                NncUtils.map(getEffectiveTemplate().getTypeParameters(), TypeVariable::getType),
                getTypeArguments()
        );
    }

    public Klass getArrayKlass() {
       if(arrayKlass == null) {
           arrayKlass = KlassBuilder.newBuilder(name + "[]", NncUtils.get(code, c -> c + "[]")).build();
           arrayKlass.setEphemeralEntity(true);
           arrayKlass.componentKlass = this;
       }
       return arrayKlass;
    }

    public Klass getComponentKlass() {
        return componentKlass;
    }
}

