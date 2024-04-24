package tech.metavm.object.type;

import tech.metavm.common.BaseDTO;
import tech.metavm.entity.IEntityContext;
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

    private SaveTypeBatch(IEntityContext context, List<? extends TypeDefDTO> typeDefDTOs, List<FlowDTO> functions) {
        this.context = context;
        for (var typeDefDTO : typeDefDTOs) {
            typeDefMap.put(typeDefDTO.id(), typeDefDTO);
            if (typeDefDTO instanceof TypeDTO typeDTO) {
                var classParam = typeDTO.getClassParam();
                if (classParam.flows() != null) {
                    for (FlowDTO flowDTO : classParam.flows())
                        flowMap.put(flowDTO.id(), flowDTO);
                }
            }
        }
        for (FlowDTO function : functions)
            functionMap.put(function.id(), function);
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

    public List<Type> getTypes() {
        return NncUtils.map(typeDefMap.keySet(), id -> context.getType(Id.parse(id)));
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
        var existing = context.getTypeDef(id);
        if(existing != null)
            return existing;
        var typeDefDTO = NncUtils.requireNonNull(typeDefMap.get(id),
                "TypeDef '" + id + "' not available");
        return Types.saveTypeDef(typeDefDTO, ResolutionStage.INIT, this);
    }

    public Klass getKlass(String id) {
        return (Klass) getTypeDef(id);
    }

    public TypeVariable getTypeVariable(String id) {
        return (TypeVariable) getTypeDef(id);
    }

    /*public Type get(String id) {
        var existing = context.getType(id);
        if (existing != null)
            return existing;
        var typeDTO = NncUtils.requireNonNull(typeMap.get(id),
                "Type '" + id + "' not available");
        return Types.saveType(typeDTO, ResolutionStage.INIT, this);
    }*/

//    public ClassType getClassType(String id) {
//        return (ClassType) get(id);
//    }

    public CapturedTypeVariable getCapturedTypeVariable(String id) {
        return (CapturedTypeVariable) getTypeDef(id);
    }

    public List<TypeDTO> getTypeDTOs() {
        return NncUtils.filterByType(typeDefMap.values(), TypeDTO.class);
    }

    public Set<String> noDependencies(TypeDefDTO typeDefDTO) {
        return Set.of();
    }

    public Set<String> initDependencies(TypeDefDTO typeDefDTO) {
        var dependencies = new HashSet<String>();
        if (Objects.requireNonNull(typeDefDTO) instanceof TypeDTO typeDTO) {
            var classTypeParam = typeDTO.getClassParam();
            if (classTypeParam.typeParameterIds() != null)
                dependencies.addAll(classTypeParam.typeParameterIds());
        }
        return dependencies;
    }

    private Set<String> declarationDependencies(TypeDefDTO typeDefDTO) {
        var dependencies = new HashSet<String>();
        if (typeDefDTO instanceof TypeDTO typeDTO) {
            var classParam = typeDTO.getClassParam();
            if (classParam.superClassId() != null)
                dependencies.add(classParam.superClassId());
            if (classParam.interfaceIds() != null)
                dependencies.addAll(classParam.interfaceIds());
        }
        return dependencies;
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

    /*public void saveParameterizedFlows(Klass type, ResolutionStage stage) {
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
        var typeDTO = getTypeDTONotNull(id);
        if(typeDTO.param() instanceof ClassTypeParam classParam) {
            return NncUtils.flatMap(
                    classParam.flows(),
                    f -> parameterizedFlowMap.getOrDefault(f.id(), List.of())
            );
        }
        else
            return List.of();
    }*/

    public @Nullable ObjectMappingDTO getMappingDTO(FieldsObjectMapping mapping) {
        var typeDTO = getTypeDTO(mapping.getSourceType().getStringId());
        if (typeDTO == null)
            return null;
        else
            return NncUtils.find(typeDTO.getClassParam().mappings(), m -> m.name().equals(mapping.getName()));
    }

    @Override
    public @Nullable TypeDTO getTypeDTO(String id) {
        return (TypeDTO) getTypeDefDTO(id);
    }

    public TypeDefDTO getTypeDefDTO(String id) {
        return typeDefMap.get(id);
    }

    public TypeDTO getTypeDTONotNull(String id) {
        return Objects.requireNonNull(getTypeDTO(id), () -> "Can not find typeDTO with id '" + id + "'");
    }

    public static SaveTypeBatch empty(IEntityContext context) {
        return new SaveTypeBatch(context, List.of(), List.of());
    }

}
