package ghumyang.tables;

import lombok.Getter;

public class MarketAccount {
    @Getter Integer id;
    @Getter Double balance;

    // BEGIN TESTING
    public MarketAccount(int n) {
        id = n;
        balance = Math.random() * 10000.0;
    }
    // END TESTING
}
