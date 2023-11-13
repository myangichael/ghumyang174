package ghumyang.tables;
import lombok.Getter;

import java.io.IOException;
import java.util.ArrayList;

public class Customer {

    @Getter String name;
    @Getter String state;
    @Getter String phoneNumber;
    @Getter String emailAddress;
    @Getter String taxID;
    @Getter String username;
    @Getter String password;
    @Getter String customerId;

    public Customer(String username, String password) {
        name = "deez nuts";
    }

    public static boolean checkLogin(String username, String password) throws IOException {
        // TODO: check DB for SQL entry containing this loginInfo pair
        return true;
    }

    public ArrayList<MarketAccount> getAccounts() {
        return new ArrayList<>();
    }
    
}
