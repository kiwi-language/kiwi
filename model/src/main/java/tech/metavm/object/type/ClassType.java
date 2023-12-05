package tech.metavm.object.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.jetbrains.annotations.NotNull;
import tech.metavm.common.ErrorCode;
import tech.metavm.entity.*;
import tech.metavm.expression.Var;
import tech.metavm.flow.Error;
import tech.metavm.flow.ErrorLevel;
import tech.metavm.flow.Flow;
import tech.metavm.flow.FlowChecker;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.util.*;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static tech.metavm.util.NncUtils.*;

@EntityType("Class类型")
public class ClassType extends Type implements GenericDeclaration {

    public static final IndexDef<ClassType> UNIQUE_NAME = IndexDef.uniqueKey(ClassType.class, "name");

    public static final IndexDef<ClassType> IDX_PARAMETERIZED_TYPE_KEY =
            IndexDef.uniqueKey(ClassType.class, "parameterizedTypeKey");

    public static final IndexDef<ClassType> UNIQUE_CODE = IndexDef.uniqueKey(ClassType.class, "code");

    public static final IndexDef<ClassType> UNIQUE_SOURCE_CLASS_NAME = IndexDef.uniqueKey(ClassType.class, "sourceClassName");

    public static final IndexDef<ClassType> TEMPLATE_IDX = IndexDef.normalKey(ClassType.class, "template");

    public static final IndexDef<ClassType> TYPE_ARGUMENTS_IDX = IndexDef.normalKey(ClassType.class, "typeArguments");

    @EntityField("超类")
    @Nullable
    private ClassType superClass;
    @EntityField("源码类名")
    private @Nullable String sourceClassName;
    @ChildEntity("接口")
    private final ReadWriteArray<ClassType> interfaces = addChild(new ReadWriteArray<>(ClassType.class), "interfaces");
    @EntityField("来源")
    private ClassSource source;
    @ChildEntity("子类列表")
    private final ReadWriteArray<ClassType> subTypes = addChild(new ReadWriteArray<>(ClassType.class), "subTypes");
    @EntityField("描述")
    private @Nullable String desc;
    @ChildEntity("字段列表")
    private final ChildArray<Field> fields = addChild(new ChildArray<>(Field.class), "fields");
    @ChildEntity("流程列表")
    private final ChildArray<Flow> flows = addChild(new ChildArray<>(Flow.class), "flows");
    @ChildEntity("静态字段列表")
    private final ChildArray<Field> staticFields = addChild(new ChildArray<>(Field.class), "staticFields");
    @ChildEntity("约束列表")
    private final ChildArray<Constraint> constraints = addChild(new ChildArray<>(Constraint.class), "constraints");
    @Nullable
    @EntityField("模板")
    private final ClassType template;
    @EntityField("是否模版")
    private final boolean isTemplate;
    // Don't remove, used for search
    @EntityField("是否参数化")
    private final boolean isParameterized;
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

    private transient ResolutionStage stage = ResolutionStage.INIT;

    private transient volatile FlowTable flowTable;

    // longest path from the current type upwards in the hierarchy
    private transient int rank;

    private transient List<ClassType> superTypes;

    private transient volatile List<Field> sortedFields;


