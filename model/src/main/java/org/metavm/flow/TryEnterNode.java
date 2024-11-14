package org.metavm.flow;

import org.metavm.api.EntityType;
import org.metavm.entity.ElementVisitor;
import org.metavm.entity.LoadAware;
import org.metavm.flow.rest.Bytecodes;

import java.util.Objects;

@EntityType
public class TryEnterNode extends Node implements LoadAware {

    private transient TryExitNode exit;

    public TryEnterNode(String name, Node previous, Code code) {
        super(name, null, previous, code);
    }

    @Override
    public void writeContent(CodeWriter writer) {
        writer.write("try-enter");
    }

    @Override
    public int getStackChange() {
        return 0;
    }

    @Override
    public void writeCode(CodeOutput output) {
        output.write(Bytecodes.TRY_ENTER);
        output.writeShort(exit.getOffset());
    }

    @Override
    public int getLength() {
        return 3;
    }

    @Override
    public <R> R accept(ElementVisitor<R> visitor) {
        return visitor.visitTryEnterNode(this);
    }

    public TryExitNode getExit() {
        if(exit != null)
            return exit;
        int numEntries = 0;
        for(var n = getSuccessor(); n != null; n = n.getSuccessor()) {
            if(n instanceof TryEnterNode)
                numEntries++;
            else if(n instanceof TryExitNode e) {
                if(numEntries == 0) {
                    exit = e;
                    break;
                }
                numEntries--;
            }
        }
        return Objects.requireNonNull(exit, () -> "Cannot find exit for TryEnterNode " + getName());
    }

    @Override
    public void onLoad() {
        getExit();
    }

    void setExit(TryExitNode exit) {
        this.exit = exit;
    }
}
