package tech.metavm.expression.dto;

public class TypeParsingContextDTO extends ParsingContextDTO{

    private long typeId;

    public TypeParsingContextDTO() {
        super(ContextTypes.CONTEXT_TYPE_TYPE);
    }

    public long getTypeId() {
        return typeId;
    }

    public void setTypeId(long typeId) {
        this.typeId = typeId;
    }
}
