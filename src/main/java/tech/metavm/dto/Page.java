package tech.metavm.dto;

import java.util.List;

public record Page<T>(List<T> data, long total) {

    public boolean isEmpty() {
        return data.isEmpty();
    }

}
