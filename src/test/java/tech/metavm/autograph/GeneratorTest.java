package tech.metavm.autograph;

import com.fasterxml.jackson.core.type.TypeReference;
import com.intellij.psi.PsiClassType;
import junit.framework.TestCase;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import tech.metavm.autograph.mocks.*;
import tech.metavm.dto.Result;
import tech.metavm.entity.*;
import tech.metavm.object.instance.ChangeLogPlugin;
import tech.metavm.object.instance.InstanceStore;
import tech.metavm.object.instance.MemInstanceSearchService;
import tech.metavm.object.instance.log.InstanceLogServiceImpl;
import tech.metavm.object.instance.persistence.mappers.*;
import tech.metavm.object.meta.*;
import tech.metavm.object.meta.rest.dto.TypeDTO;
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
            var instanceArrayMapper = session.getMapper(InstanceArrayMapper.class);
            var indexEntryMapper = session.getMapper(IndexEntryMapper.class);
            var referenceMapper = session.getMapper(ReferenceMapper.class);
            var instanceMapperGateway = new InstanceMapperGateway(instanceMapper, instanceArrayMapper);
            var instanceStore = new InstanceStore(instanceMapperGateway, indexEntryMapper, referenceMapper);
            ModelDefRegistry.setDefContext(null);
            TestContext.setTenantId(ROOT_TENANT_ID);
            var allocatorStore = new DirectoryAllocatorStore(CP_ROOT);
            var instanceSearchService = new MemInstanceSearchService();
            var instanceContextFactory = new InstanceContextFactory(instanceStore);
            InstanceLogServiceImpl instanceLogService = new InstanceLogServiceImpl(
                    instanceSearchService,
                    instanceContextFactory,
                    instanceStore
            );
            instanceContextFactory.setPlugins(List.of(new ChangeLogPlugin(instanceLogService)));
            var stdAllocators = new StdAllocators(allocatorStore);
            Bootstrap bootstrap = new Bootstrap(instanceContextFactory, stdAllocators);
            bootstrap.boot();
            var context = instanceContextFactory.newContext().getEntityContext();
            typeResolver = new MockTypeResolver(context, stdAllocators);
            return action.apply(context);
        }
    }

    public void test_product_coupon_order() {
        build(
                List.of(AstProduct.class, DirectAstCoupon.class, AstOrder.class,
                        AstCouponState.class, AstProductState.class, AstCoupon.class)
        );
    }

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
            var subTest = sub.getOverrideFlow(itTest);
            assertNotNull(subTest);
            assertEquals(subTest.getName(), itTest.getName());
            assertFalse(subTest.isAbstract());
            return null;
        });
    }

    private void build(List<Class<?>> classes) {
        clean();
        var typeDTOs = compile(classes);
        deploy(typeDTOs);
    }

    public void test_switch() {
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
            var deleteInstanceResp = HttpUtils.post("/instance/delete-by-types",
                    ids, new TypeReference<Result<Void>>() {
                    });
            if (deleteInstanceResp.code() != 0) {
                throw new InternalException("Fail to remove instances: " + deleteInstanceResp.message());
            }
            var deleteTypeResp = HttpUtils.post("/type/batch-delete",
                    ids, new TypeReference<Result<Void>>() {
                    });
            if (deleteTypeResp.code() != 0) {
                throw new InternalException("Fail to remove types: " + deleteTypeResp.message());
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
        List<TypeDTO> typeDTOs = TestUtils.readJson(OUTPUT_FILE, new TypeReference<>() {
        });
        deploy(typeDTOs);
    }

    private void deploy(List<TypeDTO> typeDTOs) {
        var resp = HttpUtils.post("/type/batch", typeDTOs, new TypeReference<Result<List<Long>>>() {
        });
        if (resp.code() != 0) {
            throw new InternalException("Deploy failed: " + resp.message());
        }
        var ids = resp.data();
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

    private List<TypeDTO> compile(List<Class<?>> classes) {
        return doInSession(entityContext -> {
            List<PsiClassType> psiTypes = NncUtils.map(classes, TranspileTestTools::getPsiClassType);
            NncUtils.map(psiTypes, k -> (ClassType) typeResolver.resolve(k));
            try (SerializeContext ignored = SerializeContext.enter()) {
                List<TypeDTO> result = new ArrayList<>();
                var generatedTypes = typeResolver.getGeneratedTypes();
                for (Type metaType : generatedTypes) {
                    if (metaType instanceof ClassType classType) {
                        typeResolver.ensureCodeGenerated(classType);
                        result.add(classType.toDTO(true, true, true, true));
                    } else {
                        result.add(metaType.toDTO());
                    }
                    if (metaType.getArrayType() != null) {
                        result.add(metaType.getArrayType().toDTO());
                    }
                    if (metaType.getNullableType() != null) {
                        result.add(metaType.getNullableType().toDTO());
                    }
                    var collTypeNames = TypeUtil.getCollectionTypeNames(metaType);
                    for (String collTypeName : collTypeNames) {
                        var collType = (ClassType) entityContext.selectByUniqueKey(Type.UNIQUE_NAME, collTypeName);
                        if (collType != null) {
                            result.add(collType.toDTO(true, true, true, true));
                        }
                    }
                }
                writeJson(OUTPUT_FILE, result);
                System.out.println("Compile succeeded");
                return result;
            }
        });
    }

}