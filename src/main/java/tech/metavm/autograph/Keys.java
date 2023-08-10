package tech.metavm.autograph;


import com.intellij.openapi.util.Key;

import java.util.List;
import java.util.Set;

public class Keys {

    public static final Key<List<Definition>> DEFINITIONS = Key.create("Definitions");

    public static final Key<QnAndMode> QN_AND_MODE = Key.create("QnAndMode");

    public static final Key<AccessMode> ACCESS_MODE = Key.create("AccessMode");

    public static final Key<Scope> SCOPE = Key.create("Scope");

    public static final Key<Scope> COND_SCOPE = Key.create("ConditionScope");

    public static final Key<Scope> BODY_SCOPE = Key.create("BodyScope");

    public static final Key<Scope> ARGS_SCOPE = Key.create("ArgsScope");

    public static final Key<Scope> ARGS_BODY_SCOPE = Key.create("ArgsAndBodyScope");

    public static final Key<Scope> ELSE_SCOPE = Key.create("ElseScope");

    public static final Key<Scope> ITERATE_SCOPE = Key.create("IterateScope");

    public static final Key<Set<QualifiedName>> DEFINED_VARS_IN = Key.create("definedVarsIn");

    public static final Key<Set<QualifiedName>> LIVE_VARS_IN = Key.create("liveVarsIn");

    public static final Key<Set<QualifiedName>> LIVE_VARS_OUT = Key.create("liveVarsOut");

}
