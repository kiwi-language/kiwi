package org.metavm.compiler.diag;

import org.metavm.compiler.element.Clazz;
import org.metavm.compiler.syntax.*;
import org.metavm.compiler.type.Type;
import org.metavm.compiler.util.List;

public class Errors {

    public static final Error unclosedComment = create("unclosed.comment");
    public static final Error malformedFloatLiteral = create("malformed.float.literal");
    public static final Error illegalEscChar = create("illegal.esc.char");
    public static final Error symbolNotFound = create("symbol.not.found");
    public static final Error nonStaticIndexField = create("non.static.index.field");
    public static final Error invalidIndexValueType = create("invalid.index.value.type");
    public static final Error misplacedIndexField = create("misplaced.index.field");
    public static final Error reservedFieldName = create("reserved.field.name");
    public static final Error fieldNotInitialized = create("field.not.initialized");

    public static Error ambiguousReference(String matches)  {
        return create("ambiguous.reference", matches);
    }

    public static Error modifierNotAllowedHere(ModifierTag mod) {
        return create("modifier.not.allowed.here", mod.name().toLowerCase());
    }

    public static Error unexpectedChar(int c) {
        return create("unexpected.char", c);
    }

    public static Error unexpectedToken(Token token) {
        return create("unexpected.token", token);
    }

    public static Error typeCheckingCircularRef = create("type.checking.circular.ref");

    public static Error cantFindConstructor(Clazz clazz, List<Type> argTypes) {
        return create("cant.find.constructor",
                (clazz.isEnum() ? "enum " : "class ") + clazz.getQualName(),
                argTypes.map(Type::getTypeText).join(", ")
        );
    }

    public static final Error illegalStartOfExpr = create("illegal.start.of.expr");
    public static final Error illegalStartOfType = create("illegal.start.of.type");
    public static final Error illegalStartOfStmt = create("illegal.start.of.stmt");
    public static final Error cantResolveExpr = create("cant.resolve.expr");
    public static final Error cantResolveSymbol = create("cant.resolve.symbol");

    public static final Error cantResolveFunc = create("cant.resolve.func");

    public static final Error illegalUseOfType = create("illegal.use.of.type");

    public static Error cantResolve(Expr expr) {
        return expr instanceof Ident ? cantResolveSymbol : cantResolveExpr;
    }

    public static Error unexpectedType = create("unexpected.type");

    public static Error cantResolveFunction(List<Type> argTypes) {
        if (argTypes.isEmpty())
            return cantResolveFunc;
        else
            return create("cant.resolve.func.with.arg.types", argTypes.map(Type::getTypeText).join(", "));
    }

    public static final Error illegalNewExpression = create("illegal.new.expr");
    public static final Error duplicateBindingName = create("duplicate.binding.name");
    public static Error illegal(TokenKind tk) {
        return create("illegal", tk);
    }
    public static final Error invalidUnicodeEscape = create("invalid.unicode.escape");

    public static final Error variableMustTypedOrInitialized = create("variable.must.typed.or.initialized");

    public static final Error summaryFieldMustBeString = create("summary.field.must.be.string");

    public static final Error cantModifyCapturedVar = create("cant.modify.captured.var");

    public static Error expected(Object a) {
        return create("expected", a);
    }

    public static Error operatorCantBeApplied(String op, String type) {
        return create("operator.cant.be.applied1", op, type);
    }

    public static Error operatorCantBeApplied(String op, String lhsType, String rhsType) {
        return create("operator.cant.be.applied2", op, lhsType, rhsType);
    }

    public static Error expected2(Object a1, Object a2) {
        return create("expected2", a1, a2);
    }

    public static Error expected3(Object a1, Object a2, Object a3) {
        return create("expected3", a1, a2, a3);
    }

    public static Error expected4(Object a1, Object a2, Object a3, Object a4) {
        return create("expected4", a1, a2, a3, a4);
    }

    public static Error forEachNotApplicableToType(Type type) {
        return create("foreach.not.applicable.to.type", type);
    }


    private static Error create(String code, Object...args) {
        return new Error(code, args);
    }

    public static Error illegalCast(String from, String to) {
        return create("illegal.cast", from, to);
    }
}
