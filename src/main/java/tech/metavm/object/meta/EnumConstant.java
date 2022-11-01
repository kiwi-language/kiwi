package tech.metavm.object.meta;

import tech.metavm.constant.FieldNames;
import tech.metavm.entity.InstanceEntity;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.instance.rest.InstanceDTO;
import tech.metavm.object.instance.rest.InstanceFieldDTO;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;
import tech.metavm.object.meta.rest.dto.EnumConstantDTO;
import tech.metavm.util.NameUtils;

import java.util.List;

public class EnumConstant extends InstanceEntity {
    private final Type declaringType;
    private String name;
    private int ordinal;

    public EnumConstant(Instance instance) {
        super(instance);
        declaringType = instance.getType();
        name = instance.getString(FieldNames.NAME);
        ordinal = instance.getInt(FieldNames.ORDINAL);
    }
//
//    public EnumConstant(InstancePO po, Type declaringType) {
//        this(
//                po.id(),
//                declaringType,
//                po.getString(ColumnNames.S0),
//                po.getInt(ColumnNames.I0),
//                po.version()
//        );
//    }

    public EnumConstant(EnumConstantDTO enumConstantDTO, Type owner) {
        this(
                enumConstantDTO.id(),
                owner,
                enumConstantDTO.name(),
                enumConstantDTO.ordinal(),
                1
        );
    }

    public EnumConstant(
                        Long id,
                        Type type,
                        String name,
                        int order,
                        long version
    ) {
        super(type);
        this.id = id;
        this.declaringType = type;
        this.ordinal = order;
        setName(name);
        type.addEnumConstant(this);
    }

    public Type getDeclaringType() {
        return declaringType;
    }

    public String getName() {
        return name;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void update(EnumConstantDTO update) {
        setName(update.name());
        setOrdinal(update.ordinal());
    }

    public void setName(String name) {
        this.name = NameUtils.checkName(name);
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public EnumConstantDTO toDTO() {
        return new EnumConstantDTO(
                id,
                declaringType.getId(),
                ordinal,
                name
        );
    }

    public ChoiceOptionDTO toChoiceOptionDTO(boolean defaultSelected) {
        return new ChoiceOptionDTO(
                id,
                name,
                ordinal,
                defaultSelected
        );
    }

    @Override
    protected InstanceDTO toInstanceDTO() {
        return InstanceDTO.valueOf(
                id,
                type.getId(),
                name,
                List.of(
                        InstanceFieldDTO.valueOf(type.getFieldByName(FieldNames.NAME).getId(), name),
                        InstanceFieldDTO.valueOf(type.getFieldByName(FieldNames.ORDINAL).getId(), ordinal)
                )
        );
    }
}
