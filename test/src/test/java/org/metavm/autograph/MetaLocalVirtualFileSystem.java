package org.metavm.autograph;

import com.intellij.openapi.vfs.VirtualFile;
import org.metavm.autograph.env.BinaryLightVirtualFile;
import org.metavm.autograph.env.DirectoryLightVirtualFile;

import java.io.File;

public class MetaLocalVirtualFileSystem {

    public VirtualFile openFile(File file) {
        if (file.isDirectory()) {
            return new DirectoryLightVirtualFile(file);
        } else {
            return new BinaryLightVirtualFile(file);
        }
    }

}
