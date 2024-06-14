package org.metavm.entity;

import org.jetbrains.annotations.NotNull;
import org.metavm.util.IdAndValue;
import org.metavm.util.NncUtils;

import java.util.*;
import java.util.function.Function;

public class RangeCache<V> {

    private final List<Range<V>> ranges = new ArrayList<>();

    private final Function<List<RangeQuery>, Map<RangeQuery, List<IdAndValue<V>>>> loader;

    public RangeCache(Function<List<RangeQuery>, Map<RangeQuery, List<IdAndValue<V>>>> loader) {
        this.loader = loader;
    }

    public Map<RangeQuery, List<V>> query(List<RangeQuery> queries) {
        load(queries);
        return NncUtils.toMap(queries, Function.identity(), this::query0);
    }

    public List<V> query(RangeQuery query) {
        return query(List.of(query)).values().iterator().next();
    }

    private List<V> query0(RangeQuery query) {
        if(query.limit() == 0) {
            return List.of();
        }
        int idx = NncUtils.binarySearch(ranges, query.startId(), Range::compareWithId);
        if(idx < 0) {
            return List.of();
        }
        Range<V> r = ranges.get(idx);
        return NncUtils.map(
                r.query(query.startId(), query.limit()),
                IdAndValue::value
        );
    }

    private void load(List<RangeQuery> rangeQueries) {
        List<Query> queries = NncUtils.mapAndSort(rangeQueries, Query::new, Query::compareTo);
        ListIterator<Query> queryIt = queries.listIterator();
        ListIterator<Range<V>> rangeIt = ranges.listIterator();
        LinkedList<RangeQuery> loadingQueries = new LinkedList<>();
        Query prev = null;
        Range<V> r = null;
        while (queryIt.hasNext()) {
            Query q = queryIt.next();
            if(prev != null && prev.startId() == q.startId() || q.limit == 0 || isBeyondLastRange(q.startId)) {
                continue;
            }
            prev = q;
            while (rangeIt.hasNext() && (r == null || r.end() < q.startId)) {
                r = rangeIt.next();
            }
            if(r != null && q.overlapsWith(r)) {
                q = q.subtract(r);
            }
            if(q != null) {
                loadingQueries.add(q.toRangeQuery());
            }
        }
        if(loadingQueries.isEmpty()) {
            return;
        }
        RangeQuery lastQ = loadingQueries.pollLast().extend();
        loadingQueries.addLast(lastQ);

        Map<RangeQuery, List<IdAndValue<V>>> loadResult = loader.apply(loadingQueries);
        List<Range<V>> loadedRanges = new ArrayList<>();
        for (Map.Entry<RangeQuery, List<IdAndValue<V>>> e : loadResult.entrySet()) {
            RangeQuery q = e.getKey();
            List<IdAndValue<V>> idAndValues = e.getValue();
            if(NncUtils.isEmpty(idAndValues)) {
                continue;
            }
            Range<V> range;
            if (lastQ == q) {
                boolean isLast;
                if (idAndValues.size() >= lastQ.limit()) {
                    idAndValues = idAndValues.subList(0, idAndValues.size() - 1);
                    isLast = false;
                } else {
                    isLast = true;
                }
                range = new Range<>(q.startId(), isLast, idAndValues);
            } else {
                range = new Range<>(q.startId(), false, idAndValues);
            }
            loadedRanges.add(range);
        }
        loadedRanges.forEach(this::addRange);
    }

    private boolean isBeyondLastRange(long id) {
        if(ranges.isEmpty()) {
            return false;
        }
        Range<V> last = ranges.get(ranges.size()-1);
        return last.isLast() && last.end() < id;
    }

    private void addRange(Range<V> range) {
        ListIterator<Range<V>> it = searchRange(range.start, true);
        Range<V> r;
        if(it.hasPrevious()) {
            Range<V> prev = it.previous();
            if(prev.shouldMergeWith(range)) {
                it.remove();
                r = Range.merge(prev, range);
            }
            else {
                it.next();
                r = range;
            }
        }
        else {
            r = range;
        }
        while (it.hasNext()) {
            Range<V> n = it.next();
            if(r.shouldMergeWith(n)) {
                it.remove();
                r = Range.merge(r, n);
            }
            else {
                it.previous();
                break;
            }
        }
        it.add(r);
    }

    private Range<V> searchRange(long id) {
        Iterator<Range<V>> it = searchRange(id, false);
        return it.hasNext() ? it.next() : null;
    }

