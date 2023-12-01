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
            Global.updateBalanceHistory(customer.getCustomer_id());

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

        // ensure value is not negative
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
            Global.messageWithConfirm("ERROR: Sorry, the market is closed and you cannot take this action");
            return;
        }

        String title = "Buy Stock";

        // prompt for stock ticker, shares to buy
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Symbol","Count")));

        // input validation
        if (!Global.isDouble(fields.get("Count"))) {
            Global.messageWithConfirm("ERROR: inputted count is invalid, should be an DOUBLE");
            return;
        }
        if (fields.get("Symbol").equals("")) {
            Global.messageWithConfirm("ERROR: Symbol is empty");
            return;
        }

        String symbol = fields.get("Symbol");
        double count = Double.parseDouble(fields.get(("Count")));

        // can't buy negative or 0 shares
        if (count <= 0) {
            Global.messageWithConfirm("ERROR: can't buy negative or 0 shares");
            return;
        }
        
        // buy query
        customer.buyStock(symbol, count);
    }

    static void sellStock(Customer customer) throws IOException {

        // ensure market is open
        if (!Global.MARKET_IS_OPEN) {
            Global.messageWithConfirm("ERROR: Sorry, the market is closed and you cannot take this action");
            return;
        }

        String title = "Sell Stock";

        // prompt for stock ticker, shares to sell
        LinkedHashMap<String,String> fields = Global.promptValues(title, new ArrayList<>(Arrays.asList("Symbol","Count","Purchased Price")));

        // input validation
        if (!Global.isDouble(fields.get("Count"))) {
            Global.messageWithConfirm("ERROR: inputted count is invalid, should be an DOUBLE");
            return;
        }
        if (!Global.isDouble(fields.get("Purchased Price"))) {
            Global.messageWithConfirm("ERROR: inputted purchase price is invalid, should be a DOUBLE");
            return;
        }
        if (fields.get("Symbol").equals("")) {
            Global.messageWithConfirm("ERROR: Symbol is empty");
            return;
        }

        String symbol = fields.get("Symbol");
        double count = Double.parseDouble(fields.get(("Count")));
        double purchasedPrice = Double.parseDouble(fields.get("Purchased Price"));

        // can't sell negative or 0 shares
        if (count <= 0) {
            Global.messageWithConfirm("ERROR: can't sell negative or 0 shares");
            return;
        }

        // sell query
        customer.sellStock(symbol, count, purchasedPrice);
    }

    static void cancelTransaction(Customer customer) throws IOException {
        Global.messageWithConfirm("You are now attempting to cancel the most recent buy or sell transaction");
        customer.cancelTransaction();
    }

    static void showBalance(Customer customer) throws IOException {

        // prints balance across all stock accounts belonging to this market account from query
        System.out.println("YOUR BALANCE IS: " + customer.getBalance());

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
                        customer.getCustomer_id()
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

                // uses helper function to output properly formatted table values
                String[] output = Global.tableToString(headers, values);

                Global.messageWithConfirm("Your user balance", output);

            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: showBalance");
            System.exit(1);
        }
    }

    static void monthTransactionHistory(Customer customer) throws IOException {
        // prints all transactions of this month from query

        // treemap will stores queries and sort them in order by transaction_id (which is chronological order)
        TreeMap<Integer, String> queries = new TreeMap<>();

        // start and end of current month
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(Global.CURRENT_DATE);

        calendar.set(Calendar.DAY_OF_MONTH,1);
        Date startDate = new Date(calendar.getTimeInMillis());
        calendar.set(Calendar.DAY_OF_MONTH,calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date endDate = new Date(calendar.getTimeInMillis());

        String customer_id = Integer.toString(customer.getCustomer_id());

        String message = "";
        message = "This current month's transaction history is listed below: ";
        queries.put(-1, message);
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
                    message = "Date: " + resultSet.getDate("xdate").toString() + " | Deposit/Withdrawal   | Amount: " + String.format("%18d", Integer.parseInt(resultSet.getString("amount")));
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
                    message = "Date: " + resultSet.getDate("xdate").toString() + " | Buy    | Symbol: " + resultSet.getString("symbol")
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
                    message = "Date: " + resultSet.getDate("xdate").toString() + " | Sell   | Symbol: " + resultSet.getString("symbol")
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
                        "SELECT T.transaction_id AS tid, A.amount as amount, T.transaction_date AS xdate\n" + //
                        "FROM transactions T INNER JOIN accrueinterests A ON T.transaction_id=A.transaction_id\n" + //
                        "WHERE (T.transaction_date >= TO_DATE ('%s', 'YYYY/MM/DD')) AND (T.transaction_date <= TO_DATE ('%s', 'YYYY/MM/DD')) AND T.customer_id='%s'", 
                        startDate, endDate, customer_id
                    )
                )
            ) {
                while (resultSet.next()) {
                    message = "Date: " + resultSet.getDate("xdate").toString() + " | Accrue Interest      | Amount: " + String.format("%18.2f", resultSet.getDouble("amount"));
                    queries.put(Integer.parseInt(resultSet.getString("tid")), message);
                }
            } catch(Exception e) {
                System.out.println("FAILED QUERY: interest");
                e.printStackTrace();
                System.exit(1);
            }

            ArrayList<String> transactionList = new ArrayList<>();

            Map.Entry<Integer,String> temp = queries.pollFirstEntry();
            String bonus = temp.getValue(); 
            for (Map.Entry<Integer,String> entry : queries.entrySet()) {
                message = entry.getKey() + " : " + entry.getValue();
                transactionList.add(message);
            }

            // assign transactionlist to String[] for message method
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

    static void displayInfo(Customer customer) throws IOException {
        // prints below info, info is already loaded for this object
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
