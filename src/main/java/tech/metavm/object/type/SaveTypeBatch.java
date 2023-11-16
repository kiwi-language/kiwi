package tech.metavm.object.type;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.entity.IEntityContext;
import tech.metavm.flow.Flow;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.type.rest.dto.ClassTypeParam;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.type.rest.dto.TypeKey;
import tech.metavm.object.type.rest.dto.UncertainTypeParam;
import tech.metavm.util.IdentitySet;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

public class SaveTypeBatch {

    public static SaveTypeBatch create(IEntityContext context, List<TypeDTO> types) {
        var batch = new SaveTypeBatch(context, types);
        batch.execute();
        return batch;
    }

    private final IEntityContext context;
    // Order matters! Don't use HashMap
    private final Map<RefDTO, TypeDTO> map = new LinkedHashMap<>();
    private final Map<TypeKey, TypeDTO> typeKey2TypeDTO = new HashMap<>();
    private final Map<RefDTO, FlowDTO> flowMap = new HashMap<>();
    private final List<ClassType> newClasses = new ArrayList<>();
    private final List<Type> types = new ArrayList<>();

    private SaveTypeBatch(IEntityContext context, List<TypeDTO> types) {
        this.context = context;
        for (TypeDTO typeDTO : types) {
            map.put(typeDTO.getRef(), typeDTO);
            var typeKey = typeDTO.getTypeKey();
            if (typeKey != null)
                typeKey2TypeDTO.put(typeKey, typeDTO);
            if(typeDTO.param() instanceof ClassTypeParam classParam) {
                if(classParam.flows() != null) {
                    for (FlowDTO flowDTO : classParam.flows())
                        flowMap.put(flowDTO.getRef(), flowDTO);
                }
            }
        }
    }

    private record SaveStage(
            ResolutionStage stage,
            Function<TypeDTO, Set<RefDTO>> getDependencies
    ) {

        List<TypeDTO> sort(Collection<TypeDTO> typeDTOs) {
            return Sorter.sort(typeDTOs, getDependencies);
        }

    }

    private void execute() {
        List<SaveStage> stages = List.of(
                new SaveStage(ResolutionStage.INIT, this::initDependencies),
                new SaveStage(ResolutionStage.SIGNATURE, this::noDependencies),
                new SaveStage(ResolutionStage.DECLARATION, this::declarationDependencies),
                new SaveStage(ResolutionStage.DEFINITION, this::noDependencies)
        );
        for (var stage : stages) {
            for (TypeDTO typeDTO : stage.sort(map.values())) {
                var type = save(typeDTO, stage.stage);
                if(stage.stage == ResolutionStage.INIT)
                    types.add(type);
            }
        }
    }

    public List<Type> getTypes() {
        return types;
    }

    public IEntityContext getContext() {
        return context;
    }

    public Flow getFlow(RefDTO ref) {
        var existing = context.getFlow(ref);
        if (existing != null) {
            return existing;
        } else {
            var flowDTO = NncUtils.requireNonNull(flowMap.get(ref), "Flow '" + ref + " not available");
            var declaringType = getClassType(flowDTO.declaringTypeRef());
            return NncUtils.requireNonNull(declaringType.findFlow(f -> f.getRef().equals(ref)));
        }
    }

    public Type get(RefDTO ref) {
        var existing = context.getType(ref);
        if (existing != null)
            return existing;
        var typeDTO = NncUtils.requireNonNull(map.get(ref),
                "Type '" + ref + "' not available");
        return save(typeDTO, ResolutionStage.INIT);
    }

    public ClassType getClassType(RefDTO ref) {
        return (ClassType) get(ref);
    }

    public TypeVariable getTypeVariable(RefDTO ref) {
        return (TypeVariable) get(ref);
    }

    private Type save(TypeDTO typeDTO, ResolutionStage stage) {
        var type = Types.saveType(typeDTO, stage, this);
        if (type instanceof ClassType classType && context.isNewEntity(classType))
            newClasses.add(classType);
        return type;
    }

    public List<TypeDTO> getClassTypeDTOs() {
        return NncUtils.filter(this.map.values(), t -> t.param() instanceof ClassTypeParam);
    }

    public Set<RefDTO> noDependencies(TypeDTO typeDTO) {
        return Set.of();
    }

    public Set<RefDTO> initDependencies(TypeDTO typeDTO) {
        var dependencies = new HashSet<RefDTO>();
        var category = TypeCategory.getByCode(typeDTO.category());
        if (category.isPojo()) {
            var param = typeDTO.getClassParam();
            dependencies.add(param.templateRef());
            if (param.typeParameterRefs() != null)
                dependencies.addAll(param.typeParameterRefs());
        } else if (category.isArray()) {
            var param = typeDTO.getArrayTypeParam();
            dependencies.add(param.elementTypeRef());
        } else if (category == TypeCategory.UNION) {
            var param = typeDTO.getUnionParam();
            dependencies.addAll(param.memberRefs());
        } else if (category == TypeCategory.FUNCTION) {
            var param = typeDTO.getFunctionTypeParam();
            dependencies.addAll(param.parameterTypeRefs());
            dependencies.add(param.returnTypeRef());
        } else if (category == TypeCategory.UNCERTAIN) {
            var param = (UncertainTypeParam) typeDTO.param();
            dependencies.add(param.lowerBoundRef());
            dependencies.add(param.upperBoundRef());
        }
        return dependencies;
    }

    private Set<RefDTO> declarationDependencies(TypeDTO typeDTO) {
        var dependencies = new HashSet<RefDTO>();
        if (typeDTO.param() instanceof ClassTypeParam classParam) {
            if (classParam.superClassRef() != null)
                dependencies.add(classParam.superClassRef());
            if(classParam.interfaceRefs() != null)
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

    public @Nullable Long getTmpId(TypeKey typeKey) {
        return NncUtils.get(getTypeDTO(typeKey), TypeDTO::tmpId);
    }

    public @Nullable TypeDTO getTypeDTO(TypeKey typeKey) {
        return typeKey2TypeDTO.get(typeKey);
    }

    public @Nullable FlowDTO getFlowDTO(Flow flow) {
        var typeDTO = getTypeDTO(flow.getDeclaringType().getRef());
        return NncUtils.get(typeDTO, t -> t.getClassParam().findFlowBySignature(flow.getSignature()));
    }

    public @Nullable TypeDTO getTypeDTO(RefDTO ref) {
        return map.get(ref);
    }

    public static SaveTypeBatch empty(IEntityContext context) {
        return new SaveTypeBatch(context, List.of());
    }

}
