package org.metavm.autograph;

import org.metavm.entity.ChildList;

import java.io.File;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class AutographLab {

    public static void main(String[] args) throws URISyntaxException {
        File file = Paths.get(ChildList.class.getProtectionDomain().getCodeSource().getLocation().toURI()).toFile();
        System.out.println(file.getPath());
    }

}