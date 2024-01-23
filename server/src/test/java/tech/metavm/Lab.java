package tech.metavm;

import java.io.*;

public class Lab {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("/Users/leen/workspace/object/wad.txt"));
        int total = 0;
        int max = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            var n = Integer.parseInt(line.trim());
            max = Math.max(max, n);
            total += n;
        }
        System.out.println(max);
        System.out.println(total);
    }

}
