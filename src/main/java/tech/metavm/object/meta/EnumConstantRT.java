package tech.metavm.object.meta;

import tech.metavm.entity.Entity;
import tech.metavm.entity.EntityContext;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;
import tech.metavm.object.meta.persistence.EnumConstantPO;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;
import tech.metavm.object.meta.rest.dto.EnumConstantDTO;

@EntityType("枚举值")
public class EnumConstantRT extends Entity {

    public static final long MIN_ID = Long.MAX_VALUE - Integer.MAX_VALUE;
    public static final long MAX_ID = Long.MAX_VALUE;

//    private transient final Field nameField;
//    private transient final Field ordinalField;

    @EntityField("类型")
    private final Type type;
    @EntityField("名称")
    private String name;
    @EntityField("序号")
    private int ordinal;

//    public EnumConstantRT(EnumConstantPO enumConstantPO, EntityContext context) {
//        super(enumConstantPO.getId(), context);
//        this.name = enumConstantPO.getName();
//        this.ordinal = enumConstantPO.getOrdinal();
//    }

    public EnumConstantRT(EnumConstantDTO dto, Type type) {
//        super(type.getContext());
        this.type = type;
        type.addEnumConstant(this);
        setName(dto.name());
        ordinal = dto.ordinal();
    }

    public EnumConstantRT(Type type, String name, int ordinal) {
        this.type = type;
        this.name = name;
        this.ordinal = ordinal;
        type.addEnumConstant(this);
    }

    public Type getType() {
        return type;
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
        this.name = name;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal;
    }

    public EnumConstantDTO toEnumConstantDTO() {
        return new EnumConstantDTO(
                id,
                type.getId(),
                getOrdinal(),
                getName()
        );
    }

    public ChoiceOptionDTO toChoiceOptionDTO(boolean defaultSelected) {
        return new ChoiceOptionDTO(
                id,
                getName(),
                getOrdinal(),
                defaultSelected
        );
    }

}
