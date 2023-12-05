package tech.metavm.object.type.rest.dto;

import tech.metavm.common.BaseDTO;
import tech.metavm.common.ErrorDTO;
import tech.metavm.common.RefDTO;
import tech.metavm.flow.rest.FlowDTO;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.util.NncUtils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class ClassTypeDTOBuilder {

    public static ClassTypeDTOBuilder newBuilder(String name) {
        return new ClassTypeDTOBuilder(name);
    }

    private final String name;
    private int category = TypeCategoryCodes.CLASS;
    private @Nullable Long id;
    private @Nullable Long tmpId;
    private @Nullable String code;
    private @Nullable String desc;
    private @Nullable Object extra;
    private @Nullable String sourceClassName;
    private boolean ephemeral;
    private boolean anonymous;
    private @Nullable RefDTO superClassRef;
    private List<RefDTO> interfaceRefs = new ArrayList<>();
    private List<FieldDTO> fields = new ArrayList<>();
    private List<ConstraintDTO> constraints = new ArrayList<>();
    private List<FlowDTO> flows = new ArrayList<>();
    private int source = ClassSourceCodes.RUNTIME;
    private boolean isTemplate;
    private RefDTO templateRef;
    private List<RefDTO> typeArgumentRefs = new ArrayList<>();
    private List<TypeDTO> typeParameters = new ArrayList<>();
    private List<RefDTO> typeParameterRefs = new ArrayList<>();
    private List<RefDTO> dependencyRefs = new ArrayList<>();
    private boolean hasSubTypes;
    private List<InstanceDTO> enumConstants = new ArrayList<>();
    private List<ErrorDTO> errors = new ArrayList<>();

    private ClassTypeDTOBuilder(String name) {
        this.name = name;
    }

    public ClassTypeDTOBuilder id(Long id) {
        this.id = id;
        return this;
    }

    public ClassTypeDTOBuilder tmpId(Long tmpId) {
        this.tmpId = tmpId;
        return this;
    }

    public ClassTypeDTOBuilder category(int category) {
        this.category = category;
        return this;
    }

    public ClassTypeDTOBuilder code(String code) {
        this.code = code;
        return this;
    }

    public ClassTypeDTOBuilder source(int source) {
        this.source = source;
        return this;
    }

    public ClassTypeDTOBuilder interfaceRefs(List<RefDTO> interfaceRefs) {
        this.interfaceRefs = new ArrayList<>(interfaceRefs);
        return this;
    }

    public ClassTypeDTOBuilder ephemeral(boolean ephemeral) {
        this.ephemeral = ephemeral;
        return this;
    }

    public ClassTypeDTOBuilder anonymous(boolean anonymous) {
        this.anonymous = anonymous;
        return this;
    }

    public ClassTypeDTOBuilder addField(FieldDTO field) {
        this.fields.add(field);
        return this;
    }

    public ClassTypeDTOBuilder hasSubTypes(boolean hasSubTypes) {
        this.hasSubTypes = hasSubTypes;
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
        this.fields = new ArrayList<>(fields);
        return this;
    }

    public ClassTypeDTOBuilder constraints(List<ConstraintDTO> constraints) {
        this.constraints = new ArrayList<>(constraints);
        return this;
    }

    public ClassTypeDTOBuilder superClassRef(RefDTO superClassRef) {
        this.superClassRef = superClassRef;
        return this;
    }

    public ClassTypeDTOBuilder isTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
        return this;
    }

    public ClassTypeDTOBuilder templateRef(RefDTO templateRef) {
        this.templateRef = templateRef;
        return this;
    }

    public ClassTypeDTOBuilder sourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
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

    public ClassTypeDTOBuilder typeArgumentRefs(List<RefDTO> typeArgumentRefs) {
        this.typeArgumentRefs = new ArrayList<>(typeArgumentRefs);
        return this;
    }

    public ClassTypeDTOBuilder dependencyRefs(List<RefDTO> dependencyRefs) {
        this.dependencyRefs = new ArrayList<>(dependencyRefs);
        return this;
    }

    public ClassTypeDTOBuilder typeParameters(List<TypeDTO> typeParameters) {
        this.typeParameters = new ArrayList<>(typeParameters);
        this.typeParameterRefs= NncUtils.map(typeParameters, BaseDTO::getRef);
        return this;
    }

    private ClassTypeParam buildClassTypeParam() {
        return new ClassTypeParam(
                sourceClassName,
                superClassRef,
                interfaceRefs,
                source,
                fields,
                fields,
                constraints,
                flows,
                desc,
                extra,
                enumConstants,
                isTemplate,
                typeParameterRefs,
                typeParameters,
                templateRef,
                typeArgumentRefs,
                dependencyRefs,
                hasSubTypes,
                errors
        );
    }

    public TypeDTO build() {
        return new TypeDTO(
                id,
                tmpId,
                name,
                code,
                category,
                ephemeral,
                anonymous,
                buildClassTypeParam()
        );
    }
}
