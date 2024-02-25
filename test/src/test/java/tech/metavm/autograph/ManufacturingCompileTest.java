package tech.metavm.autograph;

import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.rest.PrimitiveFieldValue;
import tech.metavm.object.instance.rest.ReferenceFieldValue;
import tech.metavm.util.TestUtils;

import java.util.List;

import static tech.metavm.util.TestUtils.doInTransaction;

public class ManufacturingCompileTest extends CompilerTestBase {

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/manufacturing";

    public void test() {
        new Main(HOME, SOURCE_ROOT, AUTH_CONFIG, typeClient, allocatorStore).run();
        submit(() -> {
            var roundingRuleType = getClassTypeByCode("tech.metavm.manufacturing.material.RoundingRule");
            var roundHalfUpId = TestUtils.getEnumConstantIdByName(roundingRuleType, "四舍五入");
            var unitType = getClassTypeByCode("tech.metavm.manufacturing.material.Unit");
            var unitConstructorId = TestUtils.getMethodIdByCode(unitType, "Unit");
            var unit = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                    unitConstructorId,
                    null,
                    List.of(
                            PrimitiveFieldValue.createString("米"),
                            PrimitiveFieldValue.createString("meter"),
                            ReferenceFieldValue.create(roundHalfUpId),
                            PrimitiveFieldValue.createLong(2L),
                            PrimitiveFieldValue.createNull()
                    )
            )));
            // create a material
            var materialType = getClassTypeByCode("tech.metavm.manufacturing.material.Material");
            var materialConstructorId = TestUtils.getMethodIdByCode(materialType, "Material");
            var material = doInTransaction(() -> flowExecutionService.execute(new FlowExecutionRequest(
                    materialConstructorId,
                    null,
                    List.of(
                            PrimitiveFieldValue.createString("钢板"),
                            PrimitiveFieldValue.createString("sheet metal"),
                            ReferenceFieldValue.create(unit.id())
                    )
            )));
        });
    }

}
