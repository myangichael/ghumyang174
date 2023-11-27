package ghumyang.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import ghumyang.Global;
import ghumyang.tables.Customer;
import ghumyang.tables.MarketAccount;

public class CustomerInterface {

    // initial landing page to login to customer account
    public static void Login() throws IOException {

        String[] loginInfo = Global.getLogin(); // prompt user for login info

        if (!Customer.checkLogin(loginInfo[0], loginInfo[1])) { // check login validity for customer
            Global.clearScreen();
            System.out.println();
            System.out.println("---INVALID LOGIN INFO---"); // if invalid login info jump back
            Global.awaitConfirmation();
            return;
        }

        Customer customer = new Customer(loginInfo[0], loginInfo[1]); // if valid login info continue to account page
        MarketAccountPage(customer);
    }

    public static void MarketAccountPage(Customer customer) throws IOException {

        String input = "start";

        while (!input.equals("e")) {
            MarketAccount marketAccount = new MarketAccount(customer.getCustomerId()); // load data from this Customer

             // hard coded options
            Global.clearScreen();
            System.out.println("Welcome, " + customer.getName() + "!");
            System.out.println();
            System.out.println(String.format("Account ID: %d, total balance is: %1.2f", marketAccount.getCustomerId(), marketAccount.getBalance()));
            System.out.println();
            System.out.println("Options:");
            System.out.println("   (0) Make a deposit");
            System.out.println("   (1) Make a withdrawal");
            System.out.println("   (2) Buy stock");
            System.out.println("   (3) Sell stock");
            System.out.println("   (4) Cancel a transaction");
            System.out.println("   (5) Show balance");
            System.out.println("   (6) Show this month's transaction history");
            System.out.println("   (7) Display my user information");
            System.out.println("   (e) Exit / Log Out");
            System.out.println();

            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","3","4","5","6","7","e"))); // get input

            // TODO: add switch statement to handle input
            switch (input) {
                case "0":
                    deposit(marketAccount);
                    break;
                case "1":
                    withdrawal(marketAccount);
                    break;
                case "2":
                    buyStock(marketAccount);
                    break;
                case "3":
                    sellStock(marketAccount);
                    break;
            }
        }
        
    }

    static void deposit(MarketAccount marketAccount) throws IOException {
        String title = "Deposit";
        HashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Amount")));
        Global.confirmInfo(title, fields);
        Global.awaitConfirmation();
        if (!Global.isDouble(fields.get("Amount"))) {
            Global.errorMessage("Inputted amount is invalid");
            return;
        }
        marketAccount.deposit(Double.valueOf(fields.get("Amount")));
    }

    static void withdrawal(MarketAccount marketAccount) throws IOException {
        String title = "Withdrawal";
        HashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Amount")));
        Global.confirmInfo(title, fields);
        Global.awaitConfirmation();
        if (!Global.isDouble(fields.get("Amount"))) {
            Global.errorMessage("Inputted amount is invalid");
            return;
        }
        marketAccount.withdrawal(Double.valueOf(fields.get("Amount")));
    }

    static void buyStock(MarketAccount marketAccount) throws IOException {
        String title = "Buy Stock";
        HashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Ticker","Count")));
        Global.confirmInfo(title, fields);
        Global.awaitConfirmation();
        if (!Global.isInteger(fields.get("Count"))) {
            Global.errorMessage("Inputted count is invalid");
            return;
        }
        marketAccount.buyStock(fields.get("Ticker"), Integer.valueOf(fields.get("Count")));
    }

    static void sellStock(MarketAccount marketAccount) throws IOException {
        String title = "Sell Stock";
        HashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Ticker","Count")));
        Global.confirmInfo(title, fields);
        Global.awaitConfirmation();
        if (!Global.isInteger(fields.get("Count"))) {
            Global.errorMessage("Inputted count is invalid");
            return;
        }
        marketAccount.sellStock(fields.get("Ticker"), Integer.valueOf(fields.get("Count")));
    }

    static void cancelTransaction(MarketAccount marketAccount) throws IOException {
        String title = "Withdrawal";
        HashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("TransactionId")));
        Global.confirmInfo(title, fields);
        Global.awaitConfirmation();
        if (!Global.isInteger(fields.get("TransactionId"))) {
            Global.errorMessage("Inputted count is invalid");
            return;
        }
        marketAccount.cancelTransaction(Integer.valueOf(fields.get("TransactionId")));
    }

    static void showBalance(MarketAccount marketAccount) throws IOException {
        System.out.println("YOUR BALANCE IS: " + marketAccount.getBalance());
        Global.awaitConfirmation();
    }

    static void monthTransactionHistory(MarketAccount marketAccount) throws IOException {

    }

    static void displayInfo(MarketAccount marketAccount) throws IOException {

    }

}
