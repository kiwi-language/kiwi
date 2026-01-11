package org.manul;


import org.manul.util.Utils;

import lombok.Getter;

public class Lab {

    @Getter
    private String name;

    public static void main(String[] args) {
        var lab = new Lab();
        var name = lab.getName();
    }

}
