package org.metavm.manufacturing.storage;

import org.metavm.entity.ChildEntity;
import org.metavm.entity.ChildList;
import org.metavm.entity.EntityField;
import org.metavm.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

@EntityType
public class Warehouse {
    private String code;
    @EntityField(asTitle = true)
    private String name;
    @ChildEntity
    private final ChildList<Area> areas = new ChildList<>();
    @ChildEntity
    private final List<Position> positions = new ArrayList<>();

    public Warehouse(String code, String name) {
        this.code = code;
        this.name = name;
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

    public List<Area> getAreas() {
        return new ArrayList<>(areas);
    }

    public List<Position> getPositions() {
        return new ArrayList<>(positions);
    }

    public void addArea(Area area) {
        areas.add(area);
    }

    public void removeArea(Area area) {
        areas.remove(area);
    }

    public void addPosition(Position position) {
        positions.add(position);
    }

    public void removePosition(Position position) {
        positions.remove(position);
    }
}
