package tech.metavm.manufacturing.material;

import tech.metavm.entity.EntityField;
import tech.metavm.entity.EntityType;

import javax.annotation.Nullable;

@EntityType("单位")
public class Unit {
    @EntityField("编码")
    private String code;
    @EntityField(value = "名称", asTitle = true)
    private String name;
    @EntityField("舍入规则")
    private RoundingRule roundingRule;
    @EntityField("精度")
    private int precision;
    @EntityField("备注")
    private @Nullable String notes;

    public Unit(String code, String name, RoundingRule roundingRule, int precision, @Nullable String notes) {
        this.code = code;
        this.name = name;
        this.roundingRule = roundingRule;
        this.precision = precision;
        this.notes = notes;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public RoundingRule getRoundingRule() {
        return roundingRule;
    }

    public int getPrecision() {
        return precision;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setRoundingRule(RoundingRule roundingRule) {
        this.roundingRule = roundingRule;
    }

    public void setPrecision(int precision) {
        this.precision = precision;
    }

    public @Nullable String getNotes() {
        return notes;
    }

    public void setNotes(@Nullable String notes) {
        this.notes = notes;
    }
}
