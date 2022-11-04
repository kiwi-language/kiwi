package tech.metavm.object.meta;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tech.metavm.entity.*;
import tech.metavm.object.instance.persistence.InstancePO;
import tech.metavm.object.instance.persistence.mappers.InstanceMapper;
import tech.metavm.object.meta.persistence.ConstraintPO;
import tech.metavm.object.meta.persistence.FieldPO;
import tech.metavm.object.meta.persistence.TypePO;
import tech.metavm.object.meta.persistence.mappers.ConstraintMapper;
import tech.metavm.object.meta.persistence.mappers.FieldMapper;
import tech.metavm.object.meta.persistence.mappers.TypeMapper;
import tech.metavm.object.meta.persistence.query.TypeQuery;
import tech.metavm.util.NncUtils;

import java.util.*;

@Component
public class TypeStore implements EntityStore<Type> {

    public static final long MAX_NUM_OPTIONS = 64;

    @Autowired
    private TypeMapper typeMapper;

    @Autowired
    private FieldMapper fieldMapper;

    @Autowired
    private ConstraintMapper constraintMapper;

    @Autowired
    private InstanceMapper instanceMapper;

    @Autowired
    private InstanceEntityStore instanceEntityStore;

    public Type getParameterizedType(Type rawType, List<Type> typeArguments, EntityContext context) {
        Objects.requireNonNull(rawType, "rawType is required");
        NncUtils.requireNotEmpty(typeArguments, "typeArguments can not be empty");
        TypePO typePO = typeMapper.selectParameterized(
                context.getTenantId(), rawType.getId(), NncUtils.map(typeArguments, Entity::getId)
        );
        if(typePO != null) {
            return createFromPO(typePO, context, LoadingOption.none());
        }
        else {
            Type pType = new Type(
                    rawType.getName() + "<" + NncUtils.join(typeArguments, Type::getName) + ">",
                    TypeCategory.PARAMETERIZED,
                    false,
                    rawType.isEphemeral(),
                    rawType,
                    typeArguments,
                    null,
                    context
            );
            context.add(pType);
            return pType;
        }
    }

    public List<Type> getDependentTypes(Type type) {
        List<TypePO> typePOS = NncUtils.merge(
                typeMapper.selectByRawTypeId(type.getTenantId(), type.getId()),
                typeMapper.selectByTypeArgumentId(type.getTenantId(), type.getId())
        );
        return createFromPOs(typePOS, type.getContext(), LoadingOption.none());
    }

    public Type getByCategory(TypeCategory category, EntityContext context) {
        TypePO typePO = typeMapper.selectByCode(category.code());
        return createFromPO(typePO, context, LoadingOption.none());
    }

    public Type getByName(String name, EntityContext context) {
        TypePO typePO = typeMapper.selectByName(context.getTenantId(), name);
        return createFromPO(typePO, context, LoadingOption.none());
    }

    private Type createFromPO(TypePO typePO, EntityContext context, Set<LoadingOption> options) {
        if(typePO == null) {
            return null;
        }
        return createFromPOs(List.of(typePO), context, options).get(0);
    }

    private List<Type> createFromPOs(List<TypePO> typePOs, EntityContext context, Set<LoadingOption> options) {
        if(NncUtils.isEmpty(typePOs)) {
            return List.of();
        }
        Set<Long> dependencyTypeIds = NncUtils.mergeUnique(
                NncUtils.mapAndFilter(typePOs, TypePO::getRawTypeId, Objects::nonNull),
                NncUtils.flatMapAndFilter(typePOs, TypePO::getTypeArgumentIds, Objects::nonNull)
        );
        context.batchGet(Type.class, dependencyTypeIds, options);
        List<Type> types = NncUtils.map(typePOs, typePO -> new Type(typePO, context));
        List<Type> pojoOrEnums = NncUtils.filter(types, t -> hasFieldsOrOptions(t.getCategory()));
        if(NncUtils.isNotEmpty(pojoOrEnums)) {
            if (!options.contains(LoadingOption.FIELDS_LAZY_LOADING)) {
                preloadFields(pojoOrEnums, context);
            }
            if(!options.contains(LoadingOption.CONSTRAINTS_LAZY_LOADING)) {
                preloadConstraints(pojoOrEnums);
            }
        }
        List<Type> enumTypes = NncUtils.filter(types, Type::isEnum);
        if(NncUtils.isNotEmpty(enumTypes) && !options.contains(LoadingOption.ENUM_CONSTANTS_LAZY_LOADING)) {
            preloadEnumConstants(enumTypes);
        }
        return types;
    }

    private boolean hasFieldsOrOptions(TypeCategory category) {
        return category == TypeCategory.ENUM || category == TypeCategory.CLASS;
    }

    public List<Field> getFieldsLoadingList(Type type) {
        return new LoadingList<>(() -> loadFields(List.of(type)));
    }

