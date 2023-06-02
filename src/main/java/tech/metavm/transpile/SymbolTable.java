//package tech.metavm.transpile;
//
//import tech.metavm.transpile.ir.*;
//import tech.metavm.util.InternalException;
//import tech.metavm.util.NncUtils;
//import tech.metavm.util.ReflectUtils;
//
//import javax.annotation.Nullable;
//import java.lang.reflect.Type;
//import java.util.*;
//
//public class SymbolTable {
//
//    @Nullable
//    private String packageName;
//    private final LinkedList<Scope> scopeStack = new LinkedList<>();
//    private final LinkedList<ClassScope> classStack = new LinkedList<>();
//    private final Map<String, ClassScope> className2Scope = new HashMap<>();
//    private final Set<String> definedTypeNames = new HashSet<>();
//    private final Map<String, String> className2qualifiedName = new HashMap<>();
//    private final SourceFile sourceFile = new SourceFile();
//    private final Map<String, ClassScope> classScopeMap = new HashMap<>();
//
//    public SymbolTable() {
//        scopeStack.push(sourceFile);
//    }
//
//    public void setPackageName(@SuppressWarnings("NullableProblems") String packageName) {
//        this.packageName = packageName;
//    }
//
//    public Symbol resolve(SymbolIdentifier identifier) {
//        return currentScope().resolve(identifier);
//    }
//
//    public Symbol resolve(Set<SymbolKind> kinds, String name) {
//        return currentScope().resolve(kinds, name);
//    }
//
//    public Symbol resolveMethod(String name, List<SourceType> parameterTypeNames) {
//        return resolve(SymbolIdentifier.method(name, parameterTypeNames));
//    }
//
//    public Symbol resolveVariable(String name) {
//        return resolve(SymbolIdentifier.variable(name));
//    }
//
//    public Symbol resolveType(String name) {
//        return resolve(SymbolIdentifier.type(name));
//    }
//
//    public Symbol resolveVariableOrType(String name) {
//        return resolve(EnumSet.of(SymbolKind.VARIABLE, SymbolKind.TYPE), name);
//    }
//
//    public IValueSymbol resolveValue(String name) {
//
//    }
//
//    public void defineField(IRField field) {
//
//    }
//
//    public void defineField(String name, Type type, boolean isStatic) {
//        currentClass().defineField(name, type, isStatic);
//    }
//
//    public void defineMethod(String name, List<SourceType> parameterTypes, SourceType returnType, boolean isStatic) {
//        currentClass().defineMethod(name, parameterTypes, returnType, isStatic);
//    }
//
//    public void addImport(Class<?> klass) {
//        sourceFile.addType(klass);
//        className2qualifiedName.put(klass.getSimpleName(), klass.getName());
//    }
//
//    public IRClass defineClass(IRClass irClass) {
//        return irClass;
//    }
//
//    public void defineType(IRType type) {
//        String qualifiedName;
//        if(classStack.isEmpty()) {
//            qualifiedName = packageName != null ? packageName + "." + name : name;
//            sourceFile.addType(ReflectUtils.classForName(qualifiedName));
//        }
//        else {
//            qualifiedName = currentClass().getName() + "$$" + name;
//            currentClass().defineType(name, isStatic);
//        }
//        className2qualifiedName.put(name, qualifiedName);
//    }
//
//    public void defineLocalVariable(String name, Type type) {
//        currentBlock().defineVariable(name, type);
//    }
//
//    public IRParameter defineParameter(IRParameter parameter) {
//        return parameter;
//    }
//
//    public void defineLocalVariable(LocalVariable localVariable) {
//
//    }
//
//    public void defineLocalType(String name) {
//        currentBlock().defineType(name);
//    }
//
//    public void addStaticImport(Class<?> klass, String importName) {
//        sourceFile.addStaticImport(klass, importName);
//    }
//
//    public void addStaticImportAll(Class<?> klass) {
//        sourceFile.addStaticImportAll(klass);
//    }
//
//    public ClassScope getClassScope(String className) {
//        return className2Scope.get(className);
//    }
//
//    public Scope currentScope() {
//        return NncUtils.requireNonNull(scopeStack.peek(), "Currently not in any scope");
//    }
//
//    public ClassScope currentClass() {
//        return NncUtils.requireNonNull(classStack.peek(), "Currently not in any class");
//    }
//
//    public BlockScope currentBlock() {
//        var scope = currentScope();
//        if(scope instanceof BlockScope block) {
//            return block;
//        }
//        throw new InternalException("Currently not in a block");
//    }
//
//    public boolean isInBlock() {
//        return !scopeStack.isEmpty() && scopeStack.peek() instanceof CodeBlock;
//    }
//
//    public void enterBlock(CodeBlock block) {
//        scopeStack.push(new BlockScope(currentScope(), block));
//    }
//
//    public void exitBlock() {
//        popScope();
//    }
//
//    public void enterClass(IRClass klass) {
//
//    }
//
//    public void enterClass(@Nullable String className, @Nullable String superName, boolean isStatic) {
//        var qualifiedName = getQualifiedName(className);
//        var scope = new ClassScope(
//                currentScope(),
//                qualifiedName,
//                isStatic,
//                superName != null ? getClassScope(superName) : null
//        );
//        scopeStack.push(scope);
//        classStack.push(scope);
//        if(qualifiedName != null) {
//            className2Scope.put(qualifiedName, scope);
//        }
//    }
//
////    public void addImport(String importText) {
////        int lastDotIdx = importText.lastIndexOf('.');
////        NncUtils.requirePositive(lastDotIdx);
////        var className = importText.substring(lastDotIdx + 1);
////        className2qualifiedName.put(className, importText);
////    }
//
//    public String getQualifiedName(String name) {
//        if(name == null) {
//            return null;
//        }
//        if(name.contains(".")) {
//            return name;
//        }
//
//        return NncUtils.requireNonNull(
//                className2qualifiedName.get(name),
//                "Can not get the qualified name for name '" + name + "'"
//        );
//    }
//
//    public void exitClass() {
//        popScope();
//        classStack.pop();
//    }
//
//    private void popScope() {
//        if(scopeStack.peek() == sourceFile) {
//            throw new InternalException("Scope stack underflow");
//        }
//        scopeStack.pop();
//    }
//
//    public ClassScope getScopeByClass(String type) {
//        return className2Scope.computeIfAbsent(
//                type,
//                k -> ClassScopeFactory.build(ReflectUtils.classForName(type))
//        );
//    }
//
//}
