package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType
public class Position {
    private String code;
    @EntityField(asTitle = true)
    private String name;
    private final Area area;

    public Position(String code, String name, Area area) {
        this.code = code;
        this.name = name;
        this.area = area;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Area getArea() {
        return area;
    }

    public Warehouse getWarehouse() {
        return area.getWarehouse();
    }

}
