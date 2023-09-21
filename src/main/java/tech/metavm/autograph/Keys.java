package tech.metavm.autograph;


import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Key;
import tech.metavm.flow.Flow;
import tech.metavm.object.meta.ClassType;
import tech.metavm.object.meta.Field;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Keys {

    public static final Set<Key<?>> KEYS = new HashSet<>();
    
    private static <T> Key<T> createKey(String name) {
        var key = new Key<T>(name);
        KEYS.add(key);
        return key;
    }
    
    public static final Key<List<Definition>> DEFINITIONS = createKey("Definitions");

    public static final Key<QnAndMode> QN_AND_MODE = createKey("QnAndMode");

    public static final Key<AccessMode> ACCESS_MODE = createKey("AccessMode");

    public static final Key<Scope> SCOPE = createKey("Scope");

    public static final Key<Scope> COND_SCOPE = createKey("ConditionScope");

    public static final Key<Scope> BODY_SCOPE = createKey("BodyScope");

    public static final Key<Scope> ARGS_SCOPE = createKey("ArgsScope");

    public static final Key<Scope> ARGS_BODY_SCOPE = createKey("ArgsAndBodyScope");

    public static final Key<Scope> ELSE_SCOPE = createKey("ElseScope");

    public static final Key<Scope> ITERATE_SCOPE = createKey("IterateScope");

    public static final Key<Set<QualifiedName>> DEFINED_VARS_IN = createKey("DefinedVarsIn");

    public static final Key<Set<QualifiedName>> LIVE_VARS_IN = createKey("LiveVarsIn");

    public static final Key<Set<QualifiedName>> LIVE_VARS_OUT = createKey("LiveVarsOut");

    public static final Key<Boolean> READ_ONLY_EXPR = createKey("ReadOnlyExpression");

    public static final Key<ClassType> META_CLASS = createKey("MetaClass");

    public static final Key<Flow> FLOW = createKey("Flow");

    public static final Key<Field> FIELD = createKey("Field");

    public static final Key<Integer> RESOLVE_STAGE = createKey("ResolveStage");

    public static final Key<Integer> SYNTHETIC_VAR_CNT = createKey("SyntheticVarCnt");

    public static final Key<Document> META_CACHED_DOC = createKey("MetaCachedDoc");

    public static final Key<Set<String>> BLOCK_VARS = createKey("BlockVars");

    public static final Key<Block> CONTAINING_BLOCK = createKey("ContainingBlock");

    public static Set<Key<?>> getKeys() {
        return new HashSet<>(KEYS);
    }
    
}