    public List<ConstraintRT<?>> getConstraintsLoadingList(Type type) {
        return new LoadingList<>(() -> loadConstraints(List.of(type)));
    }


    public List<EnumConstant> getEnumConstantsLoadingList(Type type) {
        return new LoadingList<>(() -> loadEnumConstants(List.of(type)));
    }

//    public List<EnumConstant> getChoiceOptionsLoadingList(Type type) {
//        return new LoadingList<>(() -> loadChoiceOptions(List.of(type)));
//    }

    private List<Field> loadFields(List<Type> types) {
        if(NncUtils.isEmpty(types)) {
            return List.of();
        }
        EntityContext context = types.get(0).getContext();
        Map<Long, Type> typeMap = NncUtils.toMap(types, Entity::getId);
        List<FieldPO> fieldPOs = NncUtils.splitAndMerge(
                types,
                Type::isStandard,
                this::loadStdFieldPOs,
                this::loadCustomFieldPOs
        );
        return NncUtils.map(
                fieldPOs,
                fieldPO -> new Field(fieldPO, typeMap.get(fieldPO.getDeclaringTypeId()), context.getTypeRef(fieldPO.getTypeId()))
        );
    }

    private List<FieldPO> loadStdFieldPOs(List<Type> types) {
        return NncUtils.flatMap(types, t -> StdTypeManager.getFieldPOs(t.getId()));
    }

    private List<FieldPO> loadCustomFieldPOs(List<Type> types) {
        if(NncUtils.isEmpty(types)) {
            return List.of();
        }
        return fieldMapper.selectByDeclaringTypeIds(types.get(0).getTenantId(), NncUtils.map(types, Entity::getId));
    }

    private List<EnumConstant> loadEnumConstants(List<Type> types) {
        types = NncUtils.filter(types, Type::isEnum);
        if(NncUtils.isEmpty(types)) {
            return List.of();
        }
        EntityContext context = types.get(0).getContext();
        return instanceEntityStore.getByTypeIds(EnumConstant.class, NncUtils.map(types, Entity::getId), context);
    }

    private List<InstancePO> loadChoiceOptionPOs(List<Long> typeIds, EntityContext context) {
        if(NncUtils.isEmpty(typeIds)) {
            return List.of();
        }
        return instanceMapper.selectByTypeIds(
                context.getTenantId(),
                typeIds,
                0, MAX_NUM_OPTIONS * typeIds.size()
        );
    }

//    private List<EnumConstant> loadChoiceOptions(List<Type> types) {
//        types = NncUtils.filter(types, Type::isEnum);
//        if(NncUtils.isEmpty(types)) {
//            return List.of();
//        }
//        EntityContext context = types.get(0).getContext();
//        Map<Long, Type> typeMap = NncUtils.toMap(types, Entity::getId);
//        List<InstancePO> instancePOs = instanceMapper.selectByTypeIds(
//                context.getTenantId(),
//                NncUtils.map(types, Entity::getId),
//                0, MAX_NUM_OPTIONS * types.size()
//        );
//        return NncUtils.map(instancePOs, instancePO -> new EnumConstant(instancePO, typeMap.get(instancePO.typeId())));
//    }

    public long count(TypeQuery query) {
        return typeMapper.count(query);
    }

    public List<Type> query(TypeQuery query, EntityContext context) {
        List<TypePO> poList = typeMapper.query(query);
        if(NncUtils.isEmpty(poList)) {
            return List.of();
        }
        List<Type> types = NncUtils.map(
                poList,
                po -> new Type(po, context)
        );
        preloadFields(types, context);
        return types;
    }

    @Override
    public List<Type> batchGet(Collection<Long> ids, EntityContext context, Set<LoadingOption> options) {
        if(NncUtils.isEmpty(ids)) {
            return List.of();
        }
        List<TypePO> typePOs = NncUtils.splitAndMerge(
                ids,
                StdTypeManager::isStandardTypeId,
                this::loadStdTypePOs,
                this::loadCustomTypePOs
        );
        return createFromPOs(typePOs, context, options);
    }

    private List<TypePO> loadStdTypePOs(List<Long> ids) {
        return NncUtils.map(ids, this::loadStdTypePO);
    }

    private TypePO loadStdTypePO(long id) {
        return StdTypeManager.getTypePO(id);
    }

    private List<TypePO> loadCustomTypePOs(List<Long> ids) {
        return typeMapper.selectByIds(ids);
    }

