package org.metavm.util;

import lombok.extern.slf4j.Slf4j;
import org.metavm.api.Entity;

import java.io.IOException;
import java.io.ObjectOutputStream;

@Slf4j
@Entity(ephemeral = true)
public class MvObjectOutputStream extends ObjectOutputStream {

    public static MvObjectOutputStream create(MarkingInstanceOutput output) {
        try {
            return new MvObjectOutputStream(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private final transient MarkingInstanceOutput out;

    public MvObjectOutputStream(MarkingInstanceOutput out) throws IOException {
        super();
        this.out = out;
    }

    public MarkingInstanceOutput getOut() {
        return out;
    }

}
