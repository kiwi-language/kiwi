package org.metavm.flow;

import lombok.extern.slf4j.Slf4j;
import org.metavm.entity.Entity;
import org.metavm.entity.EntityRepository;
import org.metavm.entity.EntityRegistry;
import org.metavm.object.instance.core.*;
import org.metavm.object.instance.core.Value;
import org.metavm.object.type.Klass;
import org.metavm.util.MvInput;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class KlassInput extends MvInput {

    private final Map<Id, Instance> cached = new HashMap<>();
    private final EntityRepository repository;

    public KlassInput(InputStream in, EntityRepository repository) {
        super(in);
        this.repository = repository;
    }

    @Override
    public Message readTree() {
        return readEntityMessage();
    }

    @Override
    public Value readRemovingInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readValueInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readRelocatingInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readRedirectingInstance() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Value readRedirectingReference() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reference readReference() {
        return repository.createReference(readId());
    }

    @Override
    public Value readFlaggedReference() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected <T extends Entity> T getEntity(Class<T> klass, Id id) {
        var existing = klass.cast(cached.get(id));
        if (existing != null)
            return existing;
        existing = repository.getEntity(klass, id);
        if (existing != null)
            return existing;
        var newEntity = super.getEntity(klass, id);
        cached.put(id, newEntity);
        return newEntity;
    }

    @Override
    public Klass readEntityMessage() {
        return readEntity(Klass.class, null);
    }

    @Override
    public <T extends Entity> T readEntity(Class<T> klass, Entity parent) {
        var e =  super.readEntity(klass, parent);
        if (e.tryGetId() instanceof TmpId tmpId && repository.getEntity(klass, tmpId) == null)
            repository.bind(e);
        return e;
    }
}
