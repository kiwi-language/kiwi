package org.manul.classfile;

import org.manul.flow.Method;
import org.manul.object.type.Field;
import org.manul.object.type.Index;
import org.manul.object.type.Klass;

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
