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
            return;
        }

        Customer customer = new Customer(loginInfo[0], loginInfo[1]); // if valid login info continue to account page
        AccountPage(customer);
    }

    // once successfully logged in, view account options
    public static void AccountPage(Customer customer) throws IOException{
        String input = "start";
        
        // TODO: replace list of all accounts associated with this customer and put here, function predefined as Customer.getMarketAccounts()
        ArrayList<MarketAccount> marketAccountList = new ArrayList<>();

        // BEGIN TESTING

        MarketAccount temp1 = new MarketAccount(0);
        MarketAccount temp2 = new MarketAccount(11);
        MarketAccount temp3 = new MarketAccount(222);
        marketAccountList.add(temp1);
        marketAccountList.add(temp2);
        marketAccountList.add(temp3);

        // END TESTING

        HashMap<String, MarketAccount> options = new HashMap<>(); // create mapping of all ids to their MarketAccounts
        for (MarketAccount marketAccount : marketAccountList) {
            options.put(String.valueOf(marketAccount.getId()), marketAccount);
        }
        options.put("e", null); // add option to exit

        while (!input.equals("e")) {
            Global.clearScreen();
            System.out.println();
            System.out.println(String.format("Welcome, %s", customer.getName()));
            System.out.println();
            System.out.println("Here is a list of your accounts: ");
            System.out.println("   id | balance");

            // for each MarketAccount associated with this Customer, output that MarketAccount's id and balance
            for (MarketAccount marketAccount : marketAccountList) {
                System.out.println(String.format("%5d | %10.2f", marketAccount.getId(), marketAccount.getBalance()));
            }

            System.out.println("Choose an account to take action in, or enter \"e\" to return home");
            System.out.println();

            input = Global.getLineSetInputs(new ArrayList<>((options.keySet()))); // get input

            if (input.equals("e")) {
                break; // exit condition
            } else {
                MarketAccountPage(customer, options.get(input)); // otherwise enter page for that Market Account
            }
        }

        Global.clearScreen();
        return;
    }

    public static void MarketAccountPage(Customer customer, MarketAccount marketAccount) throws IOException {

        String input = "start";

        while (!input.equals("e")) {
            
            // hard coded options
            Global.clearScreen();
            System.out.println(String.format("Account ID: %d, total balance is: %1.2f", marketAccount.getId(), marketAccount.getBalance()));
            System.out.println();
            System.out.println("Options:");
            System.out.println("   (0) Make a deposit");
            System.out.println("   (1) Make a withdrawal");
            System.out.println("   (2) Buy stock");
            System.out.println("   (3) Sell stock");
            System.out.println("   (4) Cancel a transaction");
            System.out.println("   (5) Show balance");
            System.out.println("   (6) Show this month's transaction history");
            System.out.println("   (7) Get information about a stock");
            System.out.println("   (8) Get information about a movie");
            System.out.println("   (8) List top movies within a time period");
            System.out.println("   (9) Display reviews for a movie");
            System.out.println("   (10) Display my user information");
            System.out.println("   (e) Exit");
            System.out.println();
            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","3","4","5","6","7","8","9","10","e"))); // get input

            // TODO: add switch statement to handle input

            if (!input.equals("e")) Global.awaitConfirmation();
        }

        return;
    }
    
}
