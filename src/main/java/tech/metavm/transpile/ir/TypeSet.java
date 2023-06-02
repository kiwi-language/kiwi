package tech.metavm.transpile.ir;

import tech.metavm.transpile.TypeRange;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public interface TypeSet  {

    default boolean containsElement(AtomicType type) {
        for (TypeRange range : ranges()) {
            if(range.contains(type)) {
                return true;
            }
        }
        return false;
    }

    default TypeSet intersect(TypeSet that) {
        var ranges = ranges();
        var thatRanges = new ArrayList<>(that.ranges());

        List<TypeRange> intersections = new ArrayList<>();
        for (TypeRange r1 : ranges) {
            Iterator<TypeRange> it = thatRanges.iterator();
            while (it.hasNext()) {
                var r2 = it.next();
                if(r1.overlapsWith(r2)) {
                    r1 = r1.intersect(r2);
                    it.remove();
                }
            }
            intersections.add(r1);
        }
        if(intersections.size() == 1) {
            var r = intersections.get(0);
            return new IRWildCardType(
                    List.of(r.getLowerBound()),
                    List.of(r.getUpperBound())
            );
        }
        else {
            return new GeneralTypeSet(intersections);
        }
    }

    default TypeSet union(TypeSet that) {
        var ranges = ranges();
        var thatRanges = new ArrayList<>(that.ranges());

        List<TypeRange> mergedRanges = new ArrayList<>();
        for (TypeRange r1 : ranges) {
            Iterator<TypeRange> it = thatRanges.iterator();
            while (it.hasNext()) {
                var r2 = it.next();
                if(r1.canMergeWith(r2)) {
                    r1 = r1.mergeWith(r2);
                    it.remove();
                }
            }
            Iterator<TypeRange> it2 = mergedRanges.iterator();
            while (it2.hasNext()) {
                var r = it2.next();
                if(r1.canMergeWith(r)) {
                    r1 = r1.mergeWith(r);
                    it2.remove();
                }
            }
            mergedRanges.add(r1);
        }
        mergedRanges.addAll(thatRanges);
        if(mergedRanges.size() == 1) {
            var r = mergedRanges.get(0);
            return new IRWildCardType(
                    List.of(r.getLowerBound()),
                    List.of(r.getUpperBound())
            );
        }
        else {
            return new GeneralTypeSet(mergedRanges);
        }
    }

    List<TypeRange> ranges();

}
