package org.metavm.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public record AuthConfig(
        String loginName,
        String password
) {

    public static AuthConfig fromFile(String filePath) {
        try (Scanner scanner = new Scanner(new FileInputStream(filePath))) {
            String loginName = scanner.next();
            String password = scanner.next();
            return new AuthConfig(loginName, password);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
