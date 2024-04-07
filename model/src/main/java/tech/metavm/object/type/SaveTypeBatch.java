package tech.metavm.object.type;

import tech.metavm.common.BaseDTO;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flows;
import tech.metavm.flow.Method;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.MethodParam;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.rest.dto.*;
import tech.metavm.object.view.FieldsObjectMapping;
import tech.metavm.object.view.rest.dto.ObjectMappingDTO;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class SaveTypeBatch implements DTOProvider {

    public static SaveTypeBatch create(IEntityContext context, List<TypeDTO> types, List<FlowDTO> functions) {
        return create(context, types, functions, List.of());
    }

    public static SaveTypeBatch create(IEntityContext context,
                                       List<TypeDTO> types,
                                       List<FlowDTO> functions,
                                       List<ParameterizedFlowDTO> pFlows) {
        var batch = new SaveTypeBatch(context, types, functions, pFlows);
        batch.execute();
        return batch;
    }

    private final IEntityContext context;
    // Order matters! Don't use HashMap
    private final LinkedHashMap<String, TypeDTO> typeMap = new LinkedHashMap<>();
    private final Map<String, FlowDTO> functionMap = new HashMap<>();
    private final Map<TypeKey, TypeDTO> typeKey2TypeDTO = new HashMap<>();
    private final Map<String, FlowDTO> flowMap = new HashMap<>();
    private final Map<String, List<ParameterizedFlowDTO>> parameterizedFlowMap = new HashMap<>();

    private SaveTypeBatch(IEntityContext context, List<TypeDTO> types,
                          List<FlowDTO> functions, List<ParameterizedFlowDTO> parameterizedFlows) {
        this.context = context;
        for (TypeDTO typeDTO : types) {
            typeMap.put(typeDTO.id(), typeDTO);
            var typeKey = typeDTO.getTypeKey();
            if (typeKey != null)
                typeKey2TypeDTO.put(typeKey, typeDTO);
            if (typeDTO.param() instanceof ClassTypeParam classParam) {
                if (classParam.flows() != null) {
                    for (FlowDTO flowDTO : classParam.flows())
                        flowMap.put(flowDTO.id(), flowDTO);
                }
            }
        }
        for (FlowDTO function : functions)
            functionMap.put(function.id(), function);
        for (ParameterizedFlowDTO pFlowDTO : parameterizedFlows) {
            parameterizedFlowMap.computeIfAbsent(pFlowDTO.getTemplateId(), k -> new ArrayList<>()).add(pFlowDTO);
        }
    }

    private record SaveStage(ResolutionStage stage, Function<TypeDTO, Set<String>> getDependencies) {
        List<TypeDTO> sort(Collection<TypeDTO> typeDTOs) {
            return Sorter.sort(typeDTOs, getDependencies);
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
            for (var typeDTO : stage.sort(typeMap.values()))
                stage.stage.saveType(typeDTO, this);
            for (var function : functionMap.values())
                stage.stage.saveFunction(function, this);
            for (var flowDTOs : parameterizedFlowMap.values())
                flowDTOs.forEach(flowDTO -> stage.stage.saveParameterizedFlow(flowDTO, this));
        }
    }

    public List<Type> getTypes() {
        return NncUtils.map(typeMap.keySet(), id -> context.getType(Id.parse(id)));
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
            var declaringType = getClassType(param.declaringTypeId());
            return NncUtils.requireNonNull(declaringType.findSelfMethod(f -> f.getStringId().equals(id)));
        }
    }

    public Type get(String id) {
        var existing = context.getType(id);
        if (existing != null)
            return existing;
        var typeDTO = NncUtils.requireNonNull(typeMap.get(id),
                "Type '" + id + "' not available");
        return Types.saveType(typeDTO, ResolutionStage.INIT, this);
    }

    public ClassType getClassType(String id) {
        return (ClassType) get(id);
    }

    public TypeVariable getTypeVariable(String id) {
        return (TypeVariable) get(id);
    }

    public CapturedType getCapturedType(String id) {
        return (CapturedType) get(id);
    }

    public List<TypeDTO> getClassTypeDTOs() {
        return NncUtils.filter(this.typeMap.values(), t -> t.param() instanceof ClassTypeParam);
    }

    public Set<String> noDependencies(TypeDTO typeDTO) {
        return Set.of();
    }

    public Set<String> initDependencies(TypeDTO typeDTO) {
        var dependencies = new HashSet<String>();
        switch (typeDTO.param()) {
            case ClassTypeParam classTypeParam -> {
                if(classTypeParam.typeParameterIds() != null)
                    dependencies.addAll(classTypeParam.typeParameterIds());
            }
            case PTypeDTO pTypeDTO -> dependencies.add(pTypeDTO.getTemplateId());
            case ArrayTypeParam arrayTypeParam -> dependencies.add(arrayTypeParam.elementTypeId());
            case UncertainTypeParam uncertainTypeParam -> {
                dependencies.add(uncertainTypeParam.lowerBoundId());
                dependencies.add(uncertainTypeParam.upperBoundId());
            }
            case UnionTypeParam unionTypeParam -> dependencies.addAll(unionTypeParam.memberIds());
            case FunctionTypeParam functionTypeParam -> {
                dependencies.addAll(functionTypeParam.parameterTypeIds());
                dependencies.add(functionTypeParam.returnTypeId());
            }
            default -> {
            }
        }
        return dependencies;
    }

    private Set<String> declarationDependencies(TypeDTO typeDTO) {
        var dependencies = new HashSet<String>();
        if (typeDTO.param() instanceof ClassTypeParam classParam) {
            if (classParam.superClassId() != null)
                dependencies.add(classParam.superClassId());
            if (classParam.interfaceIds() != null)
                dependencies.addAll(classParam.interfaceIds());
        }
        return dependencies;
    }

    private static class Sorter {

        public static List<TypeDTO> sort(Collection<TypeDTO> typeDTOs,
                                         Function<TypeDTO, Set<String>> getDependencies) {
            var sorter = new Sorter(typeDTOs, getDependencies);
            return sorter.result;
        }

        private final IdentitySet<BaseDTO> visited = new IdentitySet<>();
        private final IdentitySet<BaseDTO> visiting = new IdentitySet<>();
        private final List<TypeDTO> result = new ArrayList<>();
        private final Map<String, TypeDTO> map = new HashMap<>();
        private final Function<TypeDTO, Set<String>> getDependencies;

        public Sorter(Collection<TypeDTO> typeDTOs, Function<TypeDTO, Set<String>> getDependencies) {
            this.getDependencies = getDependencies;
            for (TypeDTO typeDTO : typeDTOs) {
                map.put(typeDTO.id(), typeDTO);
            }
            for (TypeDTO typeDTO : map.values()) {
                visit(typeDTO);
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

        private void visit(TypeDTO typeDTO) {
            if (visiting.contains(typeDTO)) {
                throw new InternalException("Circular reference");
            }
            if (visited.contains(typeDTO)) {
                return;
            }
            visiting.add(typeDTO);
            getDependencies(typeDTO).forEach(this::visitId);
            result.add(typeDTO);
            visiting.remove(typeDTO);
            visited.add(typeDTO);
        }

        private Set<String> getDependencies(TypeDTO typeDTO) {
            return getDependencies.apply(typeDTO);
        }

    }

    @Override
    public @Nullable Long getTmpId(TypeKey typeKey) {
        return NncUtils.get(getTypeDTO(typeKey), TypeDTO::tmpId);
    }

    public @Nullable TypeDTO getTypeDTO(TypeKey typeKey) {
        return typeKey2TypeDTO.get(typeKey);
    }

    @Override
    public @Nullable PTypeDTO getPTypeDTO(String templateId, List<String> typeArgumentIds) {
        var typeDTO = typeKey2TypeDTO.get(new ParameterizedTypeKey(templateId, typeArgumentIds));
        return NncUtils.get(typeDTO, TypeDTO::getPTypeDTO);
    }

    public @Nullable FlowDTO getFlowDTO(Method method) {
        var typeDTO = getTypeDTO(method.getDeclaringType().getStringId());
        return NncUtils.get(typeDTO, t -> t.getClassParam().findFlowBySignature(method.getSignature()));
    }

    @Override
    public @Nullable ParameterizedFlowDTO getParameterizedFlowDTO(String templateId, List<String> typeArgumentIds) {
        var pFlows = parameterizedFlowMap.get(templateId);
        if(pFlows == null)
            return null;
        return NncUtils.find(pFlows, pFlow -> pFlow.getTypeArgumentIds().equals(typeArgumentIds));
    }

    public void saveParameterizedFlows(ClassType type, ResolutionStage stage) {
        var pFlowDTOs = getPFlowsByDeclaringType(type.getStringId());
        for (ParameterizedFlowDTO parameterizedFlowDTO : pFlowDTOs) {
            Flows.getParameterizedFlow(
                    context.getFlow(Id.parse(parameterizedFlowDTO.getTemplateId())),
                    NncUtils.map(parameterizedFlowDTO.getTypeArgumentIds(), id -> context.getType(Id.parse(id))),
                    stage,
                    this
            );
        }
    }

    private List<ParameterizedFlowDTO> getPFlowsByDeclaringType(String id) {
        var typeDTO = getTypeDTO(id);
        if(typeDTO.param() instanceof ClassTypeParam classParam) {
            return NncUtils.flatMap(
                    classParam.flows(),
                    f -> parameterizedFlowMap.getOrDefault(f.id(), List.of())
            );
        }
        else
            return List.of();
    }

    public @Nullable ObjectMappingDTO getMappingDTO(FieldsObjectMapping mapping) {
        var typeDTO = getTypeDTO(mapping.getSourceType().getStringId());
        if (typeDTO == null)
            return null;
        else
            return NncUtils.find(typeDTO.getClassParam().mappings(), m -> m.name().equals(mapping.getName()));
    }

    @Override
    public @Nullable TypeDTO getTypeDTO(String id) {
        return typeMap.get(id);
    }

    public static SaveTypeBatch empty(IEntityContext context) {
        return new SaveTypeBatch(context, List.of(), List.of(), List.of());
    }

}
