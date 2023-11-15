package ghumyang.tables;

import java.io.IOException;

import lombok.Getter;

public class Manager {

    @Getter String username;
    @Getter String password;

    public static boolean checkLogin(String username, String password) throws IOException {
        // TODO: check DB for SQL entry containing this loginInfo pair
        return true;
    }
}
