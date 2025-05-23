package org.metavm.object.type;

import javax.annotation.Nullable;

public interface IndexedTypeProvider extends TypeProvider {

    @Nullable
    Klass findClassTypeByName(String name);

}
