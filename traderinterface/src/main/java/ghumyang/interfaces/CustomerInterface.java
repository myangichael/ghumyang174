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
            System.out.println("   (e) Exit");
            System.out.println();

            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","3","4","5","6","7","e"))); // get input

            // TODO: add switch statement to handle input
            switch (input) {
                case "0":
                    DepositHelper(marketAccount);
                    break;
            }
        }
        
    }

    static void DepositHelper(MarketAccount marketAccount) throws IOException {
        HashMap<String,String> fields = Global.promptValues("Deposit", new ArrayList<>(Arrays.asList("amount")));
        Global.confirmInfo("Deposit", fields);
        Global.awaitConfirmation();
        marketAccount.deposit(Integer.valueOf(fields.get("amount")));
    }

}
