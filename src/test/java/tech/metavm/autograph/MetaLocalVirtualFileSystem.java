package tech.metavm.autograph;

import com.intellij.openapi.vfs.VirtualFile;
import tech.metavm.autograph.env.BinaryLightVirtualFile;
import tech.metavm.autograph.env.DirectoryLightVirtualFile;
import tech.metavm.util.InternalException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class MetaLocalVirtualFileSystem {

    public VirtualFile openFile(File file) {
        if (file.isDirectory()) {
            return new DirectoryLightVirtualFile(file);
        } else {
            return new BinaryLightVirtualFile(file);
        }
    }

}
