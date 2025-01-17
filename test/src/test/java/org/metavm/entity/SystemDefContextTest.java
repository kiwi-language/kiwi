//package org.metavm.entity;
//
//import junit.framework.TestCase;
//import lombok.extern.slf4j.Slf4j;
//import org.junit.Assert;
//import org.metavm.api.ValueObject;
//import org.metavm.flow.Flow;
//import org.metavm.object.instance.core.Instance;
//import org.metavm.object.type.*;
//import org.metavm.util.ModelAndPath;
//
//import java.util.List;
//import java.util.Objects;
//import java.util.Set;
//
//@Slf4j
//public class SystemDefContextTest extends TestCase {
//
//    private SystemDefContext defContext;
//
//    public void test() {
//        var allocatorStore = new MemAllocatorStore();
//        var allocators = new StdAllocators(allocatorStore);
//        defContext = new SystemDefContext(
//                allocators, new MemColumnStore(), new MemTypeTagStore(), new IdentityContext());
//        for (Class<?> entityClass : EntityUtils.getModelClasses()) defContext.getDef(entityClass);
//        defContext.finish();
//        defContext.getIdentityMap().forEach((object, javaConstruct) -> {
//            if (object.isDurable()) {
//                if (object.isRoot())
//                    allocators.putId(javaConstruct, object.getId(), object.getNextNodeId());
//                else
//                    allocators.putId(javaConstruct, object.getId());
//            }
//        });
//        allocators.save();
//
//        var allocators1 = new StdAllocators(allocatorStore);
//        defContext = new SystemDefContext(
//                allocators1, new MemColumnStore(), new MemTypeTagStore(), new IdentityContext());
//        for (Class<?> entityClass : EntityUtils.getModelClasses()) defContext.getDef(entityClass);
//        for (Entity entity : defContext.entities()) {
//            Assert.assertNotNull(entity.tryGetId());
//        }
//    }
//
//    public void testInheritance() {
//        defContext = new SystemDefContext(
//                new StdAllocators(new MemAllocatorStore()), new MemColumnStore(), new MemTypeTagStore(), new IdentityContext());
//        StdKlass.initialize(defContext, false);
//        var superDef = defContext.getDef(Flow.class);
//        Assert.assertEquals(Objects.requireNonNull(superDef.getKlass().getSuperType()).getSuperType(), StdKlass.entity.get().getType());
//    }
//
//    public void testGetIdentityMap() {
//        defContext = new SystemDefContext(
//                new StdAllocators(new MemAllocatorStore()), new MemColumnStore(), new MemTypeTagStore(), new IdentityContext());
//        StdKlass.initialize(defContext, false);
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