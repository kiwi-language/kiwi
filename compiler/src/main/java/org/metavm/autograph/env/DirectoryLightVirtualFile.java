package org.metavm.autograph.env;

import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.metavm.util.NncUtils;

import java.io.File;
import java.util.Objects;

public class DirectoryLightVirtualFile extends BinaryLightVirtualFile {

    @Nullable
    private VirtualFile[] children;

    public DirectoryLightVirtualFile(File file) {
        super(file);
        NncUtils.requireTrue(file.isDirectory());
    }

    @Override
    public VirtualFile[] getChildren() {
        ensureChildrenInitialized();
        return NncUtils.requireNonNull(children);
    }

    private void ensureChildrenInitialized() {
        if(children != null) {
            return;
        }
        synchronized (this) {
            if (children == null) {
                File[] files = Objects.requireNonNull(file.listFiles());
                VirtualFile[] children = new VirtualFile[files.length];
                for (int i = 0; i < files.length; i++) {
                    children[i] = ourFileSystem.findFileByPath(files[i].getPath());
                }
                setChildren(children);
            }
        }
    }

    private void setChildren(@NotNull VirtualFile[] children) {
        this.children = children;
    }

    @Override
    public boolean isDirectory() {
        return true;
    }

}
