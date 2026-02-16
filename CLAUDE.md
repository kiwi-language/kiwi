# Manul Project

Manul is a programming language with its own bytecode and stack-based VM interpreter (`VmStack.java`). It compiles `.mnl` source files to custom bytecode (not JVM bytecode) and executes them on a custom VM with object pooling.

## Building and Testing

- **Full build**: `mvn clean package -DskipTests`
- **Run all tests**: `mvn clean test` (from project root, NOT `-pl test` alone — the test module has broken files in `org.manul.context` and `org.manul.application` that only compile when built from root)
- **Run a specific test**: `mvn clean test -Dtest=ManulTest#testAssign -Dsurefire.failIfNoSpecifiedTests=false`
- **Resume from test module after root build**: Does NOT work with `-rf :manul-test` due to annotation processor issues; always build from root.
- CI runs: `mvn -B verify --file pom.xml`

## Writing Test Cases for Manul

### Test infrastructure

- Test base class: `org.manul.compiler.ManulTestBase` (extends JUnit 3 `TestCase`)
- Test classes must be in `test/src/test/java/org/manul/compiler/` package to access `ManulTestBase`'s package-private methods (`deploy()`, `callMethod()`, `saveInstance()`, `getObject()`, etc.)
- Manul source files go in `test/src/test/resources/manul/`

### ManulTestBase key methods

- `deploy("manul/foo.mnl")` — compiles and deploys a Manul source file
- `deploy(List.of("manul/a.mnl", "manul/b.mnl"))` — deploy multiple files
- `callMethod("ClassName", "methodName", List.of(arg1, arg2))` — call a static method on a class
- `callMethod(ApiNamedObject.of("beanName"), "methodName", List.of(...))` — call a method on a bean
- `callMethod(instanceId, "methodName", List.of(...))` — call an instance method
- `saveInstance("ClassName", Map.of("field1", val1, ...))` — create and persist an instance, returns `Id`
- `getObject(id)` — retrieve an `ApiObject` by Id
- `deleteObject(id)` — delete an instance
- `search(className, queryMap)` — search instances

### Manul language syntax (`.mnl` files)

```
package mypackage

// Bean (singleton service, accessed via ApiNamedObject.of("beanName") in tests)
@Bean
class MyService {
    fn doSomething(x: int) -> int {
        return x + 1
    }
}

// Regular class (instantiated via saveInstance in tests)
class Foo {
    // Fields are declared implicitly by constructor/init params
    fn getValue() -> int {
        return this.value
    }

    // Static method
    static fn add(a: int, b: int) -> int {
        return a + b
    }
}

// Inheritance
class Sub: Base {
    fn speak() -> string {
        return "sub"
    }
}
```

#### Types
- Primitives: `int`, `long`, `float`, `double`, `bool`, `short`, `byte`, `char`
- Reference: `string`, `any`, custom classes
- Nullable: `Type?` (e.g., `string?`)
- Arrays: `Type[]` (e.g., `int[]`, `string[]`)
- Functions: `(ParamType) -> ReturnType`
- Generics: `class Foo<T> { ... }`, called as `Foo<string>`

#### Control flow
- `if (cond) { ... } else { ... }`
- `for (i in collection) { ... }`
- `for (i in 0...n) { ... }` (range)
- `while (cond) { ... }`
- `do { ... } while (cond)`
- `break`, `continue`, `return`
- Ternary: `cond ? a : b`
- `try { ... } catch (e: ExceptionType) { ... }`

#### Features
- `@Bean` annotation for singleton beans
- Inner classes, anonymous classes, lambdas
- Enum classes with constants and methods
- Value classes (value objects)
- Method references
- Pattern matching with `is` (instanceof)
- Index annotations for searchable fields
- `@label("...")` display attributes

### Example test pattern

```java
// In test/src/test/java/org/manul/compiler/MyFeatureTest.java
package org.manul.compiler;

import org.junit.Assert;
import java.util.List;

public class MyFeatureTest extends ManulTestBase {

    public void testMyFeature() {
        deploy("manul/my_feature.mnl");
        var bean = ApiNamedObject.of("myService");
        var result = callMethod(bean, "compute", List.of(1, 2));
        Assert.assertEquals(3, result);
    }
}
```

### Accessing VM internals (for low-level tests)

VmStack uses a static `ObjectPool<VmStack>` pool. Use reflection to access:
- `VmStack.class.getDeclaredField("pool")` — the `ObjectPool<VmStack>`
- Fields on VmStack instance: `stack` (Value[]), `pStack` (long[]), `tStack` (byte[]), `frames` (Frame[])
- Private methods: `ensureValue(int pos)`, `pushValue(int pos, Value v)`, `materializeRange(int from, int to)`
- Type tags: T_REF=0, T_INT=1, T_LONG=2, T_DOUBLE=3, T_FLOAT=4

## Key Architecture

### VM (VmStack.java)
- Dual-stack architecture: `long[] pStack` for raw primitive bits, `Value[] stack` for references, `byte[] tStack` for type tags
- VmStack instances are pooled via `ObjectPool` with `ConcurrentLinkedQueue`
- `ensureValue(pos)` materializes a Value from the dual stack at boundary points
- Bytecodes defined in `Bytecodes.java` (~70 opcodes)

### Module structure
- `model/` — core VM, types, instances, flow (bytecodes, VmStack, Code, etc.)
- `compiler/` — Manul language compiler (.mnl -> bytecode)
- `server/` — HTTP server
- `test/` — integration tests
- `share/`, `api/`, `wire/`, `meta/`, `context/` — supporting modules
