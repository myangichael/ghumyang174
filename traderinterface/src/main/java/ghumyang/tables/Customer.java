package ghumyang.tables;
import lombok.Getter;

import java.io.IOException;

public class Customer {

    @Getter String name;
    @Getter String state;
    @Getter String phoneNumber;
    @Getter String emailAddress;
    @Getter String taxID;
    @Getter String username;
    @Getter String password;
    @Getter int customerId;

    public Customer(String username, String password) {
        name = "placeholder";
    }

    public static boolean checkLogin(String username, String password) throws IOException {
        // TODO: check DB for SQL entry containing this loginInfo pair
        return true;
    }
    
}