    public ClassType(
            Long tmpId,
            String name,
            @Nullable String code,
            @Nullable String sourceClassName,
            @Nullable ClassType superClass,
            List<ClassType> interfaces,
            TypeCategory category,
            ClassSource source,
            boolean anonymous,
            boolean ephemeral,
            @Nullable String desc,
            boolean isTemplate,
            @Nullable ClassType template,
            List<Type> typeArguments
    ) {
        super(name, anonymous, ephemeral, category);
        setTmpId(tmpId);
        this.setCode(code);
        this.sourceClassName = sourceClassName;
        setSuperClass(superClass);
        setInterfaces(interfaces);
        this.source = source;
        this.desc = desc;
        this.isTemplate = isTemplate;
        this.isParameterized = template != null;
        this.typeArguments.addAll(typeArguments);
        this.template = template;
        flowTable.rebuild();
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

    void addSubType(ClassType subType) {
        if (subTypes.contains(subType)) {
            throw new InternalException("Subtype '" + subType + "' is already added to this type");
        }
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
        if (superClass != null) {
            return NncUtils.union(superClass.getAllFields(), readyFields());
        } else {
            return readyFields();
        }
    }

    public void forEachField(Consumer<Field> action) {
        if (superClass != null)
            superClass.forEachField(action);
        this.fields.stream().filter(Field::isReady).forEach(action);
    }

    @Nullable
    public String getSourceClassName() {
        return sourceClassName;
    }

    @Override
    public void onBind(IEntityContext context) {
        if (isTemplate)
            context.getGenericContext().addType(this);
    }

    public List<Field> getFieldsByPath(List<String> path) {
        List<Field> result = new ArrayList<>();
        getFieldsByPath0(path, result);
        return result;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    private void getFieldsByPath0(List<String> path, List<Field> result) {
        NncUtils.requireMinimumSize(path, 1);
        String fieldName = path.get(0);
        Field field = getFieldNyNameRequired(fieldName);
        result.add(field);
        if (path.size() > 1) {
            Type fieldType = field.getType();
            if (fieldType instanceof ClassType classType) {
                classType.getFieldsByPath0(path.subList(1, path.size()), result);
            } else {
                throw new InternalException("Invalid field path '" + NncUtils.join(path, ".") + "'");
            }
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

    public Set<Long> getSubTypeIds() {
        Set<Long> typeIds = new HashSet<>();
        accept(new VoidElementVisitor() {
            @Override
            public Void visitClassType(ClassType type) {
                typeIds.add(type.getIdRequired());
                for (ClassType subType : type.subTypes)
                    subType.accept(this);
                return super.visitClassType(type);
            }
        });
        return typeIds;
    }

    public int getRank() {
        if (rank == 0) {
            rank = NncUtils.maxInt(getSuperTypes(), Type::getRank, 0) + 1;
        }
        return rank;
    }

    public ParameterizedTypeKey getParameterizedKey() {
        NncUtils.requireNonNull(template);
        return new ParameterizedTypeKey(template.getRef(), NncUtils.map(typeArguments, Entity::getRef));
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

    //<editor-fold desc="flow">

    public Flow getDefaultConstructor() {
        return getFlowByCodeAndParamTypes(getEffectiveTemplate().getCode(), List.of());
    }

    public List<Field> getSortedFields() {
        if (sortedFields == null) {
            synchronized (this) {
                if (sortedFields == null) {
                    var sf = new ArrayList<Field>();
                    forEachField(sf::add);
                    sf.sort(Comparator.comparingLong(Field::getIdRequired));
                    sortedFields = sf;
                }
            }
        }
        return sortedFields;
    }

    private void resetFieldsMemoryDataStructures() {
        this.sortedFields = null;
    }

    public void moveFlow(Flow flow, int index) {
        moveProperty(flows, flow, index);
    }

    private FlowTable getFlowTable() {
        if (flowTable == null) {
            synchronized (this) {
                if (flowTable == null)
                    flowTable = new FlowTable(this);
            }
        }
        return flowTable;
    }

    public List<Flow> getFlows() {
        return flows.toList();
    }

    public List<Flow> getAllFlows() {
        if (superClass != null) {
            return NncUtils.union(superClass.getAllFlows(), NncUtils.listOf(flows));
        } else {
            return NncUtils.listOf(getFlows());
        }
    }

    public ReadonlyArray<Flow> getDeclaredFlows() {
        return flows;
    }

    public Flow getFlow(long id) {
        return flows.get(Entity::getId, id);
    }

    public Flow tryGetFlow(String name, List<Type> parameterTypes) {
        var flow = NncUtils.find(flows,
                f -> Objects.equals(f.getName(), name) && f.getParameterTypes().equals(parameterTypes));
        if (flow != null) {
            return flow;
        }
        if (superClass != null) {
            return superClass.getFlow(name, parameterTypes);
        }
        return null;
    }

    public Flow getFlow(String name, List<Type> parameterTypes) {
        return NncUtils.requireNonNull(
                tryGetFlow(name, parameterTypes),
                () -> new InternalException("Can not find flow '" + name + "(" +
                        NncUtils.join(parameterTypes, Type::getName, ",")
                        + ")' in type '" + getName() + "'")
        );
    }

    public Flow getFlowByCodeAndParamTypes(String code, List<Type> parameterTypes) {
        var flow = NncUtils.find(flows,
                f -> Objects.equals(f.getCode(), code) && f.getParameterTypes().equals(parameterTypes));
        if (flow != null) {
            return flow;
        }
        if (superClass != null) {
            return superClass.getFlowByCodeAndParamTypes(code, parameterTypes);
        }
        throw new InternalException("Can not find flow '" + code + "(" +
                NncUtils.join(parameterTypes, Type::getName, ",")
                + ")' in type '" + getName() + "'");
    }

    public Flow getFlowByCode(String code) {
        return getFlow(Property::getCode, code);
    }

    public Flow getFlowByVerticalTemplate(Flow template) {
        return getFlow(Flow::getVerticalTemplate, template);
    }

    public Flow findFlow(Predicate<Flow> predicate) {
        return NncUtils.find(flows.toList(), predicate);
    }

    public <T> Flow getFlow(IndexMapper<Flow, T> property, T value) {
        var flow = flows.get(property, value);
        if (flow != null) {
            return flow;
        }
        if (superClass != null) {
            return superClass.getFlow(property, value);
        }
        return null;
    }

    public void removeFlow(Flow flow) {
        flows.remove(flow);
        getFlowTable().rebuild();
    }

    public void addFlow(Flow flow) {
        if (flows.contains(flow)) {
            throw new InternalException("Flow '" + flow + "' is already added to the class type");
        }
        flows.addChild(flow);
        getFlowTable().rebuild();
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
        if (field.getId() != null && getField(field.getId()) != null) {
            throw new RuntimeException("Field " + field.getId() + " is already added");
        }
        if (tryGetFieldByName(field.getName()) != null || tryGetStaticFieldByName(field.getName()) != null) {
            throw BusinessException.invalidField(field, "字段名称'" + field.getName() + "'已存在");
        }
        if (field.getCode() != null &&
                (findFieldByCode(field.getCode()) != null || findStaticFieldByCode(field.getCode()) != null)) {
            throw BusinessException.invalidField(field, "字段编号" + field.getCode() + "已存在");
        }
        if (field.isAsTitle() && getTileField() != null) {
            throw BusinessException.multipleTitleFields();
        }
        if (field.isStatic()) {
            staticFields.addChild(field);
        } else {
            fields.addChild(field);
        }
        resetFieldsMemoryDataStructures();
    }

    @Override
    public void onLoad() {
        super.onLoad();
        stage = ResolutionStage.INIT;
    }

    public List<Index> getFieldIndices(Field field) {
        return NncUtils.filter(
                getConstraints(Index.class),
                index -> index.isFieldIndex(field)
        );
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

    public boolean isAbstract() {
        // TODO support abstract class
        return isInterface();
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

    public Field findFieldById(long fieldId) {
        Field found = NncUtils.find(fields, f -> f.idEquals(fieldId));
        if (found != null) {
            return found;
        }
        if (superClass != null)
            return superClass.findFieldById(fieldId);
        else
            return null;
    }

    public Field getField(long fieldId) {
        return NncUtils.requireNonNull(findFieldById(fieldId),
                () -> new InternalException(String.format("Field %d not found", fieldId)));
    }

    public Field findField(Predicate<Field> predicate) {
        return NncUtils.find(fields, predicate);
    }

    private List<Field> readyFields() {
        return fields.filter(Field::isReady, true);
    }

    public boolean containsField(long fieldId) {
        return fields.get(Entity::getId, fieldId) != null || superClass != null && superClass.containsField(fieldId);
    }

    public boolean containsStaticField(long fieldId) {
        return staticFields.get(Entity::getId, fieldId) != null || superClass != null && superClass.containsStaticField(fieldId);
    }

    public Field tryGetFieldByName(String fieldName) {
        if (superClass != null) {
            Field superField = superClass.tryGetFieldByName(fieldName);
            if (superField != null) {
                return superField;
            }
        }
        return fields.get(Field::getName, fieldName);
    }

    public Field getFieldByName(String fieldName) {
        return NncUtils.requireNonNull(tryGetFieldByName(fieldName));
    }

    public Field tryGetStaticFieldByName(String fieldName) {
        if (superClass != null) {
            Field superField = superClass.tryGetStaticFieldByName(fieldName);
            if (superField != null) {
                return superField;
            }
        }
        return staticFields.get(Field::getName, fieldName);
    }

    public Field getStaticFieldByName(String fieldName) {
        return NncUtils.requireNonNull(tryGetStaticFieldByName(fieldName));
    }

    public Field getStaticFieldByVar(Var var) {
        if (var.isId()) {
            return getStaticField(var.getId());
        } else {
            return tryGetStaticFieldByName(var.getName());
        }
    }

    public void setSource(ClassSource source) {
        this.source = source;
    }

    public Field getStaticField(long id) {
        if (superClass != null && superClass.containsStaticField(id)) {
            return superClass.getStaticField(id);
        }
        Field field = staticFields.get(Entity::getId, id);
        if (field != null && field.isReady()) {
            return field;
        }
        throw new InternalException("Field '" + id + "' does not exist or is not ready");
    }

    @Nullable
    public Field findFieldByCode(String code) {
        var field = fields.get(Field::getCode, code);
        if (field != null) {
            return field;
        }
        if (superClass != null) {
            return superClass.findFieldByCode(code);
        }
        return null;
    }

    public Property getPropertyByVar(Var var) {
        return switch (var.getType()) {
            case NAME -> getPropertyByName(var.getName());
            case ID -> getProperty(var.getId());
        };
    }

    public Property getProperty(long id) {
        return NncUtils.requireNonNull(getProperty(Entity::getId, id),
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
        if (field != null) {
            return field;
        }
        var flow = flows.get(property, value);
        if (flow != null) {
            return flow;
        }
        if (superClass != null) {
            return superClass.getProperty(property, value);
        }
        return null;
    }

    public List<Property> getProperties() {
        return NncUtils.concatList(fields.toList(), flows.toList());
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
    public Flow findSelfFlow(String code, List<Type> parameterTypes) {
        return NncUtils.find(
                flows,
                flow -> Objects.equals(flow.getCode(), code) &&
                        flow.getParameterTypes().equals(parameterTypes)
        );
    }

    @Nullable
    public Field findStaticFieldByCode(String code) {
        if (superClass != null) {
            Field superField = superClass.findStaticFieldByCode(code);
            if (superField != null) {
                return superField;
            }
        }
        return staticFields.get(Field::getCode, code);
    }

    public ClassSource getSource() {
        return source;
    }

    public boolean isFromReflection() {
        return source == ClassSource.BUILTIN;
    }

    public Field getFieldByVar(Var var) {
        if (var.isId()) {
            return getField(var.getId());
        } else {
            return tryGetFieldByName(var.getName());
        }
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
        if (fieldType.isBinaryNullable()) {
            fieldType = fieldType.getUnderlyingType();
        }
        if (fieldType.getSQLType() == null) {
            return null;
        }
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
        if (field.isStatic()) {
            staticFields.remove(field);
        } else {
            fields.remove(field);
        }
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
                if (s != null) {
                    return NncUtils.biAllMatch(typeArguments, s.typeArguments, Type::contains);
                } else {
                    return false;
                }
            } else {
                if (thatClass.getSuperClass() != null && isAssignableFrom(thatClass.getSuperClass())) {
                    return true;
                }
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
        NncUtils.requireTrue(template.isTemplate);
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

    @Override
    public String getKey(Function<Type, java.lang.reflect.Type> getJavaType) {
        if (template == null) {
            java.lang.reflect.Type javaType = NncUtils.requireNonNull(
                    getJavaType.apply(this), "Can not get java type for type '" + this + "'");
            return javaType.getTypeName();
        } else {
            java.lang.reflect.Type javaType = NncUtils.requireNonNull(
                    getJavaType.apply(template), "Can not get java type for type '" + this + "'");
            return javaType.getTypeName() + "<"
                    + NncUtils.join(typeArguments, arg -> arg.getKey(getJavaType))
                    + ">";
        }
    }

    @Override
    protected ClassTypeParam getParam() {
        try (var context = SerializeContext.enter()) {
            typeParameters.forEach(context::writeType);
            typeArguments.forEach(context::writeType);
            if (superClass != null) {
                context.writeType(superClass);
            }
            interfaces.forEach(context::writeType);
            if (template != null) {
                context.writeType(template);
            }
            var param = new ClassTypeParam(
                    sourceClassName,
                    NncUtils.get(superClass, context::getRef),
                    NncUtils.map(interfaces, context::getRef),
                    source.code(),
                    NncUtils.map(fields, Field::toDTO),
                    NncUtils.map(staticFields, Field::toDTO),
                    NncUtils.map(constraints, Constraint::toDTO),
                    NncUtils.map(flows, f -> f.toDTO(context.shouldWriteCode(this))),
                    desc,
                    getExtra(),
                    isEnum() ? NncUtils.map(getEnumConstants(), ClassInstance::toDTO) : List.of(),
                    isTemplate(),
                    NncUtils.map(typeParameters, context::getRef),
                    NncUtils.map(typeParameters, Type::toDTO),
                    NncUtils.get(template, context::getRef),
                    NncUtils.map(typeArguments, context::getRef),
                    NncUtils.map(dependencies, context::getRef),
                    !subTypes.isEmpty(),
                    NncUtils.map(errors, Error::toDTO)
            );
            return param;
        }
    }

    protected Object getExtra() {
        return null;
    }

    @JsonIgnore
    public Field getTileField() {
        return find(getAllFields(), Field::isAsTitle);
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

    public void setSourceClassName(@Nullable String sourceClassName) {
        this.sourceClassName = sourceClassName;
    }

    public List<Constraint> getConstraints() {
        return constraints.toList();
    }

    public <T extends Constraint> T getConstraint(Class<T> constraintType, long id) {
        return find(getConstraints(constraintType), c -> c.getIdRequired() == id);
    }

    public List<CheckConstraint> getFieldCheckConstraints(Field field) {
        var constraints = getConstraints(CheckConstraint.class);
        return NncUtils.filter(constraints, c -> c.isFieldConstraint(field));
    }

    @SuppressWarnings("unused")
    public Constraint getConstraint(long id) {
        return NncUtils.find(requireNonNull(constraints), c -> c.getIdRequired() == id);
    }

    public Index getUniqueConstraint(long id) {
        return getConstraint(Index.class, id);
    }

    @JsonIgnore
    public ClassType getConcreteType() {
        return this;
    }

    public List<ClassInstance> getEnumConstants() {
        if (!isEnum()) {
            throw new InternalException("type " + this + " is not a enum type");
        }
        return NncUtils.filterAndMap(
                staticFields,
                this::isEnumConstantField,
                f -> (ClassInstance) f.getStaticValue()
        );
    }

    public EnumConstantRT getEnumConstant(long id) {
        if (!isEnum()) {
            throw new InternalException("type " + this + " is not a enum type");
        }
        for (Field field : staticFields) {
            if (isEnumConstantField(field) && Objects.equals(field.getStaticValue().getId(), id)) {
                return createEnumConstant((ClassInstance) field.getStaticValue());
            }
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

    public Flow resolveFlow(@NotNull Flow flowRef, @NotNull IEntityContext context) {
        return Objects.requireNonNull(
                tryResolveFlow(flowRef, context),
                () -> String.format("Fail to resolve flow %s in type %s", flowRef, this)
        );
    }

    public @Nullable Flow tryResolveFlow(@NotNull Flow flowRef, @NotNull IEntityContext context) {
        if (flowRef.getDeclaringType() == this)
            return flowRef;
        var hTemplate = flowRef.getHorizontalTemplate();
        if (hTemplate != null) {
            var resolvedTemplate = tryResolveNonParameterizedFlow(hTemplate);
            if (resolvedTemplate == null)
                return null;
            return context.getGenericContext().getParameterizedFlow(resolvedTemplate, flowRef.getTypeArguments());
        } else
            return tryResolveNonParameterizedFlow(flowRef);
    }

    @Nullable
    public Flow tryResolveNonParameterizedFlow(Flow flowRef) {
        NncUtils.requireFalse(flowRef.isParameterized());
        var flowTable = getFlowTable();
        if (flowRef.getDeclaringType().isUncertain())
            flowRef = flowTable.findByVerticalTemplate(Objects.requireNonNull(flowRef.getVerticalTemplate()));
        return flowTable.findByOverridden(flowRef);
    }

    @Override
    public boolean isUncertain() {
        return NncUtils.anyMatch(typeArguments, Type::isUncertain);
    }

    @Override
    public void validate() {
        super.validate();
        rebuildFlowTable();
        if (!isInterface()) {
            for (ClassType it : interfaces) {
                for (Flow flow : it.getFlows()) {
                    if (tryResolveNonParameterizedFlow(flow) == null) {
                        throw new BusinessException(ErrorCode.INTERFACE_FLOW_NOT_IMPLEMENTED,
                                getName(), it.getName(), flow.getName());
                    }
                }
            }
        }
    }

    @Nullable
    public ClassType getTemplate() {
        return template;
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
        NncUtils.requireTrue(isTemplate(), "Can not add type parameter to a non-template type");
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
        return find(getUniqueConstraints(), c -> c.getTypeFields().equals(fields));
    }

    public List<Index> getUniqueConstraints() {
        return getConstraints(Index.class);
    }


    @Override
    protected String toString0() {
        return "ClassType " + name + " (id:" + id + ")";
    }

    public void setTypeArguments(List<Type> typeArguments) {
        NncUtils.requireFalse(isTemplate(), "Can not set type arguments for a template type");
        this.typeArguments.clear();
        this.typeArguments.addAll(typeArguments);
        parameterizedTypeKey = null;
    }

    public void setSuperClass(@Nullable ClassType superClass) {
        if (this.superClass != null) {
            this.superClass.removeSubType(this);
        }
        this.superClass = superClass;
        if (superClass != null) {
            superClass.addSubType(this);
        }
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
        getFlowTable().rebuild();
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
        requireTrue(isTemplate(), "Can not set type parameters for a non-template type");
        requireTrue(allMatch(typeParameters, typeParam -> typeParam.getGenericDeclaration() == this));
        this.typeParameters.resetChildren(typeParameters);
        this.typeArguments.reset(typeParameters);
    }

    public void setFields(List<Field> fields) {
        requireTrue(allMatch(fields, f -> f.getDeclaringType() == this));
        this.fields.resetChildren(fields);
    }

    public void moveField(Field field, int index) {
        moveProperty(fields, field, index);
    }

    private <T extends Property> void moveProperty(ChildArray<T> properties, T property, int index) {
        if (index < 0 || index >= properties.size()) {
            throw new BusinessException(ErrorCode.INDEX_OUT_OF_BOUND);
        }
        if (!properties.remove(property)) {
            throw new BusinessException(ErrorCode.PROPERTY_NOT_FOUND, property.getName());
        }
        if (index >= properties.size()) {
            properties.addChild(property);
        } else {
            properties.addChild(index, property);
        }
    }

    public void setStaticFields(List<Field> staticFields) {
        requireTrue(allMatch(staticFields, f -> f.getDeclaringType() == this));
        this.staticFields.resetChildren(staticFields);
    }

    public void setConstraints(List<Constraint> constraints) {
        requireTrue(allMatch(constraints, c -> c.getDeclaringType() == this));
        this.constraints.resetChildren(constraints);
    }

    public void setFlows(List<Flow> flows) {
        requireTrue(allMatch(flows, f -> f.getDeclaringType() == this));
        this.flows.resetChildren(flows);
    }

    @Override
    protected boolean afterContextInitIdsInternal() {
        if (template != null || isTemplate) {
            if (parameterizedTypeKey == null) {
                parameterizedTypeKey = pTypeKey(getEffectiveTemplate(), typeArguments);
            }
        }
        return true;
    }

    public boolean isParameterized() {
        return isParameterized;
    }

    @Override
    public TypeKey getTypeKey() {
        return template != null ?
                new ParameterizedTypeKey(template.getRef(), NncUtils.map(typeArguments, Entity::getRef))
                : new ClassTypeKey(getRef());
    }

    public static String pTypeKey(ClassType template, Iterable<? extends Type> typeArguments) {
        return encodeBase64(template.getIdRequired()) + "-"
                + NncUtils.join(typeArguments, typeArg -> encodeBase64(typeArg.getIdRequired()), "-");
    }

    @Override
    protected List<?> beforeRemoveInternal(IEntityContext context) {
        if (superClass != null) {
            superClass.removeSubType(this);
        }
        for (ClassType anInterface : interfaces) {
            anInterface.removeSubType(this);
        }
        if (isTemplate)
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
        if (stage == null) {
            stage = ResolutionStage.DEFINITION;
        }
        return stage;
    }

    public void rebuildFlowTable() {
        getFlowTable().rebuild();
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitClassType(this);
    }

    public ClassType findInClosure(long id) {
        return getClosure().find(t -> Objects.equals(t.getId(), id));
    }
}

