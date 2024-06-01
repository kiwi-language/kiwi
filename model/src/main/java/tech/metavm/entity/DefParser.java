package tech.metavm.entity;

import tech.metavm.object.instance.core.DurableInstance;
import tech.metavm.object.type.ResolutionStage;

public abstract class DefParser<T,I extends DurableInstance,D extends ModelDef<T,I>> {

    private ResolutionStage stage = ResolutionStage.INIT;

    public abstract D create();

    public abstract D get();

    //<editor-fold desc="generators">

    /*
    Important: the generator methods are allowed to call the generator methods of other def parsers
    with the constraint that:
        either call the generator method of a previous stage
        or call the generator method of the same stage without circular invocation
     */

    public abstract void generateSignature();

    public abstract void generateDeclaration();

    public abstract void generateDefinition();
    //</editor-fold>

    public final ResolutionStage getState() {
        return stage;
    }

    public final ResolutionStage setStage(ResolutionStage stage) {
        var oldStage = this.stage;
        if(stage.isAfter(oldStage))
            this.stage = stage;
        return oldStage;
    }

}
