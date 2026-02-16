package org.manul.compiler;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.manul.flow.VmStack;
import org.manul.object.instance.core.Value;
import org.manul.util.ApiNamedObject;
import org.manul.util.ObjectPool;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Regression test for stale tStack entries in pooled VmStack instances.
 * <p>
 * The VmStack RETURN/VOID_RETURN handlers clear {@code stack[]} via
 * {@code Arrays.fill(stack, base, base + frameSize, null)} but do NOT
 * clear {@code tStack[]} or {@code pStack[]}. When the VmStack is
 * returned to the pool and later reused, the stale type tags in
 * {@code tStack[]} cause {@code ensureValue()} to materialize values
 * from stale {@code pStack[]} data instead of returning null from
 * cleared {@code stack[]} slots.
 */
@Slf4j
public class VmStackPoolBugTest extends ManulTestBase {

    /**
     * Verifies that after a VmStack executes int-heavy bytecode and is
     * returned to the pool, its tStack contains stale non-zero type tags
     * in slots where stack[] has been cleared to null.
     * <p>
     * With the bug: tStack has stale T_INT/T_LONG/etc entries, causing
     * ensureValue() to materialize phantom values from stale pStack data.
     * <p>
     * After the fix: tStack is cleared alongside stack[], so ensureValue()
     * correctly returns null for cleared slots.
     */
    @SuppressWarnings("unchecked")
    public void testStaleTStackInPooledVmStack() throws Exception {
        // Deploy a Manul class with an int-heavy bean method
        deploy("manul/vmstack_pool_bug.mnl");

        var bean = ApiNamedObject.of("vmStackPoolBugLab");

        // Call the int-heavy method multiple times to ensure a VmStack
        // with stale T_INT entries is in the pool
        for (int i = 0; i < 5; i++) {
            callMethod(bean, "intHeavy", List.of(3, 4));
        }

        // Access the VmStack pool via reflection
        Field poolField = VmStack.class.getDeclaredField("pool");
        poolField.setAccessible(true);
        ObjectPool<Object> pool = (ObjectPool<Object>) poolField.get(null);

        // Borrow a VmStack from the pool
        Object vmStack = pool.borrowObject();
        Assert.assertNotNull("Pool should have a VmStack after method execution", vmStack);

        try {
            // Access the internal arrays via reflection
            Field stackField = VmStack.class.getDeclaredField("stack");
            stackField.setAccessible(true);
            Value[] stack = (Value[]) stackField.get(vmStack);

            Field tStackField = VmStack.class.getDeclaredField("tStack");
            tStackField.setAccessible(true);
            byte[] tStack = (byte[]) tStackField.get(vmStack);

            // Look for slots where tStack has a stale non-zero type tag
            // (T_INT=1, T_LONG=2, T_DOUBLE=3, T_FLOAT=4) but stack[] is null
            // (cleared by RETURN's Arrays.fill).
            // These are the dangerous slots: ensureValue() will read the stale
            // tStack tag and materialize a phantom value from stale pStack data.
            int stalePrimitiveCount = 0;
            int firstStaleSlot = -1;
            for (int i = 0; i < 1024; i++) {
                if (tStack[i] != 0 && stack[i] == null) {
                    stalePrimitiveCount++;
                    if (firstStaleSlot == -1) {
                        firstStaleSlot = i;
                    }
                }
            }

            // THE BUG: After RETURN clears stack[] but NOT tStack[],
            // stale primitive type tags remain. Any subsequent execution
            // reusing this VmStack could read these stale tags via ensureValue().
            Assert.assertEquals(
                    "Expected tStack to be fully cleared (no stale primitive tags) " +
                    "in slots where stack[] is null, but found " + stalePrimitiveCount +
                    " stale entries. First stale slot: " + firstStaleSlot +
                    " with tStack=" + (firstStaleSlot >= 0 ? tStack[firstStaleSlot] : "N/A") +
                    ". The RETURN handler must clear tStack[] alongside stack[].",
                    0,
                    stalePrimitiveCount
            );
        } finally {
            pool.returnObject(vmStack);
        }
    }

    /**
     * Verifies that ensureValue() on a stale slot returns a phantom value
     * materialized from stale pStack data, instead of null.
     * <p>
     * This directly demonstrates the consequence of the stale tStack bug:
     * ensureValue() trusts the stale type tag and creates a value from
     * garbage pStack data, rather than returning the actual stack[] value (null).
     */
    @SuppressWarnings("unchecked")
    public void testEnsureValueReturnsPhantomOnStaleSlot() throws Exception {
        deploy("manul/vmstack_pool_bug.mnl");

        var bean = ApiNamedObject.of("vmStackPoolBugLab");

        // Execute int-heavy code to poison tStack
        for (int i = 0; i < 5; i++) {
            callMethod(bean, "intHeavy", List.of(10, 20));
        }

        // Access the pool and borrow a VmStack
        Field poolField = VmStack.class.getDeclaredField("pool");
        poolField.setAccessible(true);
        ObjectPool<Object> pool = (ObjectPool<Object>) poolField.get(null);

        Object vmStack = pool.borrowObject();
        Assert.assertNotNull(vmStack);

        try {
            Field stackField = VmStack.class.getDeclaredField("stack");
            stackField.setAccessible(true);
            Value[] stack = (Value[]) stackField.get(vmStack);

            Field tStackField = VmStack.class.getDeclaredField("tStack");
            tStackField.setAccessible(true);
            byte[] tStack = (byte[]) tStackField.get(vmStack);

            // Find a slot with stale T_INT and null stack
            int staleSlot = -1;
            for (int i = 0; i < 1024; i++) {
                if (tStack[i] == 1 /* T_INT */ && stack[i] == null) {
                    staleSlot = i;
                    break;
                }
            }

            if (staleSlot == -1) {
                // No stale T_INT found - if the fix is applied, this is expected.
                // Test passes.
                return;
            }

            // Call ensureValue() via reflection on the stale slot
            Method ensureValue = VmStack.class.getDeclaredMethod("ensureValue", int.class);
            ensureValue.setAccessible(true);

            Value result = (Value) ensureValue.invoke(vmStack, staleSlot);

            // THE BUG: ensureValue sees tStack[staleSlot] = T_INT (stale),
            // reads pStack[staleSlot] (stale int data), and materializes
            // an IntValue. The correct behavior is to return null because
            // stack[staleSlot] is null (the slot was cleared by RETURN).
            Assert.assertNull(
                    "ensureValue() materialized a phantom value (" + result +
                    ") from stale pStack data at slot " + staleSlot +
                    ". The slot's stack[] is null (cleared by RETURN) but " +
                    "tStack[] still has T_INT=" + tStack[staleSlot] +
                    ". After the fix, tStack should be cleared to T_REF(0) " +
                    "and ensureValue should return null.",
                    result
            );
        } finally {
            pool.returnObject(vmStack);
        }
    }
}
