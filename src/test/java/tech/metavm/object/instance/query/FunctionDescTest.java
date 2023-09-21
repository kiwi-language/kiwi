package tech.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.expression.Function;
import tech.metavm.expression.FunctionDesc;
import tech.metavm.object.instance.Instance;
import tech.metavm.object.meta.Type;
import tech.metavm.util.BusinessException;

import java.util.List;

import static tech.metavm.util.InstanceUtils.*;

public class FunctionDescTest extends TestCase {

    public void test_get_return_type_max_double() {
        FunctionDesc desc = new FunctionDesc(Function.MAX$_DOUBLE);
        Type returnType = desc.getReturnType(List.of(
                getDoubleType(), getDoubleType()
        ));
        Assert.assertEquals(getDoubleType(), returnType);
    }

    public void test_get_return_type_if() {
        FunctionDesc desc = new FunctionDesc(Function.IF);
        Type returnType = desc.getReturnType(List.of(
                getAnyType(), getDoubleType()
        ));
        Assert.assertEquals(getAnyType(), returnType);
    }

    @SuppressWarnings("CatchMayIgnoreException")
    public void test_check_argument_max_double() {
        FunctionDesc desc = new FunctionDesc(Function.MAX$_DOUBLE);
        desc.checkArguments(List.of(createDouble(1.0), createDouble(1.1)));
        try {
            desc.checkArguments(List.of(createLong(1L), createDouble(1.1)));
            Assert.fail();
        }
        catch (BusinessException e) {
        }
    }

    @SuppressWarnings("CatchMayIgnoreException")
    public void test_check_argument_if() {
        FunctionDesc desc = new FunctionDesc(Function.IF);
        desc.checkArguments(List.of(
                createBoolean(true), createDouble(1.1), createLong(1L)
        ));
        try {
            desc.checkArguments(List.of(createLong(1L), createDouble(1.1)));
            Assert.fail();
        }
        catch (BusinessException e) {
        }
    }

    public void test_evaluate_max_double() {
        FunctionDesc desc = new FunctionDesc(Function.MAX$_DOUBLE);

        Instance result = desc.evaluate(
                List.of(
                        createDouble(1.0), createDouble(1.1)
                )
        );
        Assert.assertEquals(createDouble(1.1), result);
    }

    public void test_evaluate_is_blank() {
        FunctionDesc desc = new FunctionDesc(Function.IS_BLANK);
        Instance result = desc.evaluate(
                List.of(
                        createString("abc")
                )
        );
        Assert.assertEquals(createBoolean(false), result);
    }

    public void test_evaluate_sum_int() {
        FunctionDesc desc = new FunctionDesc(Function.SUM$_INT);
        Instance result = desc.evaluate(
                List.of(
                        createLong(1L), createLong(2L)
                )
        );
        Assert.assertEquals(createLong(3L), result);
    }

    public void test_evaluate_if() {
        FunctionDesc desc = new FunctionDesc(Function.IF);
        Instance result = desc.evaluate(
                List.of(
                        createBoolean(false),
                        createDouble(1.0), createDouble(1.1)
                )
        );
        Assert.assertEquals(createDouble(1.1), result);
    }

    public void test_evaluate_if_fail() {
        FunctionDesc desc = new FunctionDesc(Function.IF);
        //noinspection CatchMayIgnoreException
        try {
            desc.evaluate(
                    List.of(
                            createDouble(1.0), createDouble(1.1)
                    )
            );
            Assert.fail();
        }
        catch (BusinessException e) {}
    }

}