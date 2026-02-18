# Manul Architecture Deep-Dive

## Module Details

### 1. SHARE - Foundation Layer

**Purpose**: Shared constants, utilities, DTOs used across all modules.

**Critical Constants** (`SymbolRefs.java`):
```java
KLASS = 1                    // Class references
FIELD = 2, METHOD = 3        // Member references
FUNCTION = 4                 // Function references
ENCLOSED_KLASS = 5           // Nested classes
TYPE_VARIABLE = 6            // Generic type parameters
CAPTURED_TYPE_VARIABLE = 7   // Closure captures
LAMBADA = 8                  // Lambda expressions
PARAMETER = 9, INDEX = 10    // Parameters and indices
```

**Flag Systems**:
- `KlassFlags`: ABSTRACT(1), STRUCT(2), SEARCHABLE(4), EPHEMERAL(8), ANONYMOUS(16), TEMPLATE(32)
- `FieldFlags`: STATIC(1), CHILD(2), READONLY(4), TRANSIENT(8), LAZY(16), ENUM_CONSTANT(32)
- `MethodFlags`: CONSTRUCTOR(8), ABSTRACT(16), STATIC(32), HIDDEN(64)

**Utils.java** (1789 lines):
- Collection operations: `map`, `filter`, `flatMap`, `merge`, `deduplicate`, `toMap`, `toMultiMap`
- Binary search, sorted list merging with custom comparators
- `BATCH_SIZE = 3000` (hardcoded)
- Uses Lombok `@SneakyThrows` extensively

**NamingUtils.java**: `camelToHyphen()`, `hyphenToCamel()`, `pathToName()`, `nameToPath()`, `escapeTypeName()`. Pluralization via `InflectUtil`.

---

### 2. WIRE - Custom Binary Serialization

**Variable-Length Encoding** (`DefaultWireInput.java`/`DefaultWireOutput.java`):
- Long: LSB=sign, 6 data bits, MSB=continuation. Max 10 continuation bytes.
- Char: Standard UTF-8 (1-3 bytes). Arrays: Length-prefixed.
- ~500 MB/sec throughput, 50-70% size reduction for small integers.

**Adapter Registry** (`AdapterRegistry.java`): Uses `ServiceLoader` for `WireAdapter` discovery. Maintains `clazz2adapter` and `tag2adapter` maps.

**Limitations**: Max 10 continuation bytes (overflow unchecked), no circular reference handling, no versioning support.

---

### 3. API - User-Facing Annotations

See main CLAUDE.md for annotation list.

**Special Types**: `Password` (hashed), `HttpRequest`, `HttpResponse`, `HttpHeader`, `HttpCookie`.

---

### 4. MODEL - Runtime Type System & Object Model (877+ files)

#### Type System (`org.manul.object.type.*`)

```
Type (interface)
  ├── PrimitiveType - NEVER, VOID, BOOL, BYTE, SHORT, CHAR, INT, LONG, FLOAT, DOUBLE, ANY
  ├── ClassType - User-defined classes with fields/methods/type parameters
  │   └── ClassInst - Generic instantiation (e.g., List<String>)
  ├── ArrayType - Element type + dimensions
  ├── FunctionType - Parameter types + return type
  ├── IntersectionType - Multiple bounds (e.g., <T extends A & B>)
  ├── UnionType - Sum types (partially implemented)
  ├── CapturedType - Wildcard capture
  ├── TypeVariable - Generic type parameter (<T>)
  └── ErrorType - Error recovery during compilation
```

**Klass.java** - Class metadata:
- `ConstantPool constantPool`, `List<Method> methods`, `Map<String, Field> fieldTable`
- `List<TypeVariable> typeParams`, `List<Index> indexes`, `List<Constraint> constraints`
- `int flags` (KlassFlags), `Klass superKlass`, `List<ClassType> interfaces`

**ConstantPool.java**: Indexed array of types/strings/numbers (like JVM constant pool). Frozen after compilation. Bytecode references by index.

#### Instance System (`org.manul.object.instance.core.*`)

**Instance.java** - Base interface: `getId()`, `state()`, `getInstanceType()`, `getRoot()/getParent()`, `getVersion()`, lifecycle checks, tree/graph traversal.

**InstanceState.java** - Tracks: id, version, syncVersion, nextNodeId, isEphemeral/isNew/isRemoved/isMarked/isChangeNotified/isRemovalNotified, context, doubly-linked list pointers.

**ClassInstance.java** - Field access: `getField(Field)`, `setField(Field, Value)`, `forEachField()`, `buildSource()` (search indexing).

**Value Types**: Primitives (`LongValue`, `IntValue`, etc.), References (`EntityReference`, `ValueReference`, `StringReference`), `ArrayInstance`, `FlowValue` (method ref with closure), `NullValue`.

