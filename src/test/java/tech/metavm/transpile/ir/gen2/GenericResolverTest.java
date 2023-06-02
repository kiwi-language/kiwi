package tech.metavm.transpile.ir.gen2;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.transpile.TypeRange;
import tech.metavm.transpile.ir.*;

import java.util.ArrayList;
import java.util.List;

public class GenericResolverTest extends TestCase {

    private GenericResolver resolver;

    @Override
    protected void setUp() throws Exception {
        resolver = new GenericResolver();
    }

    public void test() {
        IRClass klass = new IRClass(
                "Foo", IRClassKind.CLASS, IRPackage.ROOT_PKG,
                List.of(), List.of(), null, null
        );

        IRMethod method = new IRMethod("test", List.of(Modifier.STATIC), List.of(), klass);

        IRClass listKlass = IRTestUtil.createClass(List.class);
        IRClass charSeqClass = IRTestUtil.createClass(CharSequence.class);
        IRType range = TypeRange.between(IRAnyType.getInstance(), charSeqClass);
        IRType listRange = new PType(null, listKlass, List.of(range));
        IRType strClass = IRTestUtil.createClass(String.class);
        IRType listStr = new PType(null, listKlass, List.of(strClass));

        var typeParamE = new TypeVariable<>(method, "E", List.of());
        IRType listE = new PType(null, listKlass, List.of(typeParamE));
        var typeParamT = new TypeVariable<>(method, "T", List.of(listE));

        method.initialize(
                typeParamE,
                List.of(
                        new IRParameter(
                                true,
                                List.of(),
                                "list",
                                typeParamT,
                                false
                        ),
                        new IRParameter(
                                true,
                                List.of(),
                                "list2",
                                typeParamT,
                                false
                        )
                ),
                List.of()
        );

        UnresolvedMethodCall umc = new UnresolvedMethodCall(
                null,
                klass,
                method.name(),
                List.of(
                        new ConstantExpression(new ArrayList<String>(), listStr),
                        new ConstantExpression(new ArrayList<String>(), listStr)
                )
        );

        Assert.assertNotNull(resolver.tryResolveMethodCall(umc, method));
    }


}