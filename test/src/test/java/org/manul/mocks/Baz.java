package org.manul.mocks;

import org.manul.api.Entity;
import org.manul.wire.Wire;
import org.manul.object.instance.core.Id;
import org.manul.object.instance.core.Instance;
import org.manul.object.instance.core.Reference;
import org.manul.util.Utils;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

@Wire(97)
@Entity
public class Baz extends org.manul.entity.Entity {

    private List<Reference> bars = new ArrayList<>();

    @Nullable
    @Override
    public org.manul.entity.Entity getParentEntity() {
        return null;
    }

    public Baz(Id id) {
        super(id);
    }

    public Baz(Id id, List<Bar> bars) {
        super(id);
        setBars(bars);
    }

    public List<Bar> getBars() {
        return Utils.map(bars, r -> (Bar) r.get());
    }

    public void setBars(List<Bar> bars) {
        this.bars = Utils.map(bars, Instance::getReference);
    }

    @Override
    public void forEachReference(Consumer<Reference> action) {
        for (var bars_ : bars) action.accept(bars_);
    }

    @Override
    public void forEachChild(Consumer<? super Instance> action) {
    }

}
