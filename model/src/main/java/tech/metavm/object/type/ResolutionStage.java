package tech.metavm.object.type;

import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.rest.dto.ParameterizedFlowDTO;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.object.view.MappingSaver;
import tech.metavm.util.NncUtils;

public enum ResolutionStage {

    INIT(0) {
        @Override
        Type saveType(TypeDTO typeDTO, SaveTypeBatch batch) {
            return Types.saveType(typeDTO, INIT, batch);
        }
    },

    SIGNATURE(2) {
        @Override
        Type saveType(TypeDTO typeDTO, SaveTypeBatch batch) {
            return Types.saveType(typeDTO, SIGNATURE, batch);
        }
    },

    DECLARATION(3) {
        @Override
        Type saveType(TypeDTO typeDTO, SaveTypeBatch batch) {
            var type = Types.saveType(typeDTO, DECLARATION, batch);
            if (type instanceof ClassType classType)
                batch.saveParameterizedFlows(classType, DECLARATION);
            return type;
        }

        @Override
        void saveFunction(FlowDTO flowDTO, SaveTypeBatch batch) {
            Types.saveFunction(flowDTO, DECLARATION, batch);
        }

        @Override
        void saveParameterizedFlow(ParameterizedFlowDTO flowDTO, SaveTypeBatch batch) {
            var context = batch.getContext();
            var template = context.getFlow(Id.parse(flowDTO.getTemplateId()));
            var typeArgs = NncUtils.map(flowDTO.getTypeArgumentIds(), id -> context.getType(Id.parse(id)));
            context.getGenericContext().getParameterizedFlow(template, typeArgs, DECLARATION, batch);
        }

    },

    DEFINITION(4) {
        @Override
        Type saveType(TypeDTO typeDTO, SaveTypeBatch batch) {
            var type = Types.saveType(typeDTO, DEFINITION, batch);
            if (type instanceof ClassType classType)
                batch.saveParameterizedFlows(classType, DEFINITION);
            if (type instanceof ClassType classType && classType.isClass() && !classType.isAnonymous())
                MappingSaver.create(batch.getContext()).saveBuiltinMapping(classType, false);
            return type;
        }

    },

    MAPPING_DEFINITION(5) {
        @Override
        Type saveType(TypeDTO typeDTO, SaveTypeBatch batch) {
            var type = batch.get(typeDTO.id());
            if (type instanceof ClassType classType && classType.isClass() && !classType.isAnonymous())
                MappingSaver.create(batch.getContext()).saveBuiltinMapping(classType, true);
            return type;
        }
    };

    private final int code;

    ResolutionStage(int code) {
        this.code = code;
    }

    public int code() {
        return code;
    }

    public boolean isBefore(ResolutionStage stage) {
        return code < stage.code;
    }

    public boolean isAfter(ResolutionStage stage) {
        return code > stage.code;
    }

    public boolean isAfterOrAt(ResolutionStage stage) {
        return code >= stage.code;
    }

    public boolean isBeforeOrAt(ResolutionStage stage) {
        return code <= stage.code;
    }

    abstract Type saveType(TypeDTO typeDTO, SaveTypeBatch batch);

    void saveFunction(FlowDTO flowDTO, SaveTypeBatch batch) {
    }

    void saveParameterizedFlow(ParameterizedFlowDTO flowDTO, SaveTypeBatch batch) {
    }

}
