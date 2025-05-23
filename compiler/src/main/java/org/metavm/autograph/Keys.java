package org.metavm.autograph;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.util.Key;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiMethod;
import org.metavm.object.type.*;

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

    public static final Key<Scope> RESOURCE_SCOPE = createKey("ResourceScope");

    public static final Key<Scope> BODY_SCOPE = createKey("BodyScope");

    public static final Key<Scope> FINALLY_SCOPE = createKey("FinallyBlock");

    public static final Key<Scope> ARGS_SCOPE = createKey("ArgsScope");

    public static final Key<Scope> ARGS_BODY_SCOPE = createKey("ArgsAndBodyScope");

    public static final Key<Scope> ELSE_SCOPE = createKey("ElseScope");

    public static final Key<Scope> ITERATE_SCOPE = createKey("IterateScope");

    public static final Key<Set<QualifiedName>> DEFINED_VARS_IN = createKey("DefinedVarsIn");

    public static final Key<Set<QualifiedName>> LIVE_VARS_IN = createKey("LiveVarsIn");

    public static final Key<Set<QualifiedName>> LIVE_VARS_OUT = createKey("LiveVarsOut");

    public static final Key<Boolean> READ_ONLY_EXPR = createKey("ReadOnlyExpression");

    public static final Key<Klass> MV_CLASS = createKey("MetaClass");

    public static final Key<TypeVariable> TYPE_VARIABLE = createKey("TypeVariable");

    public static final Key<org.metavm.flow.Method> Method = createKey("Method");

    public static final Key<Field> FIELD = createKey("Field");

    public static final Key<Integer> SYNTHETIC_VAR_CNT = createKey("SyntheticVarCnt");

    public static final Key<Document> META_CACHED_DOC = createKey("MetaCachedDoc");

    public static final Key<Set<String>> BLOCK_VARS = createKey("BlockVars");

    public static final Key<Block> CONTAINING_BLOCK = createKey("ContainingBlock");

    public static final Key<Index> INDEX = createKey("Index");

    public static final Key<Boolean> ADDED_STATIC_MODIFIER = createKey("AddedStaticModifier");

    public static final Key<Boolean> DISCARDED = Key.create("Discarded");

    public static final Key<PsiClass> SUBSTITUTION = Key.create("Substitution");

    public static final Key<String> ORIGINAL_NAME = Key.create("OriginalName");

    public static final Key<Boolean> INNER_CLASS_COPY = Key.create("InnerClassCopy");

    public static final Key<Integer> MAX_SYNTHETIC_CLASS_SEQ = Keys.createKey("MaxSyntheticClassSeq");

    public static final Key<Integer> VARIABLE_INDEX = Keys.createKey("VariableIndex");

    public static final Key<Integer> MAX_LOCALS = Keys.createKey("MaxLocals");

    public static final Key<Integer> NAX_STACK = Keys.createKey("MaxStack");

    public static final Key<Integer> CASE_INDEX = Keys.createKey("CaseIndex");

    public static final Key<PsiMethod> INITIALIZER = Keys.createKey("Initializer");

    public static final Key<List<PsiField>> ENUM_CONSTANTS = Keys.createKey("EnumConstants");

    public static final Key<Integer> ENUM_CONSTANT_COUNT = Keys.createKey("EnumConstantCount");

    public static final Key<String> ANONYMOUS_CLASS_NAME = Keys.createKey("AnonymousClassName");

    public static final Key<Integer> ORDINAL = Keys.createKey("Ordinal");

    public static Set<Key<?>> getKeys() {
        return new HashSet<>(KEYS);
    }

}
