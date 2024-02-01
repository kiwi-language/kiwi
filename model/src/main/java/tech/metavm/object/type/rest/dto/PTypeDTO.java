package tech.metavm.object.type.rest.dto;

import org.jetbrains.annotations.Nullable;
import tech.metavm.common.RefDTO;

import java.util.List;
import java.util.function.Consumer;

public final class PTypeDTO extends GenericElementDTO implements TypeParam {
    private final List<RefDTO> typeArgumentRefs;
    private final List<GenericElementDTO> fields;
    private final List<GenericElementDTO> staticFields;
    private final List<FlowInfo> flows;
    private final List<GenericElementDTO> mappings;

    public PTypeDTO(
            RefDTO ref,
            RefDTO templateRef,
            List<RefDTO> typeArgumentRefs,
            List<GenericElementDTO> fields,
            List<GenericElementDTO> staticFields,
            List<FlowInfo> flows,
            List<GenericElementDTO> mappings
    ) {
        super(templateRef, ref);
        this.typeArgumentRefs = typeArgumentRefs;
        this.fields = fields;
        this.staticFields = staticFields;
        this.flows = flows;
        this.mappings = mappings;
    }

    public ParameterizedTypeKey getKey() {
        return new ParameterizedTypeKey(getTemplateRef(), typeArgumentRefs);
    }

    @Override
    public void forEachDescendant(Consumer<GenericElementDTO> action) {
        super.forEachDescendant(action);
        fields.forEach(f -> f.forEachDescendant(action));
        flows.forEach(f -> f.forEachDescendant(action));
        mappings.forEach(m -> m.forEachDescendant(action));
    }

    public List<RefDTO> getTypeArgumentRefs() {
        return typeArgumentRefs;
    }

    public List<GenericElementDTO> getFields() {
        return fields;
    }

    public List<GenericElementDTO> getStaticFields() {
        return staticFields;
    }

    public List<FlowInfo> getFlows() {
        return flows;
    }

    public List<GenericElementDTO> getMappings() {
        return mappings;
    }

    @Override
    public int getType() {
        return 9;
    }

    @Nullable
    @Override
    public TypeKey getTypeKey() {
        return new ParameterizedTypeKey(getTemplateRef(), getTypeArgumentRefs());
    }
}
