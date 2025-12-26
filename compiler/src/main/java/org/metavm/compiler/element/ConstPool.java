package org.metavm.compiler.element;


import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.metavm.compiler.util.Traces;
import org.metavm.compiler.generate.KlassOutput;
import org.metavm.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class ConstPool {

    private final List<Constant> constants = new ArrayList<>();
    private final Map<Constant, Integer> element2index = new HashMap<>();
    private int nextIndex;
    private boolean frozen;

    public int put(@NotNull Constant c) {
        var idx = element2index.get(c);
        if (idx != null)
            return idx;
        if (frozen)
            throw new IllegalStateException("Constant pool is frozen");
        idx = nextIndex++;
        element2index.put(c, idx);
        constants.add(c);
        return idx;
    }

    public void write(KlassOutput output) {
        if (Traces.traceWritingClassFile)
            log.trace("Writing constant pool with {} entries", constants.size());
        output.writeList(constants, c -> {
            if (Traces.traceWritingClassFile)
                log.trace("Writing constant: {}", c);
            c.write(output);
        });
    }

    public void freeze() {
        Utils.require(!frozen);
        frozen = true;
    }

}
