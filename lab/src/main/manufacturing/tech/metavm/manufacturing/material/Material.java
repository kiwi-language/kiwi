package tech.metavm.manufacturing.material;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType("物料")
public class Material {

    @EntityField("编码")
    private String code;

    @EntityField(value = "名称", asTitle = true)
    private String name;

    @EntityField("单位")
    private Unit unit;

    @ChildEntity("单位转换")
    private final ChildList<UnitConversion> unitConversions = new ChildList<>();

    @EntityField("辅助单位")
    private @Nullable Unit auxiliaryUnit;

    @EntityField("物料类型")
    private MaterialKind kind = MaterialKind.NORMAL;

    @EntityField("是否启用批次")
    private boolean enableBatch;

    @ChildEntity("物料属性")
    private final ChildList<MaterialAttribute> attributes = new ChildList<>();

    public Material(String code, String name, Unit unit) {
        this.code = code;
        this.name = name;
        this.unit = unit;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Unit getUnit() {
        return unit;
    }

    public ChildList<UnitConversion> getUnitConversions() {
        return unitConversions;
    }

    public Unit getAuxiliaryUnit() {
        return auxiliaryUnit;
    }

    public void addAttribute(MaterialAttributeKey key, Object value) {
        attributes.add(new MaterialAttribute(key, value));
    }

}
