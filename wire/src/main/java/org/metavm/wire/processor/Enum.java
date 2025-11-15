package org.metavm.wire.processor;

import com.sun.tools.javac.code.Symbol;

import javax.annotation.Nullable;

public record Enum(
        Symbol.ClassSymbol symbol,
        @Nullable Symbol.MethodSymbol codeMethod,
        @Nullable Symbol.MethodSymbol fromCodeMethod
        ) {
}
