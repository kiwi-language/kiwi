package org.metavm.object.type;

import org.metavm.flow.rest.FlowDTO;
import org.metavm.object.type.rest.dto.TypeDefDTO;

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
            var typeDef = Types.saveTypeDef(typeDefDTO, DECLARATION, batch);
            if(typeDef instanceof Klass klass && klass.isTemplate())
                klass.updateParameterized();
            return typeDef;
        }

        @Override
        void saveFunction(FlowDTO flowDTO, SaveTypeBatch batch) {
            Types.saveFunction(flowDTO, DECLARATION, batch);
        }

    },

    DEFINITION(4) {
        @Override
        TypeDef saveTypeDef(TypeDefDTO typeDefDTO, SaveTypeBatch batch) {
            return Types.saveTypeDef(typeDefDTO, DEFINITION, batch);
        }

    },

    ;

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
