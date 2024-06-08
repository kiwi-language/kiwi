package tech.metavm.object.type;

import tech.metavm.entity.IEntityContext;
import tech.metavm.entity.ModelDefRegistry;
import tech.metavm.object.instance.core.ClassInstance;
import tech.metavm.object.instance.core.Id;
import tech.metavm.object.type.rest.dto.ChoiceOptionDTO;
import tech.metavm.object.type.rest.dto.EnumConstantDTO;
import tech.metavm.util.BusinessException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class EnumEditContext {

    private final String id;
    private final String name;
    private final boolean anonymous;
    private final List<ChoiceOptionDTO> optionDTOs;
    private final List<EnumConstantRT> defaultOptions = new ArrayList<>();
    private final IEntityContext entityContext;
    private Klass type;

    public EnumEditContext(String id,
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
            type = entityContext.getEntity(Klass.class, Id.parse(id));
        }
    }

    private void validate() {
        if(optionDTOs != null && optionDTOs.isEmpty()) {
            throw BusinessException.invalidField(name, "options can not be empty");
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
                    option = type.getEnumConstant(Id.parse(optionDTO.id()));
                    option.update(convertToEnumConstant(optionDTO, ordinal++));
                }
                if (optionDTO.defaultSelected()) {
                    defaultOptions.add(option);
                }
            }
        }
    }

    private void addEnumConstant(String name, ClassInstance instance) {
        FieldBuilder.newBuilder(name, null, type, type.getType())
                .isChild(true)
                .isStatic(true)
                .staticValue(instance)
                .build();
    }

    private EnumConstantDTO convertToEnumConstant(ChoiceOptionDTO choiceOptionDTO, int ordinal) {
        return new EnumConstantDTO(
                choiceOptionDTO.id(),
                null,
                ordinal,
                choiceOptionDTO.name()
        );
    }

    private Klass createType() {
        type = ClassTypeBuilder.newBuilder(name, null)
                .superClass(ModelDefRegistry.getClassType(Enum.class))
                .kind(ClassKind.ENUM)
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

    public Klass getType() {
        return type;
    }

    public List<EnumConstantRT> getDefaultOptions() {
        return defaultOptions;
    }

}
