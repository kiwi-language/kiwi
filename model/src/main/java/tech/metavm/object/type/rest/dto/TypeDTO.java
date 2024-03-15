package tech.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.common.BaseDTO;
import tech.metavm.flow.rest.GenericDeclarationDTO;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.List;

public record TypeDTO(
        String id,
        String name,
        @Nullable String code,
        int category,
        boolean ephemeral,
        boolean anonymous,
        TypeParam param
) implements BaseDTO, GenericDeclarationDTO {

    @JsonIgnore
    public ClassTypeParam getClassParam() {
        return (ClassTypeParam) param;
    }

    @JsonIgnore
    public PTypeDTO getPTypeDTO() {
        return (PTypeDTO) param;
    }

    @JsonIgnore
    public TypeVariableParam getTypeVariableParam() {
        return (TypeVariableParam) param;
    }

    @JsonIgnore
    public ArrayTypeParam getArrayTypeParam() {
        return (ArrayTypeParam) param;
    }

    @JsonIgnore
    public UnionTypeParam getUnionParam() {
        return (UnionTypeParam) param;
    }

    @JsonIgnore
    public FunctionTypeParam getFunctionTypeParam() {
        return (FunctionTypeParam) param;
    }

    @JsonIgnore
    public UncertainTypeParam getUncertainTypeParam() {
        return (UncertainTypeParam) param;
    }

    @JsonIgnore
    @Override
    public List<String> typeParameterIds() {
        if(param instanceof ClassTypeParam classTypeParam)
            return classTypeParam.typeParameterIds();
        else
            throw new InternalException("Not a generic declaration");
    }

    @JsonIgnore
    public IntersectionTypeParam getIntersectionParam() {
        return (IntersectionTypeParam) param;
    }

    @JsonIgnore
    public @Nullable TypeKey getTypeKey() {
        return param.getTypeKey();
    }

}
