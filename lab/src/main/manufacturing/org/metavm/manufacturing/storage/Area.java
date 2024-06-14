package org.metavm.manufacturing.storage;

import org.metavm.entity.ChildEntity;
import org.metavm.entity.ChildList;
import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

@EntityType
public class Area {

    private final Warehouse warehouse;
    private String code;
    @EntityField(asTitle = true)
    private String name;
    private final @Nullable Area parent;
    @ChildEntity
    private final ChildList<Area> children = new ChildList<>();
    @ChildEntity
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
