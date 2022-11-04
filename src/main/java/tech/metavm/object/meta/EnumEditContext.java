package tech.metavm.object.meta;

import tech.metavm.constant.ColumnNames;
import tech.metavm.constant.FieldNames;
import tech.metavm.entity.EntityContext;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;
import tech.metavm.object.meta.rest.dto.EnumConstantDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Column;

import java.util.ArrayList;
import java.util.List;

public class EnumEditContext {

    private final Long id;
    private final String name;
    private final boolean anonymous;
    private final List<ChoiceOptionDTO> optionDTOs;
    private final List<EnumConstant> defaultOptions = new ArrayList<>();
    private final EntityContext entityContext;
    private Type type;

    public EnumEditContext(Long id,
                           String name,
                           boolean anonymous,
                           List<ChoiceOptionDTO> optionDTOs,
                           EntityContext entityContext) {
        this.id = id;
        this.name = name;
        this.anonymous = anonymous;
        this.optionDTOs = optionDTOs;
        this.entityContext = entityContext;
    }

    public void execute() {
        init();
        validate();
        update();
    }

    private void init() {
        if(id == null) {
            type = createType();
        }
        else {
            type = entityContext.getType(id);
        }
    }

    private void validate() {
        if(optionDTOs != null && optionDTOs.isEmpty()) {
            throw BusinessException.invalidField(name, "选项不能为空");
        }
    }

    private void update() {
        type.setName(name);
        type.setAnonymous(anonymous);
        if(optionDTOs != null) {
            int ordinal = 0;
            for (ChoiceOptionDTO optionDTO : optionDTOs) {
                EnumConstant option;
                if (optionDTO.id() == null) {
                    option = new EnumConstant(convertToEnumConstant(optionDTO, ordinal++), type);
                } else {
                    option = type.getEnumConstant(optionDTO.id());
                    option.update(convertToEnumConstant(optionDTO, ordinal++));
                }
                if (optionDTO.defaultSelected()) {
                    defaultOptions.add(option);
                }
            }
        }
    }

    private EnumConstantDTO convertToEnumConstant(ChoiceOptionDTO choiceOptionDTO, int ordinal) {
        return new EnumConstantDTO(
                choiceOptionDTO.id(),
                0L,
                ordinal,
                choiceOptionDTO.name()
        );
    }

    private Type createType() {
        type = new Type(
                name,
                TypeCategory.ENUM,
                anonymous,
                false,
                null,
                null,
                name,
                entityContext
        );
        createFields();
        entityContext.initIds();
        return type;
    }

    private void createFields() {
        new Field(
                null,
                FieldNames.NAME,
                type,
                Access.GLOBAL,
                true,
                true,
                null,
                Column.valueOf(ColumnNames.S0),
                entityContext.getTypeByCategory(TypeCategory.STRING),
                entityContext,
                true
        );

        new Field(
                null,
                FieldNames.ORDINAL,
                type,
                Access.GLOBAL,
                true,
                false,
                null,
                Column.valueOf(ColumnNames.I0),
                entityContext.getTypeByCategory(TypeCategory.INT),
                entityContext,
                true
        );
    }

    public Type getType() {
        return type;
    }

    public List<EnumConstant> getDefaultOptions() {
        return defaultOptions;
    }

}
