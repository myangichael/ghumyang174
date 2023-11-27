package ghumyang.tables;
import lombok.Getter;

import java.util.ArrayList;

import ghumyang.tables.stocks.StockAccount;
import ghumyang.tables.transactions.Transaction;

public class MarketAccount {

    @Getter int customerId;
    @Getter double balance;
    @Getter ArrayList<StockAccount> stockAccounts;
    @Getter ArrayList<Transaction> transactionHistory;

    public MarketAccount(int customerId) {

    }

    public void deposit(double amount) {

    }
    
    public void withdrawal(double amount) {

    }

    public void buyStock(String ticker, int count) {

    }

    public void sellStock(String ticker, int count) {

    }

    public void cancelTransaction(int transactionId) {

    }

}
