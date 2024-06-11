package tech.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.flow.rest.GenericDeclarationDTO;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;

public record TypeDTO(
        String id,
        String name,
        @Nullable String code,
        int kind,
        boolean ephemeral,
        boolean anonymous,
        TypeParam param
) implements TypeDefDTO, GenericDeclarationDTO {

    @JsonIgnore
    public ClassTypeParam getClassParam() {
        return (ClassTypeParam) param;
    }

    @JsonIgnore
    public ArrayTypeParam getArrayTypeParam() {
        return (ArrayTypeParam) param;
    }

    @JsonIgnore
    public String getCodeRequired() {
        return Objects.requireNonNull(code);
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

    @Override
    public int getDefKind() {
        return 1;
    }
}
