package tech.metavm.object.meta;

import org.jetbrains.annotations.Nullable;
import tech.metavm.entity.EntityType;
import tech.metavm.util.NncUtils;
import tech.metavm.util.Table;

import java.util.Collections;
import java.util.List;

@EntityType("枚举类型")
public class EnumType extends ClassType {

    private final transient Table<EnumConstantRT> enumConstants = new Table<>(EnumConstantRT.class);

    public EnumType(String name, @Nullable ClassType superType, boolean anonymous) {
        super(name,
                superType,
                TypeCategory.ENUM,
                anonymous,
                false,
                null);
    }

    public List<EnumConstantRT> getEnumConstants() {
        return Collections.unmodifiableList(enumConstants);
    }

    public EnumConstantRT getEnumConstant(long id) {
        return NncUtils.filterOneRequired(
                enumConstants,
                opt -> opt.getId() == id,
                "选项ID不存在: " + id
        );
    }

    public Table<EnumConstantRT> getDeclaredEnumConstants() {
        return enumConstants;
    }

}
