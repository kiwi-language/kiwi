package org.metavm.asm;

import lombok.extern.slf4j.Slf4j;
import org.metavm.object.type.Type;
import org.metavm.util.NncUtils;

@Slf4j
record AsmVariable(String name, int index, Type type, AsmCallable callable) {

    public int getContextIndex(AsmCallable callable) {
        int idx = -1;
        AsmScope c = callable;
        while (c instanceof AsmCallable && c != this.callable) {
            c = c.parent();
            idx++;
        }
        NncUtils.requireTrue(c == this.callable, () -> "Cannot find context for variable " + name);
        return idx;
    }

}
