package org.metavm.entity.natives;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.StdMethod;
import org.metavm.flow.Flows;
import org.metavm.object.instance.core.ArrayInstance;
import org.metavm.object.instance.core.ClassInstance;
import org.metavm.object.instance.core.LongValue;
import org.metavm.object.instance.core.Value;
import org.metavm.util.Instances;

import java.util.List;

@Slf4j
public class OutputStreamNative implements NativeBase {

    private final ClassInstance instance;

    public OutputStreamNative(ClassInstance instance) {
        this.instance = instance;
    }

    public Value write(Value b, Value off, Value len, CallContext callContext) {
        write0(b.resolveArray(),
                ((LongValue) off).getValue().intValue(),
                ((LongValue) len).getValue().intValue(),
                callContext);
        return Instances.nullInstance();
    }

    public Value write(Value b, CallContext callContext) {
        var buf = b.resolveArray();
        write0(buf, 0, buf.length(), callContext);
        return Instances.nullInstance();
    }

    private void write0(ArrayInstance buf, int o, int l, CallContext callContext) {
        var writeMethod = StdMethod.outputStreamWrite.get();
        for (int i = o; i < o + l; i++) {
            var v = ((LongValue) buf.get(i));
            Flows.invokeVirtual(writeMethod.getRef(), instance, List.of(v), callContext);
        }
    }

}