    private void preloadFields(List<Type> types, EntityContext context) {
        if(NncUtils.isEmpty(types)) {
            return;
        }
        List<Field> fields = loadFields(types);
        Map<Long, List<Field>> fieldMap = NncUtils.toMultiMap(fields, f -> f.getDeclaringType().getId());
        Set<Long> fieldTypeIds = NncUtils.mapUnique(fields, f -> f.getType().getId());
        context.batchGet(Type.class, fieldTypeIds,
                LoadingOption.FIELDS_LAZY_LOADING,
                LoadingOption.CONSTRAINTS_LAZY_LOADING,
                LoadingOption.ENUM_CONSTANTS_LAZY_LOADING
        );
        for (Type type : types) {
            type.preloadFields(fieldMap.computeIfAbsent(type.getId(), k -> new ArrayList<>()));
        }
    }

    private void preloadConstraints(List<Type> types) {
        if(NncUtils.isEmpty(types)) {
            return;
        }
        List<ConstraintRT<?>> constraints = loadConstraints(types);
        Map<Long, List<ConstraintRT<?>>> constraintMap = NncUtils.toMultiMap(constraints, c -> c.getType().getId());
        for (Type type : types) {
            type.preloadConstraints(constraintMap.computeIfAbsent(type.getId(), k -> new ArrayList<>()));
        }
    }

    private void preloadEnumConstants(List<Type> types) {
        if(NncUtils.isEmpty(types)) {
            return;
        }
        List<EnumConstant> enumConstants = loadEnumConstants(types);
        Map<Long, List<EnumConstant>> enumConstantMap =
                NncUtils.toMultiMap(enumConstants, c -> c.getType().getId());
        for (Type type : types) {
            type.preloadEnumConstants(enumConstantMap.computeIfAbsent(type.getId(), k -> new ArrayList<>()));
        }
    }

    public List<ConstraintRT<?>> loadConstraints(List<Type> types) {
        Map<Long, Type> typeMap = NncUtils.toEntityMap(types);
        List<ConstraintPO> constraintPOs = NncUtils.splitAndMerge(
                types,
                Type::isStandard,
                this::loadStdConstraintsPOs,
                this::loadCustomConstraintsPOs
        );
        return NncUtils.map(
                constraintPOs,
                c -> ConstraintFactory.createFromPO(c, typeMap.get(c.getTypeId()))
        );
    }

    private List<ConstraintPO> loadStdConstraintsPOs(List<Type> types) {
        return NncUtils.flatMap(types, t -> StdTypeManager.getConstraintPOs(t.getId()));
    }


    private List<ConstraintPO> loadCustomConstraintsPOs(List<Type> types) {
        return constraintMapper.selectByTypeIds(NncUtils.mapUnique(types, Entity::getId));
    }

    @Override
    public void batchInsert(List<Type> entities) {
        if(NncUtils.isNotEmpty(entities)) {
            typeMapper.batchInsert(NncUtils.map(entities, Type::toPO));
//            List<EnumConstant> choiceOptions = NncUtils.flatMap(entities, Type::getEnumConstants);
//            if(NncUtils.isNotEmpty(choiceOptions)) {
//                instanceMapper.batchInsert(NncUtils.map(choiceOptions, EnumConstant::toPO));
//            }
        }
    }

    @Override
    public int batchUpdate(List<Type> entities) {
        if(NncUtils.isNotEmpty(entities)) {
//            long tenantId = entities.get(0).getTenantId();
            return typeMapper.batchUpdate(NncUtils.map(entities, Type::toPO));
//            List<Type> enumTypes = NncUtils.filter(entities, Type::isEnum);
//            if(NncUtils.isNotEmpty(enumTypes)) {
//                List<Long> enumIds = NncUtils.map(enumTypes, Entity::getId);
//                List<EnumConstant> options = NncUtils.flatMap(enumTypes, Type::getEnumConstants);
//                List<InstancePO> optionPOs = NncUtils.map(options, EnumConstant::toPO);
//                List<InstancePO> oldOptionPOs =
//                        instanceMapper.selectByModelIds(tenantId, enumIds, 0, MAX_NUM_OPTIONS * enumTypes.size());
//                ChangeList<InstancePO> optionChange = ChangeList.build(oldOptionPOs, optionPOs, InstancePO::id);
//                if(NncUtils.isNotEmpty(optionChange.inserts())) {
//                    instanceMapper.batchInsert(optionChange.inserts());
//                }
//                if(NncUtils.isNotEmpty(optionChange.updates())) {
//                    instanceMapper.batchUpdate(optionChange.updates());
//                }
//                if(NncUtils.isNotEmpty(optionChange.deletes())) {
//                    instanceMapper.batchDelete(
//                            tenantId,
//                            NncUtils.map(optionChange.deletes(), InstancePO::nextVersion)
//                    );
//                }
//            }
//            return affected;
        }
        else {
            return 0;
        }
    }

    @Override
    public void batchDelete(List<Type> types) {
        typeMapper.batchDelete(NncUtils.map(types, Entity::getId));
    }

    @Override
    public Class<Type> getEntityType() {
        return Type.class;
    }

}
