package org.metavm.manufacturing.storage;

import org.metavm.api.Entity;
import org.metavm.api.EntityField;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Warehouse {
    private String code;
    @EntityField(asTitle = true)
    private String name;
    private final List<Area> areas = new ArrayList<>();
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
