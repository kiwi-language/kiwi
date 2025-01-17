package org.metavm.entity;

import junit.framework.TestCase;
import org.junit.Assert;
import org.metavm.util.IdAndValue;
import org.metavm.util.Utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class RangeCacheTest extends TestCase {

    public static final int MAX_N = 100;
    private List<Long> data;
    private RangeCache<Long> rangeCache;
    private long loadingCount;

    @Override
    protected void setUp() throws Exception {
        data = new ArrayList<>();
        for (int i = 0; i < MAX_N; i++) {
            data.add((long) i);
        }
        rangeCache = new RangeCache<>(this::load);
        loadingCount = 0L;
    }

    public void test() {
        assertQuery(50, 10L);

        assertQueries(List.of(
                new RangeQuery(55, 10),
                new RangeQuery(70, 5),
                new RangeQuery(70, 10)
        ), 1);


        assertQueries(List.of(
                new RangeQuery(55, 10),
                new RangeQuery(70, 5),
                new RangeQuery(101, 10)
        ));

        assertQuery(70, 10L);
        assertQuery(55, 20L);
        assertCachedQuery(50, 30L);

        assertQuery(90, 20L);
        assertCachedQuery(101, 10L);
        assertCachedQuery(1000L, 10L);
        assertQuery(100, 10L);

        assertQuery(-1L, 10L);

        assertQuery(-10L, 5L);

    }

    @SuppressWarnings("SameParameterValue")
    private void assertQuery(long start, long limit) {
        assertRangeQuery(start, limit, -1);
    }

    private void assertQueries(List<RangeQuery> queries) {
        assertQueries(queries, -1);
    }

    private void assertQueries(List<RangeQuery> queries, int expectedLoadingTimes) {
        long lcBefore = loadingCount;
        Map<RangeQuery, List<Long>> resultMap = rangeCache.query(queries);
        long loadingTimes = loadingCount - lcBefore;
        if(expectedLoadingTimes != -1) {
            Assert.assertEquals("Loading times does not meet the expectation",
                    expectedLoadingTimes, loadingTimes);
        }
        for (RangeQuery q : queries) {
            List<Long> result = resultMap.get(q);
            List<Long> expected = Utils.map(internalLoad(q.startId(), q.limit()), IdAndValue::value);
            Assert.assertEquals(expected, result);
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void assertCachedQuery(long start, long limit) {
        assertRangeQuery(start, limit, 0);
    }

    private void assertRangeQuery(long start, long limit, int expectedLoadingTimes) {
        assertQueries(List.of(new RangeQuery(start, limit)), expectedLoadingTimes);
    }

    private Map<RangeQuery, List<IdAndValue<Long>>> load(List<RangeQuery> queries) {
        loadingCount++;
        return Utils.toMap(
                queries,
                Function.identity(),
                q -> internalLoad(q.startId(), q.limit())
        );
    }

    private List<IdAndValue<Long>> internalLoad(long start, long limit) {
        List<IdAndValue<Long>> result = new ArrayList<>();
        Iterator<Long> it = data.listIterator(Math.min(data.size(), Math.max(0, (int) start)));
        while (it.hasNext()) {
            if(result.size() >= limit) {
                return result;
            }
            long id = it.next();
            result.add(new IdAndValue<>(id, id));
        }
        return result;
    }

}