package tech.metavm.autograph;

import java.lang.annotation.Repeatable;

@Repeatable(ExtraLoopTests.class)
public @interface ExtraLoopTest {

    String value();

}
