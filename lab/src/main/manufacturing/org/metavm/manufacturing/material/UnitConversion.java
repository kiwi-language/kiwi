package org.metavm.manufacturing.material;

import org.metavm.api.EntityType;

@EntityType
public class UnitConversion {

    private int x;

    private int y;

    private Unit fromUnit;

    private Unit toUnit;

    private boolean enabled;

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
