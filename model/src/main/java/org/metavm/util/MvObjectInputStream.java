package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;

import java.io.IOException;
import java.io.ObjectInputStream;

@Slf4j
@Entity(ephemeral = true)
public class MvObjectInputStream extends ObjectInputStream {

    public static MvObjectInputStream create(InstanceInput input) {
        try {
            return new MvObjectInputStream(input);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final transient InstanceInput input;

    protected MvObjectInputStream(InstanceInput input) throws IOException {
        this.input = input;
    }

    public InstanceInput getInput() {
        return input;
    }

}
