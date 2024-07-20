package org.metavm.object.type;

import org.metavm.common.ErrorCode;
import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.ddl.Commit;
import org.metavm.entity.Entity;
import org.metavm.entity.IEntityContext;
import org.metavm.flow.Method;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.flow.rest.MethodParam;
import org.metavm.object.instance.core.Id;
import org.metavm.object.instance.core.WAL;
import org.metavm.object.type.rest.dto.*;
import org.metavm.object.view.FieldsObjectMapping;
import org.metavm.object.view.rest.dto.ObjectMappingDTO;
import org.metavm.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class SaveTypeBatch implements DTOProvider, TypeDefProvider {

    private static final Logger logger = LoggerFactory.getLogger(SaveTypeBatch.class);

    public static SaveTypeBatch create(IEntityContext context,
                                       List<? extends TypeDefDTO> typeDefDTOs,
                                       List<FlowDTO> functions) {
        var batch = new SaveTypeBatch(context, typeDefDTOs, functions);
        batch.execute();
        return batch;
    }

    private final IEntityContext context;
    // Order matters! Don't use HashMap
    private final LinkedHashMap<String, TypeDefDTO> typeDefMap = new LinkedHashMap<>();
    private final Map<String, FlowDTO> functionMap = new HashMap<>();
    private final Map<String, FlowDTO> flowMap = new HashMap<>();
    private final Set<Field> newFields = new HashSet<>();
    private final Set<Field> typeChangedFields = new HashSet<>();
    private final Set<Field> toChildFields = new HashSet<>();
    private final Set<Field> toNonChildFields = new HashSet<>();
    private final Set<Klass> changingSuperKlasses = new HashSet<>();
    private final Set<Klass> toValueKlasses = new HashSet<>();

    private SaveTypeBatch(IEntityContext context, List<? extends TypeDefDTO> typeDefDTOs, List<FlowDTO> functions) {
        this.context = context;
        for (var typeDefDTO : typeDefDTOs) {
            typeDefMap.put(typeDefDTO.id(), typeDefDTO);
            if (typeDefDTO instanceof KlassDTO klassDTO) {
                if (klassDTO.flows() != null) {
                    for (FlowDTO flowDTO : klassDTO.flows())
                        flowMap.put(flowDTO.id(), flowDTO);
                }
            }
        }
        for (FlowDTO function : functions)
            functionMap.put(function.id(), function);
    }

    public void addNewField(Field field) {
        newFields.add(field);
    }

    public void addTypeChangedField(Field field) {
        typeChangedFields.add(field);
    }

    public void addToChildField(Field field) {
        toChildFields.add(field);
    }

    public void addToNonChildField(Field field) {
        toNonChildFields.add(field);
    }

    public void addChangingSuperKlass(Klass klass) {
        changingSuperKlasses.add(klass);
    }

    public void addToValueKlass(Klass klass) {
        toValueKlasses.add(klass);
    }

    private record SaveStage(ResolutionStage stage, Function<TypeDefDTO, Set<String>> getDependencies) {
        List<TypeDefDTO> sort(Collection<TypeDefDTO> typeDefDTOs) {
            return Sorter.sort(typeDefDTOs, getDependencies);
        }
    }

    private void execute() {
        List<SaveStage> stages = List.of(
                new SaveStage(ResolutionStage.INIT, this::initDependencies),
                new SaveStage(ResolutionStage.SIGNATURE, this::noDependencies),
                new SaveStage(ResolutionStage.DECLARATION, this::declarationDependencies),
                new SaveStage(ResolutionStage.DEFINITION, this::noDependencies),
                new SaveStage(ResolutionStage.MAPPING_DEFINITION, this::noDependencies)
        );
        for (var stage : stages) {
            for (var typeDTO : stage.sort(typeDefMap.values()))
                stage.stage.saveTypeDef(typeDTO, this);
            for (var function : functionMap.values())
                stage.stage.saveFunction(function, this);
        }
    }

    public List<TypeDef> getTypes() {
        return NncUtils.map(typeDefMap.keySet(), context::getTypeDef);
    }

    public IEntityContext getContext() {
        return context;
    }

    public Method getMethod(String id) {
        var existing = context.getMethod(Id.parse(id));
        if (existing != null) {
            return existing;
        } else {
            var flowDTO = NncUtils.requireNonNull(flowMap.get(id), "Flow '" + id + " not available");
            var param = (MethodParam) flowDTO.param();
            var declaringType = getKlass(param.declaringTypeId());
            return NncUtils.requireNonNull(declaringType.findSelfMethod(f -> f.getStringId().equals(id)));
        }
    }

    public TypeDef getTypeDef(String id) {
        return getTypeDef(Id.parse(id));
    }

    @Override
    public TypeDef getTypeDef(Id id) {
        var existing = context.getTypeDef(id);
        if (existing != null)
            return existing;
        var typeDefDTO = NncUtils.requireNonNull(typeDefMap.get(id.toString()),
                "TypeDef '" + id + "' not available");
        return Types.saveTypeDef(typeDefDTO, ResolutionStage.INIT, this);
    }

    public Klass getKlass(String id) {
        return (Klass) getTypeDef(id);
    }

    public TypeVariable getTypeVariable(String id) {
        return (TypeVariable) getTypeDef(id);
    }

    public CapturedTypeVariable getCapturedTypeVariable(String id) {
        return (CapturedTypeVariable) getTypeDef(id);
    }

    public List<KlassDTO> getTypeDTOs() {
        return NncUtils.filterByType(typeDefMap.values(), KlassDTO.class);
    }

    public Set<String> noDependencies(TypeDefDTO typeDefDTO) {
        return Set.of();
    }

    public Set<String> initDependencies(TypeDefDTO typeDefDTO) {
        var dependencies = new HashSet<String>();
        if (Objects.requireNonNull(typeDefDTO) instanceof KlassDTO klassDTO) {
            if (klassDTO.typeParameterIds() != null)
                dependencies.addAll(klassDTO.typeParameterIds());
        }
        return dependencies;
    }

    private Set<String> declarationDependencies(TypeDefDTO typeDefDTO) {
        var dependencies = new HashSet<String>();
        if (typeDefDTO instanceof KlassDTO klassDTO) {
            if (klassDTO.superType() != null)
                dependencies.add(getKlassId(klassDTO.superType()));
            if (klassDTO.interfaces() != null)
                klassDTO.interfaces().forEach(t -> dependencies.add(getKlassId(t)));
        }
        return dependencies;
    }

    private String getKlassId(String typeExpression) {
        var typeKey = TypeKey.fromExpression(typeExpression);
        return switch (typeKey) {
            case ClassTypeKey ctKey -> ctKey.id().toString();
            case TaggedClassTypeKey tctKey -> tctKey.id().toString();
            case ParameterizedTypeKey ptKey -> ptKey.templateId().toString();
            case null, default -> throw new InternalException("Unexpected type key: " + typeKey);
        };
    }

    private static class Sorter {

        public static List<TypeDefDTO> sort(Collection<TypeDefDTO> typeDefDTOs,
                                            Function<TypeDefDTO, Set<String>> getDependencies) {
            var sorter = new Sorter(typeDefDTOs, getDependencies);
            return sorter.result;
        }

        private final IdentitySet<BaseDTO> visited = new IdentitySet<>();
        private final IdentitySet<BaseDTO> visiting = new IdentitySet<>();
        private final List<TypeDefDTO> result = new ArrayList<>();
        private final Map<String, TypeDefDTO> map = new HashMap<>();
        private final Function<TypeDefDTO, Set<String>> getDependencies;

        public Sorter(Collection<TypeDefDTO> typeDefDTOs, Function<TypeDefDTO, Set<String>> getDependencies) {
            this.getDependencies = getDependencies;
            for (var typeDTO : typeDefDTOs) {
                map.put(typeDTO.id(), typeDTO);
            }
            for (var typeDefDTO : map.values()) {
                visit(typeDefDTO);
            }
        }

        public void visitId(String id) {
            if (id == null || id.isEmpty()) {
                return;
            }
            var baseDTO = map.get(id);
            if (baseDTO != null) {
                visit(baseDTO);
            }
        }

        private void visit(TypeDefDTO typeDefDTO) {
            if (visiting.contains(typeDefDTO)) {
                throw new InternalException("Circular reference");
            }
            if (visited.contains(typeDefDTO)) {
                return;
            }
            visiting.add(typeDefDTO);
            getDependencies(typeDefDTO).forEach(this::visitId);
            result.add(typeDefDTO);
            visiting.remove(typeDefDTO);
            visited.add(typeDefDTO);
        }

        private Set<String> getDependencies(TypeDefDTO typeDefDTO) {
            return getDependencies.apply(typeDefDTO);
        }

    }

    public @Nullable ObjectMappingDTO getMappingDTO(FieldsObjectMapping mapping) {
        var typeDTO = getTypeDTO(mapping.getSourceType().getStringId());
        if (typeDTO == null)
            return null;
        else
            return NncUtils.find(typeDTO.mappings(), m -> m.name().equals(mapping.getName()));
    }

    @Override
    public @Nullable KlassDTO getTypeDTO(String id) {
        return (KlassDTO) getTypeDefDTO(id);
    }

    public TypeDefDTO getTypeDefDTO(String id) {
        return typeDefMap.get(id);
    }

    public KlassDTO getTypeDTONotNull(String id) {
        return Objects.requireNonNull(getTypeDTO(id), () -> "Can not find typeDTO with id '" + id + "'");
    }

    public Commit buildCommit(WAL wal) {
        checkForDDL();
        return new Commit(
                wal,
                new BatchSaveRequest(
                        new ArrayList<>(typeDefMap.values()),
                        new ArrayList<>(functionMap.values()),
                        true
                ),
                NncUtils.map(newFields, Entity::getStringId),
                NncUtils.map(typeChangedFields, Entity::getStringId),
                NncUtils.map(toChildFields, Entity::getStringId),
                NncUtils.map(changingSuperKlasses, Entity::getStringId),
                NncUtils.map(toValueKlasses, Entity::getStringId)
        );
    }

    private void checkForDDL() {
        for (Field field : newFields) {
            if (Instances.findFieldInitializer(field) == null && Instances.getDefaultValue(field) == null)
                throw new BusinessException(ErrorCode.MISSING_FIELD_INITIALIZER, field.getQualifiedName());
        }
        for (Field field : typeChangedFields) {
            if (Instances.findTypeConverter(field) == null)
                throw new BusinessException(ErrorCode.MISSING_TYPE_CONVERTER, field.getQualifiedName());
        }
        for (var klass : changingSuperKlasses) {
            if (Instances.findSuperInitializer(klass) == null)
                throw new BusinessException(ErrorCode.MISSING_SUPER_INITIALIZER, klass.getCode());
        }
    }

    public static SaveTypeBatch empty(IEntityContext context) {
        return new SaveTypeBatch(context, List.of(), List.of());
    }

}
