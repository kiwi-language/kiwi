package org.metavm.object.type;

import javax.annotation.Nullable;

public interface IndexedTypeDefProvider extends TypeDefProvider {

    @Nullable Klass findKlassByName(String name);

}
