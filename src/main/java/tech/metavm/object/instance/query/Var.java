package tech.metavm.object.instance.query;

import tech.metavm.util.NncUtils;

import java.util.List;

public class Var {

    public static List<Var> idVars(Long...ids) {
        return NncUtils.map(ids, Var::idVar);
    }

    public static List<Var> nameVars(String...names) {
        return NncUtils.map(names, Var::nameVar);
    }

    public static Var idVar(long id) {
        return new Var(VarType.ID, id);
    }

    public static Var nameVar(String name) {
        return new Var(VarType.NAME, name);
    }

    private final VarType type;
    private final Object symbol;

    public Var(VarType type, Object symbol) {
        this.type = type;
        this.symbol = symbol;
    }

    public VarType getType() {
        return type;
    }

    public String getStringSymbol() {
        return (String) symbol;
    }

    public long getLongSymbol() {
        return (long) symbol;
    }

    @Override
    public String toString() {
        return switch (type) {
            case ID -> "$" + symbol;
            case NAME -> symbol.toString();
        };
    }
}
