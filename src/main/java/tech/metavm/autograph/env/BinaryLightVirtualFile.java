package tech.metavm.autograph.env;

// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.LocalTimeCounter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import tech.metavm.autograph.Keys;
import tech.metavm.util.NncUtils;

import java.io.*;
import java.util.Arrays;

/**
 * In-memory implementation of {@link VirtualFile}.
 */
public class BinaryLightVirtualFile extends LightVirtualFileBase {

    private static final byte[] BUF = new byte[1024 * 1024 * 64];

    protected final File file;

    private @Nullable byte[] myContent;

    public BinaryLightVirtualFile(File file) {
        this(file, null);
    }

    public BinaryLightVirtualFile(File file, byte @Nullable [] content) {
        this(file, null, content, LocalTimeCounter.currentTime());
    }

    public BinaryLightVirtualFile(File file, FileType fileType, byte @Nullable [] content) {
        this(file, fileType, content, LocalTimeCounter.currentTime());
    }

    public BinaryLightVirtualFile(File file, FileType fileType, byte @Nullable [] content, long modificationStamp) {
        super(file.getName(), fileType, modificationStamp);
        this.file = file;
        if (content != null) {
            setContent(content);
        }
    }

    @Override
    public @NotNull InputStream getInputStream() throws IOException {
        ensureContentInitialized();
        return VfsUtilCore.byteStreamSkippingBOM(NncUtils.requireNonNull(myContent), this);
    }

    private void ensureContentInitialized() throws IOException {
        if (myContent != null) {
            return;
        }
        synchronized (this) {
            if (myContent == null) {
                try (var input = new FileInputStream(file)) {
                    int n = input.read(BUF);
                    setContent(Arrays.copyOf(BUF, n));
                }
            }
        }
    }

    @Override
    @NotNull
    public OutputStream getOutputStream(Object requestor, final long newModificationStamp, long newTimeStamp) throws IOException {
        return VfsUtilCore.outputStreamAddingBOM(new ByteArrayOutputStream() {
            @Override
            public void close() {
                setModificationStamp(newModificationStamp);
                setContent(toByteArray());
            }
        }, this);
    }

    @Override
    public byte @NotNull [] contentsToByteArray() throws IOException {
        ensureContentInitialized();
        return NncUtils.requireNonNull(myContent);
    }

    private void setContent(byte @NotNull [] content) {
        //StringUtil.assertValidSeparators(content);
        myContent = content;
        var doc = FileDocumentManager.getInstance().getDocument(this);
        putUserData(Keys.META_CACHED_DOC, doc);
    }

    public byte @NotNull [] getContent() {
        return myContent;
    }

    @Override
    public String toString() {
        return "BinaryLightVirtualFile: " + getPresentableUrl();
    }
}
