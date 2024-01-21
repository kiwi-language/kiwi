package tech.metavm.object.instance.query;

import junit.framework.TestCase;
import org.junit.Assert;
import tech.metavm.entity.MockStandardTypesInitializer;
import tech.metavm.entity.StandardTypes;
import tech.metavm.expression.Func;
import tech.metavm.expression.FunctionDesc;
import tech.metavm.object.instance.core.Instance;
import tech.metavm.object.type.Type;
import tech.metavm.util.BusinessException;

import java.util.List;

import static tech.metavm.util.Instances.*;

public class FunctionDescTest extends TestCase {

    @Override
    protected void setUp() throws Exception {
        MockStandardTypesInitializer.init();
    }

    public void test_get_return_type_max_double() {
        MockStandardTypesInitializer.init();
        FunctionDesc desc = new FunctionDesc(Func.MAX$_DOUBLE);
        Type returnType = desc.getReturnType(List.of(
                StandardTypes.getDoubleType(), StandardTypes.getDoubleType()
        ));
        Assert.assertEquals(StandardTypes.getDoubleType(), returnType);
    }

    public void test_get_return_type_if() {
        FunctionDesc desc = new FunctionDesc(Func.IF);
        Type returnType = desc.getReturnType(List.of(
                StandardTypes.getAnyType(), StandardTypes.getDoubleType()
        ));
        Assert.assertEquals(StandardTypes.getAnyType(), returnType);
    }

    @SuppressWarnings("CatchMayIgnoreException")
    public void test_check_argument_max_double() {
        FunctionDesc desc = new FunctionDesc(Func.MAX$_DOUBLE);
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
        FunctionDesc desc = new FunctionDesc(Func.IF);
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
        FunctionDesc desc = new FunctionDesc(Func.MAX$_DOUBLE);

        Instance result = desc.evaluate(
                List.of(
                        createDouble(1.0), createDouble(1.1)
                )
        );
        Assert.assertEquals(createDouble(1.1), result);
    }

    public void test_evaluate_is_blank() {
        FunctionDesc desc = new FunctionDesc(Func.IS_BLANK);
        Instance result = desc.evaluate(
                List.of(
                        createString("abc")
                )
        );
        Assert.assertEquals(createBoolean(false), result);
    }

    public void test_evaluate_sum_int() {
        FunctionDesc desc = new FunctionDesc(Func.SUM$_INT);
        Instance result = desc.evaluate(
                List.of(
                        createLong(1L), createLong(2L)
                )
        );
        Assert.assertEquals(createLong(3L), result);
    }

    public void test_evaluate_if() {
        FunctionDesc desc = new FunctionDesc(Func.IF);
        Instance result = desc.evaluate(
                List.of(
                        createBoolean(false),
                        createDouble(1.0), createDouble(1.1)
                )
        );
        Assert.assertEquals(createDouble(1.1), result);
    }

    public void test_evaluate_if_fail() {
        FunctionDesc desc = new FunctionDesc(Func.IF);
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