package org.manul.object.type;

import java.util.List;

public record KlassAndFields(
        Klass klass,
        List<Field> fields
) {
}
