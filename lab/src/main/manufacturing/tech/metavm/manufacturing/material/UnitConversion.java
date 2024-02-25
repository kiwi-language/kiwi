package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("单位换算")
public class UnitConversion {

    @EntityField("X")
    private int x;

    @EntityField("Y")
    private int y;

    @EntityField("源单位")
    private Unit fromUnit;

    @EntityField("目标单位")
    private Unit toUnit;

    @EntityField("启用")
    private boolean enabled;

    @EntityField("物料")
    private Material material;

    public UnitConversion(int x, int y, Unit fromUnit, Unit toUnit, boolean enabled, Material material) {
        this.x = x;
        this.y = y;
        this.fromUnit = fromUnit;
        this.toUnit = toUnit;
        this.enabled = enabled;
        this.material = material;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public Unit getFromUnit() {
        return fromUnit;
    }

    public void setFromUnit(Unit fromUnit) {
        this.fromUnit = fromUnit;
    }

    public Unit getToUnit() {
        return toUnit;
    }

    public void setToUnit(Unit toUnit) {
        this.toUnit = toUnit;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public Material getMaterial() {
        return material;
    }

    public void setMaterial(Material material) {
        this.material = material;
    }
}
