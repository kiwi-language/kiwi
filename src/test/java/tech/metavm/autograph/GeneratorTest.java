package tech.metavm.autograph;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.psi.PsiClassType;
import junit.framework.TestCase;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import tech.metavm.autograph.mocks.*;
import tech.metavm.common.Result;
import tech.metavm.entity.*;
import tech.metavm.object.instance.ChangeLogPlugin;
import tech.metavm.object.instance.InstanceStore;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.persistence.mappers.*;
import tech.metavm.object.type.*;
import tech.metavm.object.type.rest.dto.BatchSaveRequest;
import tech.metavm.object.type.rest.dto.GetTypesRequest;
import tech.metavm.object.type.rest.dto.GetTypesResponse;
import tech.metavm.object.type.rest.dto.TypeDTO;
import tech.metavm.util.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static tech.metavm.util.Constants.ROOT_TENANT_ID;
import static tech.metavm.util.TestUtils.writeJson;

public class GeneratorTest extends TestCase {

    private TypeResolver typeResolver;

    public static final String CP_ROOT = "/Users/leen/workspace/object/src/main/resources";
    public static final String OUTPUT_FILE = "/Users/leen/workspace/object/test.json";

    public static final String ID_FILE = "/Users/leen/workspace/object/result.json";


    public <T> T doInSession(Function<IEntityContext, T> action) {
        SqlSessionFactory sessionFactory = new SqlSessionFactoryBuilder().build(
                GeneratorTest.class.getResourceAsStream("/mybatis-config-mysql.xml")
        );
        try (var session = sessionFactory.openSession()) {
            var instanceMapper = session.getMapper(InstanceMapper.class);
            var indexEntryMapper = session.getMapper(IndexEntryMapper.class);
            var referenceMapper = session.getMapper(ReferenceMapper.class);
            var instanceStore = new InstanceStore(instanceMapper, indexEntryMapper, referenceMapper);
            ModelDefRegistry.setDefContext(null);
            TestContext.setTenantId(ROOT_TENANT_ID);
            var allocatorStore = new DirectoryAllocatorStore(CP_ROOT);
            var instanceSearchService = new MemInstanceSearchService();
            var instanceContextFactory = new InstanceContextFactory(instanceStore);
            InstanceLogServiceImpl instanceLogService = new InstanceLogServiceImpl(
                    instanceSearchService,
                    instanceContextFactory,
                    instanceStore,
                    new MockTransactionOperations());
            instanceContextFactory.setPlugins(List.of(new ChangeLogPlugin(instanceLogService)));
            var stdAllocators = new StdAllocators(allocatorStore);
            Bootstrap bootstrap = new Bootstrap(instanceContextFactory, stdAllocators, new MemColumnStore());
            bootstrap.boot();
            ContextUtil.setLoginInfo(-1L, 0L);
            var context = instanceContextFactory.newContext().getEntityContext();
            typeResolver = new TypeResolverImpl(context);
            return action.apply(context);
        }
    }

//    public void test_lambda() {
//        build(List.of(FuncType.class, AstLambdaFoo.class));
//    }

    public void test_branch() {
        build(List.of(AstBranchFoo.class, AstProductState.class));
    }

    public void test_product_coupon_order() {
        build(
                List.of(AstProduct.class, DirectAstCoupon.class, AstOrder.class,
                        AstCouponState.class, AstProductState.class, AstCoupon.class)
        );
    }

//    public void test_generic() {
//        compile(List.of(AstQueue.class, AstGenericLab.class));
//    }

    public void test_product_coupon_order_compile_only() {
        compile(List.of(AstProduct.class, DirectAstCoupon.class, AstOrder.class,
                AstCouponState.class, AstProductState.class, AstCoupon.class));
    }

    public void test_simple_product() {
        build(List.of(AstSimpleProduct.class, AstProductState.class, AstSimpleCoupon.class));
    }


    public void test_simple_product_compile_only() {
        compile(List.of(AstSimpleProduct.class, AstProductState.class, AstSimpleCoupon.class));
    }

    public void test_exception() {
        build(List.of(AstExceptionFoo.class, AstException.class));
    }

    public void test_enum() {
        build(List.of(AstProductState.class));
    }

    public void testInterface() {
        doInSession(entityContext -> {
            var sub = (ClassType) typeResolver.resolve(
                    TranspileUtil.createType(TranspileTestTools.getPsiClass(SubFoo.class))
            );
            var it = (ClassType) typeResolver.resolve(
                    TranspileUtil.createType(TranspileTestTools.getPsiClass(InterfaceFoo.class))
            );
            assertTrue(it.isAssignableFrom(sub));
            var itTest = it.getFlowByCode("test");
            assertTrue(itTest.isAbstract());
            var subTest = sub.resolveFlow(itTest);
            assertNotNull(subTest);
            assertEquals(subTest.getName(), itTest.getName());
            assertFalse(subTest.isAbstract());
            return null;
        });
    }

