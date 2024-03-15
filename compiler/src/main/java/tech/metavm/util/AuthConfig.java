package tech.metavm.util;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Scanner;

public record AuthConfig(
        String appId,
        String loginName,
        String password
) {

    public static AuthConfig fromFile(String filePath) {
        try (Scanner scanner = new Scanner(new FileInputStream(filePath))) {
            String appId = scanner.next();
            String loginName = scanner.next();
            String password = scanner.next();
            return new AuthConfig(appId, loginName, password);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

}
