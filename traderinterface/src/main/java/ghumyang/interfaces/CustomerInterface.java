package ghumyang.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import ghumyang.Global;
import ghumyang.tables.Customer;
import ghumyang.tables.MarketAccount;

public class CustomerInterface {

    // initial landing page to login to customer account
    public static void Login() throws IOException {

        // prompt user for login info
        String[] loginInfo = Global.getLogin();

        // validate login
        if (!Customer.checkLogin(loginInfo[0], loginInfo[1])) {
            Global.messageWithConfirm("ERROR: customer login info is invalid :(");
            return;
        }

        // if valid login info continue to account page
        Customer customer = new Customer(loginInfo[0], loginInfo[1]);
        MarketAccountPage(customer);
    }

    // landing page for managing customer account
    static void MarketAccountPage(Customer customer) throws IOException {

        String input = "start";

        // generic input loop, will loop user commands until user exits
        while (!input.equals("e")) {

            // reload data from this market account every loop
            MarketAccount marketAccount = new MarketAccount(customer.getCustomerId()); 

            // hard coded options and switch statements for navigation
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
            System.out.println("   (e) Exit to main menu / Log Out");
            System.out.println();
            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","3","4","5","6","7","e")));
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
                case "4":
                    cancelTransaction(marketAccount);
                    break;
                case "5":
                    showBalance(marketAccount);
                    break;
                case "6":
                    monthTransactionHistory(marketAccount);
                    break;
                case "7":
                    displayInfo(customer);
                    break;
            }
        }
    }

    static void deposit(MarketAccount marketAccount) throws IOException {
        String title = "Deposit";

        // prompt for deposit amount
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Amount")));

        // input validation
        if (!Global.isDouble(fields.get("Amount"))) {
            Global.messageWithConfirm("ERROR: inputted amount is invalid, should be a DECIMAL NUMBER");
            return;
        }

        // deposit query
    }

    static void withdrawal(MarketAccount marketAccount) throws IOException {
        String title = "Withdrawal";

        // prompt for withdrawal amount
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Amount")));

        // input validation
        if (!Global.isDouble(fields.get("Amount"))) {
            Global.messageWithConfirm("ERROR: inputted amount is invalid, should be a DECIMAL NUMBER");
            return;
        }

        // ensure there is enough balance to withdraw this amount
        if (marketAccount.getBalance() < Double.valueOf(fields.get("Amount"))) {
            Global.messageWithConfirm("ERROR: not enough money to withdraw");
        }

        // withdraw query
    }

    static void buyStock(MarketAccount marketAccount) throws IOException {

        // ensure market is open
        if (!Global.MARKET_IS_OPEN) {
            Global.messageWithConfirm("Sorry, the market is closed and you cannot take this action");
        }

        String title = "Buy Stock";

        // prompt for stock ticker, shares to buy
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Ticker","Count")));

        // input validation
        if (!Global.isInteger(fields.get("Count"))) {
            Global.messageWithConfirm("ERROR: inputted count is invalid, should be an INTEGER");
            return;
        }
        if (fields.get("Ticker").equals("")) {
            Global.messageWithConfirm("ERROR: Ticker is empty");
            return;
        }

        // query stock info

        // ensure there is enough balance to buy this many shares
        
        // buy query
    }

    static void sellStock(MarketAccount marketAccount) throws IOException {

        // ensure market is open
        if (!Global.MARKET_IS_OPEN) {
            Global.messageWithConfirm("Sorry, the market is closed and you cannot take this action");
        }

        String title = "Sell Stock";

        // prompt for stock ticker, shares to sell
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Ticker","Count","Purchased Price")));

        // input validation
        if (!Global.isInteger(fields.get("Count"))) {
            Global.messageWithConfirm("ERROR: inputted count is invalid, should be an INTEGER");
            return;
        }
        if (!Global.isInteger(fields.get("Purchased Price"))) {
            Global.messageWithConfirm("ERROR: inputted purchase price is invalid, should be an INTEGER");
            return;
        }
        if (fields.get("Ticker").equals("")) {
            Global.messageWithConfirm("ERROR: Ticker is empty");
            return;
        }

        // ensure that the user owns this stock at this purchase price

        // ensure that the user has enough stock at this purchase price to sell

        // sell query
    }

    static void cancelTransaction(MarketAccount marketAccount) throws IOException {

        // ensure market is open
        if (!Global.MARKET_IS_OPEN) {
            Global.messageWithConfirm("Sorry, the market is closed and you cannot take this action");
        }

        String title = "Cancel Transaction";

        // prompt for transaction to cancel
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("TransactionId")));

        // input validation
        if (!Global.isInteger(fields.get("TransactionId"))) {
            Global.messageWithConfirm("ERROR: inputted TransactionId is invalid, should be an INTEGER");
            return;
        }

        // ensure this transaction exists

        // ensure date is same
        
        // cancel transaction
    }

    static void showBalance(MarketAccount marketAccount) throws IOException {
        // prints balance across all stock accounts belonging to this market account from query
        System.out.println("YOUR BALANCE IS: " + marketAccount.getBalance());
        Global.awaitConfirmation();
    }

    static void monthTransactionHistory(MarketAccount marketAccount) throws IOException {
        // prints all transactions of this month from query
        System.out.println("This current month's transaction history is listed below: ");
    }

    static void displayInfo(Customer customer) throws IOException {
        // prints below info
        String[] userInfo = new String[] {
            "Your Personal Information",
            "",
            "         Name | " + customer.getName(),
            "        State | " + customer.getState(),
            " Phone Number | " + customer.getPhoneNumber(),
            "Email Address | " + customer.getEmailAddress(),
            "       Tax ID | " + customer.getTaxID(),
            "     Username | " + customer.getUsername(),
            "     Password | " + customer.getPassword(),
            "  Customer ID | " + String.valueOf(customer.getCustomerId())
        };
        Global.messageWithConfirm(userInfo);
    }

}
