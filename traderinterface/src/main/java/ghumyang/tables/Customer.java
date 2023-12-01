package ghumyang.tables;
import lombok.Getter;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.Statement;

import ghumyang.Global;

public class Customer {

    @Getter String name;
    @Getter String state;
    @Getter String phone_number;
    @Getter String email_address;
    @Getter String tax_id;
    @Getter String username;
    @Getter String password;
    @Getter int customer_id;
    @Getter double balance;

    public Customer(String username, String password) {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT * FROM customers C WHERE C.username = '%s' AND C.password = '%s'", 
                        username, password
                    )
                )
            ) {
                resultSet.next();
                this.name = resultSet.getString("name");
                this.state = resultSet.getString("state");
                this.phone_number = resultSet.getString("phone_number");
                this.email_address = resultSet.getString("email_address");
                this.tax_id = resultSet.getString("tax_id");
                this.username = resultSet.getString("username");
                this.password = resultSet.getString("password");
                this.customer_id = Integer.parseInt(resultSet.getString("customer_id"));
                this.balance = Double.parseDouble(resultSet.getString("balance"));
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: declare Customer");
            System.exit(1);
        }
    }

    public static boolean isThereUserWithThisLogin(String username, String password) throws IOException {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT * FROM customers C WHERE C.username = '%s' AND C.password = '%s'", 
                        username, password
                    )
                )
            ) {
                if (!resultSet.next()) {
                    // no users with this information
                    return false;
                } else {
                    // at least one user with this username and password
                    return true;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: isThereUserWithThisLogin");
            System.exit(1);
        }
        return true;
    }



    // query has no constraints, we assume all caught in application
    public void deposit(double amount, boolean shouldCreateTransaction) {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "UPDATE customers C SET balance = balance + %s WHERE C.username = '%s'", 
                        String.valueOf(amount), username
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: deposit");
            System.exit(1);
        }
        if (shouldCreateTransaction) {
            createDepositWithdrawalTransaction(amount);
        }
    }
    
    // query has no constraints, we assume all caught in application
    public void withdrawal(double amount, boolean shouldCreateTransaction) {
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "UPDATE customers C SET balance = balance - %s WHERE C.username = '%s'", 
                        String.valueOf(amount), username
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: withdrawal");
            System.exit(1);
        }
        if (shouldCreateTransaction) {
            // treat it as a negative deposit
            createDepositWithdrawalTransaction(-1*amount);
        }
    }

    // helper function, only called if a deposit or a withdrawal is an actual transaction
    void createDepositWithdrawalTransaction(double amount) {

        // creates a transaction then selects the most recent transaction_id (immediate prior insertion) and adds to depositwithdraw table
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO depositwithdrawal (transaction_id, amount) VALUES (%d, %s)", 
                        Global.createTransactionAndReturnItsID(customer_id), String.valueOf(amount)
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: createDepositWithdrawalTransaction 1");
            System.exit(1);
        }
    }

    /*
     * DESIGN: 
     * query stock for price
     * withdraw that price*shares from the account
     * create transaction in buys
     * create stock account if needed otherwise update
     */
    public void buyStock(String symbol, int count) throws IOException {

        // ensure stock exists
        if (!Global.theStockExists(symbol)) {
            Global.messageWithConfirm("ERROR: there is no stock with the symbol " + symbol);
            return;
        }
        
        // query for the current stockprice
        double stockPrice = Global.getCurrentStockPrice(symbol);
        double cost = stockPrice * count + 20;

        Global.messageWithConfirm(
            new String[] {
                String.format("You are attempting to buy " + count + " shares of " + symbol + " at %1.2f per share", stockPrice),
                String.format("Total price will be %1.2f after $20 commission", cost),
                "",
                "To cancel, exit the program"
            }
        );

        // ensure that there is enough money to make this purchase
        if (cost > balance) {
            Global.messageWithConfirm("ERROR: not enough balance to make this purchase");
            return;
        }

        int transactionId = Global.createTransactionAndReturnItsID(customer_id);

        // creates a transaction then selects the most recent transaction_id (immediate prior insertion) and adds to buys table
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO buys (transaction_id, symbol, purchase_price, num_shares) VALUES (%d, '%s', %1.2f, %d)", 
                        transactionId, symbol, stockPrice, count
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: buyStock create buy relation");
            System.exit(1);
        }

        // create stock account
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "MERGE INTO stockaccounts S\n" + //
                                "USING (SELECT customer_id FROM customers WHERE customer_id = %d) C\n" + //
                                "ON (S.customer_id = C.customer_id AND S.symbol = '%s' AND S.buy_price = %1.2f)\n" + //
                                "WHEN MATCHED THEN UPDATE SET S.num_shares = S.num_shares + %d\n" + //
                                "WHEN NOT MATCHED THEN INSERT (S.customer_id, S.symbol, S.num_shares, S.buy_price) VALUES (C.customer_id, '%s', %d, %1.2f)",
                        customer_id, symbol, stockPrice, count, symbol, count, stockPrice
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: buyStock upsert stock account");
            System.exit(1);
        }

        // remove cost from account
        withdrawal(cost, false);

    }

    public void sellStock(String symbol, int count, double purchasedPrice) throws IOException {

        // ensure stock exists
        if (!Global.theStockExists(symbol)) {
            Global.messageWithConfirm("ERROR: there is no stock with the symbol " + symbol);
            return;
        }
        
        // query for the current stockprice
        double stockPrice = Global.getCurrentStockPrice(symbol);
        double gain = stockPrice * count - 20;

        Global.messageWithConfirm(
            new String[] {
                String.format("You are attempting to sell " + count + " shares of " + symbol + " at %1.2f per share", stockPrice),
                String.format("You will receive %1.2f after $20 commission", gain),
                "",
                "To cancel, exit the program"
            }
        );

        // ensure that there is enough money to pay commission
        if (20 > balance) {
            Global.messageWithConfirm("ERROR: not enough balance to pay $20 commission");
            return;
        }

        // checks stock account to make sure you're good for it
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "SELECT *\n" + //
                        "FROM stockaccounts A\n" + //
                        "WHERE A.customer_id = %d AND A.symbol = '%s' AND A.buy_price = %1.2f AND A.num_shares >= %d",
                        customer_id, symbol, purchasedPrice, count
                        )
                )
            ) {
                if (!resultSet.next()) {
                    Global.messageWithConfirm(
                        String.format(
                            "ERROR: You don't own " + count + " shares of " + symbol + " purchased at %1.2f",
                            purchasedPrice
                        )
                    );
                    return;
                }
            }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: sellStock check stockaccount");
            System.exit(1);
        }

        // update stockAccount info
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "UPDATE stockaccounts S\n" + //
                        "SET S.num_shares = S.num_shares - %d\n" + //
                        "WHERE S.customer_id = %d AND S.symbol = '%s' AND S.buy_price = %1.2f",
                        count, customer_id, symbol, purchasedPrice
                    )
                )
            ) {}
        } catch (Exception e) {
            System.out.println("FAILED QUERY: sellStock update stockaccount");
            System.exit(1);
        }

        // clear all stockAccounts where row has 0 shares bought
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    "DELETE FROM stockaccounts WHERE num_shares = 0"
                )
            ) {}
        } catch (Exception e) {
            System.out.println("FAILED QUERY: sellStock delete empty stockaccounts");
            System.exit(1);
        }

        int transactionId = Global.createTransactionAndReturnItsID(customer_id);

        // creates a transaction then selects the most recent transaction_id (immediate prior insertion) and adds to sells table
        try (Statement statement = Global.SQL.createStatement()) {
            try (
                ResultSet resultSet = statement.executeQuery(
                    String.format(
                        "INSERT INTO sells (transaction_id, symbol, purchase_price, sell_price, num_shares) VALUES (%d, '%s', %1.2f, %1.2f, %d)", 
                        transactionId, symbol, purchasedPrice, stockPrice, count
                    )
                )
            ) { }
        } catch (Exception e) {
            System.out.println("FAILED QUERY: sellStock create sell relation");
            System.exit(1);
        }

        // add cash to balance
        deposit(gain, false);

    }

    public void cancelTransaction(int transactionId) {

    }
    
}
