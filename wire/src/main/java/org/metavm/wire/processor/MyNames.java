package org.metavm.wire.processor;

import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;

public class MyNames {
    
    private final Names names;
    final Name _this;
    final Name _super;
    final Name _init;
    final Name _class;
    final Name input;
    final Name output;
    final Name visitor;
    final Name adapter;
    final Name parent;
    final Name EntityAdapter;
    final Name __WireAdapter__;
    final Name adapterRegistry;
    final Name getAdapter;
    final Name init;
    final Name write;
    final Name writeInt;
    final Name read;
    final Name visit;
    final Name o;
    final Name __input__;
    final Name __read__;
    final Name __visit__;
    final Name __write__;
    final Name getSupportedTypes;
    final Name getTag;
    final Name value;
    final Name org_metavm_entity_Entity;
    final Name name;
    final Name valueOf;
    final Name onRead;
    final Name subTypes;
    final Name type;
    final Name tag;
    final Name of;
    final Name code;
    final Name fromCode;

    public MyNames(Names names) {
        this.names = names;
        _this = names._this;
        _super = names._super;
        _init = names.init;
        _class = names._class;
        input = fromString("input");
        output = fromString("output");
        visitor = fromString("visitor");
        adapter = fromString("adapter");
        parent = fromString("parent");
        EntityAdapter = fromString("EntityAdapter");
        __WireAdapter__ = fromString("__WireAdapter__");
        adapterRegistry = fromString("adapterRegistry");
        getAdapter = fromString("getAdapter");
        init = fromString("init");
        write = fromString("write");
        writeInt = fromString("writeInt");
        read = fromString("read");
        visit = fromString("visit");
        o = fromString("o");
        getSupportedTypes = fromString("getSupportedTypes");
        getTag = fromString("getTag");
        __input__ = fromString("__input__");
        __read__ = fromString("__read__");
        __write__ = fromString("__write__");
        __visit__ = fromString("__visit__");
        value = fromString("value");
        org_metavm_entity_Entity = fromString("org.metavm.entity.Entity");
        name = names.fromString("name");
        valueOf = names.fromString("valueOf");
        onRead = fromString("onRead");
        subTypes = fromString("subTypes");
        type = fromString("type");
        tag = fromString("tag");
        of = fromString("of");
        code = fromString("code");
        fromCode = fromString("fromCode");
    }

    Name fromString(String s) {
        return names.fromString(s);
    }
    
}
