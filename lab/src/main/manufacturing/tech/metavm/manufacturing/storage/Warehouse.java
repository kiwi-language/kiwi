package tech.metavm.manufacturing.storage;

import tech.metavm.entity.ChildEntity;
import tech.metavm.entity.ChildList;
import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import java.util.ArrayList;
import java.util.List;

@EntityType("仓库")
public class Warehouse {
    @EntityField("编码")
    private String code;
    @EntityField(value = "名称", asTitle = true)
    private String name;
    @ChildEntity("库区")
    private final ChildList<Area> areas = new ChildList<>();
    @ChildEntity("库位")
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
