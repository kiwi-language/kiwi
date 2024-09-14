package asterisk;

import java.io.Serializable;

public interface AsteriskTypeFoo<T extends Serializable> {

    AsteriskTypeFoo<?> getInstance();

}
