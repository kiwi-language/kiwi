package tech.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import tech.metavm.common.BaseDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.flow.rest.GenericDeclarationDTO;
import tech.metavm.object.type.ClassSource;
import tech.metavm.object.type.TypeCategory;
import tech.metavm.util.InternalException;

import javax.annotation.Nullable;
import java.util.List;

public record TypeDTO(
        Long id,
        Long tmpId,
        String name,
        @Nullable String code,
        int category,
        boolean ephemeral,
        boolean anonymous,
        TypeParam param
) implements BaseDTO, GenericDeclarationDTO {

    public static TypeDTO createClass(String name, List<FieldDTO> fieldDTOs) {
        return createClass(null, name, fieldDTOs);
    }

    public static TypeDTO createClass(Long id, String name, List<FieldDTO> fieldDTOs) {
        return new TypeDTO(
                id, null, name, null, TypeCategory.CLASS.code(),
                false, false,
                new ClassTypeParam(
                        null,
                        List.of(),
                        ClassSource.RUNTIME.code(),
                        fieldDTOs, List.of(), List.of(), List.of(),  null, null,
                        List.of(), false, List.of(), List.of(), null, List.of(), List.of(),
                        false, List.of()
                )
        );
    }

    @JsonIgnore
    public ClassTypeParam getClassParam() {
        return (ClassTypeParam) param;
    }

    public static TypeDTO createClass(Long id,
                                      Long tmpId,
                                      String name,
                                      Long superTypeId,
                                      boolean anonymous,
                                      boolean ephemeral,
                                      List<FieldDTO> fieldDTOs,
                                      List<ConstraintDTO> constraintDTOs,
                                      String desc) {
        return new TypeDTO(
                id, tmpId, name, null, TypeCategory.CLASS.code(),
                ephemeral, anonymous,
                new ClassTypeParam(
                        RefDTO.fromId(superTypeId),
                        List.of(),
                        ClassSource.RUNTIME.code(),
                        fieldDTOs,
                        List.of(),
                        constraintDTOs,
                        List.of(),
                        desc,
                        null,
                        List.of(),
                        false,
                        List.of(),
                        List.of(),
                        null,
                        List.of(),
                        List.of(),
                        false,
                        List.of()
                )
        );
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
    public List<RefDTO> typeParameterRefs() {
        if(param instanceof ClassTypeParam classTypeParam)
            return classTypeParam.typeParameterRefs();
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