**ID System** (`Id.java`): Sealed interface - `PhysicalId(treeId, nodeId)`, `TmpId` (random 53-bit), `MockId`, `TypeId`, `NullId`. Root gets `(treeId, 0)`, children get `(treeId, nextNodeId++)`.

#### Flow/Bytecode System (`org.manul.flow.*`)

**Flow.java**: `name`, `parameters`, `returnType`, `code` (bytecode), `constantPool`, `typeParameters`, `capturedTypeVariables`, `lambdas`, `klasses`.

**Two implementations**: `StdFlow` (user-defined, interpreted bytecode), `NativeFlow` (Java-backed via `NativeFunction`).

**Code.java**: `List<Node> nodes` (CFG), `Map<String, Node> nodeMap`, `List<Variable> locals`, `maxLocals`, `maxStack`.

**Bytecode Instructions** (`Bytecodes.java`):
- Object: GET_FIELD, SET_FIELD, GET_STATIC_FIELD, SET_STATIC, GET_METHOD, GET_STATIC_METHOD
- Creation: NEW, NEW_CHILD, NEW_ARRAY, NEW_ARRAY_WITH_DIMS
- Invoke: INVOKE_VIRTUAL/SPECIAL/STATIC, GENERIC_INVOKE_*, INVOKE_FUNCTION
- Array: GET_ELEMENT, SET_ELEMENT, ADD_ELEMENT, DELETE_ELEMENT, ARRAY_LENGTH, CLEAR_ARRAY
- Control: GOTO, IF_EQ, IF_NE, TABLE_SWITCH, LOOKUP_SWITCH, RETURN, VOID_RETURN, RAISE
- Stack: LOAD, STORE, DUP, DUP_X1, DUP_X2, DUP2, POP, LOAD_CONSTANT, LOAD_KLASS, LOAD_PARENT
- Arithmetic: INT/LONG/FLOAT/DOUBLE_ADD/SUB/MUL/DIV/REM/NEG, shifts, bitwise
- Comparison: EQ, NE, LT, LE, GT, GE, type-specific compares, REF_COMPARE_EQ/NE
- Type: CAST, INSTANCE_OF, conversions (INT_TO_LONG, etc.)
- Index: INDEX_SCAN, INDEX_COUNT, INDEX_SELECT, INDEX_SELECT_FIRST
- Exception: TRY_ENTER, TRY_EXIT
- Special: DELETE, COPY, ID, NON_NULL, NOOP

**CFG**: Directed graph of `Node` objects. `BranchNode` (true/false), `GotoNode`, `ReturnNode`.

**Execution**: Stack-based VM, ~10x slower than JIT-compiled JVM, fast startup.

#### Entity/Persistence Layer

**Entity.java**: Base class for persistent objects. `getParentEntity()`, `getRootEntity()`, `nextChildId()`, `buildSource()`.

**Tree structure**: Objects form trees. Root owns entire tree. Children share root's treeId. Reference fields can point outside tree.

**Change tracking**: `InstanceState` tracks new/modified/deleted. `ChangeLog` accumulates. Flushed on commit. Granularity: whole tree.

---

### 5. COMPILER - Source to Bytecode

Multi-pass compiler (javac-like):

**Phase 1 - Lexing/Parsing**: Hand-written `Lexer` + recursive descent `Parser` → AST.

**AST Nodes** (`org.manul.compiler.syntax.*`):
- Declarations: ClassDecl, MethodDecl, FieldDecl, ParamDecl, EnumConstDecl, LocalVarDecl, TypeVariableDecl
- Statements: BlockStmt, ExprStmt, IfStmt, WhileStmt, DoWhileStmt, ForStmt, ForeachStmt, SwitchStmt, TryStmt, ThrowStmt, RetStmt, BreakStmt, ContinueStmt, LabeledStmt
- Expressions: BinaryExpr, PrefixExpr, PostfixExpr, Call, SelectorExpr, IndexExpr, CastExpr, NewArrayExpr, AnonClassExpr, LambdaExpr, Literal, Ident, CondExpr

**Phase 2 - Symbol Entry** (`Enter.java`): Walks AST, creates `Element` objects (Clazz, Method, Field, Param, LocalVar, EnumConst, TypeVar, Lambda, Package). Populates symbol tables.

**Phase 3 - Attribution** (`Attr.java`): Bidirectional type checking (top-down expected + bottom-up actual). Generic type inference. Error recovery via `ErrorType`/`ErrorElement`. Resolver pattern: `ImmediateResolver`, `PendingResolver`, `NullResolver`.

**Phase 4 - Lowering** (`Lower.java`): Field initializers → constructors. Enum expansion. Lambda lowering (captures). Super calls. Static initializers → `<clinit>`.

**Phase 5 - Code Generation** (`Gen.java`): Emits bytecode. Stack item tracking at compile time. Constant pool building. Writes `.mvclass` files via `ClassFileWriter`.

