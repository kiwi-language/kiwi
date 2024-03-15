package tech.metavm.expression.dto;

public class TypeParsingContextDTO extends ParsingContextDTO{

    private String typeId;

    public TypeParsingContextDTO() {
        super(ContextTypes.CONTEXT_TYPE_TYPE);
    }

    public String getTypeId() {
        return typeId;
    }

    public void setTypeId(String typeId) {
        this.typeId = typeId;
    }
}
