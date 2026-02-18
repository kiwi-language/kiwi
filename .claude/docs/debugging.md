# Manul Debugging & Known Issues

## Debugging Findings

### Circular Static Initialization (Feb 2026) - FIXED

**Symptom**: Tests fail intermittently on Temurin JDK 21 (GitHub Actions CI) but pass on GraalVM JDK 21 (local macOS). `IllegalArgumentException: Cannot add null value to ConstantPool` in `Index.<init>`.

**Stack trace**:
```
ConstantPool.addEntry → ConstantPool.addValue → Index.<init>
  → Klass__KlassBuilder__.build → StdKlassRegistry.getKlass
  → Klass.<clinit> → KlassBuilder.create → KlassBuilder.build
  → Type__KlassBuilder__.build → StdKlassRegistry.getKlass
  → Type.<clinit> → Types.getAnyType → ExpressionParserTest
```

**Root cause**: The `meta` module's `EntityTransformer` annotation processor injected a `public static final Klass __klass__` field into every `@Entity`/`@Value` class, eagerly initialized in `<clinit>` via `StdKlassRegistry.instance.getKlass(X.class)`. This created a circular `<clinit>` chain:

```
Types.getAnyType() → AnyType extends Type
  → Type.<clinit>: __klass__ = getKlass(Type.class)
    → Type__KlassBuilder__.build() → new Klass(...)
      → Klass.<clinit>: __klass__ = getKlass(Klass.class)
        → Klass__KlassBuilder__.build()
          → new Index(..., Types.getAnyType(), null)
            → Types.getAnyType() → AnyType.instance
              → Per JLS 12.4.2: Type is already initializing on this thread
              → Reentrant access returns normally with partially-init state
              → AnyType.instance may be null depending on JVM init order
```

**Why JVM-specific**: JLS 12.4.2 allows reentrant class initialization on the same thread. Different JVMs (GraalVM vs Temurin) initialize static fields in different orders, affecting whether `AnyType.instance` is set before the recursive access.

**Fix**: Changed `EntityTransformer` to generate lazy initialization:
- Field: `public static Klass __klass__;` (no `final`, no initializer)
- Added: `public static Klass __getKlass__() { if (__klass__ == null) __klass__ = StdKlassRegistry.instance.getKlass(X.class); return __klass__; }`
- Updated all accessor methods (`getInstanceKlass`, `getInstanceType`, `getValueType`) to call `__getKlass__()` instead of reading `__klass__` directly.

**Files changed**: `meta/src/main/java/org/manul/meta/processor/EntityTransformer.java`

**Note**: Classes that manually declare `__klass__` (HttpCookieImpl, HttpHeaderImpl, HttpRequestImpl, HttpResponseImpl, NeverExpression) also manually declare their accessor methods, so the transformer skips them entirely (no conflict). The `__klass__` field is never accessed externally as `SomeClass.__klass__` — only through generated instance methods.

---

## Gotchas & Edge Cases

### 1. Circular References
- Wire protocol has NO automatic cycle handling
- Must use entity references (IDs) to break cycles

### 2. Type Erasure vs. Reification
- Unlike Java, Manul preserves generic types at runtime
- Can do `instanceof List<String>` checks
- Cost: larger metadata footprint

### 3. Change Tracking Granularity
- Tracked at object-level (whole tree dirty flag)
- NOT field-level — updating one field saves entire tree

### 4. Index Consistency
- Index entries are separate from objects
- Indexes rebuilt on every update (expensive)

### 5. Lambda Serialization
- Lambdas compiled to classes, always serializable
- Closure captures increase serialized size

### 6. Null Handling
- Types can be nullable (`T?`) or non-null (default)
- NullPointerException if null assigned to non-null field

### 7. Schema Evolution
- Changing entity structure requires migration
- Version mismatch detected on load
- Manual field mapping (no automatic migration yet)

### 8. Bytecode Verification
- Unlike JVM: no bytecode verifier
- Malformed bytecode can crash VM
- Compiler guarantees correctness

### 9. Exception Handling Overhead
- `TRY_ENTER`/`TRY_EXIT` bytecodes add stack frame overhead
- Nested try blocks compound the cost

### 10. Integer Overflow
- Silent wraparound (like Java), no overflow detection

### 11. Hardcoded Batch Size
- `Utils.BATCH_SIZE = 3000` — large objects can cause memory pressure

### 12. Exception Swallowing
- Many `@SneakyThrows` throughout — can hide important errors

### 13. Static Initialization Order
- `@Entity`/`@Value` classes with complex static field hierarchies can trigger circular `<clinit>` issues
- The `__klass__` lazy init pattern (see above) was introduced to address this
- Be cautious when adding new `StdKlassRegistry` calls in static initializers

---

## Technical Debt

**Error Handling**: `@SneakyThrows` hides exceptions, some catch blocks swallow errors, no structured error hierarchy.

**Testing**: Compiler has extensive tests, runtime tests scattered, no integration test suite.

**Type System**: Union types partially implemented, intersection types only for bounds, no higher-kinded types.

**Concurrency**: Optimistic locking can cause contention, no distributed locking, thread-local storage used heavily (can leak).

**Scalability**: In-memory index limits size, batch size hardcoded, no horizontal scaling.

---

## CI/CD Debugging

**Test fails on CI but passes locally**:
- CI uses **Temurin JDK 21** on **ubuntu-latest**, local uses **GraalVM JDK 21** on **macOS**
- Different JVMs can have different class initialization ordering
- Check `gh run view <run-id> --log-failed` for CI logs
- Common: static initialization races (see circular init fix above)

**Compilation errors when running single test module**:
- `mvn test -Dtest=X -pl test` may fail with "Provider not found" for annotation processors
- Use `mvn verify -pl test -am` to build all dependencies first

**Docker testing** (for CI reproduction):
- `Dockerfile.test` exists for running tests in ubuntu+JDK21 environment
- Requires Docker daemon running: `docker build -f Dockerfile.test -t manul-test . && docker run manul-test`
