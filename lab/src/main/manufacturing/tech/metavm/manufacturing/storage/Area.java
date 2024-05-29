package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityType("库区")
public class Area {

    private final Warehouse warehouse;
    @EntityField("编码")
    private String code;
    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField("上级库区")
    private final @Nullable Area parent;
    @ChildEntity("下级库区")
    private final ChildList<Area> children = new ChildList<>();
    @ChildEntity("库位")
    private final ChildList<Position> positions = new ChildList<>();

    public Area(String code, String name, Warehouse warehouse, @Nullable Area parent) {
        this.code = code;
        this.name = name;
        this.warehouse = warehouse;
        warehouse.addArea(this);
        this.parent = parent;
        if (parent != null)
            parent.addChild(this);
    }

    public Warehouse getWarehouse() {
        return warehouse;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Area> getChildren() {
        return new ArrayList<>(children);
    }

    public List<Position> getPositions() {
        return new ArrayList<>(positions);
    }

    public void addPosition(Position position) {
        positions.add(position);
    }

    public void removePosition(Position position) {
        positions.remove(position);
    }

    public void addChild(Area area) {
        children.add(area);
    }

    public void removeChild(Area area) {
        children.remove(area);
    }

    public @Nullable Area getParent() {
        return parent;
    }
}
