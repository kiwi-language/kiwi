package tech.metavm.expression;

import tech.metavm.object.instance.core.Id;
import tech.metavm.util.InternalException;
import tech.metavm.util.NncUtils;
import tech.metavm.util.ValueUtils;

import java.util.List;

public class Var {

    public static final String ID_PREFIX = "$";

    public static Var parse(String str) {
        if(str.startsWith(ID_PREFIX)) {
            String idString = str.substring(ID_PREFIX.length());
            if(ValueUtils.isIntegerStr(idString)) {
                return idVar(Long.parseLong(idString));
            }
            else {
                throw new InternalException("Malformed var '" + str + "'");
            }
        }
        else {
            return nameVar(str);
        }
    }

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

    public String getName() {
        if(isName()) {
            return (String) symbol;
        }
        else {
            throw new InternalException(this + " is not a name var");
        }
    }

    public Id getId() {
        if(isId()) {
            return (Id) symbol;
        }
        else {
            throw new InternalException(this + " is not an id var");
        }
    }

    public boolean isId() {
        return type == VarType.ID;
    }

    public boolean isName() {
        return type == VarType.NAME;
    }

    @Override
    public String toString() {
        return switch (type) {
            case ID -> "$" + symbol;
            case NAME -> symbol.toString();
        };
    }
}
