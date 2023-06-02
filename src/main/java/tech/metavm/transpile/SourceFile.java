package tech.metavm.transpile;

import tech.metavm.util.ReflectUtils;

import java.lang.reflect.Field;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SourceFile extends ScopeBase {

    private final Map<SymbolIdentifier, Symbol> variables = new LinkedHashMap<>();

    public SourceFile() {
        super(null);
    }

    public void addType(Class<?> klass) {
        var symbol = new Symbol(SymbolIdentifier.type(klass.getSimpleName()), null, klass.getSimpleName(), null);
        variables.put(symbol.identifier(), symbol);
    }

    public void addStaticImport(Class<?> klass, String name) {
        var field = ReflectUtils.getStaticField(klass, name);
        addVariable(field);
    }

    public void addStaticImportAll(Class<?> klass)  {
        List<Field> staticFields = ReflectUtils.getStaticFields(klass);
        for (Field staticField : staticFields) {
            addVariable(staticField);
        }
    }

    private void addVariable(Field field) {
        var identifier = SymbolIdentifier.variable(field.getName());
        variables.put(
                identifier,
                new Symbol(
                        identifier,
                        field.getDeclaringClass().getSimpleName(),
                        field.getName(),
                        SourceTypeUtil.fromClass(field.getType())
                )
        );
    }

    @Override
    protected Symbol resolveSelf(SymbolIdentifier identifier) {
        return variables.get(identifier);
    }


}
