package tech.metavm.object.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.Var;
import tech.metavm.flow.Error;
import tech.metavm.flow.*;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.object.view.MappingSaver;
import tech.metavm.object.view.ObjectMapping;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static tech.metavm.util.NncUtils.*;

@EntityType("Class类型")
public class ClassType extends Type implements GenericDeclaration, ChangeAware, GenericElement, StagedEntity {

    public static final Logger debugLoggerGER = LoggerFactory.getLogger("Debug");

    public static final IndexDef<ClassType> IDX_NAME = IndexDef.create(ClassType.class, "name");

    public static final IndexDef<ClassType> IDX_PARAMETERIZED_TYPE_KEY =
            IndexDef.createUnique(ClassType.class, "parameterizedTypeKey");

    public static final IndexDef<ClassType> UNIQUE_CODE = IndexDef.createUnique(ClassType.class, "code");

    public static final IndexDef<ClassType> TEMPLATE_IDX = IndexDef.create(ClassType.class, "template");

    public static final IndexDef<ClassType> TYPE_ARGUMENTS_IDX = IndexDef.create(ClassType.class, "typeArguments");

    @EntityField("超类")
    @Nullable
    private ClassType superClass;
    @ChildEntity("接口")
    private final ReadWriteArray<ClassType> interfaces = addChild(new ReadWriteArray<>(ClassType.class), "interfaces");
    @EntityField("来源")
    private ClassSource source;
    @ChildEntity("子类列表")
    private final ReadWriteArray<ClassType> subTypes = addChild(new ReadWriteArray<>(ClassType.class), "subTypes");
    @EntityField("描述")
    @Nullable
    private String desc;
    @ChildEntity("字段列表")
    private final ChildArray<Field> fields = addChild(new ChildArray<>(Field.class), "fields");
    @EntityField("标题属性")
    @Nullable
    private Field titleField;
    @ChildEntity("方法列表")
    private final ChildArray<Method> methods = addChild(new ChildArray<>(Method.class), "methods");
    @ChildEntity("参数化方法列表")
    private final ChildArray<Method> parameterizedMethods = addChild(new ChildArray<>(Method.class), "parameterizedMethods");

    @ChildEntity("静态字段列表")
    private final ChildArray<Field> staticFields = addChild(new ChildArray<>(Field.class), "staticFields");
    @ChildEntity("约束列表")
    private final ChildArray<Constraint> constraints = addChild(new ChildArray<>(Constraint.class), "constraints");
    @Nullable
    @EntityField("模板")
    private ClassType template;
    // Don't remove, for search
    @SuppressWarnings("unused")
    @EntityField("是否抽象")
    private boolean isAbstract;
    @EntityField("是否模版")
    private boolean isTemplate;
    // Don't remove, used for search
    @EntityField("是否参数化")
    private boolean isParameterized;
    @ChildEntity("类型参数")
    private final ChildArray<TypeVariable> typeParameters = addChild(new ChildArray<>(TypeVariable.class), "typeParameters");
    @ChildEntity("类型实参")
    private final ReadWriteArray<Type> typeArguments = addChild(new ReadWriteArray<>(Type.class), "typeArguments");

    // TODO (Important!) not scalable, must be optimized before going to production
    @ChildEntity("依赖")
    private final ReadWriteArray<ClassType> dependencies = addChild(new ReadWriteArray<>(ClassType.class), "dependencies");
    @Nullable
    private String parameterizedTypeKey;
    @ChildEntity("错误列表")
    private final ChildArray<Error> errors = addChild(new ChildArray<>(Error.class), "errors");

    @ChildEntity("视图映射列表")
    private final ChildArray<ObjectMapping> mappings = addChild(new ChildArray<>(ObjectMapping.class), "mappings");

    @EntityField("默认视图")
    @Nullable
    private ObjectMapping defaultMapping;

    @EntityField("复制来源")
    @CopyIgnore
    private @Nullable ClassType copySource;

    @EntityField("状态")
    private ClassTypeState state = ClassTypeState.INIT;

    private boolean struct;

    private transient ResolutionStage stage = ResolutionStage.INIT;

    private transient volatile MethodTable methodTable;

    // length of the longest path from the current type upwards to a root in the type hierarchy
    private transient int rank;

    private transient List<ClassType> superTypes;

    private transient volatile List<Field> sortedFields;

    public ClassType(
            Long tmpId,
            String name,
            @Nullable String code,
            @Nullable ClassType superClass,
            List<ClassType> interfaces,
            TypeCategory category,
            ClassSource source,
            @Nullable ClassType template,
            boolean anonymous,
            boolean ephemeral,
            boolean struct,
            @Nullable String desc,
            boolean isAbstract,
            boolean isTemplate,
            List<TypeVariable> typeParameters,
            List<Type> typeArguments) {
        super(name, code, anonymous, ephemeral, category);
        setTmpId(tmpId);
        setSuperClass(superClass);
        setInterfaces(interfaces);
        this.isAbstract = isAbstract;
        this.struct = struct;
        this.template = copySource = template;
        this.source = source;
        this.desc = desc;
        setTypeParameters(typeParameters);
        setTypeArguments(typeArguments);
        getMethodTable().rebuild();
        setTemplateFlag(isTemplate);
        NncUtils.requireTrue(getAncestorClasses().size() <= Constants.MAX_INHERITANCE_DEPTH,
                "Inheritance depth of class " + name + "  exceeds limit: " + Constants.MAX_INHERITANCE_DEPTH);
    }

