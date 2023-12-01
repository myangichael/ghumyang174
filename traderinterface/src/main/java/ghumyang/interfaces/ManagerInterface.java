package ghumyang.interfaces;

import java.io.IOException;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

        if (!Manager.isThereManagerWithThisLogin(loginInfo[0], loginInfo[1])) {
            Global.messageWithConfirm("ERROR: manager login info is invalid :(");
            return;
        }
 
        // if valid login info continue to account page
        ManagerAccountPage(new Manager(loginInfo[0], loginInfo[1]));
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

        // get desired start and end date
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(Global.CURRENT_DATE);
        calendar.set(Calendar.DAY_OF_MONTH,1);
        LocalDate startDate = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()).toLocalDate();
        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        LocalDate endDate = LocalDateTime.ofInstant(calendar.toInstant(), ZoneId.systemDefault()).toLocalDate();

        Global.messageWithConfirm("Start Date: " + startDate.toString() + ", End Date: " + endDate.toString());

        ArrayList<Integer> customerIDList = new ArrayList<>();

        // get list of all customer ids
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT customer_id FROM customers C"
                )
            ) {
                while (resultSet.next()) {
                    int local_id = Integer.parseInt(resultSet.getString("customer_id"));
                    customerIDList.add(local_id);
                    // simultaneously update their balance history for this day (last of month)
                    Global.updateBalanceHistory(local_id);
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: addInterest get customer list");
            System.exit(1);
        }

        // for each customer,
        for (int customer_id : customerIDList) {

            ArrayList<LocalDate> dateList = new ArrayList<>();
            ArrayList<Double> balanceList = new ArrayList<>();

            // query for the last value before this month started, defaults to 0 if did not exist
            double balancePriorToStartOfMonth = 0;
            try (Statement statement = Global.SQL.createStatement()) {
                try (
                    ResultSet resultSet = statement.executeQuery(
                        "SELECT B.balance\n" + //
                                "FROM balancehistories B\n" + //
                                "WHERE B.record_date IN (\n" + //
                                "    SELECT MAX(record_date)\n" + //
                                "    FROM balancehistories C\n" + //
                                "    WHERE C.record_date < TO_DATE('"+startDate.toString()+"', 'YYYY-MM-DD')\n" + //
                                "    AND C.customer_id = "+customer_id+"\n" + //
                                ") AND B.customer_id = "+customer_id
                    )
                ) {
                    while (resultSet.next()) {
                        balancePriorToStartOfMonth = Double.parseDouble(resultSet.getString("balance"));
                    }
                }
            } catch (Exception e) {
                System.out.println("FAILED QUERY: addInterest get balance prior to start of month");
                System.exit(1);
            }

            // query for a list of all dates and balances in the time period
            try (Statement statement = Global.SQL.createStatement()) {
                try (
                    ResultSet resultSet = statement.executeQuery(
                        "SELECT balance, TO_CHAR(record_date,'YYYY-MM-DD') AS record_date \n" + //
                                "FROM balancehistories\n" + //
                                "WHERE customer_id = "+customer_id+" AND record_date >= TO_DATE('" +startDate.toString()+ "', 'YYYY-MM-DD') AND record_date <= TO_DATE('" +endDate.toString()+"', 'YYYY-MM-DD')\n" + //
                                "ORDER BY record_date ASC"
                    )
                ) {
                    while (resultSet.next()) {
                        dateList.add(LocalDate.parse(resultSet.getString("record_date")));
                        balanceList.add(Double.parseDouble(resultSet.getString("balance")));
                    }
                }
            } catch (Exception e) {
                System.out.println("FAILED QUERY: addInterest list of dates and balances for this person");
                System.exit(1);
            }

            // Global.messageWithConfirm("balance history for " + customer_id,new String[] {"beforeMonthBalance: " + balancePriorToStartOfMonth, dateList.toString(), balanceList.toString()});

            // calculates amount of interest to add:

            double curBalance = balancePriorToStartOfMonth;
            double totalBalance = 0;
            int index = 0;
            int days = 0;
            int totalDays = 0;
            LocalDate dateIterator;

            for (dateIterator = startDate; dateIterator.isBefore(endDate) || dateIterator.isEqual(endDate); dateIterator = dateIterator.plusDays(1)) {
                if (index < dateList.size() && dateIterator.isEqual(dateList.get(index))) {
                    // Global.messageWithConfirm("hit match on day " + dateIterator.toString() + ", " + days + " have elapsed prior to this with balance " + curBalance);
                    // add to count
                    totalBalance += curBalance * days;
                    curBalance = balanceList.get(index);
                    index++;
                    days = 0;
                }
                days++;
                totalDays++;
            }
            totalBalance += curBalance * days;
            // Global.messageWithConfirm("ended month at " + dateIterator.toString() + ", " + days + " have elapsed prior to this with balance " + curBalance);

            Double toDeposit = (totalBalance / totalDays) * 0.02;

            // Global.messageWithConfirm(new String[] {
            //     "totalDays:  " + totalDays,
            //     "averageBalance: " + toDeposit
            // });
            
            // deposits the value into the account
            try (Statement statement = Global.SQL.createStatement()) {
                try (
                    ResultSet resultSet = statement.executeQuery(
                        String.format(
                            "UPDATE customers C SET balance = balance + %1.2f WHERE C.customer_id = %d", 
                            toDeposit, customer_id
                        )
                    )
                ) { }
            } catch (Exception e) {
                System.out.println("FAILED QUERY: addInterest deposit the value");
                System.exit(1);
            }

            // create the accrue value transaction
            int transaction_id = Global.createTransactionAndReturnItsID(customer_id);

            // creates a transaction then selects the most recent transaction_id (immediate prior insertion) and adds to accrue interest
            try (Statement statement = Global.SQL.createStatement()) {
                try (
                    ResultSet resultSet = statement.executeQuery(
                        "INSERT INTO accrueinterests (transaction_id) VALUES ("+transaction_id+")"
                    )
                ) { }
            } catch (Exception e) {
                System.out.println("FAILED QUERY: addInterest create transaction");
                System.exit(1);
            }

            // now update balance history for today
            Global.updateBalanceHistory(customer_id);

        }
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

        // prompt for user id
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Customer ID")));
        String customer_id = fields.get("Customer ID");

        // validate input
        if (!Global.isInteger(customer_id)) {
            Global.messageWithConfirm("ERROR: customer id is an invalid integer");
            return;
        }

        String message = "";
        try (Statement statement = Global.SQL.createStatement()) {
            // gets and records the withdraw/deposit transactions for this customer
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT T.transaction_id AS tid, T.transaction_date AS xdate, D.amount AS amount\n" + //
                        "FROM transactions T INNER JOIN depositwithdrawal D ON T.transaction_id=D.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id=%s",
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Date: " + resultSet.getDate("xdate").toString() + " | Deposit/Withdrawal | Amount: " + String.format("%18d", Integer.parseInt(resultSet.getString("amount")));
                    queries.put(Integer.parseInt(resultSet.getString("tid")), message);
                }
            } catch(Exception e) {
                System.out.println("FAILED QUERY: withdrawal/deposit");
                e.printStackTrace();
                System.exit(1);
            }

            // gets and records the buy transactions for this customer
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT T.transaction_id AS tid, T.transaction_date AS xdate, B.symbol AS symbol, B.purchase_price AS purchase_price, B.num_shares AS num_shares\n" + //
                        "FROM transactions T INNER JOIN buys B ON T.transaction_id=B.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id=%s", 
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Date: " + resultSet.getDate("xdate").toString() + " | Buy  | Symbol: " + resultSet.getString("symbol")
                    + " | Purchase Price: " + String.format("%10.2f", Double.parseDouble(resultSet.getString("purchase_price"))) + " | Number of Shares: " + resultSet.getString("num_shares");
                    queries.put(Integer.parseInt(resultSet.getString("tid")), message);
                }
            } catch(Exception e) {
                System.out.println("FAILED QUERY: buy");
                e.printStackTrace();
                System.exit(1);
            }

            // gets and records the sell transactions for this customer
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT T.transaction_id AS tid, T.transaction_date AS xdate, S.symbol AS symbol, S.purchase_price AS purchase_price, S.sell_price AS sell_price, S.num_shares AS num_shares\n" + //
                        "FROM transactions T INNER JOIN sells S ON T.transaction_id=S.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id='%s'", 
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Date: " + resultSet.getDate("xdate").toString() + " | Sell | Symbol: " + resultSet.getString("symbol")
                    + " | Purchase Price: " + String.format("%10.2f", Double.parseDouble(resultSet.getString("purchase_price"))) + " | Sell Price: " + resultSet.getString("sell_price")
                    + " | Number of Shares: " + resultSet.getString("num_shares");
                    queries.put(Integer.parseInt(resultSet.getString("tid")), message);
                }
            } catch(Exception e) {
                System.out.println("FAILED QUERY: sell");
                e.printStackTrace();
                System.exit(1);
            }

            // gets and records the cancel transactions for this customer
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT T.transaction_id AS tid, T.transaction_date AS xdate, C.transaction_canceled AS tc\n" + //
                        "FROM transactions T INNER JOIN cancels C ON T.transaction_id=C.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id='%s'", 
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Date: " + resultSet.getDate("xdate").toString() + " | Cancel | CancelID: " + resultSet.getString("tc");
                    queries.put(Integer.parseInt(resultSet.getString("tid")), message);
                }
            } catch(Exception e) {
                System.out.println("FAILED QUERY: cancel");
                e.printStackTrace();
                System.exit(1);
            }

            // gets and records the accrueinterests transactions for this customer
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT T.transaction_id AS tid, T.transaction_date AS xdate\n" + //
                        "FROM transactions T INNER JOIN accrueinterests A ON T.transaction_id=A.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id='%s'", 
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Date: " + resultSet.getDate("xdate").toString() + " | Accrue Interest ";
                    queries.put(Integer.parseInt(resultSet.getString("tid")), message);
                }
            } catch(Exception e) {
                System.out.println("FAILED QUERY: interest");
                e.printStackTrace();
                System.exit(1);
            }

            ArrayList<String> transactionList = new ArrayList<>();
            
            String username = "null";

            try (
                ResultSet resultSet = statement.executeQuery(
                    "SELECT username FROM customers C WHERE C.customer_id = " + customer_id
                )
            ) {
                while (resultSet.next()) {
                    username = resultSet.getString("username");
                }
            } catch(Exception e) {
                System.out.println("FAILED QUERY: get username");
                e.printStackTrace();
                System.exit(1);
            }

            String bonus = "Monthly Report for: " + username;

            for (Map.Entry<Integer,String> entry : queries.entrySet()) {
                message = entry.getKey() + " : " + entry.getValue();
                transactionList.add(message);
            }

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
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(Global.CURRENT_DATE);

        calendar.set(Calendar.DAY_OF_MONTH,1);
        Date startDate = new Date(calendar.getTimeInMillis());
        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = new Date(calendar.getTimeInMillis());

        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT C.name as Name, C.username as Username, C.phone_number as Phone_Number, C.email_address as Email_Address, buysell.sum as Num_Shares\n" + //
                        "FROM (\n" + //
                        "    SELECT buysell.cid, SUM(num_shares) AS sum\n" + //
                        "    FROM (\n" + //
                        "        SELECT T.customer_id AS cid, T.transaction_date AS tdate, B.num_shares as num_shares\n" + //
                        "        FROM transactions T INNER JOIN buys B ON T.transaction_id=B.transaction_id\n" + //
                        "        WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD'))\n" + //
                        "        UNION ALL\n" + //
                        "        SELECT T.customer_id AS cid, T.transaction_date AS tdate, S.num_shares as num_shares\n" + //
                        "        FROM transactions T INNER JOIN sells S ON T.transaction_id=S.transaction_id\n" + //
                        "        WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD'))\n" + //
                        "    ) buysell\n" + //
                        "    GROUP BY buysell.cid\n" + //
                        "    HAVING SUM(buysell.num_shares) > 1000\n" + //
                        ") buysell, Customers C\n" + //
                        "WHERE C.customer_id=buysell.cid",
                        startDate, endDate, startDate, endDate
                    )
                )
            ) {
                // store headers for output
                ArrayList<String> headers = new ArrayList<>();
                headers.add("Name");
                headers.add("Username");
                headers.add("Phone Number");
                headers.add("Email Address");
                headers.add("Shares Traded in the Last Month");

                ArrayList<String> name = new ArrayList<>();
                ArrayList<String> username = new ArrayList<>();
                ArrayList<String> phoneNumber = new ArrayList<>();
                ArrayList<String> emailAddress = new ArrayList<>();
                ArrayList<String> numberOfShares = new ArrayList<>();

                // adding results to array for output process
                while (resultSet.next()) {
                    name.add(resultSet.getString("Name"));
                    username.add(resultSet.getString("Username"));
                    phoneNumber.add(resultSet.getString("Phone_Number"));
                    emailAddress.add(resultSet.getString("Email_Address"));
                    numberOfShares.add(resultSet.getString("Num_Shares"));
                }

                if (name.size() == 0) {
                    Global.messageWithConfirm("No active customers");
                    return;
                }

                // create large array for output process
                ArrayList<ArrayList<String>> values = new ArrayList<>();
                values.add(name);
                values.add(username);
                values.add(phoneNumber);
                values.add(emailAddress);
                values.add(numberOfShares);

                String[] output = Global.tableToString(headers, values);

                Global.messageWithConfirm("Active Customers", output);

            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: activeCustomers");
            System.exit(1);
        }


    }

    static void generateGDTEReport() throws IOException {
        // check if last day of month
        Calendar calendarDate = new GregorianCalendar();
        calendarDate.setTime(Global.CURRENT_DATE);
        if (calendarDate.get(Calendar.DAY_OF_MONTH) != calendarDate.getActualMaximum(Calendar.DAY_OF_MONTH)) {
            Global.messageWithConfirm(new String[]
                {
                    "Current date is: " + Global.CURRENT_DATE.toString(), 
                    "The last day of the month is: " + calendarDate.getActualMaximum(Calendar.DAY_OF_MONTH),
                    "The month is not over yet. Please wait to generate the DTER"
                }
            ); 
            return;
        }

        Calendar calendar = new GregorianCalendar();
        calendar.setTime(Global.CURRENT_DATE);

        calendar.set(Calendar.DAY_OF_MONTH,1);
        Date startDate = new Date(calendar.getTimeInMillis());
        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = new Date(calendar.getTimeInMillis());


        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT random.cid AS cid, C.username AS username, C.name AS name, C.email_address AS email_address, random.net_profit AS net_profit\n" + //
                                "FROM (\n" + //
                                "    SELECT finalTable.cid AS cid, SUM(finalTable.profit) AS net_profit\n" + //
                                "    FROM (\n" + //
                                "        SELECT T.customer_id AS cid, SUM(S.num_shares * (S.sell_price - S.purchase_price)) AS profit\n" + //
                                "        FROM transactions T INNER JOIN sells S ON T.transaction_id=S.transaction_id\n" + //
                                "        WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD'))\n" + //
                                "        GROUP BY T.customer_id\n" + //
                                "    \n" + //
                                "        UNION ALL\n" + //
                                "    \n" + //
                                "        SELECT temp.cid AS cid, SUM(-20*temp.num_transactions) AS profit --this profit is considered\n" + //
                                "            FROM (\n" + //
                                "                SELECT T.customer_id AS cid, COUNT(T.transaction_id) AS num_transactions\n" + //
                                "                FROM transactions T INNER JOIN buys B ON T.transaction_id=B.transaction_id\n" + //
                                "                WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD'))\n" + //
                                "                GROUP BY T.customer_id\n" + //
                                "                UNION ALL\n" + //
                                "                SELECT T.customer_id AS cid, COUNT(T.transaction_id) AS num_transactions\n" + //
                                "                FROM transactions T INNER JOIN sells S ON T.transaction_id=S.transaction_id\n" + //
                                "                WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD'))\n" + //
                                "                GROUP BY T.customer_id\n" + //
                                "            ) temp\n" + //
                                "            GROUP BY temp.cid\n" + //
                                "    ) finalTable\n" + //
                                "    GROUP BY finalTable.cid\n" + //
                                "    HAVING SUM(finalTable.profit) > 10000\n" + //
                                ") random INNER JOIN Customers C ON random.cid=C.customer_id",
                                startDate, endDate, startDate, endDate, startDate, endDate
                    )
                )
            ) {
                // store headers for output
                ArrayList<String> headers = new ArrayList<>();
                headers.add("CID");
                headers.add("username");
                headers.add("name");
                headers.add("email_address");
                headers.add("net_profit");
        
                ArrayList<String> CIDs = new ArrayList<>();
                ArrayList<String> usernames = new ArrayList<>();
                ArrayList<String> names = new ArrayList<>();
                ArrayList<String> emails = new ArrayList<>();
                ArrayList<String> profits = new ArrayList<>();
        
                // adding results to array for output process
                while (resultSet.next()) {
                    CIDs.add(resultSet.getString("CID"));
                    usernames.add(resultSet.getString("username"));
                    names.add(resultSet.getString("name"));
                    emails.add(resultSet.getString("email_address"));
                    profits.add(resultSet.getString("net_profit"));
                }
        
                if (CIDs.size() == 0) {
                    Global.messageWithConfirm("No customers who made more than $10,000 this month");
                    return;
                }
        
                // create large array for output process
                ArrayList<ArrayList<String>> values = new ArrayList<>();
                values.add(CIDs);
                values.add(usernames);
                values.add(names);
                values.add(emails);
                values.add(profits);
        
                String[] output = Global.tableToString(headers, values);

                String bonus = "Accounts that made more than $10,000 this month ";
        
                Global.messageWithConfirm(bonus, output);
        
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: generateCustomerReport");
            System.exit(1);
        }



    }

    static void generateCustomerReport() throws IOException {

        // prompt for user id
        String title = "Customer Report";
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Customer ID")));
        
        // validate input
        if (!Global.isInteger(fields.get("Customer ID"))) {
            Global.messageWithConfirm("ERROR: customer id is an invalid integer");
            return;
        }

        int customer_id = Integer.parseInt(fields.get("Customer ID"));

        // prints balance across all stock accounts belonging to this market account from query
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT \n" + //
                        "    A.symbol,\n" + //
                        "    A.num_shares,\n" + //
                        "    A.buy_price,\n" + //
                        "    S.current_price,\n" + //
                        "    A.num_shares * S.current_price AS current_value,\n" + //
                        "    (A.num_shares * S.current_price) - (A.num_shares * A.buy_price) AS change\n" + //
                        "FROM \n" + //
                        "    stockaccounts A\n" + //
                        "INNER JOIN \n" + //
                        "    stocks S ON A.symbol = S.symbol\n" + //
                        "WHERE \n" + //
                        "    A.customer_id = %d", 
                        customer_id
                    )
                )
            ) {
                // store headers for output
                ArrayList<String> headers = new ArrayList<>();
                headers.add("Symbol");
                headers.add("Shares");
                headers.add("Buy Price");
                headers.add("Current Price");
                headers.add("Value");
                headers.add("Change");
        
                ArrayList<String> symbols = new ArrayList<>();
                ArrayList<String> shareCounts = new ArrayList<>();
                ArrayList<String> buyPrices = new ArrayList<>();
                ArrayList<String> curPrices = new ArrayList<>();
                ArrayList<String> curValues = new ArrayList<>();
                ArrayList<String> changes = new ArrayList<>();
        
                // adding results to array for output process
                while (resultSet.next()) {
                    symbols.add(resultSet.getString("symbol"));
                    shareCounts.add(resultSet.getString("num_shares"));
                    buyPrices.add(resultSet.getString("buy_price"));
                    curPrices.add(resultSet.getString("current_price"));
                    curValues.add(resultSet.getString("current_value"));
                    changes.add(resultSet.getString("change"));
                }
        
                if (symbols.size() == 0) {
                    Global.messageWithConfirm("No stocks in your balance");
                    return;
                }
        
                // create large array for output process
                ArrayList<ArrayList<String>> values = new ArrayList<>();
                values.add(symbols);
                values.add(shareCounts);
                values.add(buyPrices);
                values.add(curPrices);
                values.add(curValues);
                values.add(changes);
        
                String[] output = Global.tableToString(headers, values);

                String username = "null";

                try (
                    ResultSet secondResultSet = statement.executeQuery(
                        "SELECT username FROM customers C WHERE C.customer_id = " + customer_id
                    )
                ) {
                    while (secondResultSet.next()) {
                        username = secondResultSet.getString("username");
                    }
                } catch(Exception e) {
                    System.out.println("FAILED QUERY: get username");
                    e.printStackTrace();
                    System.exit(1);
                }

                String bonus = "Accounts Owned By " + username;
        
                Global.messageWithConfirm(bonus, output);
        
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: generateCustomerReport");
            System.exit(1);
        }
                   
                
    }

    static void deleteAllTransactions() throws IOException {
        // can only be done when market is closed
        if (Global.MARKET_IS_OPEN) {
            Global.messageWithConfirm("ERROR: market is still open");
            return;
        }
        // can only be done on the first of a month
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
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "DELETE FROM transactions"
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: deleteAllTransactions");
            System.exit(1);
        }
    }

}
