package org.metavm.asm;

import org.metavm.asm.antlr.AssemblyParser;
import org.metavm.flow.Code;

import javax.annotation.Nullable;
import java.util.Objects;

public interface AsmCodeGenerator {

    void processBlock(AssemblyParser.BlockContext block, Code code);

    void enterScope(AsmScope scope);

    void exitScope();

    @Nullable AsmScope scope();

    default AsmScope scopeNotNull() {
        return Objects.requireNonNull(scope());
    }

}

