package org.metavm.manufacturing.material;

import org.metavm.api.EntityField;
import org.metavm.api.EntityType;

import javax.annotation.Nullable;

@EntityType
public class Unit {
    private String code;
    @EntityField(asTitle = true)
    private String name;
    private RoundingRule roundingRule;
    private int precision;
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
