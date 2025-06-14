package org.metavm.classfile;

import org.metavm.flow.Method;
import org.metavm.object.type.Field;
import org.metavm.object.type.Index;
import org.metavm.object.type.Klass;

public interface ClassFileListener {

    void onFieldCreate(Field field);
 
    void beforeFieldUpdate(Field field);
 
    void onFieldUpdate(Field field);

    void beforeKlassCreate();

    void onKlassCreate(Klass klass);
 
    void beforeKlassUpdate(Klass klass);
 
    void onKlassUpdate(Klass klass);

    void onKlassRemove(Klass klass);

    void onMethodRead(Method method);

    void onIndexRead(Index index);

}
