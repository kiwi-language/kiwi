package org.metavm.object.type.rest.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.metavm.common.CopyContext;
import org.metavm.common.rest.dto.Copyable;
import org.metavm.common.rest.dto.ErrorDTO;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.flow.rest.GenericDeclarationDTO;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.view.rest.dto.ObjectMappingDTO;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public record KlassDTO(
        String id,
        String name,
        @Nullable String code,
        int kind,
        boolean ephemeral,
        boolean anonymous,
        Map<String, String> attributes,
        String superType,
        List<String> interfaces,
        int source,
        List<FieldDTO> fields,
        List<FieldDTO> staticFields,
        @Nullable String titleFieldId,
        List<ConstraintDTO> constraints,
        List<FlowDTO> flows,
        List<ObjectMappingDTO> mappings,
        @Nullable String defaultMappingId,
        String desc,
        Object extra,
        List<InstanceDTO> enumConstants,
        boolean isAbstract,
        boolean isTemplate,
        List<String> typeParameterIds,
        @Nullable List<TypeVariableDTO> typeParameters,
        String templateId,
        List<String> typeArguments,
        boolean hasSubTypes,
        boolean struct,
        List<ErrorDTO> errors
) implements TypeDefDTO, GenericDeclarationDTO, Copyable<KlassDTO> {

    @JsonIgnore
    public String getCodeNotNull() {
        return Objects.requireNonNull(code, "Code is not set for " + this);
    }

    @Override
    public int getDefKind() {
        return 1;
    }

    @Override
    public KlassDTO copy(CopyContext context) {
        return new KlassDTO(
                context.mapId(id),
                name,
                code,
                kind,
                ephemeral,
                anonymous,
                attributes,
                superType,
                interfaces,
                source,
                NncUtils.map(fields, context::copy),
                NncUtils.map(staticFields, context::copy),
                titleFieldId,
                NncUtils.map(constraints, context::copy),
                NncUtils.map(flows, context::copy),
                NncUtils.map(mappings, context::copy),
                defaultMappingId,
                desc,
                extra,
                NncUtils.map(enumConstants, context::copy),
                isAbstract,
                isTemplate,
                typeParameterIds,
                NncUtils.map(typeParameters, context::copy),
                templateId,
                typeArguments,
                hasSubTypes,
                struct,
                NncUtils.map(errors, context::copy)
        );
    }
}
