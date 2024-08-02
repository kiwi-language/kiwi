package org.metavm.object.instance.core;

import org.metavm.object.type.RedirectStatus;
import org.metavm.util.InstanceOutput;
import org.metavm.util.WireTypes;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Supplier;

public class RedirectingReference extends InstanceReference {

    private final InstanceReference redirectionReference;
    private final RedirectStatus status;

    public RedirectingReference(@Nullable Id id, Supplier<DurableInstance> resolver,
                                InstanceReference redirectingReference, RedirectStatus status) {
        super(id, resolver);
        this.redirectionReference = redirectingReference;
        this.status = status;
    }

    public RedirectingReference(DurableInstance resolved, InstanceReference redirectionReference, RedirectStatus status) {
        super(resolved);
        this.redirectionReference = redirectionReference;
        this.status = status;
    }

    @Override
    public void writeRecord(InstanceOutput output) {
        output.write(WireTypes.REDIRECTING_RECORD);
        Objects.requireNonNull(redirectionReference).write(output);
        output.writeId(status.getId());
        super.writeRecord(output);
    }

    @Override
    public void write(InstanceOutput output) {
        if (isInlineValueReference())
            writeRecord(output);
        else {
            output.write(WireTypes.REDIRECTING_REFERENCE);
            output.write(getFlags());
            output.writeId(getId());
            redirectionReference.write(output);
            output.writeId(status.getId());
        }
    }

    @Override
    public DurableInstance resolve() {
        return shouldRedirect() ? redirectionReference.resolve() : super.resolve();
    }

    public boolean isResolved() {
        return shouldRedirect() ? redirectionReference.isResolved() : super.isResolved();
    }

    public boolean shouldRedirect() {
        return status.shouldRedirect();
    }

    public InstanceReference tryRedirect() {
        if(shouldRedirect())
            return redirectionReference.tryRedirect();
        else
            return this;
    }

    @Override
    public boolean equals(Object obj) {
        if(shouldRedirect())
            return Objects.requireNonNull(redirectionReference).equals(obj);
        else
            return super.equals(obj);
    }

    public InstanceReference getRedirectionReference() {
        return redirectionReference;
    }

    public RedirectStatus getStatus() {
        return status;
    }
}
