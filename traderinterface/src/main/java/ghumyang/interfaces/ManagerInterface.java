package ghumyang.interfaces;

import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;

import ghumyang.Global;
import ghumyang.tables.Manager;

public class ManagerInterface {
    public static void Login() throws IOException {

        String[] loginInfo = Global.getLogin(); // prompt user for login info

        if (!Manager.checkLogin(loginInfo[0], loginInfo[1])) { // check login validity for customer
            Global.messageWithConfirm("ERROR: manager login info is invalid :(");
            return;
        }

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
            System.out.println("   (5) Delete all Transactions");
            System.out.println("   (e) Exit to main menu / Log Out");
            System.out.println();

            input = Global.getLineSetInputs(new ArrayList<>(Arrays.asList("0","1","2","3","4","5","e"))); // get input

            // TODO: add switch statement to handle input
            switch (input) {
                case "0":
                    addInterest();
                    break;
                case "1":
                    generateMonthlyStatement();
                    break;
                case "2":
                    listActiveCustomers();
                    break;
                case "3":
                    generateGDTEReport();
                    break;
                case "4":
                    generateCustomerReport();
                    break;
                case "5":
                    deleteAllTransactions();
                    break;
            }

        }
    }

    static void addInterest() throws IOException {
        // check if last day of month
        Calendar calendarDate = new GregorianCalendar();
        calendarDate.setTime(Global.CURRENT_DATE);
        if (calendarDate.get(Calendar.DAY_OF_MONTH) != calendarDate.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            Global.messageWithConfirm(new String[]
                {
                    "Current date is: " + Global.CURRENT_DATE.toString(), 
                    "The last day of the month is: " + calendarDate.getActualMaximum(Calendar.DAY_OF_MONTH),
                    "You cannot add interest today."
                }
            );   
            return;
        }
        
    }

    static void generateMonthlyStatement() throws IOException {

    }

    static void listActiveCustomers() throws IOException {
        
    }

    static void generateGDTEReport() throws IOException {
        
    }

    static void generateCustomerReport() throws IOException {
        String title = "Customer Report";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Username")));
        if (fields.get("Username").equals("")) {
            Global.messageWithConfirm("ERROR: inputted username is empty");
            return;
        }
    }

    static void deleteAllTransactions() throws IOException {

    }

}
