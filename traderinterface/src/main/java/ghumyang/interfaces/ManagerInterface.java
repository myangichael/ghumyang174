package ghumyang.interfaces;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

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
        // TODO
    }

    static void generateMonthlyStatement() throws IOException {
        TreeMap<Integer, String> queries = new TreeMap<>();

        // start and end of current month
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(Global.CURRENT_DATE);

        calendar.set(Calendar.DAY_OF_MONTH,1);
        Date startDate = new Date(calendar.getTimeInMillis());
        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = new Date(calendar.getTimeInMillis());


        String title = "Monthly Statement";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Customer ID")));
        String customer_id = fields.get("Customer ID");

        // String customer_id = "1";

        String message = "";
        try (Statement statement = Global.SQL.createStatement()) {
            // gets and records the customers information
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT name, email FROM Customers C WHERE C.customer_id='%s'", 
                        customer_id
                    )
                )
            ) {
                if (!resultSet.next()) {
                    // no customer with the provided id
                    Global.messageWithConfirm("ERROR: there is no customer with the id " + customer_id);
                    return;
                } else {
                    // customer exists
                    message = "Customer: " + resultSet.getString("name") + " | Email: " + resultSet.getString("email_address");
                    queries.put(-1, message);
                }
            }

            // gets and records the withdraw/deposit transactions for this customer
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT T.Date AS date, D.amount AS amount\n" + //
                        "FROM transactions T INNER JOIN depositwithdrawal D ON T.transaction_id=D.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id='%s'", 
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Transaction Type: Deposit/Withdrawal, " + "Date: " + resultSet.getString("date") + ", Amount: " + resultSet.getString("amount");
                    queries.put(Integer.parseInt(resultSet.getString("customer_id")), message);
                }
            }

            // gets and records the buy transactions for this customer
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT T.date AS date, B.symbol AS symbol, B.purchase_price AS purchase_price, B.num_shares AS num_shares\n" + //
                        "FROM transactions T INNER JOIN buys B ON T.transaction_id=B.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id='%s'", 
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Transaction Type: Buy, " + "Date: " + resultSet.getString("date") + ", Symbol: " + resultSet.getString("symbol")
                    + ", Purchase Price: " + resultSet.getString("purchase_price") + ", Number of Shares " + resultSet.getString("num_shares");
                    queries.put(Integer.parseInt(resultSet.getString("customer_id")), message);
                }
            }

            // gets and records the sell transactions for this customer
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT T.date AS date, B.symbol AS symbol, B.purchase_price AS purchase_price, B.sell_price AS sell_price, B.num_shares AS num_shares\n" + //
                        "FROM transactions T INNER JOIN sells S ON T.transaction_id=S.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id='%s'", 
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Transaction Type: Sell, " + "Date: " + resultSet.getString("date") + ", Symbol: " + resultSet.getString("symbol")
                    + ", Purchase Price: " + resultSet.getString("purchase_price") + ", Sell Price: " + resultSet.getString("sell_price")
                    + ", Number of Shares " + resultSet.getString("num_shares");
                    queries.put(Integer.parseInt(resultSet.getString("customer_id")), message);
                }
            }

            // gets and records the cancel transactions for this customer
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT T.date AS date, C.transaction_canceled AS tc\n" + //
                        "FROM transactions T INNER JOIN cancels C ON T.transaction_id=C.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id='%s'", 
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Transaction Type: Cancel, " + "Date: " + resultSet.getString("date") + ", Transaction Cancelled: " + resultSet.getString("tc");
                    queries.put(Integer.parseInt(resultSet.getString("customer_id")), message);
                }
            }

            // gets and records the accrueinterests transactions for this customer
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT T.date AS date\n" + //
                        "FROM transactions T INNER JOIN accrueinterests A ON T.transaction_id=A.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id='%s'", 
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Transaction Type: Cancel, " + "Date: " + resultSet.getString("date");
                    queries.put(Integer.parseInt(resultSet.getString("customer_id")), message);
                }
            }

            ArrayList<String> transactionList = new ArrayList<>();

            Map.Entry<Integer,String> temp = queries.pollFirstEntry();
            String bonus = temp.getKey() + " : " + temp.getValue(); // TODO: shouldn't be an error of empty treemap, since no customer is caught earlier, just check
            for (Map.Entry<Integer,String> entry : queries.entrySet()) {
                message = entry.getKey() + " : " + entry.getValue();
                transactionList.add(message);
            }

            int[] a = new int[2];
            String[] messageArray = new String[transactionList.size()];

            for (int i = 0; i < messageArray.length; i++) {
                messageArray[i] = transactionList.get(i);
            }

            Global.messageWithConfirm(bonus, messageArray);

        } catch (Exception e) {
            System.out.println("FAILED QUERY: generateMonthlyStatement");
            System.exit(1);
        }



    }

    static void listActiveCustomers() throws IOException {
        // TODO
    }

    static void generateGDTEReport() throws IOException {
        // TODO
    }

    static void generateCustomerReport() throws IOException {
        String title = "Customer Report";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Username")));
        if (fields.get("Username").equals("")) {
            Global.messageWithConfirm("ERROR: inputted username is empty");
            return;
        }
        // TODO
    }

    static void deleteAllTransactions() throws IOException {
        // TODO
    }

}
