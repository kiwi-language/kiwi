package org.metavm.object.type.rest.dto;

import org.metavm.common.rest.dto.BaseDTO;
import org.metavm.common.rest.dto.ErrorDTO;
import org.metavm.flow.rest.FlowDTO;
import org.metavm.object.instance.core.TmpId;
import org.metavm.object.instance.rest.InstanceDTO;
import org.metavm.object.type.ClassKind;
import org.metavm.object.type.TypeTags;
import org.metavm.object.view.rest.dto.ObjectMappingDTO;
import org.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ClassTypeDTOBuilder {

    public static ClassTypeDTOBuilder newBuilder(String name) {
        return new ClassTypeDTOBuilder(name);
    }

    private final String name;
    private int kind = ClassKind.CLASS.code();
    @Nullable
    private String id;
    @Nullable
    private Long tmpId;
    @Nullable
    private String code;
    @Nullable
    private String desc;
    @Nullable
    private Object extra;
    private boolean ephemeral;
    private boolean anonymous;
    private boolean searchable;
    @Nullable
    private String superClassId;
    private List<String> interfaceIds = new ArrayList<>();
    private List<FieldDTO> fields = new ArrayList<>();
    private List<FieldDTO> staticFields = new ArrayList<>();
    @Nullable
    private String titleFieldId;
    private List<ConstraintDTO> constraints = new ArrayList<>();
    private List<FlowDTO> flows = new ArrayList<>();
    private List<ObjectMappingDTO> mappings = new ArrayList<>();
    private List<EnumConstantDefDTO> enumConstantDefs = new ArrayList<>();
    private String defaultMappingId;
    private int source = ClassSourceCodes.RUNTIME;
    private boolean isAbstract;
    private boolean isTemplate;
    private String templateId;
    private List<String> typeArgumentIds = new ArrayList<>();
    private List<TypeVariableDTO> typeParameters = new ArrayList<>();
    private List<String> typeParameterIds = new ArrayList<>();
    private boolean hasSubTypes;
    private boolean struct;
    private int tag = TypeTags.DEFAULT;
    private Integer sourceCodeTag;
    private List<InstanceDTO> enumConstants = new ArrayList<>();
    private List<ErrorDTO> errors = new ArrayList<>();

    private ClassTypeDTOBuilder(String name) {
        this.name = name;
    }

    public ClassTypeDTOBuilder id(String id) {
        this.id = id;
        return this;
    }

    public ClassTypeDTOBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public ClassTypeDTOBuilder code(String code) {
        this.code = code;
        return this;
    }

    public ClassTypeDTOBuilder kind(int kind) {
        this.kind = kind;
        return this;
    }

    public ClassTypeDTOBuilder source(int source) {
        this.source = source;
        return this;
    }

    public ClassTypeDTOBuilder interfaceIds(List<String> interfaceIds) {
        this.interfaceIds = new ArrayList<>(interfaceIds);
        return this;
    }

    public ClassTypeDTOBuilder ephemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }

    public ClassTypeDTOBuilder titleFieldId(String titleFieldId) {
        this.titleFieldId = titleFieldId;
        return this;
    }

    public ClassTypeDTOBuilder anonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public ClassTypeDTOBuilder addField(FieldDTO field) {
        NncUtils.requireFalse(field.isStatic(), "FieldDTO must not be static");
        this.fields.add(field);
        return this;
    }

    public ClassTypeDTOBuilder addStaticField(FieldDTO field) {
        NncUtils.requireTrue(field.isStatic(), "FieldDTO must be static");
        this.staticFields.add(field);
        return this;
    }

    public ClassTypeDTOBuilder addMethod(FlowDTO method) {
        this.flows.add(method);
        return this;
    }

    public ClassTypeDTOBuilder methods(List<FlowDTO> methods) {
        this.flows = methods != null ? new ArrayList<>(methods) : null;
        return this;
    }

    public ClassTypeDTOBuilder hasSubTypes(boolean hasSubTypes) {
        this.hasSubTypes = hasSubTypes;
        return this;
    }

    public ClassTypeDTOBuilder struct(boolean struct) {
        this.struct = struct;
        return this;
    }

    public ClassTypeDTOBuilder searchable(boolean searchable) {
        this.searchable = searchable;
        return this;
    }

    public ClassTypeDTOBuilder extra(Object extra) {
        this.extra = extra;
        return this;
    }

    public ClassTypeDTOBuilder desc(String desc) {
        this.desc = desc;
        return this;
    }

    public ClassTypeDTOBuilder fields(List<FieldDTO> fields) {
        this.fields = fields != null ? new ArrayList<>(fields) : null;
        return this;
    }

    public ClassTypeDTOBuilder staticFields(List<FieldDTO> staticFields) {
        this.staticFields = staticFields;
        return this;
    }

    public ClassTypeDTOBuilder mappings(List<ObjectMappingDTO> mappings) {
        this.mappings = mappings != null ? new ArrayList<>(mappings) : null;
        return this;
    }

    public ClassTypeDTOBuilder enumConstantDefs(List<EnumConstantDefDTO> enumConstantDefs) {
        this.enumConstantDefs = enumConstantDefs != null ? new ArrayList<>(enumConstantDefs) : null;
        return this;
    }

    public ClassTypeDTOBuilder defaultMappingId(String defaultMappingId) {
        this.defaultMappingId = defaultMappingId;
        return this;
    }

    public ClassTypeDTOBuilder constraints(List<ConstraintDTO> constraints) {
        this.constraints = new ArrayList<>(constraints);
        return this;
    }

    public ClassTypeDTOBuilder superClassId(String superClassId) {
        this.superClassId = superClassId;
        return this;
    }

    public ClassTypeDTOBuilder isTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
        return this;
    }

    public ClassTypeDTOBuilder templateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    public ClassTypeDTOBuilder isAbstract(boolean isAbstract) {
        this.isAbstract = isAbstract;
        return this;
    }

    public ClassTypeDTOBuilder sourceClassName(String sourceClassName) {
        return this;
    }

    public ClassTypeDTOBuilder enumConstants(List<InstanceDTO> enumConstants) {
        this.enumConstants = new ArrayList<>(enumConstants);
        return this;
    }

    public ClassTypeDTOBuilder errors(List<ErrorDTO> errors) {
        this.errors = new ArrayList<>(errors);
        return this;
    }

    public ClassTypeDTOBuilder flows(List<FlowDTO> flows) {
        this.flows = new ArrayList<>(flows);
        return this;
    }

    public ClassTypeDTOBuilder typeArgumentIds(List<String> typeArgumentIds) {
        this.typeArgumentIds = new ArrayList<>(typeArgumentIds);
        return this;
    }

    public ClassTypeDTOBuilder sourceMappingId(String sourceMappingId) {
        return this;
    }

    public ClassTypeDTOBuilder typeParameters(List<TypeVariableDTO> typeParameters) {
        this.typeParameters = new ArrayList<>(typeParameters);
        this.typeParameterIds = NncUtils.map(typeParameters, BaseDTO::id);
        return this;
    }

    public ClassTypeDTOBuilder typeParameterIds(List<String> typeParameterIds) {
        this.typeParameterIds = new ArrayList<>(typeParameterIds);
        return this;
    }

    public ClassTypeDTOBuilder tag(int tag) {
        this.tag = tag;
        return this;
    }

    public ClassTypeDTOBuilder sourceCodeTag(Integer sourceCodeTag) {
        this.sourceCodeTag = sourceCodeTag;
        return this;
    }

    @Nullable
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public KlassDTO build() {
        if(id == null && tmpId != null)
            id = TmpId.of(tmpId).toString();
        return new KlassDTO(
                id,
                name,
                code,
                kind,
                ephemeral,
                anonymous,
                Map.of(),
                superClassId,
                interfaceIds,
                source,
                fields,
                staticFields,
                titleFieldId,
                constraints,
                flows,
                mappings,
                enumConstantDefs,
                defaultMappingId,
                desc,
                extra,
                isAbstract,
                isTemplate,
                typeParameterIds,
                typeParameters,
                templateId,
                typeArgumentIds,
                hasSubTypes,
                struct,
                searchable,
                tag,
                sourceCodeTag,
                errors
        );
    }
}
