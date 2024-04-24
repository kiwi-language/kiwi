package tech.metavm.object.type;

import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.type.rest.dto.TypeDefDTO;

public enum ResolutionStage {

    INIT(0) {
        @Override
        TypeDef saveTypeDef(TypeDefDTO typeDefDTO, SaveTypeBatch batch) {
            return Types.saveTypeDef(typeDefDTO, INIT, batch);
        }
    },

    SIGNATURE(2) {
        @Override
        TypeDef saveTypeDef(TypeDefDTO typeDefDTO, SaveTypeBatch batch) {
            return Types.saveTypeDef(typeDefDTO, SIGNATURE, batch);
        }
    },

    DECLARATION(3) {
        @Override
        TypeDef saveTypeDef(TypeDefDTO typeDefDTO, SaveTypeBatch batch) {
            return Types.saveTypeDef(typeDefDTO, DECLARATION, batch);
        }

        @Override
        void saveFunction(FlowDTO flowDTO, SaveTypeBatch batch) {
            Types.saveFunction(flowDTO, DECLARATION, batch);
        }

    },

    DEFINITION(4) {
        @Override
        TypeDef saveTypeDef(TypeDefDTO typeDefDTO, SaveTypeBatch batch) {
            //            if (type instanceof Klass classType)
//                batch.saveParameterizedFlows(classType, DEFINITION);
//            if (type instanceof Klass classType && classType.isClass() && !classType.isAnonymous())
//                MappingSaver.create(batch.getContext()).saveBuiltinMapping(classType, false);
            return Types.saveTypeDef(typeDefDTO, DEFINITION, batch);
        }

    },

    MAPPING_DEFINITION(5) {
        @Override
        TypeDef saveTypeDef(TypeDefDTO typeDefDTO, SaveTypeBatch batch) {
            //            if (type instanceof Klass classType && classType.isClass() && !classType.isAnonymous())
//                MappingSaver.create(batch.getContext()).saveBuiltinMapping(classType, true);
            return batch.getTypeDef(typeDefDTO.id());
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

    @SuppressWarnings("UnusedReturnValue")
    abstract TypeDef saveTypeDef(TypeDefDTO typeDefDTO, SaveTypeBatch batch);

    void saveFunction(FlowDTO flowDTO, SaveTypeBatch batch) {
    }

}