    private ListIterator<Range<V>> searchRange(long id, boolean returnSuccessorIfNotFound) {
        if(ranges.isEmpty()) {
            return ranges.listIterator(0);
        }
        int low = 0, high = ranges.size();
        while (low < high) {
            int mid = (low + high) / 2;
            Range<V> range = ranges.get(mid);
            int r = range.compareWithId(id);
            if(r == 0) {
                return ranges.listIterator(mid);
            }
            if(r < 0) {
                low = mid + 1;
            }
            else {
                high = mid;
            }
        }
        if(returnSuccessorIfNotFound) {
            return ranges.listIterator(high);
        }
        return ranges.listIterator(ranges.size());
    }

    private record Query(long startId, long limit) implements Comparable<Query> {

        Query(RangeQuery rangeQuery) {
            this(rangeQuery.startId(), rangeQuery.limit());
        }

        boolean overlapsWith(Range<?> range) {
            return startId >= range.start && startId <= range.end();
        }

        <V> Query subtract(Range<V> range) {
            if(range.start > startId) {
                throw new RuntimeException("Can not subtract " + range + " from " + this);
            }
            if(range.isLast) {
                return null;
            }
            List<IdAndValue<V>> idAndValues = range.query(startId, limit);
            if(idAndValues.isEmpty()) {
                return this;
            }
            else if(idAndValues.size() == limit) {
                return null;
            }
            else {
                IdAndValue<V> lastIdAndValue = idAndValues.get(idAndValues.size() - 1);
                return new Query(
                        lastIdAndValue.id() + 1,
                        limit - idAndValues.size()
                );
            }
        }

        @Override
        public int compareTo(@NotNull Query o) {
            return startId != o.startId ? Long.compare(startId, o.startId) :
                    Long.compare(o.limit, limit);
        }


        RangeQuery toRangeQuery() {
            return new RangeQuery(startId, limit);
        }

    }

    private static class Range<V> {
        private final long start;
        private final TreeMap<Long, V> map = new TreeMap<>();
        private final boolean isLast;

        public Range(long start, boolean isLast, List<IdAndValue<V>> idAndValues) {
            this.start = start;
            this.isLast = isLast;
            for (IdAndValue<V> idAndValue : idAndValues) {
                map.put(idAndValue.id(), idAndValue.value());
            }
        }

        static <V> Range<V> merge(Range<V> first, Range<V> second) {
            if(!first.shouldMergeWith(second)) {
                throw new RuntimeException("Can not merge " + first + " with " + second);
            }
            if(second.start < first.start) {
                Range<V> tmp = second;
                second = first;
                first = tmp;
            }
            if(first.contains(second)) {
                return first;
            }
            List<IdAndValue<V>> idAndValues = new ArrayList<>(first.all());
            idAndValues.addAll(second.query(first.end() + 1L));
            return new Range<>(first.start, second.isLast, idAndValues);
        }

        boolean contains(Range<V> that) {
            return start <= that.start && end() >= that.end();
        }

        int compareWithId(long id) {
            if(start > id) {
                return 1;
            }
            if(end() >= id) {
                return 0;
            }
            return -1;
        }

        long end() {
            return isLast() ? Long.MAX_VALUE : map.lastKey();
        }

        List<IdAndValue<V>> all() {
            return query(start, limit());
        }

        List<IdAndValue<V>> query(long id) {
            return query(id, limit());
        }

        long limit() {
            return map.size();
        }

        List<IdAndValue<V>> query(long id, long limit) {
            SortedMap<Long, V> tailMap = map.tailMap(id);
            List<IdAndValue<V>> result = new ArrayList<>();
            for (Map.Entry<Long, V> entry : tailMap.entrySet()) {
                if(result.size() >= limit) {
                    return result;
                }
                result.add(new IdAndValue<>(entry.getKey(), entry.getValue()));
            }
            return result;
        }

        boolean isLast() {
            return isLast;
        }

        boolean shouldMergeWith(Range<V> that) {
            return isSuccessorOf(that) || that.isSuccessorOf(this) || overlapsWith(that);
        }

        boolean isSuccessorOf(Range<V> that) {
            return start == that.end() + 1L;
        }

        boolean overlapsWith(Range<V> that) {
            return coversId(that.start) || that.coversId(start);
        }

        boolean coversId(long id) {
            return compareWithId(id) == 0;
        }

    }


}
