package tech.metavm.object.meta;

import tech.metavm.constant.ColumnNames;
import tech.metavm.entity.EntityContext;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;
import tech.metavm.object.meta.rest.dto.FieldDTO;
import tech.metavm.util.BusinessException;
import tech.metavm.util.Column;

import java.util.ArrayList;
import java.util.List;

public class EnumEditContext {

    public static final String ORDER_FIELD = "序号";
    public static final String NAME_FIELD = "名称";

    private final Long id;
    private final String name;
    private final boolean anonymous;
    private final List<ChoiceOptionDTO> optionDTOs;
    private final List<ChoiceOption> defaultOptions = new ArrayList<>();
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
            for (ChoiceOptionDTO optionDTO : optionDTOs) {
                ChoiceOption option;
                if (optionDTO.id() == null) {
                    option = new ChoiceOption(optionDTO, type);
                } else {
                    option = type.getChoiceOption(optionDTO.id());
                    option.update(optionDTO);
                }
                if (optionDTO.defaultSelected()) {
                    defaultOptions.add(option);
                }
            }
        }
    }

    private Type createType() {
        type = new Type(
                null,
                name,
                TypeCategory.ENUM,
                anonymous,
                false,
                null,
                name,
                entityContext
        );
        createFields();
        return type;
    }

    private void createFields() {

        new Field(
                null,
                NAME_FIELD,
                type,
                Access.Public,
                true,
                true,
                null,
                Column.valueOf(ColumnNames.S0),
                entityContext.getTypeByCategory(TypeCategory.STRING),
                entityContext,
                false
        );

        new Field(
                null,
                ORDER_FIELD,
                type,
                Access.Public,
                true,
                false,
                null,
                Column.valueOf(ColumnNames.I0),
                entityContext.getTypeByCategory(TypeCategory.INT32),
                entityContext,
                false
        );
    }

    public Type getType() {
        return type;
    }

    public List<ChoiceOption> getDefaultOptions() {
        return defaultOptions;
    }

}
