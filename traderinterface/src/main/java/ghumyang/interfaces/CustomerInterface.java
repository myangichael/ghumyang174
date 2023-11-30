package ghumyang.interfaces;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;

import ghumyang.Global;
import ghumyang.tables.Customer;

public class CustomerInterface {

    // initial landing page to login to customer account
    public static void Login() throws IOException {

        // prompt user for login info
        String[] loginInfo = Global.getLogin();

        // validate login
        if (!Customer.isThereUserWithThisLogin(loginInfo[0], loginInfo[1])) {
            Global.messageWithConfirm("ERROR: customer login info is invalid :(");
            return;
        }

        // if valid login info continue to account page
        MarketAccountPage(loginInfo[0], loginInfo[1]);
    }

    // landing page for managing customer account
    static void MarketAccountPage(String username, String password) throws IOException {

        String input = "start";

        // generic input loop, will loop user commands until user exits
        while (!input.equals("e")) {

            // reload data from this customer and market account every loop
            Customer customer = new Customer(username, password);
            
            // updating balance history in accordance to loaded balance
            customer.updateBalanceHistory();

            // hard coded options and switch statements for navigation
            Global.clearScreen();
            System.out.println("Welcome, " + customer.getName() + "!");
            System.out.println();
            System.out.println(String.format("Account ID: %d, total balance is: %1.2f", customer.getCustomer_id(), customer.getBalance()));
            System.out.println();
            Global.printMarketInfo();
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
                    deposit(customer);
                    break;
                case "1":
                    withdrawal(customer);
                    break;
                case "2":
                    buyStock(customer);
                    break;
                case "3":
                    sellStock(customer);
                    break;
                case "4":
                    cancelTransaction(customer);
                    break;
                case "5":
                    showBalance(customer);
                    break;
                case "6":
                    monthTransactionHistory(customer);
                    break;
                case "7":
                    displayInfo(customer);
                    break;
            }
        }
    }

    static void deposit(Customer customer) throws IOException {
        String title = "Deposit";

        // prompt for deposit amount
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Amount")));

        // input validation
        if (!Global.isDouble(fields.get("Amount"))) {
            Global.messageWithConfirm("ERROR: inputted amount is invalid, should be a DECIMAL NUMBER");
            return;
        }

        double amount = Double.valueOf(fields.get("Amount"));

        if (amount < 0) {
            Global.messageWithConfirm("ERROR: cannot deposit negative money");
            return;
        }

        // deposit query
        customer.deposit(Double.parseDouble(fields.get("Amount")), true);
    }

    static void withdrawal(Customer customer) throws IOException {
        String title = "Withdrawal";

        // prompt for withdrawal amount
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Amount")));

        // input validation
        if (!Global.isDouble(fields.get("Amount"))) {
            Global.messageWithConfirm("ERROR: inputted amount is invalid, should be a DECIMAL NUMBER");
            return;
        }

        double amount = Double.valueOf(fields.get("Amount"));

        // ensure there is enough balance to withdraw this amount
        if (customer.getBalance() < amount) {
            Global.messageWithConfirm("ERROR: not enough money to withdraw");
            return;
        }

        // ensure value is not negative
        if (amount < 0) {
            Global.messageWithConfirm("ERROR: cannot withdraw negative money");
            return;
        }

        // withdraw query
        customer.withdrawal(Double.parseDouble(fields.get("Amount")), true);
    }

    static void buyStock(Customer customer) throws IOException {

        // ensure market is open
        if (!Global.MARKET_IS_OPEN) {
            Global.messageWithConfirm("Sorry, the market is closed and you cannot take this action");
        }

        String title = "Buy Stock";

        // prompt for stock ticker, shares to buy
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Symbol","Count")));

        // input validation
        if (!Global.isInteger(fields.get("Count"))) {
            Global.messageWithConfirm("ERROR: inputted count is invalid, should be an INTEGER");
            return;
        }
        if (fields.get("Symbol").equals("")) {
            Global.messageWithConfirm("ERROR: Symbol is empty");
            return;
        }

        String symbol = fields.get("Symbol");
        int count = Integer.parseInt(fields.get(("Count")));

        // can't buy negative or 0 shares
        if (count <= 0) {
            Global.messageWithConfirm("ERROR: can't buy negative shares");
            return;
        }
        
        // buy query
        customer.buyStock(symbol, count);
    }

    static void sellStock(Customer customer) throws IOException {

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
        if (!Global.isDouble(fields.get("Purchased Price"))) {
            Global.messageWithConfirm("ERROR: inputted purchase price is invalid, should be a DOUBLE");
            return;
        }
        if (fields.get("Ticker").equals("")) {
            Global.messageWithConfirm("ERROR: Ticker is empty");
            return;
        }

        // ensure that the user owns this stock at this purchase price

        // ensure there is enough money to pay commission prior to sell

        // ensure that the user has enough stock at this purchase price to sell

        // sell query
    }

    static void cancelTransaction(Customer customer) throws IOException {

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

    static void showBalance(Customer customer) throws IOException {
        // prints balance across all stock accounts belonging to this market account from query
        System.out.println("YOUR BALANCE IS: " + customer.getBalance());
        Global.awaitConfirmation();
    }

    static void monthTransactionHistory(Customer customer) throws IOException {
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
            " Phone Number | " + customer.getPhone_number(),
            "Email Address | " + customer.getEmail_address(),
            "       Tax ID | " + customer.getTax_id(),
            "     Username | " + customer.getUsername(),
            "     Password | " + customer.getPassword(),
            "  Customer ID | " + String.valueOf(customer.getCustomer_id())
        };
        Global.messageWithConfirm(userInfo);
    }

}
