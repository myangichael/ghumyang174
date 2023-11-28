package ghumyang.interfaces;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import ghumyang.Global;
import ghumyang.tables.Manager;

public class ManagerInterface {
    public static void Login() throws IOException {

        String[] loginInfo = Global.getLogin(); // prompt user for login info

        if (!Manager.checkLogin(loginInfo[0], loginInfo[1])) { // check login validity for customer
            Global.clearScreen();
            System.out.println();
            System.out.println("Submitted Login Info is Invalid :("); // if invalid login info jump back
            Global.awaitConfirmation();
            return;
        }
        // ManagerAccountPage(customer);
        Manager manager = new Manager(loginInfo[0], loginInfo[1]); // if valid login info continue to account page
        ManagerAccountPage(manager);
    }

    static void ManagerAccountPage(Manager manager) throws IOException {

        String input = "start";

        while (!input.equals("e")) {

             // hard coded options
            Global.clearScreen();
            System.out.println("Welcome, " + manager.getUsername());
            System.out.println();
            System.out.println("Options:");
            System.out.println("   (0) Add Interest");
            System.out.println("   (1) Generate Monthly Statement");
            System.out.println("   (2) List all Active Customers");
            System.out.println("   (3) Generate Government Drug & Tax Evasion Report");
            System.out.println("   (4) Generate Accounts for Customer");
            System.out.println("   (5) Show balance");
            System.out.println("   (6) Delete all Transactions");
            System.out.println("   (e) Exit to main menu / Log Out");
            System.out.println();

            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","3","4","5","6","e"))); // get input

            // TODO: add switch statement to handle input
            switch (input) {
                case "0":
                    addInterest(manager);
                    break;
            }

        }
    }

    static void addInterest(Manager manager) throws IOException {
        //check if last day of month
        Calendar calendarDate = new GregorianCalendar();
        calendarDate.setTime(Global.CURRENT_DATE);
        if (calendarDate.get(Calendar.DAY_OF_MONTH)!= calendarDate.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            Global.errorMessage(new String[] {"current date is: " + Global.CURRENT_DATE.toString(), "but, last date of the month is: " + calendarDate.getActualMaximum(Calendar.DAY_OF_MONTH)});   
        }

    }

}