**Key Files**:
- Entry: `/compiler/src/main/java/org/manul/compiler/Main.java`
- Parser: `/compiler/src/main/java/org/manul/compiler/syntax/Parser.java`
- Code Gen: `/compiler/src/main/java/org/manul/compiler/generate/Gen.java`
- Writer: `/compiler/src/main/java/org/manul/compiler/generate/ClassFileWriter.java`
- Reader: `/model/src/main/java/org/manul/classfile/ClassFileReader.java`

---

### 6. SERVER - Runtime Execution Engine

**InstanceStore.java**: `save()`, `getVersions()`, `loadForest()`, `getIndexEntriesByKeys()`, `saveIndexEntries()`.

**Storage**: PostgreSQL tables `instance` (one row per tree), `index_entry` (one row per indexed field value). MyBatis for SQL. Batch max 3000.

**Optimistic Locking**: Load → version1, modify, save → check version == version1, mismatch → rollback/retry.

**Index System**: `@Index` annotation, materialized as table rows. Supports unique, multi-column, range, full-text.

**API Controller**: Catch-all `/**` routes → `ApiAdapter.handleGet/Post/Patch/Delete` → `InstanceContext` → `EntityRepository` → `Flow` execution → persist → search update → JSON response.

**Key Files**: `ObjectApplication.java`, `ApiController.java`, `DeployService.java`

**Native Methods** (`org.manul.entity.natives.*`): `ArrayNative`, `ExceptionNative`, `NativeMethods`, `StdFunction`.

---

### 7. CONTEXT - Dependency Injection

Spring-like DI container with compile-time processing.

**Annotations**: `@Component`, `@Configuration`, `@Bean`, `@Autowired`, `@Qualifier`, `@Primary`, `@Value("${...}")`, `@Controller`, HTTP methods, `@Init`, `@DisposableBean`, `@Scheduled(cron="...")`, `@Transactional`.

**ApplicationContext**: Lazy init, circular dependency detection, proxy generation for `@Transactional`.

**ContextProcessor**: Scans annotated classes → generates `BeanDefinition` metadata → resolves dependency graph → generates factory code.

---

### 8. DIST - CLI and Deployment

**CLI Commands**: `compile`, `deploy`, `migrate`, `server`.

**Build Process**: Scan → compile to `.mvclass` → package into `.mva` → deploy to server.

---

## Compilation & Runtime Flow

**Compilation**: `.mnl` → `CompilationTask` → `Parser` (AST) → `Enter` (symbols) → `TypeResolver` → `Attr` (types) → `Check` (semantics) → `Lower` (desugar) → `Gen` (bytecode) → `ClassFileWriter` (`.mvclass`) → `.mva` archive.

**Runtime**: `manul deploy` → `DeployService` → `ClassFileReader` → `Klass` creation → schema updates → ES index updates → REST endpoints.

**Request Flow**: HTTP → `ApiController` → `ApiAdapter` → `InstanceContext` → `EntityRepository` → `Flow` interpreter → persist → search → JSON.

---

## Key Algorithms

**Index Queries**: Lookup by index key → get IDs → load objects. In-memory index via `EntityMemoryIndex` (`Map<IndexKey, Set<Id>>`).

**Transaction Isolation**: READ_UNCOMMITTED, READ_COMMITTED, REPEATABLE_READ, SERIALIZABLE.

**Transaction Propagation**: REQUIRED, REQUIRES_NEW, NESTED, SUPPORTS, NOT_SUPPORTED, NEVER, MANDATORY.

---

## Performance

- **Parsing**: ~1000 lines/sec
- **Type Checking**: O(n^2) worst case (nested generics)
- **Code Gen**: O(n) in method size
- **Wire**: ~500 MB/sec, 50-70% smaller than Java serialization
- **Bytecode**: ~10x slower than JIT, fast startup
- **DB Batch**: 3000 objects/batch, O(log n) indexed queries
- **Memory**: ~100 bytes/object, ~50 bytes/index entry

---

## Development Patterns

**Visitor Pattern**: Used everywhere - AST (`AbstractNodeVisitor`), bytecode (`VoidStructuralVisitor`), types (`Type.Visitor`).

**Builder Pattern**: `ClassInstanceBuilder.newBuilder(type, id).data(fields).parent(parent).build()`.

**Immutable Lists** (compiler): `List<T> list = List.nil(); list = list.prepend(item);` — structural sharing.

---

## Example Manul Code

```manul
class Product(var name: string) {
    class SKU(
        var variant: string,
        var price: double,
        var stock: int
    ) {}
}

@Bean
class UserService {
    fn getUserByName(name: string) -> UserDTO {
        return UserDTO(name, "12312312312")
    }
}
```

---

## Comparisons

**vs. Java/JVM**: Custom VM, persistence built-in, simpler type system. Less mature tooling, tighter integration.

**vs. Hibernate/JPA**: Bytecode-level integration, tree storage model. Better for tree queries, less flexible for complex relations.

**vs. Spring**: Compile-time DI (no reflection), simpler model. Faster startup, less dynamic.