    public void update(TypeDTO typeDTO) {
        setName(typeDTO.name());
        ClassTypeParam param = (ClassTypeParam) typeDTO.param();
        setDesc(param.desc());
    }

    public void setDesc(@Nullable String desc) {
        this.desc = desc;
    }

    void addSubType(@NotNull ClassType subType) {
        if (subTypes.contains(subType))
            throw new InternalException("Subtype '" + subType + "' is already added to this type");
        subTypes.add(subType);
    }

    void removeSubType(ClassType subType) {
        subTypes.remove(subType);
    }

    @Nullable
    @SuppressWarnings("unused")
    public String getDesc() {
        return desc;
    }

    public List<Field> getReadyFields() {
        return readyFields();
    }

    public List<Field> getFields() {
        return fields.toList();
    }

    public List<Field> getAllFields() {
        if (superClass != null)
            return NncUtils.union(superClass.getAllFields(), readyFields());
        else
            return readyFields();
    }

    @Override
    public boolean isValidGlobalKey() {
        return isBuiltin() || template != null;
    }

    @Override
    public String getGlobalKey(@NotNull BuildKeyContext context) {
        if (template == null) {
//            java.lang.reflect.Type javaType = Objects.requireNonNull(
//                    context.getJavaType(this), () -> "Can not get java type for type '" + this + "'");
            return getCodeRequired();
        } else {
            return context.getModelName(template, this) + "<"
                    + NncUtils.join(typeArguments, object -> context.getModelName(object, this))
                    + ">";
        }
    }

    public void forEachField(Consumer<Field> action) {
        if (superClass != null)
            superClass.forEachField(action);
        this.fields.stream().filter(Field::isReady).forEach(action);
    }

    public boolean allFieldsMatch(Predicate<Field> predicate) {
        if (superClass != null && !superClass.allFieldsMatch(predicate))
            return false;
        return this.fields.stream().filter(Field::isReady).allMatch(predicate);
    }

    public void setTitleField(@Nullable Field titleField) {
        if (titleField != null && !titleField.getType().isString())
            throw new BusinessException(ErrorCode.TITLE_FIELD_MUST_BE_STRING);
        this.titleField = titleField;
    }

    @Override
    public void onBind(IEntityContext context) {
        if (isTemplate())
            context.getGenericContext().add(this);
    }

    public List<Field> getFieldsByPath(List<String> path) {
        List<Field> result = new ArrayList<>();
        getFieldsByPath0(path, result);
        return result;
    }

//    public boolean isView() {
//        return sourceMapping != null;
//    }
//
//    public ObjectMapping getSourceMapping() {
//        return Objects.requireNonNull(sourceMapping, String.format("Type '%s' is not a view", getName()));
//    }

    public boolean isTemplate() {
        return !typeParameters.isEmpty();
    }

    public boolean isLocalClass() {
        var parent = getParentEntity();
        return parent instanceof Method || parent instanceof NodeRT;
    }

    private void getFieldsByPath0(List<String> path, List<Field> result) {
        NncUtils.requireMinimumSize(path, 1);
        String fieldName = path.get(0);
        Field field = getFieldNyNameRequired(fieldName);
        result.add(field);
        if (path.size() > 1) {
            Type fieldType = field.getType();
            if (fieldType instanceof ClassType classType)
                classType.getFieldsByPath0(path.subList(1, path.size()), result);
            else
                throw new InternalException("Invalid field path '" + NncUtils.join(path, ".") + "'");
        }
    }

    //<editor-fold desc="hierarchy">

    public List<ClassType> getAncestorClasses() {
        List<ClassType> result = new ArrayList<>();
        accept(new VoidElementVisitor() {
            @Override
            public Void visitClassType(ClassType type) {
                if (type.superClass != null)
                    type.superClass.accept(this);
                result.add(type);
                return super.visitClassType(type);
            }
        });
        return result;
    }

    @Override
    public Closure<ClassType> getClosure() {
        return super.getClosure().cast(ClassType.class);
    }

    @Override
    protected Class<ClassType> getClosureElementJavaClass() {
        return ClassType.class;
    }

    public List<ClassType> getSubTypes() {
        return subTypes.toList();
    }

    public List<ClassType> getDescendantTypes() {
        List<ClassType> types = new ArrayList<>();
        visitDescendantTypes(types::add);
        return types;
    }

    public void visitDescendantTypes(Consumer<ClassType> action) {
        action.accept(this);
        for (ClassType subType : subTypes) {
            subType.visitDescendantTypes(action);
        }
    }

    public List<ObjectMapping> getMappings() {
        return mappings.toList();
    }

