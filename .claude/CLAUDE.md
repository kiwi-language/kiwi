# Manul - Quick Reference

Manul is a full-stack application framework integrating language, runtime, and persistence. Compiles `.mnl` files to custom `.mvclass` bytecode, runs on a stack-based VM, persists objects to PostgreSQL, indexes to Elasticsearch, auto-generates REST APIs.

For detailed docs, see `.claude/docs/`:
- **architecture.md** - Module deep-dives, type system, bytecode, compiler phases, patterns
- **operations.md** - Dev environment, deployment, Elasticsearch, Aliyun OSS, CI/CD
- **debugging.md** - Gotchas, known issues, debugging findings

---

## Module Dependency Graph

```
share (base utilities)
  ↓
api (annotations) → wire (serialization) → meta (annotation processing)
  ↓                    ↓                      ↓
  → model (runtime: types, entities, Flow VM) ←──┘
      ↓
  compiler (Manul .mnl → .mvclass bytecode)
      ↓
  context (dependency injection container)
      ↓
  server (HTTP server, persistence, schema)
      ↓
  dist (CLI distribution package)
```

---

## Build & Test

```bash
# Full build
mvn clean install

# Build skipping tests
mvn clean install -DskipTests

# Run all tests (CI command)
mvn -B verify --file pom.xml

# Run specific test module
mvn verify -pl test -am

# Build distribution package
mvn clean install -DskipTests -pl dist -am
```

**Requirements**: Java 21, Maven 3.x, PostgreSQL (persistent mode), Elasticsearch (search)

**Dependencies**: Lombok, SLF4J, HikariCP, MyBatis, SnakeYAML, custom JSONK (v0.0.3)

---

## Configuration

```yaml
# manul.yml structure
mode: memory|persistent
datasource:
    host: 127.0.0.1
    port: 5432
    username: postgres
    password: ***
    database: manul
es:
    host: localhost
    port: 9200
    user: elastic
    password: ***
server:
  port: 8080
```

**Config file locations** (multiple!):
1. **Source repo**: `./manul.yml` — mode: `memory`, db: `manul` (for testing)
2. **Installed server**: `~/.manul/conf/manul.yml` — mode: `persistent`, db: `kiwi`
3. **System-wide**: `/etc/manul/manul.yml` — used when running from IntelliJ IDE

---

## File Extensions

- `.mnl` - Manul source files
- `.mvclass` - Compiled bytecode (custom format, NOT JVM `.class`)
- `.mva` - Manul archive (compiled project)

---

## Testing

- Test module: `/test/`
- Test resources: `/test/src/test/resources/`
- Compiler tests: `/test/src/test/resources/basics/`
- CI uses Temurin JDK 21 on ubuntu-latest

---

## REST API Pattern

```
POST   /{app-name}/{class-name}      - Create
GET    /{app-name}/{class-name}/:id  - Read
PATCH  /{app-name}/{class-name}/:id  - Update
DELETE /{app-name}/{class-name}/:id  - Delete
```

---

## Key Annotations

- `@Entity` - Persistent entity class
- `@EntityField` - Field metadata (title, readonly, lazy, etc.)
- `@EntityFlow` - Method becomes Flow (bytecode function)
- `@Value` / `@ValueObject` - Immutable value types
- `@Wire` - Binary serialization support
- `@Component` / `@Configuration` / `@Bean` - DI
- `@Index` - Database index definition
- `@Resource` - REST endpoint
- `@Generated` - Skip compilation

---

## Annotation Processors (meta module)

The `meta` module runs annotation processors during Java compilation that transform `@Entity`/`@Value` classes:

- **EntityTransformer**: Injects `__klass__` static field and `__getKlass__()` lazy accessor into annotated classes. Also generates `getInstanceKlass()`, `getInstanceType()`, `getValueType()` methods.
- **KlassBuilder generator**: Generates `ClassName__KlassBuilder__` classes that register class metadata with `StdKlassRegistry` via `ServiceLoader`.
- **WireProcessor**: Generates serialization/deserialization adapters for `@Wire`-annotated classes.
- **ContextProcessor**: Generates `BeanDefinition` metadata for DI container.

**Important**: The `__klass__` field uses lazy initialization (not eager `static final`) to avoid circular static initialization between `Type`, `Klass`, and `AnyType`. See `.claude/docs/debugging.md` for details.

---

## Critical Architecture Notes

- Objects form **trees** (parent-child). Root owns the tree. All children share root's `treeId`.
- Change tracking is **tree-level** (whole tree dirty), not field-level.
- Optimistic locking via version counter per tree.
- `ConstantPool` - indexed array of types/strings/numbers, like JVM constant pool.
- Two Flow types: `StdFlow` (bytecode, interpreted) and `NativeFlow` (Java-backed).
- Custom binary Wire protocol - no circular reference handling, no versioning.
- `Utils.BATCH_SIZE = 3000` (hardcoded) - watch for memory with large objects.

---

## Recent Changes

**Circular static init fix** (Feb 2026):
- Fixed `__klass__` field in EntityTransformer from eager to lazy initialization
- Root cause: circular `<clinit>` chain between Type, Klass, AnyType classes
- Manifested only on Temurin JDK 21 (CI), not GraalVM (local)

**PR #117**: Fix API child object updates
**PR #116**: REST-Compliant API (removed `/api` prefix, plural nouns, PATCH with ID)
**PR #115**: Nested Transaction Propagation
