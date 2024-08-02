package org.metavm.object.type;

import org.metavm.object.instance.core.Id;

public interface RedirectStatusProvider {

    RedirectStatus getRedirectStatus(Id id);

}
