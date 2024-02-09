package tech.metavm.object.type;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flows;
import tech.metavm.flow.Method;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.flow.rest.MethodParam;
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
    private final LinkedHashMap<RefDTO, TypeDTO> typeMap = new LinkedHashMap<>();
    private final Map<RefDTO, FlowDTO> functionMap = new HashMap<>();
    private final Map<TypeKey, TypeDTO> typeKey2TypeDTO = new HashMap<>();
    private final Map<RefDTO, FlowDTO> flowMap = new HashMap<>();
    private final Map<RefDTO, List<ParameterizedFlowDTO>> parameterizedFlowMap = new HashMap<>();

    private SaveTypeBatch(IEntityContext context, List<TypeDTO> types,
                          List<FlowDTO> functions, List<ParameterizedFlowDTO> parameterizedFlows) {
        this.context = context;
        for (TypeDTO typeDTO : types) {
            typeMap.put(typeDTO.getRef(), typeDTO);
            var typeKey = typeDTO.getTypeKey();
            if (typeKey != null)
                typeKey2TypeDTO.put(typeKey, typeDTO);
            if (typeDTO.param() instanceof ClassTypeParam classParam) {
                if (classParam.flows() != null) {
                    for (FlowDTO flowDTO : classParam.flows())
                        flowMap.put(flowDTO.getRef(), flowDTO);
                }
            }
        }
        for (FlowDTO function : functions)
            functionMap.put(function.getRef(), function);
        for (ParameterizedFlowDTO pFlowDTO : parameterizedFlows) {
            parameterizedFlowMap.computeIfAbsent(pFlowDTO.getTemplateRef(), k -> new ArrayList<>()).add(pFlowDTO);
        }
    }

    private record SaveStage(ResolutionStage stage, Function<TypeDTO, Set<RefDTO>> getDependencies) {
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
        return NncUtils.map(typeMap.keySet(), context::getType);
    }

    public IEntityContext getContext() {
        return context;
    }

    public Method getMethod(RefDTO ref) {
        var existing = context.getMethod(ref);
        if (existing != null) {
            return existing;
        } else {
            var flowDTO = NncUtils.requireNonNull(flowMap.get(ref), "Flow '" + ref + " not available");
            var param = (MethodParam) flowDTO.param();
            var declaringType = getClassType(param.declaringTypeRef());
            return NncUtils.requireNonNull(declaringType.findSelfMethod(f -> f.getRef().equals(ref)));
        }
    }

    public Type get(RefDTO ref) {
        var existing = context.getType(ref);
        if (existing != null)
            return existing;
        var typeDTO = NncUtils.requireNonNull(typeMap.get(ref),
                "Type '" + ref + "' not available");
        return Types.saveType(typeDTO, ResolutionStage.INIT, this);
    }

    public ClassType getClassType(RefDTO ref) {
        return (ClassType) get(ref);
    }

    public TypeVariable getTypeVariable(RefDTO ref) {
        return (TypeVariable) get(ref);
    }

    public List<TypeDTO> getClassTypeDTOs() {
        return NncUtils.filter(this.typeMap.values(), t -> t.param() instanceof ClassTypeParam);
    }

    public Set<RefDTO> noDependencies(TypeDTO typeDTO) {
        return Set.of();
    }

    public Set<RefDTO> initDependencies(TypeDTO typeDTO) {
        var dependencies = new HashSet<RefDTO>();
        switch (typeDTO.param()) {
            case ClassTypeParam classTypeParam -> {
                if(classTypeParam.typeParameterRefs() != null)
                    dependencies.addAll(classTypeParam.typeParameterRefs());
            }
            case PTypeDTO pTypeDTO -> dependencies.add(pTypeDTO.getTemplateRef());
            case ArrayTypeParam arrayTypeParam -> dependencies.add(arrayTypeParam.elementTypeRef());
            case UncertainTypeParam uncertainTypeParam -> {
                dependencies.add(uncertainTypeParam.lowerBoundRef());
                dependencies.add(uncertainTypeParam.upperBoundRef());
            }
            case UnionTypeParam unionTypeParam -> dependencies.addAll(unionTypeParam.memberRefs());
            case FunctionTypeParam functionTypeParam -> {
                dependencies.addAll(functionTypeParam.parameterTypeRefs());
                dependencies.add(functionTypeParam.returnTypeRef());
            }
            default -> {
            }
        }
        return dependencies;
    }

    private Set<RefDTO> declarationDependencies(TypeDTO typeDTO) {
        var dependencies = new HashSet<RefDTO>();
        if (typeDTO.param() instanceof ClassTypeParam classParam) {
            if (classParam.superClassRef() != null)
                dependencies.add(classParam.superClassRef());
            if (classParam.interfaceRefs() != null)
                dependencies.addAll(classParam.interfaceRefs());
        }
        return dependencies;
    }

    private static class Sorter {

        public static List<TypeDTO> sort(Collection<TypeDTO> typeDTOs,
                                         Function<TypeDTO, Set<RefDTO>> getDependencies) {
            var sorter = new Sorter(typeDTOs, getDependencies);
            return sorter.result;
        }

        private final IdentitySet<BaseDTO> visited = new IdentitySet<>();
        private final IdentitySet<BaseDTO> visiting = new IdentitySet<>();
        private final List<TypeDTO> result = new ArrayList<>();
        private final Map<RefDTO, TypeDTO> map = new HashMap<>();
        private final Function<TypeDTO, Set<RefDTO>> getDependencies;

        public Sorter(Collection<TypeDTO> typeDTOs, Function<TypeDTO, Set<RefDTO>> getDependencies) {
            this.getDependencies = getDependencies;
            for (TypeDTO typeDTO : typeDTOs) {
                map.put(typeDTO.getRef(), typeDTO);
            }
            for (TypeDTO typeDTO : map.values()) {
                visit(typeDTO);
            }
        }

        public void visitRef(RefDTO ref) {
            if (ref == null || ref.isEmpty()) {
                return;
            }
            var baseDTO = map.get(ref);
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
            getDependencies(typeDTO).forEach(this::visitRef);
            result.add(typeDTO);
            visiting.remove(typeDTO);
            visited.add(typeDTO);
        }

        private Set<RefDTO> getDependencies(TypeDTO typeDTO) {
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
    public @Nullable PTypeDTO getPTypeDTO(RefDTO templateRef, List<RefDTO> typeArgumentRefs) {
        var typeDTO = typeKey2TypeDTO.get(new ParameterizedTypeKey(templateRef, typeArgumentRefs));
        return NncUtils.get(typeDTO, TypeDTO::getPTypeDTO);
    }

    public @Nullable FlowDTO getFlowDTO(Method method) {
        var typeDTO = getTypeDTO(method.getDeclaringType().getRef());
        return NncUtils.get(typeDTO, t -> t.getClassParam().findFlowBySignature(method.getSignature()));
    }

    @Override
    public @Nullable ParameterizedFlowDTO getParameterizedFlowDTO(RefDTO templateRef, List<RefDTO> typeArgumentRefs) {
        var pFlows = parameterizedFlowMap.get(templateRef);
        if(pFlows == null)
            return null;
        return NncUtils.find(pFlows, pFlow -> pFlow.getTypeArgumentRefs().equals(typeArgumentRefs));
    }

    public void saveParameterizedFlows(ClassType type, ResolutionStage stage) {
        var pFlowDTOs = getPFlowsByDeclaringType(type.getRef());
        for (ParameterizedFlowDTO parameterizedFlowDTO : pFlowDTOs) {
            Flows.getParameterizedFlow(
                    context.getFlow(parameterizedFlowDTO.getTemplateRef()),
                    NncUtils.map(parameterizedFlowDTO.getTypeArgumentRefs(), context::getType),
                    stage,
                    this
            );
        }
    }

    private List<ParameterizedFlowDTO> getPFlowsByDeclaringType(RefDTO ref) {
        var typeDTO = getTypeDTO(ref);
        if(typeDTO.param() instanceof ClassTypeParam classParam) {
            return NncUtils.flatMap(
                    classParam.flows(),
                    f -> parameterizedFlowMap.getOrDefault(f.getRef(), List.of())
            );
        }
        else
            return List.of();
    }

    public @Nullable ObjectMappingDTO getMappingDTO(FieldsObjectMapping mapping) {
        var typeDTO = getTypeDTO(mapping.getSourceType().getRef());
        if (typeDTO == null)
            return null;
        else
            return NncUtils.find(typeDTO.getClassParam().mappings(), m -> m.name().equals(mapping.getName()));
    }

    @Override
    public @Nullable TypeDTO getTypeDTO(RefDTO ref) {
        return typeMap.get(ref);
    }

    public static SaveTypeBatch empty(IEntityContext context) {
        return new SaveTypeBatch(context, List.of(), List.of(), List.of());
    }

}
