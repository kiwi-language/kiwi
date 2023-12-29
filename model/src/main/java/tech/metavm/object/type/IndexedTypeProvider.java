package tech.metavm.object.type;

import javax.annotation.Nullable;

public interface IndexedTypeProvider extends TypeProvider {

    @Nullable
    ClassType findClassTypeByName(String name);

}
