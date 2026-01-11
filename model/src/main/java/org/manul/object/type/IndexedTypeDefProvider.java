package org.manul.object.type;

import javax.annotation.Nullable;

public interface IndexedTypeDefProvider extends TypeDefProvider {

    @Nullable Klass findKlassByName(String name);

}
