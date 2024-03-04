package tech.metavm.manufacturing.storage;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

@EntityType("库位")
public class Position {
    @EntityField("编码")
    private String code;
    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField("库区")
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