    public @Nullable ObjectMapping getBuiltinMapping() {
        return NncUtils.find(mappings, ObjectMapping::isBuiltin);
    }

    public Set<Id> getSubTypeIds() {
        Set<Id> typeIds = new HashSet<>();
        accept(new VoidElementVisitor() {
            @Override
            public Void visitClassType(ClassType type) {
                typeIds.add(type.getId());
                for (ClassType subType : type.subTypes)
                    subType.accept(this);
                return super.visitClassType(type);
            }
        });
        return typeIds;
    }

    public int getRank() {
        if (rank == 0)
            rank = NncUtils.maxInt(getSuperTypes(), Type::getRank, 0) + 1;
        return rank;
    }

    public ParameterizedTypeKey getParameterizedKey() {
        NncUtils.requireNonNull(template);
        return new ParameterizedTypeKey(template.getStringId(), NncUtils.map(typeArguments, Entity::getStringId));
    }

    public List<Error> getErrors() {
        return errors.toList();
    }

    public void clearElementErrors(Element element) {
        errors.removeIf(e -> e.getElement() == element);
        setError(!errors.isEmpty());
    }

    public void addError(Element element, ErrorLevel level, String message) {
        errors.addChild(new Error(element, level, message));
        setError(true);
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

    public List<Field> getSortedFields() {
        if (sortedFields == null) {
            synchronized (this) {
                if (sortedFields == null) {
                    var sf = new ArrayList<Field>();
                    forEachField(f -> {
                        if (f.isIdNotNull())
                            sf.add(f);
                    });
                    sf.sort(Comparator.comparing(Field::getId));
                    sortedFields = sf;
                }
            }
        }
        return sortedFields;
    }

    void resetFieldsMemoryDataStructures() {
        this.sortedFields = null;
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
        if (superClass != null)
            return NncUtils.union(superClass.getAllMethods(), NncUtils.listOf(methods));
        else
            return NncUtils.listOf(getMethods());
    }

    public Method getMethodByInternalName(String internalName) {
        return NncUtils.findRequired(methods, m -> m.getInternalName(null).equals(internalName));
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
        if (superClass != null)
            return superClass.getMethod(name, parameterTypes);
        return null;
    }

    public Method getMethod(String name, List<Type> parameterTypes) {
        return NncUtils.requireNonNull(
                tryGetMethod(name, parameterTypes),
                () -> new InternalException("Can not find method '" + name + "(" +
                        NncUtils.join(parameterTypes, Type::getName, ",")
                        + ")' in type '" + getName() + "'")
        );
    }

    public @Nullable Method findMethodByCodeAndParamTypes(String code, List<Type> parameterTypes) {
        var method = NncUtils.find(methods,
                f -> Objects.equals(f.getCode(), code) && f.getParameterTypes().equals(parameterTypes));
        if (method != null)
            return method;
        if (superClass != null) {
            var m = superClass.findMethodByCodeAndParamTypes(code, parameterTypes);
            if(m != null)
                return m;
        }
        if(isEffectiveAbstract()) {
            for (ClassType it : interfaces) {
                var m = it.findMethodByCodeAndParamTypes(code, parameterTypes);
                if(m != null)
                    return m;
            }
        }
        return null;
    }

    public Method getMethodByCodeAndParamTypes(String code, List<Type> parameterTypes) {
        return Objects.requireNonNull(
                findMethodByCodeAndParamTypes(code, parameterTypes),
                () -> String.format("Can not find method %s(%s) in type %s",
                        code, NncUtils.join(parameterTypes, Type::getName, ","), getName())
        );
    }

    public Method findMethodBySignatureString(String signatureString) {
        return methods.get(Flow::getSignatureString, signatureString);
    }

    public @Nullable Method findMethodByCode(String code) {
        return getMethod(Method::getCode, code);
    }

    public @Nullable Method findSelfMethodByCode(String code) {
        return methods.get(Flow::getCode, code);
    }

    public @Nullable Method findMethodByVerticalTemplate(Method template) {
        return getMethod(Method::getVerticalTemplate, template);
    }

    public Method findSelfMethod(Predicate<Method> predicate) {
        return NncUtils.find(methods.toList(), predicate);
    }

    public <T> Method getMethod(IndexMapper<Method, T> property, T value) {
        var method = methods.get(property, value);
        if (method != null)
            return method;
        if (superClass != null) {
            var m = superClass.getMethod(property, value);
            if(m != null)
                return m;
        }
        if(isEffectiveAbstract()) {
            for (ClassType it : interfaces) {
                var m = it.getMethod(property, value);
                if(m != null)
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

    @Override
    public Set<TypeVariable> getVariables() {
        return NncUtils.flatMapUnique(typeArguments, Type::getVariables);
    }

    public ReadonlyArray<Field> getDeclaredFields() {
        return fields;
    }

    public ReadonlyArray<Constraint> getDeclaredConstraints() {
        return constraints;
    }

    public void addField(Field field) {
        if (fields.contains(field))
            throw new RuntimeException("Field " + field.tryGetId() + " is already added");
        if (tryGetFieldByName(field.getName()) != null || tryGetStaticFieldByName(field.getName()) != null)
            throw BusinessException.invalidField(field, "字段名称'" + field.getName() + "'已存在");
        if (field.getCode() != null &&
                (findSelfFieldByCode(field.getCode()) != null || findSelfStaticFieldByCode(field.getCode()) != null))
            throw BusinessException.invalidField(field, "字段编号" + field.getCode() + "已存在");
        if (field.isStatic())
            staticFields.addChild(field);
        else
            fields.addChild(field);
        resetFieldsMemoryDataStructures();
    }

    @Override
    public void onLoad(IEntityContext context) {
        super.onLoad(context);
        stage = ResolutionStage.INIT;
    }

    public List<Index> getFieldIndices(Field field) {
        return NncUtils.filter(
                getConstraints(Index.class),
                index -> index.isFieldIndex(field)
        );
    }

    @Override
    @Nullable
    public ClassType getCopySource() {
        return copySource;
    }

    public void setCopySource(@Nullable Object copySource) {
        this.copySource = (ClassType) copySource;
    }

    public void addConstraint(Constraint constraint) {
        constraints.addChild(constraint);
    }

    public void removeConstraint(Constraint constraint) {
        constraints.remove(constraint);
    }

    @JsonIgnore
    public boolean isEnum() {
        return category.isEnum();
    }

    public boolean isInterface() {
        return category.isInterface();
    }

    public boolean isEffectiveAbstract() {
        return isInterface() || isAbstract;
    }

    public boolean isAbstract() {
        return isAbstract;
    }

    @JsonIgnore
    public boolean isClass() {
        return category.isClass();
    }

    @JsonIgnore
    public boolean isValue() {
        return category.isValue();
    }

    @JsonIgnore
    public boolean isReference() {
        return isEnum() || isClass() || isArray();
    }

    public void addMapping(ObjectMapping mapping) {
        this.mappings.addChild(mapping);
        if (defaultMapping == null)
            defaultMapping = mapping;
    }

    public Field findFieldById(Id fieldId) {
        Field found = NncUtils.find(fields, f -> f.idEquals(fieldId));
        if (found != null)
            return found;
        if (superClass != null)
            return superClass.findFieldById(fieldId);
        else
            return null;
    }

//    public Field getField(Id fieldId) {
//        return NncUtils.requireNonNull(findFieldById(fieldId),
//                () -> new InternalException(String.format("Field %d not found", fieldId)));
//    }

    public Field getField(Id id) {
        var field = fields.get(Entity::tryGetId, id);
        if (field != null)
            return field;
        if (superClass != null)
            return superClass.getField(id);
        throw new NullPointerException("Can not find field for " + id + " in type " + name);
    }

    public Method getMethod(Id id) {
        return Objects.requireNonNull(methods.get(Entity::tryGetId, id));
    }

    public @Nullable Field findSelfField(Predicate<Field> predicate) {
        return NncUtils.find(fields, predicate);
    }

    public @Nullable Field findField(Predicate<Field> predicate) {
        var field = findSelfField(predicate);
        if (field != null)
            return field;
        if (superClass != null)
            return superClass.findField(predicate);
        return null;
    }

    private List<Field> readyFields() {
        return fields.filter(Field::isReady, true);
    }

    public boolean containsField(long fieldId) {
        return fields.get(Entity::tryGetId, fieldId) != null || superClass != null && superClass.containsField(fieldId);
    }

    public boolean containsStaticField(Id fieldId) {
        return staticFields.get(Entity::tryGetId, fieldId) != null || superClass != null && superClass.containsStaticField(fieldId);
    }

    public Field tryGetFieldByName(String fieldName) {
        if (superClass != null) {
            Field superField = superClass.tryGetFieldByName(fieldName);
            if (superField != null)
                return superField;
        }
        return fields.get(Field::getName, fieldName);
    }

    public Field getFieldByName(String fieldName) {
        return NncUtils.requireNonNull(tryGetFieldByName(fieldName));
    }

    public Field tryGetStaticFieldByName(String fieldName) {
        if (superClass != null) {
            Field superField = superClass.tryGetStaticFieldByName(fieldName);
            if (superField != null)
                return superField;
        }
        return staticFields.get(Field::getName, fieldName);
    }

    public Field getStaticFieldByName(String fieldName) {
        return NncUtils.requireNonNull(tryGetStaticFieldByName(fieldName));
    }

    public Field getStaticFieldByVar(Var var) {
        if (var.isId())
            return getStaticField(var.getId());
        else
            return tryGetStaticFieldByName(var.getName());
    }

    /**
     * Get static field, instance method or static method by var.
     */
    public Property getStaticPropertyByVar(Var var) {
        return Objects.requireNonNull(
                findStaticPropertyByVar(var),
                () -> "Can not find property for var " + var + " in type " + name
        );
    }

    /**
     * Find static field, instance method or static method by var.
     */
    public Property findStaticPropertyByVar(Var var) {
        if(var.isId()) {
            var p = findSelfStaticProperty(m -> m.idEquals(var.getId()));
            if(p != null)
                return p;
        }
        else {
            var p = findSelfStaticProperty(m -> m.getName().equals(var.getName()));
            if(p != null)
                return p;
        }
        if(superClass != null) {
            var p = superClass.findStaticPropertyByVar(var);
            if(p != null)
                return p;
        }
        for (ClassType it : interfaces) {
            var p = it.findStaticPropertyByVar(var);
            if(p != null)
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

    public Field getStaticField(Id id) {
        if (superClass != null && superClass.containsStaticField(id))
            return superClass.getStaticField(id);
        Field field = staticFields.get(Entity::tryGetId, id);
        if (field != null && field.isReady())
            return field;
        throw new InternalException("Field '" + id + "' does not exist or is not ready");
    }

    @Nullable
    public Field findFieldByCode(String code) {
        var field = fields.get(Field::getCode, code);
        if (field != null)
            return field;
        if (superClass != null)
            return superClass.findFieldByCode(code);
        return null;
    }

    public Property getPropertyByVar(Var var) {
        return switch (var.getType()) {
            case NAME -> getPropertyByName(var.getName());
            case ID -> getProperty(var.getId());
        };
    }

    public Property getProperty(Id id) {
        return NncUtils.requireNonNull(getProperty(Property::tryGetId, id),
                "Can not find attribute with id: " + id + " in type " + this);
    }

    public Property getPropertyByCode(String code) {
        return getProperty(Property::getCode, code);
    }

    public Property getPropertyByName(String name) {
        return getProperty(Property::getName, name);
    }

    private <T> Property getProperty(IndexMapper<Property, T> property, T value) {
        var field = fields.get(property, value);
        if (field != null)
            return field;
        var method = methods.get(property, value);
        if (method != null)
            return method;
        if (superClass != null)
            return superClass.getProperty(property, value);
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
    public Method findSelfMethod(String code, List<Type> parameterTypes) {
        return NncUtils.find(
                methods,
                method -> Objects.equals(method.getCode(), code) &&
                        method.getParameterTypes().equals(parameterTypes)
        );
    }

    @Nullable
    public Field findStaticFieldByCode(String code) {
        if (superClass != null) {
            Field superField = superClass.findStaticFieldByCode(code);
            if (superField != null)
                return superField;
        }
        return staticFields.get(Field::getCode, code);
    }

    public ClassSource getSource() {
        return source;
    }

    public boolean isBuiltin() {
        return source == ClassSource.BUILTIN;
    }

    public Field getFieldByVar(Var var) {
        if (var.isId())
            return getField(var.getId());
        else
            return tryGetFieldByName(var.getName());
    }

    public Field getFieldByJavaField(java.lang.reflect.Field javaField) {
        String fieldName = EntityUtils.getMetaFieldName(javaField);
        return requireNonNull(tryGetFieldByName(fieldName),
                "Can not find field for java indexItem " + javaField);
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

    private void getFieldsDownwardInHierarchy0(List<Field> results) {
        listAddAll(results, fields);
        for (ClassType subType : subTypes) {
            subType.getFieldsDownwardInHierarchy0(results);
        }
    }

    public Field getFieldNyNameRequired(String fieldName) {
        return NncUtils.requireNonNull(
                tryGetFieldByName(fieldName), "field not found: " + fieldName
        );
    }

    public void removeField(Field field) {
        if (field.isStatic())
            staticFields.remove(field);
        else
            fields.remove(field);
        resetFieldsMemoryDataStructures();
    }

    @Override
    protected boolean isAssignableFrom0(Type that) {
        if (equals(that)) {
            return true;
        }
        if (that instanceof ClassType thatClass) {
            if (template != null) {
                var s = thatClass.findAncestorType(template);
                if (s != null)
                    return NncUtils.biAllMatch(typeArguments, s.typeArguments, (type, that1) -> type.contains(that1));
                else
                    return false;
            } else {
                if (thatClass.getSuperClass() != null && isAssignableFrom(thatClass.getSuperClass()))
                    return true;
                if (isInterface()) {
                    for (ClassType it : thatClass.interfaces) {
                        if (isAssignableFrom(it)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isPojo() {
        return category.isPojo();
    }

    @Nullable
    public ClassType getSuperClass() {
        return superClass;
    }

    public ClassType asSuper(ClassType template) {
        NncUtils.requireTrue(template.isTemplate());
        ClassType sup = accept(new ElementVisitor<>() {
            @Override
            public ClassType visitClassType(ClassType type) {
                if (type.getEffectiveTemplate() == template)
                    return type;
                ClassType found;
                if (superClass != null && (found = superClass.accept(this)) != null)
                    return found;
                for (ClassType anInterface : interfaces) {
                    if ((found = anInterface.accept(this)) != null)
                        return found;
                }
                return null;
            }
        });
        return NncUtils.requireNonNull(sup,
                String.format("Can not find a super type of type '%s' with template '%s'",
                        this.name, template.name));
    }

    public List<ClassType> getInterfaces() {
        return Collections.unmodifiableList(interfaces);
    }

    @Override
    public List<ClassType> getSuperTypes() {
        if (superTypes == null) {
            superTypes = superClass != null ?
                    NncUtils.prepend(superClass, interfaces.toList()) : interfaces.toList();
        }
        return Collections.unmodifiableList(superTypes);
    }

    public ClassType getAncestorType(ClassType targetType) {
        return NncUtils.requireNonNull(findAncestorType(targetType));
    }

    public ClassType findAncestorType(ClassType targetType) {
        return getClosure().find(t -> t.templateEquals(targetType));
    }

    public PTypeDTO toGenericElementDTO(SerializeContext serializeContext) {
        return new PTypeDTO(
                serializeContext.getId(this),
                serializeContext.getId(Objects.requireNonNull(template)),
                NncUtils.map(typeArguments, serializeContext::getId),
                NncUtils.map(fields, f -> f.toGenericElementDTO(serializeContext)),
                NncUtils.map(staticFields, f -> f.toGenericElementDTO(serializeContext)),
                NncUtils.map(methods, f -> f.toGenericElementDTO(serializeContext)),
                NncUtils.map(mappings, m -> m.toGenericElementDTO(serializeContext))
        );
    }

    public TypeDTO toPTypeDTO(SerializeContext serializeContext) {
        return new TypeDTO(
                serializeContext.getId(this),
                getName(),
                getCode(),
                getCategory().code(),
                isEphemeral(),
                isAnonymous(),
                toGenericElementDTO(serializeContext)
        );
    }

    @Override
    protected ClassTypeParam getParam(SerializeContext serializeContext) {
        try (var serContext = SerializeContext.enter()) {
            typeParameters.forEach(serContext::writeType);
            typeArguments.forEach(serContext::writeType);
            if (superClass != null)
                serContext.writeType(superClass);
            interfaces.forEach(serContext::writeType);
            if (template != null)
                serContext.writeType(template);
            var param = new ClassTypeParam(
                    NncUtils.get(superClass, serContext::getId),
                    NncUtils.map(interfaces, serContext::getId),
                    source.code(),
                    NncUtils.map(fields, Field::toDTO),
                    NncUtils.map(staticFields, Field::toDTO),
                    NncUtils.get(titleField, serContext::getId),
                    NncUtils.map(constraints, Constraint::toDTO),
                    NncUtils.map(methods, f -> f.toDTO(serContext.shouldWriteCode(this), serContext)),
                    NncUtils.map(mappings, m -> m.toDTO(serContext)),
                    NncUtils.get(defaultMapping, serContext::getId),
                    desc,
                    getExtra(),
                    isEnum() ? NncUtils.map(getEnumConstants(), Instance::toDTO) : List.of(),
                    isAbstract,
                    isTemplate(),
                    NncUtils.map(typeParameters, serContext::getId),
                    NncUtils.map(typeParameters, Type::toDTO),
                    NncUtils.get(template, serContext::getId),
                    NncUtils.map(typeArguments, serContext::getId),
                    NncUtils.map(dependencies, serContext::getId),
                    !subTypes.isEmpty(),
                    struct,
                    NncUtils.map(errors, Error::toDTO)
            );
            return param;
        }
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
        if (superClass != null) {
            result = NncUtils.union(
                    superClass.getConstraints(constraintType),
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

    @JsonIgnore
    public ClassType getConcreteType() {
        return this;
    }

    public List<ClassInstance> getEnumConstants() {
        if (!isEnum())
            throw new InternalException("type " + this + " is not a enum type");
        return NncUtils.filterAndMap(
                staticFields,
                this::isEnumConstantField,
                f -> (ClassInstance) f.getStaticValue()
        );
    }

    public EnumConstantRT getEnumConstant(Id id) {
        if (!isEnum())
            throw new InternalException("type " + this + " is not a enum type");
        for (Field field : staticFields) {
            if (isEnumConstantField(field) && Objects.equals(field.getStaticValue().tryGetId(), id))
                return createEnumConstant((ClassInstance) field.getStaticValue());
        }
        throw new InternalException("Can not find enum constant with id " + id);
    }

    private EnumConstantRT createEnumConstant(ClassInstance instance) {
        return new EnumConstantRT(instance);
    }

    boolean isEnumConstantField(Field field) {
        // TODO be more precise
        return isEnum() && field.isStatic() && field.getType() == this
                && field.getStaticValue() instanceof ClassInstance;
    }

    // BFS
    public void foreachAncestor(Consumer<ClassType> action) {
        var queue = new LinkedList<ClassType>();
        queue.offer(this);
        while (!queue.isEmpty()) {
            var type = queue.poll();
            action.accept(type);
            if (type.getSuperClass() != null)
                queue.offer(type.getSuperClass());
            queue.addAll(type.getInterfaces());
        }
    }

    public Method resolveMethod(@NotNull Method methodRef, @NotNull ParameterizedFlowProvider parameterizedFlowProvider) {
        return Objects.requireNonNull(
                tryResolveMethod(methodRef, parameterizedFlowProvider),
                () -> String.format("Fail to resolve method %s.%s in type %s",
                        methodRef.getDeclaringType().getTypeDesc(),
                        methodRef.getTypeDesc(), this)
        );
    }

    public @Nullable Method tryResolveMethod(@NotNull Method methodRef, @NotNull ParameterizedFlowProvider parameterizedFlowProvider) {
        if (methodRef.getDeclaringType() == this)
            return methodRef;
        var hTemplate = methodRef.getHorizontalTemplate();
        if (hTemplate != null) {
            var resolvedTemplate = tryResolveNonParameterizedMethod(hTemplate);
            if (resolvedTemplate == null)
                return null;
            return parameterizedFlowProvider.getParameterizedFlow(resolvedTemplate, methodRef.getTypeArguments());
        } else
            return tryResolveNonParameterizedMethod(methodRef);
    }

    public Method resolveMethod(String code, List<Type> argumentTypes, boolean staticOnly) {
        return Objects.requireNonNull(tryResolveMethod(code, argumentTypes, staticOnly), () ->
                String.format("Can not find method %s(%s) in type %s", code,
                        NncUtils.join(argumentTypes, Type::getName, ","), getName()));
    }

    public @Nullable Method tryResolveMethod(String code, List<Type> argumentTypes, boolean staticOnly) {
        var candidates = new ArrayList<Method>();
        getCallCandidates(code, argumentTypes, staticOnly, candidates);
        out: for (Method m1 : candidates) {
            for (Method m2 : candidates) {
                if(m1 != m2 && m1.isHiddenBy(m2))
                    continue out;
            }
            return m1;
        }
        return null;
    }

    private void getCallCandidates(String code, List<Type> argumentTypes, boolean staticOnly, List<Method> candidates) {
        methods.forEach(m -> {
            if((m.isStatic() || !staticOnly) && m.matches(code, argumentTypes))
                candidates.add(m);
        });
        if(superClass != null)
            superClass.getCallCandidates(code, argumentTypes, staticOnly, candidates);
    }

    @Nullable
    public Method tryResolveNonParameterizedMethod(Method methodref) {
        NncUtils.requireFalse(methodref.isParameterized());
        var methodTable = getMethodTable();
        if (methodref.getDeclaringType().isUncertain())
            methodref = methodTable.findByVerticalTemplate(Objects.requireNonNull(methodref.getVerticalTemplate()));
        return methodTable.findByOverridden(methodref);
    }

    @Override
    public boolean isUncertain() {
        return NncUtils.anyMatch(typeArguments, Type::isUncertain);
    }

    @Nullable
    public ClassType getTemplate() {
        return template;
    }

    //    @Override
    public void setTemplate(Object template) {
        NncUtils.requireNull(this.template);
        isParameterized = template != null;
        this.template = copySource = (ClassType) template;
    }

    public ClassType getEffectiveTemplate() {
        return template != null ? template : this;
    }

    public boolean templateEquals(ClassType that) {
        return this == that || this.getTemplate() == that;
    }

    public List<TypeVariable> getTypeParameters() {
        return typeParameters.toList();
    }

    @Override
    public void addTypeParameter(TypeVariable typeParameter) {
        isTemplate = true;
        typeParameters.addChild(typeParameter);
        typeArguments.add(typeParameter);
    }

    public List<Type> getTypeArguments() {
        return typeArguments.toList();
    }

    public List<? extends Type> getEffectiveTypeArguments() {
        return template == null ? typeParameters.toList() : getTypeArguments();
    }

//    @Override
//    public ClassType getRawClass() {
//        return this;
//    }

//    public GenericClass getGenericSuperType() {
//        return genericSuperType;
//    }

//    public List<GenericClass> getGenericInterfaces() {
//        return Collections.unmodifiableList(genericInterfaces);
//    }

    public Index getUniqueConstraint(List<Field> fields) {
        return find(getIndices(), c -> c.isUnique() && c.getTypeFields().equals(fields));
    }

    public List<Index> getIndices() {
        return getConstraints(Index.class);
    }

    @Override
    protected String toString0() {
        return "ClassType " + name + " (id:" + id + ")";
    }

    public void setTypeArguments(List<? extends Type> typeArguments) {
        if (isTemplate() && !NncUtils.iterableEquals(typeParameters, typeArguments))
            throw new InternalException("Type arguments must equal to type parameters for a template type");
        this.typeArguments.reset(typeArguments);
        parameterizedTypeKey = null;
    }

    public void setSuperClass(@Nullable ClassType superClass) {
        if (this.superClass != null)
            this.superClass.removeSubType(this);
        this.superClass = superClass;
        if (superClass != null)
            superClass.addSubType(this);
        onSuperTypesChanged();
        superTypes = null;
    }

    public void setInterfaces(List<ClassType> interfaces) {
        for (ClassType anInterface : this.interfaces) {
            anInterface.removeSubType(this);
        }
        this.interfaces.clear();
        this.interfaces.addAll(interfaces);
        for (ClassType anInterface : interfaces) {
            anInterface.addSubType(this);
        }
        onSuperTypesChanged();
        superTypes = null;
    }

    protected void onAncestorChanged0() {
        getMethodTable().rebuild();
        rank = 0;
    }

    public void addDependency(ClassType dependency) {
        NncUtils.requireFalse(dependencies.contains(dependency),
                "Dependency " + dependency + " already exists in type " + getName());
        dependencies.add(dependency);
    }

    public void setDependencies(List<ClassType> dependencies) {
        this.dependencies.clear();
        this.dependencies.addAll(dependencies);
    }

    public List<ClassType> getDependencies() {
        return dependencies.toList();
    }

    public Type getDependency(ClassType template) {
        return NncUtils.findRequired(dependencies, dep -> Objects.equals(dep.getTemplate(), template));
    }

    public void clearTypeParameters() {
        this.typeParameters.clear();
    }

    public void setTypeParameters(List<TypeVariable> typeParameters) {
        this.isTemplate = !typeParameters.isEmpty();
        typeParameters.forEach(tp -> tp.setGenericDeclaration(this));
        this.typeParameters.resetChildren(typeParameters);
        if (isTemplate())
            setTypeArguments(typeParameters);
    }

    public ObjectMapping getMappingInAncestors(Id id) {
        var mapping = findMapping(id);
        if (mapping != null)
            return mapping;
        if (superClass != null) {
            if ((mapping = superClass.getMappingInAncestors(id)) != null)
                return mapping;
        }
        for (ClassType it : interfaces) {
            if ((mapping = it.getMappingInAncestors(id)) != null)
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
    }

    @Override
    protected boolean afterContextInitIdsInternal() {
        if (template != null || isTemplate()) {
            if (parameterizedTypeKey == null) {
                parameterizedTypeKey = Types.getParameterizedKey(getEffectiveTemplate(), typeArguments);
            }
        }
        return true;
    }

    public boolean isParameterized() {
        return template != null && template != this;
    }

    @Override
    public String getTypeDesc() {
        if(isParameterized())
            return Objects.requireNonNull(template).getName() + "<" + NncUtils.join(typeArguments, Type::getTypeDesc, ",") + ">";
        else
            return getName();
    }

    @Override
    public TypeKey getTypeKey() {
        return template != null ?
                new ParameterizedTypeKey(template.getStringId(), NncUtils.map(typeArguments, Entity::getStringId))
                : new ClassTypeKey(getId());
    }

    @Override
    protected List<?> beforeRemoveInternal(IEntityContext context) {
        if (superClass != null)
            superClass.removeSubType(this);
        for (ClassType anInterface : interfaces) {
            anInterface.removeSubType(this);
        }
        if (isTemplate())
            return context.selectByKey(ClassType.TEMPLATE_IDX, this);
        else
            return List.of();
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
        return visitor.visitClassType(this);
    }

    @Override
    public void getCapturedTypes(Set<CapturedType> capturedTypes) {
        typeArguments.forEach(t -> t.getCapturedTypes(capturedTypes));
    }

    public ClassType findInClosure(Id id) {
        return getClosure().find(t -> Objects.equals(t.tryGetId(), id));
    }

    @Override
    public void onChange(ClassInstance instance, IEntityContext context) {
        rebuildMethodTable();
        if (!isInterface()) {
            for (ClassType it : interfaces) {
                for (Method method : it.getMethods()) {
                    if (tryResolveNonParameterizedMethod(method) == null) {
                        throw new BusinessException(ErrorCode.INTERFACE_FLOW_NOT_IMPLEMENTED,
                                getName(), it.getName(), method.getName());
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
        if (!(context instanceof DefContext) && shouldGenerateBuiltinMapping()) {
            MappingSaver.create(context).saveBuiltinMapping(this, true);
        }
    }

    public boolean shouldGenerateBuiltinMapping() {
        return isClass() && !anonymous;
    }

    public void setDefaultMapping(@Nullable ObjectMapping mapping) {
        this.defaultMapping = mapping;
    }

    @Nullable
    public Field getTitleField() {
        if (titleField != null)
            return titleField;
        if (superClass != null)
            return superClass.getTitleField();
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

    @Override
    public boolean isViewType(Type type) {
        if (super.isViewType(type))
            return true;
        return NncUtils.anyMatch(mappings, m -> m.getTargetType() == type);
    }

    @Override
    public String getInternalName(@org.jetbrains.annotations.Nullable Flow current) {
        if(typeArguments.isEmpty())
            return getCodeRequired();
        else
            return requireNonNull(template).getCodeRequired() + "<" + NncUtils.join(typeArguments, type -> type.getInternalName(current)) + ">";
    }

    public boolean isList() {
        var t = getEffectiveTemplate();
        return t == StandardTypes.getListType() || StandardTypes.getChildListType() == t || StandardTypes.getReadWriteListType() == t;
    }

    public boolean isChildList() {
        return getEffectiveTemplate() == StandardTypes.getChildListType();
    }

    public Type getListElementType() {
        NncUtils.requireTrue(isList());
        return getTypeArguments().get(0);
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

    @Override
    public boolean isCaptured() {
        return isParameterized() && NncUtils.anyMatch(typeArguments, Type::isCaptured);
    }
}

