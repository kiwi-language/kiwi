package tech.metavm.autograph;

import org.junit.Assert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tech.metavm.common.ErrorDTO;
import tech.metavm.entity.StandardTypes;
import tech.metavm.flow.rest.FlowExecutionRequest;
import tech.metavm.object.instance.rest.*;
import tech.metavm.object.type.rest.dto.GetParameterizedTypeRequest;
import tech.metavm.util.DebugEnv;
import tech.metavm.util.TestUtils;

import java.util.List;

public class CapturedTypesCompileTest extends CompilerTestBase {

    public static final Logger LOGGER = LoggerFactory.getLogger(CapturedTypesCompileTest.class);

    public static final String SOURCE_ROOT = "/Users/leen/workspace/object/lab/src/main/capturedtypes";

    public void test() {
//        compile(SOURCE_ROOT);
////        DebugEnv.debugLogger_ON = true;
//        compile(SOURCE_ROOT);
//        submit(() -> {
////            DebugEnv.debugLogger_ON = true;
//            var utilsType = getClassTypeByCode("CtUtils");
//            for (ErrorDTO error : utilsType.getClassParam().errors()) {
//                LOGGER.info("Utils error: {}", error.message());
//            }
//            Assert.assertEquals(0, utilsType.getClassParam().errors().size());
//
//            var labType = getClassTypeByCode("CtLab");
//            var fooType = getClassTypeByCode("CtFoo");
//            var labFoosFieldId = TestUtils.getFieldIdByCode(labType, "foos");
//            var fooNameFieldId = TestUtils.getFieldIdByCode(fooType, "name");
//
//            var fooChildListType = typeManager.getParameterizedType(
//                    new GetParameterizedTypeRequest(
//                            StandardTypes.getChildListType().getStringId(),
//                            List.of(fooType.id()),
//                            List.of()
//                    )
//            );
//
//            var labId = TestUtils.doInTransaction(() ->
//                    instanceManager.create(InstanceDTO.createClassInstance(
//                            labType.id(),
//                            List.of(
//                                    InstanceFieldDTO.create(
//                                            labFoosFieldId,
//                                            new ListFieldValue(
//                                                    null,
//                                                    true,
//                                                    List.of(
//                                                            InstanceFieldValue.of(
//                                                                    InstanceDTO.createClassInstance(
//                                                                            fooType.id(),
//                                                                            List.of(
//                                                                                    InstanceFieldDTO.create(
//                                                                                            fooNameFieldId,
//                                                                                            PrimitiveFieldValue.createString("foo001")
//                                                                                    )
//                                                                            )
//                                                                    )
//                                                            ),
//                                                            InstanceFieldValue.of(
//                                                                    InstanceDTO.createClassInstance(
//                                                                            fooType.id(),
//                                                                            List.of(
//                                                                                    InstanceFieldDTO.create(
//                                                                                            fooNameFieldId,
//                                                                                            PrimitiveFieldValue.createString("foo002")
//                                                                                    )
//                                                                            )
//                                                                    )
//                                                            ),
//                                                            InstanceFieldValue.of(
//                                                                    InstanceDTO.createClassInstance(
//                                                                            fooType.id(),
//                                                                            List.of(
//                                                                                    InstanceFieldDTO.create(
//                                                                                            fooNameFieldId,
//                                                                                            PrimitiveFieldValue.createString("foo003")
//                                                                                    )
//                                                                            )
//                                                                    )
//                                                            )
//                                                    )
//                                            )
//                                    )
//                            )
//                    ))
//            );
//            var lab = instanceManager.get(labId, 2).instance();
//            var foos = ((InstanceFieldValue) lab.getFieldValue(labFoosFieldId)).getInstance();
//            Assert.assertEquals(3, foos.getElements().size());
//            var foo002 = ((InstanceFieldValue) foos.getElements().get(1)).getInstance();
//
//            var labGetFooByNameMethodId = TestUtils.getMethodIdByCode(labType, "getFooByName");
//            var foundFoo = TestUtils.doInTransaction(() ->
//                    flowExecutionService.execute(
//                            new FlowExecutionRequest(
//                                    labGetFooByNameMethodId,
//                                    labId,
//                                    List.of(
//                                            PrimitiveFieldValue.createString("foo002")
//                                    )
//                            )
//                    )
//            );
//            Assert.assertEquals(foo002.id(), foundFoo.id());
//        });
    }

}
