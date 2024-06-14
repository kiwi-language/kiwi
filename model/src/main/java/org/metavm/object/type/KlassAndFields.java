package org.metavm.object.type;

import java.util.List;

public record KlassAndFields(
        Klass klass,
        List<Field> fields
) {
}
