//package org.metavm.object.instance;
//
//import org.metavm.common.Page;
//import org.metavm.expression.ConstantExpression;
//import org.metavm.expression.Expression;
//import org.metavm.expression.InstanceEvaluationContext;
//import org.metavm.flow.ParameterizedFlowProvider;
//import org.metavm.object.instance.core.ClassInstance;
//import org.metavm.object.instance.core.Instance;
//import org.metavm.object.instance.search.IndexSourceBuilder;
//import org.metavm.object.instance.search.InstanceSearchService;
//import org.metavm.object.instance.search.SearchQuery;
//import org.metavm.util.Instances;
//import org.metavm.util.MultiApplicationMap;
//import org.metavm.util.NncUtils;
//
//import java.util.*;
//
//import static org.metavm.util.Constants.ROOT_APP_ID;
//import static org.metavm.util.TestContext.getAppId;
//
//public class MemInstanceSearchService implements InstanceSearchService {
//
//    private final MultiApplicationMap<Long, Map<String, Object>> sourceMap = new MultiApplicationMap<>();
//    private final MultiApplicationMap<Long, ClassInstance> instanceMap = new MultiApplicationMap<>();
//
//    @Override
//    public Page<Long> search(SearchQuery query) {
//        List<Long> result = new ArrayList<>();
//        doSearch(getAppId(), query, result);
//        doSearch(ROOT_APP_ID, query, result);
//        Collections.sort(result);
//        return new Page<>(
//                getPage(result, query.from(), query.end()),
//                result.size()
//        );
//    }
//
//    @Override
//    public long count(SearchQuery query) {
//        List<Long> result = new ArrayList<>();
//        doSearch(getAppId(), query, result);
//        doSearch(ROOT_APP_ID, query, result);
//        return result.size();
//    }
//
//    private void doSearch(long appId, SearchQuery query, List<Long> result) {
//        Collection<ClassInstance> instances = instanceMap.values(appId);
//        for (ClassInstance instance : instances) {
//            if (match(instance, query)) {
//                result.add(instance.tryGetPhysicalId());
//            }
//        }
//    }
//
//    public boolean contains(long id) {
//        return instanceMap.containsKey(getAppId(), id)
//                || instanceMap.containsKey(ROOT_APP_ID, id);
//    }
//
//    private static <T> List<T> getPage(List<T> result, int start, int end) {
//        if (start >= result.size()) {
//            return List.of();
//        }
//        return result.subList(start, Math.min(end, result.size()));
//    }
//
//    private boolean match(ClassInstance instance, SearchQuery query) {
//        if (!query.typeIds().contains(instance.getType().tryGetId()))
//            return false;
//        return query.condition() == null || Instances.isTrue(
//                query.condition().evaluate(new MyEvaluationContext(query.appId(), instance, null))
//        );
//    }
//
//    private class MyEvaluationContext extends InstanceEvaluationContext {
//
//        private final long appId;
//
//        public MyEvaluationContext(long appId, ClassInstance instance, ParameterizedFlowProvider parameterizedFlowProvider) {
//            super(instance, parameterizedFlowProvider);
//            this.appId = appId;
//        }
//
//        @Override
//        public Instance evaluate(Expression expression) {
//            if (expression instanceof ConstantExpression constantExpression
//                    && constantExpression.getValue().getId() != null) {
//                return Objects.requireNonNull(
//                        instanceMap.get(appId, constantExpression.getValue().getId().getPhysicalId())
//                );
//            }
//            return super.evaluate(expression);
//        }
//
//        @Override
//        public boolean isContextExpression(Expression expression) {
//            return super.isContextExpression(expression)
//                    || expression instanceof ConstantExpression constantExpression
//                    && constantExpression.getValue().getId() != null;
//        }
//    }
//
//    public void add(long appId, ClassInstance instance) {
//        bulk(appId, List.of(instance), List.of());
//    }
//
//    public void remove(long appId, long id) {
//        bulk(appId, List.of(), List.of(id));
//    }
//
//    @Override
//    public void bulk(long appId, List<ClassInstance> toIndex, List<Long> toDelete) {
//        for (ClassInstance instance : toIndex) {
//            NncUtils.requireNonNull(instance.tryGetPhysicalId());
//            sourceMap.put(
//                    getAppId(),
//                    instance.tryGetPhysicalId(),
//                    IndexSourceBuilder.buildSource(appId, instance)
//            );
//            instanceMap.put(getAppId(), instance.tryGetPhysicalId(), instance);
//        }
//        for (Long id : toDelete) {
//            sourceMap.remove(getAppId(), id);
//            instanceMap.remove(getAppId(), id);
//        }
//    }
//
//    public void clear() {
//        instanceMap.clear();
//        sourceMap.clear();
//    }
//
//    public MemInstanceSearchService copy() {
//        var copy = new MemInstanceSearchService();
//        copy.instanceMap.putAll(instanceMap);
//        copy.sourceMap.putAll(sourceMap);
//        return copy;
//    }
//
//}
