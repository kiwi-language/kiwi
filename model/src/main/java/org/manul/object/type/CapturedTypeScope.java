package org.manul.object.type;

import org.manul.api.Entity;
import org.manul.flow.Flow;

import javax.annotation.Nullable;
import java.util.List;

@Entity
public interface CapturedTypeScope {

    String getStringId();

    List<CapturedTypeVariable> getCapturedTypeVariables();

    int getCapturedTypeVariableIndex(CapturedTypeVariable capturedTypeVariable);

    String getInternalName(@Nullable Flow current);

    void addCapturedTypeVariable(CapturedTypeVariable capturedTypeVariable);

    String getScopeName();

    ConstantPool getConstantPool();
}