    private void build(List<Class<?>> classes) {
        clean();
        var request = compile(classes);
        deploy(request);
    }

    public void test_switch() {
        build(List.of(AstSwitchFoo.class));
    }

    public void test_switch_transform_only() {
        var file = TranspileTestTools.getPsiJavaFile(AstSwitchFoo.class);
        transform(List.of(AstSwitchFoo.class));
        System.out.println(file.getText());
    }

    public void test_clean() {
        clean();
    }

    private void clean() {
        List<Long> ids = TestUtils.readJson(ID_FILE, new TypeReference<>() {
        });
        if (!ids.isEmpty()) {
            var result = HttpUtils.post("/type/batch-get",
                    new GetTypesRequest(ids, false),
                    new TypeReference<Result<GetTypesResponse>>() {});
            List<Long> nonEnumIds = new ArrayList<>();
            for (TypeDTO type : result.getData().types()) {
                if(type.category() != TypeCategory.ENUM.code())
                    nonEnumIds.add(type.id());
            }
            var deleteInstanceResp = HttpUtils.post("/instance/delete-by-types",
                    nonEnumIds, new TypeReference<Result<Void>>() {
                    });
            if (deleteInstanceResp.getCode() != 0) {
                throw new InternalException("Fail to remove instances: " + deleteInstanceResp.getMessage());
            }
            var deleteTypeResp = HttpUtils.post("/type/batch-delete",
                    ids, new TypeReference<Result<Void>>() {
                    });
            if (deleteTypeResp.getCode() != 0) {
                throw new InternalException("Fail to remove types: " + deleteTypeResp.getMessage());
            }
            writeJson(ID_FILE, List.of());
            System.out.println("Clean succeeded");
        } else {
            System.out.println("Clean skipped (nothing to clean)");
        }
    }

    public void test_deploy() {
        deploy();
    }

    private void deploy() {
        var request = TestUtils.readJson(OUTPUT_FILE, BatchSaveRequest.class);
        deploy(request);
    }

    private void deploy(BatchSaveRequest request) {
        var resp = HttpUtils.post("/type/batch", request, new TypeReference<Result<List<Long>>>() {
        });
        if (resp.getCode() != 0) {
            throw new InternalException("Deploy failed: " + resp.getMessage());
        }
        var ids = resp.getData();
        System.out.println("Deploy succeeded");
        writeJson(ID_FILE, ids);
        System.out.println("Deployed type ids: " + ids);
    }

    private void transform(List<Class<?>> classes) {
        doInSession(entityContext -> {
            List<PsiClassType> psiTypes = NncUtils.map(classes, TranspileTestTools::getPsiClassType);
            NncUtils.forEach(psiTypes, k -> typeResolver.resolveTypeOnly(k));
            return null;
        });
    }

    private BatchSaveRequest compile(List<Class<?>> classes) {
        return doInSession(entityContext -> {
            List<PsiClassType> psiTypes = NncUtils.map(classes, TranspileTestTools::getPsiClassType);
            NncUtils.map(psiTypes, k -> (ClassType) typeResolver.resolve(k));

            try (SerializeContext context = SerializeContext.enter()) {
                context.setIncludingCode(true);
                context.setIncludingNodeOutputType(false);
                context.setIncludingValueType(false);
                var generatedTypes = typeResolver.getGeneratedTypes();
                for (Type metaType : generatedTypes) {
                    if(metaType instanceof ClassType classType) {
                        typeResolver.ensureCodeGenerated(classType);
                        context.addWritingCodeType(classType);
                    }
                }
                for (Type metaType : generatedTypes) {
                    if (metaType instanceof ClassType classType)
                        typeResolver.ensureCodeGenerated(classType);
                    context.writeType(metaType);
//                    var collTypeNames = TypeUtils.getCollectionTypeNames(metaType);
//                    for (String collTypeName : collTypeNames) {
//                        var collType = entityContext.selectByUniqueKey(ClassType.UNIQUE_NAME, collTypeName);
//                        if (collType != null) {
//                            context.writeType(collType);
//                        }
//                    }
                }
                context.writeDependencies();
                var typeDTOs = context.getNonSystemTypes();
                System.out.println("Compile succeeded");
                var request = new BatchSaveRequest(typeDTOs);
                writeJson(OUTPUT_FILE, request);
                return request;
            }
        });
    }

}