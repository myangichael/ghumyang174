package ghumyang;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;

import java.sql.SQLException;
import ghumyang.interfaces.CustomerInterface;
import ghumyang.interfaces.GeneralInterface;
import ghumyang.interfaces.ManagerInterface;
import ghumyang.interfaces.MarketInfoInterface;
import ghumyang.interfaces.NewCustomerInterface;

public class Main {
    public static void main(String args[]) throws IOException, SQLException {
        Global.clearScreen();
        Global.getPassword();
        Global.connection();
        Global.loadMarketInfo();
        MainPage();
    }
    
    public static void MainPage() throws IOException, SQLException {

        String input = "start";
        
        while (!input.equals("e")) {

            // menu output
            Global.clearScreen();
            System.out.println("Hello! Welcome to Garrett and Michael's Trader Interface.");
            System.out.println();
            System.out.println("The current date is: " + Global.CURRENT_DATE.toString());
            if (Global.MARKET_IS_OPEN) System.out.println("The market is currently OPEN");
            else System.out.println("The market is currently CLOSED");
            System.out.println();
            System.out.println("Options:");
            System.out.println("   (0) Login to a customer account");
            System.out.println("   (1) Login to a manager account");
            System.out.println("   (2) Register a new customer account");
            System.out.println("   (3) Query general stock and movie info");
            System.out.println("   (4) Update the market info");
            System.out.println("   (e) Exit the program");
            System.out.println();

            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","3","4","e"))); // get input
            
            // navigation based on input
            switch(input) {
                case "0":
                    System.out.println("moving to customer account login");
                    CustomerInterface.Login();
                    break;
                case "1":
                    System.out.println("moving to manager account login");
                    ManagerInterface.Login();
                    break;
                case "2":
                    System.out.println("moving to register new customer account");
                    NewCustomerInterface.Register();
                    break;
                case "3":
                    System.out.println("moving to general queries");
                    GeneralInterface.GeneralQueryPage();
                    break;
                case "4":
                    System.out.println("moving to update market info");
                    MarketInfoInterface.MarketUpdatePage();
                    break;
            }
        }

    }

    
}

