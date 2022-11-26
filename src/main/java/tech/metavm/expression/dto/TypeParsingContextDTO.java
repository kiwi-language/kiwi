package tech.metavm.expression.dto;

import tech.metavm.expression.ExpressionService;

public class TypeParsingContextDTO extends ParsingContextDTO{

    private long typeId;

    public TypeParsingContextDTO() {
        super(ExpressionService.CONTEXT_TYPE_TYPE);
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }
}
