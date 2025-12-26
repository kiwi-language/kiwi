package org.metavm.object.instance.core;

import org.jsonk.*;

public class IdAdapter implements Adapter<Id> {
    @Override
    public void init(AdapterEnv env) {

    }

    @Override
    public void toJson(Id o, JsonWriter writer) {
        writer.writeString(o.toString());
    }

    @Override
    public Id fromJson(JsonReader reader) {
        return Id.parse(reader.readString());
    }

    @Override
    public AdapterKey getKey() {
        return AdapterKey.of(Id.class);
    }
}
