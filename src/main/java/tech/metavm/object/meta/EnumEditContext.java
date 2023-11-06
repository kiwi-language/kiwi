package tech.metavm.object.meta;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.meta.rest.dto.ChoiceOptionDTO;
import tech.metavm.object.meta.rest.dto.EnumConstantDTO;
import tech.metavm.util.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnumEditContext {

    private final Long id;
    private final String name;
    private final boolean anonymous;
    private final List<ChoiceOptionDTO> optionDTOs;
    private final List<EnumConstantRT> defaultOptions = new ArrayList<>();
    private final IEntityContext entityContext;
    private ClassType type;

    public EnumEditContext(Long id,
                           String name,
                           boolean anonymous,
                           List<ChoiceOptionDTO> optionDTOs,
                           IEntityContext entityContext) {
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
        entityContext.initIds();
    }

    private void init() {
        if(id == null) {
            type = createType();
        }
        else {
            type = entityContext.getEntity(ClassType.class, id);
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
                EnumConstantRT option;
                if (optionDTO.id() == null) {
                    option = new EnumConstantRT(convertToEnumConstant(optionDTO, ordinal++), type);
                    Objects.requireNonNull(entityContext.getInstanceContext()).bind(option.getInstance());
//                    type.addEnumConstant(option.getInstance());
                    addEnumConstant(option.getName(), option.getInstance());
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

    private void addEnumConstant(String name, ClassInstance instance) {
        FieldBuilder.newBuilder(name, null, type, type)
                .isChild(true)
                .isStatic(true)
                .staticValue(instance)
                .build();
    }

    private EnumConstantDTO convertToEnumConstant(ChoiceOptionDTO choiceOptionDTO, int ordinal) {
        return new EnumConstantDTO(
                choiceOptionDTO.id(),
                0L,
                ordinal,
                choiceOptionDTO.name()
        );
    }

    private ClassType createType() {
        type = ClassBuilder.newBuilder(name, null)
                .superClass(ModelDefRegistry.getClassType(Enum.class))
                .category(TypeCategory.ENUM)
                .anonymous(anonymous)
                .build();
        entityContext.bind(type);
        entityContext.initIds();
        return type;
    }

//    private void createFields() {
//        new Field(
//                FieldNames.NAME,
//                type,
//                Access.GLOBAL,
//                true,
//                true,
//                null,
//                Column.valueOf(ColumnNames.S0),
//                entityContext.getStringType(),
//                entityContext,
//                true
//        );
//
//        new Field(
//                FieldNames.ORDINAL,
//                type,
//                Access.GLOBAL,
//                true,
//                false,
//                null,
//                Column.valueOf(ColumnNames.I0),
//                entityContext.getIntType(),
//                entityContext,
//                true
//        );
//    }

    public ClassType getType() {
        return type;
    }

    public List<EnumConstantRT> getDefaultOptions() {
        return defaultOptions;
    }

}
