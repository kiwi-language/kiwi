package org.metavm.autograph;

import org.metavm.util.Utils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class Namer {

    private final Set<String> generatedNames = new HashSet<>();

    public String newName(String nameRoot, Set<QualifiedName> reservedQns) {
        var pieces = nameRoot.split("_");
        var reservedNames = new HashSet<String>();
        for (var qn : reservedQns) {
            reservedNames.add(qn.toString());
        }
        int n;
        if(Utils.isDigits(pieces[pieces.length-1])) {
            nameRoot = Arrays.stream(pieces).limit(pieces.length-1).collect(Collectors.joining("_"));
            n = Integer.parseInt(pieces[pieces.length-1]);
        } else {
            n = 0;
        }
        var newName = nameRoot;
        while (reservedNames.contains(newName) || generatedNames.contains(newName)) {
            newName = nameRoot + "_" + ++n;
        }
        generatedNames.add(newName);
        return newName;
    }


}
