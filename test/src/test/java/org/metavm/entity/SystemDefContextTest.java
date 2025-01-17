//package org.metavm.entity;
//
//import junit.framework.TestCase;
//import org.junit.Assert;
//import org.metavm.api.ValueObject;
//import org.metavm.flow.Node;
//import org.metavm.object.instance.cache.LocalCache;
//import org.metavm.object.instance.core.EntityInstanceContextBridge;
//import org.metavm.object.instance.core.Instance;
//import org.metavm.object.type.Klass;
//import org.metavm.object.type.MemColumnStore;
//import org.metavm.object.type.MemTypeTagStore;
//import org.metavm.object.type.Type;
//import org.metavm.util.Constants;
//import org.metavm.util.MockIdProvider;
//import org.metavm.util.ModelAndPath;
//import org.metavm.util.TypeReference;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.util.List;
//import java.util.Map;
//import java.util.Objects;
//import java.util.Set;
//
//public class SystemDefContextTest extends TestCase {
//
//    private static final Logger logger = LoggerFactory.getLogger(SystemDefContextTest.class);
//
//    private SystemDefContext defContext;
//    private MockIdProvider idProvider;
//
//    @Override
//    protected void setUp() {
//        idProvider = new MockIdProvider();
//        var bridge = new EntityInstanceContextBridge();
//        var instanceContext = InstanceContextBuilder.newBuilder(Constants.ROOT_APP_ID,
//                        new MemInstanceStore(new LocalCache()), new DefaultIdInitializer(idProvider), bridge, bridge, bridge)
//                .readonly(false)
//                .build();
//        defContext = new SystemDefContext(
//                new StdIdProvider(new EmptyStdIdStore()), new MemColumnStore(), new MemTypeTagStore(), new IdentityContext(), new DefaultIdInitializer(idProvider));
//        bridge.setEntityContext(defContext);
//        StdKlass.initialize(defContext, false);
//    }
//
//    public void testGenerateInstances() {
//
//    }
//
//    public void testInheritance() {
//        defContext.postProcess();
//        KlassDef<Node> superDef = defContext.getDef(Node.class);
//        Assert.assertEquals(Objects.requireNonNull(superDef.getKlass().getSuperType()).getSuperType(), StdKlass.entity.get().getType());
//    }
//
//    public void testGetIdentityMap() {
//        KlassDef<Type> def = defContext.getDef(Type.class);
////        PojoDef<Type> def = defContext.getPojoDef(Type.class);
//        Klass type = def.getKlass();
//        var identityMap = defContext.getIdentityMap();
//        Set<ModelAndPath> models = EntityUtils.getReachableObjects(
//                List.of(type), o -> o instanceof Entity || o instanceof Enum<?>, true
//        );
//        defContext.flush();
//        for (ModelAndPath modelAndPath : models) {
//            Object model = modelAndPath.model();
//            String path = modelAndPath.path();
//            if (!(model instanceof ValueObject) && model instanceof Instance instance && !instance.isEphemeral()) {
//                ModelIdentity identity = identityMap.get(instance);
//                Assert.assertNotNull(
//                        "Can not find identity for model '" + model + "' at path '" + path + "'",
//                        identity
//                );
//            }
//        }
//    }
//
//}