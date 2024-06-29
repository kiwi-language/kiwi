package org.metavm.object.instance.core;

import org.metavm.object.instance.persistence.IndexEntryPO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public record WALIndexQueryResult(
        List<IndexEntryPO> hits,
        List<IndexEntryPO> removed
) {

    public List<IndexEntryPO> mergeWith(List<IndexEntryPO> entries) {
        var set = new HashSet<>(removed);
        set.addAll(hits);
        var result = new ArrayList<>(hits);
        for (IndexEntryPO entry : entries) {
            if(!set.contains(entry))
                result.add(entry);
        }
        return result;
    }

}
