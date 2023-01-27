package tech.metavm.object.meta;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.EntityType;
import tech.metavm.object.instance.ClassInstance;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.rest.dto.ClassParamDTO;
import tech.metavm.object.meta.rest.dto.EnumConstantDTO;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Collections;
import java.util.List;

@EntityType("枚举类型")
public class EnumType extends ClassType {

    @ChildEntity("枚举值列表")
    private final Table<ClassInstance> enumConstants = new Table<>(ClassInstance.class, true);

    public EnumType(String name, @Nullable ClassType superType, boolean anonymous) {
        super(name,
                superType,
                TypeCategory.ENUM,
                anonymous,
                false,
                null);
    }

    public void addEnumConstant(ClassInstance enumConstant) {
        this.enumConstants.add(enumConstant);
    }

    public List<EnumConstantRT> getEnumConstants() {
        return Collections.unmodifiableList(
                NncUtils.map(
                        enumConstants, EnumConstantRT::new
                )
        );
    }

    public EnumConstantRT getEnumConstant(long id) {
        return new EnumConstantRT(NncUtils.filterOneRequired(
                enumConstants,
                opt -> opt.getId() == id,
                "选项ID不存在: " + id
        ));
    }

    @Override
    protected List<EnumConstantDTO> getExtra() {
        return NncUtils.map(
                getEnumConstants(),
                EnumConstantRT::toEnumConstantDTO
        );
    }

    public Table<ClassInstance> getDeclaredEnumConstants() {
        return enumConstants;
    }

}
